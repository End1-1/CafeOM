package com.cafeom;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Map<Integer, CookItem> mCookItems;
    DishAdapters mDishAdapter;
    protected PowerManager.WakeLock mWakeLock;
    private MediaPlayer mMediaPlayer = null;
    private boolean first = true;
    private boolean stopThread = false;
    private boolean doNotUpdateAtTheThisTime = false;

    public MainActivity() {
        mCookItems = new TreeMap<>();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//will hide the title
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);
        //Cnf.setString(this, "cnf_password", "111");
        //Cnf.setString(this, "server_address", "10.1.0.2");
        //Cnf.setString(this, "server_port", "888");
        TextView tvDept = findViewById(R.id.tvDept);
        String reminerId = Cnf.getString(this, "reminder_id");
        if (reminerId == null) {
            reminerId = "0";
        }
        if (reminerId.isEmpty()) {
            reminerId = "0";
        }
        switch (Integer.valueOf(reminerId)) {
            case 0:
                tvDept.setText(getString(R.string.AllDepartments));
                break;
            case 1:
                tvDept.setText(getString(R.string.Kitchen));
                break;
            case 2:
                tvDept.setText(getString(R.string.Bar));
                break;
        }
        NotificationSender.cancelAll(this);

        RecyclerView rv = findViewById(R.id.rvDishes);
        mDishAdapter = new DishAdapters();
        rv.setLayoutManager(new GridLayoutManager(this, 1));
        rv.setAdapter(mDishAdapter);
        findViewById(R.id.ivConfig).setOnClickListener(this);

        final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "com.cafeom:wakeeetag");
        mWakeLock.acquire();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        first = true;
        mCookItems.clear();
        mDishAdapter.notifyDataSetChanged();
        stopThread = false;
        new Thread(new Updater()).start();
    }

    @Override
    protected void onDestroy() {
        mWakeLock.release();
        super.onDestroy();
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
        stopThread = true;
        mCookItems.clear();
        mDishAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ivConfig:
                Intent intent = new Intent(this, Config.class);
                startActivity(intent);
                break;
        }
    }

    public void compareArrays(ArrayList<CookItem> items) {
        mCookItems.clear();
        boolean playSound = false;
        boolean readyOnly = Cnf.getBoolean(this, "readyonly");
        for (CookItem ci: items) {
            if (ci == null) {
                continue;
            }
            mCookItems.put(Integer.valueOf(ci.mRecord), ci);
            if (ci.mState == 0) {
                playSound = true;
                try {
                    if (readyOnly) {
                        ci.mState = 2;
                    }
                    JSONObject jo = new JSONObject();
                    jo.put("c", 2);
                    jo.put("started", new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault()).format(new Date()));
                    jo.put("readyonly", readyOnly ? "1" : "0");
                    jo.put("state", readyOnly ? 2 : 1);
                    jo.put("rec", ci.mRecord);
                    DataSocket ds = new DataSocket(jo.toString(), MainActivity.this, 2);
                    ds.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } catch (JSONException e) {
                    e.printStackTrace();
                    DbEx.regException(this, e.getMessage());
                }
            }
        }
        if (items.size() > 0) {
            if (playSound) {
                playSound(R.raw.notification);
            }
        }
        mDishAdapter.notifyDataSetChanged();
    }

    public class CookItem {
        String mStaff;
        String mRecord;
        String mTime;
        String mTable;
        String mDish;
        String mQty;
        String mComment;
        int mState;

        public CookItem() {

        }

        public CookItem(String time, String table, String dish, String qty, int state) {
            mTime = time;
            mTable = table;
            mDish = dish;
            mQty = qty;
            mState = state;
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
            Button mButton;
            ImageButton btnRemove;

            public VH(View v) {
                super(v);
                tvTime = v.findViewById(R.id.tvTime);
                tvTable = v.findViewById(R.id.tvTable);
                tvDish = v.findViewById(R.id.tvDish);
                tvQty = v.findViewById(R.id.tvQty);
                tvStaff = v.findViewById(R.id.tvStaff);
                tvComment = v.findViewById(R.id.tvComment);
                mButton = v.findViewById(R.id.btn);
                mButton.setOnClickListener(this);
                btnRemove = v.findViewById(R.id.btnRemove);
                btnRemove.setOnClickListener(this);
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
                mButton.setText(getButtonTitle(ci.mState));
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

            void changeState(CookItem ci) {
                DataSocket ds;
                switch (ci.mState) {
                    case 0:
                    case 1:
                        try {
                            JSONObject jo = new JSONObject();
                            jo.put("c", 2);
                            jo.put("state", 2);
                            jo.put("rec", ci.mRecord);
                            jo.put("started", new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault()).format(new Date()));
                            ds = new DataSocket(jo.toString(), MainActivity.this, 2);
                            ds.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                            ci.mState = 2;
                            mButton.setText(getString(R.string.Ready));
                            notifyDataSetChanged();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            DbEx.regException(MainActivity.this, e.getMessage());
                        }
                        break;
                    case 2:
                    default:
                        try {
                            JSONObject jo = new JSONObject();
                            jo.put("c", 2);
                            jo.put("state", 3);
                            jo.put("rec", ci.mRecord);
                            jo.put("ready", new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault()).format(new Date()));
                            ds = new DataSocket(jo.toString(), MainActivity.this, 2);
                            ds.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                            ci.mState = 3;
                            mCookItems.remove(Integer.valueOf(ci.mRecord));
                            notifyDataSetChanged();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            DbEx.regException(MainActivity.this, e.getMessage());
                        }
                        break;
                }
            }

            void removeDish(CookItem ci) {
                try {
                    DataSocket ds;
                    JSONObject jo = new JSONObject();
                    jo.put("c", 2);
                    jo.put("state", 5);
                    jo.put("rec", ci.mRecord);
                    jo.put("ready", new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault()).format(new Date()));
                    ds = new DataSocket(jo.toString(), MainActivity.this, 2);
                    ds.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    ci.mState = 3;
                    mCookItems.remove(Integer.valueOf(ci.mRecord));
                    notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                    DbEx.regException(MainActivity.this, e.getMessage());
                }
            }

            @Override
            public void onClick(View v) {
                playSound(R.raw.click);
                if (mItem == null) {
                    return;
                }
                CookItem ci = mItem;
                doNotUpdateAtTheThisTime = true;
                switch (v.getId()) {
                    case R.id.btn:
                        changeState(ci);
                        break;
                    case R.id.btnRemove:
                        removeDish(ci);
                        break;
                }
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

    public void socketReply(int requestCode, String s, int code) {
        if (code == 0) {
            return;
        }
        switch (requestCode) {
            case 1:
                break;
            case 2:
                break;
        }
    }

    public class Updater implements Runnable, DataSocket.DataReceiver {

        private boolean canUpdate = true;
        @Override
        public void run() {
            try {
                while (!Thread.currentThread().isInterrupted() && !stopThread) {
                    Thread.sleep(1000);
                    if (!canUpdate) {
                        continue;
                    }
                    canUpdate = false;
                    JSONObject jo = new JSONObject();
                    jo.put("c", 1);
                    jo.put("first", first ? 1 : 1);
                    jo.put("reminder", Cnf.getString(MainActivity.this, "reminder_id"));
                    DataSocket ds = new DataSocket(jo.toString(), MainActivity.this, 1);
                    ds.mDataReceiver = this;
                    ds.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                DbEx.regException(MainActivity.this, e.getMessage());
            } catch (JSONException e) {
                e.printStackTrace();
                DbEx.regException(MainActivity.this, e.getMessage());
            }
        }

        @Override
        public void socketReply(int requestCode, String s, int code) {
            synchronized (this) {
                if (code == 0) {
                    canUpdate = true;
                    return;
                }
                try {
                    JSONObject jo = new JSONObject(s);
                    if (jo.getString("reply").equals("ok")) {
                        first = false;
                        JSONArray ja = jo.getJSONArray("dishes");
                        final ArrayList<CookItem> items = new ArrayList<>();
                        for (int i = 0; i < ja.length(); i++) {
                            jo = ja.getJSONObject(i);
                            CookItem ci = new CookItem();
                            ci.mState = jo.getInt("state");
                            ci.mRecord = jo.getString("rec");
                            ci.mTime = jo.getString("time");
                            ci.mTable = jo.getString("table");
                            ci.mDish = jo.getString("dish");
                            ci.mQty = jo.getString("qty");
                            ci.mStaff = jo.getString("staff");
                            ci.mComment = jo.getString("comment");
                            items.add(ci);
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (items.size() > 0) {
                                    if (doNotUpdateAtTheThisTime) {
                                        doNotUpdateAtTheThisTime = false;
                                    } else {
                                        compareArrays(items);
                                    }
                                }
                                canUpdate = true;
                            }
                        });

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    DbEx.regException(MainActivity.this, e.getMessage());
                }
            }
        }
    }

//    public AlertDialog createCookItemDlg(String title, int id) {
//        LayoutInflater li = LayoutInflater.from(this);
//        View v = li.inflate(R.layout.item_dish, null);
//        final EditText edPass = v.findViewById(R.id.edPassword);
//        final int msgId = id;
//        ((TextView)v.findViewById(R.id.tvMessage)).setText(title);
//        AlertDialog.Builder ab = new AlertDialog.Builder(this);
//        ab.setView(v);
//        ab.setCancelable(false);
//        ab.setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.cancel();
//            }
//        });
//        AlertDialog dlg = ab.create();
//        dlg.show();
//        return dlg;
//    }
}
