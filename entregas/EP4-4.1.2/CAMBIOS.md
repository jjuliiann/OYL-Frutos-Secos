# Cambios de OYL Frutos Secos 4.1.2

Partimos de una copia de `OYL_Frutos_Secos_EP4_CMD_4.1.1.zip`. El ZIP anterior conserva la entrega original.

| Archivo | Cambio |
|---|---|
| `ui/bienvenida/WelcomeScreen.kt` y `ui/auth/LoginScreen.kt` | Eliminamos el botón visible de configuración del servidor y el hueco que ocupaba. |
| `ui/navigation/OylNavigation.kt` | El título de la barra mantiene el mismo texto y estilo. En aula/debug una pulsación larga abre el ajuste, incluso después del login. Hay una acción accesible equivalente. En release el título es estático. |
| `ui/bienvenida/GatewayDialog.kt` | El diálogo «Servidor de aula» se muestra por petición de la barra. La prueba de Usuarios, Productos y Pedidos, la validación de URL y el guardado se conservan. |
| `android/app/build.gradle.kts` | Versión 4.1.2, código 15. La variante aula conserva el paquete `cl.oyl.mobile.aula`. |
| Documentación | Se actualizaron arranque, rúbrica, verificaciones e instrucciones para compilar y firmar. Los documentos históricos se conservan identificados por versión. |

La entrega no incluye APK, claves privadas ni la utilidad de firma de la versión anterior. El backend, sus archivos de arranque, los contratos Retrofit, Room, los recursos de la tienda y el manejo de sesiones no se editaron. No se incorporó detección automática de IP ni una dirección de red particular.
