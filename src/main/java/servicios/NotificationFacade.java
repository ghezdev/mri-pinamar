package servicios;

import modelos.TipoNotificacion;
import modelos.Usuario;
import interfaces.EmailSender;
import interfaces.PushSender;

import javax.swing.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Facade para orquestar el envío de notificaciones.
 * Patrón Facade: simplifica la interfaz para enviar notificaciones,
 * ocultando la complejidad de elegir entre Email y Push.
 * 
 * El envío de notificaciones se realiza de forma asíncrona para no bloquear la UI.
 * Para notificaciones PUSH, muestra un JOptionPane si hay un JFrame disponible.
 */
public class NotificationFacade {
    private final EmailSender emailSender;
    private final PushSender pushSender;
    private final ExecutorService executorService;
    private JFrame parentFrame; // Frame para mostrar notificaciones push como carteles

    public NotificationFacade(EmailSender emailSender, PushSender pushSender) {
        this.emailSender = emailSender;
        this.pushSender = pushSender;
        // Crear un pool de threads para envío asíncrono de notificaciones
        this.executorService = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r, "NotificationSender");
            t.setDaemon(true); // Thread daemon para que no impida la finalización de la aplicación
            return t;
        });
    }
    
    /**
     * Establece el JFrame padre para mostrar notificaciones push como carteles.
     */
    public void setParentFrame(JFrame parentFrame) {
        this.parentFrame = parentFrame;
    }

    /**
     * Notifica a un usuario según su preferencia de forma asíncrona.
     * No bloquea el thread actual.
     * @param usuario el usuario a notificar
     * @param title título de la notificación
     * @param body cuerpo del mensaje
     */
    public void notifyUser(Usuario usuario, String title, String body) {
        // Enviar notificación de forma asíncrona
        executorService.submit(() -> {
            try {
                TipoNotificacion preferencia = usuario.getPreferenciaNotificacion();
                
                if (TipoNotificacion.EMAIL.equals(preferencia)) {
                    // Enviar email
                    emailSender.send(usuario.getEmail(), title, body);
                } else if (TipoNotificacion.PUSH.equals(preferencia)) {
                    // Para PUSH, mostrar cartel (JOptionPane) si hay un frame disponible
                    if (parentFrame != null) {
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(
                                parentFrame,
                                body,
                                title,
                                JOptionPane.INFORMATION_MESSAGE
                            );
                        });
                    } else {
                        // Si no hay frame, usar el método tradicional (consola)
                        String token = obtenerTokenUsuario(usuario);
                        pushSender.send(token, title, body);
                    }
                }
            } catch (Exception e) {
                // Log del error pero no interrumpir el flujo
                System.err.println("❌ Error enviando notificación a " + usuario.getEmail() + ": " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
    
    /**
     * Método síncrono para compatibilidad (deprecated).
     * @deprecated Usar notifyUser que es asíncrono
     */
    @Deprecated
    public void notifyUserSync(Usuario usuario, String title, String body) {
        TipoNotificacion preferencia = usuario.getPreferenciaNotificacion();
        
        if (TipoNotificacion.EMAIL.equals(preferencia)) {
            emailSender.send(usuario.getEmail(), title, body);
        } else if (TipoNotificacion.PUSH.equals(preferencia)) {
            String token = obtenerTokenUsuario(usuario);
            pushSender.send(token, title, body);
        }
    }

    /**
     * Obtiene el token de dispositivo del usuario (simulado).
     * En producción, esto consultaría una base de datos.
     */
    private String obtenerTokenUsuario(Usuario usuario) {
        // Simulación: generar un token basado en el ID del usuario
        return "token_" + usuario.getId().toString().substring(0, 8);
    }
    
    /**
     * Cierra el ExecutorService de forma ordenada.
     * Debe llamarse al finalizar la aplicación.
     */
    public void shutdown() {
        executorService.shutdown();
        try {
            // Esperar hasta 5 segundos para que terminen las tareas pendientes
            if (!executorService.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}

