> **Nota de versión:** este archivo registra cambios de entregas anteriores del proyecto SQLite. Algunas decisiones descritas aquí quedaron reemplazadas por la integración EP4 de esta copia; para el estado actual, consulta `README.md` y `EP4_INTEGRACION.md`.

> Actualización **3.6**: modelos agrupados y lógica distribuida en ViewModels funcionales. Ver `MVVM_3_6.md` y `verificacion/archivos_3_6.txt` para archivos movidos y modificados.

> Actualización actual **3.5**: vistas por rol, ver `ROLES_3_5.md`. Pruebas de cámara de 3.4 siguen pendientes en dispositivo.

> Actualización **3.4**: escáner CameraX, enlaces de producto y mensajes QR; ver `ESCANER_QR_3_4.md` y `verificacion/archivos_3_4.txt`.

> Revisión actual 3.3: ver CONFIRMACION_COMPRA_3_3.md y los logs *_3_3 de verificacion.

> Reorganización 3.2: ver ESTRUCTURA_3_2.md y verificacion/mapa_paquetes.json.

> Actualización 3.1: consultar ACTUALIZACION_3_1.md. El informe siguiente es el historial de 3.0.

# Informe de cambios

Se trabajó desde el RAR adjunto, en otra carpeta. Los RAR originales no fueron modificados.
La nueva instrucción de usar solo SQLite reemplaza el requisito anterior de conservar Spring Boot activo.
No se incluyen backend ni Maven en esta versión; permanecen en el original.

## Archivos añadidos o modificados

| Archivo | Estado | Motivo |
| --- | --- | --- |
| `android/app/build.gradle.kts` | Modificado | Quita Retrofit, agrega animación explícita y dependencias de tests, versión 3.0. |
| `android/app/src/androidTest/java/cl/oyl/mobile/LocalRepositoryTest.kt` | Nuevo | Pruebas o recurso visual de esta entrega. |
| `android/app/src/androidTest/java/cl/oyl/mobile/QrImagesTest.kt` | Nuevo | Pruebas o recurso visual de esta entrega. |
| `android/app/src/main/AndroidManifest.xml` | Modificado | Logo como icono y HTTP plano deshabilitado. |
| `android/app/src/main/assets/leaflet/pickup-map.js` | Modificado | Eventos en marcadores, resaltado y flyTo. |
| `android/app/src/main/java/cl/oyl/mobile/AdminScreens.kt` | Modificado | Textos SQLite y controles animados. |
| `android/app/src/main/java/cl/oyl/mobile/AnimatedButtons.kt` | Nuevo | Animación de escala en presión para botones Material3. |
| `android/app/src/main/java/cl/oyl/mobile/CommerceScreens.kt` | Modificado | Textos locales y botones animados. |
| `android/app/src/main/java/cl/oyl/mobile/DeviceScreens.kt` | Modificado | Selección de mapa con ficha y cámara/avatar conservados; QR pasa a archivo propio. |
| `android/app/src/main/java/cl/oyl/mobile/LocalDb.kt` | Modificado | Nueva base independiente oyl_sqlite.db; tablas de negocio y StoreDao. |
| `android/app/src/main/java/cl/oyl/mobile/LocalRepository.kt` | Nuevo | Reglas de negocio locales, roles, seeding, contraseñas, pedidos y stock transaccionales. |
| `android/app/src/main/java/cl/oyl/mobile/MainActivity.kt` | Modificado | Transición de pantallas, progreso y estado de navegación. |
| `android/app/src/main/java/cl/oyl/mobile/Models.kt` | Modificado | Product se convierte también en entidad Room. |
| `android/app/src/main/java/cl/oyl/mobile/OylViewModel.kt` | Modificado | Orquesta repositorio local y flujos observables; elimina llamadas remotas. |
| `android/app/src/main/java/cl/oyl/mobile/PasswordHash.kt` | Nuevo | PBKDF2 con salt por cuenta. |
| `android/app/src/main/java/cl/oyl/mobile/PickupMap.kt` | Modificado | Selección JS → Compose, centrado sin reconstrucción y mapa mayor. |
| `android/app/src/main/java/cl/oyl/mobile/PublicScreens.kt` | Modificado | Bienvenida separada y cuentas locales. |
| `android/app/src/main/java/cl/oyl/mobile/QrParser.kt` | Modificado | Formatos controlados y rechazo de códigos arbitrarios. |
| `android/app/src/main/java/cl/oyl/mobile/QrScreen.kt` | Nuevo | Verificación real, cámara, galería y simulador por bitmap. |
| `android/app/src/main/java/cl/oyl/mobile/StoreDao.kt` | Nuevo | Cuentas, pedidos/líneas, puntos de retiro y DAO SQLite. |
| `android/app/src/main/java/cl/oyl/mobile/UiComponents.kt` | Modificado | Botón del carrito animado. |
| `android/app/src/main/java/cl/oyl/mobile/WelcomeScreen.kt` | Nuevo | Logo y nueva bienvenida animada. |
| `android/app/src/main/res/drawable/oyl_logo.xml` | Nuevo | Pruebas o recurso visual de esta entrega. |
| `android/app/src/main/res/drawable-nodpi/qr_almendras.png` | Nuevo | Recurso QR de prueba. |
| `android/app/src/main/res/drawable-nodpi/qr_avellanas.png` | Nuevo | Recurso QR de prueba. |
| `android/app/src/main/res/drawable-nodpi/qr_chia.png` | Nuevo | Recurso QR de prueba. |
| `android/app/src/main/res/drawable-nodpi/qr_cranberries.png` | Nuevo | Recurso QR de prueba. |
| `android/app/src/main/res/drawable-nodpi/qr_inexistente.png` | Nuevo | Recurso QR de prueba. |
| `android/app/src/main/res/drawable-nodpi/qr_invalido.png` | Nuevo | Recurso QR de prueba. |
| `android/app/src/main/res/drawable-nodpi/qr_mix_premium.png` | Nuevo | Recurso QR de prueba. |
| `android/app/src/main/res/drawable-nodpi/qr_nueces.png` | Nuevo | Recurso QR de prueba. |
| `android/app/src/main/res/drawable-nodpi/qr_pasas.png` | Nuevo | Recurso QR de prueba. |
| `android/app/src/test/java/cl/oyl/mobile/PasswordHashTest.kt` | Nuevo | Pruebas o recurso visual de esta entrega. |
| `android/app/src/test/java/cl/oyl/mobile/QrParserTest.kt` | Modificado | Pruebas o recurso visual de esta entrega. |

## Retirado del código activo

- `android/app/src/main/java/cl/oyl/mobile/Api.kt`: eliminado; ya no hay cliente Retrofit ni IP del servidor.
- Dependencias Retrofit/Gson eliminadas del Gradle de la app.
- Backend Spring Boot/H2 omitido del paquete nuevo por solicitud de usar solo SQLite.
- No se importaron datos H2 de un computador al que no se tuvo acceso.

## Recursos y conservación

Se conservan los siete PNG de productos, el almacenamiento de avatar, los formularios y pantallas existentes.
El logo es un vector nuevo de esta entrega, sin fuentes o imágenes externas. Los QR contienen texto generado
localmente y son recursos verificables. Leaflet y su licencia MIT del original se conservan; OpenStreetMap
mantiene su atribución en el mapa. SHA256_ORIGINALES.txt permite comprobar los adjuntos sin modificar.

Las pruebas ejecutadas y los límites se detallan en PRUEBAS.md. No se afirma compilación Android exitosa.
