package com.example.radiobe.fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import com.example.radiobe.MainActivity;
import com.example.radiobe.R;
import com.example.radiobe.adapters.MainScreenAdapter;
import com.example.radiobe.generalScreens.Settings;
import com.example.radiobe.radioLive.ExoPlayerView;
import com.facebook.login.LoginManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.ViewPager;

public class MainScreen extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    FragmentManager fm;
    ViewPager viewPager;
    MainScreenAdapter mMainScreenAdapter;
    BottomNavigationView navigation;
    Toolbar toolbar;

    FirebaseUser firebaseUser;
    Button logOutBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mainscreen);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
//        logOutBtn = findViewById(R.id.logOutBtn);
        navigation = findViewById(R.id.navigation);
        viewPager = findViewById(R.id.container);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        navigation.setOnNavigationItemSelectedListener(this);

        fm = getSupportFragmentManager();
        fm.beginTransaction().replace(R.id.container, new AllPrograms()).commit();

//        LocalBroadcastManager.getInstance(this).registerReceiver(new ExoPlayerView().broadcastReceiver, new IntentFilter("play_song"));

//        navigation.setSelectedItemId(R.id.navigation_home);


        if (firebaseUser != null) {
            Toast.makeText(this, firebaseUser.getEmail() + " login successful", Toast.LENGTH_SHORT).show();
        }

//        logOutBtn.setOnClickListener((v)->{
//            if(FirebaseAuth.getInstance().getCurrentUser() != null) {
//                AlertDialog.Builder builder = new AlertDialog.Builder(this);
//                builder.setMessage(getString(R.string.logOutDialog))
//                        .setPositiveButton(getString(R.string.Ok), new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int id) {
//                                LoginManager.getInstance().logOut();
//                                FirebaseAuth.getInstance().signOut();
//                                Toast.makeText(MainScreen.this, getString(R.string.logOutSuccess), Toast.LENGTH_SHORT).show();
//                                Intent intent = new Intent(MainScreen.this, MainActivity.class);
//                                startActivity(intent);
//                                finish();
//                            }
//                        })
//                        .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int id) {
//                                Toast.makeText(MainScreen.this, "No change!", Toast.LENGTH_SHORT).show();
//                            }
//                        });
//                AlertDialog alertDialog = builder.create();
//                alertDialog.show();
//            } else{
//                Toast.makeText(this, "There is no user currently logged in!", Toast.LENGTH_SHORT).show();
//            }
//        });


        mMainScreenAdapter = new MainScreenAdapter(fm);
        viewPager.setAdapter(mMainScreenAdapter);
        viewPager.setCurrentItem(3);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 3:
                        navigation.setSelectedItemId(R.id.navigation_home);
                        break;

                    case 2:
                        navigation.setSelectedItemId(R.id.navigation_favorites);
                        break;

                    case 1:
                        navigation.setSelectedItemId(R.id.navigation_notifications);
                        break;

                    case 0:
                        navigation.setSelectedItemId(R.id.navigation_radio);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });


    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

        switch (menuItem.getItemId()) {
            case R.id.navigation_home:
//                fm.beginTransaction().replace(R.id.container, new AllPrograms()).commit();
                viewPager.setCurrentItem(3);
                return true;

            case R.id.navigation_favorites:
//                fm.beginTransaction().replace(R.id.container, new Favorites()).commit();
                viewPager.setCurrentItem(2);

                return true;

            case R.id.navigation_notifications:
//                fm.beginTransaction().replace(R.id.container, new Notifications()).commit();
                viewPager.setCurrentItem(1);
                return true;

            case R.id.navigation_radio:
//                fm.beginTransaction().replace(R.id.container, new RadioLive()).commit();
                viewPager.setCurrentItem(0);
                return true;
        }

//        throw new IllegalArgumentException("No such Button!");
        return false;
    }

    //    menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_screen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.profile_menu) {
            return true;
        } else if (id == R.id.logout_menu) {
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(getString(R.string.logOutDialog))
                        .setPositiveButton(getString(R.string.Ok), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                LoginManager.getInstance().logOut();
                                FirebaseAuth.getInstance().signOut();
                                Toast.makeText(MainScreen.this, getString(R.string.logOutSuccess), Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(MainScreen.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        })
                        .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Toast.makeText(MainScreen.this, "No change!", Toast.LENGTH_SHORT).show();
                            }
                        });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            } else {
                Toast.makeText(this, "There is no user currently logged in!", Toast.LENGTH_SHORT).show();
            }

            return true;
        } else if (id == R.id.settings_nemu) {
            Intent intent = new Intent(MainScreen.this, Settings.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}

