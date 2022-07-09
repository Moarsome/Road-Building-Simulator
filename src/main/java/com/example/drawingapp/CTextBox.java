package com.example.drawingapp;

// CLASS THAT ALLOWS BUTTON FUNCTIONALITY ON CANVAS

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.*;

public class CTextBox {
    private double x;
    private double y;
    private double width;
    private double height;
    private String text;
    private double padding = 5;
    private Color bgColor;
    private Color textColor;

    CTextBox(double x, double y, double width, double height, String text, Color bgColor, Color textColor)
    {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.text = text;
        this.bgColor = bgColor;
        this.textColor = textColor;
    }

    public void draw(GraphicsContext gc, Color backColor, Color foreColor)
    {
        Paint oldFill = gc.getFill();
        gc.setTextAlign(TextAlignment.CENTER);

        gc.setFill(backColor);
        gc.fillRect(x,y,width,height);
        gc.setFill(foreColor);
        gc.setFont(Font.font("Verdana", FontWeight.BOLD, FontPosture.REGULAR, 12.0));
        gc.fillText(text,x+width/2,y+padding+height/2);

        gc.setTextAlign(TextAlignment.LEFT);
        gc.setFill(oldFill);
    }

    public void draw(GraphicsContext gc)
    {
        draw(gc, bgColor, textColor);
    }

    public void drawHighlight(GraphicsContext gc)
    {
        draw(gc, bgColor.darker(), textColor);
    }

    public boolean onMouseEnter(double mouseX, double mouseY)
    {
        boolean isEnter = false;

        if ((mouseX > x && mouseX < x+width) && (mouseY > y && mouseY < y+height))
        {
            isEnter = true;
        }

        return isEnter;
    }
}
