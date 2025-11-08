package vistas;

import modelos.Deporte;
import modelos.NivelJuego;
import modelos.Partido;
import modelos.Zona;
import modelos.EmparejamientoStrategyFactory;
import modelos.CrearPartidoCmd;
import controladores.CrearPartidoController;
import controladores.PartidosController;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;

/**
 * Panel para crear nuevos partidos.
 */
public class PanelCrearPartido extends JPanel {
    private final PartidosController partidosController;
    private final VentanaPrincipal ventanaPrincipal;
    private CrearPartidoController crearPartidoController;

    private JComboBox<Deporte> comboDeporte;
    private JSpinner spinnerCupo;
    private JSpinner spinnerDuracion;
    private JComboBox<Zona> comboZona;
    private JSpinner spinnerFechaHora;
    private JComboBox<String> comboEstrategia;
    
    // Campos adicionales para estrategia "Por Nivel"
    private JPanel panelNivelOpciones;
    private JComboBox<modelos.NivelJuego> comboNivelObjetivo;
    private JComboBox<String> comboTipoFiltro;


    public PanelCrearPartido(PartidosController partidosController, VentanaPrincipal ventanaPrincipal) {
        this.partidosController = partidosController;
        this.ventanaPrincipal = ventanaPrincipal;
        inicializar();
    }

    public void setCrearPartidoController(CrearPartidoController controller) {
        this.crearPartidoController = controller;
    }

