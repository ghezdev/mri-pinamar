package vistas;

import modelos.Deporte;
import modelos.NivelJuego;
import modelos.TipoNotificacion;
import modelos.Usuario;
import modelos.Zona;
import modelos.RegistrarUsuarioCmd;
import controladores.AuthController;
import controladores.RegistroController;

import javax.swing.*;
import java.awt.*;
import java.util.Optional;

/**
 * Panel de login y registro de usuarios.
 * Contiene dos vistas: Login y Registro.
 */
public class PanelLogin extends JPanel {
    private final VentanaPrincipal ventanaPrincipal;
    private RegistroController registroController;
    private AuthController authController;

    // Campos para Login
    private JTextField campoUsernameLogin;
    private JPasswordField campoPasswordLogin;
    
    // Campos para Registro
    private JTextField campoUsernameRegistro;
    private JTextField campoEmail;
    private JPasswordField campoPasswordRegistro;
    private JComboBox<Deporte> comboDeporte;
    private JComboBox<NivelJuego> comboNivel;
    private JComboBox<Zona> comboZona;
    private JComboBox<TipoNotificacion> comboNotificacion;

    private CardLayout cardLayout;
    private JPanel panelContenedor;
    private JPanel panelLogin;
    private JPanel panelRegistro;

    public PanelLogin(VentanaPrincipal ventanaPrincipal) {
        this.ventanaPrincipal = ventanaPrincipal;
        inicializar();
    }
    
    public void setRegistroController(RegistroController controller) {
        this.registroController = controller;
    }
    
    public void setAuthController(AuthController controller) {
        this.authController = controller;
    }

    private void inicializar() {
        setLayout(new BorderLayout());
        
        // Panel contenedor con CardLayout para alternar entre Login y Registro
        cardLayout = new CardLayout();
        panelContenedor = new JPanel(cardLayout);
        
        // Crear panel de Login
        panelLogin = crearPanelLogin();
        panelContenedor.add(panelLogin, "LOGIN");
        
        // Crear panel de Registro
        panelRegistro = crearPanelRegistro();
        panelContenedor.add(panelRegistro, "REGISTRO");
        
        // Mostrar Login por defecto
        cardLayout.show(panelContenedor, "LOGIN");
        
        add(panelContenedor, BorderLayout.CENTER);
    }
    
    private JPanel crearPanelLogin() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints mainGbc = new GridBagConstraints();
        mainGbc.insets = new Insets(20, 20, 20, 20);
        mainGbc.anchor = GridBagConstraints.CENTER;
        
        JPanel panelFormulario = new JPanel(new GridBagLayout());
        panelFormulario.setBorder(BorderFactory.createTitledBorder("Iniciar Sesión"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Título
        gbc.gridwidth = 2;
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel titulo = new JLabel("Iniciar Sesión");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 18f));
        panelFormulario.add(titulo, gbc);
        gbc.gridwidth = 1;

        // Username
        gbc.gridx = 0;
        gbc.gridy = 1;
        panelFormulario.add(new JLabel("Usuario:"), gbc);
        gbc.gridx = 1;
        campoUsernameLogin = new JTextField(20);
        panelFormulario.add(campoUsernameLogin, gbc);

        // Password
        gbc.gridx = 0;
        gbc.gridy = 2;
        panelFormulario.add(new JLabel("Contraseña:"), gbc);
        gbc.gridx = 1;
        campoPasswordLogin = new JPasswordField(20);
        panelFormulario.add(campoPasswordLogin, gbc);

        // Botones
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JPanel panelBotones = new JPanel(new FlowLayout());
        JButton btnLogin = new JButton("Iniciar Sesión");
        btnLogin.addActionListener(e -> login());
        panelBotones.add(btnLogin);
        
        JButton btnIrARegistro = new JButton("¿No tienes cuenta? Regístrate");
        btnIrARegistro.addActionListener(e -> cardLayout.show(panelContenedor, "REGISTRO"));
        panelBotones.add(btnIrARegistro);
        
        panelFormulario.add(panelBotones, gbc);

        // Centrar el panel del formulario
        mainGbc.gridx = 0;
        mainGbc.gridy = 0;
        mainGbc.weightx = 1.0;
        mainGbc.weighty = 1.0;
        mainGbc.fill = GridBagConstraints.NONE;
        panel.add(panelFormulario, mainGbc);
        
