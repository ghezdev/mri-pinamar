package modelos.match.state;

import modelos.model.Deporte;
import modelos.model.Futbol;
import modelos.model.Partido;
import modelos.model.Usuario;
import modelos.model.Zona;
import modelos.model.ZonaSur;
import modelos.notify.observer.DomainEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PartidoStateTest {
    private Partido partido;
    private DomainEventPublisher eventPublisher;
    private Usuario usuario1;
    private Usuario usuario2;
    private Deporte futbol;
    private Zona zonaSur;

    @BeforeEach
    void setUp() {
        eventPublisher = new DomainEventPublisher();
        futbol = new Futbol();
        zonaSur = new ZonaSur();
        partido = new Partido(
            UUID.randomUUID(),
            futbol,
            2, // Cupo de 2 para facilitar pruebas
            90,
            zonaSur,
            Instant.now().plusSeconds(3600),
            eventPublisher
        );

        usuario1 = new Usuario(UUID.randomUUID(), "user1", "user1@test.com", "hash");
        usuario2 = new Usuario(UUID.randomUUID(), "user2", "user2@test.com", "hash");
    }

    @Test
    void testTransicionNecesitamosJugadoresAArmado() {
        // Estado inicial: NECESITAMOS_JUGADORES
        assertEquals(EstadoTipo.NECESITAMOS_JUGADORES, partido.getEstado().tipo());

        // Agregar primer jugador
        partido.agregarJugador(usuario1);
        assertEquals(EstadoTipo.NECESITAMOS_JUGADORES, partido.getEstado().tipo());

        // Agregar segundo jugador (completa cupo) -> debe transicionar a ARMADO
        partido.agregarJugador(usuario2);
        assertEquals(EstadoTipo.ARMADO, partido.getEstado().tipo());
    }

    @Test
    void testTransicionArmadoAConfirmado() {
        // Llenar el partido
        partido.agregarJugador(usuario1);
        partido.agregarJugador(usuario2);
        assertEquals(EstadoTipo.ARMADO, partido.getEstado().tipo());

        // Confirmar (ambos jugadores deben confirmar)
        partido.confirmar(usuario1);
        partido.confirmar(usuario2);
        assertEquals(EstadoTipo.CONFIRMADO, partido.getEstado().tipo());
    }

    @Test
    void testNoSePuedeConfirmarSinEstarArmado() {
        // Intentar confirmar sin estar armado
        assertThrows(IllegalStateException.class, () -> partido.confirmar(usuario1));
    }

    @Test
    void testCancelarDesdeNecesitamosJugadores() {
        assertEquals(EstadoTipo.NECESITAMOS_JUGADORES, partido.getEstado().tipo());
        partido.cancelar();
        assertEquals(EstadoTipo.CANCELADO, partido.getEstado().tipo());
    }

    @Test
    void testNoSePuedeAgregarJugadorEnEstadoArmado() {
        partido.agregarJugador(usuario1);
        partido.agregarJugador(usuario2);
        assertEquals(EstadoTipo.ARMADO, partido.getEstado().tipo());

        Usuario usuario3 = new Usuario(UUID.randomUUID(), "user3", "user3@test.com", "hash");
        assertThrows(IllegalStateException.class, () -> partido.agregarJugador(usuario3));
    }

    @Test
    void testIniciarPartidoConfirmado() {
        partido.agregarJugador(usuario1);
        partido.agregarJugador(usuario2);
        partido.confirmar(usuario1);
        partido.confirmar(usuario2);
        assertEquals(EstadoTipo.CONFIRMADO, partido.getEstado().tipo());

        // Crear un clock con tiempo futuro para que el partido pueda iniciar
        Instant fechaPartido = Instant.now().minusSeconds(100);
        partido.setFechaHora(fechaPartido);
        Clock clock = Clock.fixed(fechaPartido.plusSeconds(200), ZoneId.systemDefault());

        partido.iniciar(clock);
        assertEquals(EstadoTipo.EN_JUEGO, partido.getEstado().tipo());
    }

    @Test
    void testFinalizarPartido() {
        partido.agregarJugador(usuario1);
        partido.agregarJugador(usuario2);
        partido.confirmar(usuario1);
        partido.confirmar(usuario2);
        Instant fechaPartido = Instant.now().minusSeconds(100);
        partido.setFechaHora(fechaPartido);
        Clock clock = Clock.fixed(fechaPartido.plusSeconds(200), ZoneId.systemDefault());
        partido.iniciar(clock);
        assertEquals(EstadoTipo.EN_JUEGO, partido.getEstado().tipo());

        partido.finalizar();
        assertEquals(EstadoTipo.FINALIZADO, partido.getEstado().tipo());
    }
}

