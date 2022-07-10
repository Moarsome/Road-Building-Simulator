package com.example.drawingapp;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.canvas.Canvas;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
// TODO


public class HelloController {
    @FXML private AnchorPane anchorPane;
    @FXML private Canvas canvas;
    @FXML private Canvas overlayCanvas;
    @FXML private Canvas midCanvas;
    @FXML public GraphicsContext gc;
    @FXML public GraphicsContext ogc;
    @FXML public GraphicsContext midgc;
    @FXML private Label infoLabel;
    @FXML private Button selectButton;
    @FXML private Button boxButton;
    @FXML private Button roadButton;
    @FXML private Button busLineButton;
    @FXML private Label moneyLabel;

    private String mode = "none";
    private boolean active;
    private boolean mouseDown;
    private List<Box> existingBoxes;
    private List<BusNode> busNodes;
    private CTextBox sellButton;
    private Box selectedBox;
    private Box currentBox;
    private BusNode currentBusNode;
    private User user;
    private double hoverOpacity;

    @FXML
    public void initialize() {
        initGraphics();

        existingBoxes  = new ArrayList<>();
        busNodes = new ArrayList<>();

        active = false;
        mouseDown = false;
        hoverOpacity = 0.7;

        // Initialise User
        user = new User();
        user.addMoney(1000000000);
        moneyLabel.setText(formatLargeNumber(user.getMoney()));

        // BACKGROUND
        drawBackground();

        // initialise events
        canvas.setOnMousePressed(e -> drawClicked(e.getX(), e.getY()));
        canvas.setOnMouseMoved(e -> drawUpdate(e.getX(),e.getY()));
        canvas.setOnScroll(e -> drawScroll(e.getDeltaY()));
        anchorPane.setOnKeyPressed(e -> keyPressed(e.getCode()));

        selectButton.setOnAction(e -> setMode("select"));
        boxButton.setOnAction(e -> setMode("building"));
        roadButton.setOnAction(e -> setMode("road"));
        busLineButton.setOnAction(e -> setMode("busLine"));
    }

    public void initGraphics() {
        midgc = midCanvas.getGraphicsContext2D();
        gc = canvas.getGraphicsContext2D();
        ogc = overlayCanvas.getGraphicsContext2D();
    }

    public void drawBackground()
    {
        clearOGCBoard();
        ogc.setFill(Color.rgb(115, 189, 89));
        ogc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        drawBoxes();
        drawBusLines(ogc);
        drawBusLineNodes();
    }

    private void drawBoxes()
    {
        for (Box box:existingBoxes)
        {
            ogc.setFill(box.getColor());
            ogc.fillPolygon(box.getXcoords(),box.getYcoords(),4);
        }
    }

    private void drawBusLineNodes()
    {
        for (BusNode node:busNodes)
        {
            ogc.setFill(Color.rgb(34, 170, 246));
            ogc.fillOval(node.getX()-4.0, node.getY()-4.0, 8.0, 8.0);
        }
    }

    public void setMode(String nextMode)
    {
        active = false;
        clearGCBoard();

        switch (nextMode) {
            case "building" -> {
                System.out.println("Created building");
                active = false;
                currentBox = new Building();
                gc.setFill(Color.rgb(243, 189, 96, .8));
            }
            case "road" -> {
                active = true;
                currentBox = new Road();
                gc.setFill(Color.rgb(87, 87, 87, .8));
            }
            case "select" -> {
                active = true;
            }
            case "busLine" -> {
                active = true;
                currentBusNode = new BusNode();
            }
        }

        mode = nextMode;
        String capitalise = nextMode.substring(0,1).toUpperCase() + nextMode.substring(1);
        infoLabel.setText(capitalise+" mode selected!");
    }

    public void drawScroll(double deltaY)
    {
        // Scroll up
        if (deltaY > 0)
        {
            currentBox.rotateBox(-45);
            user.setRotation(currentBox.getCurrentRotation());
            drawUpdate(currentBox.getX1(),currentBox.getY1());
        }
        // Scroll down
        else if (deltaY < 0)
        {
            currentBox.rotateBox(45);
            user.setRotation(currentBox.getCurrentRotation());
            drawUpdate(currentBox.getX1(),currentBox.getY1());
        }
    }

