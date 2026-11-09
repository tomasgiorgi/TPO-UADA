import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class LectorMapa {
    private final Lugar inicio;
    private final List<Lugar> lugares = new ArrayList<>();
    private final List<Lugar> obligatorios = new ArrayList<>();

    public LectorMapa(String ruta) throws FileNotFoundException {
        // 1) Leemos todas las lineas
        List<String> lineas = new ArrayList<>();
        Scanner sc = new Scanner(new File(ruta));
        while (sc.hasNextLine()) {
            lineas.add(sc.nextLine().replace("\uFEFF", "").trim());
        }
        sc.close();

        Map<String, Lugar> porNombre = new HashMap<>();   // clave: nombre en minuscula

        // 2) Primera pasada: solo los LUGAR
        for (int i = 0; i < lineas.size(); i++) {
            String[] p = partir(lineas.get(i));
            if (p == null || !p[0].equals("LUGAR")) continue;
            int nro = i + 1;
            exigir(p, 5, nro);

            String nombre = p[1];
            int aula = (int) numero(p[2], nro);
            double x = numero(p[3], nro);
            double y = numero(p[4], nro);
            boolean esObligatorio = p.length > 5 && p[5].equals("1");
            boolean esProhibido = p.length > 6 && p[6].equals("1");

            if (porNombre.containsKey(nombre.toLowerCase())) {
                throw new IllegalArgumentException("Linea " + nro + ": el lugar '" + nombre + "' esta repetido");
            }
            Lugar lugar = new Lugar(nombre, aula, x, y, esObligatorio, esProhibido);
            porNombre.put(nombre.toLowerCase(), lugar);
            lugares.add(lugar);
            if (esObligatorio) {
                obligatorios.add(lugar);
            }
        }

        // 3) Segunda pasada: aristas, ventanas e inicio
        String nombreInicio = "";
        for (int i = 0; i < lineas.size(); i++) {
            String[] p = partir(lineas.get(i));
            if (p == null || p[0].equals("LUGAR")) continue;
            int nro = i + 1;

            if (p[0].equals("ARISTA")) {
                exigir(p, 5, nro);
                Lugar origen = buscar(porNombre, p[1], nro);
                Lugar destino = buscar(porNombre, p[2], nro);
                double tiempo = numero(p[3], nro);

                Set<Modo> modos = new LinkedHashSet<>();
                for (char letra : p[4].toUpperCase().toCharArray()) {
                    if (letra == ' ' || letra == ',') continue;
                    try {
                        modos.add(Modo.desdeChar(letra));
                    } catch (IllegalArgumentException e) {
                        throw new IllegalArgumentException("Linea " + nro + ": " + e.getMessage());
                    }
                }
                if (origen.isProhibido() || destino.isProhibido()) continue;
                origen.agregarSalida(new Arista(origen, destino, tiempo, modos));

            } else if (p[0].equals("VENTANA")) {
                exigir(p, 4, nro);
                Lugar lugar = buscar(porNombre, p[1], nro);
                lugar.setVentana(numero(p[2], nro), numero(p[3], nro));

            } else if (p[0].equals("INICIO")) {
                exigir(p, 2, nro);
                nombreInicio = p[1];

            } else {
                throw new IllegalArgumentException("Linea " + nro + ": tipo de linea desconocido '" + p[0] + "'");
            }
        }

        if (lugares.isEmpty()) {
            throw new IllegalArgumentException("El archivo no tiene ningun LUGAR");
        }

        // 4) El inicio: el de la linea INICIO; si falta, usamos el primer lugar y avisamos
        if (nombreInicio.isEmpty()) {
            inicio = lugares.get(0);
            System.out.println("AVISO: el archivo no tiene linea INICIO, se usa '" + inicio.getNombre() + "'");
        } else {
            inicio = buscar(porNombre, nombreInicio, 0);
        }

        // el inicio va a pendientes porque hay que volver
        if (!obligatorios.contains(inicio)) {
            obligatorios.add(inicio);
        }
    }

    // Parte una linea en columnas. Devuelve null si es vacia o comentario.
    // p[0] queda en mayusculas, asi "lugar", "Lugar" y "LUGAR" valen igual.
    private static String[] partir(String linea) {
        if (linea.isEmpty() || linea.startsWith("#")) return null;
        String[] p = linea.split(";");
        for (int i = 0; i < p.length; i++) {
            p[i] = p[i].trim();
        }
        p[0] = p[0].toUpperCase();
        return p;
    }

    private static void exigir(String[] p, int minimo, int nro) {
        if (p.length < minimo) {
            throw new IllegalArgumentException("Linea " + nro + ": faltan columnas (hay " + p.length
                    + " y se necesitan al menos " + minimo + ")");
        }
    }

    // Acepta "30", "30.5" y "30,5"
    private static double numero(String texto, int nro) {
        try {
            return Double.parseDouble(texto.replace(',', '.'));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Linea " + nro + ": '" + texto + "' no es un numero");
        }
    }

    private static Lugar buscar(Map<String, Lugar> porNombre, String nombre, int nro) {
        Lugar lugar = porNombre.get(nombre.toLowerCase());
        if (lugar == null) {
            throw new IllegalArgumentException("Linea " + nro + ": el lugar '" + nombre
                    + "' no existe (tiene que estar declarado con LUGAR)");
        }
        return lugar;
    }

    public Lugar getInicio() {
        return inicio;
    }

    public List<Lugar> getObligatorios() {
        return obligatorios;
    }

    public List<Lugar> getLugares() {
        return lugares;
    }
}