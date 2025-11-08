package repositorios;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Repositorio para almacenar partidos pendientes de confirmación por usuario.
 * Se usa cuando un partido se arma y el usuario no está conectado.
 * Implementación en memoria.
 */
public class PartidoPendienteConfirmacionRepository {
    private static PartidoPendienteConfirmacionRepository instance;
    // Map: usuarioId -> Set de partidoIds pendientes
    private final Map<UUID, Set<UUID>> pendientes;

    private PartidoPendienteConfirmacionRepository() {
        this.pendientes = new ConcurrentHashMap<>();
    }

    /**
     * Obtiene la instancia única del repositorio (Singleton).
     */
    public static synchronized PartidoPendienteConfirmacionRepository getInstance() {
        if (instance == null) {
            instance = new PartidoPendienteConfirmacionRepository();
        }
        return instance;
    }

    /**
     * Agrega un partido pendiente de confirmación para un usuario.
     */
    public void agregarPendiente(UUID usuarioId, UUID partidoId) {
        pendientes.computeIfAbsent(usuarioId, k -> ConcurrentHashMap.newKeySet()).add(partidoId);
    }
    
    /**
     * Obtiene todos los partidos pendientes de confirmación para un usuario.
     */
    public List<UUID> obtenerPendientes(UUID usuarioId) {
        Set<UUID> partidos = pendientes.get(usuarioId);
        return partidos != null ? new ArrayList<>(partidos) : new ArrayList<>();
    }
    
    /**
     * Elimina un partido pendiente de confirmación para un usuario.
     */
    public void eliminarPendiente(UUID usuarioId, UUID partidoId) {
        Set<UUID> partidos = pendientes.get(usuarioId);
        if (partidos != null) {
            partidos.remove(partidoId);
            if (partidos.isEmpty()) {
                pendientes.remove(usuarioId);
            }
        }
    }
    
    /**
     * Elimina todos los partidos pendientes de confirmación para un usuario.
     */
    public void eliminarTodos(UUID usuarioId) {
        pendientes.remove(usuarioId);
    }
}

