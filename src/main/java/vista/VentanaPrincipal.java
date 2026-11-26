package vista;

import excepciones.PaqueLibreException;
import modelo.CambioEstado;
import modelo.Envio;
import modelo.TipoEnvio;
import persistencia.Conexion;
import persistencia.EnvioDAO;
import persistencia.EnvioDAOJDBC;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/** La ventana solo muestra y captura datos: reglas en el modelo, SQL en el DAO. */
public class VentanaPrincipal extends JFrame {

    @FunctionalInterface
    private interface Accion { void ejecutar(Envio e) throws PaqueLibreException; }

    private final EnvioDAO dao = new EnvioDAOJDBC();

    private final DefaultTableModel modelo = new DefaultTableModel(
            new String[]{"Guía", "Tipo", "Peso real", "Peso fact.", "Flete", "Seguro", "Total", "Estado"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable tabla = new JTable(modelo);

    private final JComboBox<TipoEnvio> cmbTipo = new JComboBox<>(TipoEnvio.values());
    private final JTextField txtGuia = new JTextField(9);
    private final JTextField txtRem = new JTextField(10);
    private final JTextField txtDest = new JTextField(10);
    private final JTextField txtDestino = new JTextField(10);
    private final JTextField txtPeso = new JTextField(6);
    private final JTextField txtLargo = new JTextField(5);
    private final JTextField txtAncho = new JTextField(5);
    private final JTextField txtAlto = new JTextField(5);
    private final JTextField txtValor = new JTextField(9);
    private final JLabel lblInfoTipo = new JLabel();
    private final JTextField txtGuiaConsulta = new JTextField(9);

    public VentanaPrincipal() {
        super("PaqueLibre - Gestión de envíos");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 640);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(8, 8));
        add(armarFormulario(), BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);
        add(armarBotones(), BorderLayout.SOUTH);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setRowHeight(22);
        cmbTipo.addActionListener(e -> actualizarInfoTipo());
        actualizarInfoTipo();
        refrescar();
    }

