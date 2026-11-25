# OYL Frutos Secos 4.1.1-aula

Base: `OYL_Frutos_Secos_EP4_CMD_Manual.zip`, conservada por separado. Cambio solicitado: retirar la sección «Información de API externa». La conexión con la tienda, sus datos y el arranque manual por CMD se mantienen.

| Archivo bajo `android/app/` | Qué cambió |
|---|---|
| `src/main/java/cl/oyl/mobile/ui/inicio/DashboardScreen.kt` | Se retiró el botón de los tres menús. El resto del panel conserva el mismo código. |
| `…/ui/navigation/Page.kt`, `…/ui/navigation/NavigationMotion.kt`, `…/ui/navigation/OylNavigation.kt` | Se retiraron el destino, su posición en las animaciones y la llamada a la pantalla. Las demás transiciones continúan activas. |
| `…/MainActivity.kt` | La actividad ya no crea ni pasa el ViewModel de esa pantalla. |
| `…/ui/externa/ExternalApiScreen.kt` y `…/viewmodel/externa/ExternalApiViewModel.kt` | Se eliminaron la pantalla nutricional y su estado. |
| `…/data/remote/OylApi.kt` | Se eliminó el cliente de Open Food Facts y sus modelos. Se conservaron el cliente Retrofit de OYL, la comprobación del Gateway y las 14 operaciones de la tienda. |
| `build.gradle.kts` | La versión pasó de código 13 a 14. Se mantiene el identificador anterior de la variante aula para poder instalar encima con la misma firma. |

En la entrega se reemplazó la APK de muestra anterior por la compilación nueva sin firmar, lista para firmarse localmente. Se añadió `apk/FIRMAR_PARA_EL_SAMSUNG.cmd` y se actualizaron README, pruebas, rúbrica, hashes e instrucciones de Windows. No se distribuye un almacén de claves. El detalle técnico anterior está en `documentacion/historial/CAMBIOS_EP4_CMD_4_1_0.md`.