    public void drawClicked(double x, double y) {
        clearGCBoard();

        switch (mode)
        {
            case "select" -> {
                // Check if player wants to sell
                if (sellButton != null) {
                    if (sellButton.onMouseEnter(x, y) && selectedBox != null) {
                        double refund = selectedBox.getCost();
                        Thread taskThread = new Thread(() -> animateLabel(refund));
                        user.addMoney(refund);
                        moneyLabel.setText(formatLargeNumber(user.getMoney()));

                        taskThread.start();
                        existingBoxes.remove(selectedBox);
                    }
                }

                sellButton = null;
                midgc.clearRect(0, 0, midCanvas.getWidth(), midCanvas.getHeight());
                Box hoverBox = checkBoxInBounds(x, y);

                if (hoverBox != null) {
                    selectedBox = hoverBox;
                    // Attempt border
                    midgc.setFill(Color.WHITE);
                    midgc.fillPolygon(borderArray(hoverBox.getXcoords(), 2), borderArray(hoverBox.getYcoords(), 2), 4);
                    midgc.setFill(hoverBox.getColor());
                    midgc.fillPolygon(hoverBox.getXcoords(), hoverBox.getYcoords(), 4);

                    // Draw sell button
                    CTextBox btn = new CTextBox(10, 10, 50, 30, "SELL", Color.RED, Color.WHITE);
                    btn.draw(gc);
                    sellButton = btn;
                }
            }
            case "building" -> {
                if (!active) {
                    active = true;
                    infoLabel.setText("Press 'esc' to cancel");
                    currentBox.updatePosition(x, y);
                    gc.setFill(currentBox.getColor().deriveColor(0, 1, 1, hoverOpacity));
                } else {
                    if (user.deductMoney(currentBox.getCost())) {
                        // Creating a thread to make deduction info disappear after 3 seconds
                        double cost = currentBox.getCost() * -1.0;
                        Thread taskThread = new Thread(() -> animateLabel(cost));
                        taskThread.start();
                        // End

                        moneyLabel.setText(formatLargeNumber(user.getMoney()));
                        active = false;

                        infoLabel.setText("Building created!");
                        ogc.setFill(Color.rgb(243, 189, 96));

                        if (currentBox.getX1() > x) {
                            currentBox.setX2(x);
                        }
                        if (currentBox.getY1() > y) {
                            currentBox.setY2(y);
                        }

                        existingBoxes.add(currentBox);

                        currentBox = new Building();
                    } else {
                        infoLabel.setText("Not enough money!");
                    }
                }
            }
            case "road" -> {
                if (active) {
                    if (user.deductMoney(currentBox.cost)) {
                        moneyLabel.setText(formatLargeNumber(user.getMoney()));
                        infoLabel.setText("Road created!");

                        ogc.setFill(currentBox.getColor());

                        // Animation thread start
                        double cost = currentBox.getCost() * -1.0;
                        Thread taskThread = new Thread(() -> animateLabel(cost));
                        taskThread.start();
                        // Animation thread end

                        existingBoxes.add(currentBox);

                        currentBox = new Road();
                        currentBox.setCurrentRotation(user.getRotation());
                        gc.setFill(currentBox.getColor().deriveColor(0, 1, 1, hoverOpacity));
                    } else {
                        infoLabel.setText("Not enough money!");
                    }
                }
            }
            case "busLine" -> {
                // Check if bus node is locked onto a road node
                if (currentBusNode.getLocked()) {


                    busNodes.add(currentBusNode);

                    currentBusNode = new BusNode();
                    currentBusNode.addConnectedNode(busNodes.get(busNodes.size() - 1));

                    ogc.setFill(Color.rgb(34, 170, 246, hoverOpacity));
                }
            }
        }
        drawBackground();
    }

