package com.bring.dat.model;

import android.content.Context;

import com.bring.dat.BuildConfig;
import com.bring.dat.R;

import timber.log.Timber;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

//@ReportsCrashes(formUri = "", mailTo = "rishav.orem@gmail.com")
public final class Application extends android.app.Application {

    public static String Font_Text = "Roboto-Regular.ttf";

    @Override
    public void onCreate() {
        super.onCreate();
        //ACRA.init(this);
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath(Font_Text)
                .setFontAttrId(R.attr.fontPath)
                .build());
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }

}