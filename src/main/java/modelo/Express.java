package modelo;

import excepciones.PaqueLibreException;

public class Express extends Envio implements Asegurable {
    Express(String g, String r, String d, String dest, double p, double l, double a, double h, double v)
            throws PaqueLibreException {
        super(g, r, d, dest, p, l, a, h, v);
    }
    @Override public TipoEnvio getTipo() { return TipoEnvio.EXPRESS; }
    @Override public double calcularFlete() { return 15000 + 3500 * pesoFacturable(); }
    @Override public double getSeguro() { return calcularSeguro(); }
}
