package com.torstar.sunshine;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class MainActivity extends AppCompatActivity implements MainActivityFragment.Callback {
    private final String TAG = MainActivity.class.getSimpleName();
    private static final String DETAILFRAGMENT_TAG = "DFTAG";

    private String mLocation;
    private boolean mTwoPanel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mLocation = Utility.getPreferredLocation(this);

        View view = findViewById(R.id.weather_detail_container);

        if (view != null) {
            mTwoPanel = true;

            if (savedInstanceState==null){
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.weather_detail_container, new DetailActivityFragment(), DETAILFRAGMENT_TAG)
                        .commit();
            }
        } else {
            mTwoPanel = false;
        }

        // The forecast fragment is added in layout xml file so no need to add again here.
        // In the code that update record when user change location, will use fragment id instead
        // of tag now. (will see if this works).
//        if (savedInstanceState == null) {
//            getSupportFragmentManager().beginTransaction()
//                    .add(R.id.fragment,new MainActivityFragment(), FORECASTFRAGMENT_TAG)
//                    .commit();
//        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_view_location){
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            String location = prefs.getString(getString(R.string.pref_location_key), getString(R.string.pref_location_default));

            // View User Location on Map App
            Intent intent = new Intent(Intent.ACTION_VIEW);
            Uri.Builder builder = Uri.parse("geo:0,0?").buildUpon()
                    .appendQueryParameter("q", location);
            intent.setData(builder.build());
            if (intent.resolveActivity(getPackageManager())!=null) {
                startActivity(intent);
            }
        } else if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        String locationSetting = Utility.getPreferredLocation(this);

        if(locationSetting!= null && !locationSetting.equals(mLocation)){
            //MainActivityFragment mf = (MainActivityFragment)getSupportFragmentManager()
                    //.findFragmentById(R.id.fragment);
            MainActivityFragment mf = (MainActivityFragment)getSupportFragmentManager()
                    .findFragmentByTag(getString(R.string.main_fragment_tag));
            if (mf != null) {
                mf.onLocationChanged();
            }

            DetailActivityFragment df = (DetailActivityFragment)getSupportFragmentManager().findFragmentByTag(DETAILFRAGMENT_TAG);
            if (df != null){
                df.onLocationChanged(locationSetting);
            }
            mLocation = locationSetting;
        }
    }

    @Override
    public void onItemSelected(Uri contentUri) {
        if (mTwoPanel) {
            Bundle args = new Bundle();
            args.putParcelable(DetailActivityFragment.DETAIL_URI, contentUri);

            DetailActivityFragment detailActivityFragment = new DetailActivityFragment();
            detailActivityFragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.weather_detail_container, detailActivityFragment, DETAILFRAGMENT_TAG)
                    .commit();
        } else {
            // Open Detail View
            Intent intent = new Intent(this, DetailActivity.class);

            intent.setData(contentUri);

            startActivity(intent);
        }
    }
}
