package common;

import components.main.MAGitController;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import static components.main.MAGitController.setTheme;

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

    public static void showExceptionDialog(Exception ex){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("ALERT");
        alert.setHeaderText(ex.getMessage());
        if (MAGitController.mainController.CSS_PATH != null) {
            alert.getDialogPane().getStyleClass().add("Background");
            alert.getDialogPane().getStylesheets().add(MAGitController.mainController.CSS_PATH);
        }
        alert.showAndWait();
    }

    public static void ALERT(String s, String title){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(s);
        setTheme(alert);
        alert.showAndWait();
    }
}
