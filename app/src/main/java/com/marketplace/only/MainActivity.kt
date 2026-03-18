package com.marketplace.only

import android.os.Bundle
import android.webkit.*
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.widget.FrameLayout
import android.view.View

class MainActivity : AppCompatActivity() {

    val marketplace = "https://touch.facebook.com/marketplace"
    private lateinit var webView: WebView
    private lateinit var overlay: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = FrameLayout(this)
        setContentView(root)

        webView = WebView(this)
        root.addView(webView, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        ))

        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.databaseEnabled = true
        webView.settings.javaScriptCanOpenWindowsAutomatically = true
        webView.settings.loadWithOverviewMode = true
        webView.settings.useWideViewPort = true
        webView.settings.userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:124.0) Gecko/20100101 Firefox/124.0"
        webView.isScrollbarFadingEnabled = true
        webView.scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY

        // CAMBIO 1: dejar que los eventos touch lleguen al contenido sin que
        // el ViewGroup padre los intercepte. Esto permite que los divs internos
        // de Facebook (scroll virtualizado) reciban el touchmove correctamente.
        webView.setOnTouchListener { v, event ->
            v.parent?.requestDisallowInterceptTouchEvent(true)
            false
        }

        webView.setOverScrollMode(View.OVER_SCROLL_ALWAYS)

        CookieManager.getInstance().setAcceptCookie(true)
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)
        CookieManager.getInstance().flush()

        webView.webChromeClient = WebChromeClient()

        overlay = FrameLayout(this).apply {
            setBackgroundColor(Color.WHITE)
            visibility = View.GONE
            elevation = 10f
            isClickable = false
            isFocusable = false
        }
        root.addView(overlay, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        ))

        webView.webViewClient = object : WebViewClient() {

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val url = request?.url.toString()
                val host = request?.url?.host ?: ""

                if (url == "about:blank") return false
                if (!url.startsWith("http://") && !url.startsWith("https://")) return true

                if (host.endsWith("facebook.com") || host.endsWith("fb.com") || host.endsWith("fb.me")) {
                    val blocked = listOf(
                        "facebook.com/reel/",
                        "facebook.com/friends",
                        "facebook.com/watch",
                        "facebook.com/groups",
                        "facebook.com/messages",
                        "facebook.com/gaming",
                        "facebook.com/notifications",
                        "facebook.com/menu",
                    )

                    for (b in blocked) {
                        if (url.contains(b)) {
                            overlay.visibility = View.VISIBLE
                            webView.post { webView.loadUrl(marketplace) }
                            return true
                        }
                    }

                    return false
                }

                return true
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)

                CookieManager.getInstance().flush()

                val isBlocked = url?.contains("facebook.com/friends") == true ||
                        url?.contains("facebook.com/reel/") == true ||
                        url?.contains("facebook.com/watch") == true ||
                        url?.contains("facebook.com/groups") == true ||
                        url?.contains("facebook.com/messages") == true ||
                        url?.contains("facebook.com/gaming") == true ||
                        url?.contains("facebook.com/notifications") == true ||
                        url?.contains("facebook.com/menu") == true

                if (isBlocked) {
                    overlay.visibility = View.VISIBLE
                    view?.loadUrl(marketplace)
                    return
                }

                overlay.visibility = View.GONE
                view?.evaluateJavascript("window._marketplaceWatcher = false;", null)
                injectJs(view)
            }
        }

        webView.addJavascriptInterface(object {
            @android.webkit.JavascriptInterface
            fun showOverlay() {
                runOnUiThread {
                    overlay.visibility = View.VISIBLE
                }
            }
        }, "Android")

        webView.loadUrl(marketplace)
    }

    private fun injectJs(view: WebView?) {
        val js = """
            (function() {
                if (window._marketplaceWatcher) return;
                window._marketplaceWatcher = true;

                var blocked = ['facebook.com/friends', 'facebook.com/reel/', 'facebook.com/watch', 'facebook.com/groups', 'facebook.com/messages', 'facebook.com/gaming', 'facebook.com/notifications', 'facebook.com/menu'];

                function isBlocked(url) {
                    for (var i = 0; i < blocked.length; i++) {
                        if (url.indexOf(blocked[i]) !== -1) return true;
                    }
                    return false;
                }

                // CAMBIO 2: stopPropagation en touchmove para que los divs internos
                // de Facebook no tengan sus eventos cancelados por contenedores padre.
                document.addEventListener('touchmove', function(e) {
                    e.stopPropagation();
                }, { passive: true });

                function hideElements() {
                    var style = document.getElementById('_mp_style');
                    if (!style) {
                        style = document.createElement('style');
                        style.id = '_mp_style';
                        document.head && document.head.appendChild(style);
                    }
                    style.innerHTML = `
                        [role="tablist"] { display: none !important; }
                        [role="banner"] { display: none !important; }
                        [aria-label="Menú"] { display: none !important; }
                        [aria-label="Buscar en Facebook"] { display: none !important; }
                        svg[viewBox="0 0 44 44"] { display: none !important; }
                        html, body { overflow-y: auto !important; touch-action: pan-y !important; }
                        * { touch-action: pan-y pinch-zoom !important; }
                        [style*="overflow: hidden"], [style*="overflow:hidden"] { overflow-y: auto !important; overflow-x: hidden !important; }
                    `;
                }

                setInterval(function() {
                    var url = window.location.href;
                    if (isBlocked(url)) {
                        Android.showOverlay();
                        window.location.href = 'https://touch.facebook.com/marketplace';
                        return;
                    }
                    hideElements();
                }, 50);
            })();
        """.trimIndent()

        view?.evaluateJavascript(js, null)
    }

    override fun onPause() {
        super.onPause()
        CookieManager.getInstance().flush()
    }

    override fun onDestroy() {
        super.onDestroy()
        CookieManager.getInstance().flush()
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}