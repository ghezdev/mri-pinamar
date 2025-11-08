package servicios;

import modelos.Usuario;
import modelos.Zona;
import repositorios.UsuarioRepository;
import modelos.RegistrarUsuarioCmd;

import java.util.UUID;

/**
 * Servicio de aplicación para registrar usuarios.
 */
public class RegistrarUsuarioService {
    private final UsuarioRepository usuarioRepository;

    public RegistrarUsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Usuario register(RegistrarUsuarioCmd cmd) {
        // Validaciones
        if (cmd.getUsername() == null || cmd.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("El username es requerido");
        }
        if (cmd.getEmail() == null || cmd.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("El email es requerido");
        }
        if (cmd.getPassword() == null || cmd.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("La contraseña es requerida");
        }

        // Verificar que no exista el username o email
        if (usuarioRepository.findByUsername(cmd.getUsername()).isPresent()) {
            throw new IllegalArgumentException("El username ya está en uso");
        }
        if (usuarioRepository.findByEmail(cmd.getEmail()).isPresent()) {
            throw new IllegalArgumentException("El email ya está en uso");
        }

        // Crear usuario
        Usuario usuario = new Usuario(
            UUID.randomUUID(),
            cmd.getUsername(),
            cmd.getEmail(),
            hashPassword(cmd.getPassword()) // En producción usar BCrypt o similar
        );

        if (cmd.getDeporteFavorito() != null) {
            usuario.setDeporteFavorito(cmd.getDeporteFavorito());
        }
        if (cmd.getNivelJuego() != null) {
            usuario.setNivelJuego(cmd.getNivelJuego());
        }
        if (cmd.getZona() != null) {
            usuario.setZona(cmd.getZona());
        }
        if (cmd.getPreferenciaNotificacion() != null) {
            usuario.setPreferenciaNotificacion(cmd.getPreferenciaNotificacion());
        }

        return usuarioRepository.save(usuario);
    }

    private String hashPassword(String password) {
        // Simulación: en producción usar BCrypt o similar
        return "hash_" + password;
    }
}

