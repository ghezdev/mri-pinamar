package modelos;

import interfaces.DomainEvent;

import java.util.UUID;

/**
 * Evento publicado cuando un partido inicia.
 */
public class PartidoEnJuego implements DomainEvent {
    private final UUID partidoId;

    public PartidoEnJuego(UUID partidoId) {
        this.partidoId = partidoId;
    }

    public UUID getPartidoId() {
        return partidoId;
    }
}

