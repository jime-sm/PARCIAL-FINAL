package modelo;

import excepciones.*;
import patron.EstadoEnvio;
import patron.Registrado;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/** Clase abstracta del modelo. Atributos privados, validados en los setters. */
public abstract class Envio {
    private String guia;
    private String remitente;
    private String destinatario;
    private String destino;
    private double pesoReal;
    private double largo, ancho, alto;
    private double valorDeclarado;
    private EstadoEnvio estado;
    private final List<CambioEstado> historial = new ArrayList<>();

    protected Envio(String guia, String remitente, String destinatario, String destino,
                    double peso, double largo, double ancho, double alto, double valor)
            throws PaqueLibreException {
        setGuia(guia);
        this.remitente = remitente;
        this.destinatario = destinatario;
        this.destino = destino;
        setPesoReal(peso);
        setDimensiones(largo, ancho, alto);
        setValorDeclarado(valor);
        this.estado = new Registrado();
        historial.add(new CambioEstado(estado.getNombre(), ahora()));
    }

    private static LocalDateTime ahora() { return LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS); }

    // ---------- validaciones ----------
    public void setGuia(String g) throws GuiaInvalidaException {
        if (g == null || !g.matches("PL-\\d{6}"))
            throw new GuiaInvalidaException("La guía debe ser PL- seguido de 6 números (ej. PL-000101).");
        this.guia = g;
    }

    public void setPesoReal(double p) throws PesoInvalidoException {
        if (p <= 0 || p > getPesoMaximo())
            throw new PesoInvalidoException("El peso debe ser mayor que 0 y como máximo "
                    + getPesoMaximo() + " kg para el servicio " + getTipo() + ".");
        this.pesoReal = p;
    }

    public void setDimensiones(double l, double a, double h) throws DimensionInvalidaException {
        if (l <= 0 || a <= 0 || h <= 0)
            throw new DimensionInvalidaException("Las dimensiones (largo, ancho y alto) deben ser mayores que 0.");
        this.largo = l; this.ancho = a; this.alto = h;
    }

    public void setValorDeclarado(double v) throws ValorInvalidoException {
        if (v < 0) throw new ValorInvalidoException("El valor declarado debe ser mayor o igual a 0.");
        this.valorDeclarado = v;
    }

    public void setRemitente(String r) { this.remitente = r; }
    public void setDestinatario(String d) { this.destinatario = d; }
    public void setDestino(String d) { this.destino = d; }

    // ---------- cálculos (polimorfismo) ----------
    public abstract TipoEnvio getTipo();
    public abstract double calcularFlete();

    public double getPesoMaximo() { return getTipo().getPesoMaximo(); }
    public double pesoVolumetrico() { return largo * ancho * alto / 5000.0; }
    public double pesoFacturable() { return Math.ceil(Math.max(pesoReal, pesoVolumetrico())); }
    public double getSeguro() { return 0; }
    public double calcularTotal() { return calcularFlete() + getSeguro(); }

    // ---------- State: el envío solo delega ----------
    public void pasarAEnRuta() throws PaqueLibreException { estado.enRuta(this); }
    public void entregar() throws PaqueLibreException { estado.entregar(this); }
    public void devolver() throws PaqueLibreException { estado.devolver(this); }
    public void corregirDatos(double peso, double l, double a, double h) throws PaqueLibreException {
        estado.corregirDatos(this, peso, l, a, h);
    }

    /** Lo llaman los estados al cambiar. */
    public void cambiarEstado(EstadoEnvio nuevo) {
        this.estado = nuevo;
        historial.add(new CambioEstado(nuevo.getNombre(), ahora()));
    }

    /** Lo usa el DAO al reconstruir desde la base de datos. */
    public void restaurar(EstadoEnvio e, List<CambioEstado> h) {
        this.estado = e;
        historial.clear();
        historial.addAll(h);
    }

    // ---------- getters ----------
    public String getGuia() { return guia; }
    public String getRemitente() { return remitente; }
    public String getDestinatario() { return destinatario; }
    public String getDestino() { return destino; }
    public double getPesoReal() { return pesoReal; }
    public double getLargo() { return largo; }
    public double getAncho() { return ancho; }
    public double getAlto() { return alto; }
    public double getValorDeclarado() { return valorDeclarado; }
    public String getEstadoNombre() { return estado.getNombre(); }
    public List<CambioEstado> getHistorial() { return List.copyOf(historial); }
}
