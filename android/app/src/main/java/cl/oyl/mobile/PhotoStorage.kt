package cl.oyl.mobile

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.util.Base64
import java.io.ByteArrayOutputStream

// Se copia el contenido a Room: no conservamos permisos ni URI de galería temporales.
fun encodePhoto(bitmap: Bitmap): String? = try {
    val side = minOf(bitmap.width, bitmap.height)
    val square = Bitmap.createBitmap(bitmap, (bitmap.width - side) / 2, (bitmap.height - side) / 2, side, side)
    val scaled = Bitmap.createScaledBitmap(square, 320, 320, true)
    val output = ByteArrayOutputStream()
    scaled.compress(Bitmap.CompressFormat.JPEG, 80, output)
    "data:image/jpeg;base64," + Base64.encodeToString(output.toByteArray(), Base64.NO_WRAP)
} catch (_: Exception) { null }

fun readPhoto(context: Context, uri: Uri): String? { return try {
    val bitmap = if (Build.VERSION.SDK_INT >= 28) {
        ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri)) { decoder, info, _ ->
            val ratio = maxOf(info.size.width, info.size.height) / 640f
            decoder.setTargetSize((info.size.width / ratio.coerceAtLeast(1f)).toInt().coerceAtLeast(1),
                (info.size.height / ratio.coerceAtLeast(1f)).toInt().coerceAtLeast(1))
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
        }
    } else {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        val opts = BitmapFactory.Options()
        while (maxOf(bounds.outWidth, bounds.outHeight) / opts.inSampleSize.coerceAtLeast(1) > 640) {
            opts.inSampleSize = opts.inSampleSize.coerceAtLeast(1) * 2
        }
        context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opts) }
            ?: return null
    }
    encodePhoto(bitmap)
} catch (_: Exception) { null }
}
