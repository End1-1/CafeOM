package com.cafeom;

import android.content.Context;
import android.content.DialogInterface;

import androidx.appcompat.app.AlertDialog;

public class Dialog {

    public static AlertDialog alertDialog(Context context, int title, int message) {
        String strTitle = "";
        if (title > 0) {
            strTitle = context.getString(title);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(context.getString(message))
                .setTitle(strTitle);
        builder.setCancelable(false);
        builder.setNeutralButton(R.string.OK, null);
        AlertDialog dialog = builder.create();
        dialog.show();
        return dialog;
    }

    public static AlertDialog alertDialog(Context context, int title, String message) {
        String strTitle = "";
        if (title > 0) {
            strTitle = context.getString(title);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(message)
                .setTitle(strTitle);
        builder.setCancelable(false);
        builder.setNeutralButton(R.string.OK, null);
        AlertDialog dialog = builder.create();
        dialog.show();
        return dialog;
    }

    public static AlertDialog alertDialog(Context context, int title, String message, DialogInterface.OnClickListener okClick) {
        AlertDialog.Builder ab = new AlertDialog.Builder(context);
        ab.setMessage(message);
        if (title > 0) {
            ab.setTitle(context.getString(title));
        }
        ab.setCancelable(false);
        ab.setPositiveButton(R.string.YES, okClick);
        ab.setNegativeButton(R.string.CANCEL, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        ab.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {

            }
        });
        AlertDialog dlg = ab.create();
        dlg.show();
        return dlg;
    }
}
