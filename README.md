[README(1).md](https://github.com/user-attachments/files/35413568/README.1.md)
# OYL Frutos Secos | Entrega EP4
Julián Martínez y Lukas Gómez · Desarrollo de Aplicaciones Móviles · 004D · Profesor Cristian Gómez · Duoc UC.

Desarrollamos una tienda Android en la que los clientes consultan el catálogo, escanean el QR de un producto y compran con retiro o despacho. El administrador gestiona productos, existencias y estados de pedidos. El código móvil está en `android/` y los microservicios en `backend/`.

**Tecnologías:** Kotlin, Jetpack Compose, Material 3, MVVM y StateFlow para Android; Room sobre SQLite para el carrito y la caché; Retrofit con OkHttp y Gson para comunicarnos por HTTP/JSON. En el servidor usamos Java 21, Spring Boot, Spring Data JPA, H2, Spring Cloud Gateway y Eureka. Podemos levantar el backend por CMD o con Docker Compose. El lector QR usa CameraX y ML Kit; el mapa integrado usa OpenStreetMap y «Cómo llegar» abre Google Maps.

## Compilar la app 4.1.2 en el PC

La bienvenida y el inicio de sesión no muestran el acceso a la configuración del servidor. La edición de aula permite cambiar la dirección con una pulsación larga sobre **OYL FRUTOS SECOS** en la barra verde superior. Retrofit conserva la dirección guardada y el backend sigue siendo el mismo.

Esta entrega contiene código Android para que el equipo genere y firme su propia APK. Sigue [Abrir en Android Studio](ABRIR_EN_ANDROID_STUDIO.md). **Si el backend ya está activo, déjalo en su carpeta actual:** no necesitas iniciar otra copia ni mover las bases de datos.

## 1. Iniciar el backend en una sola CMD (Windows 11)

La guía corta está en [ABRIR_BACKEND_CMD.txt](ABRIR_BACKEND_CMD.txt).

1. Extrae todo este ZIP en una carpeta nueva. Si ya tienes el backend en marcha, sigue utilizando esa copia. Estos pasos de arranque son para cuando los servicios están apagados.
2. Entra en **OYL_Frutos_Secos -> backend**. Donde ves `oyl.bat` y `pom.xml`, escribe **cmd** en la barra de direcciones del Explorador y presiona Enter. Java Temurin 21.0.12.1 es compatible. No necesitas compilar ni instalar Maven para esta demostración.
3. En esa misma CMD ejecuta **una línea por vez** y espera el OK. Verás el comando de Java, el PID, el puerto y los mensajes de Spring de cada proceso:

```cmd
oyl.bat eureka
oyl.bat usuarios
oyl.bat productos
oyl.bat pedidos
oyl.bat gateway
oyl.bat estado
```

4. Cuando aparezca **BACKEND LISTO**, muestra el catálogo desde CMD: `curl.exe http://localhost:8080/api/products`. Abre **tú** http://localhost:8761/ para ver los cuatro clientes registrados. El gateway está en 8080 y Eureka en 8761.
5. En Android Studio abre `android`. Sigue la guía de firma anterior y selecciona la variante **aula**. En una instalación de emulador nueva la dirección predeterminada ya es `http://10.0.2.2:8080/api/`. Abre el catálogo para comprobarlo.
6. Cuentas iniciales de una base nueva: **cliente / Cliente1234!** y **admin / Admin1234!**. La APK de aula usa el paquete `cl.oyl.mobile.aula`.
7. Al terminar, antes de cerrar la consola, escribe **oyl.bat detener**. Las bases H2 siguen en `backend/data/`. Para mostrar la ayuda sin iniciar nada usa `oyl.bat`.

## 2. Configurar el Samsung A07 por primera vez
Instala la APK ya firmada en el teléfono. Conecta ambos equipos a la misma red. En CMD del PC ejecuta `ipconfig`, copia la IPv4 del adaptador conectado y abre en el navegador del teléfono `http://IP_DEL_PC:8080/api/products`. Sustituye IP_DEL_PC por el número, sin letras ni corchetes. Si ves JSON, mantén pulsado **OYL FRUTOS SECOS** en la barra verde superior de la variante **aula** y escribe `http://IP_DEL_PC:8080/api/`; prueba y guarda. En una red privada puede ser necesario permitir el puerto TCP 8080 en el firewall para esa red. Una Wi-Fi institucional puede aislar los dispositivos: en ese caso usa el emulador o una red privada que permita la comunicación entre ambos. Si cambia la IP, repite la selección. El backend debe seguir activo. La sesión y la caché se separan por dirección de servidor; cambiar de dirección puede requerir volver a iniciar sesión.

## 3. Abrir y compilar el código
- **Android:** en Android Studio selecciona File -> Open -> `android` (contiene `settings.gradle.kts`). Configura el SDK Android 35, Build Tools 35.0.0 y Gradle JDK 21. Espera la sincronización y ejecuta `app` en el emulador. En CMD, desde `android`: `gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:createDebugUnitTestCoverageReport`. La primera compilación necesita acceso a los repositorios de Google y Maven.
- **Backend:** desde `backend`, usa JDK 21 y Maven 3.9: `mvn --version`. Después de editar el servidor, detén los servicios, ejecuta `compilar-backend.bat` y arranca cada módulo con los comandos de la sección 1. El primer comando corre las pruebas, crea los JAR y los copia a `runtime`. Un `BUILD SUCCESS` por sí solo no inicia ningún servidor.
- **Firma:** abre [la guía de Android Studio](ABRIR_EN_ANDROID_STUDIO.md), selecciona la variante `aula` y usa tu .jks. El paquete de esta variante es `cl.oyl.mobile.aula`, versión 4.1.2-aula, código 15.
- **Docker:** como alternativa al lanzador local, abre Docker Desktop con contenedores Linux. En CMD en `backend`: `docker compose up --build -d`, `docker compose ps` y `docker compose logs --tail=60`. Espera el registro y prueba por el Gateway. Los Dockerfiles usan los JAR incluidos en `runtime`. No mezcles Windows local y Docker en los mismos puertos. Para exponer el panel desde otro equipo se puede configurar `OYL_PUBLIC_GATEWAY` (p. ej. `http://192.168.1.20:8080`). El Compose provee una clave de desarrollo para clase; usa una clave propia de al menos 32 bytes en `OYL_JWT_SECRET` fuera de ese entorno.

## 4. Dónde están los datos
| Componente | Lugar | Contenido |
|---|---|---|
| Usuarios | `backend/data/oyl-users.mv.db` | Cuentas y hash de contraseña |
| Productos | `backend/data/oyl-products.mv.db` | Catálogo e inventario. Los siete productos iniciales se insertan al encontrar la tabla vacía |
| Pedidos | `backend/data/oyl-orders.mv.db` | Cabecera, líneas, estado y datos de entrega |
| Android | Room/SQLite privado de la instalación | Carrito, sesión, avatar, notificaciones y caché. La compra y el stock se confirman en Spring |

Los tres primeros son archivos H2 persistentes, no bases en memoria. Reiniciar no elimina los datos. En Docker, cada servicio usa un volumen distinto: no uses `down -v` si quieres conservarlos. El APK no incluye cuentas locales para saltarse el login. El token tiene vencimiento; vuelve a iniciar sesión si venció.

**Si te interesa conservar los datos del ZIP anterior:** detén primero los procesos antiguos, haz una copia de seguridad de sus archivos `oyl-*.mv.db` y cópialos al nuevo `backend/data/` **antes del primer inicio**, sin sobrescribir bases nuevas con información. A veces Maven los dejaba en `backend/user-service/data`, `product-service/data` y `order-service/data`. Usa el mismo juego de las tres bases. Las sesiones del formato antiguo deben iniciar sesión de nuevo. El paquete de aula tiene su propio almacenamiento Android y no borra el de la app original.

## 5. Endpoints de la API REST

**Usa el puerto 8080 para la app.** En el PC el origen es `http://localhost:8080`; en el emulador es `http://10.0.2.2:8080`. En el A07, sustituye localhost por la IPv4 del PC. Los nombres entre llaves representan valores que cambian con cada producto, usuario o pedido.

| Método | Ruta del Gateway | Quién la usa / para qué |
|---|---|---|
| GET | `/actuator/health` | Estado del Gateway |
| GET | `/api/health` | Estado de Usuarios, conservado por compatibilidad |
| GET | `/api/health/users` | Salud de Usuarios a través del Gateway |
| GET | `/api/health/products` | Salud de Productos a través del Gateway |
| GET | `/api/health/orders` | Salud de Pedidos a través del Gateway |
| POST | `/api/auth/register` | Público. Registra una cuenta de cliente y devuelve la cuenta con token |
| POST | `/api/auth/login` | Público. Recibe `alias` y `password`; devuelve la cuenta con token |
| PUT | `/api/users/{id}` | Actualizar el perfil del titular del token |
| GET | `/api/products` | Lista central de productos; la vista del cliente filtra los activos |
| POST | `/api/products?adminId={adminId}` | Crear producto como administrador |
| PUT | `/api/products/{id}?adminId={adminId}` | Editar o reactivar |
| DELETE | `/api/products/{id}?adminId={adminId}` | Dar de baja sin eliminar el historial |
| POST | `/api/products/{id}/stock?adminId={adminId}` | Registrar un ajuste de inventario, con un campo JSON llamado delta |
| GET | `/api/locations` | Puntos de retiro existentes |
| POST | `/api/orders` | Cliente: enviar pedido, unidades y datos de retiro o envío |
| GET | `/api/orders?userId={id}` | Cliente: sus propios pedidos. Administrador: listado completo |
| PATCH | `/api/orders/{id}/status?adminId={adminId}` | Actualizar estado como administrador |

Las operaciones protegidas usan la cabecera `Authorization: Bearer <token>`. El servidor compara identidad y rol; escribir un ID de administrador en la URL no concede permisos. Puedes abrir las rutas públicas GET en el navegador. Para el resto usa la app o el script de integración de esta entrega.


### Ejemplos para probar desde CMD

Con el backend iniciado, estas consultas son de solo lectura:

```cmd
curl.exe -i http://localhost:8080/actuator/health
curl.exe -i http://localhost:8080/api/health/products
curl.exe -sS http://localhost:8080/api/products
curl.exe -sS http://localhost:8080/api/locations
```

Los dos primeros comandos permiten mostrar el código HTTP y el cuerpo de la respuesta. En el catálogo se pueden comparar el ID, nombre, precio, stock y estado activo de un mismo producto antes y después de una compra. El detalle del producto en Android parte del catálogo: no hay una ruta pública `GET /api/products/{id}` definida en el controlador.

**Ejemplos de cuerpos JSON:** para un ajuste de stock se envía `{"delta":-1}`; para cambiar el estado de un pedido, `{"status":"PREPARADO"}`. Ambas operaciones requieren el token de administrador y su ID. Para crear una compra se envían `userId`, `delivery`, `contact`, `items` y `fulfillment`; cada elemento de `items` indica `productId` y `quantity`. En la app, Retrofit construye estas solicitudes en [OylApi.kt](android/app/src/main/java/cl/oyl/mobile/data/remote/OylApi.kt).

### Servicios y puertos

| Proceso | Puerto local por defecto | Qué mostrar |
|---|---|---|
| Eureka | 8761 | Panel con las cuatro instancias registradas. |
| API Gateway | 8080 | Entrada de la app; /actuator/health y catálogo responden por este puerto. |
| Usuarios | 8081 | Registro, credenciales, perfil y su base H2. |
| Productos | 8082 | Catálogo, retiros e inventario; compara el stock antes y después. |
| Pedidos | 8083 | Compra, historial y cambio de estado. |

En la ejecución por Docker, solo 8761 y 8080 se publican en Windows. Los otros tres puertos se comunican dentro de la red de contenedores. `security-core` es una biblioteca Java compartida: no tiene proceso ni puerto propio.

### Qué ocurre cuando el cliente compra

1. Android prepara las líneas y los datos de retiro o envío. Retrofit envía `POST /api/orders` al Gateway.
2. El Gateway consulta las instancias anunciadas en Eureka y envía la solicitud a **Pedidos**. Eureka actúa como directorio; no recibe el carrito ni guarda el pedido.
3. Pedidos llama a **Productos** para reservar unidades y obtener el precio real. Las llamadas a `POST /internal/products/{id}/reserve` y, si hay que revertir, `POST /internal/products/{id}/release`, son internas y no se publican en el Gateway. Existen también consultas internas `GET /internal/products/{id}` y `GET /internal/users/{id}`.
4. Productos actualiza su base H2 y Pedidos guarda el pedido en la suya. Android recibe el ID, limpia el carrito Room y puede consultar el nuevo pedido por la API.


## 6. Qué mostrar en la defensa
1. Abre http://localhost:8761/ en el PC. Deben figurar cuatro instancias UP. Cada enlace de instancia abre una ruta de estado que responde JSON.
2. Deja abierto http://localhost:8080/api/products. Desde la APK entra como admin, crea un producto, edita el precio y recarga esa pestaña. Muestra el mismo ID y precio. «Desactivar» hace baja lógica (DELETE); «Editar» permite reactivarlo, preservando los pedidos anteriores.
3. Como cliente recién registrado, compra una unidad con retiro en una tienda. Compara el stock antes y después en la API y el número de pedido en la app. Como admin, cambia su estado. Esto demuestra la conexión móvil de extremo a extremo.
4. Para explicar el código: `data/remote/OylApi.kt` define las llamadas Retrofit; `viewmodel/checkout/CheckoutViewModel.kt` arma el pedido; `backend/api-gateway/src/main/resources/application.properties` asigna la ruta al nombre Eureka; `OrderController.java` reserva existencias en Productos y guarda el pedido en H2. Eureka registra direcciones. El flujo usa HTTP; no hay Kafka en esta versión.
5. Abre [RUTAS.md](RUTAS.md) para explicar las llamadas entre Android y los servicios. En `PRUEBAS.md`, `RUBRICA.md` y los informes HTML de `evidencias/` están los resultados medidos. En `CAMBIOS.md` está el detalle de modificaciones respecto del ZIP anterior. Los documentos de `documentacion/historial` describen versiones previas.

### Pruebas, cobertura y trabajo en equipo

Desde `backend`, `mvn verify` ejecuta las pruebas del servidor. Desde `android`, `gradlew.bat :app:testDebugUnitTest :app:createDebugUnitTestCoverageReport` ejecuta las pruebas unitarias móviles y genera el informe JaCoCo en `app/build/reports/coverage/test/debug/index.html`. El porcentaje global incluye pantallas y clases generadas; se debe indicar el alcance cuando se informa cobertura de lógica. Si se usa la copia ampliada de pruebas, el informe propio de esa copia distingue ambos porcentajes. Una compra real desde el teléfono verifica la integración y se demuestra aparte.

Usamos [GitHub](https://github.com/jjuliiann/OYL-Frutos-Secos) para versionar el código y Trello para organizar el trabajo de ambos integrantes. Para la defensa podemos relacionar una tarjeta terminada con un cambio del repositorio y demostrar ese comportamiento en la app.


