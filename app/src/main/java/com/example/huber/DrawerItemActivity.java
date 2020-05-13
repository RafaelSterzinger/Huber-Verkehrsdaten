package com.example.huber;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.fragment.app.FragmentActivity;

import com.google.android.material.button.MaterialButton;

public class DrawerItemActivity extends AppCompatActivity {
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private String classType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer_item);
        //setResult(RESULT_OK);
        //finish();

        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                classType = "Favoriten";
            } else {
                classType = extras.getString("type");
            }
        } else {
            classType = (String) savedInstanceState.getSerializable("type");
        }
        setTitle(classType);

        if ("Störungen".equals(classType)){
            for (int id:
                    new int[]{R.id.fav1, R.id.fav2, R.id.fav3}) {
                View entryView = findViewById(id);
                View directionView1 = entryView.findViewById(R.id.fav_dir1);
                View directionView2 = entryView.findViewById(R.id.fav_dir2);

                ((MaterialButton)directionView1.findViewById(R.id.favour)).setIcon(getDrawable(R.drawable.ic_warning_black_24dp));
                directionView1.findViewById(R.id.line_number).setVisibility(View.INVISIBLE);

                directionView2.findViewById(R.id.favour).setVisibility(View.INVISIBLE);
                directionView2.findViewById(R.id.line_number).setVisibility(View.INVISIBLE);
                ((TextView) directionView2.findViewById(R.id.name)).setText("Fahrzeugunfall");
            }
        } else {
            Toast.makeText(this, "No functionality implemented", Toast.LENGTH_SHORT).show();
        }

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {

            //setResult(RESULT_OK);
            //finish();
            NavUtils.navigateUpFromSameTask(this);
            //onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }}
