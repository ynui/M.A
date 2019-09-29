package dialogs;

import components.main.MAGitController;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Paths;

import static components.main.MAGitController.setTheme;

public class CloneController {
    @FXML
    private TextField nameInput;
    @FXML
    private TextField localPathInput;
    @FXML
    private TextField clonePathInput;


    public CloneController() {
        this.localPathProp = new SimpleStringProperty();
        this.clonePathProp = new SimpleStringProperty();
    }

    private SimpleStringProperty localPathProp;
    private SimpleStringProperty clonePathProp;

    private Stage primaryStage;


    @FXML
    private File dirPick() {
        DirectoryChooser dirChooser = new DirectoryChooser();
        File f = dirChooser.showDialog(primaryStage);
        return f;
    }


    @FXML
    private void closeDialog() {
        Stage stage = (Stage) nameInput.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void initialize() {
        localPathInput.textProperty().bind(localPathProp);
        clonePathInput.textProperty().bind(clonePathProp);
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    @FXML
    private void dirPickLocal(ActionEvent actionEvent) {
        File f = dirPick();
        if (f != null)
            localPathProp.set(f.getAbsolutePath());
    }

    @FXML
    private void dirPickClone(ActionEvent actionEvent) {
        File f = dirPick();
        if (f != null)
            clonePathProp.set(f.getAbsolutePath());
    }

    @FXML
    private void cloneRepository(ActionEvent actionEvent) {
        if (localPathInput.getText() == null || nameInput.getText() == null || clonePathInput.getText() == null || localPathInput.getText().equals("") || nameInput.getText().equals("") || clonePathInput.getText().equals("")) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText("All fields must not be empty.\nTry again");
            setTheme(alert);
            alert.showAndWait();
            return;
        }
        if (localPathInput.getText().endsWith("\\"))
            MAGitController.mainController.cloneRepository(Paths.get(localPathInput.getText() + nameInput.getText()), Paths.get(clonePathInput.getText()));
        else
            MAGitController.mainController.cloneRepository(Paths.get(localPathInput.getText() + "/" + nameInput.getText()), Paths.get(clonePathInput.getText()));
        closeDialog();
    }
}
