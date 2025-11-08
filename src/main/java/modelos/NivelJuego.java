package modelos;

/**
 * Clase abstracta que representa los niveles de juego de un usuario.
 * Aplica el principio Open/Closed: abierta para extensión, cerrada para modificación.
 * Se pueden crear nuevos niveles extendiendo esta clase sin modificar el código existente.
 */
public abstract class NivelJuego {
    
    public abstract String getNombre();
    public abstract int getOrden();
    
    // Instancias estáticas finales (equivalente a valores de enum)
    public static final NivelJuego PRINCIPIANTE = new Principiante();
    public static final NivelJuego INTERMEDIO = new Intermedio();
    public static final NivelJuego AVANZADO = new Avanzado();
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        return getNombre().equals(((NivelJuego) obj).getNombre());
    }
    
    @Override
    public int hashCode() {
        return getNombre().hashCode();
    }
    
    @Override
    public String toString() {
        return getNombre();
    }
    
    /**
     * Método estático para obtener todos los niveles predefinidos.
     * Útil para componentes UI como JComboBox.
     */
    public static NivelJuego[] values() {
        return new NivelJuego[]{PRINCIPIANTE, INTERMEDIO, AVANZADO};
    }
}
