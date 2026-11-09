import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

// Lee el archivo del mapa y arma el grafo: lugares, aristas, ventanas, obligatorios e inicio
public class LectorMapa {
    private final Lugar inicio;                                   // lugar de partida y de llegada (Aula 348)
    private final List<Lugar> lugares = new ArrayList<>();        // lugares del grafo (sin zonas prohibidas)
    private final List<Lugar> obligatorios = new ArrayList<>();   // obligatorios + el inicio (hay que volver)

    // Formato de las lineas:
    // LUGAR;nombre;aula(0 si no es aula);x;y;obligatorio(0/1);prohibido(0/1)
    // ARISTA;origen;destino;segundos;modos (C, S, P)   -> arista dirigida origen -> destino
    // VENTANA;lugar;desde;hasta                         -> cerrado en [desde, hasta]
    // INICIO;lugar
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

            // Columnas del LUGAR (obligatorio y prohibido son opcionales: si faltan valen 0)
            String nombre = p[1];
            int aula = (int) numero(p[2], nro);
            double x = numero(p[3], nro);
            double y = numero(p[4], nro);
            boolean esObligatorio = p.length > 5 && p[5].equals("1");
            boolean esProhibido = p.length > 6 && p[6].equals("1");

            // No se permiten dos lugares con el mismo nombre
            if (porNombre.containsKey(nombre.toLowerCase())) {
                throw new IllegalArgumentException("Linea " + nro + ": el lugar '" + nombre + "' esta repetido");
            }
            Lugar lugar = new Lugar(nombre, aula, x, y, esObligatorio, esProhibido);
            porNombre.put(nombre.toLowerCase(), lugar);   // queda en el mapa de nombres para validar las lineas que lo nombran

            // Zona prohibida: se borra del grafo (no entra a lugares ni a obligatorios)
            if (esProhibido) {
                if (esObligatorio) {
                    System.out.println("AVISO: '" + nombre + "' es obligatorio y prohibido; se borra del grafo y no se cuenta como obligatorio");
                }
                continue;
            }
            // Lugar normal: entra al grafo y, si corresponde, a los obligatorios
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

            // ARISTA: conexion dirigida origen -> destino con su tiempo y sus modos permitidos
            if (p[0].equals("ARISTA")) {
                exigir(p, 5, nro);
                Lugar origen = buscar(porNombre, p[1], nro);
                Lugar destino = buscar(porNombre, p[2], nro);
                double tiempo = numero(p[3], nro);

                // Modos permitidos, en el orden en que aparecen las letras (ese es el orden de creacion de los hijos)
                Set<Modo> modos = new LinkedHashSet<>();
                for (char letra : p[4].toUpperCase().toCharArray()) {
                    if (letra == ' ' || letra == ',') continue;
                    try {
                        modos.add(Modo.desdeChar(letra));
                    } catch (IllegalArgumentException e) {
                        throw new IllegalArgumentException("Linea " + nro + ": " + e.getMessage());
                    }
                }
                // Las aristas que entran o salen de una zona prohibida no se cargan
                if (origen.isProhibido() || destino.isProhibido()) continue;
                origen.agregarSalida(new Arista(origen, destino, tiempo, modos));

            } else if (p[0].equals("VENTANA")) {   // VENTANA: intervalo en el que el lugar esta cerrado
                exigir(p, 4, nro);
                Lugar lugar = buscar(porNombre, p[1], nro);
                if (lugar.isProhibido()) continue;   // el lugar no existe en el grafo
                lugar.setVentana(numero(p[2], nro), numero(p[3], nro));

            } else if (p[0].equals("INICIO")) {    // INICIO: lugar de partida (se guarda el nombre y se busca al final)
                exigir(p, 2, nro);
                nombreInicio = p[1];

            } else {                                // cualquier otro tipo de linea es un error del archivo
                throw new IllegalArgumentException("Linea " + nro + ": tipo de linea desconocido '" + p[0] + "'");
            }
        }

        if (lugares.isEmpty()) {
            throw new IllegalArgumentException("El archivo no tiene ningun LUGAR que no sea prohibido");
        }

        // 4) El inicio: el de la linea INICIO; si falta, usamos el primer lugar y avisamos
        if (nombreInicio.isEmpty()) {
            inicio = lugares.get(0);
            System.out.println("AVISO: el archivo no tiene linea INICIO, se usa '" + inicio.getNombre() + "'");
        } else {
            inicio = buscar(porNombre, nombreInicio, 0);
            if (inicio.isProhibido()) {
                throw new IllegalArgumentException("El lugar de inicio '" + inicio.getNombre() + "' no puede ser una zona prohibida");
            }
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

    // Controla que la linea tenga al menos la cantidad de columnas necesaria
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

    // Busca un lugar por nombre (sin importar mayusculas); si no existe, error con el numero de linea
    private static Lugar buscar(Map<String, Lugar> porNombre, String nombre, int nro) {
        Lugar lugar = porNombre.get(nombre.toLowerCase());
        if (lugar == null) {
            throw new IllegalArgumentException("Linea " + nro + ": el lugar '" + nombre
                    + "' no existe (tiene que estar declarado con LUGAR)");
        }
        return lugar;
    }

    // ===== Getters =====
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