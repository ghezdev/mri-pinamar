package modelos;

import interfaces.DomainEvent;

import java.util.UUID;

/**
 * Evento publicado cuando un partido finaliza.
 */
public class PartidoFinalizado implements DomainEvent {
    private final UUID partidoId;

    public PartidoFinalizado(UUID partidoId) {
        this.partidoId = partidoId;
    }

    public UUID getPartidoId() {
        return partidoId;
    }
}

