package modelos;

import interfaces.PartidoState;
import modelos.NecesitamosJugadoresState;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Representa un partido deportivo.
 * Utiliza el patrón State para manejar su ciclo de vida.
 */
public class Partido {
    private final UUID id;
    private Deporte deporte;
    private int cupoRequerido;
    private int duracion; // en minutos
    private Zona zona;
    private Instant fechaHora;
    private PartidoState estado;
    private final List<Usuario> jugadores;
    private final Set<UUID> jugadoresConfirmados; // IDs de jugadores que han confirmado
    private UUID creadorId; // ID del usuario que creó el partido (anfitrión)
    private String estrategiaEmparejamiento; // Nombre de la estrategia seleccionada
    private String nivelJuegoObjetivo; // Para estrategia "Por Nivel"
    private String tipoFiltroNivel; // "MINIMO" o "MAXIMO" para estrategia "Por Nivel"

    public Partido(UUID id, Deporte deporte, int cupoRequerido, int duracion, 
                   Zona zona, Instant fechaHora) {
        this.id = id;
        this.deporte = deporte;
        this.cupoRequerido = cupoRequerido;
        this.duracion = duracion;
        this.zona = zona;
        this.fechaHora = fechaHora;
        this.jugadores = new ArrayList<>();
        this.jugadoresConfirmados = new HashSet<>();
        // Estado inicial: necesita jugadores
        this.estado = new NecesitamosJugadoresState();
    }

    public UUID getId() {
        return id;
    }

    public Deporte getDeporte() {
        return deporte;
    }

    public void setDeporte(Deporte deporte) {
        this.deporte = deporte;
    }

    public int getCupoRequerido() {
        return cupoRequerido;
    }

    public void setCupoRequerido(int cupoRequerido) {
        this.cupoRequerido = cupoRequerido;
    }

    public int getDuracion() {
        return duracion;
    }

    public void setDuracion(int duracion) {
        this.duracion = duracion;
    }

    public Zona getZona() {
        return zona;
    }

    public void setZona(Zona zona) {
        this.zona = zona;
    }

    public Instant getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(Instant fechaHora) {
        this.fechaHora = fechaHora;
    }

    public PartidoState getEstado() {
        return estado;
    }

    /**
     * Método interno para cambiar el estado (solo llamado por los estados mismos).
     * Patrón State: el estado controla las transiciones.
     * Solo cambia el estado, sin publicar eventos (eso lo hace el servicio).
     */
    public void cambiarEstado(PartidoState nuevoEstado) {
        this.estado = nuevoEstado;
    }

    public List<Usuario> getJugadores() {
        return jugadores;
    }

    /**
     * Delega al estado actual la lógica de agregar jugadores.
     */
    public void agregarJugador(Usuario usuario) {
        estado.intentarAgregarJugador(this, usuario);
    }
    
    /**
     * Remueve un jugador del partido.
     * Delega al estado actual la lógica de remover jugadores.
     */
    public void removerJugador(Usuario usuario) {
        estado.removerJugador(this, usuario);
    }

    /**
     * Delega al estado actual la lógica de confirmar.
     * @param usuario el usuario que confirma el partido
     */
    public void confirmar(Usuario usuario) {
        estado.confirmar(this, usuario);
    }
    
    /**
     * Registra la confirmación de un jugador.
     * @param usuarioId ID del usuario que confirma
     */
    public void registrarConfirmacion(UUID usuarioId) {
        jugadoresConfirmados.add(usuarioId);
    }
    
    /**
     * Remueve la confirmación de un jugador.
     * @param usuarioId ID del usuario
     */
    public void removerConfirmacion(UUID usuarioId) {
        jugadoresConfirmados.remove(usuarioId);
    }
    
    /**
     * Verifica si un jugador ya confirmó.
     * @param usuarioId ID del usuario
     * @return true si el usuario ya confirmó
     */
    public boolean haConfirmado(UUID usuarioId) {
        return jugadoresConfirmados.contains(usuarioId);
    }
    
    /**
     * Obtiene el número de jugadores que han confirmado.
     * @return número de confirmaciones
     */
    public int getNumeroConfirmaciones() {
        return jugadoresConfirmados.size();
    }
    
    /**
     * Verifica si todos los jugadores han confirmado.
     * @return true si todos los jugadores confirmaron
     */
    public boolean todosConfirmaron() {
        return jugadores.size() > 0 && jugadoresConfirmados.size() == jugadores.size();
    }
    
    /**
     * Obtiene el conjunto de IDs de jugadores que confirmaron.
     * @return Set de UUIDs de jugadores confirmados
     */
    public Set<UUID> getJugadoresConfirmados() {
        return new HashSet<>(jugadoresConfirmados);
    }
    
    /**
     * Calcula la fecha/hora de finalización del partido.
     * Se calcula como fechaHora (inicio) + duracion (minutos).
     * @return Instant con la fecha/hora de finalización
     */
    public Instant getFechaHoraFinalizacion() {
        return fechaHora.plusSeconds(duracion * 60L);
    }

    /**
     * Delega al estado actual la lógica de iniciar.
     */
    public void iniciar(java.time.Clock clock) {
        estado.iniciar(this, clock);
    }

    /**
     * Delega al estado actual la lógica de finalizar.
     */
    public void finalizar() {
        estado.finalizar(this);
    }

    /**
     * Delega al estado actual la lógica de cancelar.
     */
    public void cancelar() {
        estado.cancelar(this);
    }

    public String getEstrategiaEmparejamiento() {
        return estrategiaEmparejamiento;
    }

    public void setEstrategiaEmparejamiento(String estrategiaEmparejamiento) {
        this.estrategiaEmparejamiento = estrategiaEmparejamiento;
    }

    public String getNivelJuegoObjetivo() {
        return nivelJuegoObjetivo;
    }

    public void setNivelJuegoObjetivo(String nivelJuegoObjetivo) {
        this.nivelJuegoObjetivo = nivelJuegoObjetivo;
    }

    public String getTipoFiltroNivel() {
        return tipoFiltroNivel;
    }

    public void setTipoFiltroNivel(String tipoFiltroNivel) {
        this.tipoFiltroNivel = tipoFiltroNivel;
    }
    
    public UUID getCreadorId() {
        return creadorId;
    }
    
    public void setCreadorId(UUID creadorId) {
        this.creadorId = creadorId;
    }
    
    /**
     * Verifica si un usuario es el creador/anfitrión del partido.
     */
    public boolean esCreador(UUID usuarioId) {
        return creadorId != null && creadorId.equals(usuarioId);
    }

    @Override
    public String toString() {
        return "Partido{" +
                "id=" + id +
                ", deporte=" + (deporte != null ? deporte.getNombre() : "null") +
                ", cupoRequerido=" + cupoRequerido +
                ", jugadores=" + jugadores.size() +
                ", estado=" + estado.tipo() +
                ", fechaHora=" + fechaHora +
                '}';
    }
}

