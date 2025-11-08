package servicios;

import modelos.EstadisticasPartido;
import modelos.Partido;
import modelos.EstadoTipo;
import repositorios.EstadisticasPartidoRepository;
import repositorios.PartidoRepository;
import modelos.CargarEstadisticasCmd;

import java.util.UUID;

/**
 * Servicio de aplicación para cargar estadísticas de partidos.
 * Valida que solo el anfitrión pueda cargar estadísticas y que el partido esté finalizado.
 */
public class CargarEstadisticasService {
    private final EstadisticasPartidoRepository estadisticasRepository;
    private final PartidoRepository partidoRepository;

    public CargarEstadisticasService(EstadisticasPartidoRepository estadisticasRepository,
                                     PartidoRepository partidoRepository) {
        this.estadisticasRepository = estadisticasRepository;
        this.partidoRepository = partidoRepository;
    }

    /**
     * Carga las estadísticas de un partido.
     * 
     * @param cmd comando con los datos de las estadísticas
     * @param usuarioId ID del usuario que intenta cargar las estadísticas
     * @return las estadísticas guardadas
     * @throws IllegalStateException si el partido no está finalizado
     * @throws IllegalArgumentException si el usuario no es el anfitrión o si los datos son inválidos
     */
    public EstadisticasPartido cargarEstadisticas(CargarEstadisticasCmd cmd, UUID usuarioId) {
        // Validar que el partido existe
        Partido partido = partidoRepository.findById(cmd.getPartidoId())
                .orElseThrow(() -> new IllegalArgumentException("El partido no existe"));

        // Validar que el partido esté finalizado
        if (!EstadoTipo.FINALIZADO.equals(partido.getEstado().tipo())) {
            throw new IllegalStateException("Solo se pueden cargar estadísticas para partidos finalizados");
        }

        // Validar que el usuario sea el anfitrión
        if (!partido.esCreador(usuarioId)) {
            // Mensaje más descriptivo para debugging
            String mensajeError = String.format(
                "Solo el anfitrión del partido puede cargar estadísticas. " +
                "Creador del partido: %s, Usuario actual: %s",
                partido.getCreadorId() != null ? partido.getCreadorId().toString() : "null",
                usuarioId.toString()
            );
            throw new IllegalArgumentException(mensajeError);
        }

        // Validar que todos los jugadores con estadísticas estén en el partido
        java.util.Set<UUID> jugadoresPartido = partido.getJugadores().stream()
                .map(j -> j.getId())
                .collect(java.util.stream.Collectors.toSet());

        // Validar goles por jugador
        for (java.util.Map.Entry<UUID, Integer> entry : cmd.getGolesPorJugador().entrySet()) {
            UUID jugadorId = entry.getKey();
            Integer goles = entry.getValue();
            
            if (!jugadoresPartido.contains(jugadorId)) {
                throw new IllegalArgumentException(
                    "El jugador con ID " + jugadorId + " no está en el partido");
            }
            if (goles < 0) {
                throw new IllegalArgumentException("Los goles no pueden ser negativos");
            }
        }

        // Validar tarjetas por jugador
        for (java.util.Map.Entry<UUID, Integer> entry : cmd.getTarjetasPorJugador().entrySet()) {
            UUID jugadorId = entry.getKey();
            Integer tarjetas = entry.getValue();
            
            if (!jugadoresPartido.contains(jugadorId)) {
                throw new IllegalArgumentException(
                    "El jugador con ID " + jugadorId + " no está en el partido");
            }
            if (tarjetas < 0) {
                throw new IllegalArgumentException("Las tarjetas no pueden ser negativas");
            }
        }

        // Obtener estadísticas existentes o crear nuevas
        EstadisticasPartido estadisticas = estadisticasRepository.findByPartidoId(cmd.getPartidoId())
                .orElse(new EstadisticasPartido(cmd.getPartidoId()));

        // Actualizar estadísticas (solo una estadística por partido - se actualiza si ya existe)
        // Limpiar estadísticas anteriores y establecer las nuevas
        for (java.util.Map.Entry<UUID, Integer> entry : cmd.getGolesPorJugador().entrySet()) {
            estadisticas.setGolesJugador(entry.getKey(), entry.getValue());
        }
        for (java.util.Map.Entry<UUID, Integer> entry : cmd.getTarjetasPorJugador().entrySet()) {
            estadisticas.setTarjetasJugador(entry.getKey(), entry.getValue());
        }

        return estadisticasRepository.save(estadisticas);
    }

    /**
     * Obtiene las estadísticas de un partido.
     * 
     * @param partidoId ID del partido
     * @return las estadísticas si existen, null en caso contrario
     */
    public EstadisticasPartido obtenerEstadisticas(UUID partidoId) {
        return estadisticasRepository.findByPartidoId(partidoId).orElse(null);
    }
}

