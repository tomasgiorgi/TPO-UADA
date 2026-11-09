public enum Modo {
    C('C'),
    S('S'),
    P('P'),
    N('N');
    private final char letra;


    Modo(char letra) {
        this.letra = letra;

    }

    public static Modo desdeChar(char letra) {
        switch (letra) {
            case 'C': return C;
            case 'S': return S;
            case 'P': return P;
            default: throw new IllegalArgumentException("Modo desconocido: " + letra);
        }
    }

    public double tiempo (double t) {
        if (this == C) return t ;
        if (this == S) return t/2;
        return t*2;
    }

    public double bateria (double t) {
        if (this == C) return t/60;
        if (this == S) return 2.2*(t/60);
        return 0.45*(t/60);
    }
}
