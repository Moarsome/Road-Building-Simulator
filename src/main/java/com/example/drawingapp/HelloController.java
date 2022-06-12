package com.example.drawingapp;

import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

// TODO


public class HelloController {
    @FXML
    private AnchorPane anchorPane;
    @FXML
    private Canvas canvas;
    @FXML
    private Canvas overlayCanvas;
    @FXML
    public GraphicsContext gc;
    @FXML
    public GraphicsContext ogc;
    @FXML
    private Label infoLabel;
    @FXML
    private Button boxButton;
    @FXML
    private Button roadButton;
    @FXML
    private Label moneyLabel;
    @FXML
    private Label moneyChangeLabel;

    private String mode = "none";
    private boolean active = false;
    private List<Box> existingBoxes = new ArrayList<>();
    private Box currentBox;
    private User user;

    @FXML
    public void initialize() {
        initGraphics();

        moneyChangeLabel.setVisible(false);

        // Initialise User
        user = new User();
        user.addMoney(1000000000);
        moneyLabel.setText(formatLargeNumber(user.getMoney()));

        // BACKGROUND
        ogc.setFill(Color.rgb(115, 189, 89));
        ogc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // initialise events
        canvas.setOnMousePressed(e -> drawClicked(e.getX(), e.getY()));
        canvas.setOnMouseMoved(e -> drawUpdate(e.getX(),e.getY()));
        anchorPane.setOnKeyPressed(e -> keyPressed(e.getCode()));

        boxButton.setOnAction(e -> setMode("building"));
        roadButton.setOnAction(e -> setMode("road"));
    }

    public void initGraphics() {
        gc = canvas.getGraphicsContext2D();
        ogc = overlayCanvas.getGraphicsContext2D();
    }

    public void setMode(String nextMode)
    {
        active = false;
        clearBoard();

        if (nextMode == "building")
        {
            System.out.println("Created building");
            active = false;
            currentBox = new Building();
            gc.setFill(Color.rgb(243, 189, 96, .8));
        }
        else if (nextMode == "road") {
            active = true;
            currentBox = new Road();
            gc.setFill(Color.rgb(87, 87, 87, .8));
        }
        mode = nextMode;
        String capitalise = nextMode.substring(0,1).toUpperCase() + nextMode.substring(1);
        infoLabel.setText(capitalise+" mode selected!");
    }


    public void drawClicked(double x, double y) {
        clearBoard();

        if (mode == "building") {
            System.out.println("MODE BOX");

            if (!active) {
                active = true;
                infoLabel.setText("Press 'esc' to cancel");
                currentBox.updatePosition(x,y);
                gc.setFill(Color.rgb(243, 189, 96, .8));
            } else {
                if (user.deductMoney(currentBox.getCost()) == true) {
                    System.out.println("Deducting "+currentBox.cost);

                    // Formatting the deduction info
                    moneyChangeLabel.setText("-"+formatLargeNumber(currentBox.getCost()));
                    moneyChangeLabel.setStyle("-fx-text-fill: red");
                    moneyChangeLabel.setVisible(true);

                    // Creating a thread to make deduction info disappear after 3 seconds
                    TestThread removeLabel = new TestThread(moneyChangeLabel);
                    new Thread(removeLabel).start();
                    // End

                    moneyLabel.setText(formatLargeNumber(user.getMoney()));
                    active = false;
                    System.out.println("SETTING BUILDING");

                    infoLabel.setText("Building created!");
                    ogc.setFill(Color.rgb(243, 189, 96));

                    if (currentBox.getX1() > x) {
                        currentBox.setX2(x);
                    }
                    if (currentBox.getY1() > y) {
                        currentBox.setY2(y);
                    }
                    storeBox(currentBox);
                    currentBox = new Building();
                }
                else
                {
                    infoLabel.setText("Not enough money!");
                }
            }

        }
        else if (mode == "road"){

            gc.setFill(Color.DARKGRAY);
            if (active)
            {
                if (user.deductMoney(currentBox.cost) == true) {
                    System.out.println("Deducting "+currentBox.cost);
                    moneyLabel.setText(formatLargeNumber(user.getMoney()));
//                     Just drawing the nodes so its easier to see
//                double xnode[] = currentBox.getXnodes();
//                double ynode[] = currentBox.getYnodes();
//
//                for (int i = 0;i < 12;i++)
//                {
//                    ogc.setFill(Color.rgb(0, 255, 0));
//                    ogc.fillOval(xnode[i]-2.5, ynode[i]-2.5, 5.0, 5.0);
//                }

                    System.out.println("SETTING ROAD");
                    infoLabel.setText("Road created!");
                    ogc.setFill(Color.rgb(87, 87, 87));
                    storeBox(currentBox);
                    currentBox = new Road();
                    currentBox.setCurrentRotation(user.getRotation());
                    gc.setFill(Color.rgb(87, 87, 87, .8));
                }
                else
                {
                    infoLabel.setText("Not enough money!");
                }
            }
        }

    }

