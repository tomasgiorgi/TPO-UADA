import java.io.PrintWriter;
import java.util.*;

// Programa principal: lee el mapa, corre Branch & Bound y muestra el top 3 (o la ruta de contingencia)
public class Main {
    static final double BATERIA_INICIAL = 100;   // bateria con la que arranca Uada
    static final double BATERIA_MAXIMA = 100;    // tope de bateria al recargar

    // Desempate del top 3: menor tiempo, mayor bateria (a 2 decimales), menos lugares visitados, menor id
    static final Comparator<Nodo> ORDEN_SOLUCIONES = Comparator
            .comparingDouble(Nodo::getTiempo)
            .thenComparingDouble((Nodo s) -> -redondear(s.getBateria()))
            .thenComparingInt(Nodo::getTamanoVisitados)
            .thenComparingInt(Nodo::getId);

    public static void main(String[] args) throws Exception {
        // ===== 1. LEER EL MAPA =====
        // Archivo del mapa: por argumento, o se pide por consola
        String rutaArchivo;
        if (args.length > 0) {
            rutaArchivo = args[0];
        } else {
            System.out.print("Archivo del mapa: ");
            rutaArchivo = new Scanner(System.in).nextLine().trim().replace("\"", "");
        }

        // Si el archivo tiene errores, se muestra el motivo y el programa termina sin romperse
        LectorMapa mapa;
        try {
            mapa = new LectorMapa(rutaArchivo);
        } catch (Exception e) {
            System.out.println("No se pudo leer el mapa: " + e.getMessage());
            return;
        }

        // ===== 2. EJECUTAR EL ALGORITMO =====
        PrintWriter log = new PrintWriter("log.txt");          // archivo donde se registra todo el proceso
        Resultado resultado = ejecucionAlgoritmo(mapa, log);

        // Datos que devuelve el algoritmo
        Lugar inicio = mapa.getInicio();                       // Aula 348
        List<Nodo> soluciones = resultado.getSoluciones();
        Nodo mejorParcial = resultado.getMejorParcial();
        double mejorDistancia = resultado.getMejorDistancia();
        List<Nodo> todos = resultado.getTodos();
        int ordenExpansion = resultado.getNodosExpandidos();

        // ===== 3. RESULTADOS POR CONSOLA Y LOG =====
        System.out.println("Nodos creados: " + todos.size());
        System.out.println("Nodos expandidos: " + ordenExpansion);
        soluciones.sort(ORDEN_SOLUCIONES);
        for (Nodo s : soluciones) System.out.println("Solucion t=" + s.getTiempo() + " bat=" + redondear(s.getBateria()));
        System.out.println();

        if (!soluciones.isEmpty()) {
            // Hay soluciones: se muestran las 3 mejores, paso a paso
            soluciones.sort(ORDEN_SOLUCIONES);
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
            // No hay soluciones y Uada no pudo moverse a ningun lugar valido
            String msg = "SIN SOLUCION: no existe ningun recorrido que pase por los obligatorios y vuelva a " + inicio.getNombre();
            System.out.println(msg);
            log.println();
            log.println(msg);
        } else {
            // Contingencia: no hay soluciones, se muestra la ruta del nodo mas cercano al 348
            System.out.println("SIN SOLUCION. Distancia euclidea a " + inicio.getNombre() + ": " + mejorDistancia);
            System.out.println("Ruta mas cercana a " + inicio.getNombre() + ":");
            String ruta = describirRuta(reconstruirRuta(mejorParcial));
            System.out.print(ruta);

            log.println();
            log.println("SIN SOLUCION. Ruta mas cercana a " + inicio.getNombre() + " (distancia=" + mejorDistancia + ")");
            log.print(ruta);
        }

        // Arbol de exploracion completo en el log
        log.println();
        log.println("ARBOL DE EXPLORACION");
        escribirArbol(todos, log);

        // Cuenta cuantos nodos terminaron en cada estado (soluciones, cada tipo de poda, etc.)
        HashMap<Estado, Integer> conteo = new HashMap<>();
        for (Nodo n : todos) {
            Estado e = n.getEstado();
            if (!conteo.containsKey(e)) {
                conteo.put(e, 0);
            }
            conteo.put(e, conteo.get(e) + 1);
        }

        // Resumen final en el log
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

    // ===== ALGORITMO BRANCH & BOUND =====
    // Recibe el mapa ya leido y el log donde anota cada paso. Lo usan Main y Medicion.
    static Resultado ejecucionAlgoritmo(LectorMapa mapa, PrintWriter log) {
        // ===== A. ESTRUCTURAS PRINCIPALES =====
        PriorityQueue<Nodo> vivos = new PriorityQueue<>();     // cola de nodos vivos, ordenada por CI, CS e id (ver Nodo.compareTo)
        Lugar inicio = mapa.getInicio();                       // Aula 348
        Nodo.setInicio(inicio);                                // los nodos lo usan para calcular su distancia al 348
        List<Lugar> lugares = mapa.getLugares();               // lugares del grafo (sin zonas prohibidas)

        // ===== B. NODO RAIZ =====
        List<Nodo> todos = new ArrayList<>();                  // todos los nodos creados; la posicion es el id
        Nodo raiz = new Nodo(0, null, inicio, Modo.N, 0, BATERIA_INICIAL);   // Modo.N = sin modo (no se movio)
        raiz.registrarLlegada();                               // el 348 queda como visitado
        raiz.getPendientes().addAll(mapa.getObligatorios());   // pendientes: obligatorios + el 348 (hay que volver)
        todos.add(raiz);

        // ===== C. COTAS DE LA RAIZ Y COTA GENERAL (U) =====
        Cotas cotas = new Cotas(inicio, lugares);              // precalcula mejorEntrada y peorEntrada de cada lugar
        raiz.setCI(cotas.calcularCI(raiz.getTiempo(), raiz.getVisitados(), raiz.getPendientes(), inicio));
        raiz.setCS(cotas.calcularCS(raiz.getTiempo(), raiz.getVisitados()));

        double U = raiz.getCS();                               // cota general: arranca en la CS de la raiz
        List<Nodo> soluciones = new ArrayList<>();             // nodos que llegaron al 348 con todo hecho
        Nodo mejorParcial = null;                              // contingencia: nodo valido mas cercano al 348
        double mejorDistancia = Double.MAX_VALUE;              // contingencia: distancia de ese nodo al 348
        int ordenExpansion = 0;                                // cuenta en que orden se expanden los nodos

        vivos.add(raiz);

        // ===== D. CICLO PRINCIPAL DE BRANCH & BOUND =====
        while (!vivos.isEmpty()) {
            Nodo n = vivos.poll(); // saca el nodo de menor CI (el mas prometedor)

            // Poda por cota al sacar: si su CI supera U, este y todos los que quedan (CI mayor o igual) se podan
            if (n != raiz && n.getCI() > U) { //Si No hay manera de volver al 348, expandimos la raiz porque en los hijos nos damos cuenta de que no hay solucion.
                log.println("PODA | N" + n.getId() + " CI=" + n.getCI() + " > U=" + U + " | se podan los vivos restantes");
                n.setEstado(Estado.PODA_COTA);
                while (!vivos.isEmpty()) {
                    vivos.poll().setEstado(Estado.PODA_COTA);
                }
                break;
            }

            // El nodo se expande: se registra su orden y se anota en el log
            ordenExpansion ++;
            n.setOrdenExpansion(ordenExpansion);
            n.setEstado(Estado.EXPANDIDO);
            log.println("PASO " + ordenExpansion + " | extraido N" + n.getId() + " | CI=" + n.getCI() + " | CS=" + n.getCS() + " | U=" + U + " | vivos=" + vivos.size());

            // ===== E. GENERAR HIJOS: una rama por cada arista de salida y cada modo permitido =====
            for (Arista a : n.getLugar().getSalidas()) {
                for (Modo m : a.getModos()) {
                    Lugar destino = a.getDestino();

                    // Lugar ya visitado: no se crea el hijo (el 348 se permite porque es el final)
                    if (n.getVisitados().contains(a.getDestino()) && a.getDestino() != inicio) continue;

                    // Tiempo y bateria al llegar al destino con este modo (todavia sin recargar)
                    double tiempoNuevo = n.getTiempo() + m.tiempo(a.getTiempo());
                    double bateriaNueva = n.getBateria() - m.bateria(a.getTiempo());
                    Nodo hijo = new Nodo(todos.size(), n, destino, m, tiempoNuevo, bateriaNueva);
                    todos.add(hijo);

                    // --- Llegada al 348: es solucion o se descarta ---
                    if (destino == inicio ) {
                        hijo.registrarLlegada();               // saca el 348 de pendientes
                        if (hijo.getPendientes().isEmpty() && bateriaNueva > 0) {
                            // SOLUCION: no quedan pendientes y la bateria alcanzo. Recarga en el 348 con tope 100
                            hijo.setBateria(Math.min(BATERIA_MAXIMA, bateriaNueva + destino.recarga()));
                            hijo.setEstado(Estado.SOLUCION);
                            hijo.setCI(hijo.getTiempo());
                            hijo.setCS(hijo.getTiempo());
                            soluciones.add(hijo);

                            // Con 3 o mas soluciones, U pasa a ser el tiempo de la tercera mejor
                            if (soluciones.size() >= 3) {
                                soluciones.sort(ORDEN_SOLUCIONES);
                                U = soluciones.get(2).getTiempo();
                                log.println("U actualizado a " + U + " (tercera mejor solucion)");
                            }
                        } else {
                            // Llego al 348 con obligatorios pendientes (o sin bateria): no sirve
                            hijo.setEstado(Estado.SIN_OBLIGATORIOS);
                        }
                        continue;
                    }

                    // --- Podas de factibilidad ---
                    // Bateria agotada: llegar con 0 o menos es fallar
                    if (bateriaNueva <= 0) {
                        hijo.setEstado(Estado.PODA_BATERIA);
                        continue;
                    }
                    // Ventana de tiempo: el lugar esta cerrado en el momento de llegada
                    if (destino.cerradoEn(tiempoNuevo)) {
                        hijo.setEstado(Estado.PODA_VENTANA);
                        continue;
                    }

                    // El hijo es valido: se marca la visita y se recarga si es un aula (tope 100)
                    hijo.registrarLlegada();
                    hijo.setBateria(Math.min(BATERIA_MAXIMA, bateriaNueva + destino.recarga()));

                    // Contingencia: se guarda el nodo valido mas cercano al 348 (empate: menor tiempo)
                    if (hijo.getDistanciaAl348() < mejorDistancia || (hijo.getDistanciaAl348() == mejorDistancia && hijo.getTiempo() < mejorParcial.getTiempo())) {
                        mejorDistancia = hijo.getDistanciaAl348();
                        mejorParcial = hijo;
                    }

                    // Poda "no puede volver al 348": faltan obligatorios y no queda ninguna entrada al 348 sin usar
                    if (!cotas.puedeVolverAl348(hijo.getVisitados(), hijo.getPendientes(), destino)) {
                        hijo.setEstado(Estado.PODA_NO_PUEDE_VOLVER);
                        continue;
                    }

                    // Cota inferior del hijo
                    hijo.setCI(cotas.calcularCI(hijo.getTiempo(), hijo.getVisitados(), hijo.getPendientes(), destino));

                    // Poda por cota al crear: ni en el mejor caso entra al top 3
                    if (hijo.getCI() > U) {
                        hijo.setEstado(Estado.PODA_COTA);
                        continue;
                    }

                    // Cota superior del hijo (sirve para desempatar la cola) y entra a la cola de vivos
                    hijo.setCS(cotas.calcularCS(hijo.getTiempo(), hijo.getVisitados()));
                    vivos.add(hijo);
                }
            }
        }

        // ===== F. DEVOLVER LO QUE NECESITA QUIEN LLAMA =====
        return new Resultado(soluciones, mejorParcial, mejorDistancia, todos, ordenExpansion);
    }

    // ===== FUNCIONES AUXILIARES =====

    // Arma la ruta desde la raiz hasta el nodo, siguiendo los padres hacia atras
    static List<Nodo> reconstruirRuta(Nodo nodo) {
        LinkedList<Nodo> ruta = new LinkedList<>();
        while (nodo != null) {
            ruta.addFirst(nodo);
            nodo = nodo.getPadre();
        }
        return ruta;
    }

    // Redondea a 2 decimales
    static double redondear(double x) {
        return Math.round(x * 100) / 100.0;
    }

    // Devuelve cuantos nodos hay en un estado (0 si no hay ninguno)
    static int contar(HashMap<Estado, Integer> conteo, Estado e) {
        if (conteo.containsKey(e)) {
            return conteo.get(e);
        }
        return 0;
    }

    // Texto de la ruta paso a paso: origen -> destino | modo | bateria restante | tiempo acumulado
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

    // Escribe el arbol de exploracion en el log: agrupa los hijos de cada nodo y arranca desde la raiz
    static void escribirArbol(List<Nodo> todos, PrintWriter log) {
        HashMap<Integer, List<Nodo>> hijos = new HashMap<>();   // id del padre -> lista de sus hijos

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

    // Escribe un nodo con sangria segun su nivel y despues, recursivamente, a sus hijos
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
