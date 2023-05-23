package com.mobileplus.dummytriluc.ui.scanner

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import com.core.BaseActivity
import com.google.zxing.BarcodeFormat
import com.google.zxing.client.android.BeepManager
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import com.journeyapps.barcodescanner.DefaultDecoderFactory
import com.mobileplus.dummytriluc.R


/**
 * Created by KO Huyn on 27/09/2021.
 */
class ScannerActivity : BaseActivity() {
    companion object {
        const val ARG_INTENT_BAR_CODE = "ARG_INTENT_BAR_CODE"
    }

    private val barcodeView: DecoratedBarcodeView by lazy { findViewById(R.id.barcodeScanner) }

    private val beepManager: BeepManager by lazy { BeepManager(this) }

    private var isScanned: Boolean = false
    override fun getLayoutId(): Int {
        return R.layout.activity_scanner
    }

    override fun updateUI(savedInstanceState: Bundle?) {
        setupView()
    }

    private fun setupView() {
        val formats: Collection<BarcodeFormat> =
            listOf(BarcodeFormat.QR_CODE, BarcodeFormat.CODE_39)
        barcodeView.barcodeView.decoderFactory = DefaultDecoderFactory(formats)
        barcodeView.initializeFromIntent(intent)
        barcodeView.decodeContinuous(callback)
        barcodeView.setStatusText("Di chuyển camera đến vùng chứa mã để quét")
    }

    override fun onResume() {
        barcodeView.resume()
        super.onResume()
    }

    override fun onPause() {
        barcodeView.pause()
        super.onPause()
    }

    private val callback = BarcodeCallback {
        if (!isScanned) {
            beepManager.playBeepSoundAndVibrate()
            callbackToCheckInventory(it.text)
            isScanned = true
        }
    }

    private fun callbackToCheckInventory(text: String) {
        if (text.isNotBlank()) {
            isScanned = true
            Handler(Looper.getMainLooper()).postDelayed({
                setResult(RESULT_OK, Intent().putExtra(ARG_INTENT_BAR_CODE, text))
                finish()
            },500)
        }

    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return barcodeView.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event)
    }
}
