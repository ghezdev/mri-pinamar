package modelos;
import interfaces.PartidoState;

import modelos.Partido;
import modelos.Usuario;

import java.time.Clock;

/**
 * Estado final cuando el partido ha terminado.
 * Patrón State: estado terminal, no permite transiciones.
 */
public class FinalizadoState implements PartidoState {
    @Override
    public void intentarAgregarJugador(Partido partido, Usuario usuario) {
        throw new IllegalStateException("No se pueden agregar jugadores a un partido finalizado");
    }

    @Override
    public void confirmar(Partido partido, Usuario usuario) {
        throw new IllegalStateException("No se puede confirmar un partido finalizado");
    }

    @Override
    public void iniciar(Partido partido, Clock clock) {
        throw new IllegalStateException("No se puede iniciar un partido finalizado");
    }

    @Override
    public void finalizar(Partido partido) {
        throw new IllegalStateException("El partido ya está finalizado");
    }

    @Override
    public void removerJugador(Partido partido, Usuario usuario) {
        throw new IllegalStateException("No se pueden remover jugadores de un partido finalizado");
    }

    @Override
    public void cancelar(Partido partido) {
        throw new IllegalStateException("No se puede cancelar un partido finalizado");
    }

    @Override
    public EstadoTipo tipo() {
        return EstadoTipo.FINALIZADO;
    }
}

