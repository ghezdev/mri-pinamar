package modelos;

import interfaces.DomainEvent;

import java.util.UUID;

/**
 * Evento publicado cuando un partido es cancelado.
 */
public class PartidoCancelado implements DomainEvent {
    private final UUID partidoId;

    public PartidoCancelado(UUID partidoId) {
        this.partidoId = partidoId;
    }

    public UUID getPartidoId() {
        return partidoId;
    }
}

