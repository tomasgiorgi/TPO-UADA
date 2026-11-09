import java.util.ArrayList;
import java.util.List;

public class Cotas {
    private final Lugar lugar348;
    private final List<Lugar> lugares;
    private final List<Arista> arsitasAl348 = new ArrayList<>();

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

    public double calcularCI(double tiempo, List<Lugar> visitados, List<Lugar> pendientes, Lugar lugarActual) {
        if (pendientes.isEmpty()) return tiempo;

        List<Lugar> faltan = new ArrayList<>(pendientes);
        faltan.remove(lugar348);

        double mejorVuelta = Double.MAX_VALUE;
        for (Arista a : arsitasAl348) {
            boolean origenLibre = !visitados.contains(a.getOrigen());
            boolean origenActual = faltan.isEmpty() && a.getOrigen() == lugarActual;
            if (origenActual || origenLibre) {
                mejorVuelta = Math.min(mejorVuelta, a.tiempoMaximaVelocidad());
            }
        }


        double resultado = tiempo + mejorVuelta;
        for (Lugar l : faltan) resultado += l.getMejorEntrada();
        return resultado;
    }

    public double calcularCS (double tiempo, List<Lugar> visitados) {
        double resultado = tiempo + lugar348.getPeorEntrada();
        for (Lugar l : lugares) {
            if (l != lugar348 && !visitados.contains(l)) resultado += l.getPeorEntrada();

        }
        return resultado;
    }

    public boolean puedeVolverAl348(List<Lugar> visitados, List<Lugar> pendientes, Lugar lugarActual) {
        boolean faltan = false;
        for (Lugar p : pendientes) {
            if (p != lugar348) faltan = true;
        }
        for (Arista a : arsitasAl348) {
            if (!visitados.contains(a.getOrigen())) return true;
            if (!faltan && a.getOrigen() == lugarActual) return true;
        }
        return false;
    }

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