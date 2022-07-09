package com.example.drawingapp;

import javafx.scene.paint.Color;

public class Road extends Box{

    public Road(double x1, double y1)
    {
        super(x1,y1, 90.0, 30.0);
        this.type = "road";
        this.costMultiplier = 10.0;
        this.color = Color.rgb(87, 87, 87, 1);
    }

    public Road()
    {
        super();
        this.type = "road";
        this.costMultiplier = 10.0;
        this.width = 90.0;
        this.height = 30.0;
        this.color = Color.rgb(87, 87, 87, 1);
    }

}
