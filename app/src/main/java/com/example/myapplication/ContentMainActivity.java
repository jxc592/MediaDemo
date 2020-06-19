package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;

import com.example.myapplication.audio.RecorderActivity;
import com.example.myapplication.util.PermissionUtils;
import com.example.myapplication.video.VideoActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.myapplication.ui.main.SectionsPagerAdapter;

public class ContentMainActivity extends AppCompatActivity implements MenuItem.OnMenuItemClickListener {


    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(!PermissionUtils.hasCriticalPermission(this)) {
            PermissionUtils.requirePermissions(this);
            return;
        }
        init();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        menu.findItem(R.id.menu_videoedit).setOnMenuItemClickListener(this);
        menu.findItem(R.id.menu_record).setOnMenuItemClickListener(this);
        return super.onCreateOptionsMenu(menu);
    }

    void init(){

        setContentView(R.layout.activity_content_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
        FloatingActionButton fab = findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ContentMainActivity.this, VideoActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == PermissionUtils.REQUEST_CRITICAL_PERMISSIONS) {
            if(!PermissionUtils.hasCriticalPermission(this)) finish();else init();
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if(item.getItemId() == R.id.menu_videoedit) {
            Intent intent = new Intent(this,VideoActivity.class);
            startActivity(intent);
            return true;
        } else if( item.getItemId() == R.id.menu_record) {
            Intent intent = new Intent(this, RecorderActivity.class);
            startActivity(intent);
            return true;
        }
        return false;
    }
}