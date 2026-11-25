# Informe de cambios — OYL Frutos Secos EP4

## Base y resguardo

El trabajo se realizó en una copia extraída del ZIP que obtuvo nota 7. El archivo subido original no se sobrescribió. La app existente con Kotlin, Jetpack Compose, MVVM, Room/SQLite, cámara/QR, mapa y flujos de compra se mantuvo como base.

## Cambios añadidos

- `backend/`: proyecto Maven multimódulo con Spring Boot: `user-service`, `product-service`, `order-service` y `api-gateway`, más `security-core` compartido.
- Persistencia H2 separada por servicio en modo archivo; Compose define volúmenes persistentes.
- Autenticación con contraseña BCrypt y token HMAC con vencimiento. Las operaciones protegidas validan token, usuario y rol.
- Gateway en puerto `8080`; Android usa `10.0.2.2:8080` en emulador. El A07 requiere la IP LAN del PC.
- Android Retrofit integrado en login/registro, perfiles, catálogo, administración, stock, pedidos, estados y puntos de retiro.
- Room/SQLite se conserva para carrito, sesión, avatar, avisos y caché local. Los pedidos remotos exitosos se guardan también en caché local para mostrarlos sin conexión.
- Se añadió una pantalla separada de consumo Retrofit para Open Food Facts. No modifica catálogo, precios, inventario ni pedidos de OYL.
- El campo `imageUri` de producto en H2 admite imágenes optimizadas Base64 guardadas por administración.
- Comentarios cortos en los flujos centrales de Retrofit, autenticación, pedidos, inventario y seguridad; el script de inicio espera las respuestas de salud antes de levantar el Gateway.
- Documentación de ejecución, endpoints, persistencia y guion de defensa actualizada.

## Cómo funciona Spring Boot en este proyecto

Spring Boot levanta cada microservicio como un servidor HTTP independiente y configura sus rutas REST y conexión JPA/H2 desde `application.properties`. Usuarios administra cuentas e inicia sesión; Productos mantiene catálogo y stock; Pedidos persiste compras y coordina la reserva del inventario; API Gateway entrega a Android una URL y reenvía cada ruta al servicio dueño.

Retrofit convierte las interfaces Kotlin en solicitudes HTTP y serializa los modelos como JSON. Tras el login, Android guarda el token y un interceptor lo incluye en `Authorization: Bearer ...`. El Gateway enruta la solicitud, los servicios validan firma/rol cuando corresponde y devuelven JSON. La base central del catálogo y pedidos queda en H2 del backend; Room mantiene datos locales del dispositivo.

## Verificaciones realizadas en este entorno

- Se comprobó la integridad del ZIP de entrada: `unzip -t` terminó sin errores.
- XML de los seis `pom.xml` y `AndroidManifest.xml` parsea correctamente.
- Revisión estática de estructura: 28 fuentes Java y 90 fuentes Kotlin bajo los módulos.
- `mvn clean test`: no ejecutable; Maven no está instalado (`mvn: command not found`).
- `./gradlew --offline :app:compileDebugKotlin`: no llegó a compilar; Gradle Wrapper intentó descargar Gradle 8.13 y la red del entorno bloqueó la conexión.
- La captura posterior mostró que Usuarios no iniciaba porque `UserApplication` no registraba el `TokenService`. Se añadió `@Import(SecurityConfiguration.class)`, igual que en Productos y Pedidos. Falta volver a iniciar los servicios para comprobarlo en Windows. El script ahora espera que los tres servicios respondan antes de iniciar el Gateway.
- La captura compartida mostró un error de compilación de `ProductRulesTest`: el test estaba en `cl.oyl`, fuera del paquete `cl.oyl.products`, y no podía acceder al constructor protegido de `Product`. Se movió la prueba al paquete del servicio, que conserva el constructor protegido para JPA. Hay que volver a ejecutar `mvn clean test` con esta versión para confirmar el arreglo.
- No se afirma que Spring Boot, Android, las pruebas o la API externa hayan ejecutado correctamente.

## Pruebas por ejecutar en el PC

1. En `backend`, instalar JDK 21 y Maven 3.9+, luego ejecutar `mvn clean test`.
2. Ejecutar `run-local.bat`, comprobar `/api/health` y `/api/products`.
3. En Android Studio abrir `android`, sincronizar Gradle y ejecutar pruebas unitarias y `app`.
4. Probar login cliente/admin; CRUD de productos, stock, perfil, compra, estados y persistencia tras reiniciar.
5. Probar la API externa con internet; probar el A07 desde la misma Wi-Fi usando la IP del PC.
6. Medir y subir cobertura hasta acreditar el 80% exigido. Generar APK firmado con un keystore propio y documentar versión y evidencias.

## Antes de presentar

Las credenciales `admin / Admin1234!` y `cliente / Cliente1234!`, y la clave HMAC incluida son valores académicos de desarrollo. No deben reutilizarse en producción. Cambiar la clave requiere mantener el mismo valor en todos los servicios. La aplicación usa HTTP solo en la red local de desarrollo; una publicación requiere HTTPS.


## Incorporación de Eureka

Se agregó un módulo Eureka Server en el puerto 8761. Los microservicios y el API Gateway se registran y consultan el registro. Las rutas externas de Retrofit permanecen iguales; el Gateway resuelve internamente cada destino por `lb://`. La comunicación de Pedidos con Productos también usa descubrimiento por nombre y Spring Cloud LoadBalancer.
