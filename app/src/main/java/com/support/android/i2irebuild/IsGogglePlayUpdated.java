package com.support.android.i2irebuild;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.security.ProviderInstaller;


public class IsGogglePlayUpdated extends Activity
        implements ProviderInstaller.ProviderInstallListener {

    private static final int ERROR_DIALOG_REQUEST_CODE = 1;

    private boolean mRetryProviderInstall;

    //Update the security provider when the activity is created.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ProviderInstaller.installIfNeededAsync(this, this);
    }

    /**
     * This method is only called if the provider is successfully updated
     * (or is already up-to-date).
     */
    @Override
    public void onProviderInstalled() {
        // Provider is up-to-date, app can make secure network calls.
        Intent i = new Intent(this, StartActivity.class);
        startActivity(i);
    }

    /**
     * This method is called if updating fails; the error code indicates
     * whether the error is recoverable.
     */
    @Override
    public void onProviderInstallFailed(int errorCode, Intent recoveryIntent) {
        if (GooglePlayServicesUtil.isUserRecoverableError(errorCode)) {
            // Recoverable error. Show a dialog prompting the user to
            // install/update/enable Google Play services.
            GooglePlayServicesUtil.showErrorDialogFragment(
                    errorCode,
                    this,
                    ERROR_DIALOG_REQUEST_CODE,
                    new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            // The user chose not to take the recovery action
                            onProviderInstallerNotAvailable();
                        }
                    });
        } else {
            // Google Play services is not available.
            onProviderInstallerNotAvailable();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ERROR_DIALOG_REQUEST_CODE) {
            // Adding a fragment via GooglePlayServicesUtil.showErrorDialogFragment
            // before the instance state is restored throws an error. So instead,
            // set a flag here, which will cause the fragment to delay until
            // onPostResume.
            mRetryProviderInstall = true;
        }
    }

    /**
     * On resume, check to see if we flagged that we need to reinstall the
     * provider.
     */
    @Override
    protected void onPostResume() {
        //super.onPostResult();
        if (mRetryProviderInstall) {
            // We can now safely retry installation.
            ProviderInstaller.installIfNeededAsync(this, this);
        }
        mRetryProviderInstall = false;
    }

    private void onProviderInstallerNotAvailable() {
        // This is reached if the provider cannot be updated for some reason.
        // App should consider all HTTP communication to be vulnerable, and take
        // appropriate action.
        Intent i = new Intent(this, StartActivity.class);
        startActivity(i);
        //For now I will allow to go through but should change this
    }
}
