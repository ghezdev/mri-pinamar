package modelos;
import interfaces.EmparejamientoStrategy;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Factory para crear instancias de estrategias de emparejamiento.
 * Patrón Factory: centraliza la creación de estrategias.
 */
public class EmparejamientoStrategyFactory {
    
    /**
     * Crea una estrategia por su nombre.
     * @param nombre nombre de la estrategia
     * @return instancia de la estrategia
     * @throws IllegalArgumentException si el nombre no es válido
     */
    public static EmparejamientoStrategy crear(String nombre) {
        return crear(nombre, null, null);
    }
    
    /**
     * Crea una estrategia por su nombre con parámetros opcionales.
     * @param nombre nombre de la estrategia
     * @param nivelObjetivo nivel objetivo (solo para "Por Nivel")
     * @param tipoFiltro tipo de filtro "MINIMO" o "MAXIMO" (solo para "Por Nivel")
     * @return instancia de la estrategia
     * @throws IllegalArgumentException si el nombre no es válido
     */
    public static EmparejamientoStrategy crear(String nombre, modelos.NivelJuego nivelObjetivo, String tipoFiltro) {
        String nombreLower = nombre.toLowerCase();
        if ("por nivel".equals(nombreLower) || "nivel".equals(nombreLower)) {
            if (nivelObjetivo != null && tipoFiltro != null) {
                EmparejamientoPorNivel.TipoFiltro filtro = "MINIMO".equals(tipoFiltro) 
                    ? EmparejamientoPorNivel.TipoFiltro.MINIMO 
                    : EmparejamientoPorNivel.TipoFiltro.MAXIMO;
                System.out.println("    [FACTORY] Creando EmparejamientoPorNivel con nivelObjetivo=" + 
                    nivelObjetivo.getNombre() + " y tipoFiltro=" + filtro.getNombre());
                return new EmparejamientoPorNivel(nivelObjetivo, filtro);
            }
            System.out.println("    [FACTORY] Creando EmparejamientoPorNivel SIN parámetros (nivelObjetivo o tipoFiltro es null)");
            return new EmparejamientoPorNivel();
        } else if ("por cercanía".equals(nombreLower) || "cercanía".equals(nombreLower) || "cercania".equals(nombreLower)) {
            return new EmparejamientoPorCercania();
        } else if ("por historial".equals(nombreLower) || "historial".equals(nombreLower)) {
            return new EmparejamientoPorHistorial();
        } else {
            throw new IllegalArgumentException("Estrategia desconocida: " + nombre);
        }
    }

    /**
     * Retorna todas las estrategias disponibles.
     * @return lista de todas las estrategias
     */
    public static List<EmparejamientoStrategy> todas() {
        return Arrays.asList(
            new EmparejamientoPorNivel(),
            new EmparejamientoPorCercania(),
            new EmparejamientoPorHistorial()
        );
    }

    /**
     * Retorna los nombres de todas las estrategias disponibles.
     * @return lista de nombres
     */
    public static List<String> nombres() {
        return todas().stream()
                .map(EmparejamientoStrategy::nombre)
                .collect(Collectors.toList());
    }
}

