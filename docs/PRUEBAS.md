# Verificación y pruebas de aceptación

**Actualización 18/10/2026:** pruebas manuales de la versión 2.2 completadas por el estudiante según `ACEPTACION_MANUAL_2026-10-18.md`. El mapa ya se visualiza y funciona; avatar, carrito, pedidos, stock y reinicio del backend cuentan con evidencia manual. Los resultados automáticos siguientes corresponden a la preparación previa y no se han vuelto a ejecutar para este cambio documental.

No confundir inspección del código, pruebas JVM y pruebas en dispositivo. Las pruebas automáticas aquí usaron Linux y OpenJDK 17; Windows 11/JDK 21 se documentan como entorno de ejecución solicitado y aún deben verificarse allí.

## Ejecutado durante la preparación

| Comprobación | Resultado | Alcance real |
|---|---|---|
| Backend Maven: ApiIntegrationTest | 4 pruebas aprobadas, 0 fallos/errores | Health/puntos; registro/login/perfil; compra con descuento y rechazo de sobreventa; permisos de inventario. |
| Backend Maven: HttpContractTest | 2 pruebas aprobadas, 0 fallos/errores | HTTP real en puerto aleatorio, JSON de cliente compatible; creación de pedido/stock/historial; producto con imageUri, edición/inactivación y movimiento de stock, tres puntos originales. |
| Compilación y empaquetado Spring Boot | JAR construido y ejecutado para prueba de reinicio | Fuentes Java del backend. El binario temporal de pruebas no se incluye en el ZIP; se reconstruye con Maven. |
| Persistencia H2 | Aprobada, dos arranques reales del JAR | Registro, login, producto con foto, precio, stock, pedido con líneas y estado CONFIRMADO sobreviven al reinicio en una base temporal de archivo. Script reproducible en `scripts/prueba_persistencia.py`. |
| Lógica Kotlin 2.2.20 en JVM | 7 pruebas aprobadas, 0 fallos/errores | Dos pruebas QR originales y cinco de reglas por campo, enteros fuera de rango, cantidades/contacto, fechas y mapeo de imágenes. Se compilaron archivos reales de lógica en un arnés temporal Maven, con sustitutos mínimos de R y CartLine para prescindir del SDK. No es una prueba de Room ni Compose. |
| Sintaxis Kotlin/JavaScript | Parser Kotlin oficial: 18 archivos, 0 errores; node --check de pickup-map.js sin errores | No comprueba tipos Android ni ejecución del WebView. |
| PNG | 7 archivos revisados | 512 × 512, PNG RGBA, alfa 0–255, nombres de recursos válidos y revisión visual de productos. No es captura de app. |
| Recursos de mapa | Leaflet 1.9.4 incluido y hash SRI verificado | JavaScript/CSS locales con licencia, JS propio con sintaxis válida; WebView y mosaicos comprobados posteriormente por el estudiante; véase registro manual. |
| XML y contratos | Inspección estática | XML bien formado; Api.kt, Models.kt, LocalDb.kt y ApiController.java conservados sin modificar. Sin cambios a endpoints, DTO, URL del emulador ni esquema Room. |
| Compilación Android y tests Gradle | **No completados** | El wrapper intentó descargar Gradle 8.13 y falló por acceso de red; además no hay SDK Android ni emulador instalado. No hay APK validado ni lint Android ejecutado. Ver registro de intento. |
| PNG/avatar/mapa en Android | Verificación manual posterior | Mapa, cámara, galería y persistencia confirmados por el estudiante; PNG observados parcialmente. Ver registro del 18/10/2026. |

Las pruebas del backend en memoria no alteraron datos de trabajo. Para evitar el fallo de auto-adjunción de Mockito del entorno se usa el motor `mock-maker-subclass` sólo en recursos de test; no se modifica Spring Boot en ejecución ni se ignoran pruebas.

## Ejecutar en Windows antes de la entrega

En `backend/`: `mvn test` y `mvn package`. En `android/`: `.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:lintDebug`. Después ejecutar la app. Conservar capturas de resultados y marcar los casos siguientes. La tabla siguiente conserva el protocolo completo. Consultar el registro manual para saber qué pasos ya se confirmaron; los casos con varias variantes no se consideran aprobados íntegramente por verificar sólo una.

