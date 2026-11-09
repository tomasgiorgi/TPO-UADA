// Estado final de cada nodo del arbol (se usa en el log y en el conteo final)
public enum Estado {
    VIVO,                   // en la cola, esperando ser expandido
    EXPANDIDO,              // ya se generaron sus hijos
    SOLUCION,               // llego al 348 con todos los obligatorios
    SIN_OBLIGATORIOS,       // llego al 348 con obligatorios pendientes
    PODA_BATERIA,           // la bateria quedo en 0 o menos
    PODA_VENTANA,           // el lugar estaba cerrado al llegar
    PODA_NO_PUEDE_VOLVER,   // faltan obligatorios y ya no hay forma de entrar al 348
    PODA_COTA               // su CI supera la cota general U: no puede entrar al top 3
}
