# TPO Programacion III: La mision de Uada (Branch & Bound)

Uada sale del Aula 348, pasa por los lugares obligatorios, respeta las ventanas de cierre y la bateria, y vuelve al 348 en el menor tiempo posible. El programa devuelve las 3 mejores rutas, o la ruta mas cercana al 348 si no hay solucion.

## Compilar

```
javac -d out src/*.java mediciones/Medicion.java
```

## Ejecutar

```
java -cp out Main mapas/mapa_chico.txt
```

Si no se pasa el archivo por argumento, el programa lo pide por consola. Genera `log.txt` con los pasos, las rutas, el arbol de exploracion y los totales.

## Medir tiempos

```
java -cp out Medicion mapas/mapa_grande.txt
```

Ejecuta el algoritmo 10 veces sin escribir el log y muestra el promedio.

## Mapas incluidos (carpeta mapas)

| Archivo 
|---
| mapa1.txt 
| mapa2.txt
| mapa3.txt
| mapa4.txt
| mapa_grande.txt
| mapa_grande_sin_solucion.txt

## Formato del archivo de mapa

```
LUGAR;nombre;aula(0 si no es aula);x;y;obligatorio(0/1);prohibido(0/1)
ARISTA;origen;destino;segundos;modos (C, S, P)
VENTANA;lugar;desde;hasta
INICIO;lugar
```

Las aristas son dirigidas (origen a destino). La ventana indica el intervalo en que el lugar esta cerrado, con los extremos incluidos.