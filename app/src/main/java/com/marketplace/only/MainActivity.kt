package com.marketplace.only

import android.net.Uri
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

    /** Paths that must keep working (login, marketplace, seller chat under marketplace). */
    private val allowedPrefixes = listOf(
        "/marketplace",
        "/login",
        "/checkpoint",
        "/two_step_verification",
        "/recover",
        "/confirmemail",
        "/security",
        "/auth",
    )

    /** Exact path matches for the news feed / home. */
    private val feedExactPaths = setOf(
        "/",
        "/home",
        "/home.php",
        "/feed",
        "/index.php",
    )

    /** Path prefixes for stories and other distractions matched by startsWith. */
    private val blockedPrefixes = listOf(
        "/stories",
        "/story.php",
        "/reel/",
        "/reels",
        "/friends",
        "/watch",
        "/groups",
        "/messages",
        "/gaming",
        "/notifications",
        "/menu",
    )

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
                val uri = request?.url ?: return true
                val url = uri.toString()
                val host = uri.host ?: ""

                if (url == "about:blank") return false
                if (!url.startsWith("http://") && !url.startsWith("https://")) return true

                if (host.endsWith("facebook.com") || host.endsWith("fb.com") || host.endsWith("fb.me")) {
                    if (isDistractionUrl(uri)) {
                        overlay.visibility = View.VISIBLE
                        webView.post { webView.loadUrl(marketplace) }
                        return true
                    }
                    return false
                }

                return true
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)

                CookieManager.getInstance().flush()

                val uri = url?.let { runCatching { Uri.parse(it) }.getOrNull() }
                if (uri != null && isDistractionUrl(uri)) {
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

    /**
     * True when the URL is the news feed, stories, or another distraction page.
     * Root "/" is an exact-path match only, so /marketplace, /login, etc. stay open.
     */
    private fun isDistractionUrl(uri: Uri): Boolean {
        val host = uri.host ?: return false
        if (!(host.endsWith("facebook.com") || host.endsWith("fb.com") || host.endsWith("fb.me"))) {
            return false
        }

        var path = uri.path ?: "/"
        if (path.isEmpty()) path = "/"
        // Normalize trailing slash except for root
        if (path.length > 1 && path.endsWith("/")) {
            path = path.dropLast(1)
        }

        for (prefix in allowedPrefixes) {
            if (path == prefix || path.startsWith("$prefix/")) {
                return false
            }
        }

        if (path in feedExactPaths) {
            return true
        }

        for (prefix in blockedPrefixes) {
            val normalized = prefix.trimEnd('/')
            if (path == normalized || path.startsWith("$normalized/")) {
                return true
            }
            // Keep matching "/reel/" style when the stored prefix has a trailing slash
            if (prefix.endsWith("/") && path.startsWith(prefix.trimEnd('/'))) {
                return true
            }
        }

        return false
    }

    private fun injectJs(view: WebView?) {
        val js = """
            (function() {
                if (window._marketplaceWatcher) return;
                window._marketplaceWatcher = true;

                var allowed = ['/marketplace', '/login', '/checkpoint', '/two_step_verification', '/recover', '/confirmemail', '/security', '/auth'];
                var feedExact = ['/', '/home', '/home.php', '/feed', '/index.php'];
                var blockedPrefixes = ['/stories', '/story.php', '/reel', '/reels', '/friends', '/watch', '/groups', '/messages', '/gaming', '/notifications', '/menu'];

                function normalizePath(path) {
                    if (!path || path === '') return '/';
                    if (path.length > 1 && path.charAt(path.length - 1) === '/') {
                        return path.slice(0, -1);
                    }
                    return path;
                }

                function isBlocked(url) {
                    try {
                        var u = new URL(url);
                        var path = normalizePath(u.pathname);

                        for (var i = 0; i < allowed.length; i++) {
                            if (path === allowed[i] || path.indexOf(allowed[i] + '/') === 0) return false;
                        }
                        for (var j = 0; j < feedExact.length; j++) {
                            if (path === feedExact[j]) return true;
                        }
                        for (var k = 0; k < blockedPrefixes.length; k++) {
                            var p = blockedPrefixes[k];
                            if (path === p || path.indexOf(p + '/') === 0) return true;
                        }
                        return false;
                    } catch (e) {
                        return false;
                    }
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
                        [aria-label="Menu"] { display: none !important; }
                        [aria-label="Buscar en Facebook"] { display: none !important; }
                        [aria-label="Search Facebook"] { display: none !important; }
                        [aria-label="Stories"] { display: none !important; }
                        [aria-label="Historias"] { display: none !important; }
                        [aria-label="Histórias"] { display: none !important; }
                        [aria-label="Story"] { display: none !important; }
                        [data-pagelet*="Story"] { display: none !important; }
                        [data-pagelet*="Stories"] { display: none !important; }
                        [data-pagelet*="FeedUnit"] { display: none !important; }
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
