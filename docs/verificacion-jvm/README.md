# Verificación auxiliar JVM

Ejecutar `mvn test` en esta carpeta con Maven y JDK. Contiene copias de los archivos puros de lógica y tests usados en la verificación. `PlatformStubs.kt` sólo proporciona R y CartLine para compilar sin Android; nunca se incluye en app. Para cambios futuros volver a copiar los auxiliares desde android/app/src antes de ejecutar. Esto no valida Compose, Room, recursos Android ni compilación APK. La comprobación definitiva de Android es gradlew.bat :app:assembleDebug :app:testDebugUnitTest en android/.
