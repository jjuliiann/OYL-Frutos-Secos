# Informe de cambios

## Criterio de intervención

El Android original es la base. Frente al Android reconstruido, las fuentes Kotlin coinciden; la diferencia de configuración detectada estaba en app/build.gradle.kts. Se conservaron las dependencias del original, Kotlin/Compose, MVVM con StateFlow, Retrofit y Room. El backend procede del ZIP reconstruido. No se reescribió la navegación ni se introdujo una nueva arquitectura.

Se implementaron primero las tres mejoras principales y se revisaron sus recursos y código. No se hicieron cambios grandes de arquitectura porque las pruebas de aceptación de esas tres mejoras en Android todavía requieren un emulador. Los ajustes posteriores son locales: etiquetas, validaciones, fechas y persistencia H2.

## PNG

Siete fotografías generadas individualmente, PNG RGBA 512 × 512 optimizados y revisados visualmente. Mapeo exacto por nombre normalizado, compatible con IDs existentes y sin cambiar DTO. Fotos administrativas base64 mantienen prioridad; recurso neutro para productos desconocidos. Catálogo, detalle ampliado, carrito, productos e inventario comparten ProductPicture.

## Avatar

Diálogo con cámara y Photo Picker moderno. Se mantiene TakePicturePreview de la base; no se añadió permiso CAMERA innecesario. Galería se decodifica a tamaño acotado, se recorta al centro y se guarda como JPEG reducido en el mismo LocalAvatar de Room, sin persistir enlaces temporales. Visualización circular, cancelación conservadora y errores visibles. La cámara conserva la calidad de miniatura del comportamiento original; no se promete captura a resolución completa. Edición de los cuatro campos de perfil conservada con reglas visuales.

## Mapa: causa y reparación

El original cargaba el embed interactivo OSM con `javaScriptEnabled = false`; esto impide inicializar su render interactivo. Además no había indicador, manejo de errores ni liberación del WebView. La inspección HTTPS del proveedor actual mostró que su embed carga MapLibre/WebGL, lo que suma una dependencia de renderizado que puede variar por emulador. Sin ejecutar el emulador no se atribuyen todas las posibles causas del blanco a una sola condición.

Se conserva OpenStreetMap como cartografía real y se incluye Leaflet 1.9.4 JavaScript/CSS local (licencia adjunta), con mosaicos raster HTTPS, sin WebGL ni CDN en ejecución. Se dibujan los tres puntos originales entregados por /locations, se centra el seleccionado, hay atribución, estado real de mosaicos, timeout de 25 segundos, error y reintento. El WebView no expone interfaces JavaScript nativas ni acceso a archivos/contenido y se destruye al salir. User-Agent identifica OYLFrutosSecosAcademico/2.1; se mantiene la caché normal del WebView y no se descargan mapas masivamente. Cómo llegar abre Google Maps o navegador; muestra error si no existe manejador. Se eliminó la sustitución silenciosa por una lista parcial hardcodeada cuando falla /locations: se ofrece volver a cargar sin inventar puntos.

## Ajustes compatibles

Acciones de estado apiladas y etiquetas completas; filtros de categoría desplazables sin abreviar; fechas dd/MM/yyyy HH:mm, convirtiendo timestamps UTC a zona del dispositivo y conservando los valores que no pueden parsearse. Reglas puras compartidas y errores con ícono por campo en formularios; control de enteros negativos/fuera de rango. Backend H2 en archivo con ddl-auto=update, seed no destructivo y pruebas con base en memoria separada. Se agrega Avellanas al seed sólo para base vacía.

Api.kt, Models.kt, LocalDb.kt y ApiController.java se compararon byte a byte y permanecen iguales: mismos endpoints/DTO, URL de emulador, tablas Room y contratos centrales. No se sustituyeron productos, pedidos ni inventario por datos locales simulados. La notificación de demostración y la autenticación simplificada originales siguen siendo límites académicos existentes, documentados.

## Archivos Android cambiados o añadidos

