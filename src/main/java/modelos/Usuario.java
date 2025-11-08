package modelos;

import java.util.UUID;

/**
 * Representa un usuario del sistema.
 */
public class Usuario {
    private final UUID id;
    private String username;
    private String email;
    private String passwordHash;
    private Deporte deporteFavorito;
    private NivelJuego nivelJuego;
    private Zona zona;
    private TipoNotificacion preferenciaNotificacion;

    public Usuario(UUID id, String username, String email, String passwordHash) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.preferenciaNotificacion = TipoNotificacion.EMAIL; // default
    }

    public UUID getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Deporte getDeporteFavorito() {
        return deporteFavorito;
    }

    public void setDeporteFavorito(Deporte deporteFavorito) {
        this.deporteFavorito = deporteFavorito;
    }

    public NivelJuego getNivelJuego() {
        return nivelJuego;
    }

    public void setNivelJuego(NivelJuego nivelJuego) {
        this.nivelJuego = nivelJuego;
    }

    public Zona getZona() {
        return zona;
    }

    public void setZona(Zona zona) {
        this.zona = zona;
    }

    public TipoNotificacion getPreferenciaNotificacion() {
        return preferenciaNotificacion;
    }

    public void setPreferenciaNotificacion(TipoNotificacion preferenciaNotificacion) {
        this.preferenciaNotificacion = preferenciaNotificacion;
    }

    @Override
    public String toString() {
        return "Usuario{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", deporteFavorito=" + (deporteFavorito != null ? deporteFavorito.getNombre() : "null") +
                ", nivelJuego=" + nivelJuego +
                '}';
    }
}

