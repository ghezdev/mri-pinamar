package modelos;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Representa las estadísticas de un partido finalizado.
 * Almacena goles y tarjetas por jugador (sin conceptos de equipos).
 */
public class EstadisticasPartido {
    private final UUID partidoId;
    // Map de jugadorId -> cantidad de goles
    private final Map<UUID, Integer> golesPorJugador;
    // Map de jugadorId -> cantidad de tarjetas
    private final Map<UUID, Integer> tarjetasPorJugador;

    public EstadisticasPartido(UUID partidoId) {
        this.partidoId = partidoId;
        this.golesPorJugador = new HashMap<>();
        this.tarjetasPorJugador = new HashMap<>();
    }

    public UUID getPartidoId() {
        return partidoId;
    }

    /**
     * Obtiene la cantidad de goles de un jugador.
     */
    public int getGolesJugador(UUID jugadorId) {
        return golesPorJugador.getOrDefault(jugadorId, 0);
    }

    /**
     * Establece la cantidad de goles de un jugador.
     */
    public void setGolesJugador(UUID jugadorId, int goles) {
        if (goles < 0) {
            throw new IllegalArgumentException("Los goles no pueden ser negativos");
        }
        if (goles == 0) {
            golesPorJugador.remove(jugadorId);
        } else {
            golesPorJugador.put(jugadorId, goles);
        }
    }

    /**
     * Obtiene la cantidad de tarjetas de un jugador.
     */
    public int getTarjetasJugador(UUID jugadorId) {
        return tarjetasPorJugador.getOrDefault(jugadorId, 0);
    }

    /**
     * Establece la cantidad de tarjetas de un jugador.
     */
    public void setTarjetasJugador(UUID jugadorId, int tarjetas) {
        if (tarjetas < 0) {
            throw new IllegalArgumentException("Las tarjetas no pueden ser negativas");
        }
        if (tarjetas == 0) {
            tarjetasPorJugador.remove(jugadorId);
        } else {
            tarjetasPorJugador.put(jugadorId, tarjetas);
        }
    }

    /**
     * Obtiene todos los jugadores que tienen goles registrados.
     */
    public Map<UUID, Integer> getGolesPorJugador() {
        return new HashMap<>(golesPorJugador);
    }

    /**
     * Obtiene todos los jugadores que tienen tarjetas registradas.
     */
    public Map<UUID, Integer> getTarjetasPorJugador() {
        return new HashMap<>(tarjetasPorJugador);
    }

    /**
     * Obtiene el ID del jugador con más goles (goleador).
     */
    public UUID getGoleadorId() {
        return golesPorJugador.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    /**
     * Obtiene el total de goles del partido.
     */
    public int getTotalGoles() {
        return golesPorJugador.values().stream()
                .mapToInt(Integer::intValue)
                .sum();
    }

    /**
     * Obtiene el total de tarjetas del partido.
     */
    public int getTotalTarjetas() {
        return tarjetasPorJugador.values().stream()
                .mapToInt(Integer::intValue)
                .sum();
    }

    /**
     * Verifica si las estadísticas están completas.
     * Por ahora, solo verifica que haya al menos un jugador con datos.
     */
    public boolean estaCompleta() {
        return !golesPorJugador.isEmpty() || !tarjetasPorJugador.isEmpty();
    }
}

