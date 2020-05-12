package com.example.huber;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.core.app.NavUtils;
import androidx.fragment.app.FragmentActivity;

public class DrawerItemActivity extends FragmentActivity {
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer_item);

        sharedPreferences = getSharedPreferences(getResources().getString(R.string.shared_preference_name), MODE_PRIVATE);
        int insert = sharedPreferences.getInt("insert", -2);
        Log.d("SECOND", "onCreate: should be 1. Actual value: " + insert );
        editor = sharedPreferences.edit();
        editor.putInt("insert", 2);
        editor.apply();
        Log.d("SECOND", "Inserted 2");

        //setResult(RESULT_OK);
        //finish();

        setTitle("Einstellungen");
    }


    private void close(){
        NavUtils.navigateUpFromSameTask(this);
    }
}
