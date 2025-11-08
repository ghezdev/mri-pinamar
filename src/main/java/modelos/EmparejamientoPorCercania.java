package modelos;
import interfaces.EmparejamientoStrategy;

import modelos.Partido;
import modelos.Usuario;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Estrategia de emparejamiento que prioriza jugadores cercanos geográficamente.
 * Patrón Strategy: implementación concreta de la estrategia.
 */
public class EmparejamientoPorCercania implements EmparejamientoStrategy {
    @Override
    public List<Usuario> filtrarCandidatos(Partido partido, List<Usuario> candidatos) {
        if (candidatos.isEmpty() || partido.getZona() == null) {
            return new ArrayList<>(candidatos);
        }

        // Filtrar solo los usuarios de la misma zona
        return candidatos.stream()
                .filter(u -> u.getZona() != null)
                .filter(u -> u.getZona().equals(partido.getZona()))
                .collect(Collectors.toList());
    }
    
    @Override
    public boolean cumpleCriterio(Partido partido, Usuario usuario) {
        if (partido.getZona() == null || usuario.getZona() == null) {
            // Si no hay zona, no se puede verificar cercanía
            return false;
        }
        
        // La cercanía se verifica si están en la misma zona
        return partido.getZona().equals(usuario.getZona());
    }

    @Override
    public String nombre() {
        return "Por Cercanía";
    }
}

