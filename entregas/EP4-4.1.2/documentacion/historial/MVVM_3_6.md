# Organización Model y ViewModel · 3.6-sqlite

Julián Martínez y Lukas Gómez · Desarrollo de Aplicaciones Móviles · Sección 004D
Profesor: Cristian Gomez · Duoc UC

## Qué cambió

Antes OylViewModel concentraba acceso, catálogo, compras, perfil, QR y avisos. Ahora cada
funcionalidad tiene un ViewModel real, conectado a la navegación. No se crearon clases vacías
ni se instancia un ViewModel dentro de otro. MainActivity usa `by viewModels<...>()` para que
Android conserve cada instancia ante la recreación de la Activity y libere su scope al finalizar.

Se mantiene un único módulo app. Las carpetas son paquetes, no aplicaciones ni bases distintas.

| Área | Modelos o entidades utilizados | ViewModel y ubicación |
| --- | --- | --- |
| Acceso / registro | model/auth: Account, Credentials, RegisterData; entity/Session | viewmodel/auth/AuthViewModel.kt |
| Catálogo y gestión de productos | model/productos/Product | viewmodel/productos/ProductsViewModel.kt |
| Inventario | Product; model/inventario/StockChange | viewmodel/inventario/InventoryViewModel.kt |
| Carrito | data/local/entity/CartLine y Product | viewmodel/carrito/CartViewModel.kt |
| Confirmación de compra | model/checkout: CheckoutData, FulfillmentDetails, OrderRequest, OrderItem | viewmodel/checkout/CheckoutViewModel.kt |
| Pedidos | model/pedidos: PurchaseOrder, OrderLine, StatusChange | viewmodel/pedidos/OrdersViewModel.kt |
| Perfil y avatar | model/perfil/ProfileUpdate, Account, Session y entity/LocalAvatar | viewmodel/perfil/ProfileViewModel.kt |
| Mapa y retiro | model/mapa/PickupLocation | viewmodel/mapa/MapViewModel.kt |
| Escaneo QR | model/qr/QrResolution y Product | viewmodel/qr/QrViewModel.kt |
| Notificaciones | data/local/entity/LocalNotice | viewmodel/notificaciones/NotificationsViewModel.kt |
| Estado común | Aviso y estado de carga | viewmodel/OylViewModel.kt |

Los modelos históricos OrderRequest/OrderItem/StockChange/StatusChange se conservan por
compatibilidad del código entregado, aunque las operaciones actuales reciben CheckoutData o
valores simples. No se afirma que todos esos DTO se utilicen en ejecución.

Los modelos Room de carrito, sesión y notificaciones siguen en `data/local/entity`, donde ya
estaban. No se duplican como otros Model sólo para que cada carpeta tenga el mismo número de
archivos. Un ViewModel puede utilizar varios modelos y un modelo puede ser compartido por varias
pantallas. Inicio combina los estados de productos/pedidos/carrito; bienvenida y componentes
visuales conservan sus animaciones locales, sin un ViewModel vacío para cada botón.

## Flujo MVVM para explicar al profesor

1. **View:** una pantalla Compose dibuja el estado y envía eventos mediante callbacks.
2. **ViewModel:** conserva los estados observables (StateFlow) y coordina las acciones del área.
3. **Repositorio:** aplica las reglas de negocio y transacciones; consulta los DAO.
4. **Model / entidades:** representan los datos. Room guarda las entidades en SQLite.
5. Room emite los cambios; el ViewModel expone el nuevo estado y Compose vuelve a dibujar.

Ejemplo: el cliente pulsa Agregar → CartViewModel comprueba CLIENT → LocalRepository.addCart
consulta la cuenta y valida producto/stock → Room actualiza cart → StateFlow actualiza el carrito.
El administrador ve gestión de productos e inventario; no recibe controles de compra.

**Model no contiene ViewModel.** Son responsabilidades distintas. No existe una regla de un
ViewModel por cada data class. La organización aplicada es por funcionalidad/pantallas relacionadas.

## Infraestructura compartida

- `data/OylGraph.kt`: contenedor sencillo de dependencias de alcance Application. Mantiene una
  única LocalDb, LocalRepository, sesión observable e inicialización. No usa un framework adicional.
