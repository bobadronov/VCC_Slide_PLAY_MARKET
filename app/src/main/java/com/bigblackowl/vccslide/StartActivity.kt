package com.bigblackowl.vccslide

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability

@Suppress("DEPRECATION")
class StartActivity : AppCompatActivity() {
    companion object {
        const val UPDATE_REQUEST_CODE = 100
    }

    private lateinit var appUpdateManager: AppUpdateManager
    private var shouldPerformTransition = false

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        shouldPerformTransition = intent?.getBooleanExtra("performTransition", false) ?: false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.requestFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_start)

        appUpdateManager = AppUpdateManagerFactory.create(this)
        checkForAppUpdate()
    }

    fun onKyivButtonClick(view: View) {
        animateButtonClick(view)
        openMainActivityWithAnimation(view, "https://docs.google.com/presentation/d/e/2PACX-1vTomhbyjW__8Kdo5DCKSCh1I4pj2iVKaCa2GqMe5sp_jSHGMHgGZLbEWKAm_n_Vk8YoIPyog44_7_bs/pub?start=true&loop=true&delayms=3000")
    }

    fun onKhmelnytskyiButtonClick(view: View) {
        animateButtonClick(view)
        openMainActivityWithAnimation(view, "https://docs.google.com/presentation/d/e/2PACX-1vRfOXaX-0p-TrUegHFq6WuOBcR-LChLs0XgAhbnuuKnHRkCtAyvSDZ1X2Ljxrnb_nHyzFfxE85vzp_Y/pub?start=true&loop=true&delayms=3000")
    }

    private fun openMainActivityWithAnimation(view: View, url: String) {
        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(this, view, "buttonTransition")
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra(MainActivity.EXTRA_URL, url)
        startActivity(intent, options.toBundle())
    }

    private fun checkForAppUpdate() {
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
            ) {
                appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    AppUpdateType.IMMEDIATE,
                    this,
                    UPDATE_REQUEST_CODE
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (shouldPerformTransition) {
            val view: View? = findViewById(R.id.buttonKyiv) // Get the view of the clicked button
            view?.let { startReverseTransition(it) }
        }
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    AppUpdateType.IMMEDIATE,
                    this,
                    UPDATE_REQUEST_CODE
                )
            }
        }
    }

    private fun animateButtonClick(view: View) {
        val animation = AnimationUtils.loadAnimation(this@StartActivity, R.anim.button_scale)
        view.startAnimation(animation)
    }

    @Deprecated("Deprecated in Java")
    @SuppressLint("ObsoleteSdkInt")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == UPDATE_REQUEST_CODE) {
            if (resultCode != RESULT_OK) {
                // Log the result if the update flow fails or is canceled
                // You can handle the result here based on your requirements
                return
            }
        }
    }
}
