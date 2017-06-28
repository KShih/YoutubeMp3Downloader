package com.github.kshih.youtubemp3downloader;

import android.app.DownloadManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    WebView wv;
    ProgressBar pb;
    EditText keyText;
    String keyword;    //用來記錄關鍵字
    String baseURL="https://m.youtube.com/results?q=";
    String ctext_before = "";
    String ctext = "";

    //private OkHttpClient mOkHttpClient;
    private Button btn_search;
    private Button btn_download;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_search = (Button) this.findViewById(R.id.btn_search);
        btn_download = (Button) this.findViewById(R.id.btn_download);
        btn_download.setOnClickListener(this);
        btn_search.setOnClickListener(this);

        wv = (WebView) findViewById(R.id.webView);
        pb = (ProgressBar) findViewById(R.id.progressBar);
        keyText=(EditText)findViewById(R.id.editText);

        wv.getSettings().setJavaScriptEnabled(true);	// 啟用 JavaScript
        wv.setWebViewClient(new WebViewClient());		// 建立及使用 WebViewClient 物件
        wv.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                pb.setProgress(progress);       //設定進度
                pb.setVisibility(progress < 100 ? View.VISIBLE : View.GONE);  //依進度來讓進度條顯示或消失
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_search:
                search(v);
                break;
            case R.id.btn_download:
                download();
                break;
        }
    }

    @Override
    public void onBackPressed() {  //按下返回鍵時的事件處理
        if(wv.canGoBack()){   // 如果 WebView 有上一頁
            wv.goBack();	  // 回上一頁
            return;
        }
        super.onBackPressed();  //呼叫父類別的同名方法, 以執行預設動作(結束程式)
    }
    public void download(){
        boolean exception = false; // initial exception
        String wvtext = "";


        String mp3Url = "https://www.youtubeinmp3.com/fetch/?video=";
        long downloadId = 0;
        DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        final ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData data = cm.getPrimaryClip();    //  ClipData 裡保存了一個ArryList 的 Item 序列， 可以用 getItemCount() 來獲取個數

        try{
            ClipData.Item item = data.getItemAt(0);
            ctext = item.getText().toString();// 注意 item.getText 可能為空

            // Clip board's data was not the data we want, so try the webview
            if(!ctext.contains("youtu.be") && !ctext.contains("watch?v=")){
                wvtext = wv.getUrl().toString();
                exception = true;
            }
        } catch(Exception e){
            Toast.makeText(getApplicationContext(),"音樂檔有誤"+e.toString(),Toast.LENGTH_SHORT).show();
            exception = true;
            wvtext = wv.getUrl().toString(); // Clipboard Found the exception, so try the webview
        }
        if(ctext_before.contains(ctext)){
            exception = true;
            wvtext = wv.getUrl().toString();
        }

        /* Chose which data between clipboard and Webview that we gonna use */
        if(exception){ // clip board有誤
            mp3Url += wvtext;
            //Toast.makeText(getApplicationContext(), "複製了webview的資料"+mp3Url, Toast.LENGTH_SHORT).show();
        }
        else {         // clip board無誤
            mp3Url += ctext;
            ctext_before = ctext;
        }

        /*  Final Check for download */
        if(mp3Url.contains("youtu.be" )|| mp3Url.contains("watch?v=" )) {

            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(mp3Url));
            //request.setMimeType("audio/MP3");
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            downloadId = downloadManager.enqueue(request);
        }
        Uri uri = downloadManager.getUriForDownloadedFile(downloadId);

    }
    public void search(View v){
        keyword = keyText.getText().toString().replaceAll("\\s+", "+"); //將字串中的單一或連續空白置換成 +
        wv.loadUrl(baseURL + keyword);
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
        editor.putString("關鍵字", keyword);  // 儲存目前的查詢參數
        editor.commit();

    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences myPref = getPreferences(MODE_PRIVATE);
        keyword = myPref.getString("關鍵字", "音樂");
        if(wv.getUrl()==null)
            wv.loadUrl(baseURL+keyword);
    }

}
