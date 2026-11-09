import java.util.List;

// Lo que devuelve Main.ejecucionAlgoritmo: agrupa los datos que se necesitan despues del Branch & Bound
public class Resultado {
    private final List<Nodo> soluciones;     // nodos que llegaron al 348 con todo hecho
    private final Nodo mejorParcial;         // contingencia: nodo valido mas cercano al 348 (null si no hay)
    private final double mejorDistancia;     // contingencia: distancia de ese nodo al 348
    private final List<Nodo> todos;          // todos los nodos creados; la posicion es el id
    private final int nodosExpandidos;       // cantidad de nodos expandidos

    public Resultado(List<Nodo> soluciones, Nodo mejorParcial, double mejorDistancia, List<Nodo> todos, int nodosExpandidos) {
        this.soluciones = soluciones;
        this.mejorParcial = mejorParcial;
        this.mejorDistancia = mejorDistancia;
        this.todos = todos;
        this.nodosExpandidos = nodosExpandidos;
    }

    // ===== Getters =====
    public List<Nodo> getSoluciones() {
        return soluciones;
    }

    public Nodo getMejorParcial() {
        return mejorParcial;
    }

    public double getMejorDistancia() {
        return mejorDistancia;
    }

    public List<Nodo> getTodos() {
        return todos;
    }

    public int getNodosExpandidos() {
        return nodosExpandidos;
    }
}
