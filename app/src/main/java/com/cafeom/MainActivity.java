package com.cafeom;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.opengl.Visibility;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cafeom.databinding.ActivityMainBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class MainActivity extends AppAct implements View.OnClickListener {

    private ActivityMainBinding bind;
    private Map<Integer, CookItem> mCookItems;
    private DishAdapters mDishAdapter;
    protected PowerManager.WakeLock mWakeLock;
    private MediaPlayer mMediaPlayer = null;
    private boolean first = true;
    private boolean stopThread = false;
    private static int mRequestCount = 0;

    public MainActivity() {
        mCookItems = new TreeMap<>();
    }

    public static final int RC_DISHESLIST = 1;
    public static final int RC_DISHSTART = 2;
    public static final int RC_DISHREADY = 3;
    public static final int RC_DISHREMOVE = 4;
    public static final int RC_DISHRECEIVED = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bind = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(bind.getRoot());
        /*TEMP*/
        Cnf.setString("net_server", "https://195.191.155.164:29999");
        /*END TEMP*/
        //Cnf.setString(this, "cnf_password", "111");
        //Cnf.setString(this, "server_address", "10.1.0.2");
        //Cnf.setString(this, "server_port", "888");
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

        mDishAdapter = new DishAdapters();
        bind.rv.setLayoutManager(new GridLayoutManager(this, 1));
        bind.rv.setAdapter(mDishAdapter);
        bind.ivConfig.setOnClickListener(this);
        bind.ivOrder.setOnClickListener(this);

        final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "com.cafeom:wakeeetag");
        mWakeLock.acquire();

        Intent i = new Intent(this, WebService.class);
        startService(i);
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
            case R.id.ivOrder:
                Intent orderIntent = new Intent(this, GoodsOrder.class);
                startActivity(orderIntent);
                break;
        }
    }

    public void compareArrays(ArrayList<CookItem> items) {
        Map<Integer, CookItem> cookItems = new TreeMap<>();
        boolean playSound = false;
        boolean readyOnly = Cnf.getBoolean("readyonly");
        for (CookItem ci: items) {
            if (ci == null) {
                continue;
            }
            if (mCookItems.containsKey(Integer.valueOf(ci.mRecord))) {
                CookItem ct = mCookItems.get(Integer.valueOf(ci.mRecord));
                ci.btnDoneVisibility = ct.btnDoneVisibility;
                ci.btnNoVisibility = ct.btnNoVisibility;
                ci.btnYesVisibility = ct.btnYesVisibility;
                ci.progressVisibility = ct.progressVisibility;
            }
            cookItems.put(Integer.valueOf(ci.mRecord), ci);
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
                    DataSocket ds = new DataSocket(jo.toString(), MainActivity.this, readyOnly ? RC_DISHSTART : RC_DISHRECEIVED, Integer.valueOf(ci.mRecord));
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
        mCookItems = cookItems;
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

        int btnDoneVisibility;
        int btnYesVisibility;
        int btnNoVisibility;
        int progressVisibility;

        public CookItem() {
            btnDoneVisibility = View.VISIBLE;
            btnYesVisibility = View.GONE;
            btnNoVisibility = View.GONE;
            progressVisibility = View.GONE;

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
            ProgressBar progressBar;

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
                progressBar = v.findViewById(R.id.progressBar);
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
                progressBar.setVisibility(ci.progressVisibility);
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
                                ds = new DataSocket(jo.toString(), MainActivity.this, RC_DISHSTART, Integer.valueOf(ci.mRecord));
                                ds.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                mRequestCount++;
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
                                ds = new DataSocket(jo.toString(), MainActivity.this, RC_DISHREADY, Integer.valueOf(ci.mRecord));
                                ds.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                mRequestCount++;
                            } catch (JSONException e) {
                                e.printStackTrace();
                                DbEx.regException(MainActivity.this, e.getMessage());
                            }
                            break;
                    }
                    playSound(R.raw.click);
            }

            void removeDish(final CookItem ci) {
                String dialogMsg = getString(R.string.Remove) + "?\r\n" + ci.mDish + "\r\n" + ci.mQty;
                DialogInterface.OnClickListener dic = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            showProgressDialog(getString(R.string.Executing));
                            DataSocket ds;
                            JSONObject jo = new JSONObject();
                            jo.put("c", 2);
                            jo.put("state", 5);
                            jo.put("rec", ci.mRecord);
                            jo.put("ready", new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault()).format(new Date()));
                            ds = new DataSocket(jo.toString(), MainActivity.this, RC_DISHREMOVE, Integer.valueOf(ci.mRecord));
                            ds.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                            mRequestCount++;
                        } catch (JSONException e) {
                            e.printStackTrace();
                            DbEx.regException(MainActivity.this, e.getMessage());
                        }
                        playSound(R.raw.click);
                    }
                };
                createCookItemDlg(dialogMsg, dic);
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
                        removeDish(ci);
                        break;
                    case R.id.btnYes:
                        ci.btnYesVisibility = View.GONE;
                        ci.btnNoVisibility = View.GONE;
                        ci.progressVisibility = View.VISIBLE;
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

    public void socketReply(int requestCode, String s, int code, int option) {
        hideProgressDialog();
        if (code == 0) {
            if (requestCode == 3 || requestCode == 2 || requestCode == 4) {
                CookItem ci = mCookItems.get(option);
                if (ci != null) {
                    ci.progressVisibility = View.GONE;
                    ci.btnYesVisibility = View.GONE;
                    ci.btnNoVisibility = View.GONE;
                    ci.btnDoneVisibility = View.VISIBLE;
                    mDishAdapter.notifyDataSetChanged();
                }
            }
            try {
                JSONObject jo = new JSONObject(s);
                if (!jo.getString("reply").equals("ok")) {
                    createCookItemDlg(jo.getString("msg"), null);
                }
            } catch (JSONException e) {
                createCookItemDlg(e.getMessage(), null);
                createCookItemDlg(s, null);
            }
            return;
        }
        try {
            JSONObject jo = new JSONObject(s);
            if (!jo.getString("reply").equals("ok")) {

            }
            CookItem ci = null;
            switch (requestCode) {
                case RC_DISHRECEIVED:
                    break;
                case RC_DISHSTART:
                     ci = mCookItems.get(Integer.valueOf(jo.getString("rec")));
                     ci.mState = 2;
                     ci.btnDoneVisibility = View.VISIBLE;
                     ci.btnNoVisibility = View.GONE;
                     ci.btnYesVisibility = View.GONE;
                     ci.progressVisibility = View.GONE;
                     mDishAdapter.notifyDataSetChanged();
                    break;
                case RC_DISHREADY:
                case RC_DISHREMOVE:
                    ci = mCookItems.get(Integer.valueOf(jo.getString("rec")));
                    mCookItems.remove(Integer.valueOf(ci.mRecord));
                    mDishAdapter.notifyDataSetChanged();
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (requestCode == 4 || requestCode == 3 || requestCode == 2) {
            if (mRequestCount > 0) {
                mRequestCount--;
            }
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
                    if (mRequestCount > 0) {
                        continue;
                    }
                    canUpdate = false;
                    JSONObject jo = new JSONObject();
                    jo.put("c", 1);
                    jo.put("first", first ? 1 : 1);
                    jo.put("reminder", Cnf.getString("reminder_id"));
                    DataSocket ds = new DataSocket(jo.toString(), MainActivity.this, RC_DISHESLIST, 0);
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
        public void socketReply(int requestCode, String s, int code, int option) {
            synchronized (this) {
                if (requestCode == 1) {
                    if (code == 0) {
                        canUpdate = true;
                        return;
                    }
                } else {

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
                                    if (mRequestCount > 0) {
                                        canUpdate = false;
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

    public AlertDialog createCookItemDlg(String title, DialogInterface.OnClickListener okListener) {
        LayoutInflater li = LayoutInflater.from(this);
        View v = li.inflate(R.layout.layout_dialog, null);
        ((TextView)v.findViewById(R.id.tvMsg)).setText(title);
        AlertDialog.Builder ab = new AlertDialog.Builder(this);
        ab.setView(v);
        ab.setCancelable(false);
        ab.setPositiveButton(R.string.OK, okListener);
        ab.setNegativeButton(R.string.CANCEL, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        ab.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                //dialogActive = false;
            }
        });
        AlertDialog dlg = ab.create();
        dlg.show();
        return dlg;
    }
}
