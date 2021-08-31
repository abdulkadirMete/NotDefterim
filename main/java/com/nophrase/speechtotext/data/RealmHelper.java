package com.nophrase.speechtotext.data;

import android.content.Context;
import android.util.Log;

import com.nophrase.speechtotext.model.Not;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

public class RealmHelper {

    private Realm realm;
    private Context context;

    public RealmHelper(Context context, Realm realm) {
        this.realm = realm;
        this.context = context;

    }
    public List<Not> getNotList(){
        RealmResults<Not> nots =  realm.where(Not.class).findAll();
        List<Not> notList = realm.copyFromRealm(nots);
        return notList;
    }

    public void saveData(final Not not){
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm bgRealm) {
                Number currentIdNum = bgRealm.where(Not.class).max("id");
                int nextId;
                if(currentIdNum == null) {
                    nextId = 1;
                } else {
                    nextId = currentIdNum.intValue() + 1;
                }
                not.setId(nextId);
                bgRealm.insertOrUpdate(not);
            }
        }, new Realm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {

            }
        }, new Realm.Transaction.OnError() {
            @Override
            public void onError(Throwable error) {
                Log.d("lemur", "onError: " + error);
            }
        });
    }

    public void updateData(final Not not){
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.insertOrUpdate(not);
            }
        });
    }

    public void deleteData(int id){
        realm.beginTransaction();
        Not not = realm.where(Not.class).equalTo("id",id).findFirst();
        not.deleteFromRealm();
        realm.commitTransaction();
    }
}