| Archivo respecto de android/ | Cambio |
|---|---|
| `app/build.gradle.kts` | Modificado: Sólo sube versionCode/versionName en app; dependencias originales preservadas. |
| `app/src/main/AndroidManifest.xml` | Modificado: Etiqueta OYL Frutos Secos Mejorado; permisos y actividad preservados. |
| `app/src/main/assets/leaflet/LICENSE` | Nuevo: Mapa real: Leaflet local, licencia y lógica de marcadores/carga. |
| `app/src/main/assets/leaflet/leaflet.css` | Nuevo: Mapa real: Leaflet local, licencia y lógica de marcadores/carga. |
| `app/src/main/assets/leaflet/leaflet.js` | Nuevo: Mapa real: Leaflet local, licencia y lógica de marcadores/carga. |
| `app/src/main/assets/leaflet/pickup-map.js` | Nuevo: Mapa real: Leaflet local, licencia y lógica de marcadores/carga. |
| `app/src/main/java/cl/oyl/mobile/AdminScreens.kt` | Modificado: Fotos en listas; selección de galería, vista previa y validaciones de producto/cantidad; cámara conservada. |
| `app/src/main/java/cl/oyl/mobile/CommerceScreens.kt` | Modificado: Imagen en carrito, validación de contacto, etiquetas completas, acciones adaptables y fechas. |
| `app/src/main/java/cl/oyl/mobile/DeviceScreens.kt` | Modificado: Diálogo de avatar, Photo Picker y copia persistente, avatar circular, mapa y QR con errores claros. |
| `app/src/main/java/cl/oyl/mobile/DisplayFormats.kt` | Nuevo: Nuevo auxiliar o prueba específica; ver su código y PRUEBAS.md. |
| `app/src/main/java/cl/oyl/mobile/MainActivity.kt` | Modificado: Recarga de puntos y callback de reintento; mantiene navegación original. |
| `app/src/main/java/cl/oyl/mobile/OylViewModel.kt` | Modificado: Reutiliza reglas de validación; conserva API/DAO; informa fallo de puntos sin reemplazos inventados. |
| `app/src/main/java/cl/oyl/mobile/PhotoStorage.kt` | Nuevo: Nuevo auxiliar o prueba específica; ver su código y PRUEBAS.md. |
| `app/src/main/java/cl/oyl/mobile/PickupMap.kt` | Nuevo: Nuevo auxiliar o prueba específica; ver su código y PRUEBAS.md. |
| `app/src/main/java/cl/oyl/mobile/ProductImages.kt` | Nuevo: Nuevo auxiliar o prueba específica; ver su código y PRUEBAS.md. |
| `app/src/main/java/cl/oyl/mobile/PublicScreens.kt` | Modificado: Formularios por campo, categorías desplazables completas, foto ampliada en detalle. |
| `app/src/main/java/cl/oyl/mobile/QrParser.kt` | Nuevo: Nuevo auxiliar o prueba específica; ver su código y PRUEBAS.md. |
| `app/src/main/java/cl/oyl/mobile/UiComponents.kt` | Modificado: Recurso PNG con prioridad de foto propia, tamaño configurable, errores de campo con ícono. |
| `app/src/main/java/cl/oyl/mobile/Validation.kt` | Nuevo: Nuevo auxiliar o prueba específica; ver su código y PRUEBAS.md. |
| `app/src/main/res/drawable/producto_predeterminado.xml` | Nuevo: Nuevo auxiliar o prueba específica; ver su código y PRUEBAS.md. |
| `app/src/main/res/drawable-nodpi/almendras.png` | Nuevo: Nuevo PNG de producto, transparente y optimizado. |
| `app/src/main/res/drawable-nodpi/avellanas.png` | Nuevo: Nuevo PNG de producto, transparente y optimizado. |
| `app/src/main/res/drawable-nodpi/chia.png` | Nuevo: Nuevo PNG de producto, transparente y optimizado. |
| `app/src/main/res/drawable-nodpi/cranberries.png` | Nuevo: Nuevo PNG de producto, transparente y optimizado. |
| `app/src/main/res/drawable-nodpi/mix_premium.png` | Nuevo: Nuevo PNG de producto, transparente y optimizado. |
| `app/src/main/res/drawable-nodpi/nueces.png` | Nuevo: Nuevo PNG de producto, transparente y optimizado. |
| `app/src/main/res/drawable-nodpi/pasas.png` | Nuevo: Nuevo PNG de producto, transparente y optimizado. |
| `app/src/test/java/cl/oyl/mobile/ValidationTest.kt` | Nuevo: Nuevo auxiliar o prueba específica; ver su código y PRUEBAS.md. |
| `gradle/wrapper/gradle-wrapper.jar` | Nuevo: Wrapper oficial 8.13 faltante en el ZIP original. |
| `gradlew` | Nuevo: Wrapper oficial 8.13 faltante en el ZIP original. |
| `gradlew.bat` | Nuevo: Wrapper oficial 8.13 faltante en el ZIP original. |
| `settings.gradle.kts` | Modificado: Nombre del proyecto mejorado. |

