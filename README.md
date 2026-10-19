# OYL Frutos Secos

Somos Julián Martínez y Lukas Gómez, estudiantes de Ingeniería en Informática de Duoc UC. Desarrollamos este proyecto para la asignatura Desarrollo de Aplicaciones Móviles, sección 004D, con el profesor Cristian Gómez.

OYL Frutos Secos es una aplicación móvil para explorar y comprar frutos secos. La app se conecta a un backend propio para gestionar productos, inventario y pedidos. En esta versión mejoramos algunas partes de la aplicación y conservamos las funciones que ya tenía el proyecto.

## Funcionalidades

- Inicio de sesión para administrador y clientes, y registro de clientes.
- Catálogo de productos y pantalla de detalle.
- Carrito guardado localmente y confirmación de pedidos conectada al backend.
- Descuento de inventario al comprar.
- Administración de productos: crear, editar, activar, desactivar y agregar fotografías.
- Gestión de estados de pedidos.
- Lectura de códigos QR desde imágenes y notificaciones locales.
- Edición del perfil.
- Avatar circular desde cámara o galería, guardado para volver a usarlo después.
- Mapa de puntos de retiro con OpenStreetMap y opción para abrir direcciones externas.
- Imágenes PNG de almendras, nueces, mix premium, pasas, chía, cranberries y avellanas.
- Imagen predeterminada para productos sin fotografía.
- Validaciones de formularios y mejoras en la visualización de estados, pestañas y fechas.

## Tecnologías

- **Android:** Kotlin, Jetpack Compose, Material 3, ViewModel, StateFlow y navegación.
- **Datos locales:** Room sobre SQLite, para sesión, carrito, avatar, notificaciones y caché.
- **Comunicación:** Retrofit y Gson para consumir la API REST.
- **Backend:** Java, Spring Boot, Spring Data JPA y H2 en archivo.
- **Mapa:** OpenStreetMap con Leaflet dentro de un WebView.
- **QR:** Google ML Kit.

## Cómo está organizado el código

En `MainActivity.kt` definimos el tema visual y la navegación entre las pantallas. Las pantallas están repartidas en archivos como `PublicScreens.kt`, `CommerceScreens.kt` y `DeviceScreens.kt`.

`OylViewModel.kt` coordina el estado de la aplicación y las operaciones. Las pantallas observan algunos datos mediante `StateFlow`; cuando esos datos cambian, Compose actualiza la interfaz. El ViewModel se comunica con el backend usando Retrofit y con Room para los datos locales.

`Api.kt` define las solicitudes HTTP y la dirección de la API para el emulador: `http://10.0.2.2:8080/api/`. `LocalDb.kt` define las tablas y consultas de Room. `Validation.kt` contiene reglas que usamos para revisar los formularios.

Por ejemplo, al agregar un producto al carrito, el ViewModel comprueba que exista una sesión de cliente y que haya stock suficiente, y guarda la cantidad en Room. Al confirmar el pedido, envía los productos al endpoint `/api/orders`. El backend vuelve a validar la compra, descuenta el inventario y guarda el pedido. Si responde correctamente, la aplicación vacía el carrito y actualiza los datos que muestra.

En el backend, `ApiController.java` recibe las solicitudes REST. Para crear pedidos usa una transacción para que el registro del pedido y el descuento del inventario se completen juntos. Si el stock no alcanza o falla una validación, el servidor rechaza la operación.

Las imágenes de catálogo se asocian en `ProductImages.kt` con nombres de producto normalizados y usan una imagen predeterminada cuando no hay coincidencia. `PhotoStorage.kt` reduce las fotos elegidas para el avatar y las guarda localmente en Room. `PickupMap.kt` muestra OpenStreetMap con Leaflet dentro de un WebView y presenta estados de carga, error y reintento.

## Carpetas principales

```text
android/     Aplicación Android. Esta carpeta se abre en Android Studio.
backend/     API Spring Boot.
docs/        Informe de cambios, rúbrica, pruebas y evidencias.
respaldos/   Copias de los archivos originales del proyecto.
```

## Cómo ejecutar el proyecto en Windows

Necesitas Windows 11, JDK 21, Maven 3.9.x, Android Studio, Android SDK Platform 35 y un emulador Android con API 26 o superior.

### 1. Iniciar el backend

Abre una terminal en la carpeta del proyecto y ejecuta:

```powershell
cd backend
mvn test
mvn spring-boot:run
```

Deja esa terminal abierta mientras usas la aplicación. Para comprobar que el servidor inició, abre `http://localhost:8080/api/health`. Debería responder:

```json
{"status":"ok"}
```

### 2. Abrir y ejecutar Android

En Android Studio, selecciona **Open** y abre la carpeta `android`. Espera a que termine la sincronización de Gradle, inicia un emulador con conexión a internet y ejecuta la configuración `app`.

La aplicación usa `http://10.0.2.2:8080/api/` para comunicarse con el backend desde el emulador. Por eso, Spring Boot debe estar iniciado antes de probar las funciones conectadas.

Para ejecutar las pruebas Android desde PowerShell, dentro de `android/`:

```powershell
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest
.\gradlew.bat :app:lintDebug
```

## Datos y persistencia

El backend guarda usuarios, productos, stock y pedidos en `backend/data/oyl.mv.db`. La configuración está en `backend/src/main/resources/application.properties`. Ejecuta el backend desde la carpeta `backend` para que la base quede en esa ubicación.

Room guarda información propia del dispositivo, como el carrito, el avatar, la sesión y algunas cachés. El avatar se almacena localmente y no se sincroniza con el servidor. Si se desinstala la app o se borran sus datos, esa información local se elimina. Para hacer una copia de la base H2, detén Spring Boot y guarda la carpeta `backend/data`.

Usamos datos ficticios para las pruebas. Los puntos de retiro son puntos de demostración y no representan sucursales reales.

## Pruebas y documentación

Probamos manualmente en el emulador funciones como el mapa, el avatar, el carrito, las compras, la actualización de inventario, la gestión de pedidos, el QR y la administración de productos.

En `docs/` están el informe de cambios, el contraste con la rúbrica, los resultados de pruebas y capturas de algunos recorridos manuales. Revisen `docs/PRUEBAS.md` y `docs/ACEPTACION_MANUAL_2026-10-18.md` para ver el detalle y las pruebas que todavía falta realizar.

Este es un proyecto académico. La autenticación del backend está simplificada para la demostración y no está preparada para producción.
