package com.codebionic.android.nutridb;

import android.app.Activity;

import android.content.Intent;

import android.os.Bundle;

import android.view.View;

import android.widget.Button;
import android.widget.TextView;

public class NutriError extends Activity
{

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nutrierror);

        Button okButton = 
            (Button) findViewById(R.id.nutrierror_ok);
        okButton.setOnClickListener(new OnOkClick());

        TextView errText =
            (TextView) findViewById(R.id.nutrierror_desc);

        Intent intent = getIntent();
        String s = intent.getStringExtra("error");
        errText.setText(s);
    }

    private class OnOkClick implements View.OnClickListener {

        public OnOkClick() { }            

        public void onClick(View v) {
            // Closes the activity.
            finish();
        }
    } // class OnOkClick

} // class NutriError
