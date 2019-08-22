package components.singleCommit;

import components.main.MAGitController;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;


public class SingleCommitController {

    private SimpleStringProperty noteProp;
    private SimpleStringProperty authorProp;
    @FXML
    private VBox singleCommitVBox;

    public Label getNoteLabel() {
        return noteLabel;
    }

    @FXML
    private Label noteLabel;
    @FXML
    private Label authorLabel;

    public SingleCommitController() {
        this.noteProp = new SimpleStringProperty();
        this.authorProp = new SimpleStringProperty();
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
}
