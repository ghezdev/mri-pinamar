package servicios;

/**
 * Configuración para el servidor SMTP.
 * Permite configurar los parámetros de conexión para el envío de emails.
 */
public class SmtpConfig {
    private final String host;
    private final int port;
    private final String username;
    private final String password;
    private final boolean useTls;
    private final boolean useSsl;
    
    public SmtpConfig(String host, int port, String username, String password, boolean useTls, boolean useSsl) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.useTls = useTls;
        this.useSsl = useSsl;
    }
    
    /**
     * Crea una configuración por defecto usando Gmail SMTP.
     * Para usar Gmail, necesitas una "Contraseña de aplicación" en lugar de tu contraseña normal.
     */
    public static SmtpConfig gmailConfig(String email, String appPassword) {
        return new SmtpConfig("smtp.gmail.com", 587, email, appPassword, true, false);
    }
    
    /**
     * Crea una configuración para un servidor SMTP genérico.
     */
    public static SmtpConfig customConfig(String host, int port, String username, String password, boolean useTls) {
        return new SmtpConfig(host, port, username, password, useTls, false);
    }
    
    /**
     * Crea una configuración de prueba que solo imprime en consola (sin enviar emails reales).
     * Útil para desarrollo y pruebas.
     */
    public static SmtpConfig testConfig() {
        return new SmtpConfig("localhost", 25, "test", "test", false, false);
    }
    
    public String getHost() {
        return host;
    }
    
    public int getPort() {
        return port;
    }
    
    public String getUsername() {
        return username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public boolean isUseTls() {
        return useTls;
    }
    
    public boolean isUseSsl() {
        return useSsl;
    }
}

