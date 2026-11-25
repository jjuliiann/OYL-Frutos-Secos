# Backend OYL Frutos Secos

Este backend forma parte de la copia de trabajo EP4. El ZIP de SQLite que ustedes entregaron no se modifica. Android usa Retrofit para consultar estos servicios y conserva Room/SQLite en el teléfono para el carrito, el avatar, las notificaciones y la caché del catálogo.

## Servicios

| Servicio | Puerto | Función | Persistencia |
|---|---:|---|---|
| API Gateway | 8080 | URL única consumida por Android | No guarda datos |
| Eureka Server | 8761 | Registro y descubrimiento de servicios | No guarda datos |
| Usuarios | 8081 | Registro, inicio de sesión, perfil y emisión de token | H2 en archivo |
| Productos | 8082 | Catálogo, altas/edición, inventario y puntos de retiro | H2 en archivo |
| Pedidos | 8083 | Compras, detalle, estados y coordinación del stock | H2 en archivo |
| `security-core` | — | Firma/verifica tokens compartidos por los servicios | — |

Cada servicio de negocio es una aplicación Spring Boot que inicia su servidor HTTP, lee `application.properties`, configura JPA y publica rutas REST. Eureka Server mantiene el registro de instancias; cada servicio registra su nombre e IP. El Gateway usa rutas `lb://` para encontrar Usuarios, Productos y Pedidos. Pedidos usa Spring Cloud LoadBalancer para consultar Productos por nombre. El Gateway reenvía las rutas al servicio responsable. Para una compra, Android envía el carrito al Gateway; Pedidos valida la sesión, pide reservar stock a Productos y guarda el pedido en su H2.

## Iniciar en Windows 11

1. Instala JDK 21 y Apache Maven 3.9 o superior.
2. Abre CMD en esta carpeta (`backend`).
3. Ejecuta `mvn clean test`.
4. Ejecuta `run-local.bat`; compila e instala los módulos, inicia todos los servicios en segundo plano desde esa misma CMD, espera a que muestre “Backend iniciado” y guarda los logs en `logs/`. Detén los servicios con `stop-local.bat`.
5. Comprueba `http://localhost:8761` para el panel Eureka, y `http://localhost:8080/api/health` y `http://localhost:8080/api/products`.
6. En Android Studio abre la carpeta `android`, espera Gradle Sync y ejecuta `app` en el emulador.

El Gateway escucha en la red local para permitir tanto el emulador como el celular. En el A07, conecta PC y teléfono a la misma Wi-Fi, ejecuta `ipconfig` en el PC, permite el puerto TCP 8080 en el firewall de Windows y agrega `oylApiBaseUrl=http://IP_DEL_PC:8080/api/` a `android/local.properties`, conservando cualquier línea `sdk.dir` que ya exista. En Android Emulator el valor predeterminado es `http://10.0.2.2:8080/api/`.

Credenciales académicas iniciales: `admin / Admin1234!` y `cliente / Cliente1234!`. El inicio de sesión devuelve un token HMAC firmado con vencimiento de ocho horas. Cambia las contraseñas y `OYL_JWT_SECRET` antes de una publicación fuera del entorno académico.

## Persistencia

Cada servicio guarda su H2 en modo archivo dentro de `backend/data/`. Al cerrar las ventanas y reiniciar, el catálogo, usuarios y pedidos permanecen. Eliminar esa carpeta borra los datos. Con Docker, volúmenes nombrados conservan la información; `docker compose down -v` elimina los volúmenes. Android conserva su propia base SQLite por instalación; la caché local no sustituye la base central del backend.

## Eureka y descubrimiento

El registro está disponible en `http://localhost:8761`. Los servicios se registran como `oyl-user-service`, `oyl-product-service`, `oyl-order-service` y `oyl-api-gateway`. El Gateway conserva los endpoints `/api/...`, pero enruta a los microservicios con `lb://`; Pedidos resuelve Productos por el nombre `oyl-product-service`. En Docker, Eureka se alcanza como `http://eureka-server:8761/eureka/`.

## Docker

Con Docker instalado, ejecuta `mvn clean package` y luego `docker compose up --build`. Eureka expone su panel en `http://localhost:8761`. La URL de Android continúa siendo el Gateway del puerto 8080. Detén con `docker compose down` y conserva los volúmenes omitiendo `-v`.

## Rutas públicas

`GET /api/health`; `POST /api/auth/register`; `POST /api/auth/login`; `PUT /api/users/{id}`; `GET /api/products`; `POST /api/products`; `PUT /api/products/{id}`; `POST /api/products/{id}/stock`; `POST /api/orders`; `GET /api/orders?userId=...`; `PATCH /api/orders/{id}/status`; `GET /api/locations`. Para escribir se requiere el token de la cuenta con el rol correspondiente. Las rutas `/internal/**` son llamadas privadas entre servicios.

## Verificación pendiente

En el entorno donde se preparó la copia hay JDK 21, pero no está instalado Maven y Gradle no pudo descargar su distribución por falta de acceso de red. Por lo tanto, no se declara compilación ni pruebas exitosas. Ejecuta `mvn clean test` y Gradle Sync/build en un PC con conexión antes de la defensa.
