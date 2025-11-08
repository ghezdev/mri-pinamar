# Sistema de Gestión de Encuentros Deportivos

## 📋 Descripción

Sistema orientado a objetos desarrollado en Java que gestiona encuentros deportivos (fútbol, básquet, vóley, etc.), donde los usuarios pueden crear partidos, buscar encuentros con cupos disponibles y unirse a ellos.

Este proyecto corresponde al **Trabajo Práctico Final Obligatorio (TPO)** de la materia **Proceso de Desarrollo de Software**.

## 🎯 Objetivos

- Implementar arquitectura **MVC** (Modelo-Vista-Controlador)
- Aplicar **mínimo 4 patrones de diseño** de los siguientes:
  - ✅ **State** - Ciclo de vida del partido
  - ✅ **Strategy** - Emparejamiento de jugadores
  - ✅ **Observer** - Eventos de dominio y notificaciones
  - ✅ **Adapter** + **Facade** - Envío de notificaciones
  - ✅ **Factory** - Creación de estrategias de emparejamiento

## 🧩 Patrones de Diseño Implementados

### 1. **State** - Ciclo de Vida del Partido

El ciclo de vida de un `Partido` se gestiona mediante el patrón State:

- **NecesitamosJugadoresState**: Estado inicial, permite agregar jugadores
- **ArmadoState**: Partido completo, permite confirmar
- **ConfirmadoState**: Partido confirmado, permite iniciar cuando llegue la fecha
- **EnJuegoState**: Partido en curso, permite finalizar
- **FinalizadoState**: Estado terminal
- **CanceladoState**: Estado terminal

Cada estado valida las transiciones permitidas y publica eventos de dominio.

### 2. **Strategy** - Emparejamiento de Jugadores

Estrategias para filtrar y ordenar candidatos a unirse a un partido:

- **EmparejamientoPorNivel**: Prioriza jugadores del mismo nivel
- **EmparejamientoPorCercania**: Prioriza jugadores cercanos geográficamente
- **EmparejamientoPorHistorial**: Prioriza jugadores con historial (placeholder)

La estrategia se selecciona desde la UI al crear un partido.

### 3. **Observer** - Eventos de Dominio

Sistema de eventos para notificar cambios en el estado de los partidos:

- `PartidoCreado`
- `PartidoArmado`
- `PartidoConfirmado`
- `PartidoEnJuego`
- `PartidoFinalizado`
- `PartidoCancelado`

El `NotificadorPorPreferencia` suscribe a estos eventos y envía notificaciones según la preferencia del usuario (EMAIL o PUSH).

### 4. **Adapter** + **Facade** - Notificaciones

- **Adapter**: `JavaMailEmailSender` y `FirebasePushSender` adaptan servicios externos
- **Facade**: `NotificationFacade` simplifica el envío de notificaciones, ocultando la complejidad de elegir entre Email y Push

### 5. **Factory** - Creación de Estrategias

`EmparejamientoStrategyFactory` centraliza la creación de estrategias de emparejamiento, permitiendo crear instancias por nombre.

## 🚀 Cómo Ejecutar

### Requisitos

- Java 17 o superior
- Maven 3.6+

### Compilar y Ejecutar

```bash
# Compilar el proyecto
mvn clean compile

# Ejecutar la aplicación
mvn exec:java

# Ejecutar tests
mvn test
```

### Uso de la Aplicación

1. **Registro/Login**: 
   - Complete el formulario de registro con username, email, contraseña, deporte favorito, nivel y preferencia de notificación
   - O use el botón "Login (simulado)" si ya tiene un usuario registrado

2. **Buscar Partidos**:
   - Use los filtros para buscar partidos por deporte o con cupos disponibles
   - Haga clic en "Unirme" para unirse a un partido

3. **Crear Partido**:
   - Complete el formulario con los datos del partido
   - Seleccione la estrategia de emparejamiento
   - El partido se creará en estado "Necesitamos Jugadores"

4. **Gestionar Estado**:
   - Seleccione un partido de la lista
   - Use los botones para confirmar, iniciar, finalizar o cancelar según el estado actual

## 🧪 Testing

Los tests unitarios cubren:

- **Strategy**: `EmparejamientoPorNivelTest`
- **State**: `PartidoStateTest` - Transiciones de estado
- **Observer**: `DomainEventPublisherTest` - Publicación y suscripción de eventos
- **Services**: `CrearPartidoServiceTest` - Casos de uso

Ejecutar tests:
```bash
mvn test
```

## 📊 Diagrama UML

Ver `docs/diagrama.puml` para el diagrama completo de clases en PlantUML.

## 🔧 Decisiones de Diseño

### Separación de Capas

- **Domain**: Contiene la lógica de negocio pura, sin dependencias externas
- **Application**: Orquesta los casos de uso, coordina entre dominio e infraestructura
- **UI**: Interfaz gráfica Swing, delega toda la lógica a los controllers
- **Adapters**: Implementaciones concretas de puertos (repositorios, notificaciones)

### Manejo de Estado

El patrón State elimina condicionales complejos y centraliza la lógica de transiciones en clases específicas. Cada estado conoce sus transiciones válidas.

### Eventos de Dominio

Los eventos permiten desacoplar la lógica de notificaciones del dominio. El dominio solo publica eventos, y los suscriptores reaccionan según corresponda.

### Persistencia

Se usa persistencia en memoria (`InMemoryRepository`) para simplificar. Las interfaces están listas para ser reemplazadas por implementaciones JDBC/JPA.

### Notificaciones

Los adapters simulan el envío de emails y push notifications. En producción, se reemplazarían por implementaciones reales (JavaMail, Firebase Cloud Messaging).

## 📝 Notas

- Las notificaciones se muestran en consola (simuladas)
- La ubicación geográfica usa coordenadas (lat/lng)
- El scheduler verifica cada 30 segundos si hay partidos que deben iniciar automáticamente
- El historial de partidos es un placeholder (no se persiste realmente)

## 👥 Autor

Desarrollado como TPO para la materia Proceso de Desarrollo de Software.

## 📄 Licencia

Este proyecto es parte de un trabajo académico.
