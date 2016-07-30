package com.example.administrator.currencyproject;

/**
 * Created by Administrator on 7/28/2016.
 */
public class Currency {

  private String currencyName;
  private double pricePerUnit;
  private double totalAmount;
  private int uniCode;

    public Currency(String currencyName,double pricePerUnit, double totalAmount, int uniCode) {
        this.currencyName = currencyName;
        this.totalAmount = totalAmount;
        this.pricePerUnit = pricePerUnit;
        this.uniCode = uniCode;
    }

    public String getCurrencyName() {
        return currencyName;
    }

    public String getCurrencySymbol()
    {
        if(uniCode==-1)
            return "?";

        return new String(Character.toChars(uniCode));
    }

    public double getPricePerUnit() {
        return pricePerUnit;
    }

    public double getTotalAmount() {
        return totalAmount;
    }


}