## Archivos backend cambiados o añadidos

| Archivo | Motivo |
|---|---|
| src/main/resources/application.properties | H2 en archivo configurable por OYL_DB_URL, ddl-auto=update. |
| src/main/java/cl/oyl/api/SeedData.java | Agrega Avellanas en catálogo inicial vacío, sin reinicializar datos existentes. |
| src/test/resources/application.properties | Base de pruebas en memoria, aislada del archivo de trabajo. |
| src/test/resources/mockito-extensions/org.mockito.plugins.MockMaker | Evita auto-adjunción de agente JVM en tests; no afecta app en ejecución. |
| src/test/java/cl/oyl/api/HttpContractTest.java | Dos pruebas HTTP de JSON, compra, stock, imagen y administración. |

Además se agregan README, informe, rúbrica, matriz de pruebas, procedencia de imágenes, inventario de entradas, respaldos y evidencias. Se adjunta un arnés JVM auxiliar y un script opcional de reinicio H2 para reproducir la verificación fuera de Android. No se distribuyen SDK, cachés, credenciales personales, rutas local.properties, bases H2 de prueba ni carpetas target/build.

## Resultado y pendientes

Seis tests backend, siete tests de lógica Kotlin y una prueba de persistencia con dos arranques aprobaron. Las fuentes Kotlin pasaron análisis sintáctico oficial; el JavaScript propio pasó node --check. Las siete imágenes se revisaron visualmente. No se completó el build Android ni se probó la interacción nativa: esos límites están detallados en PRUEBAS.md. No se garantiza nota ni cobertura total de rúbrica: falta evidencia real de ejecución y colaboración de ambos estudiantes.

## Referencias técnicas consultadas

- WebView y JavaScript: https://developer.android.com/develop/ui/views/layout/webapps/webview
- Photo Picker: https://developer.android.com/training/data-storage/shared/photo-picker
- AGP 8.11 y Gradle 8.13: https://developer.android.com/build/releases/agp-8-11-0-release-notes
- Room: https://developer.android.com/jetpack/androidx/releases/room
- Kotlin/AGP: https://developer.android.com/build/kotlin-support
- Leaflet: https://leafletjs.com/examples/quick-start/ y https://leafletjs.com/reference
- Política de mosaicos OSM: https://operations.osmfoundation.org/policies/tiles/

## Revisión 2.2 tras prueba manual del usuario

Se actualizaron PickupMap.kt, assets/leaflet/pickup-map.js y app/build.gradle.kts (versión 2.2 / código 4). Se fijan tamaño y ciclo de vida del WebView y se comprueba geometría/mosaicos visibles con token de documento, esperando las respuestas JavaScript. Indicador/error se superponen al área del mapa. Se agrega docs/scripts/prueba_mapa.js y su resultado, con alcance explícitamente simulado. La causa exacta del dispositivo requiere verificación; no se afirma éxito visual sin nueva captura. API, backend, fotos y avatar no cambiaron en esta revisión.


## Cierre documental — 18/10/2026

Se actualizan README.md, PRUEBAS.md y RUBRICA.md para distinguir pruebas automáticas previas, capturas revisadas y confirmaciones del estudiante. Se añade ACEPTACION_MANUAL_2026-10-18.md y cuatro capturas en evidencias/manual-2026-10-18. Se añade LEEME_ENTREGA.md con pasos académicos pendientes. No se modifica código ni se repiten pruebas de compilación por esta edición documental.
