package controladores;

import servicios.RegistrarUsuarioService;
import modelos.Usuario;
import modelos.RegistrarUsuarioCmd;

/**
 * Controller para el registro de usuarios.
 * Patrón MVC: coordina entre la vista y el servicio de aplicación.
 */
public class RegistroController {
    private final RegistrarUsuarioService registrarUsuarioService;

    public RegistroController(RegistrarUsuarioService registrarUsuarioService) {
        this.registrarUsuarioService = registrarUsuarioService;
    }

    public Usuario registrar(RegistrarUsuarioCmd cmd) {
        try {
            return registrarUsuarioService.register(cmd);
        } catch (IllegalArgumentException e) {
            throw e; // Re-lanzar para que la vista maneje el error
        }
    }
}

