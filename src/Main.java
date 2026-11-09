import java.io.PrintWriter;
import java.util.*;

public class Main {
    static final double BATERIA_INICIAL = 100;

    public static void main(String[] args) throws Exception {
        // Archivo del mapa: por argumento, o se pide por consola
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

        PrintWriter log = new PrintWriter("log.txt");
        PriorityQueue<Nodo> vivos = new PriorityQueue<>();
        Lugar inicio = mapa.getInicio();
        Nodo.setInicio(inicio);
        List<Lugar> lugares = mapa.getLugares();


        List<Nodo> todos = new ArrayList<>();
        Nodo raiz = new Nodo(0, null, inicio, Modo.N, 0, BATERIA_INICIAL);
        raiz.registrarLlegada();
        raiz.getPendientes().addAll(mapa.getObligatorios());
        todos.add(raiz);




        Cotas cotas = new Cotas(inicio, lugares);
        raiz.setCI(cotas.calcularCI(raiz.getTiempo(), raiz.getVisitados(), raiz.getPendientes(), inicio));
        raiz.setCS(cotas.calcularCS(raiz.getTiempo(), raiz.getVisitados()));

        double U = raiz.getCS();
        List<Nodo> soluciones = new ArrayList<>();
        Nodo mejorParcial = null;
        double mejorDistancia = Double.MAX_VALUE;
        int ordenExpansion = 0;

        vivos.add(raiz);

        while (!vivos.isEmpty()) {
            Nodo n = vivos.poll();
            if (n != raiz && n.getCI() > U) { //Si No hay manera de volver al 348, expandimos la raiz porque en los hijos nos damos cuenta de que no hay solucion.
                log.println("PODA | N" + n.getId() + " CI=" + n.getCI() + " > U=" + U + " | se podan los vivos restantes");
                n.setEstado(Estado.PODA_COTA);
                while (!vivos.isEmpty()) {
                    vivos.poll().setEstado(Estado.PODA_COTA);
                }
                break;

            }



            ordenExpansion ++;
            n.setOrdenExpansion(ordenExpansion);
            n.setEstado(Estado.EXPANDIDO);
            log.println("PASO " + ordenExpansion + " | extraido N" + n.getId() + " | CI=" + n.getCI() + " | CS=" + n.getCS() + " | U=" + U + " | vivos=" + vivos.size());

            for (Arista a : n.getLugar().getSalidas()) {
                for (Modo m : a.getModos()) {
                    Lugar destino = a.getDestino();
                    if (n.getVisitados().contains(a.getDestino()) && a.getDestino() != inicio) continue;
                    if (destino.isProhibido()) continue;
                    double tiempoNuevo = n.getTiempo() + m.tiempo(a.getTiempo());
                    double bateriaNueva = n.getBateria() - m.bateria(a.getTiempo());
                    Nodo hijo = new Nodo(todos.size(), n, destino, m, tiempoNuevo, bateriaNueva);
                    todos.add(hijo);
                    if (destino == inicio ) {
                        hijo.registrarLlegada();
                        if (hijo.getPendientes().isEmpty() && bateriaNueva > 0) {
                            hijo.setBateria(bateriaNueva + destino.recarga());
                            hijo.setEstado(Estado.SOLUCION);
                            hijo.setCI(hijo.getTiempo());
                            hijo.setCS(hijo.getTiempo());
                            soluciones.add(hijo);

                            if (soluciones.size() >= 3) {
                                soluciones.sort(Comparator.comparingDouble(Nodo::getTiempo).thenComparing(Comparator.comparingDouble(Nodo::getBateria).reversed()).thenComparingInt(Nodo::getTamanoVisitados));
                                U = soluciones.get(2).getTiempo();
                                log.println("U actualizado a " + U + " (tercera mejor solucion)");
                            }
                        } else {
                            hijo.setEstado(Estado.SIN_OBLIGATORIOS);
                        }
                        continue;
                    }
                    if (bateriaNueva <= 0) {
                        hijo.setEstado(Estado.PODA_BATERIA);
                        continue;
                    }
                    if (destino.cerradoEn(tiempoNuevo)) {
                        hijo.setEstado(Estado.PODA_VENTANA);
                        continue;
                    }
                    hijo.registrarLlegada();
                    hijo.setBateria(bateriaNueva + destino.recarga());
                    if (hijo.getDistanciaAl348() < mejorDistancia || (hijo.getDistanciaAl348() == mejorDistancia && hijo.getTiempo() < mejorParcial.getTiempo())) {
                        mejorDistancia = hijo.getDistanciaAl348();
                        mejorParcial = hijo;
                    }

                    if (!cotas.puedeVolverAl348(hijo.getVisitados(), hijo.getPendientes(), destino)) {
                        hijo.setEstado(Estado.PODA_NO_PUEDE_VOLVER);
                        continue;
                    }

                    hijo.setCI(cotas.calcularCI(hijo.getTiempo(), hijo.getVisitados(), hijo.getPendientes(), destino));

                    if (hijo.getCI() > U) {
                        hijo.setEstado(Estado.PODA_COTA);
                        continue;
                    }

                    hijo.setCS(cotas.calcularCS(hijo.getTiempo(), hijo.getVisitados()));
                    vivos.add(hijo);
                }
            }

        }

        System.out.println("Nodos creados: " + todos.size());
        System.out.println("Nodos expandidos: " + ordenExpansion);
        soluciones.sort(Comparator.comparingDouble(Nodo::getTiempo).thenComparing(Comparator.comparingDouble(Nodo::getBateria).reversed()).thenComparingInt(Nodo::getTamanoVisitados));
        for (Nodo s : soluciones) System.out.println("Solucion t=" + s.getTiempo() + " bat=" + redondear(s.getBateria()));
        System.out.println();
        if (!soluciones.isEmpty()) {
            soluciones.sort(Comparator.comparingDouble(Nodo::getTiempo).thenComparing(Comparator.comparingDouble(Nodo::getBateria).reversed()).thenComparingInt(Nodo::getTamanoVisitados));
            for (int i = 0; i < Math.min(3, soluciones.size()); i++) {
                Nodo s = soluciones.get(i);
                String ruta = describirRuta(reconstruirRuta(s));

                System.out.println("RUTA " + (i + 1) + " | tiempo=" + s.getTiempo());
                System.out.print(ruta);
                System.out.println();

                log.println();
                log.println("RUTA " + (i + 1) + " | tiempo=" + s.getTiempo() + " | bateria final=" + redondear(s.getBateria()));
                log.print(ruta);
            }
        } else if (mejorParcial == null) {
            String msg = "SIN SOLUCION: no existe ningun recorrido que pase por los obligatorios y vuelva a " + inicio.getNombre();
            System.out.println(msg);
            log.println();
            log.println(msg);
        } else {
            System.out.println("SIN SOLUCION. Distancia euclidea a " + inicio.getNombre() + ": " + mejorDistancia);
            System.out.println("Ruta mas cercana a " + inicio.getNombre() + ":");
            String ruta = describirRuta(reconstruirRuta(mejorParcial));
            System.out.print(ruta);

            log.println();
            log.println("SIN SOLUCION. Ruta mas cercana a " + inicio.getNombre() + " (distancia=" + mejorDistancia + ")");
            log.print(ruta);
        }

        log.println();
        log.println("ARBOL DE EXPLORACION");
        escribirArbol(todos, log);

        HashMap<Estado, Integer> conteo = new HashMap<>();
        for (Nodo n : todos) {
            Estado e = n.getEstado();
            if (!conteo.containsKey(e)) {
                conteo.put(e, 0);
            }
            conteo.put(e, conteo.get(e) + 1);
        }

        log.println();
        log.println("TOTAL: creados=" + todos.size() + " expandidos=" + ordenExpansion
                + " soluciones=" + contar(conteo, Estado.SOLUCION)
                + " podas_cota=" + contar(conteo, Estado.PODA_COTA)
                + " podas_ventana=" + contar(conteo, Estado.PODA_VENTANA)
                + " podas_no_puede_volver=" + contar(conteo, Estado.PODA_NO_PUEDE_VOLVER)
                + " podas_bateria=" + contar(conteo, Estado.PODA_BATERIA)
                + " sin_obligatorios=" + contar(conteo, Estado.SIN_OBLIGATORIOS));


        log.close();

    }

