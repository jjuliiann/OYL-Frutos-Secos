# Backend de OYL: inicio manual por CMD

Abre CMD dentro de esta carpeta. Escribe una línea a la vez y espera su **OK**. Solo se inicia el servicio que nombras.

```cmd
oyl.bat eureka
oyl.bat usuarios
oyl.bat productos
oyl.bat pedidos
oyl.bat gateway
oyl.bat estado
```

Cuando aparezca **BACKEND LISTO**, muestra `curl.exe http://localhost:8080/api/products`. El panel de Eureka se abre manualmente en http://localhost:8761/. Al terminar, antes de cerrar la ventana, ejecuta `oyl.bat detener`.

El CMD queda disponible después de cada inicio. La JVM conserva la consola y Spring también escribe su registro en `logs/<módulo>.log`. No hay tarea de inicio de Windows ni se abre el navegador. `oyl.bat` o `run-local.bat` sin argumentos solo imprimen ayuda. `oyl.bat java` muestra el Java seleccionado. `estado.bat` y `stop-local.bat` son atajos para estado y detener.

`scripts/servicios.ps1` es el código del mando de CMD. `scripts/cli/cierre-cmd.jar` añade al proceso de Java una espera por una marca de cierre con nombre único en la carpeta local de esta copia. Al ejecutar `detener`, cada JVM recibe su propia marca, termina normalmente y ejecuta el cierre de Spring y H2. No se publica una ruta de apagado HTTP. El código (`CierreConsola.java`) y el manifiesto del pequeño JAR se incluyen junto a él. Los cinco JAR de Spring originales se mantienen intactos.

Para compilar los microservicios usa `compilar-backend.bat`. Para una prueba de solo lectura usa `python scripts\comprobar_api.py` con los servidores en marcha. La opción `--compra` crea un pedido y modifica stock; úsala sobre una base de evaluación. Ver [la guía rápida](../ABRIR_BACKEND_CMD.txt), [el README principal](../README.md) y [las rutas](../RUTAS.md).
