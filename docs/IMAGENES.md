# Procedencia y recursos de producto

Las siete fotografías referenciales se generaron con la herramienta integrada ImageGen de OpenAI para este proyecto; no se descargaron fotografías de terceros. No representan envases ni lotes comerciales reales y no se atribuyen a un fotógrafo humano. No hay licencias de terceros de esas fotografías que citar. Se suministran como recursos del proyecto; no se promete exclusividad jurídica de contenido generado.

Formato: PNG RGBA, 512 × 512, canal alfa real (comprobado rango 0–255), compresión PNG optimizada sin perder la transparencia. Se redujo la resolución para uso móvil y se revisaron visualmente los siete productos. Ubicación: `android/app/src/main/res/drawable-nodpi/`.

| Archivo | Sujeto del prompt | Bytes |
|---|---|---:|
| almendras.png | raw brown almonds | 398,541 |
| nueces.png | shelled walnut halves | 389,169 |
| mix_premium.png | premium trail mix of almonds walnut halves hazelnuts and raisins | 382,824 |
| pasas.png | dark seedless raisins | 420,395 |
| chia.png | tiny black and grey chia seeds | 417,986 |
| cranberries.png | red dried cranberries | 385,770 |
| avellanas.png | shelled hazelnuts with a few split kernels | 348,054 |

Prompt compartido (una generación individual por sujeto de la tabla):

> Use case: product-mockup. Create ONE individual Android catalog asset, square photorealistic studio food photograph of a small centered generous mound of [SUBJECT]. Consistent catalog style: three-quarter overhead view, soft diffuse daylight, natural warm colors, crisp realistic textures. Isolated on genuinely transparent background, ample clear margins, no bowl, no plate, no text, no labels, no watermark. The product fills 75 percent of square canvas.

`producto_predeterminado.xml` es un vector neutro de hoja creado para esta entrega, utilizado para nombres desconocidos; no es una foto inventada de un producto.

Mapeo: `ProductImages.kt` normaliza acentos, mayúsculas y espacios, y compara el nombre completo. No depende de IDs numéricos del seed y evita asignar Almendras a «Chocolate de almendras». Las fotos base64 del administrador tienen prioridad en `ProductPicture`; una imagen inválida cae al recurso referencial/predeterminado sin romper la pantalla. Se conserva el contrato `imageUri` del backend. Se usa el mismo componente en catálogo, destacados, detalle, carrito, productos e inventario administrativos y vista previa del formulario.

El wrapper Gradle se obtuvo del repositorio oficial `gradle/gradle`, etiqueta `v8.13.0`; conserva su cabecera Apache 2.0 y copyright. Leaflet 1.9.4 se incluye localmente con su licencia BSD de dos cláusulas en `android/app/src/main/assets/leaflet/LICENSE`. Su JavaScript y CSS proceden de `https://unpkg.com/leaflet@1.9.4/dist/`. No se cambiaron las licencias de dependencias. OpenStreetMap conserva atribución visible «© OpenStreetMap contributors»; sus mapas se descargan del proveedor y no se incluyen mosaicos en el ZIP.
