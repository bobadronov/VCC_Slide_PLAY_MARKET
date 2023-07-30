package com.bigblackowl.vccslide

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.Window
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.requestFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_start)

        appUpdateManager = AppUpdateManagerFactory.create(this)
        checkForAppUpdate()

        val buttonBrovary: Button = findViewById(R.id.buttonBrovary)
        val buttonVyshneve: Button = findViewById(R.id.buttonVyshneve)
        val buttonKyiv: Button = findViewById(R.id.buttonKyiv)
        val buttonKhmelnytskyi: Button = findViewById(R.id.buttonKhmelnytskyi)
        val buttonBorispol: Button = findViewById(R.id.buttonBorispol)
        buttonBrovary.setOnClickListener {openMainActivity(getString(R.string.url_kyiv))}
        buttonBorispol.setOnClickListener {openMainActivity(getString(R.string.url_kyiv))}
        buttonVyshneve.setOnClickListener {openMainActivity(getString(R.string.url_kyiv))}
        buttonKyiv.setOnClickListener {openMainActivity(getString(R.string.url_kyiv))}
        buttonKhmelnytskyi.setOnClickListener {openMainActivity(getString(R.string.url_khmelnytskyi))}
        AnimationUtils.loadAnimation(this, R.anim.slide_in_right)
        AnimationUtils.loadAnimation(this, R.anim.slide_out_left)
    }


    private fun openMainActivity(url: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra(MainActivity.EXTRA_URL, url)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
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

    @Deprecated("Deprecated in Java")
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

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            showExitDialog()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun showExitDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(getString(R.string.are_you_sure_you_want_to_exit))
        builder.setCancelable(true)
        builder.setPositiveButton(getString(R.string.yes)) { dialog, _ ->
            dialog.dismiss()
            finishAffinity() // Close all activities and exit the app
        }
        builder.setNegativeButton(getString(R.string.no)) { dialog, _ -> dialog.dismiss() }
        val alert = builder.create()
        alert.show()
    }
}
