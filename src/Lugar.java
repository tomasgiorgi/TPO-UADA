import java.util.*;

// Un lugar del mapa (aula, pasillo, etc.) con sus datos, su ventana y sus aristas de salida
public class Lugar {
    private String nombre;
    private int aula;                 // numero de aula (0 si no es aula)
    private double x;                 // coordenadas, para la distancia euclidea al 348
    private double y;
    private boolean obligatorio;
    private boolean prohibido;
    private boolean tieneVentana;     // true si tiene un intervalo en el que esta cerrado
    private double ventanaDesde;
    private double ventanaHasta;
    List<Arista> salidas = new ArrayList<>();                 // aristas que salen de este lugar
    private double mejorEntrada = Double.POSITIVE_INFINITY;   // menor tiempo para entrar aca (lo calcula Cotas)
    private double peorEntrada = 0;                           // mayor tiempo para entrar aca (lo calcula Cotas)

    public Lugar (String nombre, int aula, double x, double y, boolean obligatorio, boolean prohibido) {
        this.nombre = nombre;
        this.aula = aula;
        this.x = x;
        this.y = y;
        this.obligatorio = obligatorio;
        this.prohibido = prohibido;
        this.tieneVentana = false;
    }

    // Recarga al llegar: (suma de los digitos del aula) / 5. Si no es aula, 0
    public double recarga() {
        if (aula == 0) return 0;
        int suma = 0;
        for (char c : String.valueOf(aula).toCharArray()) suma += c - '0';
        return suma / 5.0;
    }

    // true si el lugar esta cerrado en ese tiempo (ventana con extremos incluidos)
    public boolean cerradoEn(double tiempo) {
        return tieneVentana && ventanaDesde <= tiempo && tiempo <= ventanaHasta;
    }

    // Distancia en linea recta a otro lugar
    public double distanciaA(Lugar otro) {
        double dx = x - otro.x;
        double dy = y - otro.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    // Agrega una arista de salida (la usa LectorMapa)
    public void agregarSalida(Arista arista) {
        salidas.add(arista);
    }

    // ===== Getters y setters =====
    public String getNombre() {
        return nombre;
    }

    public int getAula() {
        return aula;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public boolean isObligatorio() {
        return obligatorio;
    }

    public boolean isProhibido() {
        return prohibido;
    }

    public boolean isTieneVentana() {
        return tieneVentana;
    }

    public double getVentanaDesde() {
        return ventanaDesde;
    }

    public double getVentanaHasta() {
        return ventanaHasta;
    }

    public List<Arista> getSalidas() {
        return Collections.unmodifiableList(salidas);
    }

    public double getMejorEntrada() {
        return mejorEntrada;
    }

    public double getPeorEntrada() {
        return peorEntrada;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setPeorEntrada(double peorEntrada) {
        this.peorEntrada = peorEntrada;
    }

    public void setMejorEntrada(double mejorEntrada) {
        this.mejorEntrada = mejorEntrada;
    }

    // Define el intervalo en el que el lugar esta cerrado
    public void setVentana(double ventanaDesde, double ventanaHasta) {
        this.tieneVentana = true;
        this.ventanaDesde = ventanaDesde;
        this.ventanaHasta = ventanaHasta;

    }
}