    public void drawUpdate(double x, double y) {
        if (mode.equals("select") && active)
        {
            clearGCBoard();
            Box hoverBox = checkBoxInBounds(x,y);
            if (hoverBox != null)
            {
                gc.setFill(hoverBox.getColor());
                ColorAdjust adjust = new ColorAdjust();
                adjust.setBrightness(0.2);
                gc.setEffect(adjust);
                gc.fillPolygon(hoverBox.getXcoords(), hoverBox.getYcoords(),4);
                gc.setEffect(null);
            }
            if (sellButton != null) {
                if (sellButton.onMouseEnter(x, y)) {
                    sellButton.drawHighlight(gc);
                } else {
                    sellButton.draw(gc);
                }
            }
        }

        // This is mainly just a ghost rectangle to guide the user.
        if (active && currentBox != null && !mode.equals("select") && !mode.equals("busLine"))
        {
            // Clear the canvas to avoid unnecessary ghost rectangles
            clearGCBoard();

            // Shorten doubles to make it easier to math
            double prevX = currentBox.getX1();
            double prevY = currentBox.getY1();
            double newX = x;
            double newY = y;

            if (mode.equals("building"))
            {
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
                gc.setFill(Color.rgb(220, 0, 0));
                gc.setFont(Font.font ("Verdana", 14));
                gc.fillText("-"+formatLargeNumber(currentBox.getCost()),x+10.0,y-5.0);
                gc.setFill(currentBox.getColor().deriveColor(0,1,1,hoverOpacity));
            }
            else if (mode.equals("road"))
            {
                currentBox.updatePosition(x,y);
                lockCursor((Road) currentBox);

                // Text
                gc.setFill(Color.rgb(220, 0, 0));
                gc.setFont(Font.font ("Verdana", 14));
                gc.fillText("-"+formatLargeNumber(currentBox.getCost()),x+10.0,y-5.0);
                gc.setFill(currentBox.getColor().deriveColor(0,1,1,hoverOpacity));
            }

//            if (isRectValid(x,y, newX, newY) == false) {
//                gc.setFill(Color.rgb(220, 0, 0, 0.5));
//            }

            gc.fillPolygon(currentBox.getXcoords(), currentBox.getYcoords(),4);
        }

        if (active && currentBusNode != null && mode.equals("busLine"))
        {
            clearGCBoard();

            currentBusNode.setPosition(x,y);
            currentBusNode.setLocked(lockCursorBusLine(currentBusNode));

            if (currentBusNode.getLocked())
            {
                gc.setFill(Color.rgb(34, 170, 246, hoverOpacity));
                gc.setStroke(Color.rgb(34, 170, 246, hoverOpacity));
            }
            else
            {
                gc.setFill(Color.rgb(220, 0, 0, hoverOpacity));
                gc.setStroke(Color.rgb(220, 0, 0, hoverOpacity));
            }
            gc.fillOval(currentBusNode.getX()-4.0, currentBusNode.getY()-4.0, 8.0, 8.0);

            if (busNodes.size() > 0)
            {
                BusNode lastNode = busNodes.get(busNodes.size()-1);
                gc.beginPath();
                gc.moveTo(lastNode.getX(), lastNode.getY());
                gc.setLineWidth(5.0);
                gc.lineTo(currentBusNode.getX(), currentBusNode.getY());
                gc.stroke();
            }
        }
    }

