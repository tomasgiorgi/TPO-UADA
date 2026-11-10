import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Scanner;

// Complejidad temporal practica: corre el algoritmo varias veces sobre un mapa y promedia el tiempo
public class Medicion {
    static final int REPETICIONES = 10;   // cantidad de ejecuciones a promediar

    public static void main(String[] args) {
        // ===== 1. LEER EL MAPA (fuera de la medicion) =====
        // Archivo del mapa: por argumento, o se pide por consola (igual que Main)
        String rutaArchivo;
        if (args.length > 0) {
            rutaArchivo = args[0];
        } else {
            System.out.print("Archivo del mapa: ");
            rutaArchivo = new Scanner(System.in).nextLine().trim().replace("\"", "");
        }

        LectorMapa mapa;
        try {
            mapa = new LectorMapa(rutaArchivo);
        } catch (Exception e) {
            System.out.println("No se pudo leer el mapa: " + e.getMessage());
            return;
        }

        // Log que descarta todo lo que se escribe: asi no se mide la escritura en disco ni se pisa log.txt
        PrintWriter logDescartado = new PrintWriter(OutputStream.nullOutputStream());

        // ===== 2. EJECUTAR Y MEDIR =====
        long sumaDuracion = 0;
        Resultado resultado = null;
        for (int i = 1; i <= REPETICIONES; i++) {
            long inicio = System.nanoTime();
            resultado = Main.ejecucionAlgoritmo(mapa, logDescartado);
            long fin = System.nanoTime();

            long duracion = fin - inicio;
            sumaDuracion += duracion;
            System.out.println("Ejecucion " + i + ": " + redondear(aMilisegundos(duracion)) + " ms");
        }

        // ===== 3. RESUMEN =====
        double promedio = aMilisegundos(sumaDuracion) / REPETICIONES;
        System.out.println();
        System.out.println("MEDICION: Lugares = " + mapa.getLugares().size() + " | Nodos creados = " + resultado.getTodos().size() + " | Nodos expandidos = " + resultado.getNodosExpandidos() + " | Tiempo promedio (" + REPETICIONES + " ejecuciones) = " + redondear(promedio) + " ms");
        logDescartado.close();
    }

    // Convierte nanosegundos a milisegundos (con decimales)
    static double aMilisegundos(long nanos) {
        return nanos / 1_000_000.0;
    }

    // Redondea a 3 decimales
    static double redondear(double x) {
        return Math.round(x * 1000) / 1000.0;
    }
}
