package com.example.drawingapp;

import javafx.scene.paint.Color;

import java.util.concurrent.atomic.AtomicInteger;

/*

    x1, y1 *--------* x2, y2
           |        |
    x3, y3 *--------* x4, y4

*/
abstract class Box {
    protected double[] xcoords;
    protected double[] ycoords;
    protected double width;
    protected double height;
    protected double minDist; // Minimum distance to lock cursor
    protected double currentRotation; // Rotation in radians
    protected double[] xnodes; // Connective nodes on the box
    protected double[] ynodes;

    protected boolean currentlyHovered;
    protected boolean currentlySelected;

    protected Color color;

    protected double cost; // How much it costs to build
    protected double costMultiplier; // Set in objects

    protected String type; // type of box object
    static AtomicInteger atomicInt = new AtomicInteger(); // creates unique id
    protected int id;

    public Box()
    {
        this.xcoords = new double[4];
        this.ycoords = new double[4];
        this.xnodes = new double[8];
        this.ynodes = new double[8];

        this.minDist = 15.0;
        this.id = atomicInt.incrementAndGet(); // Generates ID
        this.currentRotation = 0;
    }

    public Box(double x1, double y1, double width, double height){
        this();

        this.width = width;
        this.height = height;

        this.updatePosition(x1,y1);
    }

    public Box(double x1, double y1)
    {
        this();

        xcoords[0] = x1;
        ycoords[0] = y1;
    }

    // Retrieves midpoint between 2 values
    public double getMid(double v1, double v2)
    {
        return (v1+v2)/2.0;
    }

    // Run this once positions/size have been created
    private void generateNodes()
    {
        // Start off by copying the four corners to nodes
        for (int i = 4; i < 8; i++)
        {
            int j = i-4;
            this.xnodes[i] = this.xcoords[j];
            this.ynodes[i] = this.ycoords[j];
        }
        // Find inner points (30.0 dist between points)
        for (int i = 0; i < 4; i++)
        {
            int k = (i%2)*2;
            int j = k + 1;
            double newDist = -30.0;
            if (i > 1) newDist*=2;
            this.xnodes[i] = getXFromDist(newDist, this.xcoords[k], this.ycoords[k], this.xcoords[j],this.ycoords[j]);
            this.ynodes[i] = getYFromDist(newDist, this.xcoords[k], this.ycoords[k], this.xcoords[j],this.ycoords[j]);
        }
    }

    // Get x coordinate from distance and 2 points
    // Calculations from https://stackoverflow.com/questions/1800138/given-a-start-and-end-point-and-a-distance-calculate-a-point-along-a-line?noredirect=1&lq=1
    public double getXFromDist(double dist, double x1, double y1, double x2, double y2)
    {
        double vx = x2-x1;
        double vy = y2-y1;
        double mag = Math.sqrt(vx*vx+vy*vy);
        vx /= mag;

        double px = x1 + vx * (mag + dist);
        return px;
    }
    // Get y coordinate from distance and 2 points
    public double getYFromDist(double dist, double x1, double y1, double x2, double y2)
    {
        double vx = x2-x1;
        double vy = y2-y1;
        double mag = Math.sqrt(vx*vx+vy*vy);
        vy /= mag;

        double py = y1 + vy * (mag + dist);
        return py;
    }
    // Retrieves magnitude distance between 2 points
    public double getDist(double x3, double y3, double x4, double y4)
    {
        double a = Math.abs(x4-x3);
        double b = Math.abs(y4-y3);

        return (Math.sqrt(a*a+b*b)); // Pythagoras theorum
    }

    // Retrieves X node of closest x of object, returns -1 if null
    public double getClosestX(double x, double y)
    {
        double closestDistance = minDist;
        double closestX = x;
        for (int i = 0; i < 8; i++)
        {
            double currentDistance = getDist(x, y, this.xnodes[i], this.ynodes[i]);
            if (currentDistance < closestDistance)
            {
                closestDistance = currentDistance;
                closestX = this.xnodes[i];
            }
        }

        if (closestDistance >= minDist)
        {
            closestX = -1;
        }

       return closestX;
    }

    // Retrieves Y coord of closest point of object, returns -1 if null
    public double getClosestY(double x, double y)
    {
        double closestDistance = minDist;
        double closestY = y;

        for (int i = 0; i < 8; i++)
        {
            double currentDistance = getDist(x, y, this.xnodes[i], this.ynodes[i]);
            if (currentDistance < closestDistance)
            {
                closestDistance = currentDistance;
                closestY = this.ynodes[i];
            }
        }

        if (closestDistance >= minDist)
        {
            closestY = -1;
        }

        return closestY;
    }

    public void rotateBox(double degrees)
    {
        double radAdd = (degrees*Math.PI)/180.0;

        double newRotation = this.currentRotation + radAdd;

        if (newRotation >= 2*Math.PI)
        {
            newRotation -= 2*Math.PI;
        }
        else if (newRotation < 0)
        {
            newRotation += 2*Math.PI;
        }

        this.currentRotation = newRotation;
    }

    public void updatePosition(double x1, double y1){
        double theta = this.currentRotation;

        this.xcoords[0] = x1;
        this.xcoords[1] = Math.cos(theta)*width+x1;
        this.xcoords[2] = Math.cos(theta)*width-Math.sin(theta)*height+x1;
        this.xcoords[3] = -(Math.sin(theta)*height)+x1;

        this.ycoords[0] = y1;
        this.ycoords[1] = Math.sin(theta) * width+y1;
        this.ycoords[2] = Math.cos(theta) * height+Math.sin(theta)*width+y1;
        this.ycoords[3] = Math.cos(theta) * height+y1;

        generateNodes();
        generateCost();
    }

    private void generateCost()
    {
        this.cost = ((this.width * this.height)/2) * 3.281;
        this.cost *= costMultiplier;
    }

      /////////////////////////
     // Getters and Setters //
    /////////////////////////

    public double[] getXnodes()
    {
        return this.xnodes;
    }

    public double[] getYnodes()
    {
        return this.ynodes;
    }

    public double[] getXcoords()
    {
        return this.xcoords;
    }

    public double[] getYcoords()
    {
        return this.ycoords;
    }

    public double getX1() {
        return this.xcoords[0];
    }

    public void setX2(double x1)
    {
        this.xcoords[1] = x1;
    }

    public double getX2() {
        return this.xcoords[1];
    }

    public double getY1() {
        return this.ycoords[0];
    }

    public void setY2(double y1)
    {
        this.ycoords[1] = y1;
    }

    public double getY2() {
        return this.ycoords[1];
    }


    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public double getMinDist() {
        return minDist;
    }

    public void setMinDist(double minDist) {
        this.minDist = minDist;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setCurrentRotation(double rotation)
    {
        this.currentRotation = rotation;
    }

    public double getCurrentRotation()
    {
        return this.currentRotation;
    }

    public double getCost()
    {
        return this.cost;
    }

    public boolean isCurrentlyHovered() {
        return currentlyHovered;
    }

    public void setCurrentlyHovered(boolean currentlyHovered) {
        this.currentlyHovered = currentlyHovered;
    }

    public boolean isCurrentlySelected() {
        return currentlySelected;
    }

    public void setCurrentlySelected(boolean currentlySelected) {
        this.currentlySelected = currentlySelected;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }
}
