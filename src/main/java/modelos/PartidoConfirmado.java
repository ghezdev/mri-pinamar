package modelos;

import interfaces.DomainEvent;

import java.util.UUID;

/**
 * Evento publicado cuando un partido es confirmado.
 */
public class PartidoConfirmado implements DomainEvent {
    private final UUID partidoId;

    public PartidoConfirmado(UUID partidoId) {
        this.partidoId = partidoId;
    }

    public UUID getPartidoId() {
        return partidoId;
    }
}

