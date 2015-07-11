package com.support.android.i2irebuild;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;


import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;

import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/*Created by Sherwin
on 29/06/2015*/

public class ServerRequests {
    ProgressDialog progressDialog;
    public static final int CONNECTION_TIMEOUT = 1000 * 15;
    public static final String SERVER_ADDRESS = "www.lookout-tt.com";

    public ServerRequests(Context context) {
        progressDialog = new ProgressDialog(context);
        progressDialog.setCancelable(false);
        progressDialog.setTitle("Processing...");
        progressDialog.setMessage("Please wait...");

    }

    public void storeUserDataInBackground(User user,
                                          GetUserCallback userCallBack) {
        progressDialog.show();
        new StoreUserDataAsyncTask(user, userCallBack).execute();
    }

    public void fetchUserDataAsyncTask(User user, GetUserCallback userCallBack) {
        progressDialog.show();
        new fetchUserDataAsyncTask(user, userCallBack).execute();
    }

    /**
     * parameter sent to task upon execution progress published during
     * background computation result of the background computation
     */

    public class StoreUserDataAsyncTask extends AsyncTask<Void, Void, Void> {
        User user;
        GetUserCallback userCallBack;

        public StoreUserDataAsyncTask(User user, GetUserCallback userCallBack) {
            this.user = user;
            this.userCallBack = userCallBack;
        }


        @Override
        protected Void doInBackground(Void... params) {
            Log.v("IN doInBackGround", "1");
            try {

                Uri.Builder builder = new Uri.Builder();
                builder.scheme("http")
                        .authority(SERVER_ADDRESS)
                        .appendPath("Register.php")
                        .appendQueryParameter("firstName", user.firstName)
                        .appendQueryParameter("lastName", user.lastName)
                        .appendQueryParameter("username", user.username)
                        .appendQueryParameter("password", user.password)
                        .build();
                Log.v("REGISTER.PHP addr", builder.toString());

                String query = builder.toString();

                URL url = new URL(builder.toString());

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(CONNECTION_TIMEOUT);
                conn.setConnectTimeout(CONNECTION_TIMEOUT);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                conn.setDoOutput(true);


                Log.v("REGISTER.PHP query", query);

               OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(query);
                writer.flush();
                writer.close();
                os.close();

                conn.connect();

                int responseCode=conn.getResponseCode();

                String response="";
                String message;
                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    String line;
                    BufferedReader br=new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    while ((line=br.readLine()) != null) {
                        response+=line;
                    }

                    Log.v("REGISTER.PHP response", response);

                    JSONObject jObject = new JSONObject(response);

                    if (jObject.length() != 0){

                        message = jObject.getString("response_message");
                        Log.v("REGISTER.PHP message", message);
                    }
                    else
                        Log.v("FROM REGISTER.PHP", "No JSON message received");



                }
                else {
                    response = responseCode+"";
                    Log.v("httpError", response);
                }
            }
            catch(Exception e) {
                e.printStackTrace();
                Log.v("ERRORS", e.toString());
            }

            return null;
        }//END OF Void doInBackground(Void... params)


        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            progressDialog.dismiss();
            userCallBack.done(null);
        }

    }

    public class fetchUserDataAsyncTask extends AsyncTask<Void, Void, User> {
        User user;
        GetUserCallback userCallBack;

        public fetchUserDataAsyncTask(User user, GetUserCallback userCallBack) {
            this.user = user;
            this.userCallBack = userCallBack;
        }

        @Override
        protected User doInBackground(Void... params) {
            User returnedUser = null;

            try {
                //-------------------------------------------------------
                Uri.Builder builder = new Uri.Builder();
                builder.scheme("http")
                        .authority(SERVER_ADDRESS)
                        .appendPath("FetchUserData.php")
                        .appendQueryParameter("username", user.username) //search by username
                        .build();
                Log.v("FetchUserData.php addr", builder.toString());

                String query = builder.toString();

                URL url = new URL(builder.toString());

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(CONNECTION_TIMEOUT);
                conn.setConnectTimeout(CONNECTION_TIMEOUT);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                conn.setDoOutput(true);


                Log.v("FetchUserData.php query", query);

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(query);
                writer.flush();
                writer.close();
                os.close();

                conn.connect();

                int responseCode=conn.getResponseCode();

                //-------------------------------------------------------

                String response="";
                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    String line;
                    BufferedReader br=new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    while ((line=br.readLine()) != null) {
                        response+=line;
                    }

                    Log.v("RESPONSE", response);
                    JSONObject jObject = new JSONObject(response);

                    if (jObject.length() != 0){
                        Log.v("happened", "2");
                        String pword = jObject.getString("password");
                        String fName = jObject.getString("firstName");
                        String lName = jObject.getString("lastName");

                        Log.v("returned pword", pword);
                        Log.v("stored pword", user.password);

                        if (BCrypt.checkpw(user.password, pword)) { //compare plain text password (user.password) with one retrieved from DB

                            returnedUser = new User(fName, lName, user.username,
                                    pword); //create new User if plainText matches hashed password retrieved from DB
                        }
                        else
                            returnedUser=null; //return null if hashed password retrieved from DB does not match plain text password stored in UserStore

                    }

                    if(returnedUser!=null){
                        storeUserDataInBackground(returnedUser,userCallBack);
                    }

                }
                else {
                    response = responseCode+"";
                    Log.v("httpError", response);
                }

            }
            catch(Exception e){
                e.printStackTrace();
            }

            return returnedUser;
        }

        @Override
        protected void onPostExecute(User returnedUser) {
            super.onPostExecute(returnedUser);
            progressDialog.dismiss();
            userCallBack.done(returnedUser);
        }
    }
}

