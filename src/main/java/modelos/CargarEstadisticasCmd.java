package modelos;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * DTO para cargar estadísticas de un partido.
 * Almacena goles y tarjetas por jugador.
 */
public class CargarEstadisticasCmd {
    private UUID partidoId;
    // Map de jugadorId -> cantidad de goles
    private Map<UUID, Integer> golesPorJugador;
    // Map de jugadorId -> cantidad de tarjetas
    private Map<UUID, Integer> tarjetasPorJugador;

    public CargarEstadisticasCmd() {
        this.golesPorJugador = new HashMap<>();
        this.tarjetasPorJugador = new HashMap<>();
    }

    public UUID getPartidoId() {
        return partidoId;
    }

    public void setPartidoId(UUID partidoId) {
        this.partidoId = partidoId;
    }

    public Map<UUID, Integer> getGolesPorJugador() {
        return golesPorJugador;
    }

    public void setGolesPorJugador(Map<UUID, Integer> golesPorJugador) {
        this.golesPorJugador = golesPorJugador != null ? new HashMap<>(golesPorJugador) : new HashMap<>();
    }

    public Map<UUID, Integer> getTarjetasPorJugador() {
        return tarjetasPorJugador;
    }

    public void setTarjetasPorJugador(Map<UUID, Integer> tarjetasPorJugador) {
        this.tarjetasPorJugador = tarjetasPorJugador != null ? new HashMap<>(tarjetasPorJugador) : new HashMap<>();
    }

    /**
     * Agrega o actualiza los goles de un jugador.
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
     * Agrega o actualiza las tarjetas de un jugador.
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
}

