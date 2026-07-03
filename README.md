# Lembra

**Idioma:** Español · [English](README.en.md)

App Android nativa (Kotlin) para gestionar fichas de alertas/recordatorios recurrentes: seguros, ITV, revisiones, vacunas de mascotas, etc.

> Hecha con [Claude Code](https://claude.com/claude-code) (modelo Claude Fable 5, familia Claude 5 de Anthropic).

## Funcionalidad

- Fichas con: título, categoría, fecha de inicio, hora y ubicación opcionales,
  cada cuánto se repite, número de repeticiones y días de aviso previo (editable).
- Notificación local automática X días antes de **cada** ocurrencia (no solo la última).
- Fichas editables y eliminables (al eliminar/editar se cancelan y reprograman los avisos).
- Categorías: Coche, Moto, Casa, Mascotas, Niños, Documentación, Licencias y
  Varios — con filtro en la lista principal.
- Ajustes: tema blanco/negro/sistema y colores principal y de acento elegibles
  (por defecto los corporativos: negro y rojo).
- Las alarmas sobreviven a un reinicio del móvil.
- Opcional por ficha: volcar todas las ocurrencias como eventos al calendario
  del móvil (eliges en cuál), con recordatorio propio y ubicación. Al editar o
  borrar la ficha, sus eventos se regeneran o eliminan automáticamente.

## Manual de uso

Lembra es una libreta de avisos: creas una **ficha** por cada cosa que caduca o
que hay que renovar y la app te avisa sola con la antelación que le digas. No hay
que registrarse, no necesita internet y no envía nada a ningún sitio.

### La pantalla principal («Mis alertas»)

Es la lista de todas tus fichas. Cada tarjeta muestra el título, cuándo es el
**próximo aviso** y cuántas repeticiones quedan («Quedan 3 de 12», o
«Finalizada» cuando ya han pasado todas).

- **Filtro por categoría** (fila de arriba): toca *Todas* o una categoría para
  ver solo esas fichas.
- **Botón `+`** (abajo a la derecha): crear una ficha nueva.
- **Toca una tarjeta** para editarla.
- **Menú ⋮ → Ajustes**: apariencia, orden de categorías y copia de seguridad.

### Crear o editar una ficha

Al pulsar `+` se abre el formulario, dividido en secciones. Campo por campo:

**Datos básicos**

| Campo | Para qué sirve |
|-------|----------------|
| **Título** | El nombre del aviso: «ITV del coche», «Seguro del hogar», «Vacuna del perro». Es lo único obligatorio junto con la fecha. |
| **Categoría** | Coche, Moto, Casa, Mascotas, Niños, Documentación, Licencias o Varios. Solo sirve para poner un icono y poder filtrar; elige la que más se acerque. |

**Cuándo**

| Campo | Para qué sirve |
|-------|----------------|
| **Fecha de inicio** | El día del primer vencimiento. A partir de aquí Lembra calcula todos los siguientes. |
| **Hora (opcional)** | Si te da igual la hora, déjalo en «Sin hora» y avisará por la mañana. Ponla si el aviso debe caer a una hora concreta. |
| **Ubicación (opcional)** | Un texto libre (dirección, taller, clínica). Solo es informativo. |

**Repetición**

| Campo | Para qué sirve |
|-------|----------------|
| **Repetir cada** + **Unidad** | Cada cuánto se repite: por ejemplo «cada **1** año» para la ITV, «cada **6** meses» para una revisión. La unidad puede ser Días, Semanas, Meses o Años. |
| **Número de repeticiones** | Cuántas veces en total. Pon **1** para un aviso único (una vacuna que no se repite); pon 12 para doce ocurrencias, etc. |
| **Avisar con antelación (días)** | Cuántos días antes quieres el aviso. Con «15» te avisa 15 días antes de **cada** vencimiento, no solo del primero. |

**Detalles**

| Campo | Para qué sirve |
|-------|----------------|
| **Notas (opcional)** | Cualquier apunte: número de póliza, teléfono del taller, lo que sea. |

**Calendario del sistema** (opcional) — ver el recuadro de abajo.

Cuando guardas, Lembra programa automáticamente todos los avisos. Si más tarde
editas la ficha, cancela los antiguos y los vuelve a programar; si la eliminas,
los borra. El botón **Eliminar** está dentro del formulario de edición.

> 🤓 **Dato avanzado — cómo consigue avisarte aunque no abras la app**
>
> Lembra no necesita estar abierta ni funcionando en segundo plano. Cuando
> guardas una ficha, «deja pedida» cada notificación al reloj del propio Android
> (el mismo mecanismo que usa una alarma del despertador). Por eso los avisos
> **sobreviven a apagar y encender el móvil**: al reiniciarse, Lembra vuelve a
> dejar pedidos todos los avisos pendientes.
>
> Programa **un aviso por cada repetición** (no solo el próximo): si algo se
> repite doce veces, deja las doce alarmas puestas. En móviles con ahorro de
> batería muy agresivo, el aviso podría llegar con unos minutos de retraso; para
> renovaciones y vencimientos eso da igual. Si quieres máxima puntualidad, dile
> al sistema que no optimice la batería de Lembra.

> 🤓 **Dato avanzado — volcar los avisos al calendario del móvil**
>
> En la sección *Calendario del sistema* del formulario puedes activar **«Añadir
> al calendario del móvil»**. Si lo enciendes, además del aviso propio de Lembra,
> la app crea **un evento real en tu calendario** (Google Calendar, el del
> fabricante, etc.) por cada repetición, con su propio recordatorio y la
> ubicación que hayas puesto. Eliges en qué calendario escribir con **«Calendario
> de destino»**.
>
> ¿Para qué sirve esto? Para que el aviso también te salga en el calendario que
> compartes o que ves en el ordenador, no solo como notificación en el móvil. La
> primera vez te pedirá permiso de calendario. Lo bueno: Lembra manda en esos
> eventos — si editas o borras la ficha, sus eventos del calendario **se
> regeneran o se borran solos**, sin que tengas que ir a limpiarlos a mano. Si
> lo dejas apagado, solo avisa la propia Lembra.

### Ajustes

- **Tema**: Blanco, Negro o Sistema (sigue el modo claro/oscuro del móvil).
- **Color principal** y **Color de acento**: por defecto los corporativos
  (negro y rojo); puedes elegir entre azul, verde, morado, teal, naranja o rosa.
- **Orden de categorías**: súbelas o bájalas para que aparezcan en el filtro en
  el orden que tú uses más.
- **Copia de seguridad**: *Exportar* guarda todas tus fichas en un archivo;
  *Importar* las recupera en otro móvil o tras reinstalar. Útil al cambiar de
  teléfono.

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
