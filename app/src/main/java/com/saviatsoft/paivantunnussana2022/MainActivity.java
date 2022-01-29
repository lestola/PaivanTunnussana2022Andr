package com.saviatsoft.paivantunnussana2022;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    TextView dateSelector;
    TextView text;
    TextView date;
    TextView header;
    Date today;
    ImageButton buttonNext;
    ImageButton buttonPrevious;
    DatePickerDialog picker;
    List<Prayer> prayers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        today = Calendar.getInstance().getTime();
        dateSelector = findViewById(R.id.dateSelectorTv);
        date = findViewById(R.id.dateTv);
        header = findViewById(R.id.headerTv);
        text = findViewById(R.id.textTv);
        buttonNext = findViewById(R.id.buttonNext);
        buttonPrevious = findViewById(R.id.buttonPrevious);

        //ladataan json data prayers listaan
        load_json_data();

        //katsotaan löytyikö päivälle rukoaus, jos ei, niin näytetään ensimmäinen rukous
        //jos löytyy, niin näytetään se
        updatePrayer(getPrayerFromDate(today));

        //valitaan seuraavan päivän rukous
        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(today);
                calendar.add(Calendar.DAY_OF_YEAR, 1);
                today = calendar.getTime();
                updatePrayer(getPrayerFromDate(today));
            }
        });

        //valitaan edellisen päivän rukous
        buttonPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(today);
                calendar.add(Calendar.DAY_OF_YEAR, -1);
                today = calendar.getTime();
                updatePrayer(getPrayerFromDate(today));
            }
        });

        //valitaan datepickeristä päivä
        dateSelector.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar cldr = Calendar.getInstance();
                int day = cldr.get(Calendar.DAY_OF_MONTH);
                int month = cldr.get(Calendar.MONTH);
                int year = cldr.get(Calendar.YEAR);
                // date picker dialog
                picker = new DatePickerDialog(MainActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                Calendar calendar = Calendar.getInstance();
                                calendar.set(Calendar.MONTH, monthOfYear);
                                calendar.set(Calendar.YEAR, year);
                                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                                today = calendar.getTime();
                                updatePrayer(getPrayerFromDate(today));
                            }
                        }, year, month, day);
                picker.show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_menu, menu);

        // first parameter is the file for icon and second one is menu
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // kun valitaan jakopainike menusta
        switch (item.getItemId()) {
            case R.id.shareButton:

                Intent sharingIntent = new Intent(Intent.ACTION_SEND);

                // type of the content to be shared
                sharingIntent.setType("text/plain");

                // Body of the content
                String shareBody = getPrayerFromDate(today).header + "\n \n" + getPrayerFromDate(today).text;

                // subject of the content. you can share anything
                String shareSubject = "Päivän tunnussana 2022";

                // passing body of the content
                sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody);

                // passing subject of the content
                sharingIntent.putExtra(Intent.EXTRA_SUBJECT, shareSubject);
                startActivity(Intent.createChooser(sharingIntent, "Jaa"));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void updatePrayer(Prayer todaysPrayer){

        //päivitetään ruudulla oleva rukous
        SimpleDateFormat dateFormat = new SimpleDateFormat("d. MMMM yyyy");
        dateSelector.setText("" + dateFormat.format(today));
        dateFormat = new SimpleDateFormat("EEE d.M");
        date.setText("" + dateFormat.format(today));

        if(todaysPrayer != null){
            header.setText(todaysPrayer.getHeader());
            text.setText(todaysPrayer.getText());
        } else {
            header.setText(R.string.noPrayer);
            text.setText("");
        }
    }

    public Prayer getPrayerFromDate(Date date){
        //vertaillaan rukouslistasta löytyykö päivälle rukousta
        Calendar comparisonDate1 = Calendar.getInstance();
        Calendar comparisonDate2 = Calendar.getInstance();
        comparisonDate1.setTime(date);
        for(Prayer prayer : prayers){
            comparisonDate2.setTime(prayer.getDate());
            if(comparisonDate1.get(Calendar.DAY_OF_YEAR) == comparisonDate2.get(Calendar.DAY_OF_YEAR)){
                return prayer;
            }
        }
        return null;
    }

    public void load_json_data(){

        String json;

        try {
            InputStream is = getAssets().open("prayers.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            json = new String(buffer,"UTF-8");
            JSONArray jsonArray = new JSONArray(json);

            for(int i = 0; i<jsonArray.length();i++){
                Prayer prayer = new Prayer();
                JSONObject obj = jsonArray.getJSONObject(i);

                //luodaan päiväys
                String dateString = obj.getString("date");
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                try {
                    Date date = format.parse(dateString);
                    prayer.date = date;
                    System.out.println(date);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                prayer.header = obj.getString("header");
                prayer.text = obj.getString("text");

                prayers.add(prayer);
            }

        }catch (IOException e){
            e.printStackTrace();
        }catch (JSONException e){
            e.printStackTrace();
        }
    }
}