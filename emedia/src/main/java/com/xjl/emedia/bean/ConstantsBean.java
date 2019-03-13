package com.xjl.emedia.bean;

import android.app.Activity;

/**
 * Created by x33664 on 2019/2/14.
 * 此类同意记录了已有的请求编码 避免在扩展时产生覆盖
 */

public class ConstantsBean {

    private int Picker_RequestCode = 20001;

    private int TakePhoto_RequestCode = 20002;

    private int TakeVideo_RequestCode = 20003;

    private int TakeVideo_Custom_RequestCode = 20004;

    private int Success_Code = Activity.RESULT_OK;

    private int Cancel_Code = 401;

    private int Failed_Code = 404;

}
