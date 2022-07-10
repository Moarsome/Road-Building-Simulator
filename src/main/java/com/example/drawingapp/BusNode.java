package com.example.drawingapp;

import java.util.ArrayList;
import java.util.List;

public class BusNode {
    private double x;
    private double y;
    private boolean locked;
    private List<BusNode> connectedNodes;

    BusNode()
    {
        this(0,0);
    }

    BusNode(double x, double y)
    {
        this.connectedNodes = new ArrayList<>();
        this.x = x;
        this.y = y;
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

    public List<BusNode> getConnectedNodes()
    {
        return this.connectedNodes;
    }

    public void addConnectedNode(BusNode n)
    {
        this.connectedNodes.add(n);
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
