# Lembra

App Android nativa (Kotlin) para gestionar fichas de alertas/recordatorios recurrentes: seguros, ITV, revisiones, vacunas de mascotas, etc.

## Funcionalidad

- Fichas con: título, categoría, fecha de inicio, cada cuánto se repite, número de repeticiones y días de aviso previo (editable).
- Notificación local automática X días antes de **cada** ocurrencia (no solo la última).
- Fichas editables y eliminables (al eliminar/editar se cancelan y reprograman los avisos).
- Categorías: Coche, Moto, Casa, Mascotas, Varios — con filtro en la lista principal.
- Las alarmas sobreviven a un reinicio del móvil.

## Cómo subir esto a GitHub y compilar el APK automáticamente

1. Crea un repositorio nuevo en GitHub (puede ser el que ya tienes, `lembra`).
2. Sube todo el contenido de esta carpeta al repositorio:
   ```bash
   cd lembra
   git init
   git add .
   git commit -m "Primera versión de Lembra"
   git branch -M main
   git remote add origin https://github.com/TU_USUARIO/lembra.git
   git push -u origin main
   ```
3. En cuanto hagas el push, GitHub Actions compilará automáticamente el proyecto (pestaña **Actions** del repo, workflow "Compilar APK").
4. Cuando termine (unos 3-5 minutos), entra en esa ejecución y descarga el artefacto `lembra-debug-apk` — dentro está el `.apk` para instalar en tu móvil.
5. Para instalarlo: pásalo al móvil y ábrelo (puede que tengas que permitir "instalar apps de origen desconocido" la primera vez).

## Abrir el proyecto en Android Studio (opcional)

Si en algún momento quieres editarlo con Android Studio en vez de solo con GitHub Actions, simplemente abre la carpeta `lembra` como proyecto existente — todo el Gradle está configurado (compileSdk 34, minSdk 26).

## Notas técnicas

- Base de datos local con Room (no hay backend ni internet implicado).
- Los avisos se programan con `AlarmManager` (alarmas exactas cuando el sistema lo permite, con fallback a inexactas).
- minSdk 26 para poder usar canales de notificación de forma fiable.
