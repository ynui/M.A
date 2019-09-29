package dialogs;

import appManager.MergeHandler;
import common.ExceptionHandler;
import components.main.MAGitController;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

public class ConflictsController {

    private List<MergeHandler.Conflict> conflicts;
    private MergeHandler.Conflict conflict;
    String yourUsername;
    String yourDate;
    @FXML
    private Label ourLabel;
    @FXML
    private Label theirLabel;
    @FXML
    private Label fatherLabel;
    @FXML
    private Button deleteBtn;

    public void setYourUsername(String yourUsername) {
        this.yourUsername = yourUsername;
    }

    public void setYourDate(String yourDate) {
        this.yourDate = yourDate;
    }

    @FXML
    private Button oursBtn;
    @FXML
    private Button theirsBtn;
    @FXML
    private Button fatherBtn;
    @FXML
    private Button yourBtn;
    @FXML
    private TextArea yourVersion;
    @FXML
    private TextArea fatherVersion;
    @FXML
    private TextArea theirVersion;
    @FXML
    private TextArea ourVersion;
    @FXML
    private Label filePath;
    @FXML
    private ListView<MergeHandler.Conflict> conflictsList;


    public void setMergeHandler(MergeHandler mergeHandler) {
        this.mergeHandler = mergeHandler;
    }

    private MergeHandler mergeHandler;
    private Stage primaryStage;
    private SimpleStringProperty ourVersionProp;
    private SimpleStringProperty theirVersionProp;
    private SimpleStringProperty fatherVersionProp;
    private SimpleStringProperty pathProp;
    private SimpleBooleanProperty ourExists;
    private SimpleBooleanProperty theirExists;
    private SimpleBooleanProperty fatherExists;


    public void setConflicts(List<MergeHandler.Conflict> conflicts) {
        this.conflicts = conflicts;
        conflictsList.getItems().addAll(conflicts);
        conflictsList.getSelectionModel().selectFirst();
        setResolvingConflict();
    }


    public ConflictsController() {
        conflicts = new LinkedList<>();
        ourVersionProp = new SimpleStringProperty();
        theirVersionProp = new SimpleStringProperty();
        fatherVersionProp = new SimpleStringProperty();
        pathProp = new SimpleStringProperty();
        ourExists = new SimpleBooleanProperty(false);
        theirExists = new SimpleBooleanProperty(false);
        fatherExists = new SimpleBooleanProperty(false);
        conflictsList = new ListView<>();
    }

    @FXML
    private void initialize() {
        ourVersion.textProperty().bind(ourVersionProp);
        theirVersion.textProperty().bind(theirVersionProp);
        fatherVersion.textProperty().bind(fatherVersionProp);
        ourLabel.visibleProperty().bind(ourExists);
        theirLabel.visibleProperty().bind(theirExists);
        fatherLabel.visibleProperty().bind(fatherExists);
        ourVersion.visibleProperty().bind(ourExists);
        theirVersion.visibleProperty().bind(theirExists);
        fatherVersion.visibleProperty().bind(fatherExists);
        oursBtn.visibleProperty().bind(ourExists);
        theirsBtn.visibleProperty().bind(theirExists);
        fatherBtn.visibleProperty().bind(fatherExists);
        filePath.textProperty().bind(pathProp);
        deleteBtn.visibleProperty().bind(ourExists.or(theirExists.or(fatherExists)));
        yourBtn.disableProperty().bind(Bindings.isEmpty(yourVersion.textProperty()));
    }


    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    @FXML
    private void insertFather() throws IOException {
        this.mergeHandler.getRepresentationMap().put(Paths.get(conflict.getPath() + "/" + conflict.getFatherFile().getName()), conflict.compToFileRep(conflict.getFatherFile()));
        MAGitController.mainController.insertFileToWc(conflict.getPath(), conflict.getFatherFile().getName(), conflict.getFatherFile().getSha1());
        conflictsList.getItems().remove(conflict);
        conflicts.remove(conflict);
        closeIfDone();
    }

    @FXML
    private void insertOurs() {
        this.mergeHandler.getRepresentationMap().put(Paths.get(conflict.getPath() + "/" + conflict.getOurFile().getName()), conflict.compToFileRep(conflict.getOurFile()));
        conflictsList.getItems().remove(conflict);
        conflicts.remove(conflict);
        closeIfDone();
    }

