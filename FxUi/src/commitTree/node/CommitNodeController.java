package commitTree.node;

import components.main.MAGitController;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import static appManager.Branch.getCommitComponents;
import static common.ExceptionHandler.showExceptionDialog;

public class CommitNodeController {
    public boolean hasRemoteBranches() {
        return hasRemoteBranches;
    }

    private boolean hasRemoteBranches;
    private List<String> branchNames;

    public List<String> getRemoteBranchNames() {
        return remoteBranchNames;
    }

    private List<String> remoteBranchNames;
    @FXML
    private VBox remoteBranchesVBox;
    @FXML
    private Separator remoteBranchSeparator;

    public List<String> getBranchNames() {
        return branchNames;
    }

    public boolean hasBranches() {
        return hasBranches;
    }

    private boolean hasBranches;

    @FXML
    private Separator branchSeparator;
    @FXML
    private VBox branchesVBox;

    public String getSha1() {
        return sha1;
    }

    private String sha1;

    public void setSha1(String sha1) {
        this.sha1 = sha1;
    }

    @FXML
    private Label commitTimeStampLabel;
    @FXML
    private Label messageLabel;

    public Label getCommitTimeStampLabel() {
        return commitTimeStampLabel;
    }

    public Label getMessageLabel() {
        return messageLabel;
    }

    public Label getCommitterLabel() {
        return committerLabel;
    }

    @FXML
    private Label committerLabel;
    @FXML
    private Circle CommitCircle;
    @FXML
    private HBox NODE;

    public void setCommitTimeStamp(String timeStamp) {
        commitTimeStampLabel.setText(timeStamp);
        commitTimeStampLabel.setTooltip(new Tooltip(timeStamp));
    }

    public void setCommitter(String committerName) {
        committerLabel.setText(committerName);
        committerLabel.setTooltip(new Tooltip(committerName));
    }

    public void setCommitMessage(String commitMessage) {
        messageLabel.setText(commitMessage);
        messageLabel.setTooltip(new Tooltip(commitMessage));
    }

    public void setBranch(String branch) {
        branchNames.add(branch);
        branchesVBox.getChildren().add(new Text(branch));
    }
    public void setRemoteBranch(String branch) {
        remoteBranchNames.add(branch + " - REMOTE");
        remoteBranchesVBox.getChildren().add(new Text(branch));
    }

    public void setCommitCircle(Circle commitCircle) {
        CommitCircle = commitCircle;
    }

    public int getCircleRadius() {
        return (int) CommitCircle.getRadius();
    }

    public Circle getCommitCircle() {
        return CommitCircle;
    }

    @FXML
    private void initialize() {
        branchNames = new LinkedList<>();
        remoteBranchNames = new LinkedList<>();
        hasBranches = false;
        hasRemoteBranches = false;
        ContextMenu cn = new ContextMenu();
        //cn.setOnHidden(e->this.committerLabel.requestFocus());
        MenuItem dataItem = new MenuItem("Get data");
        dataItem.setOnAction(e -> {
            try {
                MAGitController.mainController.showCommitData(this);
            } catch (IOException ex) {
                showExceptionDialog(ex);
                return;
            }
        });
        MenuItem newBranchToThisCommit = new MenuItem("Create a new branch from this commit");
        newBranchToThisCommit.setOnAction(e -> {
            MAGitController.mainController.createBranchToCommit(this);
        });
        MenuItem resetHeadBranchToThisCommit = new MenuItem("Reset head branch to this commit");
        resetHeadBranchToThisCommit.setOnAction(e -> {
            MAGitController.mainController.resetHeadBranchToCommit(this);
        });
        Menu mergePointingBranchWithHead = new Menu("Merge pointing branch with Head Branch");

        Menu deletePointingBranch = new Menu("Delete pointing branch");


        //add all branches pointing to this commit and bla bla bla
        cn.getItems().add(dataItem);
        cn.getItems().add(newBranchToThisCommit);
        cn.getItems().add(resetHeadBranchToThisCommit);
        cn.getItems().add(mergePointingBranchWithHead);
        cn.getItems().add(deletePointingBranch);
        NODE.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                List<String> commitComponents = getCommitComponents(this.getSha1());
                MAGitController.mainController.getWcInfoList().getRoot().getChildren().clear();
                MAGitController.mainController.showCommitRep(commitComponents.get(0), MAGitController.mainController.getWcInfoList().getRoot());
            }
        });
        NODE.setOnContextMenuRequested(e -> {
            cn.show(NODE, e.getScreenX(), e.getScreenY());
            MAGitController.mainController.fillBranchOptions(this, mergePointingBranchWithHead.getItems(), deletePointingBranch.getItems());
            if (mergePointingBranchWithHead.getItems().size() == 0)
                mergePointingBranchWithHead.disableProperty().setValue(true);
            if (deletePointingBranch.getItems().size() == 0)
                deletePointingBranch.disableProperty().setValue(true);
            NODE.requestFocus();
        });
    }

    public void makeBranchVisible() {
        hasBranches = true;
        branchesVBox.setVisible(true);
        branchSeparator.setVisible(true);
    }
    public void makeRemoteBranchVisible() {
        hasRemoteBranches = true;
        remoteBranchesVBox.setVisible(true);
        remoteBranchSeparator.setVisible(true);
    }

    public void mark(){
        this.committerLabel.getStyleClass().add("marked");
        this.messageLabel.getStyleClass().add("marked");
        this.commitTimeStampLabel.getStyleClass().add("marked");
        this.branchesVBox.getStyleClass().add("marked");
        this.remoteBranchesVBox.getStyleClass().add("marked");

        getCommitCircle().setFill(Color.BLUEVIOLET);
    }

    public void unMark() {
        this.committerLabel.getStyleClass().remove("marked");
        this.messageLabel.getStyleClass().remove("marked");
        this.commitTimeStampLabel.getStyleClass().remove("marked");
        this.branchesVBox.getStyleClass().remove("marked");
        this.remoteBranchesVBox.getStyleClass().remove("marked");
        getCommitCircle().setFill(Color.DARKGREY);
    }
}
