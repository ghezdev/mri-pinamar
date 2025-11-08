package repositorios;

import modelos.Partido;
import modelos.Usuario;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Repositorio para consultar el historial de partidos.
 * Permite verificar si dos usuarios han jugado juntos antes.
 * Implementación en memoria.
 */
public class PartidoHistorialRepository {
    private static PartidoHistorialRepository instance;
    
    // Mapa: (usuario1Id, usuario2Id) -> true si han jugado juntos
    // Usamos un Set de pares ordenados para evitar duplicados
    private final Set<String> historialJugadores;
    
    private PartidoHistorialRepository() {
        this.historialJugadores = ConcurrentHashMap.newKeySet();
    }
    
    public static synchronized PartidoHistorialRepository getInstance() {
        if (instance == null) {
            instance = new PartidoHistorialRepository();
        }
        return instance;
    }
    
    /**
     * Verifica si dos usuarios han jugado juntos en algún partido finalizado.
     * @param usuario1Id ID del primer usuario
     * @param usuario2Id ID del segundo usuario
     * @return true si han jugado juntos, false en caso contrario
     */
    public boolean hanJugadoJuntos(UUID usuario1Id, UUID usuario2Id) {
        if (usuario1Id.equals(usuario2Id)) {
            return false; // Un usuario no puede haber jugado consigo mismo
        }
        
        // Crear clave ordenada para evitar duplicados
        String clave = crearClave(usuario1Id, usuario2Id);
        return historialJugadores.contains(clave);
    }
    
    /**
     * Registra un partido finalizado en el historial.
     * @param partido el partido finalizado
     */
    public void registrarPartidoFinalizado(Partido partido) {
        List<Usuario> jugadores = partido.getJugadores();
        
        // Registrar todas las combinaciones de pares de jugadores
        for (int i = 0; i < jugadores.size(); i++) {
            for (int j = i + 1; j < jugadores.size(); j++) {
                UUID id1 = jugadores.get(i).getId();
                UUID id2 = jugadores.get(j).getId();
                String clave = crearClave(id1, id2);
                historialJugadores.add(clave);
            }
        }
    }
    
    /**
     * Obtiene todos los usuarios con los que un usuario ha jugado.
     * @param usuarioId ID del usuario
     * @return lista de IDs de usuarios con los que ha jugado
     */
    public List<UUID> obtenerCompañerosAnteriores(UUID usuarioId) {
        Set<UUID> compañeros = new HashSet<>();
        String usuarioIdStr = usuarioId.toString();
        
        for (String clave : historialJugadores) {
            String[] partes = clave.split(":");
            if (partes.length == 2) {
                if (partes[0].equals(usuarioIdStr)) {
                    compañeros.add(UUID.fromString(partes[1]));
                } else if (partes[1].equals(usuarioIdStr)) {
                    compañeros.add(UUID.fromString(partes[0]));
                }
            }
        }
        
        return new ArrayList<>(compañeros);
    }
    
    /**
     * Crea una clave ordenada para un par de usuarios.
     * Esto asegura que (A, B) y (B, A) se traten como la misma relación.
     */
    private String crearClave(UUID id1, UUID id2) {
        // Ordenar los IDs para que la clave sea consistente
        if (id1.compareTo(id2) < 0) {
            return id1.toString() + ":" + id2.toString();
        } else {
            return id2.toString() + ":" + id1.toString();
        }
    }
}

