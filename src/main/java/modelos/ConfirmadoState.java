package modelos;
import interfaces.PartidoState;

import modelos.Partido;
import modelos.Usuario;

import java.time.Clock;

/**
 * Estado cuando el partido está confirmado y listo para iniciar.
 * Patrón State: implementa la lógica para este estado específico.
 */
public class ConfirmadoState implements PartidoState {
    public ConfirmadoState() {
    }

    @Override
    public void intentarAgregarJugador(Partido partido, Usuario usuario) {
        throw new IllegalStateException("No se pueden agregar jugadores a un partido confirmado");
    }

    @Override
    public void confirmar(Partido partido, Usuario usuario) {
        throw new IllegalStateException("El partido ya está confirmado");
    }

    @Override
    public void iniciar(Partido partido, Clock clock) {
        // Verificar que la fecha/hora del partido haya llegado
        if (partido.getFechaHora().isAfter(clock.instant())) {
            throw new IllegalStateException("El partido aún no puede iniciar, la fecha/hora no ha llegado");
        }

        // El evento se publicará automáticamente en cambiarEstado()
        partido.cambiarEstado(new EnJuegoState());
    }

    @Override
    public void finalizar(Partido partido) {
        throw new IllegalStateException("No se puede finalizar un partido que no ha comenzado");
    }

    @Override
    public void removerJugador(Partido partido, Usuario usuario) {
        throw new IllegalStateException("No se pueden remover jugadores de un partido confirmado");
    }

    @Override
    public void cancelar(Partido partido) {
        partido.cambiarEstado(new CanceladoState());
    }

    @Override
    public EstadoTipo tipo() {
        return EstadoTipo.CONFIRMADO;
    }
}

