# Vistas por rol · 3.5-sqlite

## Cambios

- ProductDetailScreen.kt: selector −/+ sólo para CLIENT; ADMIN dispone de gestión de productos y ajuste del inventario del producto seleccionado. Cliente agrega productos activos con stock; invitado ve inicio de sesión.
- DashboardScreen.kt: carrito y pedidos personales sólo para CLIENT. Las tarjetas de destacados usan la misma condición exacta que el catálogo. Invitados no reciben acceso al carrito.
- OylNavigation.kt: conecta los accesos administrativos del detalle, también al llegar desde QR interno o externo. Bloquea navegación de compra a no clientes y gestión a no administradores; las ramas protegidas también revisan el rol al dibujarse, incluso ante una página restaurada.
- OylViewModel.kt: sólo observa carrito de CLIENT; vuelve a comprobar el rol al agregar, ajustar cantidades y confirmar pedido.
- LocalRepository.kt se conserva: addCart y checkout ya consultaban el rol de la cuenta real en SQLite. La autorización no depende sólo de esconder botones.
- app/build.gradle.kts: versionCode 10 / versionName 3.5-sqlite. README actualizado.

No se modifican tablas, migraciones, contraseñas, productos, stock ni el escáner CameraX de 3.4. Se mantienen los QR existentes de la entrega anterior.

## Verificación

Revisión de código, imports internos y sintaxis Kotlin. Intento de assembleDebug/testDebugUnitTest: ver logs *_3_5.txt. No se afirma ejecución de UI ni prueba en celular. Los resultados anteriores son históricos.

## Prueba manual requerida

1. Actualizar la APK con la misma firma, sin desinstalar ni borrar datos. Confirmar versión 3.5-sqlite en la compilación.
2. Entrar como admin: inicio ofrece gestión, sin carrito; abrir destacados y comprobar que no hay −/+, icono ni botón de agregar al carrito.
3. Escanear QR como admin y abrir desde lector externo compatible: misma ficha administrativa; «Ajustar inventario» debe seleccionar ese producto. «Gestionar productos» abre la lista para editar/activar/desactivar.
4. Cerrar sesión; entrar como cliente: destacados/catálogo permiten agregar. La ficha muestra cantidad y compra, sin accesos de gestión.
5. Escanear QR como cliente: mismo detalle de compra; agregar y confirmar pedido con retiro/envío.
6. Sin sesión, abrir QR externo: sólo consulta e inicio de sesión, sin compra directa ni gestión.
7. Repetir tras girar/reabrir y cambiar de admin a cliente para descartar acciones del rol anterior.
8. Comprobar que el stock y los pedidos previos permanecen.
