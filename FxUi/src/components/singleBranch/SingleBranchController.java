package components.singleBranch;

import appManager.Branch;
import appManager.Commit;
import appManager.appManager;
import common.ExceptionHandler;
import components.main.MAGitController;
import components.singleCommit.SingleCommitController;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

public class SingleBranchController {

    SimpleStringProperty nameProp;
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
        VBox target = MAGitController.mainController.getCommitsVbox();
        target.getChildren().clear();
        commitList = appManager.manager.branchHistoryToListBySha1(Branch.getCommitSha1ByBranchName(nameProp.getValue()));
        for (Commit.commitComps c : commitList) {
            FXMLLoader loader = new FXMLLoader();
            URL url = getClass().getResource("../singleCommit/singleCommit.fxml");
            loader.setLocation(url);
            Node singleCommit = loader.load();
            SingleCommitController singleCommitController = loader.getController();
            singleCommitController.setNoteProp(c.getNote());
            singleCommitController.setAuthorProp("By: " + c.getAuthor());
            singleCommitController.setSha1Prop(c.getSha1());
            singleCommitController.getCommitBtn().setTooltip(new Tooltip(c.getNote()));
            target.getChildren().add(singleCommit);
        }
    }

    @FXML
    private void branchCheckout(ActionEvent actionEvent) {
        try {
            appManager.manager.makeCheckOut(this.nameProp.getValue());
            MAGitController.mainController.updateUiRepoLabels();
            MAGitController.mainController.showWcStatus();
        } catch (Exception ex){
            ExceptionHandler.exceptionDialog(ex);
        }
    }

    @FXML
    private void branchDelete(ActionEvent actionEvent) {
        try{
        appManager.manager.deleteBranch(this.nameProp.getValue());
        MAGitController.mainController.updateUiRepoLabels();
        } catch (Exception ex){
            ExceptionHandler.exceptionDialog(ex);
        }
    }

    @FXML
    private void branchReset(ActionEvent actionEvent) {
        TextInputDialog dialog = MAGitController.setNewDialog("Reset branch","Enter new Sha-1 for the branch "+this.nameProp.getValue(),"");
        dialog.showAndWait();
        try{
        appManager.manager.manuallyChangeBranch(dialog.getResult());
        } catch (Exception ex){
            ExceptionHandler.exceptionDialog(ex);
        }
    }
}
