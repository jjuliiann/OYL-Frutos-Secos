# Abrir OYL y generar nuestra APK en Windows

OYL Frutos Secos 4.1.2 · Julián Martínez y Lukas Gómez · Desarrollo de Aplicaciones Móviles, 004D.

## 1. Abrir el código

Extrae el ZIP en una carpeta nueva. En Android Studio, **File > Open** y selecciona la carpeta **android** que contiene `settings.gradle.kts` y `gradlew.bat`. Espera la sincronización con Gradle JDK 21, Android SDK 35 y Build Tools 35.0.0. Si el backend anterior sigue activo, deja abierta esa CMD: funciona con este proyecto. No se incluye una APK ni un almacén de claves.

## 2. Firmar en tu PC

1. Selecciona **Build > Generate Signed App Bundle or APK > APK > Next**, módulo **app**.
2. Si vas a actualizar la app ya instalada, usa **Choose existing** o el selector de archivos para abrir **el mismo .jks y la misma clave** con los que firmaste esa instalación. Para la defensa usamos el alias `aula`. El alias es el nombre de la clave; la contraseña es la que eligió el equipo.
3. Si vas a crear otra identidad de firma, en **Create new / New Key Store** haz clic en la carpeta de **Key store path**, elige una ubicación privada fuera del proyecto (p. ej., Documentos) y guarda con un nombre nuevo como `oyl-aula-nueva.jks`. En **Password** y **Confirm** escribe una contraseña propia. Cambia `key0` por `aula` en **Alias**; introduce y confirma la contraseña de la clave (puede ser la misma). Puedes dejar **Validity** en 25 años, poner **Julián Martínez y Lukas Gómez** en **First and Last Name** y **CL** en **Country Code**. Guarda el almacén y la contraseña para las próximas versiones.
4. Pulsa **Next**, elige **aula** en **Build Variants** y pulsa **Create**. Cuando termine, usa **Locate** para encontrar la APK firmada que generó tu Android Studio. Los ZIP de esta revisión contienen las fuentes.
5. Para enseñar la firma al profesor, abre una CMD en la carpeta del archivo generado. Si se llama `app-aula.apk` y tienes el SDK en la ubicación habitual:

```cmd
"%LOCALAPPDATA%\Android\Sdk\build-tools\35.0.0\apksigner.bat" verify --verbose --print-certs "app-aula.apk"
```

Muestra **Verifies** y la huella SHA-256 de tu certificado. Si creaste otro .jks, la firma cambia aunque uses el mismo alias: esa APK no puede actualizar encima de una instalación con la firma anterior. Puedes probar en un emulador limpio sin borrar el Samsung.

## 3. Conectar el Samsung o el emulador

Al abrir la app, el cliente ve la bienvenida y el login sin un botón técnico. OYL usa la dirección ya guardada en esa instalación. En el emulador, el valor inicial es `http://10.0.2.2:8080/api/`.

**Samsung A07 con una instalación nueva, o si cambió la IP del PC:** conecta ambos equipos a la misma red. En la CMD del PC ejecuta `ipconfig` y toma la IPv4 del adaptador conectado. En la app **aula**, mantén pulsado aproximadamente un segundo el texto **OYL FRUTOS SECOS** de la **barra verde superior**. No es el logo grande de bienvenida. Se abre **Servidor de aula**: escribe `http://IP_DEL_PC:8080/api/` con el número real del PC, pulsa **Probar conexión** y después **Usar esta conexión**. La dirección queda guardada para las siguientes aperturas. El gesto funciona también en login o después de iniciar sesión. Un toque normal no abre el ajuste.

Para comprobar la conectividad del Wi-Fi puedes abrir `http://IP_DEL_PC:8080/api/products` en el navegador del Samsung. Debe responder el catálogo. Al cambiar de dirección pueden cambiar la sesión y el carrito visibles: las cuentas y los datos locales se separan por dirección de servidor. La aplicación no busca PCs en la red.

El ajuste existe en **aula** y **debug**. La variante **release** no tiene este gesto. Para preparar una instalación nueva con un servidor conocido, puedes fijar su URL antes de compilar en un renglón de `android/local.properties`, conservando el valor de `sdk.dir`. Por ejemplo: `oylApiBaseUrl=http://192.168.1.20:8080/api/`; reemplaza la IP de ejemplo por la real o usa el dominio HTTPS de tu servidor. Para la defensa con redes cambiantes, usa **aula** y conserva el valor de compilación habitual.
