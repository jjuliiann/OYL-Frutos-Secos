# OYL Frutos Secos

Somos Julián Martínez y Lukas Gómez, estudiantes de Ingeniería en Informática de Duoc UC. Desarrollamos este proyecto para la asignatura Desarrollo de Aplicaciones Móviles, sección 004D, con el profesor Cristian Gómez.

OYL Frutos Secos es una aplicación móvil para explorar y comprar frutos secos. La app se conecta a un backend propio para gestionar productos, inventario y pedidos. En esta versión mejoramos algunas partes de la aplicación y conservamos las funciones que ya tenía el proyecto.

## Funcionalidades

- Inicio de sesión para administrador y clientes, y registro de clientes.
- Catálogo de productos y pantalla de detalle.
- Carrito guardado localmente y confirmación de pedidos conectada al backend.
- Descuento de inventario al comprar.
- Administración de productos: crear, editar, activar, desactivar y agregar fotografías.
- Gestión de estados de pedidos.
- Lectura de códigos QR desde imágenes y notificaciones locales.
- Edición del perfil.
- Avatar circular desde cámara o galería, guardado para volver a usarlo después.
- Mapa de puntos de retiro con OpenStreetMap y opción para abrir direcciones externas.
- Imágenes PNG de almendras, nueces, mix premium, pasas, chía, cranberries y avellanas.
- Imagen predeterminada para productos sin fotografía.
- Validaciones de formularios y mejoras en la visualización de estados, pestañas y fechas.

## Tecnologías utilizadas

- **Android:** Kotlin, Jetpack Compose, Material 3, ViewModel, StateFlow y navegación.
- **Datos locales:** Room sobre SQLite.
- **Comunicación con el backend:** Retrofit y Gson.
- **Backend:** Java, Spring Boot, Spring Data JPA y H2.
- **Mapa:** OpenStreetMap y Leaflet dentro de un WebView.
- **Lectura de QR:** Google ML Kit.

## Estructura del proyecto

```text
android/     Aplicación Android. Esta carpeta se abre en Android Studio.
backend/     API Spring Boot.
docs/        Informe de cambios, pruebas, rúbrica y evidencias.
respaldos/   Copias de los archivos originales del proyecto.
