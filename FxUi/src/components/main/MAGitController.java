package components.main;


import appManager.Branch;
import appManager.DiffHandler;
import appManager.XmlRepo;
import appManager.appManager;
import common.ExceptionHandler;
import common.QuestionConsts;
import components.singleBranch.SingleBranchController;
import dialogs.newRepoController;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tasks.deployXmlTask;

import javax.xml.soap.Text;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

import static common.ExceptionHandler.exceptionDialog;
import static common.QuestionConsts.askForYesNo;

public class MAGitController {

    @FXML
    private Label repoNameLabel;
    @FXML
    private Label usernameLabel;
    @FXML
    private Label repoLocationLabel;
    @FXML
    private Label branchNameLabel;
    @FXML
    private MenuButton repoActions;
    @FXML
    private MenuItem repoLoadXml;
    @FXML
    private MenuItem repoInitEmpty;
    @FXML
    private MenuItem repoSwitch;
    @FXML
    private MenuButton branchActions;
    @FXML
    private MenuItem branchShowAll;
    @FXML
    private MenuItem branchCreateNew;
    @FXML
    private MenuItem branchDelete;
    @FXML
    private MenuItem branchCheckout;
    @FXML
    private MenuItem branchReset;
    @FXML
    private ScrollPane branchesScrollPane;
    @FXML
    private ScrollPane commitsScrollPane;
    @FXML
    private Button commitTreeBtn;
    @FXML
    private Button commitBtn;
    @FXML
    private Button homeBtn;
    @FXML
    private MenuItem openInExplorerItem;
    @FXML
    private VBox branchesVbox;
    @FXML
    private VBox commitsVbox;

    public static MAGitController mainController;
    private SimpleStringProperty repoNameProp;
    private SimpleStringProperty repoPathProp;
    private SimpleStringProperty usernameProp;
    private SimpleStringProperty branchNameProp;
    private SimpleBooleanProperty isRepoLoaded;
    private SimpleBooleanProperty isCleanState;
    private List<Branch> branchList;

    private Stage primaryStage;
    private appManager manager;
    @FXML
    private ListView<String> modifiedList;
    @FXML
    private ListView<String> deletedList;
    @FXML
    private ListView<String> createdList;
    @FXML
    private ScrollPane modifiedFilesView;
    @FXML
    private VBox mainContent;

    public VBox getBranchesVbox() {
        return branchesVbox;
    }



    public MAGitController() {
        mainController = this;
        setAppManager(new appManager());
        repoNameProp = new SimpleStringProperty();
        repoPathProp = new SimpleStringProperty();
        usernameProp = new SimpleStringProperty();
        usernameProp.set(manager.getUsername());
        branchNameProp = new SimpleStringProperty();
        branchList = new LinkedList<>();
        isRepoLoaded = new SimpleBooleanProperty(false);
        isCleanState = new SimpleBooleanProperty(true);
    }

    @FXML
    private void initialize() {
        usernameLabel.textProperty().bind(usernameProp);
        repoLocationLabel.textProperty().bind(repoPathProp);
        repoNameLabel.textProperty().bind(repoNameProp);
        branchNameLabel.textProperty().bind(branchNameProp);
        branchActions.disableProperty().bind(isRepoLoaded.not());
        openInExplorerItem.disableProperty().bind(isRepoLoaded.not());
        modifiedFilesView.visibleProperty().bind(isCleanState.not());
    }

