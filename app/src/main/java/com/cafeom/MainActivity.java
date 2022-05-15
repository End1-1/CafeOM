package com.cafeom;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cafeom.common.AppService;
import com.cafeom.common.MessageList;
import com.cafeom.common.MessageMaker;
import com.cafeom.databinding.ActivityMainBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.UUID;

public class MainActivity extends ActivityClass {

    private ActivityMainBinding _b;
    private Map<Integer, CookItem> mCookItems;
    private DishAdapters mDishAdapter;
    protected PowerManager.WakeLock mWakeLock;
    private MediaPlayer mMediaPlayer = null;
    private Timer mTimer;
    private boolean mNetworkOk = false;
    private int mTimerCounter = 0;
    private boolean mPlayWaterdrop = false;

    public MainActivity() {
        mCookItems = new TreeMap<>();
    }

    private static final int PERMISSION_CAMERA_REQUEST = 1;

    public static final int RC_DISHESLIST = 1;
    public static final int RC_DISHSTART = 2;
    public static final int RC_DISHREADY = 3;
    public static final int RC_DISHREMOVE = 4;
    public static final int RC_DISHRECEIVED = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _b = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(_b.getRoot());
        if (Cnf.getString("uuid").isEmpty()) {
            Cnf.setString("uuid", UUID.randomUUID().toString());
        }
        TextView tvDept = findViewById(R.id.tvDept);
        String reminerId = Cnf.getString("reminder_id");
        if (reminerId == null) {
            reminerId = "0";
        }
        if (reminerId.isEmpty()) {
            reminerId = "0";
        }
        switch (Integer.valueOf(reminerId)) {
            case 0:
                tvDept.setText("*");
                break;
            case 1:
                tvDept.setText(getString(R.string.Kitchen));
                break;
            case 2:
                tvDept.setText(getString(R.string.Bar));
                break;
        }
        NotificationSender.cancelAll(this);

        mDishAdapter = new DishAdapters();
        _b.rv.setLayoutManager(new GridLayoutManager(this, 1));
        _b.rv.setAdapter(mDishAdapter);
        _b.ivConfig.setOnClickListener(this);

