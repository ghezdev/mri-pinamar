package application;

import adapters.persistence.InMemoryPartidoRepository;
import adapters.persistence.InMemoryUsuarioRepository;
import modelos.model.Deporte;
import modelos.model.Futbol;
import modelos.model.Zona;
import modelos.model.ZonaSur;
import modelos.notify.observer.DomainEventPublisher;
import modelos.notify.observer.events.PartidoCreado;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ports.persistence.UsuarioRepository;
import shared.CrearPartidoCmd;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class CrearPartidoServiceTest {
    private CrearPartidoService service;
    private InMemoryPartidoRepository repository;
    private UsuarioRepository usuarioRepository;
    private DomainEventPublisher eventPublisher;
    private AtomicInteger contadorEventos;
    private Deporte futbol;
    private Zona zonaSur;

    @BeforeEach
    void setUp() {
        repository = new InMemoryPartidoRepository(false);
        usuarioRepository = InMemoryUsuarioRepository.getInstance();
        eventPublisher = new DomainEventPublisher();
        contadorEventos = new AtomicInteger(0);
        futbol = new Futbol();
        zonaSur = new ZonaSur();
        
        // Suscribir a eventos para verificar publicación
        eventPublisher.subscribe(PartidoCreado.class, event -> contadorEventos.incrementAndGet());
        
        service = new CrearPartidoService(repository, usuarioRepository, eventPublisher);
    }

    @Test
    void testCrearPartidoExitoso() {
        CrearPartidoCmd cmd = new CrearPartidoCmd();
        cmd.setDeporte(futbol);
        cmd.setCupoRequerido(10);
        cmd.setDuracion(90);
        cmd.setZona(zonaSur);
        cmd.setFechaHora(Instant.now().plusSeconds(3600));

        var partido = service.create(cmd, UUID.randomUUID());

        assertNotNull(partido);
        assertEquals(futbol, partido.getDeporte());
        assertEquals(10, partido.getCupoRequerido());
        assertTrue(repository.findById(partido.getId()).isPresent());
        assertEquals(1, contadorEventos.get()); // Verificar que se publicó el evento
    }

    @Test
    void testCrearPartidoSinDeporte() {
        CrearPartidoCmd cmd = new CrearPartidoCmd();
        cmd.setCupoRequerido(10);
        cmd.setFechaHora(Instant.now().plusSeconds(3600));

        assertThrows(IllegalArgumentException.class, () -> service.create(cmd, UUID.randomUUID()));
    }

    @Test
    void testCrearPartidoConFechaPasada() {
        CrearPartidoCmd cmd = new CrearPartidoCmd();
        cmd.setDeporte(futbol);
        cmd.setCupoRequerido(10);
        cmd.setFechaHora(Instant.now().minusSeconds(3600));

        assertThrows(IllegalArgumentException.class, () -> service.create(cmd, UUID.randomUUID()));
    }
}

