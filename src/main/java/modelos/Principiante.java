package modelos;

/**
 * Clase concreta que representa el nivel de juego Principiante.
 */
public class Principiante extends NivelJuego {
    @Override
    public String getNombre() {
        return "PRINCIPIANTE";
    }
    
    @Override
    public int getOrden() {
        return 0;
    }
}

