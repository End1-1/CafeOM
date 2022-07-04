package com.cafeom.common;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.cafeom.App;
import com.cafeom.Cnf;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class ServiceSocket {

    private SocketThread mSocketThread;
    private boolean mStopped = false;
    private boolean mConnected = false;
    private boolean mResetConnection = false;
    public BlockingDeque<byte[]> mMessageBuffer = new LinkedBlockingDeque<>();

    public ServiceSocket() {
        LocalBroadcastManager.getInstance(App.getContext()).registerReceiver(mReceiver, new IntentFilter(MessageMaker.BROADCAST_DATA));
    }

    public void startSocketThread() {
        if (mStopped) {
            return;
        }
        mSocketThread = new SocketThread();
        new Thread(mSocketThread).start();
    }

    public void stop() {
        mStopped = true;
    }

    public class SocketThread implements Runnable {
        OutputStream mOutputStream;
        InputStream mInputStream;
        boolean mIsStopped;
        byte [] mBuffer;
        int mBufferPos;
        int mBytesLeft;

        public SocketThread() {
            mIsStopped = true;
            mBytesLeft = -1;
        }

        SSLSocketFactory sslSocketFactory() {
            SSLContext sc = null;
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
                sc = SSLContext.getInstance("TLS");
                sc.init(null, victimizedManager, new SecureRandom());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return sc.getSocketFactory();
        }

        @Override
        public void run() {
            try {
                SSLSocketFactory sf = sslSocketFactory();
                SSLSocketFactory factory = sf; //(SSLSocketFactory) SSLSocketFactory.getDefault();
                SSLSocket socket = (SSLSocket) factory.createSocket();
                if (Cnf.getString("server_address").isEmpty()) {
                    Thread.sleep(2000);
//                    Runnable r = new Runnable() {
//                        @Override
//                        public void run() {
//                            startSocketThread();
//                        }
//                    };
//                    new Thread(r).start();
//                    return;
                }
                socket.connect(new InetSocketAddress(Cnf.getString("server_address"), Integer.valueOf(Cnf.getString("server_port"))), 3000);
                socket.setSoTimeout(3000);
                socket.startHandshake();

                mConnected = true;
                Intent connectedIntent = new Intent(MessageMaker.BROADCAST_DATA);
                connectedIntent.putExtra("type", MessageList.connection);
                connectedIntent.putExtra("local", true);
                connectedIntent.putExtra("value", true);
                LocalBroadcastManager.getInstance(App.getContext()).sendBroadcast(connectedIntent);
                System.out.println("SOCKET CONNECTED SUCCESSFULLY");

                mIsStopped = false;
                mInputStream = socket.getInputStream();
                mOutputStream = socket.getOutputStream();
                MessageMaker.mMessageNumber = 1;
                MessageMaker messageMaker = new MessageMaker(MessageList.hello);
                messageMaker.putString(Cnf.getString("uuid"));
                messageMaker.setPacketNumber();
                mOutputStream.write(messageMaker.mBuffer);
                mOutputStream.flush();
                mBytesLeft = -1;
                socket.setSoTimeout(10);
                loopEvents();
                mOutputStream.close();
                mInputStream.close();
                socket.close();
            } catch (Exception e){
                Date c = Calendar.getInstance().getTime();
                SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss", Locale.getDefault());
                System.out.println(df.format(c));
                e.printStackTrace();
                Cnf.setString("last_network_error", e.getMessage());
            }

            mConnected = false;
            Intent connectedIntent = new Intent(MessageMaker.BROADCAST_DATA);
            connectedIntent.putExtra("type", MessageList.connection);
            connectedIntent.putExtra("local", true);
            connectedIntent.putExtra("value", false);
            LocalBroadcastManager.getInstance(App.getContext()).sendBroadcast(connectedIntent);
            System.out.println("SOCKET DISCONNECTED");

            Runnable r = new Runnable() {
                @Override
                public void run() {
                    startSocketThread();
                }
            };
            new Thread(r).start();
        }

        public void loopEvents() {
            do {
                try {
                    while (mMessageBuffer.size() > 0) {
                        ByteBuffer msgNumberBytes = ByteBuffer.allocate(4);
                        msgNumberBytes.order(ByteOrder.LITTLE_ENDIAN);
                        msgNumberBytes.putInt(MessageMaker.getMessageNumber()).array();
                        byte[] msg = mMessageBuffer.take();
                        for (int i = 0; i < msgNumberBytes.array().length; i++) {
                            msg[i + 3] = msgNumberBytes.array()[i];
                        }
                        mOutputStream.write(msg);
                        mOutputStream.flush();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                short tempSize = 16384;
                byte [] bytes = new byte[tempSize];
                byte [] buffPattern = new byte[3];
                byte [] buffMsgNum = new byte[4];
                byte [] buffMsgId = new byte[4];
                byte [] buffMsgType = new byte[2];
                byte [] buffPacketSize = new byte[4];
                int br = 0;
                int msgNum = 0;
                int msgId = 0;
                short msgType = 0;
                try {
                    if (mBytesLeft == -1) {
                        br = mInputStream.read(bytes, 0,17);
                        if (br == -1) {
                            Log.d("CONNECTION CLOSED, probably by remote host: ", "Bye #1");
                            return;
                        }
                        if (br < 17) {
                            Log.d("CONNECTION CLOSED: ", "Bye #1");
                            return;
                        }
                        if (br == 17) {
                            System.arraycopy(bytes, 0, buffPattern, 0, 3);
                            System.arraycopy(bytes, 3, buffMsgNum, 0, 4);
                            System.arraycopy(bytes, 7, buffMsgId, 0, 4);
                            System.arraycopy(bytes, 11, buffMsgType, 0, 2);
                            System.arraycopy(bytes, 13, buffPacketSize, 0, 4);
                            msgNum = new MessageMaker(MessageList.utils).getInt(buffMsgNum);
                            msgId = new MessageMaker(MessageList.utils).getInt(buffMsgId);
                            msgType = new MessageMaker(MessageList.utils).getShort(buffMsgType);
                            mBytesLeft = new MessageMaker(MessageList.utils).getInt(buffPacketSize);
                            mBuffer = new byte[mBytesLeft];
                            mBufferPos = 0;
                        }
                    }
                    while (mBytesLeft > 0) {
                        br = mInputStream.read(bytes, 0, mBytesLeft < tempSize ? mBytesLeft : tempSize);
                        if (br < 0) {
                            Log.d("CONENCTION CLOSED: ", "Bye #2");
                            return;
                        }
                        System.arraycopy(bytes, 0, mBuffer, mBufferPos, br);
                        mBytesLeft -= br;
                        mBufferPos += br;
                        if (mBytesLeft == 0) {
                            parseData(msgType, msgNum, msgId, mBuffer);
                        }
                    }
                    mBuffer = null;
                    mBytesLeft = -1;
                } catch (SocketTimeoutException e) {
                    //Log.d("SOCKET READ TIMEOUT", "LOOP EVENT");
                    //e.printStackTrace();
                } catch (SocketException e) {
                    mIsStopped = true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } while (!mIsStopped && !Thread.currentThread().isInterrupted() && !mResetConnection);
            mResetConnection = false;
            mBuffer = null;
            mBufferPos = 0;
            mBytesLeft = 0;
            Log.d("IM QUIT FROOM LOOP", mIsStopped ?  "STOPPED" : "NOT STOPPED");
        }
    }

    private void parseData(short msgType, int msgNum, int msgId, byte[] data) {
        Intent intent = new Intent(MessageMaker.BROADCAST_DATA);
        intent.putExtra("local", true);
        intent.putExtra("type", msgType);
        intent.putExtra("num", msgNum);
        intent.putExtra("id", msgId);
        intent.putExtra("data", data);
        LocalBroadcastManager.getInstance(App.getContext()).sendBroadcast(intent);
    }

    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getBooleanExtra("local", false)) {
                switch (intent.getShortExtra("type", (short) 0)) {
                    case MessageList.reset_connection:
                        mResetConnection = true;
                        break;
                    case MessageList.check_connection:
                        if (intent.getBooleanExtra("request", false)) {
                            Intent connectionStatusIntent = new Intent(MessageMaker.BROADCAST_DATA);
                            connectionStatusIntent.putExtra("local", true);
                            connectionStatusIntent.putExtra("type", MessageList.check_connection);
                            connectionStatusIntent.putExtra("connected", mConnected);
                            LocalBroadcastManager.getInstance(App.getContext()).sendBroadcast(connectionStatusIntent);
                        }
                        break;
                }
            }
            if (intent.getBooleanExtra("socket", false)) {
                byte[] data = intent.getByteArrayExtra("data");
                mMessageBuffer.add(data);
            }
        }
    };
}
