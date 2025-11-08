package modelos;

import interfaces.DomainEvent;

import java.util.UUID;

/**
 * Evento publicado cuando se crea un nuevo partido.
 */
public class PartidoCreado implements DomainEvent {
    private final UUID partidoId;

    public PartidoCreado(UUID partidoId) {
        this.partidoId = partidoId;
    }

    public UUID getPartidoId() {
        return partidoId;
    }
}

