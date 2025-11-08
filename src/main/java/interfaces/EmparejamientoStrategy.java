package interfaces;

import modelos.Partido;
import modelos.Usuario;

import java.util.List;

/**
 * Interfaz que define el contrato para las estrategias de emparejamiento.
 * Patrón Strategy: permite intercambiar algoritmos de selección de jugadores.
 */
public interface EmparejamientoStrategy {
    /**
     * Filtra y ordena los candidatos según la estrategia específica.
     * @param partido el partido para el cual se buscan jugadores
     * @param candidatos lista de candidatos a filtrar
     * @return lista filtrada y ordenada de candidatos
     */
    List<Usuario> filtrarCandidatos(Partido partido, List<Usuario> candidatos);

    /**
     * Verifica si un usuario cumple con los criterios de la estrategia para un partido.
     * @param partido el partido a verificar
     * @param usuario el usuario a verificar
     * @return true si el usuario cumple con los criterios, false en caso contrario
     */
    boolean cumpleCriterio(Partido partido, Usuario usuario);

    /**
     * Retorna el nombre de la estrategia.
     * @return nombre descriptivo
     */
    String nombre();
}

