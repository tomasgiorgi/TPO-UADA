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

    // Tiempo con el modo mas rapido permitido (se usa para mejorEntrada, cota inferior):
    // saltando si se puede (T/2); si no, caminando (T); si solo se permite patas para arriba, 2T
    public double tiempoMaximaVelocidad() {
        if (modos.contains(Modo.S)) {
            return Modo.S.tiempo(tiempo);
        } else if (modos.contains(Modo.C)) {
            return Modo.C.tiempo(tiempo);
        }
        return Modo.P.tiempo(tiempo);
    }

    // Tiempo con el modo mas lento permitido (se usa para peorEntrada, cota superior):
    // patas para arriba si se puede (2T); si no, caminando (T); si solo se permite saltar, T/2
    public double tiempoPeorVelocidad() {
        if (modos.contains(Modo.P)) {
            return Modo.P.tiempo(tiempo);
        } else if (modos.contains(Modo.C)) {
            return Modo.C.tiempo(tiempo);
        }
        return Modo.S.tiempo(tiempo);
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
