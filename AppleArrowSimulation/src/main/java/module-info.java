module com.example.applearrowsimulation {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.applearrowsimulation to javafx.fxml;
    exports com.example.applearrowsimulation;
}