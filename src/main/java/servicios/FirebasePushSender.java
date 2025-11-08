package servicios;

import interfaces.PushSender;

/**
 * Adapter para envío de notificaciones push usando Firebase (simulado).
 * Patrón Adapter: adapta la interfaz de Firebase a nuestro puerto PushSender.
 */
public class FirebasePushSender implements PushSender {
    // En producción, aquí se inyectaría un cliente real de Firebase
    
    @Override
    public void send(String token, String title, String body) {
        // Simulación: en producción se usaría Firebase Cloud Messaging
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("🔔 NOTIFICACIÓN PUSH");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("  Token: " + token);
        System.out.println("  Título: " + title);
        System.out.println("  Mensaje: " + body);
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        // Simular envío exitoso
    }
}

