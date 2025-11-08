package modelos;

import modelos.Deporte;
import modelos.Zona;
import java.time.Instant;

/**
 * Comando para crear un nuevo partido.
 */
public class CrearPartidoCmd {
    private Deporte deporte;
    private int cupoRequerido;
    private int duracion; // en minutos
    private Zona zona;
    private Instant fechaHora;
    private String estrategiaEmparejamiento;
    private String nivelJuegoObjetivo; // Para estrategia "Por Nivel"
    private String tipoFiltroNivel; // "MINIMO" o "MAXIMO" para estrategia "Por Nivel"

    // Getters y setters
    public Deporte getDeporte() {
        return deporte;
    }

    public void setDeporte(Deporte deporte) {
        this.deporte = deporte;
    }

    public int getCupoRequerido() {
        return cupoRequerido;
    }

    public void setCupoRequerido(int cupoRequerido) {
        this.cupoRequerido = cupoRequerido;
    }

    public int getDuracion() {
        return duracion;
    }

    public void setDuracion(int duracion) {
        this.duracion = duracion;
    }

    public Zona getZona() {
        return zona;
    }

    public void setZona(Zona zona) {
        this.zona = zona;
    }

    public Instant getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(Instant fechaHora) {
        this.fechaHora = fechaHora;
    }

    public String getEstrategiaEmparejamiento() {
        return estrategiaEmparejamiento;
    }

    public void setEstrategiaEmparejamiento(String estrategiaEmparejamiento) {
        this.estrategiaEmparejamiento = estrategiaEmparejamiento;
    }

    public String getNivelJuegoObjetivo() {
        return nivelJuegoObjetivo;
    }

    public void setNivelJuegoObjetivo(String nivelJuegoObjetivo) {
        this.nivelJuegoObjetivo = nivelJuegoObjetivo;
    }

    public String getTipoFiltroNivel() {
        return tipoFiltroNivel;
    }

    public void setTipoFiltroNivel(String tipoFiltroNivel) {
        this.tipoFiltroNivel = tipoFiltroNivel;
    }
}

