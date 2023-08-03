@file:Suppress("DEPRECATION")

package com.bigblackowl.vccslide

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.os.Handler
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
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.*

@Suppress("DEPRECATION")
class SiteActivity : AppCompatActivity(){

    companion object {
        const val EXTRA_URL = "extra_url"
    }

    private lateinit var mWebView: WebView
    private lateinit var connectionLabel: TextView
    private lateinit var networkSettingsButton: Button
    private lateinit var goToMainButton: Button
    private lateinit var backgroundLayout: FrameLayout

    private val BACK_PRESS_DELAY = 2000

    private var isBackPressed = false
    private val handler = Handler()
    @SuppressLint("SetJavaScriptEnabled", "MissingInflatedId", "ClickableViewAccessibility",
        "InflateParams"
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        window.requestFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_site)

        mWebView = findViewById(R.id.webView)
        connectionLabel = findViewById(R.id.connectionLabel)
        networkSettingsButton = findViewById(R.id.networkSettingsButton)
        goToMainButton = findViewById(R.id.goToMainButton)

        goToMainButton.setOnClickListener {openStartActivity()}

        backgroundLayout = findViewById(R.id.backgroundLayout)


        mWebView.webViewClient = WebViewClient()
        mWebView.webChromeClient = MyChrome()
        mWebView.setOnLongClickListener {
            goToMain()
            true
        }
        if (isConnected()) {
            backgroundLayout.setOnClickListener {
                goToMainButton.visibility = View.GONE
                mWebView.visibility = View.VISIBLE
            }
        }
        val webSettings: WebSettings = mWebView.settings
        webSettings.javaScriptEnabled = true
        webSettings.allowFileAccess = true
        webSettings.javaScriptCanOpenWindowsAutomatically = true


        val url = intent.getStringExtra(EXTRA_URL)
        if (url != null) {
            mWebView.loadUrl(url)
            if (!isConnected()) {
                mWebView.visibility = View.GONE
                connectionLabel.visibility = View.VISIBLE
                networkSettingsButton.visibility = View.VISIBLE
            } else {
                mWebView.visibility = View.VISIBLE
                connectionLabel.visibility = View.GONE
                networkSettingsButton.visibility = View.GONE

                val toastView = layoutInflater.inflate(R.layout.toast_layout, null)
                val toastMessage = toastView.findViewById<TextView>(R.id.message)
                toastMessage.text = getString(R.string.show_message_on_start_site_view)
                val toast = Toast(applicationContext)
                toast.duration = Toast.LENGTH_LONG
                toast.view = toastView
                toast.show()
            }

            networkSettingsButton.setOnClickListener {
                openNetworkSettings()
            }
        }
    }

    private fun openStartActivity(){
        val intent = Intent(this, StartActivity::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        goToMainButton.visibility = View.GONE
    }

    private fun goToMain() {
        goToMainButton.visibility = View.VISIBLE
        mWebView.visibility = View.GONE
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

    override fun onResume() {
        super.onResume()
        if (!isConnected()) {
            mWebView.visibility = View.GONE
            connectionLabel.visibility = View.VISIBLE
            networkSettingsButton.visibility = View.VISIBLE
        } else {
            mWebView.visibility = View.VISIBLE
            connectionLabel.visibility = View.GONE
            networkSettingsButton.visibility = View.GONE
            backgroundLayout.setOnClickListener {
                goToMainButton.visibility = View.GONE
                mWebView.visibility = View.VISIBLE
            }
            mWebView.reload()
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
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    @SuppressLint("InflateParams")
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (goToMainButton.visibility == View.VISIBLE) {
            mWebView.visibility = View.VISIBLE
            goToMainButton.visibility = View.GONE
        } else if (mWebView.canGoBack()) {
            mWebView.goBack()
        } else {
            if (isBackPressed) {
                super.onBackPressed()
            } else {
                isBackPressed = true

                if (!isConnected()) {
                    val intent = Intent(this, StartActivity::class.java)
                    startActivity(intent)
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                } else {
                    val toastView = layoutInflater.inflate(R.layout.toast_layout, null)
                    val toastMessage = toastView.findViewById<TextView>(R.id.message)
                    toastMessage.text = getString(R.string.press_back_again)

                    val toast = Toast(applicationContext)
                    toast.duration = Toast.LENGTH_SHORT
                    toast.view = toastView
                    toast.show()
                    handler.postDelayed({ isBackPressed = false }, BACK_PRESS_DELAY.toLong())
                }
            }
        }
    }

}
