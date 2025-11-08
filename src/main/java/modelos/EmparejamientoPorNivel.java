package modelos;
import interfaces.EmparejamientoStrategy;

import modelos.NivelJuego;
import modelos.Partido;
import modelos.Usuario;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Estrategia de emparejamiento que prioriza jugadores según nivel (mínimo o máximo).
 * Patrón Strategy: implementación concreta de la estrategia.
 */
public class EmparejamientoPorNivel implements EmparejamientoStrategy {
    private final NivelJuego nivelObjetivo;
    private final TipoFiltro tipoFiltro;
    
    /**
     * Clase abstracta que representa el tipo de filtro para la estrategia por nivel.
     * Aplica el principio Open/Closed.
     */
    public abstract static class TipoFiltro {
        public abstract boolean cumpleFiltro(int ordenUsuario, int ordenObjetivo);
        public abstract String getNombre();
        
        // Instancias estáticas finales
        public static final TipoFiltro MINIMO = new Minimo();
        public static final TipoFiltro MAXIMO = new Maximo();
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            return getNombre().equals(((TipoFiltro) obj).getNombre());
        }
        
        @Override
        public int hashCode() {
            return getNombre().hashCode();
        }
        
        // Clases concretas
        public static class Minimo extends TipoFiltro {
            @Override
            public boolean cumpleFiltro(int ordenUsuario, int ordenObjetivo) {
                return ordenUsuario >= ordenObjetivo; // Nivel igual o superior
            }
            
            @Override
            public String getNombre() {
                return "MINIMO";
            }
        }
        
        public static class Maximo extends TipoFiltro {
            @Override
            public boolean cumpleFiltro(int ordenUsuario, int ordenObjetivo) {
                return ordenUsuario <= ordenObjetivo; // Nivel igual o inferior
            }
            
            @Override
            public String getNombre() {
                return "MAXIMO";
            }
        }
    }
    
    /**
     * Constructor por defecto (para compatibilidad).
     * Usa el nivel más común del partido.
     */
    public EmparejamientoPorNivel() {
        this.nivelObjetivo = null;
        this.tipoFiltro = null;
    }
    
    /**
     * Constructor con parámetros específicos.
     * @param nivelObjetivo el nivel objetivo
     * @param tipoFiltro si es mínimo o máximo
     */
    public EmparejamientoPorNivel(NivelJuego nivelObjetivo, TipoFiltro tipoFiltro) {
        this.nivelObjetivo = nivelObjetivo;
        this.tipoFiltro = tipoFiltro;
    }
    
    @Override
    public List<Usuario> filtrarCandidatos(Partido partido, List<Usuario> candidatos) {
        if (candidatos.isEmpty()) {
            return new ArrayList<>();
        }

        // Determinar el nivel objetivo
        NivelJuego nivelFinal = nivelObjetivo;
        
        // Si no se especificó nivel objetivo, usar el más común del partido
        if (nivelFinal == null) {
            nivelFinal = obtenerNivelObjetivo(partido);
        }
        
        // Si aún no hay nivel objetivo, usar el del primer candidato
        if (nivelFinal == null && !candidatos.isEmpty()) {
            nivelFinal = candidatos.get(0).getNivelJuego();
        }

        final NivelJuego nivelFinalRef = nivelFinal;
        final TipoFiltro tipoFiltroRef = tipoFiltro;

        // Filtrar según el tipo de filtro
        List<Usuario> filtrados = candidatos.stream()
                .filter(u -> u.getNivelJuego() != null)
                .filter(u -> {
                    if (tipoFiltroRef == null) {
                        // Sin filtro específico, aceptar todos
                        return true;
                    } else {
                        // Usar el método polimórfico del filtro
                        return tipoFiltroRef.cumpleFiltro(u.getNivelJuego().getOrden(), nivelFinalRef.getOrden());
                    }
                })
                .sorted(Comparator.comparing((Usuario u) -> {
                    if (nivelFinalRef == null) return 0;
                    // Priorizar los del mismo nivel
                    return u.getNivelJuego().equals(nivelFinalRef) ? 0 : 1;
                }).thenComparing(u -> u.getNivelJuego().getOrden()))
                .collect(Collectors.toList());
        
        return filtrados;
    }

    private NivelJuego obtenerNivelObjetivo(Partido partido) {
        if (partido.getJugadores().isEmpty()) {
            return null;
        }

        // Retornar el nivel más común
        long principiantes = partido.getJugadores().stream()
                .filter(u -> NivelJuego.PRINCIPIANTE.equals(u.getNivelJuego()))
                .count();
        long intermedios = partido.getJugadores().stream()
                .filter(u -> NivelJuego.INTERMEDIO.equals(u.getNivelJuego()))
                .count();
        long avanzados = partido.getJugadores().stream()
                .filter(u -> NivelJuego.AVANZADO.equals(u.getNivelJuego()))
                .count();

        if (avanzados >= intermedios && avanzados >= principiantes) {
            return NivelJuego.AVANZADO;
        } else if (intermedios >= principiantes) {
            return NivelJuego.INTERMEDIO;
        } else {
            return NivelJuego.PRINCIPIANTE;
        }
    }

    @Override
    public boolean cumpleCriterio(Partido partido, Usuario usuario) {
        if (usuario.getNivelJuego() == null) {
            return false;
        }
        
        // Obtener nivel objetivo del partido
        NivelJuego nivelFinal = nivelObjetivo;
        if (nivelFinal == null) {
            // Intentar obtener del partido
            if (partido.getNivelJuegoObjetivo() != null) {
                // Convertir string a NivelJuego
                nivelFinal = convertirStringANivelJuego(partido.getNivelJuegoObjetivo());
            }
            if (nivelFinal == null) {
                nivelFinal = obtenerNivelObjetivo(partido);
            }
        }
        
        if (nivelFinal == null) {
            // Sin nivel objetivo, aceptar todos
            return true;
        }
        
        // Obtener tipo de filtro
        TipoFiltro filtro = tipoFiltro;
        if (filtro == null && partido.getTipoFiltroNivel() != null) {
            filtro = "MINIMO".equals(partido.getTipoFiltroNivel()) 
                ? TipoFiltro.MINIMO 
                : TipoFiltro.MAXIMO;
        }
        
        if (filtro == null) {
            // Sin filtro específico, aceptar todos
            return true;
        }
        
        // Verificar si cumple el filtro
        int ordenUsuario = usuario.getNivelJuego().getOrden();
        int ordenObjetivo = nivelFinal.getOrden();
        boolean cumple = filtro.cumpleFiltro(ordenUsuario, ordenObjetivo);
        
        // Debug: mostrar por qué se acepta o rechaza
        System.out.println("    [DEBUG] Usuario: " + usuario.getUsername() + 
            " | Nivel: " + usuario.getNivelJuego().getNombre() + " (orden=" + ordenUsuario + ")" +
            " | Objetivo: " + nivelFinal.getNombre() + " (orden=" + ordenObjetivo + ")" +
            " | Filtro: " + filtro.getNombre() + 
            " | Cumple: " + cumple);
        
        return cumple;
    }
    
    private NivelJuego convertirStringANivelJuego(String nombre) {
        if ("PRINCIPIANTE".equals(nombre)) {
            return modelos.NivelJuego.PRINCIPIANTE;
        } else if ("INTERMEDIO".equals(nombre)) {
            return modelos.NivelJuego.INTERMEDIO;
        } else if ("AVANZADO".equals(nombre)) {
            return modelos.NivelJuego.AVANZADO;
        }
        return null;
    }

    @Override
    public String nombre() {
        return "Por Nivel";
    }
}

