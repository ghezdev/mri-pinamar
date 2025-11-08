package vistas;

import controladores.CrearPartidoController;
import controladores.GestionEstadoController;
import controladores.PartidosController;
import vistas.PanelBuscarPartidos;
import vistas.PanelCrearPartido;
import vistas.PanelGestionEstado;
import vistas.PanelLogin;
import vistas.PanelMisPartidos;

import javax.swing.*;
import java.awt.*;

/**
 * Ventana principal de la aplicación con menú de navegación.
 */
public class VentanaPrincipal extends JFrame {
    private final PartidosController partidosController;
    private final CrearPartidoController crearPartidoController;
    private final GestionEstadoController gestionEstadoController;
    private JPanel panelContenido;
    private CardLayout cardLayout;
    private PanelBuscarPartidos panelBuscarPartidos;
    private PanelMisPartidos panelMisPartidos;
    private PanelLogin panelLogin;
    private servicios.ConfirmacionPartidoSubscriber confirmacionSubscriber;

    public VentanaPrincipal(PartidosController partidosController,
                           CrearPartidoController crearPartidoController,
                           GestionEstadoController gestionEstadoController) {
        this.partidosController = partidosController;
        this.crearPartidoController = crearPartidoController;
        this.gestionEstadoController = gestionEstadoController;
        inicializar();
    }

    private void inicializar() {
        setTitle("Sistema de Gestión de Partidos Deportivos");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);

        // Layout principal
        setLayout(new BorderLayout());

        // Menú superior
        JMenuBar menuBar = crearMenuBar();
        setJMenuBar(menuBar);

        // Panel de contenido con CardLayout
        cardLayout = new CardLayout();
        panelContenido = new JPanel(cardLayout);

        // Agregar vistas
        panelLogin = new PanelLogin(this);
        panelContenido.add(panelLogin, "LOGIN");
        
        panelBuscarPartidos = new PanelBuscarPartidos(partidosController, this);
        panelContenido.add(panelBuscarPartidos, "BUSCAR");
        
        PanelCrearPartido panelCrear = new PanelCrearPartido(partidosController, this);
        panelCrear.setCrearPartidoController(crearPartidoController);
        panelContenido.add(panelCrear, "CREAR");
        
        PanelGestionEstado panelGestion = new PanelGestionEstado(partidosController, this);
        panelGestion.setGestionEstadoController(gestionEstadoController);
        panelContenido.add(panelGestion, "GESTION");
        
        panelMisPartidos = new PanelMisPartidos(partidosController, this);
        panelContenido.add(panelMisPartidos, "MIS_PARTIDOS");

        add(panelContenido, BorderLayout.CENTER);

