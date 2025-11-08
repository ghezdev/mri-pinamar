package servicios;

import modelos.Partido;
import modelos.Usuario;
import interfaces.DomainEventSubscriber;
import interfaces.DomainEvent;
import modelos.PartidoArmado;
import repositorios.PartidoPendienteConfirmacionRepository;
import repositorios.PartidoRepository;
import controladores.PartidosController;

import javax.swing.*;

/**
 * Suscriptor de eventos que guarda partidos como pendientes de confirmación cuando se arman.
 * Patrón Observer: reacciona a eventos PartidoArmado.
 */
public class ConfirmacionPartidoSubscriber implements DomainEventSubscriber {
    private final PartidoRepository partidoRepository;
    private final PartidoPendienteConfirmacionRepository pendientesRepository;
    private final PartidosController partidosController;

    public ConfirmacionPartidoSubscriber(PartidoRepository partidoRepository,
                                        PartidoPendienteConfirmacionRepository pendientesRepository,
                                        PartidosController partidosController,
                                        JFrame parentFrame) {
        this.partidoRepository = partidoRepository;
        this.pendientesRepository = pendientesRepository;
        this.partidosController = partidosController;
        // parentFrame ya no se usa, pero se mantiene en el constructor para compatibilidad
    }

    @Override
    public void onEvent(DomainEvent event) {
        if (event instanceof PartidoArmado) {
            handlePartidoArmado((PartidoArmado) event);
        }
    }

    private void handlePartidoArmado(PartidoArmado event) {
        Partido partido = partidoRepository.findById(event.getPartidoId()).orElse(null);
        if (partido == null) {
            return;
        }

        // Para cada jugador del partido, guardar como pendiente de confirmación
        // La confirmación se hará desde el botón en "Mis Partidos"
        for (Usuario jugador : partido.getJugadores()) {
            // Siempre guardar como pendiente, independientemente de si está conectado o no
            // El usuario confirmará desde el botón en "Mis Partidos"
            pendientesRepository.agregarPendiente(jugador.getId(), partido.getId());
            System.out.println("📋 Partido " + partido.getId().toString().substring(0, 8) + 
                " guardado como pendiente de confirmación para " + jugador.getUsername());
        }
    }

}

