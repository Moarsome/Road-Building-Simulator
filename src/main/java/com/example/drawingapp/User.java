package com.example.drawingapp;

import java.text.NumberFormat;

public class User {
    private double money;
    private double rotation;
    private NumberFormat format;

    public User()
    {
        this.money = 0.0;
        this.rotation = 0.0;
    }

    public String getMoneyString()
    {
        format = NumberFormat.getCurrencyInstance();

        return format.format(this.money);
    }

    public boolean deductMoney(double cost)
    {
        boolean valid = false;

        if (this.money-cost >= 0.0)
        {
            this.money -= cost;
            valid = true;
        }
        return valid;
    }

    public void addMoney(double pay)
    {
        this.money += pay;
    }

    /////////////////////////
    // Getters and Setters //
    /////////////////////////

    public double getMoney() {
        return money;
    }

    public void setMoney(double money) {
        this.money = money;
    }

    public double getRotation() {
        return rotation;
    }

    public void setRotation(double rotation) {
        this.rotation = rotation;
    }
}
