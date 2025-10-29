# 🚀 GUÍA COMPLETA: SUBIR A F-DROID

## 📋 PASO 1: PREPARAR REPOSITORIO

### 1.1 Sube tu código a GitHub
```bash
git add .
git commit -m "Ready for F-Droid submission with metadata"
git push origin main  # o development, dependiendo tu branch
```

### 1.2 Verifica que sea público
- Ve a tu repositorio GitHub
- Settings → Danger Zone → Change repository visibility
- Asegúrate que sea **Public**

## 📋 PASO 2: PREPARAR METADATA F-DROID

### 2.1 Fork el repositorio de F-Droid
- Visita: https://gitlab.com/fdroid/fdroiddata
- Click: **Fork**
- Elige tu cuenta de GitLab

### 2.2 Crea el archivo metadata
1. En tu fork, crea nuevo archivo:
   ```
   metadata/com.example.pruebaandroid.yml
   ```
2. Usa el contenido de `fdroid-metadata.yml` que creé

### 2.3 Reemplaza los datos importantes:
- **Repo**: https://github.com/Javisandomcoder/macetohuerto.git ← tu repo real
- **commit**: Tag real de la versión (ej: `v1.0`, `main`, etc.)
- **applicationId**: El ID real de tu app

## 📋 PASO 3: VERIFICACIÓN ANTES DE ENVIAR

### 3.1 Ejecuta verificación local (opcional)
```bash
# Clone tu metadata repo
git clone https://gitlab.com/TU_USUARIO/fdroiddata.git
cd fdroiddata

# Verifica tu metadata
./tools/check-metadata-yaml.sh metadata/com.example.pruebaandroid.yml
```

### 3.2 Revisa manualmente:
- [ ] Repo URL es correcta y accesible
- [ ] Licencia es correcta (MIT)
- [ ] VersionCode y versionName coinciden
- [ ] Categorías son apropiadas
- [ ] AntiFeatures está vacío (si no hay)

## 📋 PASO 4: ENVIAR A F-DROID

### 4.1 Crea Pull Request
1. En tu fork de fdroiddata:
   - **New Merge Request**
   - Target: `fdroid/fdroiddata` (main branch)
   - Source: `TU_USUARIO/fdroiddata` (tu branch)
   - Title: `Add com.example.pruebaandroid`

### 4.2 Descripción del MR
```
## App Summary
- **Name**: Macetohuerto
- **Category**: Gardening/Productivity
- **License**: MIT
- **Repo**: https://github.com/Javisandomcoder/macetohuerto.git

## Features
- Plant management system
- Smart watering reminders
- Photo gallery
- Care tracking
- Home widget

## Verification
- ✅ All dependencies are FOSS
- ✅ No non-free components
- ✅ Reproducible build configured
- ✅ Proper metadata structure
- ✅ Screenshots included
```

## 📋 PASO 5: ESPERAR REVISIÓN

### Tiempos típicos:
- **Revisión inicial**: 1-4 semanas
- **Feedback**: Solicitarán cambios si es necesario
- **Aprobación**: Se añade al catálogo oficial

## 🔧 HERRAMIENTAS ÚTILES

### Verificar dependencias:
```bash
# En tu repo del app
./gradlew app:dependencies | grep -E "(com\.google|firebase|play-services)"
```

### Verificar licencias:
```bash
# Verificar que no hay licencias no-libres
find . -name "*.gradle*" -exec grep -l "nonfree\|proprietary" {} \;
```

## 📞 CONTACTO F-DROID

- **Matrix Chat**: #fdroid:matrix.org
- **Email**: team@f-droid.org
- **Issues**: https://gitlab.com/fdroid/fdroiddata/-/issues

## ⚡ CONSEJOS PROFESIONALES

1. **Sé paciente**: La revisión puede tomar tiempo
2. **Responde rápido** a los comentarios del reviewer
3. **Sigue las políticas** F-Droid estrictamente
4. **Mantén tu repo actualizado**
5. **Documenta bien** los cambios

---

## 🎯 ¿LISTO PARA EMPEZAR?

**Puedes comenzar con el Paso 1 ahora mismo!**

¿Necesitas ayuda con algún paso específico?