- `viewmodel/BaseOylViewModel.kt`: acceso común a esas dependencias y ejecución con viewModelScope.
- `data/mapper/SessionMapper.kt`: conversión Account → Session, reutilizada por acceso y perfil.
- `OylViewModel` sólo expone avisos y carga general; sus StateFlow son de sólo lectura para la UI.
- Cámara, galería, bitmap y permisos siguen en las vistas que necesitan Android. La decisión de
  producto válido/inexistente/desactivado se realiza en QrViewModel y `domain/qr/ResolveQrProduct.kt`.
- El parser está en `domain/qr/QrParser.kt`, compartido por el escáner y la entrada externa.

## Compatibilidad

- versionCode 11 / versionName 3.6-sqlite; mismo applicationId `cl.oyl.mobile`.
- Mismo archivo `oyl_sqlite.db`, versión de esquema 4 y cadena de migraciones anterior.
- Los cambios en LocalRepository, DAO y LocalDb son imports de modelos reubicados; sus reglas,
  consultas SQL, columnas y anotaciones de tablas se conservan. Product sigue siendo tabla products.
- No se cambia el catálogo inicial, contraseñas, precios, inventario, pedidos ni puntos de retiro.
- Se conservan los PNG, CameraX, galería, simulador, mapa, avatar, animaciones y formularios.
- El nuevo resolvedor QR consulta Room después de la inicialización; no usa una lista simulada.
  Admin también puede consultar un producto inactivo por enlace externo para gestionarlo; cliente
  e invitado reciben el mensaje de desactivado. Los controles administrativos siguen protegidos.
- Se mantiene la limitación de lectores externos que no abren `oyl://`: HTTPS verificado requiere
  dominio propio y certificado. Esta reorganización no cambia esa compatibilidad.

## Verificado en el entorno de edición

| Verificación | Alcance |
| --- | --- |
| 20 pruebas JUnit | 6 de parser QR, 7 de resolución (búsqueda/rol/ausencia/formato/error), 7 de checkout |
| Comprobación parcial de tipos | Archivos reales de modelos, grafo, repositorio, DAO y ViewModels, con coroutines reales y stubs mínimos sólo de Android/Room/Lifecycle. No compila Compose ni genera DAO |
| Sintaxis Kotlin | Parser oficial 2.2.20 sobre todos los .kt; no equivale a build Android |
| Imports y estructura | Paquetes coinciden con carpetas; referencias internas y callbacks de los 11 ViewModels comprobados |
| Compatibilidad estática | SQL/migraciones, declaraciones de entidades y lógica de repositorio conservadas; recursos PNG/mapa sin cambios |
| Compilación Android | Intentada; descarga de Gradle bloqueada por Java (Network is unreachable). No se ejecutaron tareas Android ni pruebas instrumentadas |

Logs actuales: `verificacion/*_3_6.txt`. Los informes 3.0–3.5 son históricos. No se entrega APK.

## Verificación manual necesaria antes de entregar al profesor

1. Extraer en carpeta nueva; abrir `android`, configurar JDK 21 y sincronizar Gradle.
2. Ejecutar `gradlew.bat :app:assembleDebug :app:testDebugUnitTest` y, con dispositivo,
   `gradlew.bat :app:connectedDebugAndroidTest`.
3. Instalar como actualización con la misma firma, sin desinstalar ni borrar datos. Abrir el
   Database Inspector y comprobar que siguen los productos, stock, pedidos, sesión y carrito.
4. Cliente: registro/login, catálogo, QR, agregar/modificar carrito, confirmar retiro y envío,
   consultar pedidos. Probar errores de formulario y falta de stock.
5. Admin: crear/editar/activar/desactivar producto, fotografiar/elegir imagen, ajustar inventario,
   cambiar estados. Confirmar ausencia de controles de compra también al abrir QR.
6. Perfil: editar campos, avatar cámara/galería, cerrar y reabrir; sesión y foto persistentes.
7. Mapa: marcadores, ficha de destino, ruta externa, carga/error/reintento.
8. QR: escáner, galería y simulador; válido/inexistente/inactivo/ajeno. Probar URI externo con
   app cerrada y abierta, tanto como cliente como admin.
9. Girar pantalla y pasar a segundo plano durante lectura; volver y comprobar que no se repite
   una navegación ya consumida y que las cámaras se liberan.
10. Comprobar notificaciones y cambios entre Perfil/Pedidos/Inicio con las animaciones existentes.

Abrir `model` y `viewmodel` en vista Project para mostrar al profesor la correspondencia de
carpetas. Para explicar el recorrido completo, comenzar en OylNavigation → CartViewModel →
LocalRepository → StoreDao/LocalDao → LocalDb.