| ID | Tipo | Pasos | Resultado esperado |
|---|---|---|---|
| P01 | Modificado: PNG | Abrir catálogo y detalle de los siete productos | Foto correcta, transparencia limpia, sin recorte incorrecto; ficha con imagen mayor. |
| P02 | Modificado: PNG | Agregar producto al carrito; entrar a productos e inventario como admin | Misma foto coherente en todas las vistas. |
| P03 | Modificado: fotografía | Crear «Producto prueba» sin foto; después añadir foto desde cámara/galería y guardar | Primero imagen neutra; después foto administrativa, que tiene prioridad; se mantiene tras reiniciar backend. |
| A01 | Modificado: avatar | Perfil → Cambiar foto → cámara; aceptar foto | Avatar circular y sin deformación; resto del perfil editable. |
| A02 | Modificado: avatar | Elegir foto desde galería (también foto vertical grande) | Selector del sistema, imagen reducida y centrada, sin permiso general de archivos. |
| A03 | Modificado: avatar | Cancelar selector y cámara | Avatar anterior intacto, sin mensajes de falso éxito. |
| A04 | Modificado: avatar | Forzar cierre; abrir; cerrar sesión y volver con el mismo usuario; probar otro usuario | Foto conservada para su dueño, separada del segundo usuario. No debe depender de que siga existiendo el archivo de galería. |
| A05 | Error | Abrir imagen dañada/no disponible; dispositivo sin cámara; API 26/27 y API 35 | Mensaje visual o cancelación segura, sin crash. API 26/27 usa decodificación reducida alternativa; API 28+ usa ImageDecoder. |
| M01 | Modificado: mapa | Con internet y backend activo, abrir mapa y seleccionar cada uno de los tres puntos | Mosaicos reales visibles y marcador correcto; nombres/coordenadas originales; fin de carga. |
| M02 | Modificado: mapa | Cortar internet; abrir mapa; esperar hasta 25 s | Indicador durante intento, error visible y Reintentar; no pantalla blanca permanente. |
| M03 | Modificado: mapa | Restaurar internet y reintentar; pulsar Cómo llegar | Mapa vuelve a cargar; dirección abre Google Maps o navegador. |
| M04 | Error | Arrancar app sin backend; abrir mapa; restaurarlo y Volver a cargar puntos | Mensaje de puntos no disponibles y recarga; no sucursales inventadas. Si ya se cargaron puntos, se conservan en memoria de la sesión. |
| R01 | Conservado | Login admin y cliente; contraseña incorrecta; registro y alias repetido | Navegación y roles correctos, error claro de validación o servidor. |
| R02 | Conservado | Catálogo, filtros, búsqueda, detalle, cantidades, carrito | Productos activos; stock y precios reales; carrito por usuario. |
| R03 | Conservado | Comprar; comparar antes/después en admin y GET /products | Pedido real, stock descontado una sola vez y carrito vaciado al éxito. |
| R04 | Conservado | Stock insuficiente o backend caído al comprar | Error, sin confirmación falsa y sin perder carrito. |
| R05 | Conservado | Crear, editar precio/stock, desactivar/reactivar producto | Se sincroniza catálogo de cliente al actualizar; no se elimina la foto. |
| R06 | Modificado visual | Gestionar pedido → Confirmado → Preparado → Entregado | Botones con texto completo, estado actualizado por servidor y persistente; fechas dd/MM/yyyy HH:mm. |
| R07 | Conservado | Entradas/salidas inventario, salida mayor a stock, cantidad cero/texto | Cantidad inválida marcada por campo; backend rechaza salida excesiva. |
| R08 | Conservado | QR desde imagen de galería y cámara; código OYL:1; ID inexistente | ML Kit encuentra producto y permite ficha/ajuste admin; error contextual si no existe. |
| R09 | Conservado | Permitir/denegar notificaciones en API 33+ y enviar demo | Permiso respetado; demo sólo notifica si está permitido; avisos locales separados por usuario. |
| R10 | Conservado | Editar nombre/correo/teléfono/dirección; reiniciar y consultar usuario | Datos guardados por API y sesión Room; validar cada campo inválido. |
| R11 | Conservado | Carrito/cachés/avisos/avatar → forzar cierre y reabrir | Room conserva datos locales; compras siguen requiriendo servidor. |
| V01 | Modificado visual | Probar ancho 320/360/411 dp, fuente normal y ampliada | Categorías y pestañas desplazables, etiquetas completas y acciones de pedido apiladas sin corte. |
| V02 | Conservado | Bienvenida, volver atrás y todas las pestañas | Navegación original funciona; animación de bienvenida fluida. |

## Límites existentes identificados

- La notificación demostrativa y los avisos locales no equivalen a push remoto de nuevos pedidos al administrador.
- El mapa usa internet del emulador y el servicio externo OSM; sin mosaicos disponibles se muestra error y se mantiene Cómo llegar.
- Los nombres canónicos completos sirven como mapeo compatible de fotos locales; si se renombra un producto, se puede cargar una foto administrativa. Productos similares no se emparejan por coincidencia parcial.
- No se migran datos ya perdidos de H2 en memoria. Al reiniciar una base desde cero deben limpiarse los datos locales Android para evitar IDs antiguos.
- No se acreditó colaboración GitHub/Trello ni ejecución con JDK 21: requieren evidencia del equipo.
