package com.project.services.tool;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.project.services.R;


/**
 * Created by Administrator on 2016/3/7.
 */
public class DialogBuilder {
    public DialogBuilder() {
    }

    public static void getDialog(String message, String ssid, String password) {
        final Dialog dialog = new Dialog(ExceptionApplication.context, R.style.waiting_dialog);
        dialog.setContentView(R.layout.settingstation);
//        dialog.setTitle(message);
        TextView tx_message = (TextView) dialog.findViewById(R.id.msg);
        EditText ed_ssid = (EditText) dialog.findViewById(R.id.ssid_ed);
        EditText ed_password = (EditText) dialog.findViewById(R.id.password_ed);
        Button bt_ok = (Button) dialog.findViewById(R.id.ok);
        tx_message.setText(message);
        ed_ssid.setText(ssid);
        ed_password.setText(password);

        bt_ok.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.setCancelable(true);
        dialog.getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        dialog.show();
    }

    public static AlertDialog.Builder showDialog(Context con, String message, String ssid, String password) {
        LayoutInflater inflater = LayoutInflater.from(ExceptionApplication.context);

        View v = inflater.inflate(R.layout.settingstation, null);
        AlertDialog.Builder AP_builder = new AlertDialog.Builder(con);
        EditText ed_ssid = (EditText) v.findViewById(R.id.ssid_ed);
        EditText ed_password = (EditText) v.findViewById(R.id.password_ed);

        ed_ssid.setText(ssid);
        ed_password.setText(password);
        AP_builder.setMessage(message).setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        AP_builder.setView(v);

        return AP_builder;
    }
}
