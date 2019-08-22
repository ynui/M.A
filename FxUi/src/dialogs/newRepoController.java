package dialogs;

import appManager.appManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;

import static common.ExceptionHandler.exceptionDialog;

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
        try {
            appManager.manager.createEmptyRepository(localPathInput.getText() + nameInput.getText());
        } catch (Exception e) {
            exceptionDialog(e);
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
