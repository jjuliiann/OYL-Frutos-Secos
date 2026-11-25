# Escáner QR y apertura de productos · 3.4-sqlite

Julián Martínez y Lukas Gómez · Sección 004D · Profesor Cristian Gomez

## Cambios

- CameraX 1.4.2: PreviewView + ImageAnalysis + ML Kit QR incluido en la APK.
- Escáner en vivo: sin aplicación externa de fotos ni disparador; un resultado por sesión.
- Al leer un producto activo en vivo se abre su ficha. La galería, la lectura manual y el
  simulador conservan su tarjeta de resultado y el acceso administrativo al inventario.
- Permiso CAMERA en tiempo de ejecución, rechazo con alternativas y acceso a Ajustes.
- Liberación de fotogramas, analizador, observador de estado y casos de uso al cerrar;
  enlace con el ciclo de vida para dejar de usar la cámara en segundo plano.
- Manejo de QR propio inválido, ajeno, inexistente y producto desactivado.
- «Producto encontrado» sin referencia a SQLite en el resultado QR.
- Enlace `oyl://product/ID` registrado en AndroidManifest, ACTION_VIEW, BROWSABLE y singleTop.
- MainActivity recibe arranque en frío y onNewIntent; conserva solicitudes pendientes al
  recrearse. Consulta real de Room después de completar la inicialización, sin asumir que
  un catálogo vacío momentáneo signifique que no existe el producto.
- Se descartan los avisos del producto anterior antes de procesar otro enlace.
- Perfil y formulario de productos también solicitan permiso antes de lanzar su cámara:
  declarar CAMERA para el escáner obliga a revisar esas capturas existentes.
- Sin cambios de tablas, migraciones, cuentas, stock o contratos de compra. DB continúa en v4.
- versionName 3.4-sqlite, versionCode 9, mismo applicationId cl.oyl.mobile.

## Instalación

1. Extraer esta entrega en otra carpeta y abrir su carpeta `android` en Android Studio.
2. Sincronizar Gradle para descargar CameraX. Ejecutar Run o generar otra APK.
3. Instalar como actualización sobre la app anterior usando el mismo certificado de firma.
   No desinstalar ni borrar los datos para conservar productos y pedidos.
4. En QR pulsar «Abrir escáner QR», permitir cámara y enfocar uno de los nuevos PNG.
5. Para la apertura externa usar un lector que abra URI personalizados y elegir abrir OYL.
   En la cámara de Samsung/Google concreta hay que comprobar si admite este tipo de URI.

## Límite de apertura externa (pendiente de dominio)

Los nuevos QR usan enlaces personalizados, no una página web ni un servidor. Android puede
abrirlos si otra aplicación le entrega el enlace. Un lector que sólo reconoce HTTPS puede
mostrar texto o no ofrecer abrir OYL: no se garantiza funcionamiento con todas las cámaras.
Para esa compatibilidad se necesita un dominio propio, HTTPS, assetlinks.json y huella SHA256
del certificado de firma de la APK instalada. No se añadió un dominio falso ni un backend.
Fuentes oficiales:
- https://developer.android.com/training/app-links/create-deeplinks
- https://developer.android.com/training/app-links/about
- https://developer.android.com/media/camera/camerax/analyze

Los IDs son locales. Un QR puede existir en un celular y no en otro. El enlace no transporta
precio ni stock: esos datos siempre se consultan en Room, no se confía en datos del QR.
Los enlaces externos de productos desactivados muestran ese estado incluso sin sesión;
no conceden rol administrador ni habilitan compras como invitado.

## Verificaciones ejecutadas en el entorno de edición

