package com.example.drawingapp;

import javafx.scene.paint.Color;

public class Road extends Box{
    private double busNodeX[];
    private double busNodeY[];

    public Road(double x1, double y1)
    {
        super(x1,y1, 90.0, 30.0);
        this.type = "road";
        this.costMultiplier = 10.0;
        this.color = Color.rgb(87, 87, 87, 1);
        this.busNodeX = new double[2];
        this.busNodeY = new double[2];

        generateBusNodes(x1,y1);
    }

    public Road()
    {
        super();
        this.type = "road";
        this.costMultiplier = 10.0;
        this.width = 90.0;
        this.height = 30.0;
        this.color = Color.rgb(87, 87, 87, 1);
        this.busNodeX = new double[2];
        this.busNodeY = new double[2];
    }

    public void updatePosition(double x1, double y1)
    {
        super.updatePosition(x1,y1);
        generateBusNodes(x1,y1);
    }

    // Create nodes for bus lanes
    private void generateBusNodes(double x1, double y1)
    {
        double theta = this.currentRotation;
        double middle = this.height/2;

        double cosTheta = Math.cos(theta)*middle;
        double sinTheta = Math.sin(theta)*middle;

        this.busNodeX[0] = cosTheta-sinTheta+x1;
        this.busNodeY[0] = cosTheta+sinTheta+y1;

        this.busNodeX[1] = cosTheta*5-sinTheta + x1;
        this.busNodeY[1] = cosTheta+sinTheta*5 + y1;
    }

    public double[] getBusNodeX()
    {
        return this.busNodeX;
    }

    public double[] getBusNodeY()
    {
        return this.busNodeY;
    }

    // Retrieves X node of closest x of object, returns -1 if null
    public double getClosestBusX(double x, double y)
    {
        double closestDistance = minDist;
        double closestX = x;
        for (int i = 0; i < 2; i++)
        {
            double currentDistance = super.getDist(x, y,  this.busNodeX[i], this.busNodeY[i]);
            if (currentDistance < closestDistance)
            {
                closestDistance = currentDistance;
                closestX = this.busNodeX[i];
            }
        }

        if (closestDistance >= minDist)
        {
            closestX = -1;
        }

        return closestX;
    }

    // Retrieves Y coord of closest point of object, returns -1 if null
    public double getClosestBusY(double x, double y)
    {
        double closestDistance = minDist;
        double closestY = y;

        for (int i = 0; i < 2; i++)
        {
            double currentDistance = super.getDist(x, y, this.busNodeX[i], this.busNodeY[i]);
            if (currentDistance < closestDistance)
            {
                closestDistance = currentDistance;
                closestY = this.busNodeY[i];
            }
        }

        if (closestDistance >= minDist)
        {
            closestY = -1;
        }

        return closestY;
    }
}
