package modelos;

/**
 * Clase abstracta que representa los tipos de estado posibles de un partido.
 * Aplica el principio Open/Closed: abierta para extensión, cerrada para modificación.
 * Se pueden crear nuevos estados extendiendo esta clase sin modificar el código existente.
 */
public abstract class EstadoTipo {
    
    public abstract String getNombre();
    
    // Instancias estáticas finales (equivalente a valores de enum)
    public static final EstadoTipo NECESITAMOS_JUGADORES = new NecesitamosJugadores();
    public static final EstadoTipo ARMADO = new Armado();
    public static final EstadoTipo CONFIRMADO = new Confirmado();
    public static final EstadoTipo EN_JUEGO = new EnJuego();
    public static final EstadoTipo FINALIZADO = new Finalizado();
    public static final EstadoTipo CANCELADO = new Cancelado();
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        return getNombre().equals(((EstadoTipo) obj).getNombre());
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
     */
    public static EstadoTipo[] values() {
        return new EstadoTipo[]{
            NECESITAMOS_JUGADORES,
            ARMADO,
            CONFIRMADO,
            EN_JUEGO,
            FINALIZADO,
            CANCELADO
        };
    }
    
    // Clases concretas que extienden EstadoTipo
    public static class NecesitamosJugadores extends EstadoTipo {
        @Override
        public String getNombre() {
            return "NECESITAMOS_JUGADORES";
        }
    }
    
    public static class Armado extends EstadoTipo {
        @Override
        public String getNombre() {
            return "ARMADO";
        }
    }
    
    public static class Confirmado extends EstadoTipo {
        @Override
        public String getNombre() {
            return "CONFIRMADO";
        }
    }
    
    public static class EnJuego extends EstadoTipo {
        @Override
        public String getNombre() {
            return "EN_JUEGO";
        }
    }
    
    public static class Finalizado extends EstadoTipo {
        @Override
        public String getNombre() {
            return "FINALIZADO";
        }
    }
    
    public static class Cancelado extends EstadoTipo {
        @Override
        public String getNombre() {
            return "CANCELADO";
        }
    }
}
