package modelos;

/**
 * Clase abstracta que representa un deporte.
 * Aplica el principio Open/Closed: abierta para extensión, cerrada para modificación.
 * Se pueden crear nuevos deportes extendiendo esta clase sin modificar el código existente.
 */
public abstract class Deporte {
    
    public abstract String getNombre();
    
    // Instancias estáticas finales (equivalente a valores de enum)
    public static final Deporte FUTBOL = new Futbol();
    public static final Deporte BASQUET = new Basquet();
    public static final Deporte VOLEY = new Voley();
    public static final Deporte TENIS = new Tenis();
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        return getNombre().equals(((Deporte) obj).getNombre());
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
     * Método estático para obtener todos los deportes predefinidos.
     * Útil para componentes UI como JComboBox.
     */
    public static Deporte[] values() {
        return new Deporte[]{FUTBOL, BASQUET, VOLEY, TENIS};
    }
}

