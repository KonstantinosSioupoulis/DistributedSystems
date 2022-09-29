package com.example.ourmessenger;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.text.BreakIterator;

public class MainActivity extends AppCompatActivity {


    EditText inputText;
    TextView textView;
    Button nextButton;
    TextView resultTextView;
    String userName="default";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inputText = (EditText) findViewById(R.id.inputText);

        textView=(TextView) findViewById(R.id.textview_first);

        //kresultTextView=(TextView) findViewById(R.id.enteredAppTextView);

        nextButton = (Button) findViewById(R.id.enterApp);



    }

    public void updateText(View view){
        textView.setText("Hello "+ inputText.getText());
        //userName=inputText.getText().toString();
        //resultTextView.setText("Welcome to ourMessenger "+inputText.getText());
        System.out.println("Button clicked");
        nextButton.setVisibility(View.VISIBLE);

    }

    protected void onStart(){
        super.onStart();

        //Client c=new Client();


        nextButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                //setContentView(R.layout.activity_second);

                Intent s=new Intent(v.getContext(),Second_activity.class); //v.getContext()
                s.putExtra("userName",inputText.getText().toString());
                startActivity(s);
            }
        });
    }

}