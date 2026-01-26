package com.pngme.pngme_flutter_diagnostics

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.core.content.ContextCompat
import androidx.work.WorkManager
import com.pngme.sdk.library.PngmeSdk
import io.flutter.embedding.android.FlutterFragmentActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import java.util.concurrent.Executors

class MainActivity : FlutterFragmentActivity() {

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler { call, result ->
            when (call.method) {
                "startPngme" -> {
                    val clientKey: String = call.argument("clientKey") ?: ""
                    val firstName: String = call.argument("firstName") ?: ""
                    val lastName: String = call.argument("lastName") ?: ""
                    val email: String = call.argument("email") ?: ""
                    val phoneNumber: String = call.argument("phoneNumber") ?: ""
                    val externalId: String = call.argument("externalId") ?: ""
                    val companyName: String = call.argument("companyName") ?: ""

                    PngmeSdk.go(
                        activity = this,
                        clientKey = clientKey,
                        firstName = firstName,
                        lastName = lastName,
                        email = email,
                        phoneNumber = phoneNumber,
                        externalId = externalId,
                        companyName = companyName,
                    )

                    result.success(mapOf("status" to "started"))
                }

                "scheduleSmsSync" -> {
                    PngmeSdk.scheduleSmsSync(this)
                    result.success(mapOf("status" to "enqueued"))
                }

                "openAppSettings" -> {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.parse("package:$packageName")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    startActivity(intent)
                    result.success(mapOf("status" to "opened"))
                }

                "getDiagnostics" -> {
                    // WorkManager queries can block; do them off the UI thread.
                    EXECUTOR.execute {
                        try {
                            val smsPermissionGranted = ContextCompat.checkSelfPermission(
                                this,
                                android.Manifest.permission.READ_SMS
                            ) == PackageManager.PERMISSION_GRANTED

                            val sdkPermissionGranted = PngmeSdk.isPermissionGranted(this)
                            val userUuid = PngmeSdk.getUserUuid(this)

                            val workManager = WorkManager.getInstance(applicationContext)

                            fun workInfo(uniqueName: String): List<Map<String, Any?>> {
                                val infos = workManager.getWorkInfosForUniqueWork(uniqueName).get()
                                return infos.map { info ->
                                    mapOf(
                                        "id" to info.id.toString(),
                                        "state" to info.state.name,
                                        "runAttemptCount" to info.runAttemptCount
                                    )
                                }
                            }

                            val payload = mapOf(
                                "smsPermissionGranted" to smsPermissionGranted,
                                "sdkPermissionGranted" to sdkPermissionGranted,
                                "userUuid" to userUuid,
                                "work" to mapOf(
                                    "signIn" to workInfo(WORK_SIGN_IN),
                                    "sendSms" to workInfo(WORK_SEND_SMS),
                                    "recovery" to workInfo(WORK_RECOVERY_SMS),
                                )
                            )

                            runOnUiThread { result.success(payload) }
                        } catch (t: Throwable) {
                            runOnUiThread { result.error("diagnostics_error", t.message, null) }
                        }
                    }
                }

                else -> result.notImplemented()
            }
        }
    }

    companion object {
        private const val CHANNEL = "com.pngme/diagnostics"

        // WorkManager unique names used by the Pngme SDK.
        private const val WORK_SIGN_IN = "user_registration_worker"
        private const val WORK_SEND_SMS = "SendSmsWork"
        private const val WORK_RECOVERY_SMS = "RecoverySmsWork"

        private val EXECUTOR = Executors.newSingleThreadExecutor()
    }
}
