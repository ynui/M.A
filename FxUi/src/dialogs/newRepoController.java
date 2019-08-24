package dialogs;

import appManager.appManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;

import static common.ExceptionHandler.showExceptionDialog;

public class newRepoController {
    @FXML
    private TextField nameInput;
    @FXML
    private TextField localPathInput;


    public newRepoController() {
        this.pathInput = new SimpleStringProperty();
    }

    private SimpleStringProperty pathInput;

    private Stage primaryStage;


    @FXML
    private File dirPick() {
        DirectoryChooser dirChooser = new DirectoryChooser();
        File f = dirChooser.showDialog(primaryStage);
        pathInput.set(String.valueOf(f.toPath()));
        return f;
    }

    @FXML
    private void createRepo() {
        if(localPathInput.getText() == null || nameInput.getText() == null || localPathInput.getText().equals("") || nameInput.getText().equals("")){
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText("All fields must not be empty.\nTry again");
            alert.showAndWait();
            return;
        }
        try {
            appManager.manager.createEmptyRepository(localPathInput.getText() + nameInput.getText());
        } catch (Exception e) {
            showExceptionDialog(e);
        } finally {
            closeDialog();
        }
    }

    @FXML
    private void closeDialog() {
        Stage stage = (Stage)nameInput.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void initialize() {
        localPathInput.textProperty().bind(pathInput);
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }
}
