package com.example.probono;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import com.google.gson.*;
import javax.net.ssl.HttpsURLConnection;

public class subActivity extends Activity {

    TextView tv;
    Button voiceButton;
    Intent intent;
    String sourceString;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub);

        voiceButton = (Button) findViewById(R.id.voice_button);
        tv = findViewById(R.id.textView);

        intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");




        voiceButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                startActivityForResult(intent, 1);
                Log.i("클릭 확인", "클릭 확인");
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && (requestCode == 1)){
            ArrayList<String> sstResult = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

            String result_sst = sstResult.get(0);

            new GoogleTranslatorTask().execute(result_sst);

            //tv.setText(""+result_sst);
        }
    }

    // 번역 AsyncTask
    class GoogleTranslatorTask extends AsyncTask<String, Integer, String> {
        String URL = "https://www.googleapis.com/language/translate/v2?key=";
        String KEY = "AIzaSyAgJVUHXYJYOhctVpZgUUImw6tpbvJqs3M";
        String TARGET = "&target=ko";
        String SOURCE = "&source=en";
        String QUERY = "&q=";

        String englishString = "";
        String koreaString;

        @Override
        protected String doInBackground(String... editText) {
            englishString = editText[0];
            StringBuilder result = new StringBuilder();
            try{
                String encodedText = URLEncoder.encode(englishString, "UTF-8");
                java.net.URL url = new URL(URL+ KEY + SOURCE + TARGET + QUERY + encodedText);
                HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
                InputStream stream;
                if(conn.getResponseCode() == 200){
                    stream = conn.getInputStream();
                }else{
                    stream = conn.getErrorStream();
                }
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                String line;

                while((line = reader.readLine()) != null){
                    result.append(line);
                }

                JsonParser parser = new JsonParser();

                JsonElement element = parser.parse(result.toString());

                if(element.isJsonObject()){
                    JsonObject obj = element.getAsJsonObject();
                    if(obj.get("error") == null){
                        koreaString = obj.get("data").getAsJsonObject().get("translations").getAsJsonArray().get(0).getAsJsonObject().get("translatedText").getAsString();
                    }
                }
                if(conn.getResponseCode() != 200){
                    Log.e("GoogleTranslatorTask", result.toString());
                }
            }catch(IOException | JsonSyntaxException ex){
                Log.e("GoogleTranslatorTask", ex.getMessage());
            }
            return koreaString;
        }

        @Override
        protected void onPreExecute() { super.onPreExecute(); }

        @Override
        protected void onProgressUpdate(Integer ...progress) { // 파일 다운로드 퍼센티지 표시 작업

        }

        @Override
        protected void onPostExecute(String result) {
            tv.setText(result);
        }


    }

}

