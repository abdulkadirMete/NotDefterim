package com.nophrase.speechtotext.ui;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nophrase.speechtotext.R;
import com.nophrase.speechtotext.adapters.NotAdapter;
import com.nophrase.speechtotext.data.RealmHelper;
import com.nophrase.speechtotext.model.Not;
import com.nophrase.speechtotext.utils.SpaceItemDecoration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.realm.Realm;

public class MainActivity extends AppCompatActivity implements NotAdapter.OnNotListener {
    private static final int REQUEST_CODE = 1;
    private static final int ADD_REQUEST_CODE = 2;
    private RecyclerView rv_nots;
    private LinearLayoutManager layoutManager;
    private NotAdapter adapter;
    private Realm realm;
    private NotAdapter.OnNotListener onNotListener = this;
    private List<Not> mNotList = new ArrayList<>();
    private RealmHelper realmHelper;
    private FragmentManager manager;
    private FragmentTransaction transaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initTb();
        init();
        updateRv(0);
    }

    private void switchFragment() {
        manager = getSupportFragmentManager();
        transaction = manager.beginTransaction();
        FragmentSort fb = (FragmentSort) manager.findFragmentByTag("fragmentSort");
        if(fb==null) {
            FragmentSort fragment = new FragmentSort();
            transaction.add(R.id.ct_home, fragment, "fragmentSort");
            transaction.commit();
        }else{
            getSupportFragmentManager().beginTransaction().remove(fb).commit();
        }
    }

    private void sort(final int sortType){
        Collections.sort(mNotList, new Comparator<Not>() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public int compare(Not o1, Not o2) {
                switch (sortType){
                    case 1:
                        return Integer.compare(o1.getColorNum(),o2.getColorNum());

                    case 2:
                        return o1.getHeader().compareTo(o2.getHeader());

                    default:
                        return 0;

                }

            }
        });
    }

    public void onClickSort(View v){
        switch (v.getId()){
            case R.id.imb_color:
                updateRv(1);
                break;

            case R.id.imb_date:
                updateRv(0);
                break;

            case R.id.imb_name:
                updateRv(2);
                break;

            case R.id.ln_sort:
                switchFragment();
                break;
        }
        switchFragment();
    }

    private void initTb() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private void updateRv(int sortType) {
        mNotList.clear();
        realm = Realm.getDefaultInstance();
        realmHelper = new RealmHelper(this,realm);
        mNotList.addAll(realmHelper.getNotList());
        switch (sortType){
            case 0:
                Collections.reverse(mNotList);
                break;
            case 1:
                sort(1);
                break;
            case 2:
                sort(2);
                break;
        }
        adapter = new NotAdapter(this, onNotListener,mNotList);
        rv_nots.setAdapter(adapter);
    }

    private void init() {
        rv_nots = findViewById(R.id.rv_nots);
        rv_nots.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        rv_nots.setLayoutManager(layoutManager);
        SpaceItemDecoration itemDecoration = new SpaceItemDecoration(3);
        rv_nots.addItemDecoration(itemDecoration);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu,menu);
        MenuItem search_item = menu.findItem(R.id.action_search);
        MenuItem add_item = menu.findItem(R.id.action_add);
        MenuItem sort_item = menu.findItem(R.id.action_sort);
        SearchView searchView = (SearchView) search_item.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                if(query.length()>0) {

                }
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_add:
                Intent intent = new Intent(this, NotDetailActivity.class);
                intent.putExtra("requestCode",ADD_REQUEST_CODE);
                startActivityForResult(intent,ADD_REQUEST_CODE);
                break;

            case R.id.action_sort:
                switchFragment();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onNotClick(int position){
        Intent intent = new Intent(this, NotDetailActivity.class);
        intent.putExtra("not",mNotList.get(position));
        intent.putExtra("requestCode",REQUEST_CODE);
        startActivityForResult(intent,REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            updateRv(0);
        }
    }

    @Override
    public void onBackPressed() {
        Intent a = new Intent(Intent.ACTION_MAIN);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(a);
    }
}
