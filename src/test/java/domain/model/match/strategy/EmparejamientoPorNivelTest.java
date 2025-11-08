package modelos.match.strategy;

import modelos.model.Deporte;
import modelos.model.Futbol;
import modelos.model.NivelJuego;
import modelos.model.Partido;
import modelos.model.Usuario;
import modelos.model.Zona;
import modelos.model.ZonaSur;
import modelos.notify.observer.DomainEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class EmparejamientoPorNivelTest {
    private EmparejamientoPorNivel strategy;
    private Partido partido;
    private DomainEventPublisher eventPublisher;
    private Deporte futbol;
    private Zona zonaSur;

    @BeforeEach
    void setUp() {
        strategy = new EmparejamientoPorNivel();
        eventPublisher = new DomainEventPublisher();
        futbol = new Futbol();
        zonaSur = new ZonaSur();
        partido = new Partido(
            UUID.randomUUID(),
            futbol,
            10,
            90,
            zonaSur,
            Instant.now().plusSeconds(3600),
            eventPublisher
        );
    }

    @Test
    void testFiltrarCandidatosPorNivel() {
        // Crear usuarios con diferentes niveles
        Usuario principiante1 = crearUsuario("user1", NivelJuego.PRINCIPIANTE);
        Usuario intermedio1 = crearUsuario("user2", NivelJuego.INTERMEDIO);
        Usuario avanzado1 = crearUsuario("user3", NivelJuego.AVANZADO);
        Usuario principiante2 = crearUsuario("user4", NivelJuego.PRINCIPIANTE);

        List<Usuario> candidatos = new ArrayList<>();
        candidatos.add(avanzado1);
        candidatos.add(principiante1);
        candidatos.add(intermedio1);
        candidatos.add(principiante2);

        // Agregar un principiante al partido para que la estrategia priorice principiantes
        partido.agregarJugador(principiante1);

        List<Usuario> resultado = strategy.filtrarCandidatos(partido, candidatos);

        assertNotNull(resultado);
        assertFalse(resultado.isEmpty());
        // El primer candidato debería ser principiante (mismo nivel)
        assertEquals(NivelJuego.PRINCIPIANTE, resultado.get(0).getNivelJuego());
    }

    @Test
    void testFiltrarCandidatosVacio() {
        List<Usuario> candidatos = new ArrayList<>();
        List<Usuario> resultado = strategy.filtrarCandidatos(partido, candidatos);
        assertTrue(resultado.isEmpty());
    }

    private Usuario crearUsuario(String username, NivelJuego nivel) {
        Usuario usuario = new Usuario(UUID.randomUUID(), username, username + "@test.com", "hash");
        usuario.setNivelJuego(nivel);
        return usuario;
    }
}