        // Mostrar login inicialmente
        mostrarPanel("LOGIN");
    }

    private JMenuBar crearMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        
        JMenu menuNavegacion = new JMenu("Navegación");
        
        JMenuItem itemBuscar = new JMenuItem("Buscar Partidos");
        itemBuscar.addActionListener(e -> mostrarPanel("BUSCAR"));
        
        JMenuItem itemCrear = new JMenuItem("Crear Partido");
        itemCrear.addActionListener(e -> mostrarPanel("CREAR"));
        
        JMenuItem itemMisPartidos = new JMenuItem("Mis Partidos");
        itemMisPartidos.addActionListener(e -> mostrarPanel("MIS_PARTIDOS"));
        
        JMenuItem itemGestion = new JMenuItem("Gestionar Estado");
        itemGestion.addActionListener(e -> mostrarPanel("GESTION"));
        
        menuNavegacion.add(itemBuscar);
        menuNavegacion.add(itemCrear);
        menuNavegacion.add(itemMisPartidos);
        menuNavegacion.add(itemGestion);
        menuNavegacion.addSeparator();
        
        JMenuItem itemCerrarSesion = new JMenuItem("Cerrar Sesión");
        itemCerrarSesion.addActionListener(e -> cerrarSesion());
        menuNavegacion.add(itemCerrarSesion);
        
        menuBar.add(menuNavegacion);
        
        return menuBar;
    }
    
    /**
     * Cierra la sesión del usuario actual y redirige al login.
     */
    private void cerrarSesion() {
        if (partidosController.getUsuarioActual() == null) {
            JOptionPane.showMessageDialog(this, 
                "No hay sesión activa",
                "Información", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        String username = partidosController.getUsuarioActual().getUsername();
        
        int confirmacion = JOptionPane.showConfirmDialog(this,
            "¿Está seguro que desea cerrar sesión?",
            "Cerrar Sesión",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        
        if (confirmacion == JOptionPane.YES_OPTION) {
            partidosController.cerrarSesion();
            JOptionPane.showMessageDialog(this,
                "Sesión cerrada. Hasta luego, " + username + "!",
                "Sesión Cerrada",
                JOptionPane.INFORMATION_MESSAGE);
            mostrarPanel("LOGIN");
        }
    }

    public void mostrarPanel(String nombre) {
        cardLayout.show(panelContenido, nombre);
        
        // Refrescar automáticamente cuando se muestra el panel de búsqueda
        if ("BUSCAR".equals(nombre) && panelBuscarPartidos != null) {
            panelBuscarPartidos.refrescar();
        }
        
        // Refrescar automáticamente cuando se muestra el panel de mis partidos
        if ("MIS_PARTIDOS".equals(nombre) && panelMisPartidos != null) {
            panelMisPartidos.refrescar();
        }
    }
    
    public void setConfirmacionSubscriber(servicios.ConfirmacionPartidoSubscriber subscriber) {
        this.confirmacionSubscriber = subscriber;
    }
    
    public servicios.ConfirmacionPartidoSubscriber getConfirmacionSubscriber() {
        return confirmacionSubscriber;
    }
    
    public void setEstadisticasController(controladores.EstadisticasController controller) {
        if (panelMisPartidos != null) {
            panelMisPartidos.setEstadisticasController(controller);
        }
    }
    
    public void setRegistroController(controladores.RegistroController controller) {
        if (panelLogin != null) {
            panelLogin.setRegistroController(controller);
        }
    }
    
    public void setAuthController(controladores.AuthController controller) {
        if (panelLogin != null) {
            panelLogin.setAuthController(controller);
        }
    }
    
    /**
     * Método llamado cuando el login es exitoso.
     * Establece el usuario en todos los controladores necesarios y verifica partidos pendientes.
     * 
     * @param usuario el usuario que se logueó exitosamente
     */
    public void onLoginExitoso(modelos.Usuario usuario) {
        // Establecer el usuario en todos los controladores que lo necesiten
        partidosController.setUsuarioActual(usuario);
        gestionEstadoController.setUsuarioActual(usuario);
        
        // Verificar si hay partidos pendientes de confirmación
        verificarPartidosPendientes(usuario);
        
        // Redirigir a la vista de búsqueda
        mostrarPanel("BUSCAR");
    }
    
    /**
     * Verifica si hay partidos pendientes de confirmación para el usuario.
     * Solo informa al usuario, no muestra ventanas automáticas.
     */
    private void verificarPartidosPendientes(modelos.Usuario usuario) {
        java.util.List<java.util.UUID> partidosPendientes = partidosController.obtenerPartidosPendientes();
        
        if (!partidosPendientes.isEmpty()) {
            System.out.println("📋 Usuario " + usuario.getUsername() + " tiene " + 
                partidosPendientes.size() + " partido(s) pendiente(s) de confirmación");
            
            // Informar al usuario que tiene partidos pendientes
            JOptionPane.showMessageDialog(this,
                "Tienes " + partidosPendientes.size() + " partido(s) pendiente(s) de confirmación.\n" +
                "Ve a 'Mis Partidos' para confirmarlos.",
                "Partidos Pendientes",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
}

