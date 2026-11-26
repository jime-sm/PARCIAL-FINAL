package persistencia;

import excepciones.DAOException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Conexion {
    private static final String URL = "jdbc:sqlite:paquelibre.db";

    public static Connection obtener() throws SQLException {
        Connection c = DriverManager.getConnection(URL);
        try (Statement s = c.createStatement()) { s.execute("PRAGMA foreign_keys = ON"); }
        return c;
    }

    /** Crea las tablas si no existen (mismo contenido que script.sql). */
    public static void inicializar() throws DAOException {
        try (Connection c = obtener(); Statement s = c.createStatement()) {
            s.execute("CREATE TABLE IF NOT EXISTS envio ("
                    + "guia TEXT PRIMARY KEY, tipo TEXT NOT NULL, remitente TEXT NOT NULL,"
                    + "destinatario TEXT NOT NULL, destino TEXT NOT NULL, peso_real REAL NOT NULL,"
                    + "largo REAL NOT NULL, ancho REAL NOT NULL, alto REAL NOT NULL,"
                    + "valor_declarado REAL NOT NULL, estado TEXT NOT NULL)");
            s.execute("CREATE TABLE IF NOT EXISTS historial_estado ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, guia TEXT NOT NULL REFERENCES envio(guia),"
                    + "estado TEXT NOT NULL, fecha_hora TEXT NOT NULL)");
        } catch (SQLException ex) {
            throw new DAOException("No se pudo inicializar la base de datos: " + ex.getMessage(), ex);
        }
    }
}
