package interfaces;

/**
 * Puerto (interfaz) para el envío de notificaciones push.
 * Patrón Adapter: define el contrato que los adapters deben implementar.
 */
public interface PushSender {
    /**
     * Envía una notificación push.
     * @param token token del dispositivo
     * @param title título de la notificación
     * @param body cuerpo del mensaje
     */
    void send(String token, String title, String body);
}

