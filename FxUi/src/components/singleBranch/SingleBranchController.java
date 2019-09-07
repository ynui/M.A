package components.singleBranch;

import appManager.Commit;
import appManager.appManager;
import common.ExceptionHandler;
import components.main.MAGitController;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextInputDialog;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class SingleBranchController {

    SimpleStringProperty nameProp;

    public List<Commit.commitComps> getCommitList() {
        return commitList;
    }

    public void setCommitList(List<Commit.commitComps> commitList) {
        this.commitList = commitList;
    }

    private List<Commit.commitComps> commitList;
    @FXML
    private Button branchBtn;

    public SingleBranchController() {
        nameProp = new SimpleStringProperty();
        commitList = new LinkedList<>();
    }

    public String getNameProp() {
        return nameProp.get();
    }

    public SimpleStringProperty namePropProperty() {
        return nameProp;
    }

    public void setNameProp(String nameProp) {
        this.nameProp.set(nameProp);
    }

    @FXML
    private void initialize() {
        branchBtn.textProperty().bind(nameProp);
    }

    @FXML
    public void showBranchCommits() throws IOException {
        MAGitController.mainController.showBranchCommits(nameProp.getValue());
    }

    @FXML
    private void branchCheckout(ActionEvent actionEvent) {
        try {
            MAGitController.mainController.branchCheckout(this.nameProp.getValue());
        } catch (IOException e) {
            ExceptionHandler.showExceptionDialog(e);
        }
    }

    @FXML
    private void branchDelete(ActionEvent actionEvent) {
        try{
        appManager.manager.deleteBranch(this.nameProp.getValue());
        MAGitController.mainController.updateUiRepoLabels();
        } catch (Exception ex){
            ExceptionHandler.showExceptionDialog(ex);
        }
    }

    @FXML
    private void branchReset(ActionEvent actionEvent) {
        TextInputDialog dialog = MAGitController.setNewDialog("Reset branch","Enter new Sha-1 for the branch "+this.nameProp.getValue(),"");
        dialog.showAndWait();
        try{
        appManager.manager.manuallyChangeBranch(dialog.getResult());
        } catch (Exception ex){
            ExceptionHandler.showExceptionDialog(ex);
        }
    }

    @FXML
    private void mergeBranch(ActionEvent actionEvent) {
        try {
            MAGitController.mainController.mergeBranch(this.getNameProp());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
