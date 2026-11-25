# Integración EP4 — OYL Frutos Secos

## Arquitectura y responsabilidad

| Componente | Puerto local | Responsabilidad |
|---|---:|---|
| API Gateway | 8080 | Única URL pública de Retrofit; enruta por ruta REST. |
| Usuarios | 8081 | Registro, inicio de sesión, BCrypt, perfil y token. |
| Productos | 8082 | CRUD de catálogo, stock y puntos de retiro. |
| Pedidos | 8083 | Compra, detalle, estados y coordinación de reserva de stock. |
| security-core | — | Crea y verifica tokens HMAC que comparten los servicios. |
| H2 por servicio | — | Persistencia remota separada en archivos `backend/data/`. |

La aplicación Android mantiene Room/SQLite para el carrito, la sesión, el avatar, avisos y caché. Room no reemplaza a la base del servidor: la fuente central para productos, stock y pedidos ahora es el backend.

## Rutas públicas por el Gateway

- `GET /api/health`
- `POST /api/auth/register` y `POST /api/auth/login`
- `PUT /api/users/{id}` (requiere token de ese usuario)
- `GET /api/products`
- `POST /api/products?adminId={id}`
- `PUT /api/products/{id}?adminId={id}`
- `POST /api/products/{id}/stock?adminId={id}`
- `POST /api/orders`, `GET /api/orders?userId={id}`
- `PATCH /api/orders/{id}/status?adminId={id}`
- `GET /api/locations`

Las rutas `/internal/**` no se publican por el Gateway; solo se usan entre los servicios en ejecución local o dentro de Docker.

## Microservicios en Spring Boot

Cada servicio tiene su propia clase `@SpringBootApplication`. Spring Boot inicia un servidor HTTP independiente, carga `application.properties`, configura JPA y crea/actualiza el esquema de su H2. Los controladores REST reciben JSON, los repositorios acceden a la base y las reglas validan el dominio. El Gateway no guarda negocio: reenvía la petición al servicio dueño de la ruta.

En modo local los servicios escuchan en `127.0.0.1`; el Gateway escucha el puerto 8080. Con Docker, `SERVER_ADDRESS=0.0.0.0` deja que los contenedores se comuniquen por sus nombres internos. Android solo conoce `10.0.2.2:8080` (emulador) o la IP LAN del PC (teléfono).

## Persistencia y pérdida de datos

Las propiedades JDBC usan H2 en modo archivo (`jdbc:h2:file:./data/...`), no `mem:`. El backend local guarda archivos bajo `backend/data`. En Docker, cada H2 usa un volumen nombrado. Reiniciar la app o los servicios no borra estos datos. Borrar esos archivos o ejecutar `docker compose down -v` sí los elimina. Los datos locales SQLite siguen en cada instalación Android y no se sincronizan entre teléfonos fuera del flujo enviado al backend.

## Pruebas de API para la defensa

1. Levantar los servicios; mostrar `GET /api/health`.
2. Iniciar sesión con `admin / Admin1234!` y explicar respuesta con `id`, `role` y `token`.
3. Mostrar catálogo desde `GET /api/products` y crear/editar o cambiar stock desde Android.
4. Iniciar sesión como cliente, realizar una compra con retiro o envío y mostrar el pedido en `GET /api/orders?userId=...`.
5. Verificar que el stock del microservicio de productos disminuyó y que pedido y líneas persisten luego de reiniciar los servicios.
6. Ingresar como admin, cambiar estado del pedido y revisar respuesta.
7. Ejecutar `mvn clean test` y explicar pruebas de `TokenService`, `ProductRules`, `OrderPolicy` y `UserController`.
8. Abrir la pantalla externa, mostrar datos Open Food Facts y explicar que esa API no altera datos internos de OYL.

Este guion es una propuesta; no se afirma que los pasos se hayan ejecutado todavía. La integración debe probarse localmente antes de la defensa.


## Descubrimiento con Eureka

Eureka Server escucha en el puerto 8761. Usuarios, Productos, Pedidos y API Gateway se registran como clientes. El Gateway mantiene las rutas públicas `/api/...`, pero sus destinos usan `lb://oyl-user-service`, `lb://oyl-product-service` y `lb://oyl-order-service`. Pedidos resuelve Productos por el nombre `oyl-product-service` usando Spring Cloud LoadBalancer. El arranque local primero espera Eureka, luego los tres servicios y su registro, y finalmente inicia el Gateway.
