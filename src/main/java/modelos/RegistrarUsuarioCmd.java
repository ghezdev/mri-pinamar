package modelos;

import modelos.Deporte;
import modelos.NivelJuego;
import modelos.TipoNotificacion;
import modelos.Zona;

/**
 * Comando para registrar un nuevo usuario.
 */
public class RegistrarUsuarioCmd {
    private String username;
    private String email;
    private String password;
    private Deporte deporteFavorito;
    private NivelJuego nivelJuego;
    private Zona zona;
    private TipoNotificacion preferenciaNotificacion;

    // Getters y setters
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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
}

