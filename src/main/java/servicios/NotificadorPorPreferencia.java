package servicios;

import modelos.Partido;
import modelos.Usuario;
import servicios.NotificationFacade;
import repositorios.PartidoRepository;
import repositorios.UsuarioRepository;
import interfaces.DomainEventSubscriber;
import interfaces.DomainEvent;
import modelos.PartidoArmado;
import modelos.PartidoConfirmado;
import modelos.PartidoEnJuego;
import modelos.PartidoFinalizado;
import modelos.PartidoCancelado;

/**
 * Suscriptor de eventos de dominio que envía notificaciones a los usuarios.
 * Patrón Observer: reacciona a eventos de dominio y delega al NotificationFacade.
 */
public class NotificadorPorPreferencia implements DomainEventSubscriber {
    private final NotificationFacade notificationFacade;
    private final PartidoRepository partidoRepository;
    private final UsuarioRepository usuarioRepository;

    public NotificadorPorPreferencia(NotificationFacade notificationFacade, 
                                     PartidoRepository partidoRepository,
                                     UsuarioRepository usuarioRepository) {
        this.notificationFacade = notificationFacade;
        this.partidoRepository = partidoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public void onEvent(DomainEvent event) {
        // PartidoCreado ahora se maneja en CrearPartidoService
        if (event instanceof PartidoArmado) {
            handlePartidoArmado((PartidoArmado) event);
        } else if (event instanceof PartidoConfirmado) {
            handlePartidoConfirmado((PartidoConfirmado) event);
        } else if (event instanceof PartidoEnJuego) {
            handlePartidoEnJuego((PartidoEnJuego) event);
        } else if (event instanceof PartidoFinalizado) {
            handlePartidoFinalizado((PartidoFinalizado) event);
        } else if (event instanceof PartidoCancelado) {
            handlePartidoCancelado((PartidoCancelado) event);
        }
    }

    private void handlePartidoArmado(PartidoArmado event) {
        Partido partido = partidoRepository.findById(event.getPartidoId()).orElse(null);
        if (partido != null) {
            String nombreDeporte = partido.getDeporte() != null ? partido.getDeporte().getNombre() : "desconocido";
            notificarJugadores(partido, "Partido Armado", 
                "El partido de " + nombreDeporte + " ya tiene todos los jugadores necesarios");
        }
    }

    private void handlePartidoConfirmado(PartidoConfirmado event) {
        Partido partido = partidoRepository.findById(event.getPartidoId()).orElse(null);
        if (partido != null) {
            String nombreDeporte = partido.getDeporte() != null ? partido.getDeporte().getNombre() : "desconocido";
            notificarJugadores(partido, "Partido Confirmado", 
                "El partido de " + nombreDeporte + " ha sido confirmado");
        }
    }

    private void handlePartidoEnJuego(PartidoEnJuego event) {
        Partido partido = partidoRepository.findById(event.getPartidoId()).orElse(null);
        if (partido != null) {
            String nombreDeporte = partido.getDeporte() != null ? partido.getDeporte().getNombre() : "desconocido";
            notificarJugadores(partido, "Partido Iniciado", 
                "El partido de " + nombreDeporte + " ha comenzado");
        }
    }

    private void handlePartidoFinalizado(PartidoFinalizado event) {
        Partido partido = partidoRepository.findById(event.getPartidoId()).orElse(null);
        if (partido != null) {
            String nombreDeporte = partido.getDeporte() != null ? partido.getDeporte().getNombre() : "desconocido";
            notificarJugadores(partido, "Partido Finalizado", 
                "El partido de " + nombreDeporte + " ha finalizado");
        }
    }

    private void handlePartidoCancelado(PartidoCancelado event) {
        Partido partido = partidoRepository.findById(event.getPartidoId()).orElse(null);
        if (partido != null) {
            String nombreDeporte = partido.getDeporte() != null ? partido.getDeporte().getNombre() : "desconocido";
            notificarJugadores(partido, "Partido Cancelado", 
                "El partido de " + nombreDeporte + " ha sido cancelado");
        }
    }

    private void notificarJugadores(Partido partido, String title, String body) {
        for (Usuario jugador : partido.getJugadores()) {
            notificationFacade.notifyUser(jugador, title, body);
        }
    }
}

