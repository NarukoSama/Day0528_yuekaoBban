package com.bwie.day0528_yuekaobban;

import android.app.Application;

import org.xutils.x;

/**
 * Created by
 * Chenxin
 * on 2017/5/28.
 */

public class IApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        x.Ext.init(this);

    }


}
