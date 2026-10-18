# OYL Frutos Secos Mejorado

**Cierre de pruebas manuales 18/10/2026:** versión 2.2 del mapa probada por el estudiante. Consultar `docs/ACEPTACION_MANUAL_2026-10-18.md` para resultados y pendientes. Este paquete actualiza documentación, sin cambios al código que ya está funcionando.

Proyecto académico DSY1105, Duoc UC. Mejora incremental del Android original de los estudiantes, con el backend Spring Boot de la versión reconstruida. Integrantes: **completar nombres y apellidos de ambos estudiantes**. Equipo/sección: **completar**.

## Organización

- `android/`: abrir ESTA carpeta en Android Studio.
- `backend/`: API Spring Boot original reconstruida, con H2 persistente.
- `docs/INFORME_CAMBIOS.md`: alcance, archivos y decisiones.
- `docs/PRUEBAS.md`: resultados reales y protocolo manual pendiente.
- `docs/RUBRICA.md`: contraste con la pauta y el caso entregados.
- `docs/evidencias/`: registros de las comprobaciones ejecutadas.
- `respaldos/`: ZIP originales sin modificar y sus hashes SHA-256.

## Qué contiene

Se conservan login, registro, roles, catálogo, detalle, carrito por usuario en Room, pedidos reales por Retrofit, administración, inventario, QR con ML Kit, cámara, notificaciones y perfil. Se agregan siete PNG de producto, elección de avatar por cámara o galería con copia persistente en Room, y corrección del WebView de OpenStreetMap con carga, error y reintento. También se corrigen textos cortados, fechas y validaciones por campo. No se cambia la arquitectura general ni se reemplaza Spring Boot por simulaciones.

**Estado de entrega:** consultar `docs/PRUEBAS.md`. No se certifica compilación ni ejecución Android en este entorno. El estudiante ejecutó satisfactoriamente los recorridos descritos en el registro manual; quedan las variantes de error, logs Gradle y evidencias académicas indicadas allí.

## 1. Preparar Windows 11

1. Descomprimir en una ruta corta, por ejemplo `C:\Proyectos\OYL_Frutos_Secos_Mejorado`.
2. Instalar JDK 21 y configurar `JAVA_HOME` a su carpeta, por ejemplo `C:\Program Files\Java\jdk-21`. Agregar `%JAVA_HOME%\bin` a Path. Abrir una terminal nueva.
3. Instalar Apache Maven 3.9.x, descomprimirlo y agregar su carpeta `bin` a Path.
4. En PowerShell ejecutar `java -version` y `mvn -version`. Ambos deben mostrar el JDK seleccionado. No usar Java 8/11.
5. Instalar Android Studio compatible con Android Gradle Plugin 8.11.1. Conservar las versiones declaradas del proyecto al abrirlo.
6. En SDK Manager instalar Android SDK Platform 35, Build Tools 35.0.0, Android SDK Platform-Tools y Android Emulator.
7. En Device Manager crear un teléfono emulado con Android API 35 (imagen Google APIs). Este proyecto requiere Android 8/API 26 o superior.

El proyecto conserva el objetivo de bytecode Java/Kotlin 17; **JDK 21 puede ejecutar Gradle y Maven sin cambiar ese objetivo**. Las pruebas aquí se ejecutaron con el Java 17 disponible, no con JDK 21.

## 2. Iniciar Spring Boot

Desde PowerShell, dentro de la carpeta descomprimida:

```powershell
cd backend
mvn test
mvn spring-boot:run
```

Esperar el mensaje de servidor iniciado en puerto 8080. Mantener esa ventana abierta. Abrir `http://localhost:8080/api/health`: debe mostrar `{"status":"ok"}`. El catálogo se consulta en `http://localhost:8080/api/products` y los tres puntos ficticios originales en `/api/locations`.

Alternativa, para un JAR:

```powershell
mvn package
java -jar target\oyl-api-1.0.0.jar
```

Ejecutar siempre desde `backend/` para que la ruta relativa de H2 sea constante. No iniciar dos procesos sobre el mismo archivo de base de datos ni sobre el mismo puerto.

## 3. Abrir Android

1. Android Studio > Open > seleccionar `android/` (no la carpeta raíz que contiene también backend).
2. En Settings > Build, Execution, Deployment > Build Tools > Gradle, seleccionar el JDK 21 instalado como Gradle JDK.
3. Esperar Sync Project with Gradle Files. Se incluyen `gradlew`, `gradlew.bat`, el JAR oficial del wrapper y la configuración de Gradle 8.13; el ZIP Android de entrada no contenía los tres primeros.
4. Si se solicita SDK, seleccionar el SDK local instalado. `local.properties` lo genera Android Studio en cada PC; no se distribuye una ruta de otra máquina.
5. Encender el emulador y verificar su conexión a internet; el mapa descarga mosaicos reales de OpenStreetMap.
6. Con backend activo, ejecutar la configuración `app` con Run.
7. Para comprobar por terminal desde `android/`:

