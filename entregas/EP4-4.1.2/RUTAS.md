# Rutas reales de OYL

**Usa el puerto 8080 para la app.** En el PC el origen es `http://localhost:8080`; en el emulador es `http://10.0.2.2:8080`. En el A07, sustituye localhost por la IPv4 del PC. Los nombres entre llaves representan valores que cambian con cada producto, usuario o pedido.

| Método | Ruta del Gateway | Quién la usa / para qué |
|---|---|---|
| GET | `/actuator/health` | Estado del Gateway |
| GET | `/api/health` | Estado de Usuarios, conservado por compatibilidad |
| GET | `/api/health/users`, `/api/health/products`, `/api/health/orders` | Comprueban **por separado** que el Gateway alcanza cada servicio |
| POST | `/api/auth/register`, `/api/auth/login` | Registrar un cliente o comprobar credenciales y recibir un JWT |
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

## Procesos que debes poder señalar

| Proceso | Puerto local por defecto | Qué mostrar |
|---|---|---|
| Eureka | 8761 | Panel con las cuatro instancias registradas. |
| API Gateway | 8080 | Entrada de la app; /actuator/health y catálogo responden por este puerto. |
| Usuarios | 8081 | Registro, credenciales, perfil y su base H2. |
| Productos | 8082 | Catálogo, retiros e inventario; compara el stock antes y después. |
| Pedidos | 8083 | Compra, historial y cambio de estado. |

En la ejecución por Docker, solo 8761 y 8080 se publican en Windows. Los otros tres puertos se comunican dentro de la red de contenedores. `security-core` es una biblioteca Java compartida: no tiene proceso ni puerto propio.

## Qué ocurre cuando el cliente compra

1. Android prepara las líneas y los datos de retiro o envío. Retrofit envía `POST /api/orders` al Gateway.
2. El Gateway consulta las instancias anunciadas en Eureka y envía la solicitud a **Pedidos**. Eureka actúa como directorio; no recibe el carrito ni guarda el pedido.
3. Pedidos llama a **Productos** para reservar unidades y obtener el precio real. Las llamadas a `POST /internal/products/{id}/reserve` y, si hay que revertir, `POST /internal/products/{id}/release`, son internas y no se publican en el Gateway. Existen también consultas internas `GET /internal/products/{id}` y `GET /internal/users/{id}`.
4. Productos actualiza su base H2 y Pedidos guarda el pedido en la suya. Android recibe el ID, limpia el carrito Room y puede consultar el nuevo pedido por la API.

**Demostración de un minuto:** en el PC abre `http://localhost:8761/` y luego `http://localhost:8080/api/products`. En Android compra una unidad; recarga la segunda pestaña y compara el stock del mismo ID. Después enseña el mismo número de pedido en la cuenta del cliente y en Gestión de pedidos.
