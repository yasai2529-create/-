package com.example.lightbrowser;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.webkit.*;
import android.widget.*;
import java.io.ByteArrayInputStream;
import java.net.URLEncoder;

public class MainActivity extends Activity {
    WebView web;
    EditText urlBar;
    Button btnImg, btnJs, btnVid;
    boolean imgOn = false, jsOn = false, vidOn = false;

    static final String[] VIDEO_EXT = {".mp4", ".webm", ".m3u8", ".mpd", ".mkv", ".mov"};
    static final String[] VIDEO_HOST = {"googlevideo.com"};

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);

        urlBar = new EditText(this);
        urlBar.setSingleLine(true);
        urlBar.setHint("URL or 検索");
        urlBar.setImeOptions(EditorInfo.IME_ACTION_GO);
        urlBar.setOnEditorActionListener((v, id, e) -> { go(urlBar.getText().toString()); return true; });
        root.addView(urlBar, new LinearLayout.LayoutParams(-1, -2));

        LinearLayout bar = new LinearLayout(this);
        btnImg = btn(bar, "画像OFF", v -> { imgOn = !imgOn; apply(); web.reload(); });
        btnJs  = btn(bar, "JS OFF",  v -> { jsOn = !jsOn; apply(); web.reload(); });
        btnVid = btn(bar, "動画OFF", v -> { vidOn = !vidOn; apply(); web.reload(); });
        btn(bar, "外部で開く", v -> startActivity(Intent.createChooser(
                new Intent(Intent.ACTION_VIEW, Uri.parse(web.getUrl())), "開く")));
        root.addView(bar, new LinearLayout.LayoutParams(-1, -2));

        web = new WebView(this);
        root.addView(web, new LinearLayout.LayoutParams(-1, 0, 1f));
        setContentView(root);

        WebSettings s = web.getSettings();
        s.setCacheMode(WebSettings.LOAD_NO_CACHE);
        s.setSaveFormData(false);
        s.setMediaPlaybackRequiresUserGesture(true);
        apply();

        web.setWebViewClient(new WebViewClient() {
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView v, WebResourceRequest r) {
                if (!vidOn && isVideo(r.getUrl())) {
                    return new WebResourceResponse("text/plain", "utf-8", new ByteArrayInputStream(new byte[0]));
                }
                return null;
            }
            @Override
            public void onPageFinished(WebView v, String u) { urlBar.setText(u); }
        });

        go("https://duckduckgo.com/html/");
    }

    Button btn(LinearLayout p, String t, android.view.View.OnClickListener l) {
        Button x = new Button(this);
        x.setText(t);
        x.setAllCaps(false);
        x.setOnClickListener(l);
        p.addView(x, new LinearLayout.LayoutParams(0, -2, 1f));
        return x;
    }

    void apply() {
        web.getSettings().setBlockNetworkImage(!imgOn);
        web.getSettings().setJavaScriptEnabled(jsOn);
        btnImg.setText(imgOn ? "画像ON" : "画像OFF");
        btnJs.setText(jsOn ? "JS ON" : "JS OFF");
        btnVid.setText(vidOn ? "動画ON" : "動画OFF");
    }

    boolean isVideo(Uri u) {
        String host = u.getHost() == null ? "" : u.getHost();
        String path = u.getPath() == null ? "" : u.getPath().toLowerCase();
        for (String h : VIDEO_HOST) if (host.endsWith(h)) return true;
        for (String e : VIDEO_EXT) if (path.endsWith(e)) return true;
        return false;
    }

    void go(String in) {
        in = in.trim();
        if (in.isEmpty()) return;
        String u;
        if (in.startsWith("http://") || in.startsWith("https://")) u = in;
        else if (in.contains(".") && !in.contains(" ")) u = "https://" + in;
        else {
            try { u = "https://duckduckgo.com/html/?q=" + URLEncoder.encode(in, "UTF-8"); }
            catch (Exception e) { return; }
        }
        web.loadUrl(u);
    }

    @Override
    public boolean onKeyDown(int k, KeyEvent e) {
        if (k == KeyEvent.KEYCODE_BACK && web.canGoBack()) { web.goBack(); return true; }
        return super.onKeyDown(k, e);
    }
}