    @FXML
    private void insertTheirs() throws IOException {
        this.mergeHandler.getRepresentationMap().put(Paths.get(conflict.getPath() + "/" + conflict.getTheirFile().getName()), conflict.compToFileRep(conflict.getTheirFile()));
        MAGitController.mainController.insertFileToWc(conflict.getPath(), conflict.getTheirFile().getName(), conflict.getTheirFile().getSha1());
        conflictsList.getItems().remove(conflict);
        conflicts.remove(conflict);
        closeIfDone();
    }

    @FXML
    private void insertYours() throws IOException {
        String text = yourVersion.getText();
        if (text.length() == 0)
            return;
        boolean alreadyExists = checkIfYoursAlreadyExists(text);
        if (!alreadyExists) {
            List<String> compList = getYourComps(text);
            MAGitController.createAndZipNewFile(text, conflict.getName());
            this.mergeHandler.getRepresentationMap().put(Paths.get(conflict.getPath() + "/" + conflict.getName()), compList);
            MAGitController.mainController.insertFileToWc(conflict.getPath(), conflict.getName(), compList.get(1));
        }
        conflictsList.getItems().remove(conflict);
        conflicts.remove(conflict);
        closeIfDone();
    }

    private List<String> getYourComps(String text) {
        List<String> out = new LinkedList();
        out.add(conflict.getName());
        out.add(MAGitController.stringToSha1(text));
        out.add("FILE");
        out.add(yourUsername);
        out.add(yourDate);
        return out;
    }

    private boolean checkIfYoursAlreadyExists(String text) throws IOException {
        String yourSha1 = MAGitController.stringToSha1(text);
        if (conflict.getOurFile() != null)
            if (yourSha1.equals(conflict.getOurFile().getSha1())) {
                ExceptionHandler.ALERT("Your version equals to ours.\nOur version will be added", "ALERT");
                insertOurs();
                return true;
            }
        if (conflict.getTheirFile() != null)
            if (yourSha1.equals(conflict.getTheirFile().getSha1())) {
                ExceptionHandler.ALERT("Your version equals to theirs.\nTheir version will be added", "ALERT");
                insertTheirs();
                return true;
            }
        if (conflict.getFatherFile() != null)
            if (yourSha1.equals(conflict.getFatherFile().getSha1())) {
                ExceptionHandler.ALERT("Your version equals to fathers.\nFather version will be added", "ALERT");
                insertFather();
                return true;
            }
        return false;
    }

    private void closeDialog() {
        Stage stage = (Stage) fatherVersion.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void setResolvingConflict() {
        if (conflictsList.getSelectionModel().getSelectedItem() != null) {
            conflict = conflictsList.getSelectionModel().getSelectedItem();
            updateWindowToConflict();
        }
    }

    private void updateWindowToConflict() {
        String ours = "", their = "", father = "";
        if (conflict.getOurFile() != null) {
            ours = MAGitController.getFileDataFromSha1(conflict.getOurFile().getSha1());
            ourExists.set(true);
        }
        if (conflict.getTheirFile() != null) {
            their = MAGitController.getFileDataFromSha1(conflict.getTheirFile().getSha1());
            theirExists.set(true);
        }
        if (conflict.getFatherFile() != null) {
            father = MAGitController.getFileDataFromSha1(conflict.getFatherFile().getSha1());
            fatherExists.set(true);
        }
        ourVersionProp.set(ours);
        theirVersionProp.set(their);
        fatherVersionProp.set(father);
        pathProp.set(conflict.getPath() + "\\" + conflict.getName());
    }

    private void closeIfDone() {
        if (conflictsList.getItems().isEmpty())
            closeDialog();
        else
            setResolvingConflict();
    }

    @FXML
    private void deleteFile(ActionEvent actionEvent) throws IOException {
        if (ourExists.getValue())
            mergeHandler.addToFilesToRemove(conflict.getName(), conflict.getPath());
        conflictsList.getItems().remove(conflict);
        conflicts.remove(conflict);
        closeIfDone();
    }
}
