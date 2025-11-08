package servicios;

import modelos.Partido;
import modelos.Usuario;
import modelos.EstadoTipo;
import interfaces.EmparejamientoStrategy;
import modelos.EmparejamientoStrategyFactory;
import repositorios.PartidoHistorialRepository;
import repositorios.PartidoRepository;
import repositorios.UsuarioRepository;
import interfaces.DomainEventPublisher;
import modelos.PartidoArmado;
import modelos.PartidoConfirmado;
import modelos.PartidoEnJuego;
import modelos.PartidoFinalizado;
import modelos.PartidoCancelado;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Servicio de aplicación para gestionar el estado de los partidos.
 * Se encarga de publicar eventos de dominio cuando cambian los estados.
 */
public class GestionEstadoPartidoService {
    private final PartidoRepository partidoRepository;
    private final UsuarioRepository usuarioRepository;
    private final PartidoHistorialRepository historialRepository;
    private final Clock clock;
    private final DomainEventPublisher eventPublisher;

    public GestionEstadoPartidoService(PartidoRepository partidoRepository,
                                       UsuarioRepository usuarioRepository,
                                       Clock clock,
                                       DomainEventPublisher eventPublisher) {
        this.partidoRepository = partidoRepository;
        this.usuarioRepository = usuarioRepository;
        this.historialRepository = repositorios.PartidoHistorialRepository.getInstance();
        this.clock = clock;
        this.eventPublisher = eventPublisher;
    }
    
    public GestionEstadoPartidoService(PartidoRepository partidoRepository,
                                       UsuarioRepository usuarioRepository,
                                       PartidoHistorialRepository historialRepository,
                                       Clock clock,
                                       DomainEventPublisher eventPublisher) {
        this.partidoRepository = partidoRepository;
        this.usuarioRepository = usuarioRepository;
        this.historialRepository = historialRepository;
        this.clock = clock;
        this.eventPublisher = eventPublisher;
    }

    public void agregarJugador(UUID partidoId, UUID usuarioId) {
        Partido partido = partidoRepository.findById(partidoId)
                .orElseThrow(() -> new IllegalArgumentException("Partido no encontrado"));
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        EstadoTipo estadoAnterior = partido.getEstado().tipo();
        partido.agregarJugador(usuario);
        EstadoTipo estadoNuevo = partido.getEstado().tipo();
        
        partidoRepository.save(partido);
        
        // Publicar evento si cambió el estado
        publicarEventoSiCambioEstado(estadoAnterior, estadoNuevo, partido.getId());
    }

    public void confirmar(UUID partidoId, UUID usuarioId) {
        Partido partido = partidoRepository.findById(partidoId)
                .orElseThrow(() -> new IllegalArgumentException("Partido no encontrado"));
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        
        EstadoTipo estadoAnterior = partido.getEstado().tipo();
        partido.confirmar(usuario);
        EstadoTipo estadoNuevo = partido.getEstado().tipo();
        
        partidoRepository.save(partido);
        
        // Publicar evento si cambió el estado
        publicarEventoSiCambioEstado(estadoAnterior, estadoNuevo, partido.getId());
    }

    public void iniciarSiCorresponde(UUID partidoId) {
        Partido partido = partidoRepository.findById(partidoId)
                .orElseThrow(() -> new IllegalArgumentException("Partido no encontrado"));
        
        // Solo intentar iniciar si está confirmado y la fecha/hora llegó
        if (EstadoTipo.CONFIRMADO.equals(partido.getEstado().tipo()) &&
            !partido.getFechaHora().isAfter(clock.instant())) {
            EstadoTipo estadoAnterior = partido.getEstado().tipo();
            partido.iniciar(clock);
            EstadoTipo estadoNuevo = partido.getEstado().tipo();
            
            partidoRepository.save(partido);
            
            // Publicar evento si cambió el estado
            publicarEventoSiCambioEstado(estadoAnterior, estadoNuevo, partido.getId());
        }
    }
    
