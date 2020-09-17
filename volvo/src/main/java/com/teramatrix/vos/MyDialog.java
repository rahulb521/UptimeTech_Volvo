package com.teramatrix.vos;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.WindowManager;

public  class MyDialog extends Activity {

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // Show error alret for device IMEI no more active on
    // server
    AlertDialog.Builder alertbox = new AlertDialog.Builder(
            MyDialog.this);
    // set the title for this Alert Dialog
    alertbox.setTitle(getResources().getString(
            R.string.error_message));
    // set the message for this Alert Dialog
    alertbox.setMessage("Login Failed\nPlease enter valid credentials");
    // set cancelable false outbox in app click
    alertbox.setCancelable(false);
    alertbox.setNeutralButton(
            getResources().getString(R.string.ok),
            new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog,
                                  int which) {
                // Dismsss the Dialog on click on OK.
                dialog.dismiss();
                // finish dialog activity
                Intent intent = new Intent(MyDialog.this, ConfigurationLicenseActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
              }
            });
    alertbox.show();
  }
}
