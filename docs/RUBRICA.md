# Contraste con rúbrica y caso

Fuente: PDF «Ev. Parcial 2_Estudiante_Encargo_DSY1105(1)», páginas 3-6; caso «036 SEC004D Caso OYL Frutos Secos(1)», páginas 1-3. Evaluación por evidencia de código y comprobaciones disponibles; no es una nota garantizada. No se adjuntaron guías adicionales distintas de estos dos PDF.

| Indicador | Peso | Evidencia concreta | Estado y evidencia que falta |
|---|---:|---|---|
| IE 2.1.1 Interfaz y navegación | 15% | MaterialTheme Material 3, Scaffold, Page/BackHandler en MainActivity; filtros con scroll; estados de pedido completos y fecha legible | Implementado en código, pendiente recorrido visual a 320/360/411 dp y fuente ampliada. No se sustituyó la navegación original por Navigation Compose. |
| IE 2.1.2 Formularios con validaciones, íconos y errores | 15% | FormField con isError, supportingText e ícono ErrorOutline; Validation aplicada a acceso, registro, perfil, producto, contacto y cantidad | Error visual de correo comprobado en emulador; ejecutar los demás casos inválidos/válidos y comprobar errores HTTP (alias duplicado, credenciales, stock). El ID manual de QR tiene error contextual, no un formulario de negocio. |
| IE 2.2.1 Validación central y desacoplada | 10% | Validation.kt con reglas puras; ViewModel reutiliza reglas de acceso/registro/perfil/producto y conserva reglas de stock/pedido | Mejora parcial: resultados lógicos compartidos, pero algunos estados de formulario siguen en composables. No se declara desacoplamiento total ni se reorganiza toda la app. |
| IE 2.2.2 Animaciones útiles | 10% | AnimatedVisibility con fadeIn + slideInVertically en WelcomeScreen original; indicador de carga real del mapa | Parcial: existe animación de bienvenida, pero faltan evidencias de fluidez/retroalimentación en otros flujos y transiciones entre pantallas. No se añadieron animaciones ficticias para marcar cumplimiento. |
| IE 2.3.1 Modularidad y persistencia local | 15% | OylViewModel + StateFlow, LocalDb/DAO/entidades Room; Api.kt Retrofit, pantallas separadas; auxiliares de imagen/foto/mapa/validación/fechas | Base MVVM funcional por inspección; capas UI/lógica/datos están en archivos distintos pero un mismo paquete y ViewModel accede a API/DAO directamente. Persistencia de carrito y avatar tras cierre/reapertura confirmada manualmente; sin afirmar arquitectura completamente desacoplada. |
| IE 2.3.2 Colaboración real | 20% | No se incluyeron enlaces/historial Git ni tableros verificables en los adjuntos | Pendiente de los estudiantes: repositorio público con acceso docente, commits reales de ambos, issues/tareas y planificación Trello. Un ZIP no acredita participación. |
| IE 2.4.1 Dos recursos nativos seguros y funcionales | 15% | Cámara por Activity Result; Photo Picker; notificaciones con permiso POST_NOTIFICATIONS; QR ML Kit; enlace externo de mapas | Cámara y notificación local probadas; galería, QR y persistencia del avatar también confirmados. Pendientes permisos denegados y cancelación; el nivel máximo no se certifica sólo con el recorrido exitoso. |

## Requisitos del caso OYL

| Requisito | Evidencia y límite |
|---|---|
| Android nativo, Kotlin y Compose | Conservados en el Android original. App ejecutada por el estudiante; falta log reproducible de compilación, tests y lint Android. |
| Catálogo por categoría y producto con imagen | Product, catálogo filtrado, detalles, PNG locales y foto administrativa. Caché Room original conservada. |
| Cliente solicita compras | Carrito Room + Retrofit createOrder; backend recalcula importe y descuenta inventario con bloqueo pesimista. Las pruebas backend comprueban stock y rechazo de sobreventa. |
| Administrador gestiona productos, precios, inventario y estados | Endpoints originales y pantallas conservados; se amplían etiquetas. Contratos HTTP y operaciones del backend comprobados según informe de pruebas. |
| Spring Boot como fuente central exclusiva | Api.kt mantiene 10.0.2.2:8080/api; sin mock del servidor ni sustitución de pedidos. No hay sincronización automática implementada con WhatsApp/Mercado Libre. |
| Persistencia y baja señal | Room para datos locales; H2 archivo para servidor. Compras requieren servidor, mapa requiere internet. Validar cambio online/offline en emulador. |
| Avisos de nuevos pedidos | Hay aviso local al comprar y notificación de demostración. No hay push remoto al administrador por nuevos pedidos. Pendiente si el docente exige alertas automáticas entre dispositivos. |
| Datos sintéticos | Conservados admin y puntos ficticios; fotos referenciales generadas. No se inventan direcciones de sucursales reales. |

## Evidencias para completar antes de entregar

Actualización 18/10/2026: varios recorridos exitosos ya están acreditados en `ACEPTACION_MANUAL_2026-10-18.md`. La lista siguiente es la cobertura completa deseada, no una afirmación de que todos sus pasos sigan pendientes.

1. Video de cliente: registro → catálogo → carrito → pedido → stock actualizado → historial.
2. Video de administrador: crear/editar producto, foto, desactivar/reactivar, inventario y estados completos.
3. Fotos PNG correctas en catálogo, detalle, carrito y administración; producto desconocido con imagen neutra.
4. Avatar por ambas alternativas, cancelación, reinicio y cambio de usuario.
5. Mapa cargado, tres puntos originales seleccionables, sin internet con mensaje, reintento y Cómo llegar.
6. Validaciones por campo, permiso de notificación y QR desde galería/cámara.
7. Compilación y pruebas Android, persistencia Room y tamaño de fuente/pantalla.
8. Capturas/enlaces reales de trabajo colaborativo, nombres completos y cumplimiento de AVA y fecha de corte.


## Entrega exigida por la pauta (página 3)

- Repositorio GitHub público con acceso del docente y del equipo, README con integrantes y ejecución, commits técnicos y participación real.
- Planificación Trello o similar con distribución de tareas. Este indicador pesa 20%; no se ha aportado evidencia verificable.
- Un integrante sube el código fuente al AVA grupal, con los mismos elementos que el repositorio.
- Cada integrante activa el enlace AVA individual con nombre, apellido, número de equipo y aplicación/contexto.
- Confirmar fecha de corte del docente. La pauta prohíbe cambios en el repositorio después de la entrega y antes de la evaluación; indica nota 1.0 en los incumplimientos detallados en esa página. Preparar todo antes del cierre.

No se asigna una nota estimada: la pauta pondera implementación, evidencia y colaboración. Animaciones y desacoplamiento siguen con alcance parcial; los casos de error aún requieren pruebas.
