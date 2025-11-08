package modelos;notify.observer;

import modelos.notify.observer.events.PartidoCreado;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class DomainEventPublisherTest {
    private DomainEventPublisher publisher;
    private AtomicInteger contadorEventos;

    @BeforeEach
    void setUp() {
        publisher = new DomainEventPublisher();
        contadorEventos = new AtomicInteger(0);
    }

    @Test
    void testPublicarEvento() {
        DomainEventSubscriber subscriber = event -> contadorEventos.incrementAndGet();
        publisher.subscribe(PartidoCreado.class, subscriber);

        PartidoCreado evento = new PartidoCreado(UUID.randomUUID());
        publisher.publish(evento);

        assertEquals(1, contadorEventos.get());
    }

    @Test
    void testMultipleSuscriptores() {
        AtomicInteger contador1 = new AtomicInteger(0);
        AtomicInteger contador2 = new AtomicInteger(0);

        DomainEventSubscriber subscriber1 = event -> contador1.incrementAndGet();
        DomainEventSubscriber subscriber2 = event -> contador2.incrementAndGet();

        publisher.subscribe(PartidoCreado.class, subscriber1);
        publisher.subscribe(PartidoCreado.class, subscriber2);

        PartidoCreado evento = new PartidoCreado(UUID.randomUUID());
        publisher.publish(evento);

        assertEquals(1, contador1.get());
        assertEquals(1, contador2.get());
    }

    @Test
    void testDesuscribir() {
        DomainEventSubscriber subscriber = event -> contadorEventos.incrementAndGet();
        publisher.subscribe(PartidoCreado.class, subscriber);

        PartidoCreado evento1 = new PartidoCreado(UUID.randomUUID());
        publisher.publish(evento1);
        assertEquals(1, contadorEventos.get());

        publisher.unsubscribe(PartidoCreado.class, subscriber);

        PartidoCreado evento2 = new PartidoCreado(UUID.randomUUID());
        publisher.publish(evento2);
        assertEquals(1, contadorEventos.get()); // No debe incrementar
    }
}

