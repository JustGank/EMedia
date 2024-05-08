package com.xjl.emedia.impl

import android.content.Context
import android.view.View
import android.widget.Toast
import com.xjl.emedia.R

class RecordPreOnClickListener : PreOnClickListener {
    var currentTime = System.currentTimeMillis()
    var toast: Toast? = null
    private fun showToast(context: Context?, res: Int) {
        if (toast == null) {
            toast = Toast.makeText(context, res, Toast.LENGTH_SHORT)
        } else {
            toast!!.setText(res)
        }
        toast!!.show()
    }

    override fun preOnClick(view: View?, context: Context?): Boolean {
        return if (System.currentTimeMillis() - currentTime > 1500) {
            currentTime = System.currentTimeMillis()
            true
        } else {
            showToast(context, R.string.click_too_fast)
            false
        }
    }
}
