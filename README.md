# PaqueLibre - Parcial Tercer Corte (Programación 1)

- **Estudiante:** _(escribe aquí tu nombre)_
- **Variante:** 5 - Paquetería PaqueLibre
- **Patrón de diseño:** State
- **Base de datos:** SQLite (archivo `paquelibre.db`, se crea solo al abrir la aplicación; el script está en `script.sql`)

## Cómo ejecutar
Requisitos: Java 17 o superior y Maven.

```
mvn compile exec:java
```

También puede abrir el proyecto en IntelliJ/NetBeans/VS Code y ejecutar `vista.VentanaPrincipal`.

## Estructura
- `excepciones`: excepciones propias (una por validación).
- `modelo`: `Envio` (abstracta), `Estandar`, `Express`, `Internacional`, `Asegurable`, `TipoEnvio` (fábrica).
- `patron`: patrón State (`Registrado`, `EnRuta`, `Entregado`, `Devuelto`).
- `persistencia`: `Conexion`, `EnvioDAO` y `EnvioDAOJDBC`.
- `vista`: ventana Swing.
# PARCIAL-FINAL
