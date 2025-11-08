package controladores;

import servicios.GestionEstadoPartidoService;
import modelos.Partido;
import modelos.Usuario;
import repositorios.PartidoRepository;

import java.util.UUID;

/**
 * Controller para gestionar el estado de los partidos.
 * Patrón MVC: coordina entre la vista y el servicio de aplicación.
 */
public class GestionEstadoController {
    private final GestionEstadoPartidoService gestionEstadoPartidoService;
    private final PartidoRepository partidoRepository;
    private Usuario usuarioActual;

    public GestionEstadoController(GestionEstadoPartidoService gestionEstadoPartidoService,
                                  PartidoRepository partidoRepository) {
        this.gestionEstadoPartidoService = gestionEstadoPartidoService;
        this.partidoRepository = partidoRepository;
    }
    
    public void setUsuarioActual(Usuario usuario) {
        this.usuarioActual = usuario;
    }

    public Partido obtenerPartido(UUID partidoId) {
        return partidoRepository.findById(partidoId)
                .orElseThrow(() -> new IllegalArgumentException("Partido no encontrado"));
    }

    public void confirmar(UUID partidoId) {
        if (usuarioActual == null) {
            throw new IllegalStateException("Debe estar autenticado para confirmar un partido");
        }
        gestionEstadoPartidoService.confirmar(partidoId, usuarioActual.getId());
    }

    public void iniciar(UUID partidoId) {
        gestionEstadoPartidoService.iniciarSiCorresponde(partidoId);
    }

    public void finalizar(UUID partidoId) {
        gestionEstadoPartidoService.finalizar(partidoId);
    }

    public void cancelar(UUID partidoId) {
        if (usuarioActual == null) {
            throw new IllegalStateException("Debe estar autenticado para cancelar un partido");
        }
        // Verificar que el usuario sea el anfitrión
        Partido partido = partidoRepository.findById(partidoId)
                .orElseThrow(() -> new IllegalArgumentException("Partido no encontrado"));
        if (!partido.esCreador(usuarioActual.getId())) {
            throw new IllegalStateException("Solo el anfitrión del partido puede cancelarlo");
        }
        gestionEstadoPartidoService.cancelar(partidoId, usuarioActual.getId());
    }
}

