package controladores;

import servicios.BuscarPartidosService;
import servicios.GestionEstadoPartidoService;
import modelos.Partido;
import modelos.Usuario;
import modelos.BusquedaPartidosCriteria;

import java.util.List;
import java.util.UUID;

/**
 * Controller para buscar y gestionar partidos.
 * Patrón MVC: coordina entre la vista y los servicios de aplicación.
 */
public class PartidosController {
    private final BuscarPartidosService buscarPartidosService;
    private final GestionEstadoPartidoService gestionEstadoPartidoService;
    private final AuthController authController;
    private Usuario usuarioActual;

    public PartidosController(BuscarPartidosService buscarPartidosService,
                              GestionEstadoPartidoService gestionEstadoPartidoService,
                              AuthController authController) {
        this.buscarPartidosService = buscarPartidosService;
        this.gestionEstadoPartidoService = gestionEstadoPartidoService;
        this.authController = authController;
    }

    public void setUsuarioActual(Usuario usuario) {
        this.usuarioActual = usuario;
    }

    public Usuario getUsuarioActual() {
        return usuarioActual;
    }

    public List<Partido> buscarPartidos(BusquedaPartidosCriteria criteria) {
        return buscarPartidosService.search(criteria, usuarioActual);
    }

    public List<Partido> listarTodos() {
        return buscarPartidosService.findAll();
    }

    public void unirseAPartido(UUID partidoId) {
        if (usuarioActual == null) {
            throw new IllegalStateException("Debe estar autenticado para unirse a un partido");
        }
        gestionEstadoPartidoService.agregarJugador(partidoId, usuarioActual.getId());
    }

    public List<Usuario> buscarCandidatos(UUID partidoId, String estrategia) {
        return gestionEstadoPartidoService.buscarCandidatos(partidoId, estrategia);
    }
    
    /**
     * Confirma un partido.
     */
    public void confirmarPartido(UUID partidoId) {
        if (usuarioActual == null) {
            throw new IllegalStateException("Debe estar autenticado para confirmar un partido");
        }
        gestionEstadoPartidoService.confirmar(partidoId, usuarioActual.getId());
    }
    
    /**
     * Obtiene un partido por su ID.
     */
    public Partido obtenerPartidoPorId(UUID partidoId) {
        return buscarPartidosService.findById(partidoId);
    }
    
    /**
     * Obtiene un partido por su ID (puede retornar null si no existe).
     */
    public Partido obtenerPartidoPorIdOpcional(UUID partidoId) {
        try {
            return buscarPartidosService.findById(partidoId);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    
    /**
     * Cancela un partido (solo el anfitrión puede hacerlo).
     */
    public void cancelarPartido(UUID partidoId) {
        gestionEstadoPartidoService.cancelar(partidoId);
    }
    
    /**
     * Elimina un partido pendiente de confirmación para el usuario actual.
     */
    public void eliminarPartidoPendiente(UUID partidoId) {
        if (usuarioActual == null) {
            throw new IllegalStateException("Debe estar autenticado");
        }
        authController.eliminarPartidoPendiente(usuarioActual.getId(), partidoId);
    }
    
    /**
     * Obtiene los partidos pendientes de confirmación para el usuario actual.
     */
    public java.util.List<UUID> obtenerPartidosPendientes() {
        if (usuarioActual == null) {
            return new java.util.ArrayList<>();
        }
        return authController.obtenerPartidosPendientes(usuarioActual.getId());
    }
    
    /**
     * Cierra la sesión del usuario actual.
     */
    public void cerrarSesion() {
        this.usuarioActual = null;
    }
}

