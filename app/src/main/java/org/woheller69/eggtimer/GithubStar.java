package org.woheller69.eggtimer;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;

import androidx.appcompat.app.AlertDialog;
import androidx.preference.PreferenceManager;

public class GithubStar {
    public static void setAskForStar(Context context, boolean askForStar){
        SharedPreferences prefManager = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefManager.edit();
        editor.putBoolean("askForStar", askForStar);
        editor.apply();
    }

    static void starDialog(Context context){
        SharedPreferences prefManager = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefManager.getBoolean("askForStar",true))    {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
            alertDialogBuilder.setMessage(R.string.dialog_StarOnGitHub);
            alertDialogBuilder.setPositiveButton(context.getString(R.string.dialog_OK_button), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/woheller69/eggtimer")));
                    setAskForStar(context,false);
                }
            });
            alertDialogBuilder.setNegativeButton(context.getString(R.string.dialog_NO_button), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    setAskForStar(context,false);
                }
            });
            alertDialogBuilder.setNeutralButton(context.getString(R.string.dialog_Later_button), null);

            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }
    }

}