    // Escape to cancel action
    private void keyPressed(KeyCode key)
    {
        // CANCEL OPERATION
        if (key == KeyCode.ESCAPE)
        {
            clearGCBoard();
            active = false;
        }
        else if (key == KeyCode.R)
        {
            currentBox.rotateBox(45);
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
    private void clearGCBoard()
    {
        gc.clearRect(0,0,canvas.getWidth(),canvas.getHeight());
    }

    // Clears the bottom canvas board
    private void clearOGCBoard()
    {
        ogc.clearRect(0,0,canvas.getWidth(),canvas.getHeight());
    }

    //////////////////
    /// AESTHETICS ///
    //////////////////

    // Animates change in money

    private void animateLabel(double cost)
    {
        Label label = new Label();
        String formatCost = formatLargeNumber(cost);

        if (cost > 0)
        {
            label.setText("+" + formatCost);
            label.setStyle("-fx-text-fill: green");
        }
        else
        {
            label.setText(formatCost);
            label.setStyle("-fx-text-fill: red");
        }

        Platform.runLater(() -> anchorPane.getChildren().add(label));
        label.setLayoutY(50.0);

        int secondsWait = 15;
        while (secondsWait > 0) {
            label.setLayoutY(label.getLayoutY()-2.0);
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            secondsWait--;
        }

        label.setVisible(false);
        Platform.runLater(() -> anchorPane.getChildren().remove(label));
    }

    // Draw nodes
    private void drawNodes(Box cB, GraphicsContext graphicsC)
    {
        Paint previousColor = graphicsC.getFill();

        double[] xnode = cB.getXnodes();
        double[] ynode = cB.getYnodes();

        for (int i = 0;i < 12;i++)
        {
            graphicsC.setFill(Color.rgb(0, 255, 0));
            graphicsC.fillOval(xnode[i]-2.5, ynode[i]-2.5, 5.0, 5.0);
        }
        graphicsC.setFill(previousColor);
    }

    // Draw bus nodes
    private void drawBusNodes(Road cB, GraphicsContext graphicsC)
    {
        double[] xnode = cB.getBusNodeX();
        double[] ynode = cB.getBusNodeY();

        for (int i = 0;i < 2;i++)
        {
            graphicsC.setFill(Color.rgb(0, 255, 0));
            graphicsC.fillOval(xnode[i]-2.5, ynode[i]-2.5, 5.0, 5.0);
        }
    }

    private void drawBusLines(GraphicsContext graphicsC)
    {
        Paint previousColor = graphicsC.getFill();

        for (BusNode n:busNodes)
        {
            for (BusNode nChild:n.getConnectedNodes())
            {
                graphicsC.beginPath();
                graphicsC.moveTo(n.getX(), n.getY());
                graphicsC.setLineWidth(5.0);
                graphicsC.lineTo(nChild.getX(), nChild.getY());
                graphicsC.setStroke(Color.rgb(34, 170, 246));
                graphicsC.stroke();
            }
        }

        graphicsC.setFill(previousColor);
    }

    // Create border
    private double[] borderArray(double[] dArray, double multiplier)
    {
        double[] newArray = new double[dArray.length];

        double minNum = dArray[0], maxNum = dArray[0];

        for (double v : dArray) {
            if (v < minNum)
                minNum = v;
            if (v > maxNum)
                maxNum = v;
        }

        int i = 0;
        for (double num:dArray)
        {
            if (num == minNum)
                newArray[i] = num - multiplier;
            if (num == maxNum)
                newArray[i] = num + multiplier;
            i++;
        }

        return newArray;
    }

      //////////////////////////////
     /// CURSOR LOCKING METHODS ///
    //////////////////////////////

    private boolean lockCursorBusLine(BusNode node)
    {
        double closestX = node.getX();
        double closestY = node.getY();
        boolean locked = false;

        for (Box road:existingBoxes)
        {
            if (road instanceof Road)
            {
                double currentX = ((Road) road).getClosestBusX(node.getX(),node.getY());
                double currentY = ((Road) road).getClosestBusY(node.getX(),node.getY());

                if (currentX != -1 && currentY != -1)
                {
                    locked = true;
                    closestX = currentX;
                    closestY = currentY;
                }
            }
        }

        node.setPosition(closestX, closestY);

        return locked;
    }

    private void lockCursor(Road road)
    {
        double closestX = road.getX1();
        double closestY = road.getY1();

        // Second position
        for (Box box:existingBoxes)
        {
            double currentX = box.getClosestX(road.getXcoords()[3],road.getYcoords()[3]);
            double currentY = box.getClosestY(road.getXcoords()[3],road.getYcoords()[3]);
            double offsetX = road.getXcoords()[0] - road.getXcoords()[3];
            double offsetY = road.getYcoords()[0] - road.getYcoords()[3];

            if (currentX != -1 && currentY != -1)
            {
                closestX = currentX+offsetX;
                closestY = currentY+offsetY;
            }
        }

        // Mouse position
        for (Box box:existingBoxes)
        {
            double currentX = box.getClosestX(road.getX1(),road.getY1());
            double currentY = box.getClosestY(road.getX1(),road.getY1());

            if (currentX != -1 && currentY != -1)
            {
                    closestX = currentX;
                    closestY = currentY;
            }
        }



        road.updatePosition(closestX, closestY);
    }

    private Box checkBoxInBounds(double x, double y)
    {
        Box hoverBox = null;

        for (Box box:existingBoxes)
        {
             double[] xcoords = box.getXcoords();
             double[] ycoords = box.getYcoords();
             double minX = xcoords[0], maxX = xcoords[0];
             double minY = ycoords[0], maxY = ycoords[0];

             // Find min and max values
             for (int i = 0; i < 4; i++) {
                 if (xcoords[i] < minX)
                     minX = xcoords[i];
                 if (ycoords[i] < minY)
                     minY = ycoords[i];

                 if (xcoords[i] > maxX)
                     maxX = xcoords[i];
                 if (ycoords[i] > maxY)
                     maxY = ycoords[i];
             }

             // Check within boundaries
             if ((x > minX && x < maxX) && (y > minY && y < maxY))
             {
                 hoverBox = box;
             }
        }

        return hoverBox;
    }


    ////////////////////////
    // FORMATTING METHODS //
    ////////////////////////

    // Abbreviates large doubles as K, M or B
    // e.g 1.01K, 403.42M
    private String formatLargeNumber(double inputNumber)
    {
        DecimalFormat df = new DecimalFormat("#.##");
        String formatOutput = "";
        if (inputNumber < 0)
        {
            formatOutput += "-";
            inputNumber *= -1.0;
        }
        String output = String.valueOf(df.format(inputNumber));
        String outputNoDecimal = String.valueOf(Math.round(inputNumber));

        if (1000.0 <= inputNumber && inputNumber < 1000000.0)
        {
            output = outputNoDecimal.substring(0,outputNoDecimal.length()-3)+"."+output.substring(outputNoDecimal.length()-3,outputNoDecimal.length()-1)+" K";
        }
        else if (1000000.0 <= inputNumber && inputNumber < 1000000000.0)
        {
            output = outputNoDecimal.substring(0,outputNoDecimal.length()-6)+"."+output.substring(outputNoDecimal.length()-6,outputNoDecimal.length()-4)+" M";
        }
        else if (1000000000.00 <= inputNumber && inputNumber < 1000000000000.0)
        {
            output = outputNoDecimal.substring(0,outputNoDecimal.length()-9)+"."+output.substring(outputNoDecimal.length()-9,outputNoDecimal.length()-7)+" B";
        }

        formatOutput += "$"+output;

        return formatOutput;
    }
}