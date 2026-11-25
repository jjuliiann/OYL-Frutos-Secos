# Actualización de interfaz · 3.1 SQLite

## Textos

Se retiran las etiquetas “ficticio”, “de prueba” y “demostración” de formularios,
validaciones, avisos, perfil, mapa, datos iniciales y visor QR. También se elimina la
caja de credenciales de la pantalla de acceso. Las credenciales iniciales siguen
explicadas en el README. La bienvenida termina con el eslogan de la tienda.

El visor continúa llamándose “cámara simulada” para identificar su función; se conserva
la lectura real de las imágenes con ML Kit. No se presenta como captura de una cámara física.

Las coordenadas y direcciones originales no se cambian. Cambiar sus etiquetas visuales
no verifica que correspondan a sucursales comerciales; siguen siendo las referencias del proyecto.

## Animaciones

`MainActivity.kt` utiliza desplazamiento lateral de entrada (un tercio de pantalla),
salida parcial, desvanecimiento y escala 0,94 → 1. La transición dura hasta 520 ms.
`NavigationMotion.kt` define el orden de navegación: Perfil → Pedidos entra desde la izquierda;
Pedidos → Perfil desde la derecha. Se aplica a las demás pantallas, incluido volver atrás.
Los iconos inferiores crecen suavemente al seleccionar su pestaña. Se conservan los efectos
de presión de botones y la animación de bienvenida.

## Conservar la instalación actual

1. Extraer el ZIP actualizado en otra carpeta.
2. Abrir su carpeta `android` en Android Studio.
3. Ejecutar con Run en el mismo dispositivo, sin desinstalar ni borrar datos.

Se mantienen applicationId y nombre `oyl_sqlite.db`. versionCode sube a 6 y versionName a
3.1-sqlite. La base pasa de versión 1 a 2 con `DisplayCopyMigration`: nueve actualizaciones
acotadas a textos iniciales conocidos. No usa migración destructiva. No cambia stock,
pedidos, contraseñas, carrito, IDs, fotografías ni coordenadas. Los textos personalizados
no se reemplazan. Los textos antiguos en el archivo de migración son patrones de búsqueda,
no textos que se presenten nuevamente en pantalla.

## Verificación de esta revisión

- SQL real de la migración ejecutado sobre SQLite en Python: conserva pedido, stock,
  carrito, coordenadas y valores personalizados; ejecutarlo dos veces no produce efectos adicionales.
- Auditoría de textos de la interfaz: no quedan las etiquetas retiradas en las pantallas.
- Prueba `DisplayCopyMigrationTest` añadida para Room real: pendiente en Android.
- Compilación Gradle intentada: bloqueada al descargar la distribución por red desde Java.
- Las transiciones requieren prueba visual en Android Studio; no se afirma que hayan sido
  ejecutadas en emulador desde este entorno.

Ver los archivos `verificacion/*3_1*`. Los resultados previos de `PRUEBAS.md` corresponden
a la versión 3.0 y se conservan como historial, no como pruebas nuevas de esta revisión.

## Archivos modificados

- MainActivity.kt: transiciones e iconos de navegación.
- NavigationMotion.kt: nuevo orden para la dirección de las transiciones.
- DisplayCopyMigration.kt: nueva migración de textos ya almacenados.
- LocalDb.kt: versión 2 y registro de la migración.
- LocalRepository.kt: textos iniciales nuevos.
- PublicScreens.kt: etiquetas y acceso sin caja de credenciales.
- DeviceScreens.kt: mapa, perfil y avisos.
- CommerceScreens.kt: campo contacto.
- Validation.kt: mensajes por campo.
- OylViewModel.kt: título de avisos guardados.
- WelcomeScreen.kt y QrScreen.kt: textos visibles.
- app/build.gradle.kts: versión de aplicación 3.1.
- DisplayCopyMigrationTest.kt: prueba instrumentada de actualización sin perder pedido y stock.