    private void storeBox(Box box)
    {
        ogc.fillPolygon(box.getXcoords(),box.getYcoords(),4);
        // existingBoxes is a collection of every object to track borders and valid positions
        existingBoxes.add(currentBox);
        currentBox = null;
    }

    public void drawUpdate(double x, double y) {
        // This is mainly just a ghost rectangle to guide the user.

        if (active && currentBox != null)
        {
            // Clear the canvas to avoid unnecessary ghost rectangles
            clearBoard();

            // Shorten doubles to make it easier to math
            double prevX = currentBox.getX1();
            double prevY = currentBox.getY1();
            double newX = x;
            double newY = y;

            if (mode == "building") {

                double width = Math.abs(x - prevX);
                double height = Math.abs(y - prevY);

                if (prevX < x) {
                    newX = prevX;
                }
                if (prevY < y) {
                    newY = prevY;
                }

                currentBox.setWidth(width);
                currentBox.setHeight(height);
                currentBox.updatePosition(newX, newY);

                // Cost cursor text
                gc.setFill(Color.rgb(220, 0, 0, 0.5));
                gc.setFont(Font.font ("Verdana", 14));
                gc.fillText("-"+formatLargeNumber(currentBox.getCost()),x,y);
                gc.setFill(Color.rgb(243, 189, 96, .8));
            }
            else if (mode == "road")
            {
                currentBox.updatePosition(x,y);
                lockCursor(x,y, (Road) currentBox);

                // Text
                gc.setFill(Color.rgb(220, 0, 0, 0.5));
                gc.setFont(Font.font ("Verdana", 14));
                gc.fillText("-"+formatLargeNumber(currentBox.getCost()),x,y);
                gc.setFill(Color.rgb(87, 87, 87, .8));
            }

//            if (isRectValid(x,y, newX, newY) == false) {
//                gc.setFill(Color.rgb(220, 0, 0, 0.5));
//            }

            gc.fillPolygon(currentBox.getXcoords(), currentBox.getYcoords(),4);
        }
    }

    // Escape to cancel action
    private void keyPressed(KeyCode key)
    {
        // CANCEL OPERATION
        if (key == KeyCode.ESCAPE)
        {
            clearBoard();
            active = false;
        }
        else if (key == KeyCode.R)
        {
            currentBox.rotateBox();
            user.setRotation(currentBox.getCurrentRotation());
            drawUpdate(currentBox.getX1(),currentBox.getY1());
        }
    }

    // Check if the rectangle is not overlapping with other existing ones
    private boolean isRectValid(double x, double y, double startX, double startY)
    {
        boolean valid = true;
//        for (Rectangle currentRect:existingRect) {
//            double xEnd = currentRect.getX()+currentRect.getWidth();
//            double yEnd = currentRect.getY()+currentRect.getHeight();
//
//            if ((x > currentRect.getX() && y > currentRect.getY()) && (xEnd > startX && yEnd > startY))
//            {
//                valid = false;
//            }
//        }
        return valid;
    }

    // Clears the temporary canvas board
    private void clearBoard()
    {
        gc.clearRect(0,0,canvas.getWidth(),canvas.getHeight());
    }

      //////////////////////////////
     /// CURSOR LOCKING METHODS ///
    //////////////////////////////

    private void lockCursor(double x, double y, Road road) {

        double shortestDist = road.getMinDist();
        double closestX = x;
        double closestY = y;

        for (Box box:existingBoxes)
        {
            double currentX = box.getClosestX(x,y);
            double currentY = box.getClosestY(x,y);

            if (currentX != -1 && currentY != -1) {
                double currentDist = box.getDist(currentX, x, currentY, y);
                    closestX = currentX;
                    closestY = currentY;

            }
        }

        road.updatePosition(closestX, closestY);
    }

    ////////////////////////
    // FORMATTING METHODS //
    ////////////////////////

    // Abbreviates large doubles as K, M or B
    // e.g 1.01K, 403.42M
    private String formatLargeNumber(double inputNumber)
    {
        DecimalFormat df = new DecimalFormat("#.##");
        String output = String.valueOf(df.format(inputNumber));
        String outputNoDecimal = String.valueOf(Math.round(inputNumber));

        if (1000.0 <= inputNumber && inputNumber < 1000000.0)
        {
            output = outputNoDecimal.substring(0,outputNoDecimal.length()-3)+"."+output.substring(outputNoDecimal.length()-3,outputNoDecimal.length()-1)+" K";
        }
        else if (1000000.0 <= inputNumber && inputNumber < 1000000000.0)
        {
            output = outputNoDecimal.substring(0,outputNoDecimal.length()-6)+"."+output.substring(outputNoDecimal.length()-6,outputNoDecimal.length()-5)+" M";
        }
        else if (1000000000.00 <= inputNumber && inputNumber < 1000000000000.0)
        {
            output = outputNoDecimal.substring(0,outputNoDecimal.length()-9)+"."+output.substring(outputNoDecimal.length()-9,outputNoDecimal.length()-7)+" B";
        }

        return "$"+output;
    }
}