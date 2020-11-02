package com.teramatrix.vos.volvouptime.custom;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.teramatrix.vos.R;

/**
 * Created by arun.singh on 3/28/2018.
 * Confiramtion dialog to show a message to user and take positive or negative action from user.
 * Used while Log out confirmation
 */

public class ConfirmationDialog {


    /**
     * Show confirmation dialog
     * @param context context of activity
     * @param message message to be displayed
     * @param i_confirmationResponse instance of callback interface
     */
    public static void showConfirmationDialog(final Context context,String message,final I_ConfirmationResponse i_confirmationResponse)
    {
        try {

            final Dialog confirmjobDialog = new Dialog(context,
                    android.R.style.Theme_Translucent);
            // dialog.requestWindowFeature(WindowManager.LayoutParams.FLAG_FULLSCREEN);

            confirmjobDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            /*confirmjobDialog.getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);*/
            ((Activity)context).getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

            confirmjobDialog.setCancelable(false);

            // Inflate pop-up view view,set layout parameter
            View screen_van_reached_popup_seekbar = LayoutInflater.from(
                    context).inflate(
                    R.layout.screen_confirmbox_popup, null);

            confirmjobDialog.setContentView(screen_van_reached_popup_seekbar);

            confirmjobDialog.getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

            TextView confirmation_messsage_text = (TextView) confirmjobDialog
                    .findViewById(R.id.confirmation_messsage_text);
            confirmation_messsage_text
                    .setText(message);
            // initialize textview component
            TextView btn_ok = (TextView) confirmjobDialog
                    .findViewById(R.id.tv_ok);
            TextView btn_cancel = (TextView) confirmjobDialog
                    .findViewById(R.id.tv_cancel);

            btn_ok.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    // TODO Auto-generated method stub

                    confirmjobDialog.hide();

                    i_confirmationResponse.onPositive_ConfirmationDialog();
                    //Clear All Login Credentials by clearing sharedpreferemce
                    //new VECVPreferences(context).clearSharedPreference();
                    //Launch Splash Screen
                    //startActivity(new Intent(context, SplashActivity.class));
                    //close this activity
                    //finish();

                }
            });

            btn_cancel.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    confirmjobDialog.hide();
                    i_confirmationResponse.onNegative_ConfirmationDialog();
                }
            });

            confirmjobDialog.show();

        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    /**
     * callback interface to deliver action result to implimeted class
     */
    public static interface I_ConfirmationResponse
    {
        void onPositive_ConfirmationDialog();
        void onNegative_ConfirmationDialog();
    }

}
