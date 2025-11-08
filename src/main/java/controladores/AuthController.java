package controladores;

import modelos.Usuario;
import repositorios.PartidoPendienteConfirmacionRepository;
import repositorios.UsuarioRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Controlador para autenticación y gestión de sesión.
 * Patrón MVC: coordina entre la vista y los repositorios/servicios.
 */
public class AuthController {
    private final UsuarioRepository usuarioRepository;
    private final PartidoPendienteConfirmacionRepository pendientesRepository;

    public AuthController(UsuarioRepository usuarioRepository,
                         PartidoPendienteConfirmacionRepository pendientesRepository) {
        this.usuarioRepository = usuarioRepository;
        this.pendientesRepository = pendientesRepository;
    }

    /**
     * Autentica un usuario por username y password.
     * En producción, esto debería usar un AuthService con hash de contraseñas.
     */
    public Optional<Usuario> login(String username, String password) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(username);
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            // En producción, comparar hash de contraseñas
            // Por ahora, asumimos que la contraseña es correcta si el usuario existe
            return Optional.of(usuario);
        }
        return Optional.empty();
    }

    /**
     * Obtiene los partidos pendientes de confirmación para un usuario.
     */
    public List<UUID> obtenerPartidosPendientes(UUID usuarioId) {
        return pendientesRepository.obtenerPendientes(usuarioId);
    }

    /**
     * Elimina un partido pendiente de confirmación para un usuario.
     */
    public void eliminarPartidoPendiente(UUID usuarioId, UUID partidoId) {
        pendientesRepository.eliminarPendiente(usuarioId, partidoId);
    }
}

