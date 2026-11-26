package persistencia;

import excepciones.DAOException;
import excepciones.PaqueLibreException;
import modelo.CambioEstado;
import modelo.Envio;
import modelo.TipoEnvio;
import patron.EstadoFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class EnvioDAOJDBC implements EnvioDAO {

    @Override
    public void insertar(Envio e) throws DAOException {
        String sql = "INSERT INTO envio (guia,tipo,remitente,destinatario,destino,peso_real,largo,ancho,alto,"
                + "valor_declarado,estado) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection c = Conexion.obtener()) {
            c.setAutoCommit(false);
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, e.getGuia());
                ps.setString(2, e.getTipo().name());
                ps.setString(3, e.getRemitente());
                ps.setString(4, e.getDestinatario());
                ps.setString(5, e.getDestino());
                ps.setDouble(6, e.getPesoReal());
                ps.setDouble(7, e.getLargo());
                ps.setDouble(8, e.getAncho());
                ps.setDouble(9, e.getAlto());
                ps.setDouble(10, e.getValorDeclarado());
                ps.setString(11, e.getEstadoNombre());
                ps.executeUpdate();
            }
            guardarHistorial(c, e);
            c.commit();
        } catch (SQLException ex) {
            throw new DAOException("No se pudo guardar el envío (¿la guía ya existe?): " + ex.getMessage(), ex);
        }
    }

    @Override
    public void actualizar(Envio e) throws DAOException {
        String sql = "UPDATE envio SET remitente=?,destinatario=?,destino=?,peso_real=?,largo=?,ancho=?,alto=?,"
                + "valor_declarado=?,estado=? WHERE guia=?";
        try (Connection c = Conexion.obtener()) {
            c.setAutoCommit(false);
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, e.getRemitente());
                ps.setString(2, e.getDestinatario());
                ps.setString(3, e.getDestino());
                ps.setDouble(4, e.getPesoReal());
                ps.setDouble(5, e.getLargo());
                ps.setDouble(6, e.getAncho());
                ps.setDouble(7, e.getAlto());
                ps.setDouble(8, e.getValorDeclarado());
                ps.setString(9, e.getEstadoNombre());
                ps.setString(10, e.getGuia());
                ps.executeUpdate();
            }
            guardarHistorial(c, e);
            c.commit();
        } catch (SQLException ex) {
            throw new DAOException("No se pudo actualizar el envío: " + ex.getMessage(), ex);
        }
    }

    /** Reescribe el historial del envío (está completo en memoria). */
    private void guardarHistorial(Connection c, Envio e) throws SQLException {
        try (PreparedStatement del = c.prepareStatement("DELETE FROM historial_estado WHERE guia=?")) {
            del.setString(1, e.getGuia());
            del.executeUpdate();
        }
        try (PreparedStatement ins = c.prepareStatement(
                "INSERT INTO historial_estado (guia,estado,fecha_hora) VALUES (?,?,?)")) {
            for (CambioEstado ce : e.getHistorial()) {
                ins.setString(1, e.getGuia());
                ins.setString(2, ce.estado());
                ins.setString(3, ce.fecha().toString());
                ins.executeUpdate();
            }
        }
    }

    @Override
    public void eliminar(String guia) throws DAOException {
        try (Connection c = Conexion.obtener()) {
            c.setAutoCommit(false);
            try (PreparedStatement h = c.prepareStatement("DELETE FROM historial_estado WHERE guia=?");
                 PreparedStatement e = c.prepareStatement("DELETE FROM envio WHERE guia=?")) {
                h.setString(1, guia);
                h.executeUpdate();
                e.setString(1, guia);
                e.executeUpdate();
            }
            c.commit();
        } catch (SQLException ex) {
            throw new DAOException("No se pudo eliminar el envío: " + ex.getMessage(), ex);
        }
    }

    @Override
    public List<Envio> listar() throws DAOException {
        List<Envio> lista = new ArrayList<>();
        try (Connection c = Conexion.obtener();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM envio ORDER BY guia");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs, c));
        } catch (SQLException ex) {
            throw new DAOException("No se pudo listar los envíos: " + ex.getMessage(), ex);
        }
        return lista;
    }

    @Override
    public Envio buscar(String guia) throws DAOException {
        try (Connection c = Conexion.obtener();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM envio WHERE guia=?")) {
            ps.setString(1, guia);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs, c) : null;
            }
        } catch (SQLException ex) {
            throw new DAOException("No se pudo buscar el envío: " + ex.getMessage(), ex);
        }
    }

    /** R3: reconstruye la subclase correcta a través de la fábrica TipoEnvio. */
    private Envio mapear(ResultSet rs, Connection c) throws SQLException, DAOException {
        try {
            Envio e = TipoEnvio.valueOf(rs.getString("tipo")).crear(
                    rs.getString("guia"), rs.getString("remitente"), rs.getString("destinatario"),
                    rs.getString("destino"), rs.getDouble("peso_real"), rs.getDouble("largo"),
                    rs.getDouble("ancho"), rs.getDouble("alto"), rs.getDouble("valor_declarado"));
            e.restaurar(EstadoFactory.crear(rs.getString("estado")), historial(c, e.getGuia()));
            return e;
        } catch (PaqueLibreException ex) {
            throw new DAOException("Dato inválido en la base de datos: " + ex.getMessage(), ex);
        }
    }

    @Override
    public List<CambioEstado> historial(String guia) throws DAOException {
        try (Connection c = Conexion.obtener()) {
            return historial(c, guia);
        } catch (SQLException ex) {
            throw new DAOException("No se pudo leer el historial: " + ex.getMessage(), ex);
        }
    }

    private List<CambioEstado> historial(Connection c, String guia) throws SQLException {
        List<CambioEstado> h = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT estado, fecha_hora FROM historial_estado WHERE guia=? ORDER BY id")) {
            ps.setString(1, guia);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    h.add(new CambioEstado(rs.getString(1), LocalDateTime.parse(rs.getString(2))));
            }
        }
        return h;
    }

    /** El total sale del cálculo polimórfico del modelo, por eso se agrupa en Java. */
    @Override
    public Map<String, double[]> resumenPorEstado() throws DAOException {
        Map<String, double[]> r = new TreeMap<>();
        for (Envio e : listar()) {
            double[] v = r.computeIfAbsent(e.getEstadoNombre(), k -> new double[2]);
            v[0]++;
            v[1] += e.calcularTotal();
        }
        return r;
    }
}
