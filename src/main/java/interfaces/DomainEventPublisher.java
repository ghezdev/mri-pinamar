package interfaces;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Publicador de eventos de dominio.
 * Patrón Observer: mantiene una lista de suscriptores y publica eventos.
 */
public class DomainEventPublisher {
    private final Map<Class<? extends DomainEvent>, List<DomainEventSubscriber>> subscribers;

    public DomainEventPublisher() {
        this.subscribers = new HashMap<>();
    }

    /**
     * Publica un evento a todos los suscriptores registrados para ese tipo de evento.
     * @param event el evento a publicar
     */
    public void publish(DomainEvent event) {
        Class<? extends DomainEvent> eventType = event.getClass();
        List<DomainEventSubscriber> eventSubscribers = subscribers.get(eventType);
        
        if (eventSubscribers != null) {
            for (DomainEventSubscriber subscriber : eventSubscribers) {
                subscriber.onEvent(event);
            }
        }
    }

    /**
     * Suscribe un subscriber a un tipo específico de evento.
     * @param eventType el tipo de evento
     * @param subscriber el suscriptor
     */
    public void subscribe(Class<? extends DomainEvent> eventType, DomainEventSubscriber subscriber) {
        subscribers.computeIfAbsent(eventType, k -> new ArrayList<>()).add(subscriber);
    }

    /**
     * Desuscribe un subscriber de un tipo específico de evento.
     * @param eventType el tipo de evento
     * @param subscriber el suscriptor
     */
    public void unsubscribe(Class<? extends DomainEvent> eventType, DomainEventSubscriber subscriber) {
        List<DomainEventSubscriber> eventSubscribers = subscribers.get(eventType);
        if (eventSubscribers != null) {
            eventSubscribers.remove(subscriber);
        }
    }
}

