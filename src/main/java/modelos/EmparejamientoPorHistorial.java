package modelos;
import interfaces.EmparejamientoStrategy;

import modelos.Partido;
import modelos.Usuario;
import repositorios.PartidoHistorialRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Estrategia de emparejamiento que prioriza jugadores con historial de participación.
 * Patrón Strategy: implementación concreta de la estrategia.
 */
public class EmparejamientoPorHistorial implements EmparejamientoStrategy {
    private final PartidoHistorialRepository historialRepository;
    
    public EmparejamientoPorHistorial() {
        // Por defecto, usar la instancia singleton
        this.historialRepository = repositorios.PartidoHistorialRepository.getInstance();
    }
    
    public EmparejamientoPorHistorial(PartidoHistorialRepository historialRepository) {
        this.historialRepository = historialRepository;
    }

    @Override
    public List<Usuario> filtrarCandidatos(Partido partido, List<Usuario> candidatos) {
        if (candidatos.isEmpty()) {
            return new ArrayList<>();
        }

        // Priorizar usuarios que ya han jugado con algún jugador del partido
        List<Usuario> resultado = new ArrayList<>(candidatos);
        resultado.sort((u1, u2) -> {
            boolean u1TieneHistorial = tieneHistorialConJugadores(partido, u1);
            boolean u2TieneHistorial = tieneHistorialConJugadores(partido, u2);
            
            if (u1TieneHistorial && !u2TieneHistorial) {
                return -1; // u1 primero
            } else if (!u1TieneHistorial && u2TieneHistorial) {
                return 1; // u2 primero
            } else {
                return 0; // Mismo orden
            }
        });
        
        return resultado;
    }
    
    @Override
    public boolean cumpleCriterio(Partido partido, Usuario usuario) {
        // Si el partido no tiene jugadores aún, aceptar a todos
        if (partido.getJugadores().isEmpty()) {
            return true;
        }
        
        // Verificar si el usuario ha jugado con algún jugador del partido
        return tieneHistorialConJugadores(partido, usuario);
    }
    
    /**
     * Verifica si un usuario ha jugado con algún jugador del partido.
     */
    private boolean tieneHistorialConJugadores(Partido partido, Usuario usuario) {
        for (Usuario jugador : partido.getJugadores()) {
            if (historialRepository.hanJugadoJuntos(usuario.getId(), jugador.getId())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String nombre() {
        return "Por Historial";
    }
}

