package dialogs;

import appManager.DiffHandler;
import components.main.MAGitController;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.stage.Stage;

public class CommitDataController {
    Stage stage;
    private SimpleStringProperty sha1Prop;
    private SimpleStringProperty noteProp;
    private SimpleStringProperty authorProp;
    private SimpleStringProperty dateProp;

    public void setFirstSha1Prop(String firstSha1Prop) {
        this.firstSha1Prop.set(firstSha1Prop);
    }

    public void setSecSha1Prop(String secSha1Prop) {
        this.secSha1Prop.set(secSha1Prop);
    }

    private SimpleStringProperty firstSha1Prop;
    private SimpleStringProperty secSha1Prop;
    private SimpleBooleanProperty firstSha1Exists;
    private SimpleBooleanProperty secSha1Exists;
    private DiffHandler diff1;
    private DiffHandler diff2;


    public void setSha1Prop(String sha1Prop) {
        this.sha1Prop.set(sha1Prop);
    }

    public void setNoteProp(String noteProp) {
        this.noteProp.set(noteProp);
    }

    public void setAuthorProp(String authorProp) {
        this.authorProp.set(authorProp);
    }

    public void setDateProp(String dateProp) {
        this.dateProp.set(dateProp);
    }

    public void setFirstSha1Exists(boolean firstSha1Exists) {
        this.firstSha1Exists.set(firstSha1Exists);
    }

    public void setSecSha1Exists(boolean secSha1Exists) {
        this.secSha1Exists.set(secSha1Exists);
    }


    public void setDiff1(DiffHandler diff1) {
        this.diff1 = diff1;
        MAGitController.addToModifiedListView(diff1.getCreated(),"CREATED",firstChangeList);
        MAGitController.addToModifiedListView(diff1.getChanged(),"CHANGED",firstChangeList);
        MAGitController.addToModifiedListView(diff1.getDeleted(),"REMOVED",firstChangeList);

    }

    public void setDiff2(DiffHandler diff2) {
        this.diff2 = diff2;
        MAGitController.addToModifiedListView(diff2.getCreated(),"CREATED",secChangeList);
        MAGitController.addToModifiedListView(diff2.getChanged(),"CHANGED",secChangeList);
        MAGitController.addToModifiedListView(diff2.getDeleted(),"REMOVED",secChangeList);
    }


    public CommitDataController() {
        this.sha1Prop = new SimpleStringProperty();
        this.noteProp = new SimpleStringProperty();
        this.authorProp = new SimpleStringProperty();
        this.dateProp = new SimpleStringProperty();
        this.firstSha1Prop = new SimpleStringProperty();
        this.secSha1Prop = new SimpleStringProperty();
        this.firstSha1Exists = new SimpleBooleanProperty(false);
        this.secSha1Exists = new SimpleBooleanProperty(false);
        firstChangeList = new TreeView();
        secChangeList = new TreeView();
    }


    @FXML
    private void initialize() {
        commitSha1Place.textProperty().bind(sha1Prop);
        commitNotePlace.textProperty().bind(noteProp);
        commitAuthorPlace.textProperty().bind(authorProp);
        commitDatePlace.textProperty().bind(dateProp);
        firstSha1Place.textProperty().bind(firstSha1Prop);
        secSha1Place.textProperty().bind(secSha1Prop);
        firstChangeList.visibleProperty().bind(firstSha1Exists);
        secChangeList.visibleProperty().bind(secSha1Exists);
        firstChangeList.setRoot(new TreeItem("root"));
        secChangeList.setRoot(new TreeItem("root"));

    }
    @FXML
    private TextField commitSha1Place;
    @FXML
    private TextField commitNotePlace;
    @FXML
    private Label firstSha1Place;
    @FXML
    private Label secSha1Place;
    @FXML
    private TextField commitAuthorPlace;
    @FXML
    private TreeView firstChangeList;
    @FXML
    private TreeView secChangeList;
    @FXML
    private TextField commitDatePlace;

    public void setPrimaryStage(Stage stage) {
        this.stage = stage;
    }
}
