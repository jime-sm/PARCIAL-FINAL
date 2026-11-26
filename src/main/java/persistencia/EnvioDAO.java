package persistencia;

import excepciones.DAOException;
import modelo.CambioEstado;
import modelo.Envio;

import java.util.List;
import java.util.Map;

public interface EnvioDAO {
    void insertar(Envio e) throws DAOException;
    void actualizar(Envio e) throws DAOException;
    void eliminar(String guia) throws DAOException;
    Envio buscar(String guia) throws DAOException;
    List<Envio> listar() throws DAOException;
    /** Consulta b): historial de estados de una guía. */
    List<CambioEstado> historial(String guia) throws DAOException;
    /** Consulta a): por estado {cantidad de envíos, total facturado}. */
    Map<String, double[]> resumenPorEstado() throws DAOException;
}
