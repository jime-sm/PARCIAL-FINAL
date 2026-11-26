package modelo;

import excepciones.PaqueLibreException;

public class Internacional extends Envio implements Asegurable {
    Internacional(String g, String r, String d, String dest, double p, double l, double a, double h, double v)
            throws PaqueLibreException {
        super(g, r, d, dest, p, l, a, h, v);
    }
    @Override public TipoEnvio getTipo() { return TipoEnvio.INTERNACIONAL; }
    @Override public double calcularFlete() {
        return 60000 + 12000 * pesoFacturable() + 0.10 * getValorDeclarado();
    }
    @Override public double getSeguro() { return calcularSeguro(); }
}
