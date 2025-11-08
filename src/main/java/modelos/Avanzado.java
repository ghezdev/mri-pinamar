package modelos;

/**
 * Clase concreta que representa el nivel de juego Avanzado.
 */
public class Avanzado extends NivelJuego {
    @Override
    public String getNombre() {
        return "AVANZADO";
    }
    
    @Override
    public int getOrden() {
        return 2;
    }
}

