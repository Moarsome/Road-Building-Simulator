package com.example.drawingapp;

public class Building extends Box{

    public Building(double x1, double y1)
    {
        super(x1,y1);
        this.type = "building";
        this.costMultiplier = 50.0;
    }

    public Building()
    {
        super();
        this.type = "building";
        this.costMultiplier = 50.0;
    }

}
