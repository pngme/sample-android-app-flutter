package com.example.sampleflutter

import com.pngme.sdk.library.PngmeSdk

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class PngmeSDKHelper : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        goHelper()
    }

    private fun goHelper() {
        /**
         * capture values from Activity intent
         * (passed to intent via MainActivity w. Flutter channel)
         */
        val sdkToken: String = intent.getStringExtra("sdkToken")!!
        val firstName : String = intent.getStringExtra("firstName")!!
        val lastName : String = intent.getStringExtra("lastName")!!
        val email : String = intent.getStringExtra("email")!!
        val phoneNumber : String = intent.getStringExtra("phoneNumber")!!
        val externalId : String = intent.getStringExtra("externalId")!!
        val companyName : String = intent.getStringExtra("companyName")!!
        val hasAcceptedTerms : Boolean = intent.getBooleanExtra("hasAcceptedTerms", false)

        PngmeSdk.goWithCustomDialog(
            this,
            sdkToken,
            firstName,
            lastName,
            email,
            phoneNumber,
            externalId,
            companyName,
            hasAcceptedTerms,
            ::onComplete
        )
    }

    private fun onComplete() {
        /**
         * callback invoked when the SDK is complete
         * (-> i.e. when the permissions dialog flow is done, and no longer needs the activity)
         * when SDK is complete, close this PngmeSDKHelper Activity
         */
        this.finish()
    }

}