| Verificación | Resultado |
| --- | --- |
| JUnit sobre parser real Kotlin 2.2.20 | 6 tests pasaron: formatos anteriores, nuevo URI, rechazo de host/ruta/query/fracción/overflow, IDs límites, generador y valores no positivos |
| PNG con ZXing-C++ | 18 archivos decodificados: 9 entregables individuales y 9 recursos; contenidos coinciden |
| Parser oficial Kotlin 2.2.20 | 81 archivos, 0 errores de sintaxis. No comprueba tipos ni compila Android |
| Paquetes/imports internos | 72 archivos principales coherentes; cadena de migraciones v4 conservada |
| Manifest y recursos XML | Parseo y filtro ACTION_VIEW/BROWSABLE revisados |
| Integridad de la entrega | ZIP sin errores y comparación de archivos con la versión 3.3 |
| assembleDebug + testDebugUnitTest | Intentado; Java no pudo descargar Gradle 8.13: Network is unreachable. No se ejecutaron esas tareas |
| Cámara, ML Kit en dispositivo, UI y apertura por cámara externa | No ejecutados en este entorno; requieren Android Studio/dispositivo |

Los logs están en `verificacion/*_3_4.txt`. No se entrega ni se afirma haber compilado una APK.
Los informes de versiones anteriores se conservan como historial, no como validación de esta UI.

## Lista manual para Android Studio / celular

- [ ] Actualizar la APK y comprobar que conserva usuarios, pedidos, carrito y stock.
- [ ] Abrir escáner, conceder permiso, enfocar Almendras: abre detalle con precio/stock reales.
- [ ] Mantener el mismo QR ante la cámara: sólo debe abrir una vez.
- [ ] Volver a QR y escanear otro producto; comprobar cambio de ficha y mensaje limpio.
- [ ] Escanear ID 999999: «Producto inexistente», sin ficha anterior ni «Producto encontrado».
- [ ] Escanear código ajeno: mensaje de formato; código desactivado: mensaje de desactivado.
- [ ] Negar permiso, probar simulador/galería, permitir desde Ajustes y volver al escáner.
- [ ] Salir de QR / segundo plano / girar pantalla / volver: cámara liberada y recuperable.
- [ ] Comprobar fotos de avatar y producto antes y después de conceder permiso al escáner.
- [ ] Galería/simulador: analizar los nueve PNG, abrir detalle y ajustar inventario como admin.
- [ ] Con app cerrada, cámara externa compatible → QR Almendras → abrir OYL → detalle.
- [ ] Con app abierta en Perfil, QR Nueces externo → detalle de Nueces, no ficha anterior.
- [ ] Repetir producto inexistente desde fuera, con y sin sesión; comprobar mensaje.
- [ ] Repetir enlace y girar la pantalla; no repetir el evento de navegación ya consumido.
- [ ] Si la cámara externa no ofrece abrir oyl://, registrar la incompatibilidad; configurar
      dominio/App Links para habilitar HTTPS. No confundir con fallo de consulta SQLite.

### Aislar la apertura de Android de la aplicación de cámara

Con un único dispositivo conectado, abrir CMD en la carpeta platform-tools del SDK y ejecutar:

```bat
adb shell am start -W -a android.intent.action.VIEW -d "oyl://product/1"
adb shell am start -W -a android.intent.action.VIEW -d "oyl://product/2"
adb shell am start -W -a android.intent.action.VIEW -d "oyl://product/999999"
adb shell am start -W -a android.intent.action.VIEW -d "oyl://product/abc"
```

Los dos primeros deben abrir sus detalles; el tercero debe indicar inexistente; el cuarto,
enlace inválido. Estos comandos no se ejecutaron aquí. Probar primero con OYL cerrada y después
abierta. En emulador sin cámara usar cámara simulada o configurar webcam/virtual scene en AVD.

### Generar enlaces para nuevos productos

Usar el ID real que aparece en `products`, por ejemplo `oyl://product/8`. Generar un QR con
ese texto exacto; no poner nombre/precio ni OYL:8 si se quiere abrir desde otra aplicación.
La función Kotlin `productQrLink(id)` centraliza este formato. El lector valida el ID y consulta
el producto; no se incluye un generador visual de QR para administrador en esta modificación.
