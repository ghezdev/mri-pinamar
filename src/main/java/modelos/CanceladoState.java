package modelos;
import interfaces.PartidoState;

import modelos.Partido;
import modelos.Usuario;

import java.time.Clock;

/**
 * Estado cuando el partido ha sido cancelado.
 * Patrón State: estado terminal, no permite transiciones.
 */
public class CanceladoState implements PartidoState {
    @Override
    public void intentarAgregarJugador(Partido partido, Usuario usuario) {
        throw new IllegalStateException("No se pueden agregar jugadores a un partido cancelado");
    }

    @Override
    public void confirmar(Partido partido, Usuario usuario) {
        throw new IllegalStateException("No se puede confirmar un partido cancelado");
    }

    @Override
    public void iniciar(Partido partido, Clock clock) {
        throw new IllegalStateException("No se puede iniciar un partido cancelado");
    }

    @Override
    public void finalizar(Partido partido) {
        throw new IllegalStateException("No se puede finalizar un partido cancelado");
    }

    @Override
    public void removerJugador(Partido partido, Usuario usuario) {
        throw new IllegalStateException("No se pueden remover jugadores de un partido cancelado");
    }

    @Override
    public void cancelar(Partido partido) {
        throw new IllegalStateException("El partido ya está cancelado");
    }

    @Override
    public EstadoTipo tipo() {
        return EstadoTipo.CANCELADO;
    }
}