        final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "com.cafeom:wakeeetag");
        mWakeLock.acquire();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, PERMISSION_CAMERA_REQUEST);
        }

    }

    @Override
    protected void onStart() {
        super.onStart();

        if (isServiceRunning(AppService.class)) {
            Intent intent = new Intent(MessageMaker.BROADCAST_DATA);
            intent.putExtra("local", true);
            intent.putExtra("type", MessageList.check_connection);
            intent.putExtra("request", true);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        } else {
            Intent srvLocation = new Intent(this, AppService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(srvLocation);
            } else {
                startService(srvLocation);
            }
        }

        mTimer = new Timer();
        mTimer.schedule(new RegularTask(), 1000, 1000);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mTimer.cancel();
        mTimer = null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCookItems.clear();
        mDishAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        mWakeLock.release();
        super.onDestroy();
    }

    @SuppressWarnings("deprecation")
    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ivConfig:
                Intent intent = new Intent(this, ActivityCodeReader.class);
                mCodeResult.launch(intent);
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_CAMERA_REQUEST:
                break;
        }
    }

    void playSound(int res) {
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        mMediaPlayer = MediaPlayer.create(this, res);
        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (mMediaPlayer != null) {
                    mMediaPlayer.reset();
                    mMediaPlayer.release();
                    mMediaPlayer = null;
                }
            }
        });
        mMediaPlayer.start();
    }

    public CookItem getItemByIndex(int index) {
        int pos = 0;
        for ( Map.Entry<Integer , CookItem> e: mCookItems.entrySet() ) {
            if (pos == index) {
                return e.getValue();
            }
            pos++;
        }
        return new CookItem();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mCookItems.clear();
        mDishAdapter.notifyDataSetChanged();
    }

    @Override
    protected void messageHandler(Intent intent) {
        dismissProgressDialog();
        _b.progressBar2.setVisibility(View.INVISIBLE);
        if (intent.getBooleanExtra(MessageMaker.NETWORK_ERROR, false)) {
            DialogClass.error(this, getString(R.string.Network_error));
            return;
        }
        switch (intent.getShortExtra("type", (short) 0)) {
            case MessageList.connection:
                mPlayWaterdrop = true;
                mNetworkOk = false;
                mCookItems.clear();
                mDishAdapter.notifyDataSetChanged();
                if (intent.getBooleanExtra("value", false)) {
                    _b.ivConfig.setImageResource(R.drawable.wifib);
                } else {
                    _b.ivConfig.setImageResource(R.drawable.wifi_off);
                }
                break;
            case MessageList.login_status:
                if (intent.getBooleanExtra("value", false)) {
                    _b.ivConfig.setImageResource(R.drawable.wifi_on);
                    mPlayWaterdrop = false;
                    mNetworkOk = true;
                }
                break;
            case MessageList.dll_op:
                byte[] data = intent.getByteArrayExtra("data");
                MessageMaker mm = new MessageMaker(MessageList.utils);
                byte op = mm.getByte(data);
                if (op < 3) {
                    String err = mm.getString(data);
                    DialogClass.error(this, err);
                    return;
                }
                byte success = mm.getByte(data);
                if (success == 0) {
                    String error = mm.getString(data);
                    DialogClass.error(this, error);
                    return;
                }
                switch (op) {
                    case DllOp.op_get_list:
                        String s = mm.getString(data);
                        parseList(s);
                        break;
                    case DllOp.op_update_state:
                        int recid = mm.getInt(data);
                        byte newstate = mm.getByte(data);
                        updateDishState(recid, newstate);
                        break;
                }
                break;
        }
    }

    private void parseList(String s) {
        boolean hasNew = false;
        try {
            JSONArray ja = new JSONArray(s);
            for (int i = 0; i < ja.length(); i++) {
                JSONObject jo = ja.getJSONObject(i);
                CookItem ci = new CookItem();
                ci.mState = jo.getInt("state");
                ci.mRecord = jo.getInt("rec");
                ci.mTime = jo.getString("time");
                ci.mTable = jo.getString("table");
                ci.mDish = jo.getString("dish");
                ci.mQty = jo.getString("qty");
                ci.mStaff = jo.getString("staff");
                ci.mComment = jo.getString("comment");
                if (mCookItems.containsKey(ci.mRecord)) {

                } else {
                    mCookItems.put(ci.mRecord, ci);
                    hasNew = true;
                }
            }
        } catch (JSONException e){
            e.printStackTrace();
        }
        mDishAdapter.notifyDataSetChanged();
        if (hasNew) {
            playSound(R.raw.notification);
        }
    }

    private void updateDishState(int recid, byte newstate) {
        if (mCookItems.containsKey(recid)) {
            CookItem ci = mCookItems.get(recid);
            ci.mState = newstate;
            ci.btnDoneVisibility = View.VISIBLE;
            if (newstate > 2) {
                mCookItems.remove(Integer.valueOf(ci.mRecord));
            }
            mDishAdapter.notifyDataSetChanged();
        }
    }

    public class CookItem {
        String mStaff;
        int mRecord;
        String mTime;
        String mTable;
        String mDish;
        String mQty;
        String mComment;
        int mState;

        int btnDoneVisibility;
        int btnYesVisibility;
        int btnNoVisibility;

        public CookItem() {
            btnDoneVisibility = View.VISIBLE;
            btnYesVisibility = View.GONE;
            btnNoVisibility = View.GONE;

        }
    }

    public class DishAdapters extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        public class VH extends RecyclerView.ViewHolder implements View.OnClickListener {

            CookItem mItem;
            TextView tvTime;
            TextView tvTable;
            TextView tvDish;
            TextView tvQty;
            TextView tvStaff;
            TextView tvComment;
            Button btnDone;
            Button btnYes;
            Button btnNo;
            ImageButton btnRemove;

            public VH(View v) {
                super(v);
                tvTime = v.findViewById(R.id.tvTime);
                tvTable = v.findViewById(R.id.tvTable);
                tvDish = v.findViewById(R.id.tvDish);
                tvQty = v.findViewById(R.id.tvQty);
                tvStaff = v.findViewById(R.id.tvStaff);
                tvComment = v.findViewById(R.id.tvComment);
                btnDone = v.findViewById(R.id.btn);
                btnDone.setOnClickListener(this);
                btnRemove = v.findViewById(R.id.btnRemove);
                btnRemove.setOnClickListener(this);
                btnYes = v.findViewById(R.id.btnYes);
                btnYes.setOnClickListener(this);
                btnNo = v.findViewById(R.id.btnNo);
                btnNo.setOnClickListener(this);
            }

            void onBind(int position) {
                CookItem ci = getItemByIndex(position);
                mItem = ci;
                tvTime.setText(ci.mTime);
                tvTable.setText(ci.mTable);
                tvDish.setText(ci.mDish);
                tvQty.setText(ci.mQty);
                tvStaff.setText(ci.mStaff);
                tvComment.setText(ci.mComment);
                if (ci.mComment.isEmpty()) {
                    tvComment.setVisibility(View.GONE);
                } else {
                    tvComment.setVisibility(View.VISIBLE);
                }
                btnDone.setText(getButtonTitle(ci.mState));
                btnDone.setVisibility(ci.btnDoneVisibility);
                btnYes.setVisibility(ci.btnYesVisibility);
                btnNo.setVisibility(ci.btnNoVisibility);
            }

            String getButtonTitle(int state) {
                switch (state) {
                    case 0:
                    case 1:
                        return getString(R.string.StartCooking);
                    case 2:
                        return getString(R.string.Ready);
                }
                return String.format("%d", state);
            }

            void changeState(final CookItem ci) {
                _b.progressBar2.setVisibility(View.VISIBLE);
                MessageMaker messageMaker = new MessageMaker(MessageList.dll_op);
                messageMaker.putString("rwjz");
                messageMaker.putString(Cnf.getString("server_database"));
                messageMaker.putByte(DllOp.op_update_state);
                messageMaker.putInteger(ci.mRecord);
                switch (ci.mState) {
                    case 0:
                    case 1:
                        messageMaker.putByte((byte) 2);
                    case 2:
                        messageMaker.putByte((byte) 3);
                    default:
                        break;
                }
                sendMessage(messageMaker);
                playSound(R.raw.click);
            }

            void removeDish(final CookItem ci) {
                String dialogMsg = getString(R.string.Remove) + "?\r\n" + ci.mDish + "\r\n" + ci.mQty;
                DialogClass.question(MainActivity.this, dialogMsg, new DialogClass.DialogYesNo() {
                    @Override
                    public void yes() {
                        _b.progressBar2.setVisibility(View.VISIBLE);
                        MessageMaker messageMaker = new MessageMaker(MessageList.dll_op);
                        messageMaker.putString("rwjz");
                        messageMaker.putString(Cnf.getString("server_database"));
                        messageMaker.putByte(DllOp.op_update_state);
                        messageMaker.putInteger(ci.mRecord);
                        messageMaker.putByte((byte) 5);
                        sendMessage(messageMaker);
                        playSound(R.raw.click);
                    }

                    @Override
                    public void no() {

                    }
                });
            }

            @Override
            public void onClick(View v) {
                if (mItem == null) {
                    return;
                }
                CookItem ci = mItem;
                //doNotUpdateAtTheThisTime = true;
                switch (v.getId()) {
                    case R.id.btn:
                        ci.btnYesVisibility = View.VISIBLE;
                        ci.btnNoVisibility = View.VISIBLE;
                        ci.btnDoneVisibility = View.GONE;
                        break;
                    case R.id.btnRemove:
                        if (_b.progressBar2.getVisibility() == View.VISIBLE) {
                            return;
                        }
                        removeDish(ci);
                        break;
                    case R.id.btnYes:
                        if (_b.progressBar2.getVisibility() == View.VISIBLE) {
                            return;
                        }
                        ci.btnYesVisibility = View.GONE;
                        ci.btnNoVisibility = View.GONE;
                        changeState(ci);
                        break;
                    case R.id.btnNo:
                        ci.btnYesVisibility = View.GONE;
                        ci.btnNoVisibility = View.GONE;
                        ci.btnDoneVisibility = View.VISIBLE;
                        break;
                }
                notifyDataSetChanged();
            }
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new VH(MainActivity.this.getLayoutInflater().inflate(R.layout.item_dish, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            VH vh = (VH) holder;
            vh.onBind(position);
        }

        @Override
        public int getItemCount() {
            return MainActivity.this.mCookItems.size();
        }
    }

    private class RegularTask extends TimerTask {
        @Override
        public void run() {
            mTimerCounter++;
            if (mPlayWaterdrop) {
                playSound(R.raw.waterdrop);
            }
            if (mTimerCounter % 3 == 0) {
                if (_b.progressBar2.getVisibility() != View.VISIBLE && mNetworkOk) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            _b.progressBar2.setVisibility(View.VISIBLE);
                        }
                    });

                    MessageMaker messageMaker = new MessageMaker(MessageList.dll_op);
                    messageMaker.putString("rwjz");
                    messageMaker.putString(Cnf.getString("server_database"));
                    messageMaker.putByte(DllOp.op_get_list);
                    messageMaker.putInteger(Integer.valueOf(Cnf.getString("reminder_id")).intValue());
                    sendMessage(messageMaker);
                }
            }
        }
    };

    private ActivityResultLauncher<Intent> mCodeResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult activityResult) {
                    if (activityResult.getData() == null) {
                        return;
                    }
                    String code = activityResult.getData().getStringExtra("code");
                    List<String> params = Arrays.asList(code.split(";"));
                    if (params.size() == 7) {
                        Cnf.setString("server_address", params.get(0));
                        Cnf.setString("server_port", params.get(1));
                        Cnf.setString("server_username", params.get(2));
                        Cnf.setString("server_password", params.get(3));
                        Cnf.setString("server_database", params.get(4));
                        Cnf.setString("server_store", params.get(5));
                        Cnf.setString("server_readyonly", params.get(6));
                    }
                }
            });
}
