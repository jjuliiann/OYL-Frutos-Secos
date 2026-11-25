> **Nota de versión:** los resultados marcados como pendientes corresponden a las pruebas de la versión SQLite anterior. La integración EP4 con backend debe verificarse en Windows siguiendo `README.md`; no se afirma aquí que los servicios hayan compilado o ejecutado.

> Revisión actual **3.6**: consultar `MVVM_3_6.md` y logs `*_3_6.txt`. 20 tests JVM pasaron; tipos comprobados parcialmente con stubs externos. Build Android y UI pendientes. Lo siguiente es historial.

> Actualización actual **3.5**: vistas por rol, ver `ROLES_3_5.md`. Pruebas de cámara de 3.4 siguen pendientes en dispositivo.

> Revisión actual **3.4**: consultar `ESCANER_QR_3_4.md` y logs `*_3_4.txt`. Cámara y apertura externa pendientes de prueba en Android; 6 pruebas JVM QR y 18 PNG verificados.

# Verificación y pruebas pendientes

**Historial de 3.0:** los resultados siguientes se conservan de la primera entrega.
Para 3.1, consultar `ACTUALIZACION_3_1.md`. Para la organización y verificación actuales, consultar `ESTRUCTURA_3_2.md`.

## Ejecutado en este entorno

| Verificación | Resultado y límite |
| --- | --- |
| Inspección de la entrega RAR y cuatro ejemplos | Realizada; extracción separada y originales sin modificar. Se omitieron cachés, binarios build y metadatos de IDE al copiar. |
| 8 pruebas JUnit de lógica pura | Pasaron: registro por campo, números fuera de rango, cantidades/contacto, fechas, imágenes, formatos QR y contraseñas con salt. |
| Revisión sintáctica de 26 archivos Kotlin | Sin errores usando parser Kotlin 2.2.20. Esto no es compilar la app. |
| Comprobación parcial de tipos del modelo/DAO/repositorio/ViewModel | Compiló con Kotlin 2.2.20, coroutines y stubs locales mínimos de Android/Room/Lifecycle. No ejecutó Room ni generó DAO. No equivale a compilación Android. |
| 9 imágenes QR | ZXing-C++ recuperó los 7 IDs, el ID inexistente y el contenido ajeno. No se ejecutó ML Kit en Android. |
| Selección del mapa JS | Pasó clic de marcador, selección desde lista, centrado y rechazo de ID desconocido usando dobles de Leaflet. No se descargaron mosaicos en esta prueba. |
| Recursos XML | Parseados sin errores. |
| Gradle assembleDebug/testDebugUnitTest | Intentado; bloqueado antes de ejecutar tareas por conexión de Java al descargar Gradle 8.13. SDK Android no disponible. No hay APK compilado ni prueba de UI. |

Las pruebas JVM se compilaron desde los archivos reales de lógica y tests. Se proporcionaron
stubs temporales solo para las anotaciones de Room, R.drawable y el modelo CartLine requeridos
por Models/Validation; no se usó SQLite en estas 8 pruebas. El compilador y los stubs temporales
no forman parte de la aplicación entregada. Los logs están en `verificacion`.

## Pruebas Android añadidas, NO ejecutadas aquí

`LocalRepositoryTest` utiliza Room real en Android y cubre:

1. Compra: pedido y líneas, stock, carrito vacío y aviso local.
2. Compra rechazada por stock: conserva ambos productos, carrito y lista de pedidos.
3. Alias duplicado sin distinguir mayúsculas y contraseña incorrecta.
4. Cliente sin permisos administrativos y producto inactivo; puede quitarlo de su carrito.
5. Inicialización repetida sin reponer el stock ni duplicar cuentas/puntos.
6. Persistencia de perfil y cambio de estado con aviso al dueño del pedido.
7. Cierre y reapertura de un archivo SQLite conservando el stock.

`QrImagesTest`: ML Kit decodifica las 9 imágenes empaquetadas y no detecta QR en un bitmap vacío.
Ejecutar con `gradlew.bat :app:connectedDebugAndroidTest` y un Android API 26+.

## Prueba manual obligatoria antes de entregar al profesor

| Paso | Resultado esperado | Estado |
| --- | --- | --- |
| Compilar y arrancar con Spring Boot apagado | App inicia y consulta SQLite | Pendiente en Android Studio |
| Bienvenida | Logo, hero, botones, contenido accesible con scroll | Pendiente visual |
| Mantener presionado un botón y soltar | Escala .95 y vuelve a 1 con resorte; acción única | Pendiente visual |
| Navegar login/catálogo/mapa/perfil y volver | Transición de desvanecimiento y desplazamiento, navegación correcta | Pendiente visual |
| Iniciar con admin y cliente | Roles y cuentas locales correctos | Pendiente Android |
| Registrar cliente con errores y alias repetido | Errores por campo o aviso de alias ocupado | Pendiente Android |
| Agregar nueces y comprar 1 | Stock 18 → 17 en SQLite; pedido con línea de precio original | Pendiente Android |
| Intentar comprar producto con stock insuficiente/desactivado | Rechazo completo sin descuento parcial | Pendiente Android |
| Activar/desactivar/editar/crear producto y fotografía | Cliente ve solo activos; imagen persistente; fallback en productos nuevos | Pendiente Android |
| Cerrar, forzar detención y volver a abrir | Datos, sesión, perfil, avatar, pedidos y stock conservados | Pendiente Android |
| Cámara simulada: los 7 productos | ML Kit lee la imagen y muestra producto correspondiente | Pendiente ML Kit |
| QR ajeno, inexistente, imagen sin QR, cancelación | Mensaje claro, sin conservar resultado anterior como lectura nueva | Pendiente Android |
| Galería del emulador | Lee PNG importado del PC; no requiere cámara física | Pendiente Android |
| Cámara real | Abre cámara externa, cancelación y lectura nítida | Pendiente teléfono |
| Cambiar avatar por galería y cámara | Circular, persiste tras reinicio, errores manejados | Pendiente teléfono/emulador |
| Cambiar punto con lista y con marcador | Cambian resaltado, centrado y ficha nombre/dirección/horario/coordenadas | Pendiente WebView |
| Seleccionar otro punto y pulsar Cómo llegar | Google Maps recibe las coordenadas del punto seleccionado | Pendiente dispositivo con manejador de enlaces |
| Internet desactivado en mapa | Error con reintento; catálogo/carrito/gestión siguen locales | Pendiente Android |
| Notificaciones | Solicitud permiso API 33+, aviso Android y registro SQLite | Pendiente Android |
| Pantalla pequeña/teclado/rotación | Sin botones cortados; formularios desplazables; verificar estado tras recreación | Pendiente visual |

## Funciones conservadas en código

Registro/login, roles, catálogo, detalle, carrito por usuario, compra transaccional, descuento
de inventario, CRUD de productos con activación/desactivación y fotos, estados de pedidos,
QR de galería y cámara externa, avisos y permisos Android, perfil y avatar persistente, mapa
real y enlaces Google Maps. La lógica del negocio pasó del backend a `LocalRepository`.
Los datos del servidor anterior no se han importado; la base nueva es independiente por dispositivo.


- [ ] Eureka inicia en 8761 y muestra registrados Usuarios, Productos, Pedidos y API Gateway.
- [ ] El Gateway resuelve las rutas `/api/...` mediante Eureka y mantiene la respuesta esperada.
- [ ] Pedidos consulta el servicio Productos por nombre; comprobar una compra y reserva de stock.
