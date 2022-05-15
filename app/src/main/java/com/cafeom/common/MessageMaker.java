package com.cafeom.common;

import android.content.Intent;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.cafeom.App;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

public class MessageMaker {

    public static final String BROADCAST_DATA = "BROADCAST_DATA";
    public static final String NETWORK_ERROR = "NETWORK_ERROR";

    public byte[] mBuffer;
    public static int mMessageNumber = 1;
    private static int mMessageIdCounter = 1;
    public int mMessageId = 0;
    private static short mMessageType = 0;
    public int mPosition = 0;

    public MessageMaker(short c) {
        mMessageType = c;
        mMessageId = getNewMessageId();
        mBuffer = new byte[]{
                0x03, 0x04, 0x15,               //pattern 0
                0x00, 0x00, 0x00, 0x00,         //packet number 3
                0x00, 0x00, 0x00, 0x00,         //message id 7
                0x00, 0x00,                     //command 11
                0x00, 0x00, 0x00, 0x00          //data size 13
        };
        ByteBuffer bb = ByteBuffer.allocate(4);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        byte[] buf = bb.putInt(mMessageId).array();
        System.arraycopy(buf, 0, mBuffer, 7, 4);
        bb = ByteBuffer.allocate(2);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        buf = bb.putShort(c).array();
        System.arraycopy(buf, 0, mBuffer, 11, 2);
    }

    public synchronized int getNewMessageId() {
        return mMessageIdCounter++;
    }

    public double getDouble(byte[] bytes) {
        ByteBuffer bb = ByteBuffer.allocate(8);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.put(bytes, mPosition, 8);
        mPosition += 8;
        return bb.getDouble(0);
    }

    public byte getByte(byte[] bytes) {
        return bytes[mPosition++];
    }

    public int getInt(byte[] bytes) {
        ByteBuffer bb = ByteBuffer.allocate(4);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.put(bytes, mPosition, 4);
        mPosition += 4;
        return bb.getInt(0);
    }

    public short getShort(byte[] bytes) {
        ByteBuffer bb = ByteBuffer.allocate(2);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.put(bytes[mPosition]);
        bb.put(bytes[mPosition + 1]);
        mPosition += 2;
        return bb.getShort(0);
    }

    private void correctSize(int delta) {
        byte[] buf = new byte[4];
        System.arraycopy(mBuffer, 13, buf, 0, 4);
        int newSize = new MessageMaker(com.cafeom.common.MessageList.utils).getInt(buf) + delta;
        ByteBuffer bb = ByteBuffer.allocate(4);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        buf = bb.putInt(newSize).array();
        System.arraycopy(buf, 0, mBuffer, 13, 4);
    }

    public int getMessageId() {
        byte[] bb = new byte[4];
        System.arraycopy(mBuffer, 7, bb, 0, 4);
        return new MessageMaker(com.cafeom.common.MessageList.utils).getInt(bb);
    }

    public short getType() {
        byte[] bb = new byte[2];
        System.arraycopy(mBuffer, 11, bb, 0, 2);
        return new MessageMaker(com.cafeom.common.MessageList.utils).getShort(bb);
    }

    public void putByte(byte i) {
        ByteBuffer bb = ByteBuffer.allocate(1);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        byte[] bytes = bb.put(i).array();
        ByteBuffer out = ByteBuffer.allocate(bytes.length + mBuffer.length);
        out.put(mBuffer);
        out.put(bytes);
        mBuffer = out.array();
        correctSize(1);
    }

    public void putShort(short i) {
        ByteBuffer bb = ByteBuffer.allocate(2);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        byte[] bytes = bb.putShort(i).array();
        ByteBuffer out = ByteBuffer.allocate(bytes.length + mBuffer.length);
        out.put(mBuffer);
        out.put(bytes);
        mBuffer = out.array();
        correctSize(2);
    }

    public void putInteger(int i) {
        ByteBuffer bb = ByteBuffer.allocate(4);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        byte[] bytes = bb.putInt(i).array();
        ByteBuffer out = ByteBuffer.allocate(bytes.length + mBuffer.length);
        out.put(mBuffer);
        out.put(bytes);
        mBuffer = out.array();
        correctSize(4);
    }

    public void putDouble(double i) {
        ByteBuffer bb = ByteBuffer.allocate(8);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        byte[] bytes = bb.putDouble(i).array();
        ByteBuffer out = ByteBuffer.allocate(bytes.length + mBuffer.length);
        out.put(mBuffer);
        out.put(bytes);
        mBuffer = out.array();
        correctSize(8);
    }

    public void putString(String s) {
        int strSize = s.getBytes(StandardCharsets.UTF_8).length + 1;
        ByteBuffer bb = ByteBuffer.allocate(4 + strSize);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.putInt(strSize);
        bb.put(s.getBytes(StandardCharsets.UTF_8));
        bb.put((byte) 0x00);
        ByteBuffer out = ByteBuffer.allocate(mBuffer.length + bb.array().length);
        out.put(mBuffer);
        out.put(bb.array());
        mBuffer = out.array();
        correctSize(strSize + 4);
    }

    public String getString(byte[] data) {
        ByteBuffer bb = ByteBuffer.allocate(data.length);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.put(data);
        bb.position(mPosition);
        int sz;
        sz = bb.getInt();
        mPosition += 4;
        byte[] strbuf = new byte[sz];
        bb.get(strbuf, 0, sz);
        mPosition += sz;
        return new String(strbuf);
    }

    public byte[] getBytes(byte[] data) {
        ByteBuffer bb = ByteBuffer.allocate(data.length);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.put(data);
        bb.position(mPosition);
        int sz = bb.getInt();
        mPosition += 4;
        byte[] buf = new byte[sz];
        bb.get(buf, 0, sz);
        mPosition += sz;
        return buf;
    }

    public void setPacketNumber() {
        ByteBuffer msgNumberBytes = ByteBuffer.allocate(4);
        msgNumberBytes.order(ByteOrder.LITTLE_ENDIAN);
        msgNumberBytes.putInt(MessageMaker.getMessageNumber()).array();
        for (int i = 0; i < msgNumberBytes.array().length; i++) {
            mBuffer[i + 3] = msgNumberBytes.array()[i];
        }
    }

    public static int getMessageNumber() {
        return mMessageNumber++;
    }

    public int send() {
        Intent intent = new Intent(MessageMaker.BROADCAST_DATA);
        intent.putExtra("socket", true);
        intent.putExtra("data", mBuffer);
        LocalBroadcastManager.getInstance(App.getContext()).sendBroadcast(intent);
        return mMessageId;
    }
}