    /**
     * Finaliza un partido si corresponde (está en juego y la duración terminó).
     * También verifica si un partido confirmado debe iniciar automáticamente.
     */
    public void finalizarSiCorresponde(UUID partidoId) {
        Partido partido = partidoRepository.findById(partidoId)
                .orElseThrow(() -> new IllegalArgumentException("Partido no encontrado"));
        
        // Primero, verificar si un partido confirmado debe iniciar automáticamente
        if (EstadoTipo.CONFIRMADO.equals(partido.getEstado().tipo())) {
            // Si la fecha/hora del partido ya llegó, iniciarlo automáticamente
            if (!partido.getFechaHora().isAfter(clock.instant())) {
                try {
                    EstadoTipo estadoAnterior = partido.getEstado().tipo();
                    partido.iniciar(clock);
                    EstadoTipo estadoNuevo = partido.getEstado().tipo();
                    
                    partidoRepository.save(partido);
                    
                    // Publicar evento si cambió el estado
                    publicarEventoSiCambioEstado(estadoAnterior, estadoNuevo, partido.getId());
                } catch (Exception e) {
                    // Ignorar errores (puede que el partido no pueda iniciar por alguna razón)
                }
            }
        }
        
        // Luego, verificar si un partido en juego debe finalizar
        if (EstadoTipo.EN_JUEGO.equals(partido.getEstado().tipo())) {
            Instant fechaFinalizacion = partido.getFechaHoraFinalizacion();
            if (!fechaFinalizacion.isAfter(clock.instant())) {
                EstadoTipo estadoAnterior = partido.getEstado().tipo();
                partido.finalizar();
                EstadoTipo estadoNuevo = partido.getEstado().tipo();
                
                partidoRepository.save(partido);
                
                // Publicar evento si cambió el estado
                publicarEventoSiCambioEstado(estadoAnterior, estadoNuevo, partido.getId());
            }
        }
    }

    public void finalizar(UUID partidoId) {
        Partido partido = partidoRepository.findById(partidoId)
                .orElseThrow(() -> new IllegalArgumentException("Partido no encontrado"));
        
        EstadoTipo estadoAnterior = partido.getEstado().tipo();
        partido.finalizar();
        EstadoTipo estadoNuevo = partido.getEstado().tipo();
        
        partidoRepository.save(partido);
        
        // Registrar en el historial para la estrategia "Por Historial"
        historialRepository.registrarPartidoFinalizado(partido);
        
        // Publicar evento si cambió el estado
        publicarEventoSiCambioEstado(estadoAnterior, estadoNuevo, partido.getId());
    }

    public void cancelar(UUID partidoId) {
        Partido partido = partidoRepository.findById(partidoId)
                .orElseThrow(() -> new IllegalArgumentException("Partido no encontrado"));
        
        // Verificar que el partido pueda cancelarse (no puede estar FINALIZADO, CANCELADO o EN_JUEGO)
        EstadoTipo estadoActual = partido.getEstado().tipo();
        if (EstadoTipo.FINALIZADO.equals(estadoActual) || 
            EstadoTipo.CANCELADO.equals(estadoActual) || 
            EstadoTipo.EN_JUEGO.equals(estadoActual)) {
            throw new IllegalStateException("No se puede cancelar un partido en estado " + estadoActual.getNombre());
        }
        
        EstadoTipo estadoAnterior = partido.getEstado().tipo();
        partido.cancelar();
        EstadoTipo estadoNuevo = partido.getEstado().tipo();
        
        partidoRepository.save(partido);
        
        // Publicar evento si cambió el estado
        publicarEventoSiCambioEstado(estadoAnterior, estadoNuevo, partido.getId());
    }
    
