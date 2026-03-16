# MarketplaceOnly

Una app Android minimalista que convierte Facebook Marketplace en una experiencia dedicada, sin distracciones de feed, reels, amigos ni ninguna otra sección de Facebook.

## ¿Qué hace?

- Abre directamente Facebook Marketplace al iniciar
- Bloquea navegación a feed, friends, reels, watch, groups, messages, gaming y notificaciones
- Oculta la barra de navegación de Facebook
- Buscador integrado para buscar dentro de Marketplace
- Botón de regreso flotante al ver un artículo
- Botón atrás del teléfono funciona de forma nativa dentro del WebView

## Requisitos

- Android 6.0 (API 23) o superior
- Android SDK instalado
- JDK 17

## Compilar
```bash
./gradlew assembleDebug
```

El APK se genera en:
```
app/build/outputs/apk/debug/app-debug.apk
```

## Estructura
```
MarketplaceOnly/
├── app/
│   └── src/
│       └── main/
│           ├── java/com/marketplace/only/
│           │   └── MainActivity.kt
│           └── AndroidManifest.xml
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
└── local.properties
```

## Configuración

Asegúrate de tener `local.properties` en la raíz con la ruta a tu Android SDK:
```properties
sdk.dir=/home/tu-usuario/Android
```

Y `gradle.properties` con:
```properties
org.gradle.parallel=true
org.gradle.caching=true
android.useAndroidX=true
```

## Funcionalidades detalladas

### Bloqueo de secciones
Cualquier intento de navegar a secciones bloqueadas redirige automáticamente a Marketplace. El bloqueo opera en dos capas:
- Interceptación de URLs via `shouldOverrideUrlLoading`
- Monitor de JavaScript cada 50ms para capturar navegación interna de Facebook

### Buscador
Botón flotante azul en la esquina inferior derecha:
- Toca para abrir la barra de búsqueda
- Escribe y presiona enter o toca el botón de nuevo para buscar
- Toca el botón sin texto para cerrar

### Botón de regreso a Marketplace
Aparece en la esquina superior izquierda únicamente cuando estás viendo un artículo (`/marketplace/item/`). Al tocarlo regresa al listado principal.

## Notas

- Facebook puede mostrar una pantalla en blanco si detecta múltiples intentos de login en poco tiempo. Espera 15-30 minutos y borra los datos de la app antes de reintentar.
- El user agent está configurado como Chrome móvil para evitar redirecciones a esquemas `fb://` que el WebView no soporta.
