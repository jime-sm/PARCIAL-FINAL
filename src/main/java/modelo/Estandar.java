package modelo;

import excepciones.PaqueLibreException;

public class Estandar extends Envio {
    Estandar(String g, String r, String d, String dest, double p, double l, double a, double h, double v)
            throws PaqueLibreException {
        super(g, r, d, dest, p, l, a, h, v);
    }
    @Override public TipoEnvio getTipo() { return TipoEnvio.ESTANDAR; }
    @Override public double calcularFlete() { return 8000 + 2000 * pesoFacturable(); }
}
