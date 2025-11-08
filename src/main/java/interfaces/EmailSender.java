package interfaces;

/**
 * Puerto (interfaz) para el envío de emails.
 * Patrón Adapter: define el contrato que los adapters deben implementar.
 */
public interface EmailSender {
    /**
     * Envía un email.
     * @param to destinatario
     * @param subject asunto
     * @param body cuerpo del mensaje
     */
    void send(String to, String subject, String body);
}

