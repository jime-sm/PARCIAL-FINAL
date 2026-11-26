package patron;

import excepciones.PaqueLibreException;
import modelo.Envio;

public class Registrado extends EstadoBase {
    @Override public String getNombre() { return "REGISTRADO"; }
    @Override public void enRuta(Envio e) { e.cambiarEstado(new EnRuta()); }
    @Override public void corregirDatos(Envio e, double p, double l, double a, double h)
            throws PaqueLibreException {
        e.setDimensiones(l, a, h);
        e.setPesoReal(p);
    }
}
