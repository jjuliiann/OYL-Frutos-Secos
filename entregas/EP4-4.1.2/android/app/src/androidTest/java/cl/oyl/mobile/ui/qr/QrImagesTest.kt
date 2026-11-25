package cl.oyl.mobile.ui.qr

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.TimeUnit
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class QrImagesTest {
    @Test fun bundledImagesAreDecodedByMLKit() {
        val context=ApplicationProvider.getApplicationContext<Context>()
        val scanner=BarcodeScanning.getClient()
        try {
            for (sample in qrSamples) {
                val bmp=BitmapFactory.decodeResource(context.resources,sample.resource)
                val codes=Tasks.await(scanner.process(InputImage.fromBitmap(bmp,0)),20,TimeUnit.SECONDS)
                assertTrue(sample.label,codes.any { it.rawValue==sample.payload })
            }
            val blank=Bitmap.createBitmap(320,320,Bitmap.Config.ARGB_8888).apply { eraseColor(android.graphics.Color.WHITE) }
            assertTrue(Tasks.await(scanner.process(InputImage.fromBitmap(blank,0)),20,TimeUnit.SECONDS).isEmpty())
        } finally { scanner.close() }
    }
}
