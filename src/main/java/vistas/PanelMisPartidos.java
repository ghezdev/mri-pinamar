package vistas;

import modelos.EstadisticasPartido;
import modelos.Partido;
import modelos.Usuario;
import modelos.EstadoTipo;
import controladores.EstadisticasController;
import controladores.PartidosController;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Panel para visualizar todos los partidos a los que el usuario logueado está unido.
 */
public class PanelMisPartidos extends JPanel {
    private final PartidosController controller;
    private final VentanaPrincipal ventanaPrincipal;
    private JTable tablaPartidos;
    private DefaultTableModel modeloTabla;
    private JLabel labelUsuario;
    private EstadisticasController estadisticasController;

    public PanelMisPartidos(PartidosController controller, VentanaPrincipal ventanaPrincipal) {
        this.controller = controller;
        this.ventanaPrincipal = ventanaPrincipal;
        inicializar();
    }
    
    /**
     * Método público para refrescar la tabla de partidos.
     * Se llama cuando se muestra este panel para asegurar que los datos estén actualizados.
     */
    public void refrescar() {
        if (controller.getUsuarioActual() == null) {
            // Si no hay usuario autenticado, no mostrar mensaje, solo limpiar la tabla
            labelUsuario.setText("No hay usuario logueado");
            modeloTabla.setRowCount(0);
            return;
        }
        cargarMisPartidos();
    }

