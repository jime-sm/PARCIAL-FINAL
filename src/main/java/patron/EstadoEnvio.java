package patron;

import excepciones.PaqueLibreException;
import modelo.Envio;

/** Patrón State: cada estado es una clase y Envio no usa if/switch sobre el estado. */
public interface EstadoEnvio {
    String getNombre();
    void enRuta(Envio e) throws PaqueLibreException;
    void entregar(Envio e) throws PaqueLibreException;
    void devolver(Envio e) throws PaqueLibreException;
    void corregirDatos(Envio e, double peso, double l, double a, double h) throws PaqueLibreException;
}
