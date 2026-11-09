import java.util.*;

public class Nodo implements Comparable<Nodo>{
    public static Lugar INICIO;

    private int id;
    private Nodo padre;
    private Lugar lugar;
    private Modo modo;
    private double tiempo;
    private double bateria;
    private List<Lugar> visitados;
    private List<Lugar> pendientes;
    private double CI;
    private double CS;
    private double distanciaAl348;
    private Estado estado;
    private int ordenExpansion;

    public Nodo (int id, Nodo padre, Lugar lugar, Modo modo, double tiempo, double bateria) {
        this.id = id;
        this.padre = padre;
        this.lugar = lugar;
        this.modo = modo;
        this.tiempo = tiempo;
        this.bateria = bateria;
        this.visitados = padre == null ? new ArrayList<>() : new ArrayList<>(padre.visitados);
        this.pendientes = padre == null ? new ArrayList<>() : new ArrayList<>(padre.pendientes);
        CI = 0;
        CS = 0;
        distanciaAl348 = lugar.distanciaA(INICIO);
        estado = Estado.VIVO;
        ordenExpansion = 0;

    }

    public void registrarLlegada() {
        visitados.add(lugar);
        pendientes.remove(lugar);
    }

    @Override
    public int compareTo(Nodo o) {
        int porCI = Double.compare(this.CI, o.CI);
        if (porCI != 0) return porCI;
        int porCS = Double.compare(this.CS, o.CS);
        if (porCS != 0 ) return porCS;
        return Integer.compare(this.id, o.id);

    }

    public int getTamanoVisitados() {
        return getVisitados().size();
    }

    public static Lugar getInicio() {
        return INICIO;
    }

    public static void setInicio(Lugar inicio) {
        INICIO = inicio;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Nodo getPadre() {
        return padre;
    }

    public void setPadre(Nodo padre) {
        this.padre = padre;
    }

    public Lugar getLugar() {
        return lugar;
    }

    public void setLugar(Lugar lugar) {
        this.lugar = lugar;
    }

    public Modo getModo() {
        return modo;
    }

    public void setModo(Modo modo) {
        this.modo = modo;
    }

    public double getTiempo() {
        return tiempo;
    }

    public void setTiempo(double tiempo) {
        this.tiempo = tiempo;
    }

    public double getBateria() {
        return bateria;
    }

    public void setBateria(double bateria) {
        this.bateria = bateria;
    }

    public List<Lugar> getVisitados() {
        return visitados;
    }

    public void setVisitados(List<Lugar> visitados) {
        this.visitados = visitados;
    }

    public List<Lugar> getPendientes() {
        return pendientes;
    }

    public void setPendientes(List<Lugar> pendientes) {
        this.pendientes = pendientes;
    }

    public double getCI() {
        return CI;
    }

    public void setCI(double CI) {
        this.CI = CI;
    }

    public double getCS() {
        return CS;
    }

    public void setCS(double CS) {
        this.CS = CS;
    }

    public double getDistanciaAl348() {
        return distanciaAl348;
    }

    public void setDistanciaAl348(double distanciaAl348) {
        this.distanciaAl348 = distanciaAl348;
    }

    public Estado getEstado() {
        return estado;
    }

    public void setEstado(Estado estado) {
        this.estado = estado;
    }

    public int getOrdenExpansion() {
        return ordenExpansion;
    }

    public void setOrdenExpansion(int ordenExpansion) {
        this.ordenExpansion = ordenExpansion;
    }


}
