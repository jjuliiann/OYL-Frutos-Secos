# Organización por paquetes · 3.2 SQLite

## Dónde encontrar cada parte

Dentro de `android/app/src/main/java/cl/oyl/mobile`:

| Carpeta | Responsabilidad y archivos principales |
| --- | --- |
| `model` | Un archivo por modelo: Account, Product, PurchaseOrder, OrderLine, PickupLocation, datos de registro y perfil. |
| `viewmodel` | OylViewModel: estado y acciones de la aplicación. |
| `data/local` | LocalDb: configuración de Room y archivo SQLite. |
| `data/local/entity` | Entidades locales: Session, CartLine, StoredAccount, StoredOrder, StoredLine, StoredLocation, avatar, avisos y compatibilidad de caché. |
| `data/local/dao` | LocalDao y StoreDao: consultas SQLite. |
| `data/local/migration` | Migraciones de base, conservando datos de versiones anteriores. |
| `data/repository` | LocalRepository: usuarios, productos, compras, inventario y reglas de negocio. |
| `ui/bienvenida` | WelcomeScreen: logo y pantalla inicial. |
| `ui/auth` | LoginScreen y RegisterScreen. |
| `ui/inicio` | DashboardScreen. |
| `ui/catalogo` | CatalogScreen y ProductDetailScreen. |
| `ui/carrito` | CartScreen y CheckoutScreen. |
| `ui/pedidos` | OrdersScreen. |
| `ui/productos` | AdminProductsScreen y ProductForm. |
| `ui/inventario` | InventoryScreen. |
| `ui/mapa` | MapScreen y PickupMap. |
| `ui/qr` | QrScreen, muestras QR y QrParser. |
| `ui/perfil` | ProfileScreen. |
| `ui/notificaciones` | NotificationsScreen. |
| `ui/components` | Botones animados, campos, títulos, imágenes y tarjetas compartidas. |
| `ui/navigation` | OylNavigation, Page y NavigationMotion. |
| `ui/theme` | Colors: paleta compartida. |
| `util` | Validación, formatos, fotos, contraseñas y cálculos monetarios. |

`MainActivity.kt` queda en la raíz como punto de entrada, con la creación del canal de
notificaciones y la aplicación del tema. Las pantallas ya no están agrupadas dentro de
PublicScreens.kt, CommerceScreens.kt, DeviceScreens.kt ni AdminScreens.kt.

Son paquetes Kotlin separados dentro del módulo Android `app`; se reutilizan modelos,
repositorio, ViewModel y componentes comunes. No se han creado aplicaciones o bases
independientes por pantalla. Por ejemplo, carrito y pedidos comparten los mismos productos.

## Qué cambió

- 65 archivos Kotlin principales, distribuidos por función y responsabilidad.
- Modelos y entidades con un archivo por tipo.
- Imports internos actualizados explícitamente y archivos de prueba en los paquetes correspondientes.
- ProductForm tiene visibilidad `internal` para ser usado desde AdminProductsScreen en su paquete.
- El código de navegación se extrae de MainActivity; animaciones y rutas se conservan.
- Los cuerpos de LocalRepository, OylViewModel, QrScreen, PickupMap y la migración anterior
  fueron comparados con la versión 3.1: idénticos tras quitar package/imports/espacios.

## Base de datos y actualización

Se mantiene applicationId `cl.oyl.mobile` y archivo `oyl_sqlite.db`.
Versión de app: 3.2-sqlite; versionCode 7.

La base pasa a versión 3 para que Room valide el esquema tras cambiar los nombres de paquete
de sus clases. `PackageLayoutMigration` conecta 2 → 3 sin modificar tablas ni registros.
Se mantiene también `DisplayCopyMigration` 1 → 2, para actualizar desde cualquiera de ambas versiones.
No se usa `fallbackToDestructiveMigration` ni se reinicia el inventario.

Extraer esta entrega en otra carpeta, abrir `android`, sincronizar y ejecutar con Run en
el mismo dispositivo. No desinstalar ni borrar los datos de la app si se quieren conservar.

## Comprobaciones ejecutadas

- 65 archivos: ruta y declaración de paquete coherentes, sin símbolos duplicados.
- Imports internos comprobados contra el índice de declaraciones.
- Conservación exacta de los cuerpos del repositorio, ViewModel, QR y mapa indicados arriba.
- Prueba SQL de limpieza de textos ejecutada después de mover su archivo: correcta.
- Prueba JavaScript de selección de marcadores: correcta.
- Se intentó Gradle assembleDebug y testDebugUnitTest: bloqueado al descargar Gradle por red.
  No se afirma compilación Android ni ejecución de pruebas instrumentadas de esta revisión.

Antes de entregar, compilar en Android Studio y comprobar actualización con datos existentes,
login, carrito, compra, pedidos, inventario, QR, mapa y transiciones. Los tests instrumentados
reubicados requieren un emulador o teléfono.

`verificacion/mapa_paquetes.json` relaciona cada archivo anterior con su destino actual.
Los informes anteriores se mantienen como historial de sus respectivas revisiones.