    @FXML
    private void initEmptyRepo() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("../../dialogs/newRepoDialog.fxml"));
        Parent parent = fxmlLoader.load();
        newRepoController controller = fxmlLoader.getController();
        Scene scene = new Scene(parent, 361, 206);
        Stage stage = new Stage();
        controller.setPrimaryStage(stage);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(scene);
        stage.showAndWait();
        updateUiRepoLabels();
    }


    public void updateUiRepoLabels() {
        if (appManager.workingPath == null) return;
        try {
            isRepoLoaded.set(true);
            repoPathProp.set(String.valueOf(appManager.workingPath));
            repoNameProp.set(String.valueOf(appManager.workingPath.getName(appManager.workingPath.getNameCount() - 1)));
            branchNameProp.set(manager.getHeadBranchName());
            branchesVbox.getChildren().clear();
            getCommitsVbox().getChildren().clear();
            showAllBranches(null);
        } catch (Exception ex) {
            ExceptionHandler.exceptionDialog(ex);
        }
    }

    public static TextInputDialog setNewDialog(String title, String headerText, String contentText) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(title);
        dialog.setHeaderText(headerText);
        dialog.setContentText(contentText);
        return dialog;
    }

    @FXML
    private void changeUsername() {
        TextInputDialog dialog = setNewDialog("Change Username", "Current user: " + manager.getUsername(), "Choose a new username: ");
        dialog.showAndWait();
        if (!(dialog.getResult() == null)) {
            manager.setUsername(dialog.getResult());
            usernameProp.set(dialog.getResult());
        }
    }

    @FXML
    private void loadXmlRepo() {
        FileChooser fileChooser = new FileChooser();
        File f = fileChooser.showOpenDialog(primaryStage);
        if (f == null) return;
        try {
            XmlRepo xmlRepo = new XmlRepo(f.getAbsolutePath());
            String location = xmlRepo.getRepository().getLocation();
            xmlRepo.checkIfLegalRepo(xmlRepo);
            if (Files.exists(Paths.get(location))) {
                if (manager.isMagitRepo(location)) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setHeaderText("Target location: " + Paths.get(location));
                    alert.showAndWait();
                    if (askForYesNo(QuestionConsts.ASK_XML_OVERRIDE)) {
                        //System.out.println("Loading XML...");
                        manager.deleteRepository(location);
//                        deployXml(xmlRepo);
                        manager.deployXml(xmlRepo);
                        //System.out.println("Done!");
                    } else {
                        alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setHeaderText("XML loading terminated\nmoving to " + Paths.get(location));
                        alert.showAndWait();
                        manager.switchRepo(location);
                        //System.out.println("Done!");
                    }
                } else {
                    exceptionDialog(new UnsupportedOperationException("the target folder in this XML '" + Paths.get(location) + "' is not supported by MAGit\nOperation terminated"));
                }
            } else if (Files.notExists(Paths.get(xmlRepo.getRepository().getLocation()))) {
                //System.out.println("Loading XML...");
//                deployXml(xmlRepo);
                manager.deployXml(xmlRepo);
                //System.out.println("Done!");
            } else {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setHeaderText("Target location " + Paths.get(xmlRepo.getRepository().getLocation()) + "does not belong to MAGit. Loading terminated.");
            }
            updateUiRepoLabels();
        } catch (Exception e) {
            exceptionDialog(e);
        }
    }

    private void deployXml(XmlRepo xmlRepo) {
        Task<Boolean> deployXmlTask = new deployXmlTask(xmlRepo, manager);
        new Thread(deployXmlTask).start();
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public void setAppManager(appManager manager) {
        this.manager = manager;
    }


    @FXML
    private void switchRepo() {
        DirectoryChooser dirChooser = new DirectoryChooser();
        File f = dirChooser.showDialog(primaryStage);
        if (f == null) return;
        try {
            manager.switchRepo(f.getAbsolutePath());
            updateUiRepoLabels();
        } catch (Exception e) {
            exceptionDialog(e);
        }
    }

    @FXML
    private void showAllBranches(ActionEvent actionEvent) throws IOException {
        branchesVbox.getChildren().clear();
        branchList = Branch.allBranchesToList();
        for (Branch b : branchList) {
            FXMLLoader loader = new FXMLLoader();
            URL url = getClass().getResource("../singleBranch/singleBranch.fxml");
            loader.setLocation(url);
            Node singleBranch = loader.load();
            SingleBranchController singleBranchController = loader.getController();
            singleBranchController.setNameProp(b.getName());
            branchesVbox.getChildren().add(singleBranch);
        }
    }

    public VBox getCommitsVbox() {
        return commitsVbox;
    }

    @FXML
    private void createNewBranch(ActionEvent actionEvent) {
        TextInputDialog dialog = setNewDialog("Create new branch", "Enter a name for the new branch", "");
        dialog.showAndWait();
        appManager.manager.createNewBranch(dialog.getResult());
        try {
            if (askForYesNo(QuestionConsts.ASK_CHECKOUT)) {
                DiffHandler diff;
                diff = manager.getDiff();
                if (!appManager.isCleanState(diff)) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setHeaderText("This repository is not in \"Clean State\"\nCheckout was not preformed");
                } else {
                    manager.makeCheckOut(dialog.getResult());
                }
            }
            updateUiRepoLabels();
        } catch (Exception ex) {
            ExceptionHandler.exceptionDialog(ex);
        }
    }

    @FXML
    private void openInExplorer(ActionEvent actionEvent) {
        try {
            Desktop.getDesktop().open(new File(String.valueOf(appManager.workingPath)));
        } catch (Exception ex) {
            ExceptionHandler.exceptionDialog(ex);
        }
    }

    @FXML
    private void showWcStatus(ActionEvent actionEvent) {
        if(appManager.workingPath == null) return;
        DiffHandler diff = manager.getDiff();
        if (appManager.isCleanState(diff)){
            isCleanState.set(true);
           showCleanState();
            return;
        }
        showNotCleanState(diff);
    }

    private void showCleanState() {
        mainContent.getChildren().clear();
        mainContent.getChildren().add(new Label("Woo Hoo!\nNo Local "))
    }

    private void showNotCleanState(DiffHandler diff) {
        isCleanState.set(false);
        modifiedList.getItems().clear();
        addToModifiedListView(diff.getCreated(),"+ CREATED:");
        addToModifiedListView(diff.getChanged(),"* CHANGED:");
        addToModifiedListView(diff.getDeleted(),"- REMOVED:");
        modifiedList.setPrefHeight(modifiedList.getItems().size() * 24);
    }

    private void addToModifiedListView(List<String> lst, String icon) {
        String chaser =icon+" ";
        List<String> temp = new LinkedList<>();
        temp.addAll(lst);
        temp.replaceAll(string -> chaser.concat(string));
        modifiedList.getItems().addAll(temp);
    }

    @FXML
    private void makeCommit(ActionEvent actionEvent) {
        TextInputDialog dialog = setNewDialog("Commit", "Commting to "+manager.getHeadBranchName()+"\nEnter note:","");
        dialog.showAndWait();
        if(dialog.getResult() == null) return;
        try {
            manager.createNewCommit(dialog.getResult());
        } catch (IOException e) {
            ExceptionHandler.exceptionDialog(e);
        }
    }
}
