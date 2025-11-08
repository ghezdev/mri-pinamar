package modelos;
import interfaces.PartidoState;

import modelos.Partido;
import modelos.Usuario;

import java.time.Clock;

/**
 * Estado cuando el partido tiene todos los jugadores necesarios.
 * Patrón State: implementa la lógica para este estado específico.
 */
public class ArmadoState implements PartidoState {
    public ArmadoState() {
    }

    @Override
    public void intentarAgregarJugador(Partido partido, Usuario usuario) {
        throw new IllegalStateException("El partido ya tiene todos los jugadores necesarios");
    }

    @Override
    public void confirmar(Partido partido, Usuario usuario) {
        // Verificar que el usuario esté en el partido
        if (!partido.getJugadores().contains(usuario)) {
            throw new IllegalStateException("El usuario no está en este partido");
        }
        
        // Verificar que el usuario no haya confirmado ya
        if (partido.haConfirmado(usuario.getId())) {
            throw new IllegalStateException("El usuario ya confirmó este partido");
        }
        
        // Registrar la confirmación del usuario
        partido.registrarConfirmacion(usuario.getId());
        
        // Solo cambiar a CONFIRMADO si todos los jugadores confirmaron
        // El evento se publicará automáticamente en cambiarEstado()
        if (partido.todosConfirmaron()) {
            partido.cambiarEstado(new ConfirmadoState());
        }
    }
    
    @Override
    public void removerJugador(Partido partido, Usuario usuario) {
        if (!partido.getJugadores().contains(usuario)) {
            throw new IllegalStateException("El usuario no está en este partido");
        }
        
        // Remover el jugador
        partido.getJugadores().remove(usuario);
        // Si había confirmado, remover también de confirmaciones
        if (partido.haConfirmado(usuario.getId())) {
            partido.removerConfirmacion(usuario.getId());
        }
        
        // Si ahora no tiene el cupo completo, volver a NecesitamosJugadores
        if (partido.getJugadores().size() < partido.getCupoRequerido()) {
            partido.cambiarEstado(new NecesitamosJugadoresState());
        }
    }

    @Override
    public void iniciar(Partido partido, Clock clock) {
        throw new IllegalStateException("El partido debe estar confirmado antes de iniciar");
    }

    @Override
    public void finalizar(Partido partido) {
        throw new IllegalStateException("No se puede finalizar un partido que no ha comenzado");
    }

    @Override
    public void cancelar(Partido partido) {
        partido.cambiarEstado(new CanceladoState());
    }

    @Override
    public EstadoTipo tipo() {
        return EstadoTipo.ARMADO;
    }
}

