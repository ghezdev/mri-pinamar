package modelos;

/**
 * Clase abstracta que representa una zona geográfica.
 * Aplica el principio Open/Closed: abierto para extensión, cerrado para modificación.
 * NO se usa enum, sino clases abstractas con herencia.
 */
public abstract class Zona {
    
    /**
     * Retorna el nombre de la zona.
     */
    public abstract String getNombre();
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        return getNombre().equals(((Zona) obj).getNombre());
    }
    
    @Override
    public int hashCode() {
        return getNombre().hashCode();
    }
    
    @Override
    public String toString() {
        return getNombre();
    }
    
    // Instancias estáticas finales para las zonas disponibles
    public static final Zona ZONA_SUR = new ZonaSur();
    public static final Zona ZONA_NORTE = new ZonaNorte();
    public static final Zona ZONA_OESTE = new ZonaOeste();
    public static final Zona ZONA_ESTE = new ZonaEste();
}

