# Lembra

App Android nativa (Kotlin) para gestionar fichas de alertas/recordatorios recurrentes: seguros, ITV, revisiones, vacunas de mascotas, etc.

> Hecha con [Claude Code](https://claude.com/claude-code) (modelo Claude Fable 5, familia Claude 5 de Anthropic).

## Funcionalidad

- Fichas con: título, categoría, fecha de inicio, cada cuánto se repite, número de repeticiones y días de aviso previo (editable).
- Notificación local automática X días antes de **cada** ocurrencia (no solo la última).
- Fichas editables y eliminables (al eliminar/editar se cancelan y reprograman los avisos).
- Categorías: Coche, Moto, Casa, Mascotas, Varios — con filtro en la lista principal.
- Las alarmas sobreviven a un reinicio del móvil.

## Instalación y actualizaciones vía F-Droid (recomendado)

La app se publica en un repositorio F-Droid propio servido con GitHub Pages
([tizzenn/fdroid](https://github.com/tizzenn/fdroid)). Para recibir
actualizaciones automáticas:

1. Abre F-Droid en el móvil → **Ajustes → Repositorios → +**
2. Añade esta URL (lleva incluida la huella de verificación):
   ```
   https://tizzenn.github.io/fdroid/repo?fingerprint=63D6489D8FC1E7D4076E52B127003666B7805D83189A4F83F4933BB72F8FB144
   ```
3. Busca **Lembra** e instálala. Las próximas versiones se actualizarán solas.

### Publicar una versión nueva

1. Sube `versionCode` (y `versionName`) en `app/build.gradle.kts`.
2. Haz commit y crea una etiqueta: `git tag v1.2 && git push origin main v1.2`.
3. El workflow **"Publicar en F-Droid"** compila el APK, lo firma con la clave
   de release (secrets del repo), actualiza el índice F-Droid y crea la
   release en GitHub con el APK adjunto.

La clave de firma y sus contraseñas están en `C:\Users\rober\lembra-claves\`
(fuera del repo). **Haz copia de seguridad de esa carpeta**: sin ella no se
pueden publicar más actualizaciones.

## Compilación de depuración

Cada push a `main` compila un APK de depuración (workflow "Compilar APK");
se descarga como artefacto `lembra-debug-apk` desde la pestaña **Actions**.

## Abrir el proyecto en Android Studio (opcional)

Si en algún momento quieres editarlo con Android Studio en vez de solo con GitHub Actions, simplemente abre la carpeta `lembra` como proyecto existente — todo el Gradle está configurado (compileSdk 34, minSdk 26).

## Notas técnicas

- Base de datos local con Room (no hay backend ni internet implicado).
- Los avisos se programan con `AlarmManager` (alarmas exactas cuando el sistema lo permite, con fallback a inexactas).
- minSdk 26 para poder usar canales de notificación de forma fiable.
