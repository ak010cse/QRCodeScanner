package com.arvind.qrcodescanner

import android.R.attr.label
import android.R.attr.text
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.arvind.qrcodescanner.databinding.ActivityMainBinding
import com.google.zxing.integration.android.IntentIntegrator


class MainActivity : AppCompatActivity() {
    val PERMISSION_REQUEST_CAMERA = 1
    private lateinit var binding: ActivityMainBinding
    var copiedText=""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.rlCopy.setOnClickListener {
            val clipboard: ClipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("label", copiedText)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "Copied to Clipboard!!!", Toast.LENGTH_LONG).show()

        }

        binding.qrCodeScan.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ContextCompat.checkSelfPermission(this,android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this@MainActivity, arrayOf(android.Manifest.permission.CAMERA), PERMISSION_REQUEST_CAMERA)
                } else {
                    initQRCodeScanner()
                }
            } else {
                initQRCodeScanner()
            }
        }

    }

    private fun initQRCodeScanner() {
        val integrator = IntentIntegrator(this)
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
        integrator.setOrientationLocked(true)
        integrator.setCaptureActivity(CaptureActivityPortrait::class.java)
        integrator.setPrompt("Scan a QR code")
        integrator.initiateScan()
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSION_REQUEST_CAMERA) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initQRCodeScanner()
            } else {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri: Uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivityForResult(intent, PERMISSION_REQUEST_CAMERA)
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) {
                Toast.makeText(this, "Scan cancelled", Toast.LENGTH_LONG).show()
                binding.rlCopy.visibility = View.GONE
                binding.llData.visibility = View.GONE
                binding.scannedDataTv.text= ""
            } else {
                copiedText = result.contents
                binding.scannedDataTv.text = result.contents
                binding.rlCopy.visibility = View.VISIBLE
                binding.llData.visibility = View.VISIBLE
                Toast.makeText(this, "Scanned: " + result.contents, Toast.LENGTH_LONG).show()
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

}