    public void cancelar(UUID partidoId, UUID usuarioId) {
        Partido partido = partidoRepository.findById(partidoId)
                .orElseThrow(() -> new IllegalArgumentException("Partido no encontrado"));
        
        // Verificar que el usuario sea el anfitrión
        if (!partido.esCreador(usuarioId)) {
            throw new IllegalStateException("Solo el anfitrión del partido puede cancelarlo");
        }
        
        // Verificar que el partido pueda cancelarse (no puede estar FINALIZADO, CANCELADO o EN_JUEGO)
        EstadoTipo estadoActual = partido.getEstado().tipo();
        if (EstadoTipo.FINALIZADO.equals(estadoActual) || 
            EstadoTipo.CANCELADO.equals(estadoActual) || 
            EstadoTipo.EN_JUEGO.equals(estadoActual)) {
            throw new IllegalStateException("No se puede cancelar un partido en estado " + estadoActual.getNombre());
        }
        
        EstadoTipo estadoAnterior = partido.getEstado().tipo();
        partido.cancelar();
        EstadoTipo estadoNuevo = partido.getEstado().tipo();
        
        partidoRepository.save(partido);
        
        // Publicar evento si cambió el estado
        publicarEventoSiCambioEstado(estadoAnterior, estadoNuevo, partido.getId());
    }
    
    /**
     * Publica el evento de dominio correspondiente según la transición de estado.
     */
    private void publicarEventoSiCambioEstado(EstadoTipo estadoAnterior, EstadoTipo estadoNuevo, UUID partidoId) {
        if (estadoAnterior.equals(estadoNuevo)) {
            return; // No hay cambio de estado
        }
        
        // Transición a ARMADO (desde NECESITAMOS_JUGADORES)
        if (estadoNuevo.equals(EstadoTipo.ARMADO) && estadoAnterior.equals(EstadoTipo.NECESITAMOS_JUGADORES)) {
            eventPublisher.publish(new PartidoArmado(partidoId));
            return;
        }
        
        // Transición a CONFIRMADO (desde ARMADO)
        if (estadoNuevo.equals(EstadoTipo.CONFIRMADO) && estadoAnterior.equals(EstadoTipo.ARMADO)) {
            eventPublisher.publish(new PartidoConfirmado(partidoId));
            return;
        }
        
        // Transición a EN_JUEGO (desde CONFIRMADO)
        if (estadoNuevo.equals(EstadoTipo.EN_JUEGO) && estadoAnterior.equals(EstadoTipo.CONFIRMADO)) {
            eventPublisher.publish(new PartidoEnJuego(partidoId));
            return;
        }
        
        // Transición a FINALIZADO (desde EN_JUEGO)
        if (estadoNuevo.equals(EstadoTipo.FINALIZADO) && estadoAnterior.equals(EstadoTipo.EN_JUEGO)) {
            eventPublisher.publish(new PartidoFinalizado(partidoId));
            return;
        }
        
        // Transición a CANCELADO (desde cualquier estado excepto FINALIZADO)
        if (estadoNuevo.equals(EstadoTipo.CANCELADO) && !estadoAnterior.equals(EstadoTipo.FINALIZADO)) {
            eventPublisher.publish(new PartidoCancelado(partidoId));
        }
    }

    /**
     * Busca candidatos para un partido usando una estrategia de emparejamiento.
     */
    public List<Usuario> buscarCandidatos(UUID partidoId, String nombreEstrategia) {
        Partido partido = partidoRepository.findById(partidoId)
                .orElseThrow(() -> new IllegalArgumentException("Partido no encontrado"));
        
        // Si la estrategia es "Por Nivel" y el partido tiene parámetros guardados, usarlos
        EmparejamientoStrategy strategy;
        if ("Por Nivel".equals(nombreEstrategia) && partido.getNivelJuegoObjetivo() != null && partido.getTipoFiltroNivel() != null) {
            // Convertir string a NivelJuego
            modelos.NivelJuego nivelObjetivo = convertirStringANivelJuego(partido.getNivelJuegoObjetivo());
            strategy = modelos.EmparejamientoStrategyFactory.crear(
                nombreEstrategia, nivelObjetivo, partido.getTipoFiltroNivel());
        } else {
            strategy = modelos.EmparejamientoStrategyFactory.crear(nombreEstrategia);
        }
        
        List<Usuario> todosUsuarios = usuarioRepository.findAll();
        
        // Filtrar usuarios que ya están en el partido
        List<Usuario> candidatos = todosUsuarios.stream()
                .filter(u -> !partido.getJugadores().contains(u))
                .collect(java.util.stream.Collectors.toList());
        
        return strategy.filtrarCandidatos(partido, candidatos);
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
}

