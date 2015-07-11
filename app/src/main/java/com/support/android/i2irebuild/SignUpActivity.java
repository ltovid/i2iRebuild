package com.support.android.i2irebuild;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.plus.Plus;


public class SignUpActivity extends LoginActivity {
    Toolbar mToolbar;
    EditText etFName, etLName, etUsername, etPassword;

    UserLocalStore userLocalStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API)
                .addScope(new Scope(Scopes.PROFILE))
                .build();

        etFName = (EditText) findViewById(R.id.signUpName);
        etLName = (EditText) findViewById(R.id.signUp_Lastname);
        etUsername = (EditText) findViewById(R.id.signUp_email_address);
        etPassword = (EditText) findViewById(R.id.signIn_password);

        //This is the NEXT button on activity_sign_up.xml
        findViewById(R.id.signInActiviy).setOnClickListener(this);

        mToolbar = (Toolbar) findViewById(R.id.toolbarSignUp);
        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
            mToolbar.setNavigationIcon(R.drawable.abc_edit_text_material);
        }

        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Toast.makeText(SignUpActivity.this, "Nav Pressed", Toast.LENGTH_SHORT).show();
            }
        });

        userLocalStore = new UserLocalStore(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.signInActiviy:
                String firstName = etFName.getText().toString();
                String lastName = etLName.getText().toString();
                String userName = etUsername.getText().toString();
                String password = etPassword.getText().toString();
                password = BCrypt.hashpw(password, BCrypt.gensalt());

                User user = new User(firstName, lastName, userName, password);
                registerUser(user);
                break;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_sign_up, menu);
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


    private void registerUser(final User user) {
        ServerRequests serverRequest = new ServerRequests(this);
        serverRequest.storeUserDataInBackground(user, new GetUserCallback() {
            @Override
            public void done(User returnedUser) {
                authenticate(user);
            }
        });
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
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(SignUpActivity.this);
        dialogBuilder.setMessage("Registration Failed");
        dialogBuilder.setPositiveButton("Ok", null);
        dialogBuilder.show();
    }

    private void logUserIn(User returnedUser) {
        userLocalStore.storeUserData(returnedUser);
        userLocalStore.setUserLoggedIn(true);
        startActivity(new Intent(this, JoinCommunityActivity.class));
    }

}
