package dialogs;

import appManager.MergeHandler;
import components.main.MAGitController;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

public class ConflictsController {

    private List<MergeHandler.Conflict> conflicts;
    @FXML
    private Button oursBtn;
    @FXML
    private Button theirsBtn;
    @FXML
    private Button fatherBtn;
    @FXML
    private Button yourBtn;

    public void setConflict(MergeHandler.Conflict conflict) {
        this.conflict = conflict;
        start();
    }

    private MergeHandler.Conflict conflict;

    public void setMergeHandler(MergeHandler mergeHandler) {
        this.mergeHandler = mergeHandler;
    }

    private MergeHandler mergeHandler;
    private Stage primaryStage;
    private SimpleStringProperty ourVersionProp;
    private SimpleStringProperty theirVersionProp;
    private SimpleStringProperty fatherVersionProp;
    private SimpleBooleanProperty ourExists;
    private SimpleBooleanProperty theirExists;
    private SimpleBooleanProperty fatherExists;

    @FXML
    private Label ourVersion;
    @FXML
    private Label theirVersion;
    @FXML
    private Label fatherVersion;
    @FXML
    private TextArea yourVersion;


    public void setConflicts(List<MergeHandler.Conflict> conflicts) {
        this.conflicts = conflicts;
    }


    public ConflictsController() {
        conflicts = new LinkedList<>();
        ourVersionProp = new SimpleStringProperty();
        theirVersionProp = new SimpleStringProperty();
        fatherVersionProp = new SimpleStringProperty();
        ourExists = new SimpleBooleanProperty(false);
        theirExists = new SimpleBooleanProperty(false);
        fatherExists = new SimpleBooleanProperty(false);
    }

    @FXML
    private void initialize() {
        ourVersion.textProperty().bind(ourVersionProp);
        theirVersion.textProperty().bind(theirVersionProp);
        fatherVersion.textProperty().bind(fatherVersionProp);
        oursBtn.disableProperty().bind(ourExists.not());
        theirsBtn.disableProperty().bind(theirExists.not());
        fatherBtn.disableProperty().bind(fatherExists.not());
    }


    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public void start() {
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

    }

    @FXML
    private void insertFather() throws IOException {
        this.mergeHandler.getRepresentationMap().put(Paths.get(conflict.getPath() + "/" + conflict.getFatherFile().getName()), conflict.compToFileRep(conflict.getFatherFile()));
        MAGitController.mainController.insertFileToWc(conflict.getPath(), conflict.getFatherFile().getName(), conflict.getFatherFile().getSha1());
        closeDialog();
    }

    @FXML
    private void insertOurs() {
        this.mergeHandler.getRepresentationMap().put(Paths.get(conflict.getPath() + "/" + conflict.getOurFile().getName()), conflict.compToFileRep(conflict.getOurFile()));
        closeDialog();
    }

    @FXML
    private void insertTheirs() throws IOException {
        this.mergeHandler.getRepresentationMap().put(Paths.get(conflict.getPath() + "/" + conflict.getTheirFile().getName()), conflict.compToFileRep(conflict.getTheirFile()));
        MAGitController.mainController.insertFileToWc(conflict.getPath(), conflict.getFatherFile().getName(), conflict.getFatherFile().getSha1());
        closeDialog();
    }

    @FXML
    private void insertYours() {
        //not yet...
        //  this.mergeHandler.getRepresentationMap().put(conflict.getPath(), conflict.compToFileRep(conflict.getTheirFile()));
        closeDialog();
    }

    private void closeDialog() {
        Stage stage = (Stage) fatherVersion.getScene().getWindow();
        stage.close();
    }
}
