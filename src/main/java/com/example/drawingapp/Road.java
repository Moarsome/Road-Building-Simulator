package com.example.drawingapp;

public class Road extends Box{

    public Road(double x1, double y1)
    {
        super(x1,y1, 90.0, 30.0);
        this.type = "road";
        this.costMultiplier = 10.0;
    }

    public Road()
    {
        super();
        this.type = "road";
        this.costMultiplier = 10.0;
        this.width = 90.0;
        this.height = 30.0;
    }

}
