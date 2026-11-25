# Comparación con los requisitos disponibles

Se utiliza la descripción de Evaluación Parcial 2 compartida en el chat. No se adjuntó en
esta solicitud una rúbrica completa con indicadores/puntajes: no se asigna nota ni se asegura
cumplimiento total. Las evidencias siguientes corresponden al código, no a una validación visual.

| Requisito | Evidencia de implementación | Qué falta acreditar |
| --- | --- | --- |
| Material 3 | MaterialTheme, Scaffold, Card, campos y controles Material3; nueva bienvenida | Capturas y evaluación visual en Android |
| Formularios validados | Validation.kt y FormField, errores por campo, validación de repositorio | Demostrar registro inválido/duplicado y edición |
| Navegación funcional | MainActivity y enum Page, BackHandler, AnimatedContent | Recorrido completo y recreación de Activity |
| Gestión de estado | OylViewModel, StateFlow y flujos Room; rememberSaveable para página | Demostrar actualización de stock y pedidos |
| Almacenamiento local SQLite | LocalDb, StoreDao, entidades y LocalRepository.withTransaction | Ejecutar tests Room y mostrar Database Inspector |
| Recursos nativos | Activity Result cámara/galería, notificaciones, Intent Maps y WebView | Pruebas reales y permisos API 33+ |
| Animaciones | AnimatedButtons, bienvenida, transiciones de pantallas, detalles del mapa y línea QR | Mostrar presión mantenida, navegación y selección |
| Trabajo en pareja | README identifica a Julián Martínez y Lukas Gómez | Historial Git, organización y aportes reales de ambos |
| Arquitectura/separación | Modelos, ViewModel, repositorio, DAO y pantallas separados por archivo | Desde 3.2 hay carpetas model, viewmodel, data y ui por función; revisar ESTRUCTURA_3_2.md |
| QR sin teléfono | QrScreen y PNG empaquetados; scanner ML Kit procesa bitmap | Ejecutar QrImagesTest y demostrar emulador |
| Mapa con puntos existentes | StoredLocation SQLite y Leaflet, callback de selección y enlace de destino | Demostrar conexión, error/reintento y destino correcto |

Los ejemplos sirven de referencia didáctica. El ejemplo de animaciones usa Views/ObjectAnimator;
esta entrega usa las APIs de animación de Compose para las mismas propiedades. El ejemplo de
SQLite usa Room: se conserva ese enfoque. El ejemplo de QR usa Code Scanner; se conserva ML Kit
para admitir fuentes de imagen sin cámara. No se mezclaron Activities de ejemplo en OYL.

## Evidencia nueva 3.4

- Cámara nativa: PreviewView/ImageAnalysis y ML Kit, control de ciclo de vida y permisos. Implementado; pendiente demostrar en Android.
- Navegación externa: intent-filter y resolución de producto real, válido/inexistente/desactivado. Implementado con esquema propio; pendiente probar cold/warm y cámara externa compatible. HTTPS verificado pendiente de dominio y certificado.
- Tests de formatos: 6 tests JVM ejecutados; no sustituyen evidencia visual de la cámara.

## Organización MVVM actual (3.6)

Modelos por funcionalidad, ViewModels reales para acceso/productos/inventario/carrito/checkout/pedidos/perfil/mapa/QR/notificaciones y coordinador de avisos/carga. MainActivity usa ViewModelProvider; OylNavigation conecta estados/callbacks. Room y repositorio se comparten mediante OylGraph. Los campos visuales y contratos de cámara se conservan en Compose.

Evidencia: MVVM_3_6.md, 20 tests JVM y comprobación parcial de tipos. Pendiente compilar Android, ejecutar tests Room y demostrar flujo completo/rotación/permisos. No se asegura coincidencia con una instrucción verbal del profesor ni se asigna nota sin rúbrica completa.

## Requisitos de Evaluación Parcial 4 y defensa EP5

Se revisaron las guías adjuntas `Ev. Parcial 4_Encargo_Estudiante_ DSY1105.pdf` y `Ev. Parcial 5_Estudiante_Presentación_ DSY1105.pdf`. La descripción de EP2 anterior se mantiene como referencia del hito original; para el avance actual aplican también estos indicadores de EP4/EP5.

| Indicador / requisito | Evidencia presente en esta copia | Estado y evidencia que falta |
|---|---|---|
| App integrada con microservicios | Retrofit `OylApi`, ViewModels por flujo y configuración de Gateway | Implementación conectada en código; levantar servicios y demostrar CRUD en emulador/celular |
| Microservicios Spring Boot con base activa | Usuarios, Productos, Pedidos, Gateway y `security-core`; H2 por servicio en archivo | Código/configuración presentes; ejecutar Maven, comprobar endpoints y persistencia |
| API externa consumida vía Retrofit | Pantalla Open Food Facts y cliente Retrofit separado | Código presente; probar con internet y mostrarlo durante la defensa |
| Pruebas unitarias, cobertura mínima 80% | Tests para tokens, reglas de producto, pedidos y controlador de usuario | No se pudo ejecutar ni medir cobertura; faltan pruebas de integración/ViewModel y reporte de cobertura |
| APK firmado y `.jks` | Instrucciones en README | No se generó APK firmado ni keystore; realizar firma en Android Studio y guardar ambos según pauta |
| README y documentación técnica | `README.md`, `backend/README.md`, `EP4_INTEGRACION.md` e informe | Presente; completar con endpoint probado, versión y evidencia una vez ejecutado localmente |
| GitHub y Trello con aportes por integrante | No se configuraron desde esta copia | Pendiente: publicar repositorio, commits de ambos y tablero con tareas/evidencias |
| Defensa individual EP5 | Guion en `EP4_INTEGRACION.md` | Pendiente practicar inicio, arquitectura, APIs, pruebas, APK, colaboración y modificación en vivo |

El código por sí solo no acredita que una prueba pasó o que el servicio arrancó. La copia se validó estructuralmente, pero este entorno carece de Maven y no pudo descargar Gradle 8.13. Ver `INFORME_EP4.md` para los comandos y el estado exacto.


## Eureka

Eureka Server y el registro de los microservicios están incorporados. Evidencia pendiente: ejecutar el backend y mostrar en `http://localhost:8761` los servicios registrados.
