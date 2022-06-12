module com.example.drawingapp {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.drawingapp to javafx.fxml;
    exports com.example.drawingapp;
}