package com.example.drawingapp;

import javafx.scene.control.Label;

public class TestThread implements Runnable {
    private Label label;

    TestThread(Label label)
    {
        this.label = label;
    }

    public void run() {
        int secondsWait = 2;
        while (secondsWait > 0) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            secondsWait--;
        }
        label.setVisible(false);
    }
}