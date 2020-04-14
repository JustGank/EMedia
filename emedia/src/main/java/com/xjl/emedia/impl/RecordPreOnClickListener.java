package com.xjl.emedia.impl;

import android.content.Context;
import android.view.View;
import android.widget.Toast;

import com.xjl.emedia.impl.PreOnClickListener;

public class RecordPreOnClickListener implements PreOnClickListener {


    Long currentTime = System.currentTimeMillis();

    Toast toast = null;

    private void showToast(Context context, int res) {
        if (toast == null) {
            toast = Toast.makeText(context, res, Toast.LENGTH_SHORT);
        } else {
            toast.setText(res);
        }
        toast.show();

    }

    @Override
    public boolean preOnClick(View view, Context context) {
        if (System.currentTimeMillis() - currentTime > 1500) {
            currentTime = System.currentTimeMillis();
            return true;
        } else {
            showToast(context, com.xjl.emedia.R.string.click_too_fast);
            return false;
        }

    }



}
