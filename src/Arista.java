import java.util.*;

public class Arista {
    private Lugar origen;
    private Lugar destino;
    private double tiempo;
    private Set<Modo> modos;

    public Arista (Lugar origen, Lugar destino, double tiempo, Set<Modo> modos) {
        this.origen = origen;
        this.destino = destino;
        this.tiempo = tiempo;
        this.modos = modos;
    }

    public double tiempoMaximaVelocidad() {
        return modos.contains(Modo.S) ? tiempo / 2 : tiempo;
    }

    public double tiempoPeorVelocidad() {
        return modos.contains(Modo.P) ? tiempo * 2 : tiempo;
    }

    public Lugar getDestino() {
        return destino;
    }

    public Set<Modo> getModos() {
        return modos;
    }

    public double getTiempo() {
        return tiempo;
    }

    public Lugar getOrigen() {
        return origen;
    }
}
