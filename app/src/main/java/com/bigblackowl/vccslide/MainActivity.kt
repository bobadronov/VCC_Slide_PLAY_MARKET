@file:Suppress("DEPRECATION")

package com.bigblackowl.vccslide

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import java.util.*

class StartActivity : AppCompatActivity() {

    private lateinit var buttonKyiv: Button
    private lateinit var buttonKhmelnytskyi: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.requestFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_start)

        buttonKyiv = findViewById(R.id.buttonKyiv) // Initialize the buttons
        buttonKhmelnytskyi = findViewById(R.id.buttonKhmelnytskyi)
        buttonKyiv.setOnClickListener {
            animateButtonClick(it)
            openMainActivity("https://docs.google.com/presentation/d/e/2PACX-1vTomhbyjW__8Kdo5DCKSCh1I4pj2iVKaCa2GqMe5sp_jSHGMHgGZLbEWKAm_n_Vk8YoIPyog44_7_bs/pub?start=true&loop=true&delayms=3000")
        }

        buttonKhmelnytskyi.setOnClickListener {
            animateButtonClick(it)
            openMainActivity("https://docs.google.com/presentation/d/e/2PACX-1vRfOXaX-0p-TrUegHFq6WuOBcR-LChLs0XgAhbnuuKnHRkCtAyvSDZ1X2Ljxrnb_nHyzFfxE85vzp_Y/pub?start=true&loop=true&delayms=3000")
        }
    }

    override fun onResume() {
        super.onResume()
        // Reset button scale to default (1.0) when returning to this activity

    }
    private fun animateButtonClick(view: View) {
        val animation = AnimationUtils.loadAnimation(this@StartActivity, R.anim.button_scale)
        view.startAnimation(animation)
    }

    private fun openMainActivity(url: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra(MainActivity.EXTRA_URL, url)
        startActivity(intent)
    }
}

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_URL = "extra_url"
        const val UPDATE_REQUEST_CODE = 100
    }

    private lateinit var mWebView: WebView
    private lateinit var mTimer: Timer
    private lateinit var appUpdateManager: AppUpdateManager
    private lateinit var connectionLabel: TextView
    private lateinit var networkSettingsButton: Button

    @SuppressLint("SetJavaScriptEnabled", "MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.requestFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_main)

        mWebView = findViewById(R.id.webView)
        connectionLabel = findViewById(R.id.connectionLabel)
        networkSettingsButton = findViewById(R.id.networkSettingsButton)

        mWebView.webViewClient = WebViewClient()
        mWebView.webChromeClient = MyChrome()
        val webSettings: WebSettings = mWebView.settings
        webSettings.javaScriptEnabled = true
        webSettings.allowFileAccess = true

        val url = intent.getStringExtra(EXTRA_URL)
        if (url != null) {
            mWebView.loadUrl(url)
            mTimer = Timer()
            mTimer.schedule(object : TimerTask() {
                override fun run() {
                    mWebView.post { mWebView.reload() }
                }
            }, 0, 300 * 1000) // Reload every 5 min

            appUpdateManager = AppUpdateManagerFactory.create(this)
            checkForAppUpdate()

            if (!isConnected()) {
                connectionLabel.visibility = View.VISIBLE
                mWebView.visibility = View.GONE
                networkSettingsButton.visibility = View.VISIBLE
            } else {
                connectionLabel.visibility = View.GONE
                mWebView.visibility = View.VISIBLE
                networkSettingsButton.visibility = View.GONE
            }

            networkSettingsButton.setOnClickListener {
                openNetworkSettings()
            }
        }
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

    private inner class MyChrome : WebChromeClient() {

        private var mCustomView: View? = null
        private var mCustomViewCallback: CustomViewCallback? = null
        private var mOriginalOrientation = 0
        private var mOriginalSystemUiVisibility = 0

        override fun getDefaultVideoPoster(): Bitmap? {
            if (mCustomView == null) {
                return null
            }
            return BitmapFactory.decodeResource(applicationContext.resources, 2130837573)
        }

        override fun onHideCustomView() {
            (window.decorView as FrameLayout).removeView(mCustomView)
            mCustomView = null
            window.decorView.systemUiVisibility = mOriginalSystemUiVisibility
            requestedOrientation = mOriginalOrientation
            mCustomViewCallback?.onCustomViewHidden()
            mCustomViewCallback = null
        }

        override fun onShowCustomView(paramView: View, paramCustomViewCallback: CustomViewCallback) {
            if (mCustomView != null) {
                onHideCustomView()
                return
            }
            mCustomView = paramView
            mOriginalSystemUiVisibility = window.decorView.systemUiVisibility
            mOriginalOrientation = requestedOrientation
            mCustomViewCallback = paramCustomViewCallback
            (window.decorView as FrameLayout).addView(mCustomView, FrameLayout.LayoutParams(-1, -1))
            window.decorView.systemUiVisibility = 3846 or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mWebView.saveState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        mTimer = Timer()
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
        if (!isConnected()) {
            connectionLabel.visibility = View.VISIBLE
            mWebView.visibility = View.GONE
            networkSettingsButton.visibility = View.VISIBLE
        } else {
            connectionLabel.visibility = View.GONE
            mWebView.visibility = View.VISIBLE
            networkSettingsButton.visibility = View.GONE
        }
    }

    private fun isConnected(): Boolean {
        val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
        return networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }

    private fun openNetworkSettings() {
        val intent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
        startActivity(intent)
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
