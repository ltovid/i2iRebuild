package com.support.android.i2irebuild;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;


public class JoinCommunityActivity extends AppCompatActivity {
    Toolbar mToolbar;
    ArrayAdapter<String> adapterCity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_community);

        mToolbar = (Toolbar) findViewById(R.id.toolbarSignJoinCommunity);
        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
        }

      /*  // Get a reference to the AutoCompleteTextView in the layout
        AutoCompleteTextView textView = (AutoCompleteTextView) findViewById(R.id.autocomplete_years);
// Get the string array
        String[] years = getResources().getStringArray(R.array.yearList);
// Create the adapter and set it to the AutoCompleteTextView
        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, years);
        textView.setAdapter(adapter);
*/

        // Get the string array
        String[] cities = getResources().getStringArray(R.array.city_array);
        // Get a reference to the AutoCompleteTextView in the layout
        AutoCompleteTextView cityView = (AutoCompleteTextView) findViewById(R.id.autocomplete_cities);

// Create the adapter and set it to the AutoCompleteTextView
        // ArrayAdapter<String> adapterCity =
        //        new ArrayAdapter<String>(this, android.R.layout.simple_list_item_2, cities);
        adapterCity = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, cities);
        cityView.setAdapter(adapterCity);

    }

    public void gotoMain(View view) {
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_join_community, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
