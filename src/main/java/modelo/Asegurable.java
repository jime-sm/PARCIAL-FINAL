package modelo;

/** Solo la implementan Express e Internacional. */
public interface Asegurable {
    double getValorDeclarado();

    /** 1 % del valor declarado, con un mínimo de $5.000. */
    default double calcularSeguro() {
        return Math.max(5000, getValorDeclarado() * 0.01);
    }
}
