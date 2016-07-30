package com.example.administrator.currencyproject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.zip.DeflaterInputStream;

public class CurrencyList extends Activity {

    ArrayList<Currency> currencies= new ArrayList<>();
    double amount;
    JSONObject jsonRates=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_currency_list);

        Intent intent=getIntent();
        String json=intent.getStringExtra("rates");
        String amt=intent.getStringExtra("amount");

        try {
            jsonRates=new JSONObject(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        amount=Double.parseDouble(amt);

        populateData();
        configureListView();
    }

    private void populateData()
    {
        int indexOfName=getResources().getInteger(R.integer.index_name);
        int indexOfSymbol=getResources().getInteger(R.integer.index_symbol);

        String []rawCurrencies= getResources().getStringArray(R.array.target);
        ArrayList<String> name=new ArrayList<>();
        ArrayList<Integer> symbols = new ArrayList<>();

        for(int i=0;i<rawCurrencies.length;i++)
        {
            String []data=rawCurrencies[i].split(",");
            name.add(data[indexOfName]);
            symbols.add(Integer.parseInt(data[indexOfSymbol]));
        }

        try
        {
            for(int i=0;i<rawCurrencies.length;i++)
            {
                currencies.add(new Currency(name.get(i), jsonRates.getDouble(name.get(i)), jsonRates.getDouble(name.get(i)) * amount, symbols.get(i)));
            }
        }
        catch (JSONException e) {
            Log.d(MainActivity.TAG,"error "+e.toString());
        }
        catch (ArrayIndexOutOfBoundsException ex)
        {
            Log.d(MainActivity.TAG,"Array out of bound. Please check you names and symbols are equal");
        }
        catch(Exception ex)
        {
            Log.d(MainActivity.TAG,"Exception occured. "+ ex.toString());
        }
    }

    private void configureListView()
    {
        ArrayAdapter<Currency> customAdapter= new myListAdapter();
        ListView listView = (ListView)findViewById(R.id.listView);
        listView.setAdapter(customAdapter);

    }

    private class myListAdapter extends ArrayAdapter<Currency>
    {   public myListAdapter()
        {
            super(CurrencyList.this, R.layout.activity_currency_list, currencies);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            DecimalFormat df=new DecimalFormat("#.00");
            View itemView=convertView;

            if(itemView==null)
            {
                if(itemView==null){
                    itemView=getLayoutInflater().inflate(R.layout.currency_list_layout,parent,false);
                }

                TextView tvName=(TextView)itemView.findViewById(R.id.tvName);
                TextView tvAmount=(TextView)itemView.findViewById(R.id.tvAmount);
                TextView tvRate=(TextView)itemView.findViewById(R.id.tvRate);
                TextView tvSymbol=(TextView)itemView.findViewById(R.id.tvSymbol);

                Currency current=currencies.get(position);

                tvName.setText(current.getCurrencyName());
                tvAmount.setText(df.format(current.getTotalAmount()));
                tvRate.setText(df.format(current.getPricePerUnit()));
                tvSymbol.setText(current.getCurrencySymbol());
            }
            return itemView;
        }
    }
}
