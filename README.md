# 🏠 CasaYa

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Firebase](https://img.shields.io/badge/Firebase-FFCA28?style=for-the-badge&logo=firebase&logoColor=black)
![Google Maps](https://img.shields.io/badge/Google_Maps-4285F4?style=for-the-badge&logo=googlemaps&logoColor=white)
![Clean Architecture](https://img.shields.io/badge/Clean_Architecture-9C27B0?style=for-the-badge)
![MVVM](https://img.shields.io/badge/MVVM-blue?style=for-the-badge)
![minSdk](https://img.shields.io/badge/minSdk-24-success?style=for-the-badge)

> Proyecto universitario para el curso **Desarrollo de Aplicaciones Móviles**: una app Android de búsqueda,
> publicación, gestión y contacto sobre propiedades inmobiliarias (casas, departamentos y terrenos),
> construida con **Clean Architecture + MVVM**, casos de uso, y **Firebase** como backend completo.

---

## 📖 Descripción

**CasaYa** conecta a compradores/arrendatarios con propietarios que quieren publicar sus inmuebles. Todo
usuario se registra como **Cliente** por defecto, y puede convertirse en **Propietario** desde su perfil
para desbloquear la opción de publicar. Las propiedades se guardan en **Firestore**, las fotos en
**Firebase Storage**, cada publicación incluye su ubicación exacta (Google Maps + Geocoder), y compradores
y propietarios pueden **chatear en tiempo real** sobre una propiedad puntual. La app también recibe
**notificaciones push reales** vía Firebase Cloud Messaging.

## ✨ Funcionalidades

### Cuenta y roles
- ✅ Login/registro (correo+contraseña y Google, con Credential Manager) en una sola pantalla con pestañas
- ✅ Recuperar contraseña (Firebase Auth)
- ✅ Roles: Cliente por defecto → Propietario opcional, con control de acceso real (un cliente no puede publicar)
- ✅ Editar perfil (nombre y foto), cerrar sesión

### Propiedades
- ✅ Listado con búsqueda en vivo (título/zona), filtro por tipo + rango de precio, y **paginación real** ("cargar más")
- ✅ Publicar y **editar** una propiedad (misma pantalla, con `sealed class ModoPublicar`), con selector de fotos, buscador de dirección (Geocoder) y marker arrastrable/mantener-presionado en el mapa
- ✅ Eliminar una publicación propia
- ✅ Detalle con galería, características, mapa de ubicación, favorito y compartir (`Intent.ACTION_SEND`)
- ✅ Favoritos persistidos en Firestore, sincronizados en tiempo real, con conteo visible en Perfil
- ✅ No puedes contactar/marcar como favorita tu propia propiedad

### Chat
- ✅ Conversaciones en tiempo real entre comprador y propietario, por propiedad
- ✅ Lista de conversaciones accesible desde el bottom nav ("Mensajes")
- ✅ Burbujas de mensaje diferenciadas por remitente

### Notificaciones
- ✅ Firebase Cloud Messaging: recepción de notificaciones push reales, incluso con la app cerrada
- ✅ Token guardado automáticamente al iniciar sesión

### UX general
- ✅ Splash screen con logo
- ✅ Soporte edge-to-edge en toda la app
- ✅ **Modo claro y oscuro**, con paleta propia (azul marino + verde menta) para ambos
- ✅ Estado vacío y pull-to-refresh en el listado
- ✅ Confirmaciones visuales (Toast) al publicar/editar

## 🧱 Arquitectura

Clean Architecture en 3 capas, con **casos de uso** explícitos en el dominio:

```
Presentation → Domain ← Data → (Firebase: Auth / Firestore / Storage / Cloud Messaging)
```

| Capa | Responsabilidad |
|------|------------------|
| **domain** | Modelos puros (`Propiedad`, `Usuario`, `Conversacion`, `Mensaje`, `FiltroPropiedad`), interfaces de repositorio, y **casos de uso** (`PublicarPropiedadUseCase`, `ActualizarPropiedadUseCase`) — Kotlin puro, sin Android ni Firebase |
| **data** | DTOs, Mappers (`toDomain()` / `toDto()`) y las implementaciones reales de los repositorios contra Firebase |
| **presentation** | Un paquete por pantalla, cada uno con `Fragment` + `ViewModel` + `State` + `ViewModelFactory`, todo con **ViewBinding** |

### Árbol de carpetas

```
com.microsol.casaya
├── MainActivity.kt
├── domain/
│   ├── model/          Propiedad, Usuario, Conversacion, Mensaje, TipoPropiedad, Operacion, RolUsuario, FiltroPropiedad
│   ├── repository/     PropiedadRepository, UsuarioRepository, FavoritoRepository, ChatRepository (interfaces)
│   └── usecase/        PublicarPropiedadUseCase, ActualizarPropiedadUseCase
├── data/
│   ├── model/          PropiedadDto, UsuarioDto
│   ├── mapper/          PropiedadMapper, UsuarioMapper
│   └── repository/      Firestore*RepositoryImpl (implementaciones)
└── presentation/
    ├── login/           Login/registro + recuperar contraseña (una sola pantalla con tabs)
    ├── home/             Listado + búsqueda + filtro + paginación + bottom nav
    ├── detalle/          Detalle de propiedad + mapa + compartir + contactar
    ├── publicar/         Formulario (crear/editar) + mapa interactivo + fotos
    ├── perfil/           Perfil + roles + editar perfil + cerrar sesión
    ├── favoritos/         Lista de propiedades favoritas
    ├── conversaciones/    Lista de chats del usuario
    ├── chat/              Una conversación (mensajes en tiempo real)
    ├── notificaciones/    CasaYaMessagingService (Firebase Cloud Messaging)
    └── common/            Componentes compartidos (TouchableMapWrapper)
```

### Flujo de datos (lectura reactiva)

```
Fragment ⇄ ViewModel (StateFlow) ⇄ UseCase / Repository (interfaz) ⇄ RepositoryImpl ⇄ Firestore/Storage
                                                              │
                                              Flow + addSnapshotListener:
                                          la UI se actualiza sola ante cualquier cambio
```

## 🗺️ Navegación

Un solo `Activity` (single-Activity) + `Navigation Component`:

```
loginFragment (inicio)
   └──► homeFragment ──► detalleFragment ──► chatFragment
            ├──► publicarFragment (crear o editar)
            ├──► perfilFragment ──► publicarFragment / favoritosFragment / conversacionesFragment
            ├──► favoritosFragment ──► detalleFragment / conversacionesFragment
            └──► conversacionesFragment ──► chatFragment / favoritosFragment / perfilFragment
```

## 🛠️ Stack tecnológico

Versiones exactas, tal como están en `gradle/libs.versions.toml`:

| Tecnología | Versión | Uso |
|------------|---------|-----|
| Android Gradle Plugin (AGP) | 9.3.0 | Compilación del proyecto (Kotlin va integrado) |
| Firebase BoM | 34.16.0 | Versiona Auth, Firestore, Storage y Messaging juntos |
| Google Services (plugin) | 4.5.0 | Conecta el proyecto con `google-services.json` |
| Firebase Authentication | (vía BoM) | Login/registro, recuperar contraseña |
| Firebase Firestore | (vía BoM) | Base de datos (propiedades, usuarios, favoritos, conversaciones, mensajes) |
| Firebase Storage | (vía BoM) | Fotos de propiedades y de perfil (plan Blaze) |
| Firebase Cloud Messaging | (vía BoM) | Notificaciones push reales |
| Credential Manager (`androidx.credentials`) | 1.6.0 | Login con Google, API vigente |
| googleid | 1.2.0 | Sign in with Google (idToken) |
| kotlinx-coroutines-play-services | 1.11.0 | `.await()` sobre tareas de Firebase/Google |
| Navigation Component (fragment-ktx / ui-ktx) | 2.9.8 | Navegación entre Fragments |
| Google Maps SDK for Android (play-services-maps) | 20.0.0 | Mapa interactivo, markers, cámara |
| Coil | 2.7.0 | Carga de imágenes |
| Splash Screen API (core-splashscreen) | 1.0.1 | Pantalla de carga con logo |
| SwipeRefreshLayout | 1.1.0 | Pull-to-refresh en el listado |
| Material Components | 1.10.0 | Componentes visuales, con paleta propia día/noche |
| RecyclerView | 1.4.0 | Listas (propiedades, mensajes, conversaciones) |
| ConstraintLayout | 2.1.4 | Layouts de las pantallas |
| Core KTX | 1.10.1 | Extensiones Kotlin de Android |
| Activity KTX | 1.8.0 | Extensiones Kotlin de Activity |
| AppCompat | 1.6.1 | Compatibilidad de componentes |
| Geocoder | Android nativo | Dirección ↔ coordenadas (sin dependencia externa) |

## 🔐 Configuración de Firebase (la haces tú)

1. Crea un proyecto en la [consola de Firebase](https://console.firebase.google.com) y registra una app Android con el package `com.microsol.casaya`.
2. Saca el **SHA-1** de tu firma de debug: `./gradlew signingReport`, y agrégalo en Firebase.
3. Habilita **Authentication** → Correo/contraseña y Google.
4. Habilita **Firestore Database**.
5. Habilita **Storage** (requiere plan Blaze — se mantiene gratis dentro de su cuota: 5GB almacenamiento / 100GB transferencia al mes).
6. Habilita **Cloud Messaging** (viene activo por defecto en cualquier proyecto Firebase, no requiere paso extra).
7. Descarga `google-services.json` y colócalo en `app/`.
8. Consigue una **API Key de Google Maps** (Google Cloud Console → Maps SDK for Android habilitado) y agrégala en `local.properties`:
   ```
   MAPS_API_KEY=tu_clave_aquí
   ```

## 🚀 Cómo ejecutar el proyecto

1. Clona o abre el proyecto en Android Studio.
2. Completa la configuración de Firebase (sección anterior).
3. Sincroniza Gradle (**File → Sync Project with Gradle Files**).
4. Ejecuta en un emulador/dispositivo **con Google Play Services** (necesario para Google Sign-In, Maps y Messaging), API 24+.

## 🎓 Propósito de aprendizaje

Este proyecto integra en una sola app lo trabajado por separado en distintas sesiones de clase:

- Autenticación con Firebase (`AppAutenticacionFirebase`)
- Persistencia y Clean Architecture + MVVM (`PersistenceApp`)
- Coroutines y `Flow` (`CorutineApp`)
- Google Maps SDK (`GoogleMapsStarter`)

Y suma, como extensión propia del proyecto: casos de uso explícitos en el dominio, chat en tiempo real,
notificaciones push, y soporte completo de modo oscuro.

## 🔮 Pendientes / mejoras futuras

- [ ] Reglas de seguridad definitivas en Firestore y Storage (quedaron en modo de prueba a propósito — el proyecto no tendrá más actualizaciones tras la entrega)
- [ ] Verificación de correo (`sendEmailVerification`)
- [ ] Notificación push disparada automáticamente (hoy se envía manualmente desde Firebase Console → Messaging)

## 📝 Notas

- Proyecto con fines educativos, curso Desarrollo de Aplicaciones Móviles. 📚
