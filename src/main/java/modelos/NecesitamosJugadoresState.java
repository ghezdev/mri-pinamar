package modelos;
import interfaces.PartidoState;

import modelos.Partido;
import modelos.Usuario;

import java.time.Clock;

/**
 * Estado inicial del partido: necesita más jugadores.
 * Patrón State: implementa la lógica para este estado específico.
 */
public class NecesitamosJugadoresState implements PartidoState {
    public NecesitamosJugadoresState() {
    }

    @Override
    public void intentarAgregarJugador(Partido partido, Usuario usuario) {
        if (partido.getJugadores().contains(usuario)) {
            throw new IllegalStateException("El usuario ya está en el partido");
        }

        partido.getJugadores().add(usuario);

        // Si se completó el cupo, transicionar a ARMADO
        // El evento se publicará automáticamente en cambiarEstado()
        if (partido.getJugadores().size() >= partido.getCupoRequerido()) {
            partido.cambiarEstado(new ArmadoState());
        }
    }

    @Override
    public void confirmar(Partido partido, Usuario usuario) {
        throw new IllegalStateException("No se puede confirmar un partido que aún necesita jugadores");
    }

    @Override
    public void iniciar(Partido partido, Clock clock) {
        throw new IllegalStateException("No se puede iniciar un partido que aún necesita jugadores");
    }

    @Override
    public void finalizar(Partido partido) {
        throw new IllegalStateException("No se puede finalizar un partido que aún necesita jugadores");
    }

    @Override
    public void removerJugador(Partido partido, Usuario usuario) {
        if (!partido.getJugadores().contains(usuario)) {
            throw new IllegalStateException("El usuario no está en este partido");
        }
        partido.getJugadores().remove(usuario);
        // Si había confirmado, remover también de confirmaciones
        if (partido.haConfirmado(usuario.getId())) {
            partido.removerConfirmacion(usuario.getId());
        }
    }

    @Override
    public void cancelar(Partido partido) {
        partido.cambiarEstado(new CanceladoState());
    }

    @Override
    public EstadoTipo tipo() {
        return EstadoTipo.NECESITAMOS_JUGADORES;
    }
}

