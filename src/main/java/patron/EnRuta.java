package patron;

import modelo.Envio;

public class EnRuta extends EstadoBase {
    @Override public String getNombre() { return "EN_RUTA"; }
    @Override public void entregar(Envio e) { e.cambiarEstado(new Entregado()); }
    @Override public void devolver(Envio e) { e.cambiarEstado(new Devuelto()); }
}
