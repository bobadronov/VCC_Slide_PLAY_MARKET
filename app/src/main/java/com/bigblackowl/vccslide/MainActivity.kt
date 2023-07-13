@file:Suppress("DEPRECATION")

package com.bigblackowl.vccslide

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.view.View
import android.widget.Button
import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.view.Window
import android.view.WindowManager
import java.util.*

class StartActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.requestFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_start)

        val buttonKyiv: Button = findViewById(R.id.buttonKyiv)
        buttonKyiv.setOnClickListener {
            openMainActivity("https://docs.google.com/presentation/d/e/2PACX-1vTomhbyjW__8Kdo5DCKSCh1I4pj2iVKaCa2GqMe5sp_jSHGMHgGZLbEWKAm_n_Vk8YoIPyog44_7_bs/pub?start=true&loop=true&delayms=3000")
        }

        val buttonKhmelnytskyi: Button = findViewById(R.id.buttonKhmelnytskyi)
        buttonKhmelnytskyi.setOnClickListener {
            openMainActivity("https://docs.google.com/presentation/d/e/2PACX-1vRfOXaX-0p-TrUegHFq6WuOBcR-LChLs0XgAhbnuuKnHRkCtAyvSDZ1X2Ljxrnb_nHyzFfxE85vzp_Y/pub?start=true&loop=true&delayms=3000")
        }
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
    }

    private lateinit var mWebView: WebView
    private var mTimer: Timer? = null

    @SuppressLint("SetJavaScriptEnabled", "MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.requestFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_main)

        mWebView = findViewById(R.id.webView)

        mWebView.webViewClient = WebViewClient()
        mWebView.webChromeClient = MyChrome()
        val webSettings: WebSettings = mWebView.settings
        webSettings.javaScriptEnabled = true
        webSettings.allowFileAccess = true

        val url = intent.getStringExtra(EXTRA_URL)
        if (url != null) {
            mWebView.loadUrl(url)
            mTimer = Timer()
            mTimer?.schedule(object : TimerTask() {
                override fun run() {
                    mWebView.post { mWebView.reload() }
                }
            }, 0, 60 * 1000) // Reload every 60 seconds / 3600 hour
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

    override fun onDestroy() {
        super.onDestroy()
        mTimer?.cancel()
    }
}

