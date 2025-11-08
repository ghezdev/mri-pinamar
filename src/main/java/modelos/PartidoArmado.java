package modelos;

import interfaces.DomainEvent;

import java.util.UUID;

/**
 * Evento publicado cuando un partido alcanza el cupo completo de jugadores.
 */
public class PartidoArmado implements DomainEvent {
    private final UUID partidoId;

    public PartidoArmado(UUID partidoId) {
        this.partidoId = partidoId;
    }

    public UUID getPartidoId() {
        return partidoId;
    }
}

