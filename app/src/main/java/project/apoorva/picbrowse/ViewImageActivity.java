package project.apoorva.picbrowse;

import android.app.Activity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Created by Apoorva on 18/11/14.
 */
public class ViewImageActivity extends Activity {
    private final String ARG_LINK = "arg_link";

    private WebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_image);

        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mWebView = (WebView) findViewById(R.id.webview);

        String url = "";

        url = getIntent().getStringExtra(ARG_LINK);

        if (url != null && !url.equals("")) {
            mWebView.getSettings().setJavaScriptEnabled(true);
            mWebView.setWebViewClient(new WebViewClient());
            mWebView.loadUrl(url);

        }
    }

}
