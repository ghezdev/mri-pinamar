package interfaces;

import modelos.Partido;
import modelos.Usuario;
import modelos.EstadoTipo;

import java.time.Clock;

/**
 * Interfaz que define el contrato para los estados del ciclo de vida de un Partido.
 * Patrón State: cada estado implementa su propia lógica de transición.
 */
public interface PartidoState {
    /**
     * Intenta agregar un jugador al partido.
     * @param partido el partido al que se intenta agregar el jugador
     * @param usuario el usuario que se intenta agregar
     * @throws IllegalStateException si la operación no es válida en el estado actual
     */
    void intentarAgregarJugador(Partido partido, Usuario usuario);
    
    /**
     * Remueve un jugador del partido.
     * @param partido el partido del que se remueve el jugador
     * @param usuario el usuario que se remueve
     * @throws IllegalStateException si la operación no es válida en el estado actual
     */
    void removerJugador(Partido partido, Usuario usuario);

    /**
     * Confirma el partido (transición a CONFIRMADO).
     * @param partido el partido a confirmar
     * @param usuario el usuario que confirma el partido
     * @throws IllegalStateException si la operación no es válida en el estado actual
     */
    void confirmar(Partido partido, Usuario usuario);

    /**
     * Inicia el partido si corresponde (transición a EN_JUEGO).
     * @param partido el partido a iniciar
     * @param clock reloj para verificar la fecha/hora
     * @throws IllegalStateException si la operación no es válida en el estado actual
     */
    void iniciar(Partido partido, Clock clock);

    /**
     * Finaliza el partido (transición a FINALIZADO).
     * @param partido el partido a finalizar
     * @throws IllegalStateException si la operación no es válida en el estado actual
     */
    void finalizar(Partido partido);

    /**
     * Cancela el partido (transición a CANCELADO).
     * @param partido el partido a cancelar
     * @throws IllegalStateException si la operación no es válida en el estado actual
     */
    void cancelar(Partido partido);

    /**
     * Retorna el tipo de estado actual.
     * @return el tipo de estado
     */
    EstadoTipo tipo();
}

