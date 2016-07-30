package com.example.administrator.currencyproject;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;

import java.io.InputStream;
import java.io.InputStreamReader;

import java.net.URL;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

   public static String TAG="CurrencyApp";
    EditText etAmount;
    TextView tvResult;
    Spinner spinnerBase, spinnerTarget;
    JSONObject jsonRates=null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etAmount=(EditText)findViewById(R.id.etAmount);
        tvResult=(TextView)findViewById(R.id.tvResult);

        configureBaseSpinner();
        configureTargetSpinner();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==R.id.menu1)
        {
            if(!validateData())
                return false;

            ExchangeRates er=new ExchangeRates(true);
            er.execute(true);
        }
        return super.onOptionsItemSelected(item);
    }

    private void configureBaseSpinner()
    {
        spinnerBase=(Spinner)findViewById(R.id.spinnerBase);
        ArrayAdapter<CharSequence> adapter=ArrayAdapter.createFromResource(this,R.array.base, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);
        spinnerBase.setAdapter(adapter);
    }
    private void configureTargetSpinner()
    {
        int indexOfName=R.integer.index_name;

        String []rawCurrencies= getResources().getStringArray(R.array.target);
        ArrayList<String> currencies=new ArrayList<>();
        for(int i=0;i<rawCurrencies.length;i++)
        {
            String []data=rawCurrencies[i].split(",");
            currencies.add(data[indexOfName]);
        }
        String[] currenciesName=currencies.toArray(new String[0]);

        spinnerTarget=(Spinner)findViewById(R.id.spinnerTarget);
        ArrayAdapter<CharSequence> adapter=new ArrayAdapter<CharSequence>(this,android.R.layout.simple_spinner_item,currenciesName);
        adapter.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);
        spinnerTarget.setAdapter(adapter);
    }

    private boolean isInternetAvailable()
    {
        ConnectivityManager manager=(ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if(manager==null)
            return false;
        else
            return true;
    }



    private boolean validateData()
    {
            if(!isInternetAvailable())
            {
                Toast.makeText(MainActivity.this, "No internet connection.", Toast.LENGTH_SHORT).show();
            }

            String tempAmount= etAmount.getText().toString();

            if(tempAmount.isEmpty())
            {
                Toast.makeText(MainActivity.this, "Please enter a valid amount", Toast.LENGTH_LONG).show();
                return false;
            }
            double temp=Double.parseDouble(etAmount.getText().toString());
            if(temp==0)
            {
                Toast.makeText(MainActivity.this, "Amount is zero", Toast.LENGTH_LONG).show();
                return false;
            }
        return true;
    }
    public void executeTask(View view)
    {
        if(!validateData())
        return;

        ExchangeRates exchangeRates = new ExchangeRates(false);
        exchangeRates.execute();
    }

    private class ExchangeRates extends AsyncTask
    {
        ProgressDialog progressDialog;
        int serverCode;

        boolean showDetails;
        ExchangeRates(boolean flag)
        {
            showDetails=flag;
        }

        @Override
        protected Object doInBackground(Object[] params)
        {
            HttpURLConnection connection=null;

            Log.d(TAG,"doInBackground is running...");
            try {
                URL url = new URL("https://openexchangerates.org/api/latest.json?app_id=f071b294358d42c1bd70c0b318d2e40d");
                connection= (HttpURLConnection) url.openConnection();
                connection.connect();
                serverCode=connection.getResponseCode();
                Log.d(TAG,serverCode+"<<<---Server code..");
                if(serverCode==200)
                {
                    //Parse Data.

                    //Get the input stream from connection.
                    InputStream inputStream=connection.getInputStream();

                    //Open the stream with buffer reader to read.
                    BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(inputStream));

                    String line="";
                    //Create a StringBuffer
                    StringBuffer data=new StringBuffer();
                    //Read a line from buffer and assign to line object.
                    //If line is readed as null; terminate the loop.
                    while((line=bufferedReader.readLine())!=null)
                    {
                      data.append(line);
                    }
                    connection.disconnect();
                    Log.d(TAG,"Data Received"+data.toString());

                    JSONObject jsonRootObject= new JSONObject(data.toString());
                    jsonRates=jsonRootObject.getJSONObject("rates");

                }

            } catch (MalformedURLException e) {
                Log.d(TAG,"Malfored URL");
                e.printStackTrace();
            } catch (IOException e) {
                Log.d(TAG,"IOException\n\n"+e.toString());
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {

           progressDialog= ProgressDialog.show(MainActivity.this,
                    "Converting",
                    "Getting exchange rates from serer",
                    true,
                    false);

            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Object o)
        {
            progressDialog.dismiss();
            super.onPostExecute(o);

            String target=spinnerTarget.getSelectedItem().toString();
            double amount=Double.parseDouble(etAmount.getText().toString());

            if(serverCode==200 && jsonRates!=null)
            {
                if(showDetails)
                {
                    Intent intent=new Intent(MainActivity.this,CurrencyList.class);

                    intent.putExtra("rates",jsonRates.toString());
                    intent.putExtra("amount",etAmount.getText().toString());
                    startActivity(intent);
                }
                else
                {
                    try
                    {
                        double currentRate=jsonRates.getDouble(target);
                        double result=currentRate * amount;
                        tvResult.setText(result+" "+target);

                    }
                    catch (JSONException e)
                    {
                        Log.d(TAG,e.toString());
                        e.printStackTrace();
                    }
                }
            }
            else
            {
                Toast.makeText(MainActivity.this, "Please check your internet connection.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