    // ---------------- construcción de la interfaz ----------------
    private JPanel armarFormulario() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createTitledBorder("Datos del envío"));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 6, 4, 6);
        g.anchor = GridBagConstraints.WEST;
        Object[][] filas = {
                {"Tipo de servicio", cmbTipo, "Guía (PL-000000)", txtGuia, "Remitente", txtRem},
                {"Destinatario", txtDest, "Ciudad / país destino", txtDestino, "Valor declarado ($)", txtValor},
                {"Peso real (kg)", txtPeso, "Largo (cm)", txtLargo, "Ancho (cm)", txtAncho},
                {"Alto (cm)", txtAlto, "", lblInfoTipo, "", new JLabel()}
        };
        for (int f = 0; f < filas.length; f++)
            for (int c = 0; c < 6; c++) {
                g.gridx = c; g.gridy = f;
                Object o = filas[f][c];
                p.add(o instanceof String s ? new JLabel(s) : (Component) o, g);
            }
        return p;
    }

    private JPanel armarBotones() {
        JPanel p = new JPanel(new GridLayout(2, 1, 4, 4));
        JPanel a = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        JPanel b = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));

        a.add(boton("Guardar nuevo", e -> guardar()));
        a.add(boton("Cargar selección", e -> cargarSeleccion()));
        a.add(boton("Guardar edición", e -> guardarEdicion()));
        a.add(boton("Eliminar", e -> eliminar()));
        a.add(boton("Limpiar", e -> limpiar()));
        a.add(new JLabel("  Estado:"));
        a.add(boton("Pasar a EN_RUTA", e -> cambiarEstado(Envio::pasarAEnRuta)));
        a.add(boton("Entregar", e -> cambiarEstado(Envio::entregar)));
        a.add(boton("Devolver", e -> cambiarEstado(Envio::devolver)));

        b.add(new JLabel("Consultas:"));
        b.add(boton("Envíos y total por estado", e -> consultaResumen()));
        b.add(new JLabel("  Guía:"));
        b.add(txtGuiaConsulta);
        b.add(boton("Historial de la guía", e -> consultaHistorial()));
        p.add(a);
        p.add(b);
        return p;
    }

    private JButton boton(String texto, java.awt.event.ActionListener l) {
        JButton b = new JButton(texto);
        b.addActionListener(l);
        return b;
    }

    // ---------------- utilidades ----------------
    private double num(JTextField t) { return Double.parseDouble(t.getText().trim().replace(',', '.')); }

    private void msg(String m) { JOptionPane.showMessageDialog(this, m, "PaqueLibre", JOptionPane.INFORMATION_MESSAGE); }

    private void error(String m) { JOptionPane.showMessageDialog(this, m, "Error", JOptionPane.ERROR_MESSAGE); }

    private void actualizarInfoTipo() {
        TipoEnvio t = (TipoEnvio) cmbTipo.getSelectedItem();
        lblInfoTipo.setText("Peso máximo: " + (int) t.getPesoMaximo() + " kg | Seguro: "
                + (t.tieneSeguro() ? "sí (1 %, mín. $5.000)" : "no aplica"));
    }

    private void limpiar() {
        for (JTextField t : new JTextField[]{txtGuia, txtRem, txtDest, txtDestino, txtPeso, txtLargo, txtAncho, txtAlto, txtValor})
            t.setText("");
        txtGuia.setEnabled(true);
        cmbTipo.setEnabled(true);
        tabla.clearSelection();
    }

    private Envio seleccionado() throws PaqueLibreException {
        int f = tabla.getSelectedRow();
        if (f < 0) { msg("Seleccione un envío de la tabla."); return null; }
        return dao.buscar((String) modelo.getValueAt(f, 0));
    }

    // ---------------- R5: ver ----------------
    private void refrescar() {
        try {
            modelo.setRowCount(0);
            for (Envio e : dao.listar())
                modelo.addRow(new Object[]{e.getGuia(), e.getTipo(), e.getPesoReal(), e.pesoFacturable(),
                        pesos(e.calcularFlete()), pesos(e.getSeguro()), pesos(e.calcularTotal()), e.getEstadoNombre()});
        } catch (PaqueLibreException ex) { error(ex.getMessage()); }
    }

    private String pesos(double v) { return String.format("$%,.0f", v); }

    // ---------------- R6: guardar ----------------
    private void guardar() {
        try {
            TipoEnvio t = (TipoEnvio) cmbTipo.getSelectedItem();
            Envio e = t.crear(txtGuia.getText().trim().toUpperCase(), txtRem.getText().trim(),
                    txtDest.getText().trim(), txtDestino.getText().trim(), num(txtPeso), num(txtLargo),
                    num(txtAncho), num(txtAlto), num(txtValor));
            dao.insertar(e);
            refrescar();
            limpiar();
        } catch (NumberFormatException ex) {
            error("Ingrese valores numéricos válidos en peso, dimensiones y valor declarado.");
        } catch (PaqueLibreException ex) { error(ex.getMessage()); }
    }

    // ---------------- R7: editar ----------------
    private void cargarSeleccion() {
        try {
            Envio e = seleccionado();
            if (e == null) return;
            cmbTipo.setSelectedItem(e.getTipo());
            cmbTipo.setEnabled(false);
            txtGuia.setText(e.getGuia());
            txtGuia.setEnabled(false);
            txtRem.setText(e.getRemitente());
            txtDest.setText(e.getDestinatario());
            txtDestino.setText(e.getDestino());
            txtPeso.setText(String.valueOf(e.getPesoReal()));
            txtLargo.setText(String.valueOf(e.getLargo()));
            txtAncho.setText(String.valueOf(e.getAncho()));
            txtAlto.setText(String.valueOf(e.getAlto()));
            txtValor.setText(String.valueOf(e.getValorDeclarado()));
        } catch (PaqueLibreException ex) { error(ex.getMessage()); }
    }

    private void guardarEdicion() {
        try {
            if (txtGuia.isEnabled()) { msg("Primero seleccione un envío y pulse \"Cargar selección\"."); return; }
            Envio e = dao.buscar(txtGuia.getText().trim());
            if (e == null) { error("El envío ya no existe."); return; }
            e.corregirDatos(num(txtPeso), num(txtLargo), num(txtAncho), num(txtAlto)); // el State decide si se puede
            e.setRemitente(txtRem.getText().trim());
            e.setDestinatario(txtDest.getText().trim());
            e.setDestino(txtDestino.getText().trim());
            e.setValorDeclarado(num(txtValor));
            dao.actualizar(e);
            refrescar();
            limpiar();
        } catch (NumberFormatException ex) {
            error("Ingrese valores numéricos válidos en peso, dimensiones y valor declarado.");
        } catch (PaqueLibreException ex) { error(ex.getMessage()); }
    }

    // ---------------- R8: eliminar ----------------
    private void eliminar() {
        try {
            Envio e = seleccionado();
            if (e == null) return;
            if (JOptionPane.showConfirmDialog(this, "¿Eliminar el envío " + e.getGuia() + "?", "Confirmar",
                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                dao.eliminar(e.getGuia());
                refrescar();
            }
        } catch (PaqueLibreException ex) { error(ex.getMessage()); }
    }

    // ---------------- cambios de estado (patrón State) ----------------
    private void cambiarEstado(Accion a) {
        try {
            Envio e = seleccionado();
            if (e == null) return;
            a.ejecutar(e);               // lanza TransicionInvalidaException si no se permite
            dao.actualizar(e);
            refrescar();
        } catch (PaqueLibreException ex) { error(ex.getMessage()); }
    }

    // ---------------- R9: consultas ----------------
    private void consultaResumen() {
        try {
            StringBuilder sb = new StringBuilder();
            dao.resumenPorEstado().forEach((est, v) ->
                    sb.append(String.format("%s: %d envíos, total facturado $%,.0f%n", est, (int) v[0], v[1])));
            msg(sb.length() == 0 ? "No hay envíos registrados." : sb.toString());
        } catch (PaqueLibreException ex) { error(ex.getMessage()); }
    }

    private void consultaHistorial() {
        try {
            String guia = txtGuiaConsulta.getText().trim().toUpperCase();
            if (guia.isEmpty() && tabla.getSelectedRow() >= 0)
                guia = (String) modelo.getValueAt(tabla.getSelectedRow(), 0);
            StringBuilder sb = new StringBuilder("Historial de " + guia + "\n\n");
            var h = dao.historial(guia);
            for (CambioEstado c : h)
                sb.append(c.fecha().toString().replace('T', ' ')).append("   →   ").append(c.estado()).append("\n");
            msg(h.isEmpty() ? "No hay historial para esa guía." : sb.toString());
        } catch (PaqueLibreException ex) { error(ex.getMessage()); }
    }

    // ---------------- main ----------------
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { Conexion.inicializar(); }
            catch (PaqueLibreException e) { JOptionPane.showMessageDialog(null, e.getMessage()); }
            new VentanaPrincipal().setVisible(true);
        });
    }
}
