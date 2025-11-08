package vistas;

import modelos.Partido;
import modelos.Usuario;
import modelos.EstadoTipo;
import controladores.GestionEstadoController;
import controladores.PartidosController;

import javax.swing.*;
import java.awt.*;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * Panel para gestionar el estado de los partidos.
 */
public class PanelGestionEstado extends JPanel {
    private final PartidosController partidosController;
    private final VentanaPrincipal ventanaPrincipal;
    private GestionEstadoController gestionEstadoController;

    private JComboBox<Partido> comboPartidos;
    private JLabel labelEstado;
    private JList<Usuario> listaJugadores;
    private DefaultListModel<Usuario> modeloJugadores;
    private JButton btnConfirmar;
    private JButton btnIniciar;
    private JButton btnFinalizar;
    private JButton btnCancelar;

    public PanelGestionEstado(PartidosController partidosController, VentanaPrincipal ventanaPrincipal) {
        this.partidosController = partidosController;
        this.ventanaPrincipal = ventanaPrincipal;
        inicializar();
    }

    public void setGestionEstadoController(GestionEstadoController controller) {
        this.gestionEstadoController = controller;
        // Actualizar usuario actual cuando se establece el controlador
        if (partidosController != null && partidosController.getUsuarioActual() != null) {
            this.gestionEstadoController.setUsuarioActual(partidosController.getUsuarioActual());
        }
    }

