-- PaqueLibre - Variante 5 (SQLite)
CREATE TABLE IF NOT EXISTS envio (
    guia            TEXT PRIMARY KEY,
    tipo            TEXT NOT NULL,          -- ESTANDAR, EXPRESS o INTERNACIONAL
    remitente       TEXT NOT NULL,
    destinatario    TEXT NOT NULL,
    destino         TEXT NOT NULL,
    peso_real       REAL NOT NULL,
    largo           REAL NOT NULL,
    ancho           REAL NOT NULL,
    alto            REAL NOT NULL,
    valor_declarado REAL NOT NULL,
    estado          TEXT NOT NULL           -- REGISTRADO, EN_RUTA, ENTREGADO o DEVUELTO
);

CREATE TABLE IF NOT EXISTS historial_estado (
    id         INTEGER PRIMARY KEY AUTOINCREMENT,
    guia       TEXT NOT NULL REFERENCES envio(guia),
    estado     TEXT NOT NULL,
    fecha_hora TEXT NOT NULL
);
