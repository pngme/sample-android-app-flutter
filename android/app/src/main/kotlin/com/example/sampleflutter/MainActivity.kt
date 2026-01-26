package com.example.sampleflutter

import io.flutter.embedding.android.FlutterFragmentActivity
import io.flutter.embedding.engine.FlutterEngine

import androidx.annotation.NonNull
import io.flutter.plugin.common.MethodChannel
import com.pngme.sdk.library.PngmeSdk

class MainActivity: FlutterFragmentActivity() {
    private val CHANNEL = "com.flutter.pngme/sdk"

    override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler {
                call, result ->
            if (call.method == "go") {
                val clientKey: String = call.argument("clientKey")!!
                val firstName: String = call.argument("firstName")!!
                val lastName: String = call.argument("lastName")!!
                val email: String = call.argument("email")!!
                val phoneNumber: String = call.argument("phoneNumber")!!
                val externalId: String = call.argument("externalId")!!
                val companyName: String = call.argument("companyName")!!

                runOnUiThread {
                    PngmeSdk.go(
                        this@MainActivity,
                        clientKey,
                        firstName,
                        lastName,
                        email,
                        phoneNumber,
                        externalId,
                        companyName
                    )
                }
                result.success("started")
            } else {
                result.notImplemented()
            }
        }
    }
}
