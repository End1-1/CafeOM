package com.cafeom;

import android.app.Application;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class WebService extends Service {

    public static final int mMethodGET = 1;
    public static final int mMethodPOST = 2;
    public static final int mMethodPUT = 3;

    private BlockingDeque<Intent> mMessages = new LinkedBlockingDeque<>();

    @Override
    public void onCreate() {
        super.onCreate();
        initSSLNoVerify();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(looper).start();
        LocalBroadcastManager.getInstance(this).registerReceiver(listener, new IntentFilter("web_service"));
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(listener);
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static String getMethodName(int method) {
        switch (method) {
            case 1:
                return "GET";
            case 2:
                return "POST";
            case 3:
                return "PUT";
            default:
                return "UNKNOWN";
        }
    }

    void initSSLNoVerify() {
        try {
            TrustManager[] victimizedManager = new TrustManager[]{
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() {
                            X509Certificate[] myTrustedAnchors = new X509Certificate[0];
                            return myTrustedAnchors;
                        }

                        @Override
                        public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        }

                        @Override
                        public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        }
                    }
            };
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, victimizedManager, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String s, SSLSession sslSession) {
                    return true;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class Request {

        private Intent mIntent;
        public Map<String, String> mParamers;
        public Map<String, String> mHeaders;

        public Request(String url, int code, int method) {
            mParamers = new LinkedHashMap<>();
            mHeaders = new LinkedHashMap<>();
            mIntent = new Intent("web_service");
            mIntent.putExtra("url", url);
            mIntent.putExtra("requestcode", code);
            mIntent.putExtra("method", method);
        }

        public void go() {
            mIntent.putExtra("params", (Serializable) mParamers);
            mIntent.putExtra("headers", (Serializable) mHeaders);
            LocalBroadcastManager.getInstance(App.getContext()).sendBroadcast(mIntent);
        }
    }

    private URLConnection urlConnection(URL url) throws IOException {
        if (url.getProtocol().contains("https")) {
            return (HttpsURLConnection) url.openConnection();
        } else {
            return (HttpURLConnection) url.openConnection();
        }
    }

    Runnable looper = new Runnable() {
        @Override
        public void run() {
            do {
                int webResponse = 0;
                int requestCode = 0;
                StringBuilder sb = new StringBuilder();
                Intent intent = null;
                try {
                    intent = mMessages.take();
                    Map<String, String> parameters = (Map<String, String>) intent.getSerializableExtra("params");
                    Map<String, String> headers = (Map<String, String>) intent.getSerializableExtra("headers");
                    String urlString = intent.getStringExtra("url");
                    int method = intent.getIntExtra("method", mMethodGET);
                    requestCode = intent.getIntExtra("requestcode", 0);

                    String urlParameters = new String();
                    for (Map.Entry<String, String> e: parameters.entrySet()) {
                        if (!urlParameters.isEmpty()) {
                            urlParameters += "&";
                        }
                        urlParameters += e.getKey() + "=" + e.getValue();
                    }
                    if (urlParameters.length() > 0 && method == mMethodGET) {
                        urlString += "?" + urlParameters;
                    }

                    URL url = new URL(urlString);
                    URLConnection urlcon  = urlConnection(url);
                    urlcon.setConnectTimeout(4000);
                    ((HttpURLConnection) urlcon).setRequestMethod(getMethodName(method));
                    urlcon.setDoOutput(true);
                    urlcon.setDoInput(true);
                    for (Map.Entry<String, String> e: headers.entrySet()) {
                        urlcon.setRequestProperty(e.getKey(), e.getValue());
                    }

                    OutputStream out = urlcon.getOutputStream();
                    out.write(urlParameters.getBytes(StandardCharsets.UTF_8));
                    out.flush();
                    out.close();
                    webResponse = ((HttpURLConnection) urlcon).getResponseCode();

                    InputStream is = null;
                    if (webResponse < 400) {
                        is =  urlcon.getInputStream();
                    } else {
                        is = ((HttpURLConnection) urlcon).getErrorStream();
                    }
                    InputStreamReader ir = new InputStreamReader(is);
                    BufferedReader br = new BufferedReader(ir);
                    String inputLine;
                    while ((inputLine = br.readLine()) != null) {
                        sb.append(inputLine);
                    }
                    is.close();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    sb.append(e.getMessage());
                } catch ( MalformedURLException e) {
                    e.printStackTrace();
                    sb.append(e.getMessage());
                } catch (IOException e) {
                    e.printStackTrace();
                    sb.append(e.getMessage());
                }

                intent = new Intent("web_response");
                intent.putExtra("requestcode", requestCode);
                intent.putExtra("data", sb.toString());
                intent.putExtra("webresponse", webResponse);
                LocalBroadcastManager.getInstance(WebService.this).sendBroadcast(intent);

                Log.d(String.format("WEBREQUEST, %d", sb.length()), sb.toString());
            } while (true);
        }
    };

    BroadcastReceiver listener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mMessages.add(intent);
        }
    };
}
