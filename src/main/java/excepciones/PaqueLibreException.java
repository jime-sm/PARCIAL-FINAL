package excepciones;

/** Excepción base de la aplicación: la ventana solo captura esta. */
public class PaqueLibreException extends Exception {
    public PaqueLibreException(String m) { super(m); }
    public PaqueLibreException(String m, Throwable c) { super(m, c); }
}
