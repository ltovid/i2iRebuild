package com.support.android.i2irebuild;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.widget.EditText;


import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.plus.Plus;



public class SignInActivity extends LoginActivity{
    Toolbar mToolbar;
    UserLocalStore userLocalStore;

    //TextView registerLink;   //Register Link not Created Yet
    EditText etUsername, etPassword;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API)
                .addScope(new Scope(Scopes.PROFILE))
                .build();


        etUsername = (EditText) findViewById(R.id.signIn_email_address);
        etPassword = (EditText) findViewById(R.id.signIn_password);
        //registerLink = (TextView) findViewById(R.id.tvRegisterLink); //Register Link not Created Yet

        //this is the button to sign-in with email and password
        findViewById(R.id.signInActiviy).setOnClickListener(this);

        //registerLink.setOnClickListener(this);  //Register Link not Created Yet

        //button for Goggle sign-in
        findViewById(R.id.sign_in_button).setOnClickListener(this);

        mToolbar = (Toolbar) findViewById(R.id.toolbarSignIn);
        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
        }


        userLocalStore = new UserLocalStore(this);

    }

    public void signIn(View view) {
        Intent i = new Intent(this, StartActivity.class);
       startActivity(i);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_sign_in, menu);
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

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.signInActiviy:
                String username = etUsername.getText().toString();
                String password = etPassword.getText().toString();

                User user = new User(username, password);
                authenticate(user);
                break;

            /*case R.id.tvRegisterLink:
                startActivity(new Intent(this, SignUpActivity.class));
                break;*/
        }
    }

    private void authenticate(User user) {
        ServerRequests serverRequest = new ServerRequests(this);
        serverRequest.fetchUserDataAsyncTask(user, new GetUserCallback() {
            @Override
            public void done(User returnedUser) {
                if (returnedUser == null) {
                    showErrorMessage();
                } else {
                    logUserIn(returnedUser);
                }
            }
        });
    }

    private void showErrorMessage() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(SignInActivity.this);
        dialogBuilder.setMessage("Incorrect user details");
        dialogBuilder.setPositiveButton("Ok", null);
        dialogBuilder.show();
    }

    private void logUserIn(User returnedUser) {
        userLocalStore.storeUserData(returnedUser);
        userLocalStore.setUserLoggedIn(true);
        startActivity(new Intent(this, MainActivity.class));
    }


}
