package modelos;

/**
 * Clase abstracta que representa el tipo de notificación preferido por el usuario.
 * Aplica el principio Open/Closed: abierta para extensión, cerrada para modificación.
 * Se pueden crear nuevos tipos de notificación extendiendo esta clase sin modificar el código existente.
 */
public abstract class TipoNotificacion {
    
    public abstract String getNombre();
    
    // Instancias estáticas finales (equivalente a valores de enum)
    public static final TipoNotificacion EMAIL = new Email();
    public static final TipoNotificacion PUSH = new Push();
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        return getNombre().equals(((TipoNotificacion) obj).getNombre());
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
     * Método estático para obtener todos los tipos predefinidos.
     * Útil para componentes UI como JComboBox.
     */
    public static TipoNotificacion[] values() {
        return new TipoNotificacion[]{EMAIL, PUSH};
    }
    
    // Clases concretas que extienden TipoNotificacion
    public static class Email extends TipoNotificacion {
        @Override
        public String getNombre() {
            return "EMAIL";
        }
    }
    
    public static class Push extends TipoNotificacion {
        @Override
        public String getNombre() {
            return "PUSH";
        }
    }
}
