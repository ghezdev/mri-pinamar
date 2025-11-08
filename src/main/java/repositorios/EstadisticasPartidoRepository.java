package repositorios;

import modelos.EstadisticasPartido;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio de estadísticas de partidos.
 * Implementación en memoria.
 */
public class EstadisticasPartidoRepository {
    private static EstadisticasPartidoRepository instance;
    private final Map<UUID, EstadisticasPartido> estadisticas;

    private EstadisticasPartidoRepository() {
        this.estadisticas = new HashMap<>();
    }

    public static synchronized EstadisticasPartidoRepository getInstance() {
        if (instance == null) {
            instance = new EstadisticasPartidoRepository();
        }
        return instance;
    }

    /**
     * Guarda o actualiza las estadísticas de un partido.
     */
    public EstadisticasPartido save(EstadisticasPartido estadisticas) {
        this.estadisticas.put(estadisticas.getPartidoId(), estadisticas);
        return estadisticas;
    }

    /**
     * Busca las estadísticas de un partido por su ID.
     */
    public Optional<EstadisticasPartido> findByPartidoId(UUID partidoId) {
        return Optional.ofNullable(estadisticas.get(partidoId));
    }

    /**
     * Verifica si existen estadísticas para un partido.
     */
    public boolean existsByPartidoId(UUID partidoId) {
        return estadisticas.containsKey(partidoId);
    }
}

