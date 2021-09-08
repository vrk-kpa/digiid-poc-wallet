package fi.dvv.digiid.poc.wallet.ui.verifier

import android.graphics.ImageFormat
import android.media.Image
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer

class ZxingQrCodeAnalyzer(
    private val onQRCodeDetected: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private val reader = MultiFormatReader()

    private fun isSupportedFormat(image: Image): Boolean {
        return when (image.format) {
            ImageFormat.YUV_420_888, ImageFormat.YUV_422_888, ImageFormat.YUV_444_888 -> image.planes.size == 3
            else -> false
        }
    }

    @androidx.camera.core.ExperimentalGetImage
    override fun analyze(image: ImageProxy) {
        image.image?.takeIf(::isSupportedFormat)?.let {
            kotlin.runCatching {
                val buffer = it.planes[0].buffer
                val bytes = ByteArray(buffer.capacity())
                buffer.get(bytes)

                val source = PlanarYUVLuminanceSource(
                    bytes,
                    it.width,
                    it.height,
                    0,
                    0,
                    it.width,
                    it.height,
                    false
                )
                val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
                val result = reader.decode(binaryBitmap)

                if (result.barcodeFormat == BarcodeFormat.QR_CODE) {
                    onQRCodeDetected(result.text)
                }
            }
        }

        image.close()
    }
}