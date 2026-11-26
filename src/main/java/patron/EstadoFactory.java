package patron;

import java.util.Map;
import java.util.function.Supplier;

/** Reconstruye el estado guardado en la BD usando un mapa (sin switch). */
public class EstadoFactory {
    private static final Map<String, Supplier<EstadoEnvio>> MAPA = Map.of(
            "REGISTRADO", Registrado::new,
            "EN_RUTA", EnRuta::new,
            "ENTREGADO", Entregado::new,
            "DEVUELTO", Devuelto::new);

    public static EstadoEnvio crear(String nombre) {
        return MAPA.get(nombre).get();
    }
}
