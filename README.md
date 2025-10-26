# Macetohuerto - Android Nativo

Aplicación de gestión de plantas urbanas desarrollada en Android nativo con Kotlin y Jetpack Compose.

## Descripción

Macetohuerto es una aplicación móvil para Android que ayuda a los usuarios a gestionar y cuidar sus plantas y pequeños huertos urbanos. Esta es la versión nativa de Android de la aplicación originalmente desarrollada en Flutter.

## Características Implementadas

### 🌱 Gestión de Plantas
- **Registro de plantas** con información detallada:
  - Nombre de la planta
  - Especie
  - Descripción
  - Frecuencia de riego (en días)
  - Necesidades de luz (Sol Completo, Sol Parcial, Sombra)
  - Notas adicionales

- **Lista de plantas** ordenada por próxima fecha de riego
- **Edición de plantas** existentes
- **Eliminación de plantas**
- **Vista detallada** de cada planta

### 💧 Sistema de Riego
- **Seguimiento de riego** con fechas de último riego y próximo riego
- **Indicador visual** cuando una planta necesita ser regada (tarjeta roja)
- **Función de riego rápido** desde la lista con botón de gota de agua
- **Cálculo automático** de próxima fecha de riego

### 🔔 Notificaciones
- **Recordatorios diarios** usando WorkManager
- **Notificaciones inteligentes** que muestran cuántas plantas necesitan riego
- **Soporte para Android 13+** con permisos de notificaciones
- **Configuración personalizada** de hora de notificación
- **Activar/desactivar notificaciones** desde la configuración

### 📸 Álbum de Fotos
- **Galería de fotos** individual para cada planta
- **Captura de fotos** con cámara integrada
- **Selección desde galería** del dispositivo
- **Descripciones opcionales** para cada foto
- **Vista previa** en detalle de planta
- **Almacenamiento local** seguro de imágenes
- **Eliminación de fotos** individuales

### 💾 Persistencia de Datos
- **Base de datos Room** para almacenamiento local robusto
- **Flow de datos reactivo** para actualizaciones en tiempo real
- **Patrón Repository** para separación de capas
- **SharedPreferences** para configuraciones de usuario
- **Almacenamiento de fotos** en directorio privado de la app

## Arquitectura

### Tecnologías Utilizadas
- **Kotlin** - Lenguaje de programación
- **Jetpack Compose** - UI moderna y declarativa
- **Room Database** - Persistencia de datos local
- **MVVM Architecture** - Patrón de arquitectura
- **Kotlin Coroutines & Flow** - Programación asíncrona
- **WorkManager** - Tareas en segundo plano y notificaciones programadas
- **Navigation Compose** - Navegación entre pantallas
- **Coil** - Carga y caché de imágenes
- **CameraX / FileProvider** - Captura y manejo de fotos
- **SharedPreferences** - Almacenamiento de configuraciones

### Estructura del Proyecto
```
app/src/main/java/com/example/pruebaandroid/
├── data/
│   ├── Plant.kt                  # Entidad de datos de planta
│   ├── PlantDao.kt               # Data Access Object para plantas
│   ├── PlantPhoto.kt             # Entidad de datos de fotos
│   ├── PlantPhotoDao.kt          # Data Access Object para fotos
│   ├── PlantDatabase.kt          # Configuración de Room
│   ├── PlantRepository.kt        # Capa de repositorio
│   ├── PlantViewModel.kt         # ViewModel con lógica de negocio
│   └── PreferencesManager.kt     # Gestor de configuraciones
├── screens/
│   ├── PlantListScreen.kt        # Pantalla principal con lista
│   ├── PlantDetailScreen.kt      # Pantalla de detalle de planta
│   ├── AddEditPlantScreen.kt     # Pantalla agregar/editar
│   ├── PlantPhotoGalleryScreen.kt # Galería de fotos
│   ├── AddPhotoScreen.kt         # Captura/selección de fotos
│   └── SettingsScreen.kt         # Configuración de la app
├── navigation/
│   └── NavGraph.kt               # Configuración de navegación
├── notifications/
│   └── WateringReminderWorker.kt # Worker para notificaciones
├── ui/theme/                     # Tema de la aplicación
└── MainActivity.kt               # Activity principal
```

## Requisitos del Sistema

- **Mínimo SDK:** Android 7.0 (API 24)
- **SDK Target:** Android 14 (API 36)
- **Lenguaje:** Kotlin
- **Gradle:** 8.x

## Instalación

1. Clona el repositorio
2. Abre el proyecto en Android Studio
3. Sincroniza Gradle
4. Ejecuta la aplicación en un emulador o dispositivo físico

## Comparación con la Versión Flutter

| Característica | Flutter | Android Nativo |
|---------------|---------|----------------|
| Persistencia | SharedPreferences | Room Database |
| UI Framework | Flutter Widgets | Jetpack Compose |
| Navegación | Navigator | Navigation Compose |
| Notificaciones | flutter_local_notifications | WorkManager + NotificationManager |
| Arquitectura | - | MVVM + Repository |
| Fotos | - | Coil + FileProvider + CameraX |
| Configuración | - | SharedPreferences + UI dedicada |

## Ventajas de la Versión Nativa

1. **Mejor integración** con el ecosistema Android
2. **Rendimiento optimizado** para Android
3. **Acceso completo** a APIs nativas de Android
4. **Room Database** más robusto que SharedPreferences
5. **Arquitectura MVVM** bien definida
6. **Jetpack Compose** - UI moderna y declarativa

## Funcionalidades Completas ✅

- ✅ Añadir soporte para fotos de plantas (Álbum completo con cámara y galería)
- ✅ Configuración de hora de notificaciones

## Próximas Mejoras Planeadas

- [ ] Gráficos de crecimiento y seguimiento
- [ ] Recordatorios adicionales (fertilización, trasplante)
- [ ] Integración con sensores ESP32 (humedad, temperatura, luz)
- [ ] Widget de home screen
- [ ] Modo oscuro personalizado
- [ ] Exportación/importación de datos (JSON/CSV)
- [ ] Búsqueda y filtros de plantas
- [ ] Estadísticas de cuidado

## Autor

Desarrollado basado en el proyecto original Flutter: [macetohuerto](https://github.com/Javisandomcoder/macetohuerto.git)

## Licencia

Este proyecto mantiene la licencia MIT del proyecto original.
