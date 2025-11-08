package repositorios;

import modelos.Usuario;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Repositorio para la persistencia de usuarios.
 * Implementación en memoria.
 */
public class UsuarioRepository {
    private static UsuarioRepository instance;
    private final Map<UUID, Usuario> usuarios;
    private final Map<String, Usuario> usuariosPorUsername;
    private final Map<String, Usuario> usuariosPorEmail;

    private UsuarioRepository() {
        this.usuarios = new ConcurrentHashMap<>();
        this.usuariosPorUsername = new ConcurrentHashMap<>();
        this.usuariosPorEmail = new ConcurrentHashMap<>();
    }

    /**
     * Obtiene la instancia única del repositorio (Singleton).
     * @return la instancia única
     */
    public static synchronized UsuarioRepository getInstance() {
        if (instance == null) {
            instance = new UsuarioRepository();
        }
        return instance;
    }

    /**
     * Constructor público para permitir inyección de dependencias.
     */
    public UsuarioRepository(boolean useSingleton) {
        this.usuarios = new ConcurrentHashMap<>();
        this.usuariosPorUsername = new ConcurrentHashMap<>();
        this.usuariosPorEmail = new ConcurrentHashMap<>();
    }

    public Usuario save(Usuario usuario) {
        usuarios.put(usuario.getId(), usuario);
        usuariosPorUsername.put(usuario.getUsername().toLowerCase(), usuario);
        usuariosPorEmail.put(usuario.getEmail().toLowerCase(), usuario);
        return usuario;
    }

    public Optional<Usuario> findById(UUID id) {
        return Optional.ofNullable(usuarios.get(id));
    }

    public Optional<Usuario> findByUsername(String username) {
        return Optional.ofNullable(usuariosPorUsername.get(username.toLowerCase()));
    }

    public Optional<Usuario> findByEmail(String email) {
        return Optional.ofNullable(usuariosPorEmail.get(email.toLowerCase()));
    }

    public List<Usuario> findAll() {
        return new ArrayList<>(usuarios.values());
    }

    public void delete(UUID id) {
        Usuario usuario = usuarios.remove(id);
        if (usuario != null) {
            usuariosPorUsername.remove(usuario.getUsername().toLowerCase());
            usuariosPorEmail.remove(usuario.getEmail().toLowerCase());
        }
    }
}

