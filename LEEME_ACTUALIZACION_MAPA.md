# Actualización del mapa — versión 2.2

Esta revisión responde al recuadro vacío observado en el emulador de Julian el 17/10/2026. Se conservan backend, imágenes, avatar, datos Room y demás funciones.

## Instalar sin empezar todo de nuevo

1. Detener la app con Stop en Android Studio. No desinstalarla ni borrar sus datos.
2. Descomprimir este ZIP en una carpeta nueva.
3. En Android Studio, File > Open y seleccionar `android/` dentro de la nueva carpeta.
4. Esperar la sincronización, elegir el mismo emulador y presionar Run. El backend que ya está funcionando puede seguir abierto; no hace falta reiniciarlo para esta corrección.
5. Abrir Mapa y esperar. Debe aparecer el mapa o un mensaje con Reintentar; ya no se acepta sólo un área vacía como carga terminada.

Si Android muestra un conflicto de firma, detenerse y consultar antes de desinstalar para conservar los datos locales.

## Qué se corrigió

- WebView estable con tamaño MATCH_PARENT y área fija en Compose; contenedor HTML posicionado explícitamente y reajustado al cambiar su tamaño.
- Carga confirmada sólo cuando el contenedor tiene dimensiones positivas y hay un mosaico real cargado dentro de sus límites visibles.
- Identificador por documento y espera de la respuesta JavaScript: descarta estados de un punto/documento anterior.
- Indicador o error sobre el área del mapa, sin ocultar la información en un recuadro vacío; reintento y actualización del estado si vuelve la conexión.
- Se mantiene Leaflet local y OpenStreetMap real; mismos tres puntos y Cómo llegar.

La captura comprueba el fallo visual de la revisión anterior. La causa exacta en ese WebView no pudo reproducirse aquí: se corrige el manejo de tamaño/estado detectado en código y se añaden comprobaciones para no dar una carga vacía por exitosa. Esta revisión necesita una nueva prueba en el emulador.

## Verificación de esta revisión

- Parser oficial Kotlin: 18 archivos, sin errores de sintaxis. No es compilación de tipos Android.
- JavaScript propio: node --check aprobado.
- Ocho comprobaciones de regresión en Node, con DOM/Leaflet de prueba: contenedor de alto cero, imagen sin contenido, mosaico fuera del área visible, carga visible, tres marcadores, identificador de documento y error. No equivale a ejecución en WebView.
- No se pudo instalar un navegador de pruebas por descarga incompleta. No hay resultado visual local que permita certificar esta corrección.
- La lógica/API del backend no se modifica; sus pruebas anteriores siguen documentadas, sin afirmar que se repitieron innecesariamente.

## Evidencia manual recibida durante esta conversación

- Inicio de administrador y fotografías de Almendras, Nueces y Mix Premium: observados en captura.
- Menú cámara/galería, apertura del selector y selección de imagen para avatar: observados en capturas.
- Avatar conservado tras cerrar y reabrir: confirmado por Julian.
- Mapa anterior: fallido (recuadro vacío). Mapa versión 2.2: pendiente de prueba.
