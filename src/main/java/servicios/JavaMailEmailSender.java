package servicios;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import interfaces.EmailSender;

import java.util.Properties;

/**
 * Adapter para envío de emails usando Jakarta Mail (JavaMail).
 * Patrón Adapter: adapta la interfaz de Jakarta Mail a nuestro puerto EmailSender.
 * 
 * Permite cambiar fácilmente a otro servicio de email implementando EmailSender.
 */
public class JavaMailEmailSender implements EmailSender {
    private final SmtpConfig config;
    private final boolean testMode;
    
    /**
     * Constructor con configuración SMTP.
     * @param config configuración del servidor SMTP
     */
    public JavaMailEmailSender(SmtpConfig config) {
        this.config = config;
        this.testMode = config.getHost().equals("localhost");
    }
    
    /**
     * Constructor por defecto que usa modo de prueba (solo imprime en consola).
     * Útil para desarrollo sin configurar un servidor SMTP real.
     */
    public JavaMailEmailSender() {
        this.config = SmtpConfig.testConfig();
        this.testMode = true;
    }
    
    @Override
    public void send(String to, String subject, String body) {
        if (testMode) {
            // Modo de prueba: solo imprime en consola
            sendTestEmail(to, subject, body);
        } else {
            // Modo real: envía email usando Jakarta Mail
            sendRealEmail(to, subject, body);
        }
    }
    
    /**
     * Envía un email real usando Jakarta Mail.
     */
    private void sendRealEmail(String to, String subject, String body) {
        try {
            // Configurar propiedades para la sesión SMTP
            Properties properties = new Properties();
            properties.put("mail.smtp.host", config.getHost());
            properties.put("mail.smtp.port", String.valueOf(config.getPort()));
            properties.put("mail.smtp.auth", "true");
            
            if (config.isUseTls()) {
                properties.put("mail.smtp.starttls.enable", "true");
                properties.put("mail.smtp.starttls.required", "true");
            }
            
            if (config.isUseSsl()) {
                properties.put("mail.smtp.ssl.enable", "true");
                properties.put("mail.smtp.socketFactory.port", String.valueOf(config.getPort()));
                properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            }
            
            // Crear sesión autenticada
            Session session = Session.getInstance(properties, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(config.getUsername(), config.getPassword());
                }
            });
            
            // Crear mensaje
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(config.getUsername()));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setText(body);
            
            // Enviar mensaje
            Transport.send(message);
            
            System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            System.out.println("📧 EMAIL ENVIADO EXITOSAMENTE");
            System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            System.out.println("  Para: " + to);
            System.out.println("  Asunto: " + subject);
            System.out.println("  Mensaje: " + body);
            System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            
        } catch (MessagingException e) {
            System.err.println("❌ Error al enviar email: " + e.getMessage());
            e.printStackTrace();
            // En caso de error, también imprimir en consola para no perder la notificación
            sendTestEmail(to, subject, body);
        }
    }
    
    /**
     * Modo de prueba: imprime el email en consola sin enviarlo realmente.
     */
    private void sendTestEmail(String to, String subject, String body) {
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("📧 NOTIFICACIÓN POR EMAIL (MODO PRUEBA)");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("  Para: " + to);
        System.out.println("  Asunto: " + subject);
        System.out.println("  Mensaje: " + body);
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("  ℹ️  Para enviar emails reales, configura SmtpConfig en Main.java");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }
}

