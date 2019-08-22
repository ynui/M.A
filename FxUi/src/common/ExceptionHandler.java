package common;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class ExceptionHandler extends Throwable {
    @FXML
    private Label msg;
    @FXML
    private Button OkBtn;

    private SimpleStringProperty msgProp;

    @FXML
    private void initialize() {
        msg.textProperty().bind(msgProp);
    }

    public ExceptionHandler(String msg) {
        msgProp = new SimpleStringProperty("msg");
    }

    @FXML
    private void exitDialog() {
        return;
    }

    public static void exceptionDialog(Exception ex){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("ALERT");
        alert.setHeaderText(ex.getMessage());
        alert.showAndWait();
    }
}
