package components.singleCommit;

import appManager.Folder;
import appManager.PathConsts;
import components.main.MAGitController;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

import java.util.List;

import static appManager.Branch.getCommitComponents;
import static appManager.ZipHandler.unzipFolderToCompList;


public class SingleCommitController {

    @FXML
    private Label noteLabel;
    @FXML
    private Label authorLabel;

    private SimpleStringProperty noteProp;
    private SimpleStringProperty authorProp;
    private SimpleStringProperty sha1Prop;
    private MAGitController mainController;
    public Button getCommitBtn() {
        return commitBtn;
    }

    @FXML
    private Button commitBtn;

    public void setSha1Prop(String sha1Prop) {
        this.sha1Prop.set(sha1Prop);
    }

    @FXML
    private VBox singleCommitVBox;



    public SingleCommitController() {
        this.noteProp = new SimpleStringProperty();
        this.authorProp = new SimpleStringProperty();
        this.sha1Prop = new SimpleStringProperty();
        mainController = MAGitController.mainController;
//        noteLabel.setWrapText(true);
    }

    public String getNoteProp() {
        return noteProp.get();
    }

    public SimpleStringProperty notePropProperty() {
        return noteProp;
    }

    public void setNoteProp(String noteProp) {
        this.noteProp.set(noteProp);
    }

    public String getAuthorProp() {
        return authorProp.get();
    }

    public SimpleStringProperty authorPropProperty() {
        return authorProp;
    }

    public void setAuthorProp(String authorProp) {
        this.authorProp.set(authorProp);
    }


    @FXML
    private void initialize() {
        noteLabel.textProperty().bind(noteProp);
        authorLabel.textProperty().bind(authorProp);
//      singleCommitVBox.prefWidthProperty().bind(MAGitController.mainController.getCommitsVbox().widthProperty());
    }

    @FXML
    private void showCommitFiles(ActionEvent actionEvent) {
        //you have commit sha1 now need to get the folderrep of it and send it to showcommitrep
        List<String> commitComponents = getCommitComponents(sha1Prop.getValue());
        mainController.showCommitRep(commitComponents.get(0));
    }
}
