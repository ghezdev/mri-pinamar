package modelos;

import modelos.Deporte;

/**
 * Criterios de búsqueda para partidos.
 */
public class BusquedaPartidosCriteria {
    private Deporte deporte;
    private String zona; // placeholder para ubicación
    private Boolean necesitaJugadores;

    public Deporte getDeporte() {
        return deporte;
    }

    public void setDeporte(Deporte deporte) {
        this.deporte = deporte;
    }

    public String getZona() {
        return zona;
    }

    public void setZona(String zona) {
        this.zona = zona;
    }

    public Boolean getNecesitaJugadores() {
        return necesitaJugadores;
    }

    public void setNecesitaJugadores(Boolean necesitaJugadores) {
        this.necesitaJugadores = necesitaJugadores;
    }
}

