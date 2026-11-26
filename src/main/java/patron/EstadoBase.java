package patron;

import excepciones.PaqueLibreException;
import excepciones.TransicionInvalidaException;
import modelo.Envio;

/** Por defecto todo está prohibido; cada estado habilita solo lo suyo. */
public abstract class EstadoBase implements EstadoEnvio {
    private TransicionInvalidaException no(String accion) {
        return new TransicionInvalidaException("No se puede " + accion + " un envío en estado " + getNombre() + ".");
    }
    @Override public void enRuta(Envio e) throws PaqueLibreException { throw no("pasar a EN_RUTA"); }
    @Override public void entregar(Envio e) throws PaqueLibreException { throw no("entregar"); }
    @Override public void devolver(Envio e) throws PaqueLibreException { throw no("devolver"); }
    @Override public void corregirDatos(Envio e, double p, double l, double a, double h)
            throws PaqueLibreException { throw no("corregir los datos de"); }
}