```powershell
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest
.\gradlew.bat :app:lintDebug
```

Si las descargas fallan, revisar conexión/proxy de Android Studio y acceso a Google Maven, Maven Central y la distribución oficial Gradle. No cambiar versiones al azar. El entorno donde se preparó esta entrega no disponía de SDK Android ni emulador.

## 4. Conexión y credenciales de demostración

- Android Emulator → PC anfitrión: `http://10.0.2.2:8080/api/` (se mantiene en `Api.kt`).
- Navegador de Windows → backend: `http://localhost:8080/api/`.
- Administrador sintético: `admin` / `Admin1234!`.
- Cliente: crear uno desde Registrar usando datos ficticios, contraseña de al menos ocho caracteres, correo con formato válido, teléfono y dirección de prueba.
- Un teléfono físico no usa `10.0.2.2`: requiere configurar la IP LAN del PC en `Api.kt`, una red compartida y acceso al puerto. La configuración entregada prioriza el emulador solicitado.

Se conserva la autenticación simplificada de la base académica (identificación por ID en operaciones). No se presenta como autenticación de producción. Usar únicamente datos sintéticos, conforme al caso del profesor.

## 5. Datos y persistencia

Antes: H2 `mem` + `create-drop` perdía al reiniciar usuarios registrados, cambios de perfil, productos, fotos de producto, inventario y pedidos del servidor. Los datos que ya desaparecieron de esa base no se pueden recuperar desde estos ZIP.

Ahora: `jdbc:h2:file:./data/oyl;DB_CLOSE_ON_EXIT=FALSE` y `ddl-auto=update`. Se conservan esos registros en `backend/data/oyl.mv.db`. El seed crea admin si falta y los siete productos sintéticos solamente si la tabla de productos está vacía. No sobrescribe precios, stock ni productos existentes. Avellanas es un dato de demostración nuevo; si se reutiliza una base con productos, puede crearse desde administración. Los IDs siguen siendo generados por H2.

El avatar, sesión, carrito, avisos y cachés Android siguen en Room, separados por usuario. La foto seleccionada se copia como JPEG reducido en `local_avatars`, no se guarda un enlace temporal a la galería. No requiere permiso de acceso general a fotos. Cámara conserva el contrato de miniatura existente. Borrar datos/desinstalar la app elimina Room. El avatar es local al dispositivo; no se sincroniza al servidor.

Para respaldar H2, detener Spring Boot y copiar `backend/data/`. Para comenzar desde cero, detenerlo y renombrar esa carpeta. Si se reinicia la base del servidor, limpiar también los datos de la app antes de probar para evitar IDs/cachés de usuarios antiguos.

Para una sesión efímera voluntaria (se perderán cambios al detener el proceso):

```powershell
$env:OYL_DB_URL='jdbc:h2:mem:oyl;DB_CLOSE_DELAY=-1'
mvn spring-boot:run
Remove-Item Env:\OYL_DB_URL
```

Las pruebas backend usan su propia configuración `src/test/resources/application.properties`, H2 en memoria y `create-drop`; no deben tocar la base de trabajo.

## 6. Imágenes, avatar y mapa

- PNG locales: `android/app/src/main/res/drawable-nodpi/`. Su procedencia y prompts están en `docs/IMAGENES.md`.
- El mapeo usa nombres canónicos normalizados completos, independientes de IDs y orden del seed. Una foto `imageUri` válida del administrador tiene prioridad. Un producto nuevo de nombre no reconocido usa la imagen neutra predeterminada. Renombrar un producto canónico puede requerir cargar su foto; no se adivinan nombres parciales.
- Perfil > Cambiar foto / avatar > Tomar fotografía o Elegir desde galería. Cancelar mantiene la foto anterior. Probar cerrar y abrir la app, y cambiar de usuario.
- El mapa sigue siendo OpenStreetMap real. El código anterior deshabilitaba el JavaScript que necesita su mapa interactivo. Se utiliza Leaflet 1.9.4 local con JavaScript, sin WebGL, sin CDN ni puentes JavaScript nativos, y mosaicos HTTPS de OpenStreetMap con control de carga, tiempo límite, mensajes y reintento. Se dibujan los tres puntos de demostración originales recibidos del backend; seleccionar uno centra y destaca su marcador; no son sucursales reales.
- Se conserva Cómo llegar mediante enlace de Google Maps, también utilizable en navegador. No se inventan puntos cuando falla `/locations`.

## 7. Entrega al profesor

Completar nombres, número de equipo, enlaces reales de GitHub y Trello y evidencias de ambos estudiantes. No se inventaron commits ni participación. Ejecutar la lista de pruebas manuales, adjuntar capturas/video y revisar los pendientes de la rúbrica. La pauta exige subida grupal en AVA y activación individual; respetar su fecha de corte y la prohibición de cambios posteriores indicada por el profesor.
