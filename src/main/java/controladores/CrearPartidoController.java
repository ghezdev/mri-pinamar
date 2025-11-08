package controladores;

import servicios.CrearPartidoService;
import modelos.Partido;
import modelos.CrearPartidoCmd;

import java.util.UUID;

/**
 * Controller para crear partidos.
 * Patrón MVC: coordina entre la vista y el servicio de aplicación.
 */
public class CrearPartidoController {
    private final CrearPartidoService crearPartidoService;
    private PartidosController partidosController;

    public CrearPartidoController(CrearPartidoService crearPartidoService) {
        this.crearPartidoService = crearPartidoService;
    }

    public void setPartidosController(PartidosController partidosController) {
        this.partidosController = partidosController;
    }

    public Partido crearPartido(CrearPartidoCmd cmd) {
        if (partidosController == null || partidosController.getUsuarioActual() == null) {
            throw new IllegalStateException("Debe estar autenticado para crear un partido");
        }
        return crearPartidoService.create(cmd, partidosController.getUsuarioActual().getId());
    }
}

