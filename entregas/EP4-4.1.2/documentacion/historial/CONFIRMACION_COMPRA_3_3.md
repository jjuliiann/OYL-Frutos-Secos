# Confirmación de compra · 3.3 SQLite

## Retiro en tienda

La persona selecciona una tienda entre los puntos existentes, revisa su dirección y horario
e indica el nombre de quien retira y un teléfono válido. El correo y las observaciones son
opcionales. No se permite confirmar sin elegir un punto disponible.

Se conservan los puntos y coordenadas del proyecto. Sus etiquetas no acreditan que sean
sucursales comerciales verificadas.

## Envío a domicilio

Campos obligatorios: nombre completo, teléfono, calle/avenida, número (acepta S/N), comuna y región.
Opcionales: correo, departamento/casa/bloque, código postal e indicaciones de entrega.
El código postal, si se ingresa, debe tener 7 dígitos. Hay errores por campo y validación
repetida en el repositorio antes de guardar, no solamente en la pantalla.

La modalidad cambia el formulario con una transición animada. Nombre, teléfono y correo
se precargan del perfil; la dirección se completa por campos, sin intentar adivinar cómo
dividir una dirección antigua de texto libre. Los campos del formulario usan rememberSaveable.

## Resumen y consulta

Se muestran cantidades, subtotal y modalidad. La tienda y dirección quedan visibles después
de registrar el pedido, en Mis pedidos y en Gestión de pedidos.

El costo/plazo del envío no estaba definido en el proyecto: se informa que se coordina con la
tienda y que no está incluido en el subtotal. No se inventa una tarifa, transportista ni cobro.
Esta modificación registra el pedido; no agrega una pasarela de pago o despacho externo.

## Persistencia

Se añade `order_fulfillment`, relacionada uno a uno con `orders` por `orderId`.
Guarda destinatario, teléfono, correo, punto de retiro (ID, nombre, dirección y horario),
dirección de envío desglosada y observaciones. Es una copia del momento de compra; editar
el perfil después no cambia el pedido. En retiro no se guarda una dirección de envío oculta.

El detalle de recepción se inserta en la misma transacción que el pedido, sus líneas,
el descuento de inventario y el vaciado del carrito. Un formulario inválido no crea pedido
ni descuenta existencias.

Base SQLite: versión 4. `CheckoutMigration` conecta 3→4, conservando `orders` y añadiendo
la nueva tabla. Se registran también las migraciones 1→2 y 2→3. Los pedidos antiguos
mantienen su contacto original; no se les inventa una tienda o dirección.

La app pasa a 3.3-sqlite, versionCode 8, con el mismo applicationId y archivo de base.
Instalar desde Android Studio en el mismo dispositivo SIN desinstalar ni borrar datos.

## Archivos

- model/CheckoutData.kt: datos del formulario y normalización.
- model/FulfillmentDetails.kt: detalle que se consulta en los pedidos.
- model/PurchaseOrder.kt: referencia opcional al detalle de recepción.
- util/CheckoutValidation.kt: validación pura compartida.
- ui/carrito/CheckoutScreen.kt: formulario por modalidad, selección de tienda y resumen.
- ui/components/FulfillmentSummary.kt: detalle reutilizable de retiro o envío.
- ui/pedidos/OrdersScreen.kt: consulta para cliente y administrador, compatible con pedidos antiguos.
- ui/navigation/OylNavigation.kt: transmite puntos, sesión y pedido registrado a la pantalla.
- viewmodel/OylViewModel.kt y data/repository/LocalRepository.kt: guardado transaccional de la compra completa.
- data/local/entity/OrderFulfillment.kt y data/local/dao/StoreDao.kt: nueva tabla y sus consultas.
- data/local/LocalDb.kt y data/local/migration/CheckoutMigration.kt: migración conservadora.

## Verificación de esta revisión

Ejecutado:

- 7 tests JUnit compilando CheckoutData y CheckoutValidation reales con Kotlin 2.2.20:
  retiro con tienda existente, falta de tienda, dirección obligatoria solo para envío,
  destinatario/teléfono, correo y postal opcionales, normalización y límites. Todos pasaron.
- SQL real de la migración ejecutado en SQLite/Python: conserva pedidos anteriores,
  crea las 16 columnas del detalle, guarda la dirección y verifica la FK con eliminación en cascada.
- Parser oficial Kotlin: 80 archivos Kotlin sin errores sintácticos.
- Imports internos/rutas de los 71 archivos principales comprobados.
- Tipos del modelo/DAO/repositorio/ViewModel revisados con Kotlin, coroutines y stubs mínimos de Android/Room/Lifecycle. No se ejecutó Room en esta comprobación.

No se pudo compilar la aplicación Android ni ejecutar tests instrumentados: Gradle no logra
descargar su distribución en este entorno. Las verificaciones anteriores no sustituyen
la prueba de Room generado ni la revisión visual en un dispositivo.

Pruebas Android añadidas/actualizadas (pendientes):

- CheckoutRepositoryTest: snapshot del punto elegido, dirección independiente del perfil y
  rechazo de envío incompleto sin crear pedido/descontar stock.
- CheckoutMigrationTest: actualización desde versión 3 conservando un pedido antiguo.
- Tests anteriores adaptados a CheckoutData y a la cadena completa de migraciones.

## Prueba manual recomendada

1. Actualizar sin desinstalar, comprobar stock/pedidos existentes.
2. Retiro: intentar confirmar sin tienda, elegir un punto y registrar pedido.
3. Abrir Mis pedidos y comprobar persona, teléfono, dirección y horario del punto elegido.
4. Envío: intentar confirmar con campos vacíos, después completar los obligatorios y registrar.
5. Ver dirección, departamento e indicaciones en Mis pedidos y como administrador.
6. Editar perfil y verificar que el pedido conserva sus datos originales.
7. Cerrar/reabrir y volver a consultar; probar giro del dispositivo durante el formulario.

En CMD, desde `android`:

```bat
gradlew.bat :app:assembleDebug :app:testDebugUnitTest
gradlew.bat :app:connectedDebugAndroidTest
```
