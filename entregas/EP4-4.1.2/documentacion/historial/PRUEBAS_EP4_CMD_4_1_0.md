# Pruebas de la entrega EP4

Se usó una copia separada del proyecto y una base de evaluación sin datos de los estudiantes. Se compilaron el backend con Java 21 y Maven 3.9.9, y Android con Gradle 8.13 y SDK 35. Se instaló y ejecutó la app en un emulador Android 11 (API 30). Las cuentas, pedidos y productos transitorios creados durante las verificaciones no están en el ZIP.

## Qué se pudo demostrar

| Prueba ejecutada | Resultado observado | Evidencia |
|---|---|---|
| Compilación y pruebas automatizadas Spring | Los siete módulos del proyecto Maven terminaron en SUCCESS. 25 pruebas pasaron, sin errores ni omitidas. Se ejercitaron reglas, controladores, roles, tokens y rutas de Eureka. | `evidencias/maven.txt`, `evidencias/pruebas-unitarias.tsv` |
| Compilación y pruebas JVM Android | Gradle generó las APK y 26 pruebas de lógica pasaron. | `evidencias/gradle.txt`, `evidencias/unitarias-android/index.html` |
| Usuarios, Productos y Pedidos por el Gateway | Cuatro instancias registradas UP: tres servicios y el Gateway. Se abrió cada uno de los cuatro enlaces de estado anunciados; todos respondieron. La dirección /eureka/ del navegador lleva al panel. | `evidencias/enlaces-eureka.txt`, `evidencias/estado-powershell.txt` |
| Flujo completo por HTTP | Registro, login, perfil, creación y edición de producto, ajuste de stock, rechazo por falta de stock, baja lógica, permisos, historial y cambio de estado. Una compra de dos unidades llevó el inventario de 7 a 5. | `evidencias/api-compra.txt`, `backend/scripts/comprobar_api.py` |
| Suite completa en dispositivo (API 30) | **14 pruebas aprobadas** en Android. Cubre las migraciones, persistencia Room, retiro y envío, roles, compra por Gateway y decodificación real con ML Kit de nueve imágenes QR y una imagen en blanco. | `evidencias/dispositivo.txt` |
| **Compra desde el emulador** | Retrofit se conectó a `10.0.2.2:8080`. Un cliente registrado solo en Spring agregó un producto al carrito Room, compró con retiro por $1.250 y vio el pedido. El carrito quedó vacío y el stock bajó **de 3 a 2**. El administrador confirmó el pedido; el cliente recibió HTTP 403 al intentar administrar. | `evidencias/compra-desde-android.txt`, `GatewayEmulatorTest.kt` |
| APK de aula, desde la pantalla | APK firmada instalada en el emulador. «Probar conexión» confirmó Usuarios, Productos y Pedidos. Se inició sesión por la interfaz. Un enlace QR externo al producto 1 abrió Almendras y un ID ausente mostró «Producto inexistente». | `evidencias/apk-pantallas.txt`, captura `evidencias/apk-qr-almendras.png`, `evidencias/firma-apk.txt` |
| Cierre y reinicio del backend | Al reiniciar los procesos con las mismas bases H2, los pedidos 1 y 2 seguían CONFIRMADOS, con sus unidades y totales. Los dos productos conservaron stocks de 5 y 2 y siguieron desactivados. | `evidencias/reinicio-h2.txt` |
| Mapa y migración | El test del JavaScript seleccionó otro marcador, movió la vista y rechazó un ID inválido. Se ejecutaron las dos migraciones contra SQLite comprobando la conservación de datos y las nuevas columnas. | `evidencias/mapa-y-sqlite.txt` |
| Detección de Java del lanzador | Reproducido el falso error del lanzador antiguo con OpenJDK 21.0.13. Siete escenarios aprobados para la detección corregida. La prueba se hizo con PowerShell en Linux y rutas dispuestas como en Windows; el arranque completo en Windows se verifica en el PC. | `evidencias/java-cmd.txt` |
| Arranque y cierre manual de esta edición | Se inició cada JVM solo a petición. La consola mostró los cinco registros de Spring; el Gateway validó cuatro instancias y sus rutas. Compra real de 2 unidades, stock 7 -> 5. Al detenerlos, los cinco puertos quedaron libres y se leyó de nuevo el pedido confirmado en H2. Android y los cinco JAR no cambiaron: las pruebas Android de esta tabla corresponden a esos mismos binarios. | `evidencias/arranque-manual.txt` |
| Docker y lanzador | Compose validó la configuración de los cinco contenedores. La comprobación de salud de PowerShell se ejecutó contra los cinco procesos Java. | `evidencias/docker-compose.txt`, `evidencias/estado-powershell.txt` |

