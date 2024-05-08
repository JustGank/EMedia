package com.xjl.emedia.impl

import android.content.Context
import android.view.View

interface PreOnClickListener {
    fun preOnClick(view: View?, context: Context?): Boolean
}
