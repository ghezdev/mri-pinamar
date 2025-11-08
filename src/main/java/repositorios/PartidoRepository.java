package repositorios;

import modelos.Partido;
import modelos.BusquedaPartidosCriteria;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Repositorio para la persistencia de partidos.
 * Implementación en memoria.
 */
public class PartidoRepository {
    private static PartidoRepository instance;
    private final Map<UUID, Partido> partidos;

    private PartidoRepository() {
        this.partidos = new ConcurrentHashMap<>();
    }

    /**
     * Obtiene la instancia única del repositorio (Singleton).
     * @return la instancia única
     */
    public static synchronized PartidoRepository getInstance() {
        if (instance == null) {
            instance = new PartidoRepository();
        }
        return instance;
    }

    /**
     * Constructor público para permitir inyección de dependencias.
     */
    public PartidoRepository(boolean useSingleton) {
        this.partidos = new ConcurrentHashMap<>();
    }

    public Partido save(Partido partido) {
        partidos.put(partido.getId(), partido);
        return partido;
    }

    public Optional<Partido> findById(UUID id) {
        return Optional.ofNullable(partidos.get(id));
    }

    public List<Partido> findAll() {
        return new ArrayList<>(partidos.values());
    }

    public List<Partido> search(BusquedaPartidosCriteria criteria) {
        return partidos.values().stream()
                .filter(p -> criteria.getDeporte() == null || 
                            p.getDeporte().equals(criteria.getDeporte()))
                .filter(p -> criteria.getNecesitaJugadores() == null || 
                            (criteria.getNecesitaJugadores() && 
                             p.getJugadores().size() < p.getCupoRequerido()))
                .collect(Collectors.toList());
    }

    public void delete(UUID id) {
        partidos.remove(id);
    }
}

