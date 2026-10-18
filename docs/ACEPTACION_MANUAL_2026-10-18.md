# Registro de aceptación manual — 18/10/2026

Pruebas ejecutadas por Julián en su emulador Android y backend local desde CMD en Windows. Resultados revisados mediante capturas y confirmaciones en el chat. No son pruebas instrumentadas ejecutadas por el asistente. No se registró la versión efectiva del JDK, API del emulador ni un log de compilación Android.

## Resultados confirmados

| Función | Evidencia y alcance |
|---|---|
| Catálogo con PNG | Capturas iniciales muestran almendras, nueces y mix; funcionamiento general confirmado. Falta revisión individual de los siete PNG en todas las vistas. |
| Mapa integrado | Captura con mosaicos reales de Santiago y marcador Centro; usuario confirmó selección de puntos, controles y Cómo llegar. No se probó modo sin conexión. |
| Avatar desde galería | Selector observado; foto circular y conservación tras cerrar/reabrir confirmada. |
| Avatar desde cámara | Nueva foto circular observada; usuario confirmó que permanece tras cerrar/reabrir. |
| Carrito local | Usuario confirmó conservación de producto y cantidad tras cerrar/reabrir. |
| Pedido y estados | Pedido #1, Mix Premium × 1, $3.990: capturas de Pendiente de validación, Confirmado, Preparado y Entregado en administración. Captura de Mis pedidos del cliente muestra el mismo pedido Entregado. |
| Legibilidad de pedidos | Fechas 18/10/2026 12:59, acciones completas y pestañas desplazables observadas. No equivale a verificar todos los tamaños de pantalla/fuente. |
| Descuento de stock | Usuario reportó Nueces de 18 a 17 tras comprar una unidad. |
| Activación de catálogo | Captura de búsqueda sin resultados al desactivar Nueces; usuario confirmó reaparición con stock 17 tras activar. |
| Validación de correo | Captura de correo@invalido con borde rojo, ícono y mensaje; después correo válido sin error. No se observó una lectura independiente del servidor durante el intento inválido. |
| Perfil | Se observan nombre, correo, teléfono y dirección; no se acreditó por separado modificar y persistir cada campo. |
| QR | Usuario confirmó reconocimiento del QR de almendras desde galería. |
| Notificación | Usuario confirmó recepción de la notificación local de demostración. No acredita push remoto de pedidos. |
| Producto sin foto | Captura de Producto Prueba con imagen predeterminada, precio $1.000 y stock 2. |
| Edición de producto con foto | Usuario confirmó cambio a $1.200 y fotografía visible desde cliente. |
| Persistencia del backend | Configuración mostrada: H2 archivo y ddl-auto=update. Tras detener y arrancar desde la misma carpeta, usuario confirmó pedido #1 Entregado y stock 17. |

## Pendientes concretos

- Guardar log de assembleDebug, testDebugUnitTest y lintDebug; identificar JDK y API del dispositivo. La app ejecutada por el estudiante es evidencia de funcionamiento, no un registro de estos comandos.
- Probar registro de una cuenta nueva, credenciales incorrectas y usuario duplicado en esta entrega.
- Probar validaciones de todos los campos de registro, perfil, producto y movimientos de inventario; errores HTTP y rechazo de stock insuficiente.
- Probar desconexión/reconexión del mapa y backend, reintento y conservación del carrito si falla una compra.
- Probar cancelación de cámara/galería, permisos denegados, imagen no disponible y separación de datos entre usuarios.
- Revisar siete PNG en catálogo, detalle, carrito y administración; fuente ampliada y otras dimensiones.
- Demostrar animaciones y navegación en video. La animación de bienvenida existe en código; su fluidez y suficiencia para el nivel máximo de la pauta no están acreditadas.
- Incorporar nombres completos, sección/equipo y enlaces reales a GitHub/Trello; registrar participación auténtica de ambos estudiantes.

## Capturas incluidas

En evidencias/manual-2026-10-18 se incluyen capturas aportadas por el usuario del mapa, pedido administrativo entregado, pedido del cliente y producto sin foto. Las confirmaciones verbales se documentan como tales; no se transforman en resultados de pruebas automáticas.

## Alcance de esta actualización

Sólo documentación y evidencias. El código Android, backend, configuración y recursos son idénticos a la versión Mapa v2 probada. No hace falta reinstalar para obtener estas mejoras documentales. La base de datos generada en el PC del estudiante no está dentro del ZIP: conservar backend/data en ese PC y respaldarla con el backend detenido.
