import java.lang.instrument.Instrumentation;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;

/** Control de cierre local: permite a Spring y H2 ejecutar sus hooks sin detener otras JVM. */
public final class CierreConsola {
    public static void premain(String opciones, Instrumentation ignorada) {
        var ruta = Path.of(System.getProperty("oyl.cmd.stop")).toAbsolutePath();
        var vigilar = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    if (Files.isRegularFile(ruta, LinkOption.NOFOLLOW_LINKS) && Files.deleteIfExists(ruta)) {
                        System.out.println("OYL CMD: cierre solicitado; Spring esta cerrando sus recursos.");
                        System.exit(0);
                    }
                    Thread.sleep(400);
                } catch (InterruptedException fin) {
                    Thread.currentThread().interrupt();
                } catch (java.io.IOException error) {
                    System.err.println("OYL CMD: no se pudo leer la marca de cierre: " + error.getMessage());
                    return;
                }
            }
        }, "oyl-cierre-consola");
        vigilar.setDaemon(true);
        vigilar.start();
    }
}