    private void inicializar() {
        setLayout(new BorderLayout());

        // Panel superior: selección de partido
        JPanel panelSuperior = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelSuperior.add(new JLabel("Seleccionar Partido:"));
        comboPartidos = new JComboBox<>();
        comboPartidos.addActionListener(e -> cargarPartido());
        panelSuperior.add(comboPartidos);
        
        JButton btnRefrescar = new JButton("Refrescar");
        btnRefrescar.addActionListener(e -> cargarPartidos());
        panelSuperior.add(btnRefrescar);

        add(panelSuperior, BorderLayout.NORTH);

        // Panel central: información del partido
        JPanel panelCentral = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        panelCentral.add(new JLabel("Estado:"), gbc);
        gbc.gridx = 1;
        labelEstado = new JLabel("N/A");
        panelCentral.add(labelEstado, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panelCentral.add(new JLabel("Jugadores:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        modeloJugadores = new DefaultListModel<>();
        listaJugadores = new JList<>(modeloJugadores);
        JScrollPane scrollJugadores = new JScrollPane(listaJugadores);
        scrollJugadores.setPreferredSize(new Dimension(300, 200));
        panelCentral.add(scrollJugadores, gbc);

        add(panelCentral, BorderLayout.CENTER);

        // Panel inferior: botones de acción
        JPanel panelAcciones = new JPanel(new FlowLayout());
        btnConfirmar = new JButton("Confirmar");
        btnConfirmar.addActionListener(e -> confirmar());
        panelAcciones.add(btnConfirmar);

        btnIniciar = new JButton("Iniciar");
        btnIniciar.addActionListener(e -> iniciar());
        panelAcciones.add(btnIniciar);

        btnFinalizar = new JButton("Finalizar");
        btnFinalizar.addActionListener(e -> finalizar());
        panelAcciones.add(btnFinalizar);

        btnCancelar = new JButton("Cancelar");
        btnCancelar.addActionListener(e -> cancelar());
        panelAcciones.add(btnCancelar);

        add(panelAcciones, BorderLayout.SOUTH);

        // Cargar partidos iniciales
        cargarPartidos();
    }

    private void cargarPartidos() {
        comboPartidos.removeAllItems();
        List<Partido> partidos = partidosController.listarTodos();
        for (Partido partido : partidos) {
            comboPartidos.addItem(partido);
        }
        if (comboPartidos.getItemCount() > 0) {
            comboPartidos.setSelectedIndex(0);
        }
    }

    private void cargarPartido() {
        Partido partido = (Partido) comboPartidos.getSelectedItem();
        if (partido == null) {
            labelEstado.setText("N/A");
            modeloJugadores.clear();
            return;
        }

        // Actualizar estado
        labelEstado.setText(partido.getEstado().tipo().getNombre());

        // Actualizar jugadores
        modeloJugadores.clear();
        for (Usuario jugador : partido.getJugadores()) {
            modeloJugadores.addElement(jugador);
        }

        // Asegurar que el usuario actual esté establecido en el controlador de gestión
        if (gestionEstadoController != null && partidosController != null && 
            partidosController.getUsuarioActual() != null) {
            gestionEstadoController.setUsuarioActual(partidosController.getUsuarioActual());
        }

        // Habilitar/deshabilitar botones según el estado
        EstadoTipo estado = partido.getEstado().tipo();
        btnConfirmar.setEnabled(EstadoTipo.ARMADO.equals(estado));
        btnIniciar.setEnabled(EstadoTipo.CONFIRMADO.equals(estado));
        btnFinalizar.setEnabled(EstadoTipo.EN_JUEGO.equals(estado));
        
        // Permitir cancelar solo si:
        // 1. El estado permite cancelar (no FINALIZADO, CANCELADO, EN_JUEGO)
        // 2. El usuario actual es el anfitrión del partido
        boolean puedeCancelarPorEstado = !EstadoTipo.FINALIZADO.equals(estado) && 
                                         !EstadoTipo.CANCELADO.equals(estado) && 
                                         !EstadoTipo.EN_JUEGO.equals(estado);
        
        // Verificar que el usuario sea el anfitrión
        boolean esAnfitrion = false;
        if (partidosController != null && partidosController.getUsuarioActual() != null) {
            esAnfitrion = partido.esCreador(partidosController.getUsuarioActual().getId());
        }
        
        // El botón se habilita solo si el estado lo permite Y el usuario es anfitrión
        btnCancelar.setEnabled(puedeCancelarPorEstado && esAnfitrion);
    }

    private void confirmar() {
        if (gestionEstadoController == null) {
            JOptionPane.showMessageDialog(this, 
                "Servicio no inicializado",
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        ejecutarAccion(() -> {
            Partido partido = (Partido) comboPartidos.getSelectedItem();
            if (partido != null) {
                // Actualizar usuario actual antes de confirmar
                if (partidosController != null && partidosController.getUsuarioActual() != null) {
                    gestionEstadoController.setUsuarioActual(partidosController.getUsuarioActual());
                }
                gestionEstadoController.confirmar(partido.getId());
                
                // Recargar partido para obtener estado actualizado
                partido = gestionEstadoController.obtenerPartido(partido.getId());
                if (partido.todosConfirmaron()) {
                    JOptionPane.showMessageDialog(this, 
                        "¡Todos los jugadores confirmaron! El partido está confirmado.", 
                        "Partido Confirmado", 
                        JOptionPane.INFORMATION_MESSAGE);
                } else {
                    int confirmaciones = partido.getNumeroConfirmaciones();
                    int total = partido.getJugadores().size();
                    JOptionPane.showMessageDialog(this, 
                        "Su confirmación fue registrada.\nConfirmaciones: " + confirmaciones + "/" + total + 
                        "\nEsperando confirmación de los demás jugadores...", 
                        "Confirmación Registrada", 
                        JOptionPane.INFORMATION_MESSAGE);
                }
                cargarPartidos();
                cargarPartido();
            }
        });
    }

    private void iniciar() {
        if (gestionEstadoController == null) {
            JOptionPane.showMessageDialog(this, 
                "Servicio no inicializado",
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        ejecutarAccion(() -> {
            Partido partido = (Partido) comboPartidos.getSelectedItem();
            if (partido != null) {
                gestionEstadoController.iniciar(partido.getId());
                cargarPartidos();
                cargarPartido();
            }
        });
    }

    private void finalizar() {
        if (gestionEstadoController == null) {
            JOptionPane.showMessageDialog(this, 
                "Servicio no inicializado",
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        ejecutarAccion(() -> {
            Partido partido = (Partido) comboPartidos.getSelectedItem();
            if (partido != null) {
                gestionEstadoController.finalizar(partido.getId());
                cargarPartidos();
                cargarPartido();
            }
        });
    }

    private void cancelar() {
        if (gestionEstadoController == null) {
            JOptionPane.showMessageDialog(this, 
                "Servicio no inicializado",
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        ejecutarAccion(() -> {
            Partido partido = (Partido) comboPartidos.getSelectedItem();
            if (partido != null) {
                gestionEstadoController.cancelar(partido.getId());
                cargarPartidos();
                cargarPartido();
            }
        });
    }

    private void ejecutarAccion(Runnable accion) {
        try {
            accion.run();
            JOptionPane.showMessageDialog(this, 
                "Operación realizada exitosamente",
                "Éxito", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

