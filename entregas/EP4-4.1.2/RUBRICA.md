# Criterios visibles en la planilla del profesor

La foto de la planilla es parcial y no muestra todas las ponderaciones. No se estima una nota. Ver `PRUEBAS.md` para distinguir esta revisión de la evidencia anterior.

| Criterio | Qué mostrar y qué falta |
|---|---|
| Pantallas, formularios y navegación | En Inicio mostrar catálogo y pedidos como cliente, y gestión como administrador; la consulta nutricional se retiró. Revisar los formularios y el recorrido de compra en el Samsung de la defensa. |
| Microservicios Spring Boot y BD | Usuarios, Productos y Pedidos usan H2 persistente; Eureka registra los servicios y el Gateway. Mostrar la salida real de `oyl.bat estado`. |
| Integración móvil y CRUD | Crear o editar un producto como admin y comprobar su ID y precio en `/api/products`. Comprar como cliente, mostrar el pedido y la disminución de stock. El carrito usa Room/SQLite. |
| API Retrofit | Mostrar `OylApi.kt`: `@GET("products")` consulta el catálogo y `@POST("orders")` envía la compra al Gateway. Son las API de OYL. Si el profesor pide específicamente una API nutricional de terceros, esta versión ya no la incluye. |
| Autenticación y roles | Demostrar inicio de sesión de ambos roles y sus menús. El backend valida JWT y rol. |
| Pruebas unitarias / 80 % | 4.1.2: 26/26 pruebas JVM Android. En 4.1.0: 25 unitarias backend, 87,42 % de líneas backend y 3,16 % Android. No se volvió a medir la cobertura Android actual: el objetivo del 80 % en Android sigue sin acreditarse. |
| APK firmada | Generar y firmar la variante **aula** en Android Studio. Mostrar en el PC la salida de `apksigner verify --verbose --print-certs` sobre la APK creada por el equipo. Ver `ABRIR_EN_ANDROID_STUDIO.md`. |
| GitHub y Trello | Presentar los enlaces, los commits y el tablero del equipo. No hay constancia aquí de sus permisos o actualización. |
