package modelos;

/**
 * Clase concreta que representa el nivel de juego Intermedio.
 */
public class Intermedio extends NivelJuego {
    @Override
    public String getNombre() {
        return "INTERMEDIO";
    }
    
    @Override
    public int getOrden() {
        return 1;
    }
}

