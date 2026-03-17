# 🛒 MarketplaceOnly

![Status](https://img.shields.io/badge/status-stable-success)
![Platform](https://img.shields.io/badge/platform-Android-green)
![Android](https://img.shields.io/badge/Android-6.0%2B-brightgreen)
![Kotlin](https://img.shields.io/badge/made%20with-Kotlin-7F52FF?logo=kotlin&logoColor=white)
![No Ads](https://img.shields.io/badge/ads-none-blue)
![No Tracking](https://img.shields.io/badge/tracking-none-blue)

**MarketplaceOnly** es una app Android minimalista que te permite usar **Facebook Marketplace** sin distracciones. Sin feed, sin reels, sin amigos — solo lo que importa: **COMPRAR**.

---

## ⬇️ Descarga

<div align="center">

[📦 Descargar APK — v1.1](https://github.com/AngelDev2343/MarketPlaceOnly/releases/download/1.1/app-debug.apk)

</div>

> **Nota:** Activa **Instalar apps de fuentes desconocidas** en Ajustes → Seguridad antes de instalar.

---

## 📱 Capturas

<table align="center">
  <tr>
    <td align="center">
      <img src="https://github.com/user-attachments/assets/ec3b255a-e7af-4938-bdc6-8b48fb647906" width="200"/>
    </td>
    <td align="center">
      <img src="https://github.com/user-attachments/assets/906e8306-2954-43ca-ab17-c5012813e199" width="200"/>
    </td>
  </tr>
  <tr>
    <td align="center">
      <img src="https://github.com/user-attachments/assets/68d99013-a429-4672-b6da-407086352e9a" width="200"/>
    </td>
    <td align="center">
      <img src="https://github.com/user-attachments/assets/72300ac9-6141-4611-a162-3d0a19cc2c15" width="200"/>
    </td>
  </tr>
</table>

---

## 🎯 Objetivo del proyecto

MarketplaceOnly nace de una necesidad concreta:

> Usar Facebook Marketplace sin que el algoritmo te arrastre al feed.

Facebook está diseñado para mantenerte dentro de su ecosistema. Esta app rompe ese ciclo — accedés directo al Marketplace, sin distracciones, sin contenido no solicitado.

---

## ✨ Funcionalidades

- 🏠 **Apertura directa** en Facebook Marketplace al iniciar la app
- 🚫 **Bloqueo automático** de feed, reels, amigos, watch, grupos, mensajes y notificaciones
- 🔄 **Redirección inteligente** — cualquier intento de salir de Marketplace vuelve al inicio
- 📱 **Botón atrás nativo** del teléfono completamente funcional
- 🚫 Sin publicidad adicional, sin tracking, sin cuentas propias.

---

## 🧠 Arquitectura general

La app está construida sobre un único bloque funcional:

### 📱 WebView controlado

- Desarrollado en **Kotlin**
- Carga directa de `facebook.com/marketplace`
- Interceptación de navegación para bloquear URLs no permitidas
- Redirección automática al Marketplace ante cualquier desvío

Todo ocurre en tu dispositivo.

Sin servidores propios.\
Sin almacenamiento externo.\
Sin procesamiento en la nube.

---

## ⚙️ Requisitos

| Requisito | Mínimo |
|---|---|
| Android | 6.0 (Marshmallow) o superior |
| Conexión | Internet requerida |
| Cuenta | Cuenta de Facebook existente |

---

## 🔐 Privacidad

- No se almacenan datos de sesión propios
- No se transmite ningún dato a servidores externos
- No hay backend propio
- No hay cuentas adicionales
- Todo el tráfico va directamente entre tu dispositivo y los servidores de Facebook

> Esta app es un WebView de Facebook Marketplace. Actúa como un navegador dedicado — nada más.

---

## 🛠️ Estado del proyecto

- **Estado:** Estable
- **Versión actual:** 1.1
- **Naturaleza:** Utilidad minimalista
- **Enfoque:** Productividad y foco

---

## ❓ FAQ

### ¿La app almacena mis credenciales de Facebook?
No. El login ocurre directamente en los servidores de Facebook a través del WebView, igual que en un navegador.

### ¿Por qué veo una pantalla en blanco al iniciar sesión?
Facebook detectó múltiples intentos de login en poco tiempo.

### ¿Funciona sin cuenta de Facebook?
No. Se necesita una cuenta de Facebook activa para usar Marketplace.

### ¿Es compatible con Facebook Marketplace de mi país?
Depende de la disponibilidad de Marketplace en tu región. La app no modifica la disponibilidad geográfica del servicio.

---

## ⚠️ Aviso legal

Esta app no está afiliada, asociada, autorizada ni respaldada por **Meta Platforms, Inc.**
Facebook y Marketplace son marcas registradas de Meta Platforms, Inc.