        return panel;
    }
    
    private JPanel crearPanelRegistro() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints mainGbc = new GridBagConstraints();
        mainGbc.insets = new Insets(20, 20, 20, 20);
        mainGbc.anchor = GridBagConstraints.CENTER;
        
        JPanel panelFormulario = new JPanel(new GridBagLayout());
        panelFormulario.setBorder(BorderFactory.createTitledBorder("Registro de Usuario"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Título
        gbc.gridwidth = 2;
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel titulo = new JLabel("Crear Nueva Cuenta");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 18f));
        panelFormulario.add(titulo, gbc);
        gbc.gridwidth = 1;

        // Username
        gbc.gridx = 0;
        gbc.gridy = 1;
        panelFormulario.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        campoUsernameRegistro = new JTextField(20);
        panelFormulario.add(campoUsernameRegistro, gbc);

        // Email
        gbc.gridx = 0;
        gbc.gridy = 2;
        panelFormulario.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        campoEmail = new JTextField(20);
        panelFormulario.add(campoEmail, gbc);

        // Password
        gbc.gridx = 0;
        gbc.gridy = 3;
        panelFormulario.add(new JLabel("Contraseña:"), gbc);
        gbc.gridx = 1;
        campoPasswordRegistro = new JPasswordField(20);
        panelFormulario.add(campoPasswordRegistro, gbc);

        // Deporte favorito
        gbc.gridx = 0;
        gbc.gridy = 4;
        panelFormulario.add(new JLabel("Deporte Favorito:"), gbc);
        gbc.gridx = 1;
        comboDeporte = new JComboBox<>(Deporte.values());
        comboDeporte.setSelectedItem(Deporte.FUTBOL);
        panelFormulario.add(comboDeporte, gbc);

        // Nivel
        gbc.gridx = 0;
        gbc.gridy = 5;
        panelFormulario.add(new JLabel("Nivel:"), gbc);
        gbc.gridx = 1;
        comboNivel = new JComboBox<>(NivelJuego.values());
        panelFormulario.add(comboNivel, gbc);

        // Zona
        gbc.gridx = 0;
        gbc.gridy = 6;
        panelFormulario.add(new JLabel("Zona:"), gbc);
        gbc.gridx = 1;
        comboZona = new JComboBox<>(new Zona[]{
            Zona.ZONA_SUR, Zona.ZONA_NORTE, Zona.ZONA_OESTE, Zona.ZONA_ESTE
        });
        comboZona.setSelectedItem(Zona.ZONA_SUR);
        panelFormulario.add(comboZona, gbc);

        // Preferencia notificación
        gbc.gridx = 0;
        gbc.gridy = 7;
        panelFormulario.add(new JLabel("Notificación:"), gbc);
        gbc.gridx = 1;
        comboNotificacion = new JComboBox<>(TipoNotificacion.values());
        panelFormulario.add(comboNotificacion, gbc);

        // Botones
        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JPanel panelBotones = new JPanel(new FlowLayout());
        JButton btnRegistrar = new JButton("Registrarse");
        btnRegistrar.addActionListener(e -> registrar());
        panelBotones.add(btnRegistrar);
        
        JButton btnIrALogin = new JButton("¿Ya tienes cuenta? Inicia Sesión");
        btnIrALogin.addActionListener(e -> cardLayout.show(panelContenedor, "LOGIN"));
        panelBotones.add(btnIrALogin);
        
        panelFormulario.add(panelBotones, gbc);

        // Centrar el panel del formulario
        mainGbc.gridx = 0;
        mainGbc.gridy = 0;
        mainGbc.weightx = 1.0;
        mainGbc.weighty = 1.0;
        mainGbc.fill = GridBagConstraints.NONE;
        panel.add(panelFormulario, mainGbc);
        
        return panel;
    }

    private void registrar() {
        try {
            // Validaciones básicas
            if (campoUsernameRegistro.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "El username es requerido",
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (campoEmail.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "El email es requerido",
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (campoPasswordRegistro.getPassword().length == 0) {
                JOptionPane.showMessageDialog(this, 
                    "La contraseña es requerida",
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            RegistrarUsuarioCmd cmd = new RegistrarUsuarioCmd();
            cmd.setUsername(campoUsernameRegistro.getText().trim());
            cmd.setEmail(campoEmail.getText().trim());
            cmd.setPassword(new String(campoPasswordRegistro.getPassword()));
            cmd.setDeporteFavorito((Deporte) comboDeporte.getSelectedItem());
            cmd.setNivelJuego((NivelJuego) comboNivel.getSelectedItem());
            cmd.setZona((Zona) comboZona.getSelectedItem());
            cmd.setPreferenciaNotificacion((TipoNotificacion) comboNotificacion.getSelectedItem());

            Usuario usuario = registroController.registrar(cmd);
            JOptionPane.showMessageDialog(this, 
                "Usuario registrado exitosamente: " + usuario.getUsername(),
                "Éxito", JOptionPane.INFORMATION_MESSAGE);
            
            // Auto-login después del registro
            loginConUsuario(usuario);
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, 
                "Error: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void login() {
        String username = campoUsernameLogin.getText().trim();
        String password = new String(campoPasswordLogin.getPassword());
        
        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Ingrese un usuario",
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (password.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Ingrese una contraseña",
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (authController == null) {
            JOptionPane.showMessageDialog(this,
                "Controlador de autenticación no inicializado",
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        Optional<modelos.Usuario> usuarioOpt = authController.login(username, password);
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            // En producción, aquí se verificaría el hash de la contraseña
            // Por ahora, solo verificamos que el usuario exista
            loginConUsuario(usuario);
        } else {
            JOptionPane.showMessageDialog(this, 
                "Usuario o contraseña incorrectos",
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loginConUsuario(Usuario usuario) {
        // Notificar a VentanaPrincipal que el login fue exitoso
        // VentanaPrincipal se encargará de establecer el usuario en los controladores
        JOptionPane.showMessageDialog(this, 
            "Bienvenido " + usuario.getUsername(),
            "Login Exitoso", JOptionPane.INFORMATION_MESSAGE);
        
        // Notificar a la ventana principal del login exitoso
        ventanaPrincipal.onLoginExitoso(usuario);
    }
}

