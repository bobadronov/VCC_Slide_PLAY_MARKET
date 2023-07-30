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
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.*

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_URL = "extra_url"
    }

    private lateinit var mWebView: WebView
    private lateinit var mTimer: Timer

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
    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, StartActivity::class.java)
        startActivity(intent)
    }
}
