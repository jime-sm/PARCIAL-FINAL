package modelo;

import excepciones.PaqueLibreException;

/**
 * Factory Method de los envíos y única clase que hace new Estandar/Express/Internacional.
 * Se usa al registrar desde la ventana y al reconstruir desde la base de datos.
 */
public enum TipoEnvio {
    ESTANDAR("Estándar", 50, false) {
        @Override public Envio crear(String g, String r, String d, String dest, double p,
                                     double l, double a, double h, double v) throws PaqueLibreException {
            return new Estandar(g, r, d, dest, p, l, a, h, v);
        }
    },
    EXPRESS("Express", 50, true) {
        @Override public Envio crear(String g, String r, String d, String dest, double p,
                                     double l, double a, double h, double v) throws PaqueLibreException {
            return new Express(g, r, d, dest, p, l, a, h, v);
        }
    },
    INTERNACIONAL("Internacional", 30, true) {
        @Override public Envio crear(String g, String r, String d, String dest, double p,
                                     double l, double a, double h, double v) throws PaqueLibreException {
            return new Internacional(g, r, d, dest, p, l, a, h, v);
        }
    };

    private final String etiqueta;
    private final double pesoMaximo;
    private final boolean conSeguro;

    TipoEnvio(String etiqueta, double pesoMaximo, boolean conSeguro) {
        this.etiqueta = etiqueta; this.pesoMaximo = pesoMaximo; this.conSeguro = conSeguro;
    }

    public abstract Envio crear(String guia, String remitente, String destinatario, String destino,
                                double peso, double largo, double ancho, double alto, double valor)
            throws PaqueLibreException;

    public double getPesoMaximo() { return pesoMaximo; }
    public boolean tieneSeguro() { return conSeguro; }
    @Override public String toString() { return etiqueta; }
}
