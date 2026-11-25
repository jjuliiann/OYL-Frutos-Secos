# Verificación de OYL 4.1.2

## Qué se verificó

| Comprobación | Evidencia |
|---|---|
| Código Android | Se compilaron los 90 archivos Kotlin, con Compose 2.2.20 y Android SDK 35. Se ejecutó JUnit sobre las clases nuevas: **26 pruebas JVM aprobadas**. Ver `evidencias/4.1.2/unitarias-android.txt`. |
| Alcance del cambio | Solo cuatro archivos de interfaz y la versión difieren en el proyecto Android. Los 72 archivos del backend coinciden byte por byte con 4.1.1; no hay cambios de base de datos ni de contrato de red. Ver `evidencias/4.1.2/comparacion.txt`. |
| Red | Se ejecutaron los cinco JAR en un entorno independiente. La ruta del catálogo por el Gateway devolvió los siete productos de OYL. |
| Entrega | Se preparan únicamente los fuentes Android con Gradle, y un ZIP alternativo del proyecto completo con el mismo backend. Ninguno incluye APK, App Bundle ni claves privadas del equipo. |

Para las pruebas internas de interfaz usamos un emulador Android 11 y una clave separada de la del equipo. Se emplearon el compilador de Kotlin y el plugin Compose del proyecto, D8, Java de Room, aapt2 y los recursos y dependencias sin modificar de la versión previa. El wrapper no pudo descargar Gradle 8.13 en este entorno; no se atribuye una ejecución satisfactoria de Gradle en Windows ni la firma definitiva a estas pruebas.

## Recorrido de interfaz observado

- 4.1.1 guardó una dirección distinta del valor predeterminado y los tres servicios respondieron.
- actualización de ensayo a 4.1.2 sin desinstalar ni borrar datos.
- Bienvenida sin botón de conexión.
- Inicio de sesión sin botón de conexión.
- una pulsación larga abrió el ajuste; conservó la dirección elegida en 4.1.1 y llegó a los tres servicios.
- inicio de sesión y catálogo de Almendras contra el Gateway real en esa dirección.
- se cambió de dirección desde el catálogo, se validó y quedó guardada tras cerrar y reabrir.

## Qué probar en el equipo de la defensa

1. Compilar **aula** y firmarla con la clave del equipo siguiendo `ABRIR_EN_ANDROID_STUDIO.md`; verificar el certificado con apksigner en Windows. La clave del ensayo no se entrega.
2. En el Samsung A07, abrir bienvenida y login. No debe haber un botón de configuración. Si se actualizó con la misma clave y se conserva la dirección de la tienda, el catálogo debe seguir funcionando. Para una instalación nueva, mantener pulsado el texto OYL FRUTOS SECOS en la barra superior y guardar la URL del PC.
3. En la red de la defensa, iniciar sesión, hacer una compra de una unidad y comparar el stock y el número de pedido con el backend. Verificar también el escáner usando la cámara física.
4. La cobertura Android no se volvió a medir. Los informes históricos de cobertura (4.1.0) y la lista de pruebas anteriores (4.1.1) están identificados en `evidencias/LEER_PRIMERO.md`. Las pruebas de las versiones previas no se cuentan como ejecuciones nuevas.
