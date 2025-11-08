package controladores;

import servicios.CargarEstadisticasService;
import modelos.EstadisticasPartido;
import modelos.Partido;
import modelos.Usuario;
import repositorios.PartidoRepository;
import modelos.CargarEstadisticasCmd;

import java.util.UUID;

/**
 * Controlador para gestionar estadísticas de partidos.
 * Patrón MVC: coordina entre la vista y los servicios de aplicación.
 */
public class EstadisticasController {
    private final CargarEstadisticasService cargarEstadisticasService;
    private final PartidoRepository partidoRepository;
    private PartidosController partidosController;

    public EstadisticasController(CargarEstadisticasService cargarEstadisticasService,
                                  PartidoRepository partidoRepository) {
        this.cargarEstadisticasService = cargarEstadisticasService;
        this.partidoRepository = partidoRepository;
    }

    public void setPartidosController(PartidosController partidosController) {
        this.partidosController = partidosController;
    }

    private Usuario getUsuarioActual() {
        return partidosController != null ? partidosController.getUsuarioActual() : null;
    }

    /**
     * Carga las estadísticas de un partido.
     * 
     * @param partidoId ID del partido
     * @param golesPorJugador Map de jugadorId -> cantidad de goles
     * @param tarjetasPorJugador Map de jugadorId -> cantidad de tarjetas
     */
    public void cargarEstadisticas(UUID partidoId, 
                                   java.util.Map<UUID, Integer> golesPorJugador,
                                   java.util.Map<UUID, Integer> tarjetasPorJugador) {
        Usuario usuarioActual = getUsuarioActual();
        if (usuarioActual == null) {
            throw new IllegalStateException("Debe estar autenticado para cargar estadísticas");
        }

        CargarEstadisticasCmd cmd = new CargarEstadisticasCmd();
        cmd.setPartidoId(partidoId);
        cmd.setGolesPorJugador(golesPorJugador);
        cmd.setTarjetasPorJugador(tarjetasPorJugador);

        cargarEstadisticasService.cargarEstadisticas(cmd, usuarioActual.getId());
    }

    /**
     * Obtiene las estadísticas de un partido.
     */
    public EstadisticasPartido obtenerEstadisticas(UUID partidoId) {
        return cargarEstadisticasService.obtenerEstadisticas(partidoId);
    }

    /**
     * Verifica si el usuario actual puede cargar estadísticas para un partido.
     */
    public boolean puedeCargarEstadisticas(UUID partidoId) {
        Usuario usuarioActual = getUsuarioActual();
        if (usuarioActual == null) {
            return false;
        }

        Partido partido = partidoRepository.findById(partidoId).orElse(null);
        if (partido == null) {
            return false;
        }

        return partido.esCreador(usuarioActual.getId()) &&
               modelos.EstadoTipo.FINALIZADO.equals(partido.getEstado().tipo());
    }
}

