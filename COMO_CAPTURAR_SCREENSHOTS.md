# 📱 Cómo Capturar Screenshots para F-Droid

## ✅ ¡Buenas noticias! El APK está construido:

**APK Debug:** `app/build/outputs/apk/debug/app-debug.apk`

## 🎯 Opciones para capturar screenshots:

### Opción 1: Usar Android Studio (Recomendado)
1. **Abrir Android Studio**
2. **Abrir este proyecto** (`File > Open > C:\Users\javis\AndroidStudioProjects\PruebaAndroid`)
3. **Conectar dispositivo/emulador**
4. **Run app** (▶️ button)
5. **Tomar screenshots** usando la herramienta "Screen Capture" de Android Studio

### Opción 2: Instalar manualmente
```bash
# Instalar el APK en tu dispositivo (con adb en PATH)
adb install app/build/outputs/apk/debug/app-debug.apk

# Luego toma screenshots con Power + Vol Down
```

### Opción 3: Usar un servicio online
- Sube el APK a: `appetize.io`, `appetize.io`, `genymotion.cloud`
- Toma screenshots desde el navegador

## 📸 Screenshots necesarios:
1. **Pantalla principal** - Lista de plantas
2. **Detalle de planta** - Con toda la información
3. **Agregar/editar planta** - El formulario
4. **Galería de fotos** - Vista de fotos
5. **Configuración** - Preferencias
6. **Widget** - Si lo tienes configurado

## 📁 Cuando tengas las screenshots:
Cópielas a:
```
C:\Users\javis\AndroidStudioProjects\PruebaAndroid\fastlane\metadata\en-US\phoneScreenshots\
```

Nómbrelas: `Screenshot_01.png`, `Screenshot_02.png`, etc.

## ⚡ Alternativa rápida:
Si tienes un teléfono Android con la app instalada, toma screenshots directamente de allí y las mueves a la carpeta.

¿Prefieres que te guíe paso a paso con Android Studio?