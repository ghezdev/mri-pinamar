package modelos;
import interfaces.PartidoState;

import modelos.Partido;
import modelos.Usuario;

import java.time.Clock;

/**
 * Estado cuando el partido está en curso.
 * Patrón State: implementa la lógica para este estado específico.
 */
public class EnJuegoState implements PartidoState {
    public EnJuegoState() {
    }

    @Override
    public void intentarAgregarJugador(Partido partido, Usuario usuario) {
        throw new IllegalStateException("No se pueden agregar jugadores a un partido en juego");
    }

    @Override
    public void confirmar(Partido partido, Usuario usuario) {
        throw new IllegalStateException("El partido ya está en juego");
    }

    @Override
    public void iniciar(Partido partido, Clock clock) {
        throw new IllegalStateException("El partido ya está en juego");
    }

    @Override
    public void finalizar(Partido partido) {
        // El evento se publicará automáticamente en cambiarEstado()
        partido.cambiarEstado(new FinalizadoState());
    }

    @Override
    public void removerJugador(Partido partido, Usuario usuario) {
        throw new IllegalStateException("No se pueden remover jugadores de un partido en juego");
    }

    @Override
    public void cancelar(Partido partido) {
        throw new IllegalStateException("No se puede cancelar un partido que ya está en juego");
    }

    @Override
    public EstadoTipo tipo() {
        return EstadoTipo.EN_JUEGO;
    }
}

