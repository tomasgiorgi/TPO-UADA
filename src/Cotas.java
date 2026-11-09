import java.util.ArrayList;
import java.util.List;

// Calcula las cotas de cada nodo (CI y CS) y la poda "no puede volver al 348"
public class Cotas {
    private final Lugar lugar348;                                 // inicio y fin del recorrido
    private final List<Lugar> lugares;                            // lugares del grafo
    private final List<Arista> arsitasAl348 = new ArrayList<>();  // aristas que llegan al 348

    // Precalculo (una sola vez): recorre todas las aristas y guarda en cada destino
    // su mejorEntrada (menor tiempo a maxima velocidad) y su peorEntrada (mayor tiempo a peor velocidad).
    // Tambien guarda las aristas que llegan al 348.
    public Cotas (Lugar lugar348, List<Lugar> lugares) {
        this.lugar348 = lugar348;
        this.lugares = lugares;
        for (Lugar l : lugares) {
            for (Arista a : l.salidas) {
                Lugar d = a.getDestino();
                double mejorEntrada = Math.min(d.getMejorEntrada(), a.tiempoMaximaVelocidad());
                d.setMejorEntrada(mejorEntrada);
                double peorEntrada = Math.max(d.getPeorEntrada(), a.tiempoPeorVelocidad());
                d.setPeorEntrada(peorEntrada);
                if (d == lugar348) arsitasAl348.add(a);
            }

        }
    }

    // Cota inferior (CI): lo minimo que puede tardar una ruta que pase por este nodo.
    // CI = tiempo + mejor entrada al 348 todavia usable + suma de mejorEntrada de cada obligatorio pendiente
    public double calcularCI(double tiempo, List<Lugar> visitados, List<Lugar> pendientes, Lugar lugarActual) {
        // Sin pendientes ya es una solucion: la CI es su tiempo
        if (pendientes.isEmpty()) return tiempo;

        // Obligatorios que faltan, sin contar el 348
        List<Lugar> faltan = new ArrayList<>(pendientes);
        faltan.remove(lugar348);

        // Mejor arista para entrar al 348 que todavia se pueda usar:
        // su origen no fue visitado, o es el lugar actual y ya no faltan obligatorios
        double mejorVuelta = Double.MAX_VALUE;
        for (Arista a : arsitasAl348) {
            boolean origenLibre = !visitados.contains(a.getOrigen());
            boolean origenActual = faltan.isEmpty() && a.getOrigen() == lugarActual;
            if (origenActual || origenLibre) {
                mejorVuelta = Math.min(mejorVuelta, a.tiempoMaximaVelocidad());
            }
        }

        // Suma: tiempo acumulado + vuelta al 348 + entrada a cada obligatorio pendiente
        double resultado = tiempo + mejorVuelta;
        for (Lugar l : faltan) resultado += l.getMejorEntrada();
        return resultado;
    }

    // Cota superior (CS): lo maximo que puede tardar cualquier ruta que termine desde este nodo.
    // CS = tiempo + peorEntrada del 348 + suma de peorEntrada de cada lugar no visitado
    public double calcularCS (double tiempo, List<Lugar> visitados) {
        double resultado = tiempo + lugar348.getPeorEntrada();
        for (Lugar l : lugares) {
            if (l != lugar348 && !visitados.contains(l)) resultado += l.getPeorEntrada();

        }
        return resultado;
    }

    // Poda "no puede volver al 348": devuelve false si faltan obligatorios
    // y ya no queda ninguna arista sin usar para entrar al 348
    public boolean puedeVolverAl348(List<Lugar> visitados, List<Lugar> pendientes, Lugar lugarActual) {
        // faltan = true si queda algun obligatorio pendiente distinto del 348
        boolean faltan = false;
        for (Lugar p : pendientes) {
            if (p != lugar348) faltan = true;
        }
        // Alcanza con una arista al 348 que se pueda usar (origen libre, o el lugar actual sin obligatorios pendientes)
        for (Arista a : arsitasAl348) {
            if (!visitados.contains(a.getOrigen())) return true;
            if (!faltan && a.getOrigen() == lugarActual) return true;
        }
        return false;
    }

    // ===== Getters =====
    public Lugar getLugar348() {
        return lugar348;
    }

    public List<Lugar> getLugares() {
        return lugares;
    }

    public List<Arista> getArsitasAl348() {
        return arsitasAl348;
    }
}
