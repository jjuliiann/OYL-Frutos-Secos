# Verificación de la revisión 4.1.1-aula

| Verificación de esta entrega | Resultado |
|---|---|
| Código y pruebas Android | Se compilaron los 90 archivos Kotlin de producción y los archivos de las pruebas. Pasaron **26/26** pruebas JVM (QR, compra, validaciones y hash). Ver `evidencias/4.1.1/`. |
| Construcción de la APK | Kotlin + Compose, Java de Room y D8; aapt verificó el paquete `cl.oyl.mobile.aula`, versión 14; zipalign comprobó la alineación. Se usaron compiladores locales porque el wrapper no obtuvo Gradle 8.13 en este entorno. Se incluye el código para volver a construir con Android Studio. |
| Menú por rol | Se comprobó que el único cambio en los botones del panel fue retirar el de consulta externa en las ramas de administrador, cliente e invitado. En los DEX no queda ni el rótulo ni el dominio del proveedor. |
| Backend y comunicación | Los 72 archivos bajo `backend/`, incluidos los cinco JAR y el arranque por CMD, son idénticos a la base. Las 14 operaciones Retrofit de la tienda conservan sus contratos. |
| Recursos y datos | Las clases de las dependencias, recursos, imágenes, bibliotecas nativas y mapa empaquetados se compararon con el APK original. La versión de Room, su esquema y la selección de URL del Gateway no se modificaron. |
| Firma | Se verificó una copia con certificado de demostración para ensayar. El APK entregado se firma en el PC del equipo; el comando comprueba el certificado público de su APK anterior. No se dispone de su clave privada. |

## En el Samsung y el PC de la defensa

1. Ejecutar `apk/FIRMAR_PARA_EL_SAMSUNG.cmd` en Windows; leer la salida real de apksigner. El CMD se entrega para su PC; la firma personal y la instalación en ese equipo no se ejecutaron aquí.
2. Instalar encima con **Actualizar**; comprobar la sesión, la dirección del PC y el carrito que ya tenía el teléfono.
3. Probar en la red de la defensa: abrir catálogo, comprar una unidad y contrastar el stock con `http://localhost:8080/api/products` desde el PC. Recorrer el QR de la cámara y dos marcadores de retiro si van a mostrarlos.
4. El detalle de las pruebas históricas de integración, instrumentación y cobertura de 4.1.0 se guarda en `documentacion/historial/PRUEBAS_EP4_CMD_4_1_0.md`. No se presenta la cobertura histórica ni sus 14 pruebas instrumentadas como resultados nuevos de 4.1.1.

## Resultado de la prueba en el emulador

- El Gateway en el PC de la prueba devolvió 7 productos: Almendras, Nueces, Mix Premium, Pasas, Semillas de chía, Cranberries, Avellanas.
- APK 4.1.0-aula instalada por adb; login correcto de cliente, panel anterior y entrada a la consulta externa desde el panel.
- adb install -r aceptó la actualización a 4.1.1-aula, firmada para este ensayo con el mismo certificado de demostración de la versión de partida. No se desinstaló ni se borraron los datos.
- La versión 4.1.1-aula abrió el panel de cliente con la sesión anterior. «Mis pedidos» está presente y no aparece el botón eliminado al recorrer el menú.
- El catálogo se abrió tras la actualización y mostró Almendras.
- Un enlace oyl://product/1 abrió el detalle de Almendras en la nueva app.
- Login como administrador en la versión nueva; siguen los accesos a productos, inventario y pedidos y no aparece la sección retirada.

Evidencia: `evidencias/4.1.1/actualizacion-emulador.txt`. El ensayo usa una firma de demostración entre ambas versiones; la firma personal y el Samsung se comprueban en el PC del equipo.
