package com.example.drawingapp;

public class BusNode {
    private double x;
    private double y;
    private boolean locked;

    BusNode(double x, double y)
    {
        this.x = x;
        this.y = y;
        this.locked = false;
    }

    BusNode()
    {
        this.x = 0;
        this.y = 0;
        this.locked = false;
    }

    public void setPosition(double x, double y)
    {
        this.x = x;
        this.y = y;
    }

    public double getX()
    {
        return x;
    }

    public double getY()
    {
        return y;
    }

    public boolean getLocked()
    {
        return this.locked;
    }

    public void setLocked(boolean locked)
    {
        this.locked = locked;
    }
}