Se provocó un reinicio del registro durante una ejecución y se observó un HTTP 503 mientras los servicios volvían a registrarse. Con el catálogo, las tres rutas y el registro listos, la compra desde el emulador volvió a pasar. Por eso el comando de estado espera respuestas reales antes de mostrar que está listo.

## Cobertura

El informe de líneas de JaCoCo del backend incluye los módulos con código y dio **285 de 326 (87,42 %)**. En Android, el informe de las pruebas JVM del módulo completo, con UI y código generado por Room, dio **110 de 3485 (3,16 %)**. El recorrido por HTTP y las pruebas en dispositivo son evidencias separadas; no se usaron para inflar la cobertura. El objetivo del 80 % está cubierto en backend y pendiente si se exige también a Android.

Abre `evidencias/cobertura-backend/index.html` o `evidencias/cobertura-android/index.html` para revisar cada clase.

## Repetir en Windows

1. En CMD, desde `backend`: `mvn clean verify`. Para recompilar y además renovar los JAR del lanzador usa `compilar-backend.bat`.
2. Desde CMD, en `backend`, inicia manualmente cada servicio como indica `ABRIR_BACKEND_CMD.txt`. Espera a que `oyl.bat estado` indique BACKEND LISTO. En esa misma CMD, ejecuta `python scripts\comprobar_api.py`. Esto solo consulta. Para repetir el flujo de compra usa `python scripts\comprobar_api.py --compra` en una **base de evaluación**: registra un cliente, modifica inventario y crea un pedido.
3. Desde `android`: `gradlew.bat :app:testDebugUnitTest :app:createDebugUnitTestCoverageReport`.
4. Arranca un único emulador Android; verifica que usa la dirección 10.0.2.2 en la variante debug. Con backend de evaluación y cuentas iniciales disponibles ejecuta `gradlew.bat :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.oylLive=true`. La opción habilita una prueba que realiza una compra real. Sin ella se omite ese único recorrido de red.
5. Para mostrarlo sin herramientas de pruebas abre la APK de aula: administra un producto, anota su stock; entra como cliente y compra una unidad; vuelve como administrador y compara stock y estado del pedido.

## Qué falta probar en el entorno de la defensa

- **Windows 11 del Duoc y Docker Desktop:** no se ejecutó el BAT en un Windows nativo ni se arrancó el motor Docker aquí. En ese PC corre el lanzador o Docker (solo uno a la vez) y comprueba los enlaces. El backend por JAR sí se ejecutó realmente.
- **Samsung A07:** alcance de la IP del PC en su Wi-Fi, cámara física en vivo, permiso de notificaciones, cámara/galería de avatar y persistencia tras cerrar la app. Los QR del proyecto usan el esquema `oyl://`: se probó que Android abre los enlaces válidos. La decisión de lanzar ese esquema al leer una imagen con la cámara de Samsung depende del lector; pruébalo también con el escáner integrado de OYL. Revisa las instrucciones del teléfono en el README.
- **Cartografía y proveedores externos:** en el emulador o teléfono de la presentación, toca por lo menos dos puntos, comprueba sus detalles, mosaicos de OpenStreetMap y el botón «Cómo llegar» de Google Maps. La prueba de marcadores no mide si los servidores externos responden en esa red. Lo mismo aplica a la búsqueda nutricional.
- **Revisión visual:** recorre envío a domicilio con campos incompletos, retiro en tienda, perfil, pedidos y transiciones, tanto como cliente como administrador. Los resultados de las pruebas de datos no sustituyen una revisión visual de cada pantalla, tamaño de letra y permiso.
