package servicios;

import modelos.Partido;
import modelos.Usuario;
import modelos.Zona;
import interfaces.DomainEventPublisher;
import modelos.PartidoCreado;
import repositorios.PartidoRepository;
import repositorios.UsuarioRepository;
import modelos.CrearPartidoCmd;
import interfaces.EmparejamientoStrategy;
import modelos.EmparejamientoStrategyFactory;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Servicio de aplicación para crear partidos.
 * Se encarga de crear el partido y notificar a los usuarios candidatos.
 */
public class CrearPartidoService {
    private final PartidoRepository partidoRepository;
    private final UsuarioRepository usuarioRepository;
    private final DomainEventPublisher eventPublisher;
    private final NotificationFacade notificationFacade;

    public CrearPartidoService(PartidoRepository partidoRepository,
                                UsuarioRepository usuarioRepository,
                                DomainEventPublisher eventPublisher,
                                NotificationFacade notificationFacade) {
        this.partidoRepository = partidoRepository;
        this.usuarioRepository = usuarioRepository;
        this.eventPublisher = eventPublisher;
        this.notificationFacade = notificationFacade;
    }

    public Partido create(CrearPartidoCmd cmd, UUID creadorId) {
        // Validaciones
        if (cmd.getDeporte() == null) {
            throw new IllegalArgumentException("El deporte es requerido");
        }
        if (cmd.getCupoRequerido() < 2) {
            throw new IllegalArgumentException("El cupo requerido debe ser al menos 2");
        }
        if (cmd.getFechaHora() == null) {
            throw new IllegalArgumentException("La fecha/hora es requerida");
        }
        if (cmd.getFechaHora().isBefore(java.time.Instant.now())) {
            throw new IllegalArgumentException("La fecha/hora no puede ser en el pasado");
        }
        if (cmd.getZona() == null) {
            throw new IllegalArgumentException("La zona es requerida");
        }

        // Crear partido
        Partido partido = new Partido(
            UUID.randomUUID(),
            cmd.getDeporte(),
            cmd.getCupoRequerido(),
            cmd.getDuracion(),
            cmd.getZona(),
            cmd.getFechaHora()
        );

        // Guardar parámetros de estrategia
        partido.setEstrategiaEmparejamiento(cmd.getEstrategiaEmparejamiento());
        if ("Por Nivel".equals(cmd.getEstrategiaEmparejamiento())) {
            partido.setNivelJuegoObjetivo(cmd.getNivelJuegoObjetivo());
            partido.setTipoFiltroNivel(cmd.getTipoFiltroNivel());
        }
        
        // Guardar el ID del creador (anfitrión) ANTES de guardar
        partido.setCreadorId(creadorId);
        
        // Agregar el creador como jugador del partido ANTES de guardar
        Usuario creador = usuarioRepository.findById(creadorId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario creador no encontrado"));
        partido.agregarJugador(creador);
        
        // Guardar el partido con el creadorId y el jugador agregado
        partido = partidoRepository.save(partido);
        
        // Verificar que el creadorId y el jugador se guardaron correctamente
        System.out.println("✅ Partido creado - ID: " + partido.getId().toString().substring(0, 8) + 
                          ", Creador: " + (partido.getCreadorId() != null ? partido.getCreadorId().toString().substring(0, 8) : "null") +
                          ", Jugadores: " + partido.getJugadores().size() + "/" + partido.getCupoRequerido());

        // Buscar y notificar a usuarios candidatos
        notificarCandidatos(partido);

        // Publicar evento (otros suscriptores pueden escucharlo si es necesario)
        eventPublisher.publish(new PartidoCreado(partido.getId()));

        return partido;
    }
    
    /**
     * Busca usuarios candidatos que cumplan con los criterios del partido y los notifica.
     * Criterios:
     * - Mismo deporte favorito que el partido
     * - Cumplen con la estrategia de emparejamiento del partido
     */
    private void notificarCandidatos(Partido partido) {
        String nombreDeporte = partido.getDeporte() != null ? partido.getDeporte().getNombre() : "desconocido";
        
        System.out.println("\n" + "=".repeat(80));
        System.out.println("🔔 PROCESANDO NOTIFICACIONES PARA NUEVO PARTIDO");
        System.out.println("=".repeat(80));
        System.out.println("  Partido ID: " + partido.getId().toString().substring(0, 8) + "...");
        System.out.println("  Deporte: " + nombreDeporte);
        System.out.println("  Estrategia: " + (partido.getEstrategiaEmparejamiento() != null ? partido.getEstrategiaEmparejamiento() : "N/A"));
        if ("Por Nivel".equals(partido.getEstrategiaEmparejamiento())) {
            System.out.println("    - Nivel Objetivo: " + (partido.getNivelJuegoObjetivo() != null ? partido.getNivelJuegoObjetivo() : "N/A"));
            System.out.println("    - Tipo Filtro: " + (partido.getTipoFiltroNivel() != null ? partido.getTipoFiltroNivel() : "N/A"));
        }
        System.out.println("=".repeat(80) + "\n");
        
        // Buscar usuarios que cumplan con los criterios
        List<Usuario> candidatos = buscarCandidatosParaNotificacion(partido);
        
        System.out.println("📋 Usuarios candidatos encontrados: " + candidatos.size());
        for (Usuario usuario : candidatos) {
            System.out.println("  ✓ " + usuario.getUsername() + 
                " (" + usuario.getEmail() + ")" +
                " - Deporte: " + (usuario.getDeporteFavorito() != null ? usuario.getDeporteFavorito().getNombre() : "N/A") +
                ", Nivel: " + (usuario.getNivelJuego() != null ? usuario.getNivelJuego().getNombre() : "N/A"));
        }
        System.out.println();
        
        // Notificar solo a los candidatos que cumplen los criterios (de forma asíncrona)
        System.out.println("📧 Enviando " + candidatos.size() + " notificación(es) de forma asíncrona...");
        String mensaje = construirMensajePartido(partido, nombreDeporte);
        for (Usuario usuario : candidatos) {
            notificationFacade.notifyUser(usuario, "Nuevo Partido Disponible", mensaje);
        }
        
        System.out.println("\n✅ Total de notificaciones programadas: " + candidatos.size() + " usuario(s)");
        System.out.println("   (Los emails se enviarán en segundo plano sin bloquear la aplicación)\n");
    }
    
    /**
     * Busca usuarios que cumplan con los criterios para ser notificados sobre un nuevo partido:
     * - Mismo deporte favorito que el partido
     * - Cumplen con la estrategia de emparejamiento del partido
     */
    private List<Usuario> buscarCandidatosParaNotificacion(Partido partido) {
        // Obtener todos los usuarios
        List<Usuario> todosUsuarios = usuarioRepository.findAll();
        
        // Filtrar por deporte favorito y excluir al creador y jugadores ya en el partido
        List<UUID> idsJugadores = partido.getJugadores().stream()
                .map(Usuario::getId)
                .collect(Collectors.toList());
        if (partido.getCreadorId() != null) {
            idsJugadores.add(partido.getCreadorId());
        }
        
        List<Usuario> usuariosMismoDeporte = todosUsuarios.stream()
                .filter(u -> u.getDeporteFavorito() != null && partido.getDeporte() != null)
                .filter(u -> u.getDeporteFavorito().equals(partido.getDeporte()))
                .filter(u -> !idsJugadores.contains(u.getId())) // Excluir creador y jugadores ya en el partido
                .collect(Collectors.toList());
        
        // Si no hay estrategia definida, notificar a todos los del mismo deporte
        String nombreEstrategia = partido.getEstrategiaEmparejamiento();
        if (nombreEstrategia == null || nombreEstrategia.isEmpty()) {
            return usuariosMismoDeporte;
        }
        
        // Crear la estrategia según el partido
        EmparejamientoStrategy strategy;
        try {
            if ("Por Nivel".equals(nombreEstrategia) && partido.getNivelJuegoObjetivo() != null && partido.getTipoFiltroNivel() != null) {
                // Convertir string a NivelJuego
                modelos.NivelJuego nivelObjetivo = convertirStringANivelJuego(partido.getNivelJuegoObjetivo());
                System.out.println("  [DEBUG] Creando estrategia 'Por Nivel' con:");
                System.out.println("    - Nivel objetivo (string): " + partido.getNivelJuegoObjetivo());
                System.out.println("    - Nivel objetivo (objeto): " + (nivelObjetivo != null ? nivelObjetivo.getNombre() : "NULL"));
                System.out.println("    - Tipo filtro: " + partido.getTipoFiltroNivel());
                strategy = EmparejamientoStrategyFactory.crear(nombreEstrategia, nivelObjetivo, partido.getTipoFiltroNivel());
            } else {
                System.out.println("  [DEBUG] Creando estrategia '" + nombreEstrategia + "' sin parámetros específicos");
                strategy = EmparejamientoStrategyFactory.crear(nombreEstrategia);
            }
        } catch (IllegalArgumentException e) {
            System.out.println("  [ERROR] Error creando estrategia: " + e.getMessage());
            // Si hay error creando la estrategia, notificar a todos del mismo deporte
            return usuariosMismoDeporte;
        }
        
        // Filtrar usuarios que cumplan con la estrategia
        return usuariosMismoDeporte.stream()
                .filter(u -> {
                    boolean cumple = strategy.cumpleCriterio(partido, u);
                    if (!cumple) {
                        System.out.println("  ✗ " + u.getUsername() + " (" + u.getEmail() + 
                            ") - NO cumple criterio. Nivel: " + 
                            (u.getNivelJuego() != null ? u.getNivelJuego().getNombre() : "N/A"));
                    }
                    return cumple;
                })
                .collect(Collectors.toList());
    }
    
    private modelos.NivelJuego convertirStringANivelJuego(String nivelStr) {
        if ("PRINCIPIANTE".equals(nivelStr)) {
            return modelos.NivelJuego.PRINCIPIANTE;
        } else if ("INTERMEDIO".equals(nivelStr)) {
            return modelos.NivelJuego.INTERMEDIO;
        } else if ("AVANZADO".equals(nivelStr)) {
            return modelos.NivelJuego.AVANZADO;
        }
        return null;
    }
    
    /**
     * Construye un mensaje detallado con la información del partido.
     */
    private String construirMensajePartido(Partido partido, String nombreDeporte) {
        StringBuilder mensaje = new StringBuilder();
        mensaje.append("Se ha creado un nuevo partido de ").append(nombreDeporte).append(" que podría interesarte.\n\n");
        
        mensaje.append("📋 Detalles del partido:\n");
        mensaje.append("  • Deporte: ").append(nombreDeporte).append("\n");
        mensaje.append("  • Cupo: ").append(partido.getJugadores().size()).append("/").append(partido.getCupoRequerido()).append(" jugadores\n");
        mensaje.append("  • Duración: ").append(partido.getDuracion()).append(" minutos\n");
        
        if (partido.getZona() != null) {
            mensaje.append("  • Zona: ").append(partido.getZona().toString()).append("\n");
        }
        
        if (partido.getFechaHora() != null) {
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                    .withZone(java.time.ZoneId.systemDefault());
            mensaje.append("  • Fecha/Hora: ").append(formatter.format(partido.getFechaHora())).append("\n");
        }
        
        if (partido.getEstrategiaEmparejamiento() != null) {
            mensaje.append("  • Estrategia: ").append(partido.getEstrategiaEmparejamiento()).append("\n");
            if ("Por Nivel".equals(partido.getEstrategiaEmparejamiento())) {
                if (partido.getNivelJuegoObjetivo() != null && partido.getTipoFiltroNivel() != null) {
                    mensaje.append("    - Nivel requerido: ").append(partido.getNivelJuegoObjetivo())
                           .append(" (").append(partido.getTipoFiltroNivel()).append(")\n");
                }
            }
        }
        
        mensaje.append("\n¡Únete al partido desde la aplicación!");
        
        return mensaje.toString();
    }
}

