package com.cafeom;

        import android.content.BroadcastReceiver;
        import android.content.Context;
        import android.content.Intent;
        import android.os.Build;
        import android.widget.Toast;

public class BroadcastListener extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Toast.makeText(context, "BROADCAST RECEIVER", Toast.LENGTH_LONG).show();
            Intent pushIntent = new Intent(context, Listener.class);
            context.startForegroundService(pushIntent);
        } else {
            Toast.makeText(context, "BROADCAST RECEIVER", Toast.LENGTH_LONG).show();
            Intent pushIntent = new Intent(context, Listener.class);
            context.startService(pushIntent);
        }
        if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {

        }
    }
}
