package excepciones;

/** Errores de la base de datos: se muestran en la ventana sin cerrarla. */
public class DAOException extends PaqueLibreException {
    public DAOException(String m, Throwable c) { super(m, c); }
}