    private void inicializar() {
        setLayout(new BorderLayout());

        // Panel superior con título y cerrar sesión
        JPanel panelTitulo = new JPanel(new BorderLayout());
        JPanel panelTituloCentro = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel titulo = new JLabel("Crear Nuevo Partido");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 18f));
        panelTituloCentro.add(titulo);
        panelTitulo.add(panelTituloCentro, BorderLayout.CENTER);
        
        // Botón cerrar sesión a la derecha
        JPanel panelDerecha = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnCerrarSesion = new JButton("🚪 Cerrar Sesión");
        btnCerrarSesion.addActionListener(e -> {
            int confirmacion = JOptionPane.showConfirmDialog(this,
                "¿Está seguro que desea cerrar sesión?",
                "Cerrar Sesión",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
            
            if (confirmacion == JOptionPane.YES_OPTION) {
                partidosController.cerrarSesion();
                JOptionPane.showMessageDialog(this,
                    "Sesión cerrada exitosamente",
                    "Sesión Cerrada",
                    JOptionPane.INFORMATION_MESSAGE);
                ventanaPrincipal.mostrarPanel("LOGIN");
            }
        });
        panelDerecha.add(btnCerrarSesion);
        panelTitulo.add(panelDerecha, BorderLayout.EAST);
        add(panelTitulo, BorderLayout.NORTH);

        // Panel central con formulario
        JPanel panelFormulario = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Deporte
        gbc.gridx = 0;
        gbc.gridy = 0;
        panelFormulario.add(new JLabel("Deporte:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        comboDeporte = new JComboBox<>(Deporte.values());
        comboDeporte.setSelectedItem(Deporte.FUTBOL);
        panelFormulario.add(comboDeporte, gbc);
        gbc.weightx = 0.0;

        // Cupo requerido
        gbc.gridx = 0;
        gbc.gridy = 1;
        panelFormulario.add(new JLabel("Cupo Requerido:"), gbc);
        gbc.gridx = 1;
        spinnerCupo = new JSpinner(new SpinnerNumberModel(10, 2, 50, 1));
        panelFormulario.add(spinnerCupo, gbc);

        // Duración
        gbc.gridx = 0;
        gbc.gridy = 2;
        panelFormulario.add(new JLabel("Duración (min):"), gbc);
        gbc.gridx = 1;
        spinnerDuracion = new JSpinner(new SpinnerNumberModel(30, 1, 90, 1));
        panelFormulario.add(spinnerDuracion, gbc);

        // Zona
        gbc.gridx = 0;
        gbc.gridy = 3;
        panelFormulario.add(new JLabel("Zona:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        comboZona = new JComboBox<>(new Zona[]{
            Zona.ZONA_SUR, Zona.ZONA_NORTE, Zona.ZONA_OESTE, Zona.ZONA_ESTE
        });
        comboZona.setSelectedItem(Zona.ZONA_SUR);
        panelFormulario.add(comboZona, gbc);
        gbc.weightx = 0.0;

        // Fecha/Hora
        gbc.gridx = 0;
        gbc.gridy = 4;
        panelFormulario.add(new JLabel("Fecha/Hora:"), gbc);
        gbc.gridx = 1;
        LocalDateTime fechaDefault = LocalDateTime.now().plusDays(1).withHour(18).withMinute(0);
        spinnerFechaHora = new JSpinner(new SpinnerDateModel(
            java.sql.Timestamp.valueOf(fechaDefault),
            null, null, java.util.Calendar.HOUR_OF_DAY));
        JSpinner.DateEditor editor = new JSpinner.DateEditor(spinnerFechaHora, "dd/MM/yyyy HH:mm");
        spinnerFechaHora.setEditor(editor);
        panelFormulario.add(spinnerFechaHora, gbc);

        // Estrategia de emparejamiento
        gbc.gridx = 0;
        gbc.gridy = 5;
        panelFormulario.add(new JLabel("Estrategia Emparejamiento:"), gbc);
        gbc.gridx = 1;
        comboEstrategia = new JComboBox<>(EmparejamientoStrategyFactory.nombres().toArray(new String[0]));
        comboEstrategia.addActionListener(e -> actualizarCamposEstrategia());
        panelFormulario.add(comboEstrategia, gbc);
        
        // Panel de opciones para estrategia "Por Nivel" (inicialmente oculto)
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 2;
        panelNivelOpciones = new JPanel(new GridBagLayout());
        GridBagConstraints gbcNivel = new GridBagConstraints();
        gbcNivel.insets = new Insets(5, 5, 5, 5);
        gbcNivel.anchor = GridBagConstraints.WEST;
        
        gbcNivel.gridx = 0;
        gbcNivel.gridy = 0;
        panelNivelOpciones.add(new JLabel("Nivel Objetivo:"), gbcNivel);
        gbcNivel.gridx = 1;
        comboNivelObjetivo = new JComboBox<>(NivelJuego.values());
        panelNivelOpciones.add(comboNivelObjetivo, gbcNivel);
        
        gbcNivel.gridx = 0;
        gbcNivel.gridy = 1;
        panelNivelOpciones.add(new JLabel("Tipo de Filtro:"), gbcNivel);
        gbcNivel.gridx = 1;
        comboTipoFiltro = new JComboBox<>(new String[]{"MINIMO", "MAXIMO"});
        panelNivelOpciones.add(comboTipoFiltro, gbcNivel);
        
        panelNivelOpciones.setBorder(BorderFactory.createTitledBorder("Opciones de Emparejamiento por Nivel"));
        panelNivelOpciones.setVisible(false);
        panelFormulario.add(panelNivelOpciones, gbc);
        gbc.gridwidth = 1;

        // Panel de botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton btnCrear = new JButton("✅ Crear Partido");
        btnCrear.setFont(btnCrear.getFont().deriveFont(Font.BOLD, 14f));
        btnCrear.setPreferredSize(new Dimension(200, 35));
        btnCrear.addActionListener(e -> crearPartido());
        panelBotones.add(btnCrear);
        
        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.addActionListener(e -> ventanaPrincipal.mostrarPanel("BUSCAR"));
        panelBotones.add(btnCancelar);

        // Agregar paneles
        JPanel panelCentral = new JPanel(new BorderLayout());
        panelCentral.add(panelFormulario, BorderLayout.CENTER);
        panelCentral.add(panelBotones, BorderLayout.SOUTH);
        
        // Agregar padding
        panelCentral.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));
        
        add(panelCentral, BorderLayout.CENTER);
        
        // Verificar si "Por Nivel" está seleccionado por defecto y mostrar campos
        actualizarCamposEstrategia();
    }
    
    /**
     * Actualiza la visibilidad de los campos adicionales según la estrategia seleccionada.
     */
    private void actualizarCamposEstrategia() {
        String estrategiaSeleccionada = (String) comboEstrategia.getSelectedItem();
        boolean esPorNivel = "Por Nivel".equals(estrategiaSeleccionada);
        panelNivelOpciones.setVisible(esPorNivel);
        revalidate();
        repaint();
    }

    private void crearPartido() {
        try {
            CrearPartidoCmd cmd = new CrearPartidoCmd();
            Deporte deporteSeleccionado = (Deporte) comboDeporte.getSelectedItem();
            if (deporteSeleccionado == null) {
                throw new IllegalArgumentException("Debe seleccionar un deporte");
            }
            cmd.setDeporte(deporteSeleccionado);
            cmd.setCupoRequerido((Integer) spinnerCupo.getValue());
            cmd.setDuracion((Integer) spinnerDuracion.getValue());
            
            Zona zona = (Zona) comboZona.getSelectedItem();
            if (zona == null) {
                throw new IllegalArgumentException("Debe seleccionar una zona");
            }
            cmd.setZona(zona);

            java.util.Date fecha = (java.util.Date) spinnerFechaHora.getValue();
            cmd.setFechaHora(fecha.toInstant());
            
            String estrategia = (String) comboEstrategia.getSelectedItem();
            cmd.setEstrategiaEmparejamiento(estrategia);
            
            // Si la estrategia es "Por Nivel", validar y guardar parámetros adicionales
            if ("Por Nivel".equals(estrategia)) {
                if (comboNivelObjetivo.getSelectedItem() == null) {
                    throw new IllegalArgumentException("Debe seleccionar un nivel objetivo");
                }
                if (comboTipoFiltro.getSelectedItem() == null) {
                    throw new IllegalArgumentException("Debe seleccionar un tipo de filtro");
                }
                NivelJuego nivel = (NivelJuego) comboNivelObjetivo.getSelectedItem();
                cmd.setNivelJuegoObjetivo(nivel.getNombre());
                cmd.setTipoFiltroNivel((String) comboTipoFiltro.getSelectedItem());
            }

            if (partidosController.getUsuarioActual() == null) {
                throw new IllegalStateException("Debe estar autenticado para crear un partido");
            }

            if (crearPartidoController == null) {
                throw new IllegalStateException("Controlador no inicializado");
            }
            Partido partido = crearPartidoController.crearPartido(cmd);
            
            JOptionPane.showMessageDialog(this, 
                "¡Partido creado exitosamente!\n\n" +
                "ID: " + partido.getId() + "\n" +
                "Deporte: " + (partido.getDeporte() != null ? partido.getDeporte().getNombre() : "N/A") + "\n" +
                "Cupo: " + partido.getCupoRequerido() + " jugadores\n" +
                "Estado: " + partido.getEstado().tipo().getNombre() + "\n\n" +
                "📧 Las notificaciones se han enviado a los usuarios\n" +
                "   que cumplen con los criterios del partido.\n" +
                "   (Revisa la consola para ver los detalles)",
                "Partido Creado", JOptionPane.INFORMATION_MESSAGE);
            
            // Limpiar formulario
            comboDeporte.setSelectedItem(Deporte.FUTBOL);
            spinnerCupo.setValue(10);
            spinnerDuracion.setValue(30);
            comboZona.setSelectedItem(Zona.ZONA_SUR);
            LocalDateTime nuevaFecha = LocalDateTime.now().plusDays(1).withHour(18).withMinute(0);
            spinnerFechaHora.setValue(java.sql.Timestamp.valueOf(nuevaFecha));
            comboEstrategia.setSelectedIndex(0);
            actualizarCamposEstrategia();
            
            ventanaPrincipal.mostrarPanel("BUSCAR");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

