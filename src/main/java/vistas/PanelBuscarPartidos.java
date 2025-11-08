package vistas;

import modelos.Partido;
import modelos.BusquedaPartidosCriteria;
import controladores.PartidosController;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Panel para buscar y unirse a partidos.
 */
public class PanelBuscarPartidos extends JPanel {
    private final PartidosController controller;
    private final VentanaPrincipal ventanaPrincipal;
    private JTable tablaPartidos;
    private DefaultTableModel modeloTabla;

    public PanelBuscarPartidos(PartidosController controller, VentanaPrincipal ventanaPrincipal) {
        this.controller = controller;
        this.ventanaPrincipal = ventanaPrincipal;
        inicializar();
    }
    
    /**
     * Método público para refrescar la tabla de partidos.
     * Se llama cuando se muestra este panel para asegurar que los datos estén actualizados.
     */
    public void refrescar() {
        buscar();
    }

    private void inicializar() {
        setLayout(new BorderLayout());

        // Panel superior con usuario y cerrar sesión
        JPanel panelSuperior = new JPanel(new BorderLayout());
        
        // Panel de usuario y cerrar sesión (derecha)
        JPanel panelUsuario = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        if (controller.getUsuarioActual() != null) {
            JLabel labelUsuario = new JLabel("Usuario: " + controller.getUsuarioActual().getUsername());
            labelUsuario.setFont(labelUsuario.getFont().deriveFont(Font.BOLD));
            panelUsuario.add(labelUsuario);
            panelUsuario.add(Box.createHorizontalStrut(10));
        }
        JButton btnCerrarSesion = new JButton("🚪 Cerrar Sesión");
        btnCerrarSesion.addActionListener(e -> {
            int confirmacion = JOptionPane.showConfirmDialog(this,
                "¿Está seguro que desea cerrar sesión?",
                "Cerrar Sesión",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
            
            if (confirmacion == JOptionPane.YES_OPTION) {
                controller.cerrarSesion();
                JOptionPane.showMessageDialog(this,
                    "Sesión cerrada exitosamente",
                    "Sesión Cerrada",
                    JOptionPane.INFORMATION_MESSAGE);
                ventanaPrincipal.mostrarPanel("LOGIN");
            }
        });
        panelUsuario.add(btnCerrarSesion);
        
        panelSuperior.add(panelUsuario, BorderLayout.EAST);
        add(panelSuperior, BorderLayout.NORTH);

        // Tabla de partidos
        String[] columnas = {"ID", "Deporte", "Jugadores", "Cupo", "Estado", "Fecha/Hora", "Ubicación"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaPartidos = new JTable(modeloTabla);
        tablaPartidos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(tablaPartidos);
        add(scrollPane, BorderLayout.CENTER);

        // Panel de acciones
        JPanel panelAcciones = new JPanel(new FlowLayout());
        
        // Botón destacado para crear partido
        JButton btnCrearPartido = new JButton("➕ Crear Nuevo Partido");
        btnCrearPartido.setFont(btnCrearPartido.getFont().deriveFont(Font.BOLD, 12f));
        btnCrearPartido.addActionListener(e -> ventanaPrincipal.mostrarPanel("CREAR"));
        panelAcciones.add(btnCrearPartido);
        
        // Botón para ver mis partidos
        JButton btnMisPartidos = new JButton("📋 Mis Partidos");
        btnMisPartidos.setFont(btnMisPartidos.getFont().deriveFont(Font.BOLD, 12f));
        btnMisPartidos.addActionListener(e -> ventanaPrincipal.mostrarPanel("MIS_PARTIDOS"));
        panelAcciones.add(btnMisPartidos);
        
        // Separador visual
        panelAcciones.add(Box.createHorizontalStrut(20));
        
        JButton btnUnirse = new JButton("Unirme");
        btnUnirse.addActionListener(e -> unirse());
        panelAcciones.add(btnUnirse);
        
        JButton btnRefrescar = new JButton("🔄 Refrescar");
        btnRefrescar.addActionListener(e -> buscar());
        panelAcciones.add(btnRefrescar);

        add(panelAcciones, BorderLayout.SOUTH);

        // Cargar partidos iniciales
        buscar();
    }

    private void buscar() {
        // Buscar todos los partidos sin filtros
        BusquedaPartidosCriteria criteria = new BusquedaPartidosCriteria();
        List<Partido> partidos = controller.buscarPartidos(criteria);
        actualizarTabla(partidos);
    }

    private void actualizarTabla(List<Partido> partidos) {
        modeloTabla.setRowCount(0);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                .withZone(ZoneId.systemDefault());

        for (Partido partido : partidos) {
            int cantidadJugadores = partido.getJugadores().size();
            // Debug: verificar que la cantidad de jugadores sea correcta
            if (cantidadJugadores == 0 && partido.getCupoRequerido() > 0) {
                System.out.println("⚠️ Advertencia: Partido " + partido.getId().toString().substring(0, 8) + 
                    " tiene 0 jugadores pero debería tener al menos 1 (el creador)");
            }
            Object[] fila = {
                partido.getId().toString().substring(0, 8),
                partido.getDeporte() != null ? partido.getDeporte().getNombre() : "N/A",
                cantidadJugadores,
                partido.getCupoRequerido(),
                partido.getEstado().tipo().getNombre(),
                formatter.format(partido.getFechaHora()),
                partido.getZona() != null ? partido.getZona().toString() : "N/A"
            };
            modeloTabla.addRow(fila);
        }
    }

    private void unirse() {
        int filaSeleccionada = tablaPartidos.getSelectedRow();
        if (filaSeleccionada < 0) {
            JOptionPane.showMessageDialog(this, 
                "Seleccione un partido",
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (controller.getUsuarioActual() == null) {
            JOptionPane.showMessageDialog(this, 
                "Debe estar autenticado",
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            // Obtener ID del partido desde la tabla
            String idStr = (String) modeloTabla.getValueAt(filaSeleccionada, 0);
            // Buscar el partido completo
            List<Partido> partidos = controller.listarTodos();
            Partido partido = partidos.stream()
                    .filter(p -> p.getId().toString().startsWith(idStr))
                    .findFirst()
                    .orElse(null);

            if (partido != null) {
                controller.unirseAPartido(partido.getId());
                JOptionPane.showMessageDialog(this, 
                    "Te has unido al partido exitosamente",
                    "Éxito", JOptionPane.INFORMATION_MESSAGE);
                buscar(); // Refrescar
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

