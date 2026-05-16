# Librario

> Aplicación Android para la gestión de bibliotecas personales, desarrollada con tecnologías modernas del ecosistema Android.

![Platform](https://img.shields.io/badge/Platform-Android-3DDC84?style=flat-square&logo=android&logoColor=white)
![Language](https://img.shields.io/badge/Language-Kotlin-B125EA?style=flat-square&logo=kotlin&logoColor=white)
![UI](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?style=flat-square&logo=jetpackcompose&logoColor=white)
![Architecture](https://img.shields.io/badge/Architecture-MVVM-orange?style=flat-square)
![Database](https://img.shields.io/badge/Database-Room-blue?style=flat-square)
![Status](https://img.shields.io/badge/Status-En%20desarrollo-yellow?style=flat-square)

---

## Descripción

**Librario** es una aplicación Android desarrollada como proyecto de práctica de desarrollo móvil moderno. Permite a los usuarios gestionar una colección personal de libros, con funcionalidades de registro manual, escaneo de códigos de barras, consulta de información bibliográfica desde APIs externas y respaldo de datos.

El proyecto aplica los principios del desarrollo Android contemporáneo: arquitectura limpia con MVVM, inyección de dependencias con Hilt, persistencia con Room, consumo de APIs REST con Retrofit y una interfaz construida íntegramente con Jetpack Compose.

---

## Funcionalidades

### Gestión de libros
- Registro manual de libros con datos personalizados.
- Carga automática de información bibliográfica mediante escaneo de código de barras o ISBN.
- Edición y eliminación de registros existentes.
- Búsqueda y filtrado de la colección local.

### Estados de lectura
- **Por leer** — libros en lista de espera.
- **Leyendo** — lectura en curso.
- **Leído** — libros finalizados.
- Lista de deseos para libros pendientes de adquisición.

### Datos y estadísticas
- Estadísticas generales sobre la biblioteca personal.
- Exportación de la colección en formato JSON.
- Importación de respaldos desde archivos JSON externos.

---

## Arquitectura

El proyecto sigue el patrón **MVVM (Model-View-ViewModel)** con separación clara de responsabilidades en capas:

```
UI (Jetpack Compose)
    │
    ▼
ViewModel
    │
    ▼
Repository
    ├── Room Database (datos locales)
    └── Remote APIs (Google Books / Open Library)
```

| Capa | Descripción |
|---|---|
| **UI** | Pantallas y componentes declarativos con Jetpack Compose |
| **ViewModel** | Estado de la UI y mediación con la capa de datos |
| **Repository** | Abstracción de fuentes de datos locales y remotas |
| **Room** | Persistencia local de la biblioteca del usuario |
| **APIs remotas** | Consulta de información bibliográfica externa |

---

## Stack tecnológico

| Categoría | Tecnología |
|---|---|
| Lenguaje | Kotlin |
| UI | Jetpack Compose |
| Arquitectura | MVVM |
| Inyección de dependencias | Dagger Hilt |
| Base de datos local | Room |
| Red | Retrofit + OkHttp |
| Concurrencia | Coroutines + Flow |
| Carga de imágenes | Coil |
| APIs externas | Google Books API, Open Library API |
| Build | Gradle Kotlin DSL |
| IDE | Android Studio |

---

## APIs integradas

### Google Books API
Fuente principal de información bibliográfica. Permite obtener título, autor, descripción, editorial, año de publicación y portada a partir de un ISBN o búsqueda por texto.

### Open Library API
Fuente alternativa utilizada cuando Google Books no devuelve resultados suficientes o al trabajar con ediciones menos comunes.

---

## Respaldo de datos

Librario permite exportar la colección en formato **JSON** para su almacenamiento externo, y restaurarla importando el mismo archivo desde el dispositivo. Esto garantiza la portabilidad de los datos entre dispositivos o reinstalaciones de la aplicación.

---

## Instalación

### Requisitos previos

- Android Studio (versión estable recomendada)
- JDK compatible con el proyecto
- Emulador Android o dispositivo físico con Android compatible
- Cámara disponible en el dispositivo para el escaneo de códigos de barras

### Pasos

1. Clonar el repositorio:

```bash
git clone https://github.com/MatiasMT93/librario-android.git
cd librario-android
```

2. Abrir el proyecto en **Android Studio**.

3. Esperar la sincronización automática de Gradle.

4. Configurar las credenciales de API si corresponde (ver sección de configuración).

5. Ejecutar la aplicación en un emulador o dispositivo físico.

---

## Configuración de APIs

> Si la aplicación requiere una API key para Google Books, agregar en `local.properties`:

```properties
GOOGLE_BOOKS_API_KEY=tu_clave_aqui
```

---

## Estructura del proyecto

```
Librario/
├── app/
│   └── src/
│       └── main/
│           ├── java/
│           │   └── com.example.librario/
│           │       ├── data/          # Room, repositorios, modelos
│           │       ├── domain/        # Casos de uso
│           │       ├── ui/            # Pantallas y componentes Compose
│           │       └── di/            # Módulos Hilt
│           └── res/
├── gradle/
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```

---

## Estado del proyecto

El proyecto se encuentra **funcional y en desarrollo activo**. Las funcionalidades principales están implementadas y en uso.

### Mejoras planificadas

- [ ] Filtros avanzados por autor, estado, categoría y editorial.
- [ ] Mejoras visuales en pantallas y componentes.
- [ ] Estadísticas de lectura más detalladas.
- [ ] Modo oscuro/claro configurable por el usuario.
- [ ] Gestión de préstamos entre usuarios.
- [ ] Sincronización en la nube.
- [ ] Exportación en formatos adicionales.
- [ ] Publicación de versión instalable.

---

## Licencia

Este proyecto fue desarrollado con fines académicos y de aprendizaje personal.
