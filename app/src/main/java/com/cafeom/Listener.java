package com.cafeom;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Listener extends Service {

    public static String IntentId = "MESSAGE";

    Handler handleClientSocket = new Handler();
    ServerThread mServerThread;
    Future mServerThreadFuture;

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        try {
            if (mServerThread != null) {
                mServerThread.closeSocket();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        mServerThreadFuture.cancel(true);
        Intent restartServiceIntent = new Intent(getApplicationContext(),this.getClass());
        restartServiceIntent.setPackage(getPackageName());
        startService(restartServiceIntent);
        super.onTaskRemoved(rootIntent);
    }


    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= 26) {
            String CHANNEL_ID = "my_channel_01";
            Uri soundUri = Uri.parse("android.resource://" + getApplicationContext().getPackageName() + "/" + R.raw.notification);
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,"Channel human readable title", NotificationManager.IMPORTANCE_DEFAULT);
            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentTitle("START")
                    .setContentText("TEXT")
                    .setSound(soundUri);
            startForeground(1, builder.build());
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        task();
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    void sendBroadcastMessage(String message) {
        Intent intent = new Intent(Listener.IntentId);
        intent.putExtra("msg", message);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
        NotificationSender.send(this, "NEW MESSAGE", message);
    }

    public void task() {
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        mServerThread = new ServerThread();
        mServerThreadFuture = executorService.submit(mServerThread);
    }

    class ServerThread implements Runnable {
        ServerSocket mServerSocket;
        Socket mSocket;

        public ServerThread() {
            try {
                mServerSocket = new ServerSocket(2000);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    mSocket = mServerSocket.accept();
                    ClientSocketThread clientSocketThread = new ClientSocketThread(mSocket);
                    new Thread(clientSocketThread).start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void closeSocket() {
            try {
                mServerSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    class ClientSocketThread implements Runnable {
        Socket mSocket;
        BufferedReader mReader;
        BufferedWriter mWriter;

        public ClientSocketThread(Socket socket) {
            mSocket = socket;
            try {
                mSocket.setSoTimeout(1000);
                mReader = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
                mWriter = new BufferedWriter(new OutputStreamWriter(mSocket.getOutputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            String s;
            List<String> lines = new LinkedList<>();
            try {
                while ((s = mReader.readLine()) != null) {
                    lines.add(s);
                }
            } catch (SocketTimeoutException e) {

            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                for (String l: lines) {
                    handleClientSocket.post(new MessageHandleThread(l));
                }
                mWriter.write(String.format("OK"));
                mWriter.flush();
                mSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    class MessageHandleThread implements Runnable {
        String mMessage;
        public MessageHandleThread(String s) {
            mMessage = s;
        }
        @Override
        public void run() {
            Listener.this.sendBroadcastMessage(mMessage);
        }
    }
}
