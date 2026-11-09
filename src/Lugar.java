import java.util.*;

public class Lugar {
    private String nombre;
    private int aula;
    private double x;
    private double y;
    private boolean obligatorio;
    private boolean prohibido;
    private boolean tieneVentana;
    private double ventanaDesde;
    private double ventanaHasta;
    List<Arista> salidas = new ArrayList<>();
    private double mejorEntrada = Double.POSITIVE_INFINITY;
    private double peorEntrada = 0;

    public Lugar (String nombre, int aula, double x, double y, boolean obligatorio, boolean prohibido) {
        this.nombre = nombre;
        this.aula = aula;
        this.x = x;
        this.y = y;
        this.obligatorio = obligatorio;
        this.prohibido = prohibido;
        this.tieneVentana = false;
    }

    public double recarga() {
        if (aula == 0) return 0;
        int suma = 0;
        for (char c : String.valueOf(aula).toCharArray()) suma += c - '0';
        return suma / 5.0;
    }

    public boolean cerradoEn(double tiempo) {
        return tieneVentana && ventanaDesde <= tiempo && tiempo <= ventanaHasta;
    }

    public double distanciaA(Lugar otro) {
        double dx = x - otro.x;
        double dy = y - otro.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    public void agregarSalida(Arista arista) {
        salidas.add(arista);
    }

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

    public void setVentana(double ventanaDesde, double ventanaHasta) {
        this.tieneVentana = true;
        this.ventanaDesde = ventanaDesde;
        this.ventanaHasta = ventanaHasta;

    }
}
