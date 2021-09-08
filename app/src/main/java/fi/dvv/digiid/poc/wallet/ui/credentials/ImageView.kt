package fi.dvv.digiid.poc.wallet.ui.credentials

import android.graphics.Bitmap
import android.graphics.Color
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import fi.dvv.digiid.poc.wallet.R
import kotlin.math.min

@BindingAdapter("qrcode")
fun ImageView.renderQRCode(contents: String?) {
    if (contents == null) return

    val size = min(resources.displayMetrics.widthPixels, resources.displayMetrics.heightPixels) - resources.getDimensionPixelSize(
        R.dimen.qrcode_padding) * 2
    val qrCodeWriter = QRCodeWriter()
    val bitMatrix = qrCodeWriter.encode(contents, BarcodeFormat.QR_CODE, size, size)
    val bitmap = Bitmap.createBitmap(bitMatrix.width, bitMatrix.height, Bitmap.Config.RGB_565)

    for (x in 0 until bitMatrix.width) {
        for (y in 0 until bitMatrix.height) {
            bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE)
        }
    }

    setImageBitmap(bitmap)
}