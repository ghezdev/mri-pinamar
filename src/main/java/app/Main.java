package app;

import repositorios.PartidoRepository;
import repositorios.UsuarioRepository;
import servicios.FirebasePushSender;
import servicios.BuscarPartidosService;
import servicios.CrearPartidoService;
import servicios.GestionEstadoPartidoService;
import servicios.RegistrarUsuarioService;
import modelos.Deporte;
import interfaces.DomainEventPublisher;
import servicios.NotificadorPorPreferencia;
import modelos.*;
import servicios.NotificationFacade;
import interfaces.EmailSender;
import interfaces.PushSender;
import controladores.PartidosController;
import vistas.VentanaPrincipal;

import javax.swing.*;
import java.time.Clock;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Clase principal de la aplicación.
 * Configura el wiring de dependencias y lanza la aplicación Swing.
 */
public class Main {
    public static void main(String[] args) {
        // Configurar Look & Feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Inicializar dependencias
        repositorios.PartidoRepository partidoRepository = repositorios.PartidoRepository.getInstance();
        repositorios.UsuarioRepository usuarioRepository = repositorios.UsuarioRepository.getInstance();
        
        // ========================================================================
        // CONFIGURACIÓN DE EMAIL SMTP
        // ========================================================================
        // Para enviar emails reales, necesitas:
        // 
        // GMAIL (Recomendado):
        //   1. Tu email de Gmail (ej: "tu-email@gmail.com")
        //   2. Contraseña de aplicación (16 caracteres) - NO tu contraseña normal
        //      Obtener en: https://myaccount.google.com/apppasswords
        //      Requiere verificación en 2 pasos activada
        //
        // OTROS PROVEEDORES:
        //   - Host SMTP (ej: "smtp-mail.outlook.com")
        //   - Puerto (587 para TLS, 465 para SSL)
        //   - Usuario/Email
        //   - Contraseña
        //   - Usar TLS (true/false)
        //
        // Ver CONFIGURACION_EMAIL.md para más detalles
        // ========================================================================
        
        // Opción 1: Modo prueba (solo imprime en consola) - por defecto
        // EmailSender emailSender = new servicios.JavaMailEmailSender();
        
        // Opción 2: Para enviar emails reales con Gmail, descomenta y configura:
        // servicios.SmtpConfig smtpConfig = servicios.SmtpConfig.gmailConfig(
        //     "pinamardeportesapp@gmail.com",                    // Tu email de Gmail
        //     "pinamar2025"                     // Contraseña de aplicación (16 caracteres)
        // );
        // EmailSender emailSender = new servicios.JavaMailEmailSender(smtpConfig);
        
        // Opción 3: Para otro servidor SMTP, descomenta y configura:
        servicios.SmtpConfig smtpConfig = servicios.SmtpConfig.customConfig(
            "smtp.gmail.com",                      // Host SMTP
            587,                                      // Puerto (587 TLS, 465 SSL)
            "pinamardeportesapp@gmail.com",                   // Usuario/Email
            "faws znnr nebu sdsi",                         // Contraseña
            true                                      // Usar TLS
        );
        EmailSender emailSender = new servicios.JavaMailEmailSender(smtpConfig);
        PushSender pushSender = new servicios.FirebasePushSender();
        NotificationFacade notificationFacade = new NotificationFacade(emailSender, pushSender);
        
        DomainEventPublisher eventPublisher = new DomainEventPublisher();
        
        // Suscribir notificador a eventos (PartidoCreado ahora se maneja en CrearPartidoService)
        NotificadorPorPreferencia notificador = new NotificadorPorPreferencia(
            notificationFacade, partidoRepository, usuarioRepository);
        eventPublisher.subscribe(PartidoArmado.class, notificador);
        eventPublisher.subscribe(PartidoConfirmado.class, notificador);
        eventPublisher.subscribe(PartidoEnJuego.class, notificador);
        eventPublisher.subscribe(PartidoFinalizado.class, notificador);
        eventPublisher.subscribe(PartidoCancelado.class, notificador);
        
        // Crear servicios de aplicación
        RegistrarUsuarioService registrarUsuarioService = new RegistrarUsuarioService(usuarioRepository);
        BuscarPartidosService buscarPartidosService = new BuscarPartidosService(partidoRepository);
        CrearPartidoService crearPartidoService = new CrearPartidoService(partidoRepository, usuarioRepository, eventPublisher, notificationFacade);
        Clock clock = Clock.systemDefaultZone();
        GestionEstadoPartidoService gestionEstadoPartidoService = new GestionEstadoPartidoService(
            partidoRepository, usuarioRepository, clock, eventPublisher);
        
        // Crear repositorio de partidos pendientes de confirmación
        repositorios.PartidoPendienteConfirmacionRepository pendientesRepository = 
            repositorios.PartidoPendienteConfirmacionRepository.getInstance();
        
        // Crear controladores
        controladores.AuthController authController = new controladores.AuthController(
            usuarioRepository, pendientesRepository);
        controladores.RegistroController registroController = new controladores.RegistroController(
            registrarUsuarioService);
        PartidosController partidosController = new PartidosController(
            buscarPartidosService, gestionEstadoPartidoService, authController);
        controladores.CrearPartidoController crearPartidoController = 
            new controladores.CrearPartidoController(crearPartidoService);
        repositorios.PartidoRepository partidoRepo = 
            repositorios.PartidoRepository.getInstance();
        controladores.GestionEstadoController gestionEstadoController = 
            new controladores.GestionEstadoController(gestionEstadoPartidoService, partidoRepo);
        
        // Configurar referencias entre controladores
        crearPartidoController.setPartidosController(partidosController);
        
        // Crear repositorio, servicio y controlador de estadísticas
        repositorios.EstadisticasPartidoRepository estadisticasRepository = 
            repositorios.EstadisticasPartidoRepository.getInstance();
        servicios.CargarEstadisticasService cargarEstadisticasService = 
            new servicios.CargarEstadisticasService(estadisticasRepository, partidoRepository);
        controladores.EstadisticasController estadisticasController = 
            new controladores.EstadisticasController(cargarEstadisticasService, partidoRepository);
        estadisticasController.setPartidosController(partidosController);
        
        // Crear y mostrar ventana principal PRIMERO (para que se levante rápido)
        SwingUtilities.invokeLater(() -> {
            VentanaPrincipal ventana = new VentanaPrincipal(
                partidosController, crearPartidoController, gestionEstadoController);
            ventana.setVisible(true);
            
            // Configurar el NotificationFacade para mostrar notificaciones push como carteles
            notificationFacade.setParentFrame(ventana);
            
            // Suscribir el suscriptor de confirmación de partidos (después de crear la ventana)
            servicios.ConfirmacionPartidoSubscriber confirmacionSubscriber = 
                new servicios.ConfirmacionPartidoSubscriber(
                    partidoRepository, pendientesRepository, partidosController, ventana);
            eventPublisher.subscribe(PartidoArmado.class, confirmacionSubscriber);
            
            // Guardar referencia al suscriptor en la ventana
            ventana.setConfirmacionSubscriber(confirmacionSubscriber);
            
            // Configurar controladores en las vistas
            ventana.setEstadisticasController(estadisticasController);
            ventana.setRegistroController(registroController);
            ventana.setAuthController(authController);
        });
        
        // Inicializar datos de ejemplo en segundo plano (para no bloquear la UI)
        new Thread(() -> {
            try {
                inicializarDatosEjemplo(usuarioRepository, partidoRepository, eventPublisher, crearPartidoService);
            } catch (Exception e) {
                System.err.println("Error al inicializar datos de ejemplo: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
        
        // Iniciar scheduler para iniciar partidos automáticamente
        iniciarScheduler(gestionEstadoPartidoService, partidoRepository);
    }
    
    /**
     * Inicializa datos de ejemplo para demostración.
     * Crea usuarios y partidos con diferentes configuraciones.
     */
    private static void inicializarDatosEjemplo(UsuarioRepository usuarioRepository,
                                               PartidoRepository partidoRepository,
                                               DomainEventPublisher eventPublisher,
                                               CrearPartidoService crearPartidoService) {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("INICIALIZANDO DATOS DE EJEMPLO");
        System.out.println("=".repeat(80) + "\n");
        
        // Usuario para recibir notificaciones de partidos principiantes
        /*
        modelos.Usuario usuarioGuillermo = crearUsuarioEjemplo(usuarioRepository, "guille", 
            "hernandez17.guillermo@gmail.com", 
            Deporte.FUTBOL, modelos.NivelJuego.AVANZADO, modelos.Zona.ZONA_ESTE);
        */
        
        // Usuarios adicionales para recibir notificaciones
        modelos.Usuario usuarioSantiago = crearUsuarioEjemplo(usuarioRepository, "santiago", 
            "santiiago.romano@gmail.com", 
            Deporte.TENIS, modelos.NivelJuego.PRINCIPIANTE, modelos.Zona.ZONA_NORTE);
        
        modelos.Usuario usuarioDiego = crearUsuarioEjemplo(usuarioRepository, "diego", 
            "diego.abel.rua@gmail.com", 
            Deporte.TENIS, modelos.NivelJuego.INTERMEDIO, modelos.Zona.ZONA_SUR);
        
        modelos.Usuario usuarioLucas = crearUsuarioEjemplo(usuarioRepository, "lucas", 
            "lstareselsky5@gmail.com", 
            Deporte.FUTBOL, modelos.NivelJuego.INTERMEDIO, modelos.Zona.ZONA_OESTE);
        
        System.out.println("✅ Usuarios creados:");
        /*
        System.out.println("  - " + usuarioGuillermo.getUsername() + " (" + usuarioGuillermo.getEmail() + 
                          ", " + usuarioGuillermo.getDeporteFavorito().getNombre() + 
                          ", " + usuarioGuillermo.getNivelJuego().getNombre() + ")");
        */
        System.out.println("  - " + usuarioSantiago.getUsername() + " (" + usuarioSantiago.getEmail() + 
                          ", " + usuarioSantiago.getDeporteFavorito().getNombre() + 
                          ", " + usuarioSantiago.getNivelJuego().getNombre() + ")");
        System.out.println("  - " + usuarioDiego.getUsername() + " (" + usuarioDiego.getEmail() + 
                          ", " + usuarioDiego.getDeporteFavorito().getNombre() + 
                          ", " + usuarioDiego.getNivelJuego().getNombre() + ")");
        System.out.println("  - " + usuarioLucas.getUsername() + " (" + usuarioLucas.getEmail() + 
                          ", " + usuarioLucas.getDeporteFavorito().getNombre() + 
                          ", " + usuarioLucas.getNivelJuego().getNombre() + ")\n");
        
        // Crear partidos de ejemplo con diferentes configuraciones
        java.time.Instant ahora = java.time.Instant.now();
        java.util.List<modelos.Partido> partidosCreados = new java.util.ArrayList<>();
        
        
        /*
        // Partido 1: Fútbol - Necesita jugadores - Estrategia Por Nivel (creado por santi)
        modelos.CrearPartidoCmd cmd1 = new modelos.CrearPartidoCmd();
        cmd1.setDeporte(Deporte.FUTBOL);
        cmd1.setCupoRequerido(2);
        cmd1.setDuracion(5);
        cmd1.setZona(modelos.Zona.ZONA_SUR);
        cmd1.setFechaHora(ahora.plusSeconds(60));
        cmd1.setEstrategiaEmparejamiento("Por Nivel");
        cmd1.setNivelJuegoObjetivo("AVANZADO");
        cmd1.setTipoFiltroNivel("MINIMO");
        modelos.Partido partido1 = crearPartidoService.create(cmd1, usuarioSantiago.getId());
        partidosCreados.add(partido1);
        
        
        // Partido 2: Básquet - Parcialmente lleno - Estrategia Por Cercanía
        modelos.CrearPartidoCmd cmd2 = new modelos.CrearPartidoCmd();
        cmd2.setDeporte(Deporte.TENIS);
        cmd2.setCupoRequerido(2);
        cmd2.setDuracion(7);
        cmd2.setZona(modelos.Zona.ZONA_NORTE);
        cmd2.setFechaHora(ahora.plusSeconds(60));
        cmd2.setEstrategiaEmparejamiento("Por Cercanía");
        modelos.Partido partido2 = crearPartidoService.create(cmd2, usuarioDiego.getId());
        partidosCreados.add(partido2);
        
        
        // Partido 3: Vóley - Armado (completo) - Estrategia Por Historial
        modelos.Partido partido3 = crearPartidoEjemplo(partidoRepository, eventPublisher, Deporte.VOLEY, 5, 90,
            -34.6035, -58.3815, ahora.plusSeconds(14400), "Por Historial");
        // Llenar el partido con diferentes usuarios (5 usuarios únicos)
        partido3.agregarJugador(usuario1);
        partido3.agregarJugador(usuario2);
        partido3.agregarJugador(usuario3);
        partido3.agregarJugador(usuario4);
        partido3.agregarJugador(usuario5);
        partidoRepository.save(partido3);
        partidosCreados.add(partido3);
        
        // Partido 4: Tenis - Confirmado - Estrategia Por Nivel
        modelos.Partido partido4 = crearPartidoEjemplo(partidoRepository, eventPublisher, Deporte.TENIS, 4, 120,
            -34.6045, -58.3825, ahora.plusSeconds(18000), "Por Nivel");
        partido4.setNivelJuegoObjetivo("AVANZADO");
        partido4.setTipoFiltroNivel("MAXIMO");
        // Llenar y confirmar
        partido4.agregarJugador(usuario4);
        partido4.agregarJugador(usuario2);
        partido4.agregarJugador(usuario5);
        partido4.agregarJugador(usuario1);
        partido4.confirmar();
        partidoRepository.save(partido4);
        partidosCreados.add(partido4);
        
        // Partido 5: Fútbol - Necesita jugadores - Estrategia Por Cercanía
        modelos.Partido partido5 = crearPartidoEjemplo(partidoRepository, eventPublisher, Deporte.FUTBOL, 12, 90,
            -34.6030, -58.3810, ahora.plusSeconds(21600), "Por Cercanía");
        partidoRepository.save(partido5);
        partidosCreados.add(partido5);
        
        // Partido 6: Básquet - Parcialmente lleno - Estrategia Por Nivel
        modelos.Partido partido6 = crearPartidoEjemplo(partidoRepository, eventPublisher, Deporte.BASQUET, 10, 60,
            -34.6042, -58.3822, ahora.plusSeconds(25200), "Por Nivel");
        partido6.setNivelJuegoObjetivo("PRINCIPIANTE");
        partido6.setTipoFiltroNivel("MAXIMO");
        partido6.agregarJugador(usuario2);
        partido6.agregarJugador(usuario4);
        partido6.agregarJugador(usuario3);
        partidoRepository.save(partido6);
        partidosCreados.add(partido6);
        
        // Partido 7: Vóley - Necesita jugadores - Estrategia Por Historial
        modelos.Partido partido7 = crearPartidoEjemplo(partidoRepository, eventPublisher, Deporte.VOLEY, 8, 90,
            -34.6038, -58.3818, ahora.plusSeconds(28800), "Por Historial");
        partidoRepository.save(partido7);
        partidosCreados.add(partido7);
        
        // Partido 8: Tenis - Necesita jugadores - Estrategia Por Cercanía
        modelos.Partido partido8 = crearPartidoEjemplo(partidoRepository, eventPublisher, Deporte.TENIS, 6, 90,
            -34.6048, -58.3830, ahora.plusSeconds(32400), "Por Cercanía");
        partidoRepository.save(partido8);
        partidosCreados.add(partido8);
        
        // Partido 9: Fútbol - Parcialmente lleno - Estrategia Por Historial
        modelos.Partido partido9 = crearPartidoEjemplo(partidoRepository, eventPublisher, Deporte.FUTBOL, 14, 90,
            -34.6025, -58.3805, ahora.plusSeconds(36000), "Por Historial");
        partido9.agregarJugador(usuario1);
        partido9.agregarJugador(usuario5);
        partido9.agregarJugador(usuario3);
        partidoRepository.save(partido9);
        partidosCreados.add(partido9);
        
        // Partido 10: Básquet - Necesita jugadores - Estrategia Por Nivel (AVANZADO, MINIMO)
        modelos.Partido partido10 = crearPartidoEjemplo(partidoRepository, eventPublisher, Deporte.BASQUET, 6, 60,
            -34.6050, -58.3835, ahora.plusSeconds(39600), "Por Nivel");
        partido10.setNivelJuegoObjetivo("AVANZADO");
        partido10.setTipoFiltroNivel("MINIMO");
        partidoRepository.save(partido10);
        partidosCreados.add(partido10);
        
        // Partido 11: Vóley - Parcialmente lleno - Estrategia Por Cercanía
        modelos.Partido partido11 = crearPartidoEjemplo(partidoRepository, eventPublisher, Deporte.VOLEY, 6, 90,
            -34.6032, -58.3812, ahora.plusSeconds(43200), "Por Cercanía");
        partido11.agregarJugador(usuario3);
        partido11.agregarJugador(usuario1);
        partidoRepository.save(partido11);
        partidosCreados.add(partido11);
        
        // Partido 12: Tenis - Necesita jugadores - Estrategia Por Nivel (INTERMEDIO, MAXIMO)
        modelos.Partido partido12 = crearPartidoEjemplo(partidoRepository, eventPublisher, Deporte.TENIS, 4, 120,
            -34.6040, -58.3825, ahora.plusSeconds(46800), "Por Nivel");
        partido12.setNivelJuegoObjetivo("INTERMEDIO");
        partido12.setTipoFiltroNivel("MAXIMO");
        partidoRepository.save(partido12);
        partidosCreados.add(partido12);
        */
        
        // Imprimir resumen de todos los partidos creados
        System.out.println("=".repeat(80));
        System.out.println("PARTIDOS CREADOS (" + partidosCreados.size() + " total)");
        System.out.println("=".repeat(80) + "\n");
        
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                .withZone(java.time.ZoneId.systemDefault());
        
        for (int i = 0; i < partidosCreados.size(); i++) {
            modelos.Partido p = partidosCreados.get(i);
            System.out.println("PARTIDO #" + (i + 1));
            System.out.println("  ID: " + p.getId().toString().substring(0, 8) + "...");
            System.out.println("  Deporte: " + (p.getDeporte() != null ? p.getDeporte().getNombre() : "N/A"));
            System.out.println("  Cupo: " + p.getJugadores().size() + "/" + p.getCupoRequerido() + " jugadores");
            System.out.println("  Estado: " + p.getEstado().tipo().getNombre());
            System.out.println("  Duración: " + p.getDuracion() + " minutos");
            System.out.println("  Zona: " + (p.getZona() != null ? p.getZona().toString() : "N/A"));
            System.out.println("  Fecha/Hora: " + formatter.format(p.getFechaHora()));
            System.out.println("  Estrategia: " + (p.getEstrategiaEmparejamiento() != null ? p.getEstrategiaEmparejamiento() : "N/A"));
            
            if ("Por Nivel".equals(p.getEstrategiaEmparejamiento())) {
                System.out.println("    - Nivel Objetivo: " + (p.getNivelJuegoObjetivo() != null ? p.getNivelJuegoObjetivo() : "N/A"));
                System.out.println("    - Tipo Filtro: " + (p.getTipoFiltroNivel() != null ? p.getTipoFiltroNivel() : "N/A"));
            }
            
            if (!p.getJugadores().isEmpty()) {
                System.out.println("  Jugadores:");
                for (modelos.Usuario jugador : p.getJugadores()) {
                    System.out.println("    - " + jugador.getUsername() + " (" + 
                                      (jugador.getNivelJuego() != null ? jugador.getNivelJuego().getNombre() : "N/A") + ")");
                }
            } else {
                System.out.println("  Jugadores: (ninguno)");
            }
            
            System.out.println();
        }
        
        System.out.println("=".repeat(80));
        System.out.println("✅ Inicialización completada: " + partidosCreados.size() + " partidos y 9 usuarios creados");
        System.out.println("=".repeat(80) + "\n");
    }
    
    /**
     * Crea un usuario de ejemplo.
     */
    private static modelos.Usuario crearUsuarioEjemplo(UsuarioRepository usuarioRepository,
                                                           String username, String email,
                                                           Deporte deporteFavorito,
                                                           modelos.NivelJuego nivel,
                                                           modelos.Zona zona) {
        modelos.Usuario usuario = new modelos.Usuario(
            java.util.UUID.randomUUID(), username, email, username);
        usuario.setDeporteFavorito(deporteFavorito);
        usuario.setNivelJuego(nivel);
        usuario.setZona(zona);
        usuario.setPreferenciaNotificacion(modelos.TipoNotificacion.EMAIL);
        return usuarioRepository.save(usuario);
    }
    
    /**
     * Crea un partido de ejemplo.
     * @param creador Usuario creador del partido (se agregará como jugador). Si es null, no se agrega ningún jugador.
     */
    private static modelos.Partido crearPartidoEjemplo(PartidoRepository partidoRepository,
                                                           DomainEventPublisher eventPublisher,
                                                           Deporte deporte, int cupo, int duracion,
                                                           modelos.Zona zona, java.time.Instant fechaHora,
                                                           String estrategia, modelos.Usuario creador) {
        modelos.Partido partido = new modelos.Partido(
            java.util.UUID.randomUUID(),
            deporte,
            cupo,
            duracion,
            zona,
            fechaHora
        );
        
        // Guardar el ID del creador si existe
        if (creador != null) {
            partido.setCreadorId(creador.getId());
        }
        // Asignar estrategia
        partido.setEstrategiaEmparejamiento(estrategia);
        partido = partidoRepository.save(partido);
        
        // Agregar el creador como jugador si se proporciona
        if (creador != null) {
            partido.agregarJugador(creador);
            partido = partidoRepository.save(partido);
        }
        
        // Publicar evento de creación (se envía en segundo plano, no bloquea)
        eventPublisher.publish(new PartidoCreado(partido.getId()));
        return partido;
    }

    /**
     * Inicia un scheduler que verifica periódicamente si hay partidos que deben iniciar o finalizar.
     */
    private static void iniciarScheduler(GestionEstadoPartidoService gestionEstadoService,
                                        PartidoRepository partidoRepository) {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        
        scheduler.scheduleAtFixedRate(() -> {
            try {
                partidoRepository.findAll().forEach(partido -> {
                    try {
                        // Verificar si debe iniciar (Confirmado -> En Juego)
                        gestionEstadoService.iniciarSiCorresponde(partido.getId());
                        
                        // Verificar si debe finalizar (En Juego -> Finalizado)
                        // También verifica si un partido confirmado debe iniciar automáticamente
                        gestionEstadoService.finalizarSiCorresponde(partido.getId());
                    } catch (Exception e) {
                        // Ignorar errores (partido puede no estar en estado correcto)
                    }
                });
            } catch (Exception e) {
                System.err.println("Error en scheduler: " + e.getMessage());
            }
        }, 0, 10, TimeUnit.SECONDS); // Ejecutar cada 10 segundos para mayor precisión
    }
}

