package servicios;

import modelos.Partido;
import modelos.Usuario;
import interfaces.EmparejamientoStrategy;
import modelos.EmparejamientoStrategyFactory;
import repositorios.PartidoRepository;
import modelos.BusquedaPartidosCriteria;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Servicio de aplicación para buscar partidos.
 * Aplica filtros según las estrategias de emparejamiento de cada partido.
 */
public class BuscarPartidosService {
    private final PartidoRepository partidoRepository;

    public BuscarPartidosService(PartidoRepository partidoRepository) {
        this.partidoRepository = partidoRepository;
    }

    /**
     * Busca partidos aplicando los filtros de estrategia según el usuario actual.
     * Solo muestra partidos que:
     * 1. Necesiten jugadores
     * 2. El usuario cumpla con los criterios de la estrategia del partido
     */
    public List<Partido> search(BusquedaPartidosCriteria criteria, Usuario usuarioActual) {
        List<Partido> partidos = partidoRepository.search(criteria);
        
        if (usuarioActual == null) {
            // Si no hay usuario autenticado, solo filtrar por necesidad de jugadores
            return partidos.stream()
                    .filter(p -> p.getJugadores().size() < p.getCupoRequerido())
                    .collect(Collectors.toList());
        }
        
        // Filtrar partidos que necesiten jugadores Y el usuario cumpla con la estrategia
        return partidos.stream()
                .filter(p -> p.getJugadores().size() < p.getCupoRequerido()) // Solo partidos que necesiten jugadores
                .filter(p -> cumpleEstrategiaDelPartido(p, usuarioActual)) // Filtrar según estrategia
                .collect(Collectors.toList());
    }
    
    /**
     * Verifica si el usuario cumple con los criterios de la estrategia del partido.
     */
    private boolean cumpleEstrategiaDelPartido(Partido partido, Usuario usuario) {
        String nombreEstrategia = partido.getEstrategiaEmparejamiento();
        
        if (nombreEstrategia == null || nombreEstrategia.isEmpty()) {
            // Sin estrategia definida, aceptar todos
            return true;
        }
        
        // Crear la estrategia según el partido
        EmparejamientoStrategy strategy;
        if ("Por Nivel".equals(nombreEstrategia) && partido.getNivelJuegoObjetivo() != null && partido.getTipoFiltroNivel() != null) {
            // Convertir string a NivelJuego
            modelos.NivelJuego nivelObjetivo = convertirStringANivelJuego(partido.getNivelJuegoObjetivo());
            strategy = EmparejamientoStrategyFactory.crear(nombreEstrategia, nivelObjetivo, partido.getTipoFiltroNivel());
        } else {
            strategy = EmparejamientoStrategyFactory.crear(nombreEstrategia);
        }
        
        // Verificar si el usuario cumple con los criterios
        return strategy.cumpleCriterio(partido, usuario);
    }
    
    private modelos.NivelJuego convertirStringANivelJuego(String nivelStr) {
        if ("PRINCIPIANTE".equals(nivelStr)) {
            return modelos.NivelJuego.PRINCIPIANTE;
        } else if ("INTERMEDIO".equals(nivelStr)) {
            return modelos.NivelJuego.INTERMEDIO;
        } else if ("AVANZADO".equals(nivelStr)) {
            return modelos.NivelJuego.AVANZADO;
        }
        return null;
    }

    public List<Partido> findAll() {
        return partidoRepository.findAll();
    }

    /**
     * Obtiene un partido por su ID.
     */
    public Partido findById(UUID partidoId) {
        return partidoRepository.findById(partidoId)
                .orElseThrow(() -> new IllegalArgumentException("Partido no encontrado"));
    }
}

