import java.util.*;

// Conexion dirigida entre dos lugares: origen -> destino, con su tiempo en segundos y los modos permitidos
public class Arista {
    private Lugar origen;
    private Lugar destino;
    private double tiempo;      // segundos indicados en el mapa (tiempo caminando)
    private Set<Modo> modos;    // modos permitidos en esta arista (C, S, P)

    public Arista (Lugar origen, Lugar destino, double tiempo, Set<Modo> modos) {
        this.origen = origen;
        this.destino = destino;
        this.tiempo = tiempo;
        this.modos = modos;
    }

    // Tiempo con el modo mas rapido permitido: T/2 si se puede saltar, si no T (se usa para mejorEntrada)
    public double tiempoMaximaVelocidad() {
        return modos.contains(Modo.S) ? tiempo / 2 : tiempo;
    }

    // Tiempo con el modo mas lento permitido: 2T si se puede ir patas para arriba, si no T (se usa para peorEntrada)
    public double tiempoPeorVelocidad() {
        return modos.contains(Modo.P) ? tiempo * 2 : tiempo;
    }

    // ===== Getters =====
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
