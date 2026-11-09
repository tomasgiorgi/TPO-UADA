// Modos de movimiento de Uada. N = sin modo (solo lo usa la raiz, que no se movio)
public enum Modo {
    C('C'),   // caminando
    S('S'),   // saltando
    P('P'),   // patas para arriba
    N('N');   // ninguno (raiz)
    private final char letra;


    Modo(char letra) {
        this.letra = letra;

    }

    // Convierte la letra del archivo (C, S o P) en el modo
    public static Modo desdeChar(char letra) {
        switch (letra) {
            case 'C': return C;
            case 'S': return S;
            case 'P': return P;
            default: throw new IllegalArgumentException("Modo desconocido: " + letra);
        }
    }

    // Tiempo del tramo segun el modo (t = segundos del mapa): C = t, S = t/2, P = 2t
    public double tiempo (double t) {
        if (this == C) return t ;
        if (this == S) return t/2;
        return t*2;
    }

    // Bateria consumida segun el modo: C = t/60, S = 2.2 * t/60, P = 0.45 * t/60
    public double bateria (double t) {
        if (this == C) return t/60;
        if (this == S) return 2.2*(t/60);
        return 0.45*(t/60);
    }
}
