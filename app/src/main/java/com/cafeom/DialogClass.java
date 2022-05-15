package com.cafeom;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;

public class DialogClass extends Dialog implements View.OnClickListener {

    public interface DialogYesNo {
        void yes();
        void no();
    }

    public interface DialogQty {
        void qty(double v);
        void no();
    }

    public interface DialogOk {
        void ok();
    }

    private int mContentId;
    private String mMessage;
    private DialogYesNo mDialogYesNo;
    private DialogQty mDialogQty;
    private DialogOk mDialogOk;

    public DialogClass(@NonNull Context context, int contentId, String message) {
        super(context);
        mContentId = contentId;
        mMessage = message;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setCancelable(false);
        setContentView(mContentId);
        ((TextView) findViewById(R.id.txtMessage)).setText(mMessage);
        Button btn = findViewById(R.id.btnClose);
        if (btn != null) {
            btn.setOnClickListener(this);
        }
        btn = findViewById(R.id.btnYes);
        if (btn != null) {
            btn.setOnClickListener(this);
        }
        btn = findViewById(R.id.btnNo);
        if (btn != null) {
            btn.setOnClickListener(this);
        }
    }

    public static void error(Context c, String s) {
         DialogClass dc = new DialogClass(c, R.layout.dialog_class_error_ok, s);
         dc.show();
    }

    public static void information(Context c, String s, DialogOk d) {
        DialogClass dc = new DialogClass(c, R.layout.dialog_class_information_ok, s);
        dc.mDialogOk = d;
        dc.show();
    }

    public static void question(Context c, String s, DialogYesNo d) {
        DialogClass dc = new DialogClass(c, R.layout.dialog_class_question_yesno, s);
        dc.mDialogYesNo = d;
        dc.show();
    }

    public static void qty(Context c, String s, DialogQty d) {
        DialogClass dc = new DialogClass(c, R.layout.dialog_class_qty_yesno, s);
        dc.mDialogQty = d;
        dc.show();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnClose:
                if (mDialogOk != null ){
                    mDialogOk.ok();
                }
                dismiss();
                break;
            case R.id.btnYes:
                if (mDialogYesNo != null) {
                    mDialogYesNo.yes();
                }
                if (mDialogQty != null) {
                    double value = 0;
                    String txt = ((EditText) findViewById(R.id.edtQty)).getText().toString();
                    if (txt.isEmpty() == false) {
                        value = Double.valueOf(txt);
                    }
                    mDialogQty.qty(value);
                }
                dismiss();
                break;
            case R.id.btnNo:
                if (mDialogYesNo != null) {
                    mDialogYesNo.no();
                }
                if (mDialogQty != null) {
                    mDialogQty.no();
                }
                dismiss();
                break;
        }
    }
}