    private void inicializar() {
        setLayout(new BorderLayout());

        // Panel superior con título y usuario
        JPanel panelTitulo = new JPanel(new BorderLayout());
        JLabel titulo = new JLabel("Mis Partidos");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 18f));
        panelTitulo.add(titulo, BorderLayout.WEST);
        
        labelUsuario = new JLabel();
        labelUsuario.setFont(labelUsuario.getFont().deriveFont(Font.ITALIC, 12f));
        panelTitulo.add(labelUsuario, BorderLayout.EAST);
        
        add(panelTitulo, BorderLayout.NORTH);

        // Tabla de partidos
        String[] columnas = {"ID", "Deporte", "Jugadores", "Cupo", "Estado", "Fecha/Hora", "Zona"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaPartidos = new JTable(modeloTabla);
        tablaPartidos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaPartidos.setRowHeight(25);
        JScrollPane scrollPane = new JScrollPane(tablaPartidos);
        add(scrollPane, BorderLayout.CENTER);

        // Panel de acciones - usar BoxLayout vertical para mejor organización
        JPanel panelAcciones = new JPanel();
        panelAcciones.setLayout(new BoxLayout(panelAcciones, BoxLayout.Y_AXIS));
        panelAcciones.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Fila 1: Navegación
        JPanel panelNavegacion = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnRefrescar = new JButton("🔄 Refrescar");
        btnRefrescar.addActionListener(e -> refrescar());
        panelNavegacion.add(btnRefrescar);
        
        JButton btnVolver = new JButton("← Volver a Buscar Partidos");
        btnVolver.addActionListener(e -> ventanaPrincipal.mostrarPanel("BUSCAR"));
        panelNavegacion.add(btnVolver);
        panelAcciones.add(panelNavegacion);
        
        panelAcciones.add(Box.createVerticalStrut(10));
        
        // Fila 2: Acciones de partido
        JPanel panelAccionesPartido = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnConfirmarPartido = new JButton("✅ Confirmar Partido");
        btnConfirmarPartido.addActionListener(e -> confirmarPartidoSeleccionado());
        panelAccionesPartido.add(btnConfirmarPartido);
        
        JButton btnCancelarPartido = new JButton("❌ Cancelar Partido (Anfitrión)");
        btnCancelarPartido.addActionListener(e -> cancelarPartidoSeleccionado());
        panelAccionesPartido.add(btnCancelarPartido);
        panelAcciones.add(panelAccionesPartido);
        
        panelAcciones.add(Box.createVerticalStrut(10));
        
        // Fila 3: Estadísticas (DESTACADAS)
        JPanel panelEstadisticas = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelEstadisticas.setBorder(BorderFactory.createTitledBorder("📊 Estadísticas del Partido"));
        JButton btnCargarEstadisticas = new JButton("➕ Cargar Estadísticas");
        btnCargarEstadisticas.setFont(btnCargarEstadisticas.getFont().deriveFont(Font.BOLD, 12f));
        btnCargarEstadisticas.addActionListener(e -> cargarEstadisticasPartido());
        panelEstadisticas.add(btnCargarEstadisticas);
        
        JButton btnVerEstadisticas = new JButton("👁️ Ver Estadísticas");
        btnVerEstadisticas.setFont(btnVerEstadisticas.getFont().deriveFont(Font.BOLD, 12f));
        btnVerEstadisticas.addActionListener(e -> verEstadisticasPartido());
        panelEstadisticas.add(btnVerEstadisticas);
        panelAcciones.add(panelEstadisticas);
        
        panelAcciones.add(Box.createVerticalStrut(10));
        
        // Fila 4: Cerrar sesión
        JPanel panelCerrarSesion = new JPanel(new FlowLayout(FlowLayout.RIGHT));
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
        panelCerrarSesion.add(btnCerrarSesion);
        panelAcciones.add(panelCerrarSesion);

        add(panelAcciones, BorderLayout.SOUTH);

        // No cargar partidos iniciales - se cargarán cuando el usuario acceda al panel
    }

    private void cargarMisPartidos() {
        if (controller.getUsuarioActual() == null) {
            labelUsuario.setText("No hay usuario logueado");
            modeloTabla.setRowCount(0);
            // No mostrar mensaje automáticamente - solo mostrar cuando el usuario intente acceder
            return;
        }

        // Actualizar label del usuario
        labelUsuario.setText("Usuario: " + controller.getUsuarioActual().getUsername());

        // Obtener todos los partidos y filtrar los que contienen al usuario actual
        List<Partido> todosLosPartidos = controller.listarTodos();
        List<Partido> misPartidos = todosLosPartidos.stream()
                .filter(p -> p.getJugadores().contains(controller.getUsuarioActual()))
                .collect(Collectors.toList());

        actualizarTabla(misPartidos);
    }

    private void actualizarTabla(List<Partido> partidos) {
        modeloTabla.setRowCount(0);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                .withZone(ZoneId.systemDefault());

        if (partidos.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "No estás unido a ningún partido actualmente.",
                "Sin partidos", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        for (Partido partido : partidos) {
            Object[] fila = {
                partido.getId().toString().substring(0, 8),
                partido.getDeporte() != null ? partido.getDeporte().getNombre() : "N/A",
                partido.getJugadores().size(),
                partido.getCupoRequerido(),
                partido.getEstado().tipo().getNombre(),
                formatter.format(partido.getFechaHora()),
                partido.getZona() != null ? partido.getZona().toString() : "N/A"
            };
            modeloTabla.addRow(fila);
        }
    }
    
    public void setEstadisticasController(EstadisticasController controller) {
        this.estadisticasController = controller;
    }
    
    private Partido obtenerPartidoSeleccionado() {
        int filaSeleccionada = tablaPartidos.getSelectedRow();
        if (filaSeleccionada < 0) {
            return null;
        }
        
        String idPartidoStr = (String) modeloTabla.getValueAt(filaSeleccionada, 0);
        List<Partido> todosLosPartidos = controller.listarTodos();
        return todosLosPartidos.stream()
                .filter(p -> p.getId().toString().startsWith(idPartidoStr))
                .findFirst()
                .orElse(null);
    }
    
    private void cargarEstadisticasPartido() {
        if (controller.getUsuarioActual() == null) {
            JOptionPane.showMessageDialog(this,
                "Debe estar autenticado para cargar estadísticas",
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        Partido partido = obtenerPartidoSeleccionado();
        if (partido == null) {
            JOptionPane.showMessageDialog(this,
                "Seleccione un partido de la tabla",
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (estadisticasController == null) {
            JOptionPane.showMessageDialog(this,
                "Controlador de estadísticas no inicializado",
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Verificar que el partido esté finalizado
        if (!EstadoTipo.FINALIZADO.equals(partido.getEstado().tipo())) {
            JOptionPane.showMessageDialog(this,
                "Solo se pueden cargar estadísticas para partidos finalizados.\n" +
                "Estado actual: " + partido.getEstado().tipo().getNombre(),
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Verificar que el usuario sea el anfitrión
        if (!partido.esCreador(controller.getUsuarioActual().getId())) {
            JOptionPane.showMessageDialog(this,
                "Solo el anfitrión (creador) del partido puede cargar estadísticas.\n\n" +
                "Creador del partido: " + (partido.getCreadorId() != null ? partido.getCreadorId().toString().substring(0, 8) : "N/A") + "\n" +
                "Tu ID: " + controller.getUsuarioActual().getId().toString().substring(0, 8),
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Verificar si ya existen estadísticas y cargarlas en el diálogo si existen
        EstadisticasPartido estadisticasExistentes = estadisticasController.obtenerEstadisticas(partido.getId());
        boolean tieneEstadisticas = estadisticasExistentes != null;
        
        // Crear diálogo para cargar/actualizar estadísticas
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Cargar Estadísticas", true);
        dialog.setLayout(new BorderLayout());
        
        // Panel con scroll para los jugadores
        JPanel panelFormulario = new JPanel();
        panelFormulario.setLayout(new BoxLayout(panelFormulario, BoxLayout.Y_AXIS));
        panelFormulario.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Título
        JLabel titulo = new JLabel("Estadísticas por Jugador");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 14f));
        panelFormulario.add(titulo);
        panelFormulario.add(Box.createVerticalStrut(10));
        
        // Map para almacenar los spinners de goles y tarjetas por jugador
        Map<UUID, JSpinner> spinnersGoles = new java.util.HashMap<>();
        Map<UUID, JSpinner> spinnersTarjetas = new java.util.HashMap<>();
        
        // Crear fila para cada jugador
        for (Usuario jugador : partido.getJugadores()) {
            JPanel filaJugador = new JPanel(new FlowLayout(FlowLayout.LEFT));
            filaJugador.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEtchedBorder(),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
            ));
            
            // Nombre del jugador
            JLabel labelJugador = new JLabel(jugador.getUsername() + ":");
            labelJugador.setPreferredSize(new Dimension(120, 25));
            filaJugador.add(labelJugador);
            
            // Goles
            filaJugador.add(new JLabel("Goles:"));
            int golesInicial = tieneEstadisticas ? estadisticasExistentes.getGolesJugador(jugador.getId()) : 0;
            JSpinner spinnerGoles = new JSpinner(new SpinnerNumberModel(golesInicial, 0, 100, 1));
            spinnerGoles.setPreferredSize(new Dimension(60, 25));
            spinnersGoles.put(jugador.getId(), spinnerGoles);
            filaJugador.add(spinnerGoles);
            
            // Tarjetas
            filaJugador.add(new JLabel("Tarjetas:"));
            int tarjetasInicial = tieneEstadisticas ? estadisticasExistentes.getTarjetasJugador(jugador.getId()) : 0;
            JSpinner spinnerTarjetas = new JSpinner(new SpinnerNumberModel(tarjetasInicial, 0, 10, 1));
            spinnerTarjetas.setPreferredSize(new Dimension(60, 25));
            spinnersTarjetas.put(jugador.getId(), spinnerTarjetas);
            filaJugador.add(spinnerTarjetas);
            
            panelFormulario.add(filaJugador);
            panelFormulario.add(Box.createVerticalStrut(5));
        }
        
        // Panel con scroll
        JScrollPane scrollPane = new JScrollPane(panelFormulario);
        scrollPane.setPreferredSize(new Dimension(400, 300));
        dialog.add(scrollPane, BorderLayout.CENTER);
        
        // Botones
        JPanel panelBotones = new JPanel(new FlowLayout());
        String textoBoton = tieneEstadisticas ? "Actualizar Estadísticas" : "Guardar Estadísticas";
        JButton btnGuardar = new JButton(textoBoton);
        JButton btnCancelar = new JButton("Cancelar");
        
        btnGuardar.addActionListener(e -> {
            try {
                // Recolectar goles y tarjetas por jugador
                Map<UUID, Integer> golesPorJugador = new java.util.HashMap<>();
                Map<UUID, Integer> tarjetasPorJugador = new java.util.HashMap<>();
                
                for (Usuario jugador : partido.getJugadores()) {
                    int goles = (Integer) spinnersGoles.get(jugador.getId()).getValue();
                    int tarjetas = (Integer) spinnersTarjetas.get(jugador.getId()).getValue();
                    
                    if (goles > 0) {
                        golesPorJugador.put(jugador.getId(), goles);
                    }
                    if (tarjetas > 0) {
                        tarjetasPorJugador.put(jugador.getId(), tarjetas);
                    }
                }
                
                estadisticasController.cargarEstadisticas(
                    partido.getId(),
                    golesPorJugador,
                    tarjetasPorJugador
                );
                
                String mensajeExito = tieneEstadisticas 
                    ? "✅ Estadísticas actualizadas exitosamente"
                    : "✅ Estadísticas cargadas exitosamente";
                JOptionPane.showMessageDialog(dialog,
                    mensajeExito,
                    "Éxito",
                    JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                refrescar();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog,
                    "Error al cargar estadísticas: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        });
        
        btnCancelar.addActionListener(e -> dialog.dispose());
        
        panelBotones.add(btnGuardar);
        panelBotones.add(btnCancelar);
        dialog.add(panelBotones, BorderLayout.SOUTH);
        
        // Actualizar título del diálogo
        if (tieneEstadisticas) {
            dialog.setTitle("Actualizar Estadísticas");
        }
        
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
    
    private void verEstadisticasPartido() {
        if (controller.getUsuarioActual() == null) {
            JOptionPane.showMessageDialog(this,
                "Debe estar autenticado para ver estadísticas",
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        Partido partido = obtenerPartidoSeleccionado();
        if (partido == null) {
            JOptionPane.showMessageDialog(this,
                "Seleccione un partido de la tabla",
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (estadisticasController == null) {
            JOptionPane.showMessageDialog(this,
                "Controlador de estadísticas no inicializado",
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        EstadisticasPartido estadisticas = estadisticasController.obtenerEstadisticas(partido.getId());
        
        if (estadisticas == null) {
            JOptionPane.showMessageDialog(this,
                "Este partido no tiene estadísticas cargadas",
                "Sin estadísticas",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        // Construir mensaje con las estadísticas por jugador
        StringBuilder mensaje = new StringBuilder();
        mensaje.append("📊 Estadísticas del Partido\n\n");
        
        // Goles por jugador
        Map<UUID, Integer> golesPorJugador = estadisticas.getGolesPorJugador();
        if (!golesPorJugador.isEmpty()) {
            mensaje.append("⚽ GOLES POR JUGADOR:\n");
            golesPorJugador.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue())) // Ordenar por goles descendente
                .forEach(entry -> {
                    Usuario jugador = partido.getJugadores().stream()
                            .filter(j -> j.getId().equals(entry.getKey()))
                            .findFirst()
                            .orElse(null);
                    if (jugador != null) {
                        mensaje.append("  • ").append(jugador.getUsername())
                               .append(": ").append(entry.getValue()).append(" goles\n");
                    }
                });
            mensaje.append("\n");
        }
        
        // Tarjetas por jugador
        Map<UUID, Integer> tarjetasPorJugador = estadisticas.getTarjetasPorJugador();
        if (!tarjetasPorJugador.isEmpty()) {
            mensaje.append("🟨 TARJETAS POR JUGADOR:\n");
            tarjetasPorJugador.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue())) // Ordenar por tarjetas descendente
                .forEach(entry -> {
                    Usuario jugador = partido.getJugadores().stream()
                            .filter(j -> j.getId().equals(entry.getKey()))
                            .findFirst()
                            .orElse(null);
                    if (jugador != null) {
                        mensaje.append("  • ").append(jugador.getUsername())
                               .append(": ").append(entry.getValue()).append(" tarjetas\n");
                    }
                });
            mensaje.append("\n");
        }
        
        // Goleador (jugador con más goles)
        UUID goleadorId = estadisticas.getGoleadorId();
        if (goleadorId != null) {
            Usuario goleador = partido.getJugadores().stream()
                    .filter(j -> j.getId().equals(goleadorId))
                    .findFirst()
                    .orElse(null);
            if (goleador != null) {
                mensaje.append("🏆 Goleador: ").append(goleador.getUsername())
                       .append(" (").append(estadisticas.getGolesJugador(goleadorId)).append(" goles)\n");
            }
        }
        
        // Totales
        mensaje.append("\n📈 TOTALES:\n");
        mensaje.append("  • Total de goles: ").append(estadisticas.getTotalGoles()).append("\n");
        mensaje.append("  • Total de tarjetas: ").append(estadisticas.getTotalTarjetas());
        
        JOptionPane.showMessageDialog(this,
            mensaje.toString(),
            "Estadísticas del Partido",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void confirmarPartidoSeleccionado() {
        if (controller.getUsuarioActual() == null) {
            JOptionPane.showMessageDialog(this,
                "Debe estar autenticado para confirmar un partido",
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        int filaSeleccionada = tablaPartidos.getSelectedRow();
        if (filaSeleccionada < 0) {
            JOptionPane.showMessageDialog(this,
                "Seleccione un partido de la tabla",
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Obtener el ID del partido desde la tabla
        String idPartidoStr = (String) modeloTabla.getValueAt(filaSeleccionada, 0);
        // Buscar el partido completo
        List<Partido> todosLosPartidos = controller.listarTodos();
        Partido partidoSeleccionado = todosLosPartidos.stream()
                .filter(p -> p.getId().toString().startsWith(idPartidoStr))
                .findFirst()
                .orElse(null);
        
        if (partidoSeleccionado == null) {
            JOptionPane.showMessageDialog(this,
                "No se pudo encontrar el partido seleccionado",
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Verificar que el usuario esté en el partido
        if (!partidoSeleccionado.getJugadores().contains(controller.getUsuarioActual())) {
            JOptionPane.showMessageDialog(this,
                "No estás en este partido",
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Verificar que el partido esté en estado Armado
        EstadoTipo estado = partidoSeleccionado.getEstado().tipo();
        if (!EstadoTipo.ARMADO.equals(estado)) {
            JOptionPane.showMessageDialog(this,
                "Solo se pueden confirmar partidos en estado 'Armado'",
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Verificar si ya confirmó
        if (partidoSeleccionado.haConfirmado(controller.getUsuarioActual().getId())) {
            JOptionPane.showMessageDialog(this,
                "Ya confirmaste este partido",
                "Información", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        try {
            // Confirmar el partido
            controller.confirmarPartido(partidoSeleccionado.getId());
            
            // Eliminar de pendientes
            controller.eliminarPartidoPendiente(partidoSeleccionado.getId());
            
            // Recargar partido para obtener estado actualizado
            Partido partidoActualizado = controller.obtenerPartidoPorIdOpcional(partidoSeleccionado.getId());
            if (partidoActualizado == null) {
                partidoActualizado = partidoSeleccionado;
            }
            
            if (partidoActualizado.todosConfirmaron()) {
                JOptionPane.showMessageDialog(this,
                    "✅ ¡Todos los jugadores confirmaron! El partido está confirmado.",
                    "Partido Confirmado",
                    JOptionPane.INFORMATION_MESSAGE);
            } else {
                int confirmaciones = partidoActualizado.getNumeroConfirmaciones();
                int total = partidoActualizado.getJugadores().size();
                JOptionPane.showMessageDialog(this,
                    "✅ Tu confirmación fue registrada.\n" +
                    "Confirmaciones: " + confirmaciones + "/" + total + 
                    "\nEsperando confirmación de los demás jugadores...",
                    "Confirmación Registrada",
                    JOptionPane.INFORMATION_MESSAGE);
            }
            
            refrescar();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error al confirmar el partido: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void cancelarPartidoSeleccionado() {
        if (controller.getUsuarioActual() == null) {
            JOptionPane.showMessageDialog(this,
                "Debe estar autenticado para cancelar un partido",
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        int filaSeleccionada = tablaPartidos.getSelectedRow();
        if (filaSeleccionada < 0) {
            JOptionPane.showMessageDialog(this,
                "Seleccione un partido de la tabla",
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Obtener el ID del partido desde la tabla
        String idPartidoStr = (String) modeloTabla.getValueAt(filaSeleccionada, 0);
        // Buscar el partido completo
        List<Partido> todosLosPartidos = controller.listarTodos();
        Partido partidoSeleccionado = todosLosPartidos.stream()
                .filter(p -> p.getId().toString().startsWith(idPartidoStr))
                .findFirst()
                .orElse(null);
        
        if (partidoSeleccionado == null) {
            JOptionPane.showMessageDialog(this,
                "No se pudo encontrar el partido seleccionado",
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Verificar que el usuario sea el creador
        if (!partidoSeleccionado.esCreador(controller.getUsuarioActual().getId())) {
            JOptionPane.showMessageDialog(this,
                "Solo el anfitrión (creador) del partido puede cancelarlo",
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Verificar que el estado permita cancelar (NecesitamosJugadores o Armado)
        EstadoTipo estado = partidoSeleccionado.getEstado().tipo();
        if (!EstadoTipo.NECESITAMOS_JUGADORES.equals(estado) && 
            !EstadoTipo.ARMADO.equals(estado)) {
            JOptionPane.showMessageDialog(this,
                "Solo se pueden cancelar partidos en estado 'Necesitamos Jugadores' o 'Armado'",
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Confirmar cancelación
        int confirmacion = JOptionPane.showConfirmDialog(this,
            "¿Está seguro que desea cancelar este partido?\n\n" +
            "Deporte: " + (partidoSeleccionado.getDeporte() != null ? partidoSeleccionado.getDeporte().getNombre() : "N/A") + "\n" +
            "Jugadores: " + partidoSeleccionado.getJugadores().size() + "/" + partidoSeleccionado.getCupoRequerido(),
            "Cancelar Partido",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirmacion == JOptionPane.YES_OPTION) {
            try {
                controller.cancelarPartido(partidoSeleccionado.getId());
                JOptionPane.showMessageDialog(this,
                    "✅ Partido cancelado exitosamente",
                    "Partido Cancelado",
                    JOptionPane.INFORMATION_MESSAGE);
                refrescar();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                    "Error al cancelar el partido: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}

