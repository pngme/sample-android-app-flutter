package com.pngme.pngme_flutter_diagnostics

import android.content.Context
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.pngme.sdk.library.PngmeSdk
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class PngmeSdkE2ETest {

    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val device = UiDevice.getInstance(instrumentation)

    private val targetContext: Context
        get() = instrumentation.targetContext

    private val targetPackage: String
        get() = targetContext.packageName

    @Before
    fun resetAppState() {
        // Avoid `pm clear` inside instrumentation (can be slow/flaky on emulators). Instead, reset the
        // SDK prefs + workers + runtime permission in-process.
        targetContext.getSharedPreferences("pr3f3r3nc3", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()

        WorkManager.getInstance(targetContext).cancelAllWork()
            .result
            .get(10, TimeUnit.SECONDS)

        device.wakeUp()
        device.pressHome()
        device.waitForIdle()
    }

    @Test
    fun test01_acceptTerms_and_denyPermission_still_enqueuesWorkers() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                PngmeSdk.go(
                    activity = activity,
                    clientKey = "test-client-key",
                    firstName = "Test",
                    lastName = "User",
                    email = "test@example.com",
                    phoneNumber = "0123456789",
                    externalId = "ext-2",
                    companyName = "Acme"
                )
            }

            acceptTermsInBottomSheet()
            denySmsPermission()

            assertFalse("SDK should report not granted after deny", PngmeSdk.isPermissionGranted(targetContext))

            // Critical regression guard: ABSA flow can grant permission later in Settings.
            // Workers MUST already be enqueued once terms are accepted.
            assertHasWorkEnqueued(WORK_SEND_SMS)
            assertHasWorkEnqueued(WORK_RECOVERY_SMS)
            assertHasWorkEnqueued(WORK_SIGN_IN)
        }
    }

    @Test
    fun test02_acceptTerms_and_allowPermission_enqueuesWorkers_and_marksPermissionGranted() {
        val onComplete = CountDownLatch(1)

        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                PngmeSdk.go(
                    activity = activity,
                    clientKey = "test-client-key",
                    firstName = "Test",
                    lastName = "User",
                    email = "test@example.com",
                    phoneNumber = "0123456789",
                    externalId = "ext-1",
                    companyName = "Acme",
                    onComplete = { onComplete.countDown() }
                )
            }

            acceptTermsInBottomSheet()
            allowSmsPermission()

            assertTrue("SDK did not call onComplete after granting permission", onComplete.await(15, TimeUnit.SECONDS))

            assertTrue("SDK should report permission granted after terms+allow", PngmeSdk.isPermissionGranted(targetContext))

            assertHasWorkEnqueued(WORK_SEND_SMS)
            assertHasWorkEnqueued(WORK_RECOVERY_SMS)
            assertHasWorkEnqueued(WORK_SIGN_IN)
        }
    }

    private fun acceptTermsInBottomSheet() {
        // These IDs come from the SDK's bottom sheet layout; they are merged into the app namespace at runtime.
        val checkbox =
            device.wait(Until.findObject(By.res(targetPackage, "terms_checkbox")), UI_TIMEOUT_MS)
                ?: device.wait(Until.findObject(By.textContains("Terms")), UI_TIMEOUT_MS)
        assertNotNull("Terms checkbox not found (bottom sheet not displayed?)", checkbox)
        checkbox!!.click()

        val continueButton =
            device.wait(Until.findObject(By.res(targetPackage, "main_button")), UI_TIMEOUT_MS)
                ?: device.wait(Until.findObject(By.textContains("Continue")), UI_TIMEOUT_MS)
        assertNotNull("Continue button not found", continueButton)
        continueButton!!.click()
    }

    private fun allowSmsPermission() {
        val allowButton =
            device.wait(Until.findObject(By.res(PERMISSION_CONTROLLER_PKG, "permission_allow_button")), UI_TIMEOUT_MS)
                ?: device.wait(Until.findObject(By.res(PERMISSION_CONTROLLER_PKG, "permission_allow_foreground_only_button")), UI_TIMEOUT_MS)
                ?: device.wait(Until.findObject(By.textContains("ALLOW")), UI_TIMEOUT_MS)
                ?: device.wait(Until.findObject(By.textContains("Allow")), UI_TIMEOUT_MS)
        assertNotNull("Allow button not found on permission dialog", allowButton)
        allowButton!!.click()

        // Wait for dialog to disappear.
        device.wait(Until.gone(By.pkg(PERMISSION_CONTROLLER_PKG)), UI_TIMEOUT_MS)
    }

    private fun denySmsPermission() {
        val denyButton =
            device.wait(Until.findObject(By.res(PERMISSION_CONTROLLER_PKG, "permission_deny_button")), UI_TIMEOUT_MS)
                ?: device.wait(Until.findObject(By.textContains("Don't allow")), UI_TIMEOUT_MS)
                ?: device.wait(Until.findObject(By.textContains("DENY")), UI_TIMEOUT_MS)
                ?: device.wait(Until.findObject(By.textContains("Deny")), UI_TIMEOUT_MS)
        assertNotNull("Deny button not found on permission dialog", denyButton)
        denyButton!!.click()

        device.wait(Until.gone(By.pkg(PERMISSION_CONTROLLER_PKG)), UI_TIMEOUT_MS)
    }

    private fun assertHasWorkEnqueued(uniqueName: String) {
        val wm = WorkManager.getInstance(targetContext)
        val deadline = System.currentTimeMillis() + 15_000
        var lastInfos: List<WorkInfo> = emptyList()

        while (System.currentTimeMillis() < deadline) {
            lastInfos = wm.getWorkInfosForUniqueWork(uniqueName).get()
            if (lastInfos.isNotEmpty()) {
                // We don't assert on specific state (periodic work is usually ENQUEUED),
                // only that it exists and isn't cancelled.
                assertTrue(
                    "Work '$uniqueName' was cancelled unexpectedly: $lastInfos",
                    lastInfos.none { it.state == WorkInfo.State.CANCELLED }
                )
                return
            }
            Thread.sleep(250)
        }

        assertTrue("Expected WorkManager work '$uniqueName' to be enqueued, but none found", lastInfos.isNotEmpty())
    }

    companion object {
        private const val UI_TIMEOUT_MS = 15_000L
        private const val PERMISSION_CONTROLLER_PKG = "com.android.permissioncontroller"

        // WorkManager unique names used by the Pngme SDK.
        private const val WORK_SIGN_IN = "user_registration_worker"
        private const val WORK_SEND_SMS = "SendSmsWork"
        private const val WORK_RECOVERY_SMS = "RecoverySmsWork"
    }
}