    static List<Nodo> reconstruirRuta(Nodo nodo) {
        LinkedList<Nodo> ruta = new LinkedList<>();
        while (nodo != null) {
            ruta.addFirst(nodo);
            nodo = nodo.getPadre();
        }
        return ruta;
    }

    static double redondear(double x) {
        return Math.round(x * 100) / 100.0;
    }

    static int contar(HashMap<Estado, Integer> conteo, Estado e) {
        if (conteo.containsKey(e)) {
            return conteo.get(e);
        }
        return 0;
    }

    static String describirRuta(List<Nodo> ruta) {
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < ruta.size(); i++) {
            Nodo anterior = ruta.get(i - 1);
            Nodo n = ruta.get(i);
            sb.append(anterior.getLugar().getNombre()).append(" -> ").append(n.getLugar().getNombre())
                    .append(" | ").append(n.getModo())
                    .append(" | bateria=").append(redondear(n.getBateria()))
                    .append(" | t=").append(n.getTiempo())
                    .append(System.lineSeparator());
        }
        return sb.toString();
    }

    static void escribirArbol(List<Nodo> todos, PrintWriter log) {
        HashMap<Integer, List<Nodo>> hijos = new HashMap<>();

        for (Nodo n : todos) {
            if (n.getPadre() != null) {
                int idPadre = n.getPadre().getId();
                if (!hijos.containsKey(idPadre)) {
                    hijos.put(idPadre, new ArrayList<>());
                }
                hijos.get(idPadre).add(n);
            }
        }

        escribirNodo(todos.get(0), hijos, 0, log);
    }

    static void escribirNodo(Nodo n, HashMap<Integer, List<Nodo>> hijos, int nivel, PrintWriter log) {
        String espacios = "";
        for (int i = 0; i < nivel; i++) {
            espacios = espacios + "  ";
        }

        log.println(espacios + "N" + n.getId() + " | " + n.getLugar().getNombre() + " " + n.getModo()
                + " (" + n.getLugar().getX() + "," + n.getLugar().getY() + ")"
                + " | t=" + n.getTiempo() + " | bat=" + redondear(n.getBateria())
                + " | CI=" + n.getCI() + " | CS=" + n.getCS()
                + " | " + n.getEstado() + " | orden=" + n.getOrdenExpansion());

        if (hijos.containsKey(n.getId())) {
            for (Nodo h : hijos.get(n.getId())) {
                escribirNodo(h, hijos, nivel + 1, log);
            }
        }

    }

}