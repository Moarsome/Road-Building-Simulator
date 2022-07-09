package com.example.drawingapp;

import javafx.scene.paint.Color;

public class Building extends Box{

    public Building(double x1, double y1)
    {
        super(x1,y1);
        this.type = "building";
        this.costMultiplier = 50.0;
        this.color = Color.rgb(243, 189, 96, 1);
    }

    public Building()
    {
        super();
        this.type = "building";
        this.costMultiplier = 50.0;
        this.color = Color.rgb(243, 189, 96, 1);
    }

}
