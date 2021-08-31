package com.nophrase.speechtotext.data;

import android.app.Application;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class RealmCreate extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);
        RealmConfiguration configuration = new RealmConfiguration.Builder().name("RealmData.realm").build();
        Realm.setDefaultConfiguration(configuration);
    }
}
