package com.cafeom;

import android.content.Context;
import android.os.AsyncTask;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class DataSocket extends AsyncTask {

    public interface DataReceiver {
        void socketReply(int requestCode, String s, int code);
    }

    public DataReceiver mDataReceiver;
    private Context mContext;
    private String mMessage;
    private int mReplyCode;
    private int mRequestCode;

    public DataSocket(String s, Context context, int requestCode) {
        mContext = context;
        mMessage = s;
        mRequestCode = requestCode;
        mDataReceiver = null;
    }

    @Override
    protected Object doInBackground(Object[] objects) {
        if (Cnf.getString(mContext, "server_address").isEmpty()) {
            mReplyCode = 0;
            mMessage = "Setup server address";
            return mReplyCode;
        }
        try {
            Socket s = new Socket();
            s.setSoTimeout(3000);
            s.connect(new InetSocketAddress(Cnf.getString(mContext, "server_address"), Integer.valueOf(Cnf.getString(mContext, "server_port"))));
            OutputStream dos = s.getOutputStream();
            ByteBuffer bb = ByteBuffer.allocate(4);
            bb.order(ByteOrder.LITTLE_ENDIAN);
            bb.putInt(mMessage.getBytes("UTF-8").length);
            byte[] bytes = bb.array();
            dos.write(bytes, 0, 4);
            bytes = mMessage.getBytes("UTF-8");
            dos.write(bytes, 0, bytes.length);
            dos.flush();

            InputStream is = s.getInputStream();
            DataInputStream dis = new DataInputStream(is);
            byte [] b = new byte[4];
            dis.read(b, 0, 4);
            bb.clear();
            bb.put(b);
            bb.position(0);
            Integer datasize = bb.getInt();
            mMessage = "";
            while (dis.available() > 0 || datasize > 0) {
                int pt;
                byte [] bbb = new byte[8192];
                pt = dis.read(bbb, 0, 8192);
                datasize -= pt;
                mMessage += new String(bbb, 0, pt);
            }
            mReplyCode = mMessage.length();
            s.close();
        } catch (IOException e) {
            e.printStackTrace();
            mMessage = e.getMessage();
            mReplyCode = 0;
        }
        return mReplyCode;
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
        if (mDataReceiver != null) {
            mDataReceiver.socketReply(mRequestCode, mMessage, mReplyCode);
        } else {
            if (mContext != null) {
                MainActivity ma = (MainActivity) mContext;
                ma.socketReply(mRequestCode, mMessage, mReplyCode);
            }
        }
    }
}
