# OYL Frutos Secos — copia de trabajo EP4

Esta copia conserva la aplicación que aprobaron con SQLite y agrega la arquitectura solicitada para la Evaluación Parcial 4. El ZIP original no se modifica. Esta variante mantiene Room/SQLite para carrito, sesión, avatar, avisos y caché; las cuentas, catálogo/inventario y pedidos del flujo integrado se consultan y persisten en servicios Spring Boot.

## Qué contiene

- `android/`: Kotlin, Jetpack Compose, MVVM, Room/SQLite, Retrofit y la API externa Open Food Facts.
- `backend/security-core/`: firma y validación de tokens HMAC.
- `backend/user-service/` (8081): registro, login, perfiles y emisión de token.
- `backend/product-service/` (8082): catálogo, administración, inventario y puntos de retiro.
- `backend/order-service/` (8083): pedidos, líneas, estados y reserva central de stock.
- `backend/api-gateway/` (8080): entrada única que usa Android.
- `backend/eureka-server/` (8761): registro y descubrimiento de servicios.
- `backend/docker-compose.yml`: servicios y volúmenes de datos persistentes.

Cada microservicio tiene una base H2 independiente en modo archivo. Por eso sus datos permanecen después de cerrar y volver a iniciar el backend. En Docker, los volúmenes mantienen las bases aunque se recreen los contenedores; `docker compose down -v` sí borra esos volúmenes.

## Ejecutar en Windows 11 — modo local

1. Instala **JDK 21** y **Apache Maven 3.9 o superior**.
2. Abre CMD en `backend` y ejecuta `mvn clean test`.
3. Ejecuta `run-local.bat`; compila los módulos, abre Eureka, Usuarios, Productos y Pedidos, espera a que respondan y queden registrados, y luego inicia el Gateway. Deja abiertas las cinco ventanas. El Gateway acepta conexiones desde el celular en la red local.
4. Comprueba `http://localhost:8761` para el panel Eureka, y `http://localhost:8080/api/health` y `http://localhost:8080/api/products`.
5. Abre en Android Studio la carpeta `android`, espera la sincronización de Gradle y ejecuta `app` en un emulador Android.
6. Para entrar por primera vez usa `admin / Admin1234!` o `cliente / Cliente1234!`. Las cuentas se crean en el servicio de usuarios.

La primera compilación necesita internet para descargar dependencias. Para detener el backend local, ejecuta `stop-local.bat`. Las bases quedan en `backend/data/`.

### Emulador y teléfono físico

El emulador usa `http://10.0.2.2:8080/api/`, que apunta al puerto 8080 del PC anfitrión. En un teléfono conectado a la misma red Wi-Fi, `10.0.2.2` no sirve: en `android/local.properties` agrega `oylApiBaseUrl=http://IP_DEL_PC:8080/api/`, conservando la línea `sdk.dir` si ya existe. Obtén la IPv4 del PC con `ipconfig`; autoriza el puerto TCP 8080 en el firewall si Windows lo bloquea.

La app usa HTTP local para la demo. El Gateway local se enlaza a la red para permitir el A07; los servicios de negocio permanecen accesibles solo desde el PC en modo local. Una publicación real requiere HTTPS y reemplazar la clave de firma de desarrollo.

## Componentes opcionales

Eureka se incorporó para registrar y descubrir los microservicios. Kafka no se agregó porque la guía EP4 adjunta no lo exige y el flujo actual de pedidos no necesita mensajería asíncrona.

## Ejecutar con Docker

Desde `backend`, ejecuta `mvn clean package` y luego `docker compose up --build`. Android sigue conectándose al Gateway en el puerto 8080. Para apagar: `docker compose down`. No agregues `-v` si quieres conservar las bases. Solo se publica el puerto del Gateway; los servicios internos se comunican por la red de Docker.

## Recorrido de una compra

Android lee el carrito local y envía sus productos y datos de retiro/envío con Retrofit. El Gateway dirige la petición al servicio de pedidos. Ese servicio valida el token y el rol de cliente, consulta los productos y solicita reservar stock al servicio de productos. Al completar la compra persiste pedido y líneas en su base; el servicio de productos descuenta stock. Android vuelve a consultar los productos y actualiza su caché Room y el carrito local.

El login comprueba la contraseña con BCrypt. El servicio de usuarios entrega un token firmado que Android conserva localmente; el interceptor de Retrofit agrega `Authorization: Bearer ...` en las llamadas protegidas. Los servicios de producto y pedidos verifican firma, vencimiento, usuario y rol. No guardan contraseñas en SQLite del teléfono.

## API externa

La pantalla **Información de API externa** consulta Open Food Facts por HTTPs con Retrofit y muestra productos de la categoría de frutos secos. Esa información se presenta aparte y no modifica el catálogo, los precios, el inventario ni los pedidos de OYL. La búsqueda usa la API de Open Food Facts y solicita solo los campos que la pantalla necesita. 

## Pruebas disponibles

Backend: desde `backend`, `mvn clean test`. Android: desde `android`, `gradlew.bat :app:testDebugUnitTest`; con emulador conectado, `gradlew.bat :app:connectedDebugAndroidTest`.

En el entorno donde se preparó esta copia, JDK 21 está disponible, pero Maven no está instalado y la descarga del Gradle Wrapper fue bloqueada por falta de conectividad de red. Por eso no afirmamos que el backend ni Android hayan compilado o pasado pruebas aquí. Se debe ejecutar esos comandos en el PC con Maven, SDK Android y acceso a internet.

## Alcance pendiente de comprobación

- Ejecutar los cuatro servicios y probar CRUD, login/token, compra, stock, estados y persistencia.
- Confirmar la consulta Open Food Facts con internet en el emulador.
- Medir cobertura de pruebas y completar el 80 % solicitado por la pauta.
- Generar y verificar el APK firmado con su `.jks` y documentar su ubicación segura.
- Probar conectividad desde el Samsung A07, si usarán dispositivo físico.

Los comentarios `//` en Kotlin/Java y `REM`/`#` en scripts/configuración resumen las líneas principales del flujo. Ver `EP4_INTEGRACION.md` para rutas, dependencias entre servicios y guion de demostración; `INFORME_EP4.md` resume cambios y verificaciones; `RUBRICA.md` compara lo implementado con los requisitos EP4/EP5.
