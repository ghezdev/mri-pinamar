package interfaces;

/**
 * Interfaz para suscriptores de eventos de dominio.
 * Patrón Observer: los suscriptores reaccionan a eventos publicados.
 */
public interface DomainEventSubscriber {
    /**
     * Método llamado cuando se publica un evento del tipo que el suscriptor está escuchando.
     * @param event el evento publicado
     */
    void onEvent(DomainEvent event);
}

