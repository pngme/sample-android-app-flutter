package com.example.sampleflutter

import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine

import androidx.annotation.NonNull
import io.flutter.plugin.common.MethodChannel
import android.content.Intent
import com.pngme.sdk.library.PngmeSdk
import com.pngme.sdk.library.common.Environment
import com.pngme.sdk.library.data.LoanInfo
import com.pngme.sdk.library.data.UserInfo

class MainActivity: FlutterActivity() {
    private val CHANNEL = "com.flutter.pngme/sdk"

    override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler {
                call, result ->
            if (call.method == "go") {
                // capture call parameters
                val sdkToken: String = call.argument("sdkToken")!!
                val firstName : String = call.argument("firstName")!!
                val lastName : String = call.argument("lastName")!!
                val email : String = call.argument("email")!!
                val phoneNumber : String = call.argument("phoneNumber")!!
                val externalId : String = call.argument("externalId")!!
                val companyName : String = call.argument("companyName")!!
                val hidePngmeDialog: Boolean = call.argument("hidePngmeDialog")!!

                // create intent for PngmeSDKHelper Activity
                val intent = Intent(context, PngmeSDKHelper::class.java)
                intent.putExtra("sdkToken", sdkToken)
                intent.putExtra("firstName", firstName)
                intent.putExtra("lastName", lastName)
                intent.putExtra("email", email)
                intent.putExtra("phoneNumber", phoneNumber)
                intent.putExtra("externalId", externalId)
                intent.putExtra("companyName", companyName)
                intent.putExtra("hidePngmeDialog", hidePngmeDialog)

                // launch activity
                activity.startActivity(intent)
                result.success("sdk invoked")
            } else {
                result.notImplemented()
            }
        }
    }
}
