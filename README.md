# CoreFx

CoreFx es un framework base diseñado para simplificar el desarrollo de aplicaciones JavaFX, proporcionando utilidades estandarizadas para la navegación, persistencia, gestión de UI y tematización.

## 🚀 Características

El proyecto se divide en varios módulos de utilidad para acelerar el desarrollo de interfaces gráficas robustas:

### 🗺️ Navegación y Flujo

- **AppContext**: Contexto central para el estado de la aplicación y datos compartidos.
- **FlowController**: Lógica para gestionar las transiciones entre diferentes vistas y flujos de trabajo.
- **StageManager**: Gestión de etapas (`Stages`) de JavaFX y operaciones de ventanas.

### 💾 Persistencia

- **EntityManagerHelper**: Utilidad para la gestión simplificada de `EntityManagers` de JPA/Hibernate.

### 🎨 Utilidades de UI

- **ThemeManager**: Control centralizado de temas y estilos CSS de la aplicación.
- **AlertUtil**: Creación y despliegue simplificado de cuadros de diálogo de alerta.
- **BindingUtils**: Ayudantes para el enlace (`binding`) de propiedades de JavaFX.
- **Format**: Utilidades para el formateo de datos y texto.
- **ImageUtil**: Ayudantes para la carga y procesamiento de imágenes.
- **Message**: Sistema para la gestión de mensajes y notificaciones del sistema.
- **TableUtils**: Utilidades para la configuración y gestión de tablas (`TableView`) de JavaFX.

### 🛠️ Utilidades Generales

- **Validator**: Conjunto de herramientas de validación para asegurar la integridad de los datos.
- **Answer**: Envoltorio (`wrapper`) estandarizado para las respuestas de los métodos de utilidad.

## 🛠️ Requisitos Técnicos

Para compilar y ejecutar CoreFx, se requiere:

- **Java**: JDK 21
- **JavaFX**: 21.0.2
- **Gestor de Dependencias**: Apache Maven

## 📂 Estructura del Proyecto

El proyecto sigue una estructura multi-módulo de Maven:

- `core/`: Módulo principal que contiene toda la lógica del framework.
  - `src/main/java/cr/ac/una/corefx/`:
    - `navigation/`: Gestión de navegación y flujo.
    - `persistence/`: Utilidades de base de datos y entidades.
    - `ui/`: Componentes de UI, temas y utilidades de vista.
    - `util/`: Clases de utilidad de propósito general.

## 🤝 Contribuciones

Si deseas contribuir a CoreFx, por favor revisa nuestra guía de contribución:
👉 [CONTRIBUTING.md](./CONTRIBUTING.md)

## 📜 Historial de Cambios

Para conocer las últimas actualizaciones y versiones, consulta el registro de cambios:
👉 [CHANGELOG.md](./CHANGELOG.md)

---

Desarrollado por **cr.ac.una**
