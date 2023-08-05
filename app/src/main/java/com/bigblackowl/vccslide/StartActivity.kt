package com.bigblackowl.vccslide

import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.provider.Settings
import android.view.KeyEvent
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
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

    private lateinit var networkSettingsButton: Button
    private lateinit var appUpdateManager: AppUpdateManager
    private lateinit var chooseCityText: TextView
    private lateinit var buttonBrovary: Button
    private lateinit var buttonVyshneve: Button
    private lateinit var buttonKyiv: Button
    private lateinit var buttonKhmelnytskyi: Button
    private lateinit var buttonBorispol: Button
    private lateinit var buttonSoon: Button
    private lateinit var buttonOurSite: Button
    private lateinit var chekInternet: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.requestFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_start)

        val mainLogoImageView: ImageView = findViewById(R.id.main_logo)
        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val newImageSize = (screenWidth * 0.24).toInt()
        val layoutParams = mainLogoImageView.layoutParams
        layoutParams.width = newImageSize
        layoutParams.height = newImageSize
        mainLogoImageView.layoutParams = layoutParams

        networkSettingsButton = findViewById(R.id.networkSettingsButton)
        networkSettingsButton.setOnClickListener { openNetworkSettings() }

        appUpdateManager = AppUpdateManagerFactory.create(this)
        chooseCityText = findViewById(R.id.choose_city_text)
        buttonBrovary = findViewById(R.id.buttonBrovary)
        buttonVyshneve = findViewById(R.id.buttonVyshneve)
        buttonKyiv = findViewById(R.id.buttonKyiv)
        buttonKhmelnytskyi = findViewById(R.id.buttonKhmelnytskyi)
        buttonBorispol = findViewById(R.id.buttonBorispol)
        buttonSoon = findViewById(R.id.buttonSoon)
        buttonOurSite = findViewById(R.id.buttonOurSite)
        chekInternet = findViewById(R.id.chek_internet)

        checkForAppUpdate()
        openWeb()
    }

    private fun openNetworkSettings() {
        val intent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    private fun showInternetSetting() {
        val buttons = listOf(buttonBrovary, buttonVyshneve, buttonKyiv, buttonKhmelnytskyi, buttonBorispol, buttonSoon, buttonOurSite)
        buttons.forEach { it.visibility = View.GONE }
        chooseCityText.visibility = View.GONE
        chekInternet.visibility = View.VISIBLE
    }

    private fun openWeb() {
        val cities = mapOf(
            buttonBrovary to R.string.url_kyiv,
            buttonVyshneve to R.string.url_kyiv,
            buttonKyiv to R.string.url_kyiv,
            buttonKhmelnytskyi to R.string.url_khmelnytskyi,
            buttonBorispol to R.string.url_kyiv
        )

        cities.forEach { (button, urlStringId) ->
            button.setOnClickListener { openMainActivity(getString(urlStringId)) }
        }

        buttonOurSite.setOnClickListener { openSiteActivity(getString(R.string.our_site)) }

        AnimationUtils.loadAnimation(this, R.anim.slide_in_right)
        AnimationUtils.loadAnimation(this, R.anim.slide_out_left)
    }

    private fun openMainActivity(url: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra(MainActivity.EXTRA_URL, url)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    private fun openSiteActivity(url: String) {
        val intent = Intent(this, SiteActivity::class.java)
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
        println("on resume")
        val isConnected = isConnected()
        if (!isConnected){
            showInternetSetting()
        }else{
            val buttons = listOf(buttonBrovary, buttonVyshneve, buttonKyiv, buttonKhmelnytskyi, buttonBorispol, buttonSoon, buttonOurSite)
            buttons.forEach { it.visibility = View.VISIBLE }
            chooseCityText.visibility = View.VISIBLE
            chekInternet.visibility = View.GONE
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

    private fun isConnected(): Boolean {
        val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
        return networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
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
