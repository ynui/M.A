package components.main;


import appManager.*;
import common.Consts;
import common.ExceptionHandler;
import common.QuestionConsts;
import components.FileView;
import components.singleBranch.SingleBranchController;
import components.singleCommit.SingleCommitController;
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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tasks.deployXmlTask;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

import static appManager.ZipHandler.unzipFileToString;
import static appManager.ZipHandler.unzipFolderToCompList;
import static common.ExceptionHandler.showExceptionDialog;
import static common.QuestionConsts.askForYesNo;

public class MAGitController {

    @FXML
    private Label repoNameLabel;
    @FXML
    private Label usernameLabel;
    @FXML
    private Label repoLocationLabel;
    @FXML
    private Label headBranchLabel;
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
    private Tooltip repoLocationTooltip;

    private Stage primaryStage;
    private appManager manager;
    @FXML
    private ScrollPane modifiedFilesView;
    @FXML
    private VBox mainContent;
    @FXML
    private TreeView WcInfoList;
    @FXML
    private Label textPlace;

    public void showBranchCommits(String branchName) throws IOException {
        SingleBranchController b = new SingleBranchController();
        VBox target = MAGitController.mainController.getCommitsVbox();
        target.getChildren().clear();
        b.setCommitList(appManager.manager.branchHistoryToListBySha1(Branch.getCommitSha1ByBranchName(branchName)));
        for (Commit.commitComps c : b.getCommitList()) {
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
        repoLocationTooltip = new Tooltip();
        WcInfoList = new TreeView();
    }

    @FXML
    private void initialize() {
        usernameLabel.textProperty().bind(usernameProp);
        repoLocationLabel.textProperty().bind(repoPathProp);
        repoLocationTooltip.textProperty().bind(repoPathProp);
        repoNameLabel.textProperty().bind(repoNameProp);
        headBranchLabel.textProperty().bind(branchNameProp);
        branchActions.disableProperty().bind(isRepoLoaded.not());
        openInExplorerItem.disableProperty().bind(isRepoLoaded.not());
        commitBtn.disableProperty().bind(isCleanState.or(isRepoLoaded.not()));
        repoLocationLabel.setTooltip(repoLocationTooltip);
        WcInfoList.setRoot(new TreeItem("root"));
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
        showWcStatus();
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
            showExceptionDialog(ex);
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
                    if (askForYesNo("Target location: " + Paths.get(location) + "\n" + QuestionConsts.ASK_XML_OVERRIDE)) {
                        //System.out.println("Loading XML...");
                        manager.deleteRepository(location);
                        manager.deployXml(xmlRepo);
                        updateUiRepoLabels();
                        //System.out.println("Done!");

                    } else {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setHeaderText("XML loading terminated\nmoving to " + Paths.get(location));
                        alert.showAndWait();
                        manager.switchRepo(location);
                        updateUiRepoLabels();
                    }
                } else {
                    showExceptionDialog(new UnsupportedOperationException("the target folder in this XML '" + Paths.get(location) + "' is not supported by MAGit\nOperation terminated"));
                }
            } else if (Files.notExists(Paths.get(xmlRepo.getRepository().getLocation()))) {
                //System.out.println("Loading XML...");
//                deployXml(xmlRepo);
                manager.deployXml(xmlRepo);
                updateUiRepoLabels();
                //System.out.println("Done!");
            } else {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setHeaderText("Target location " + Paths.get(xmlRepo.getRepository().getLocation()) + "does not belong to MAGit. Loading terminated.");
            }
            showWcStatus();
        } catch (Exception e) {
            showExceptionDialog(e);
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
            showWcStatus();
        } catch (Exception e) {
            showExceptionDialog(e);
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
            showExceptionDialog(ex);
        }
    }

    @FXML
    private void openInExplorer(ActionEvent actionEvent) {
        try {
            Desktop.getDesktop().open(new File(String.valueOf(appManager.workingPath)));
        } catch (Exception ex) {
            showExceptionDialog(ex);
        }
    }

    @FXML
    public void showWcStatus() {
        if (appManager.workingPath == null) return;
        DiffHandler diff = manager.getDiff();
        if (appManager.isCleanState(diff)) {
            isCleanState.set(true);
            showCleanState();
        } else {
            isCleanState.set(false);
            showNotCleanState(diff);
        }
    }

    private void showCleanState() {
        textPlace.setText("Woo Hoo !\nNo local changes");
        if (WcInfoList.getRoot() != null)
            WcInfoList.getRoot().getChildren().clear();
    }

    private void showNotCleanState(DiffHandler diff) {
        textPlace.setText("This repository is dirty...\nThere are local changes");
        if (WcInfoList.getRoot() != null)
            WcInfoList.getRoot().getChildren().clear();
        addToModifiedListView(diff.getCreated(), "CREATED");
        addToModifiedListView(diff.getChanged(), "CHANGED");
        addToModifiedListView(diff.getDeleted(), "REMOVED");
    }

    private void addToModifiedListView(List<String> lst, String title) {
        if (lst.size() == 0) return;
        TreeItem root = new TreeItem(title);
        for (String s : lst) {
            TreeItem newItem = new TreeItem(s, getModificationIcon(title));
            root.getChildren().add(newItem);
        }
        WcInfoList.getRoot().getChildren().add(root);
    }

    private ImageView getModificationIcon(String title) {
        Image newImage;
        ImageView outImage;
        switch (title) {
            case "CREATED":
                newImage = new Image(Consts.PLUS_SIGN, 16, 16, true, false);
                break;
            case "CHANGED":
//                newImage = new Image(Consts.DOT_SIGN, 16, 16, true, false);
                newImage = new Image(getClass().getResourceAsStream(Consts.DOT_SIGN), 16, 16, true, false);
                break;
            case "REMOVED":
                newImage = new Image(Consts.MINUS_SIGN, 16, 16, true, false);
                break;
            case "FILE":
                newImage = new Image(Consts.FILE_ICON, 16, 16, true, false);
                break;
            case "FOLDER":
                newImage = new Image(Consts.FOLDER_ICON, 16, 16, true, false);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + title);
        }
        outImage = new ImageView(newImage);
        return outImage;
    }

    @FXML
    private void makeCommit(ActionEvent actionEvent) throws IOException {
        TextInputDialog dialog = setNewDialog("Commit", "Committing to " + manager.getHeadBranchName() + "\nEnter note:", "");
        dialog.showAndWait();
        if (dialog.getResult() == null) return;
        try {
            manager.createNewCommit(dialog.getResult());
        } catch (Exception e) {
            showExceptionDialog(e);
        }
        showBranchCommits(branchNameProp.getValue());
        showWcStatus();
    }

//    private Node getHeadBranchBtn() {
//        for (Node n : branchesVbox.getChildren())
//            if (n.toString().endsWith("'" + this.branchNameProp.getValue() + "'")) {
//                return n;
//            }
//        return null;
//    }

    public List<String> getCommitRep(String sha1) {
        return manager.getCommitRep(sha1);
    }

//    public void showCommitRep(String sha1) {
//        List<String> folderRep = unzipFolderToCompList(sha1, PathConsts.OBJECTS_FOLDER());
//        WcInfoList.getRoot().getChildren().clear();
//        List<String> prevComps;
//        for (String s : folderRep) {
//            prevComps = appManager.folderRepToList(s);
//            WcInfoList.getRoot().getChildren().add(new TreeItem<>(new FileView(prevComps)));
//        }
//    }

    public void showCommitRep(String sha1, TreeItem currRoot) {
        ImageView icon;
        if (sha1.equals(Consts.EMPTY_SHA1)) return;
        List<String> folderRep = unzipFolderToCompList(sha1, PathConsts.OBJECTS_FOLDER());
        List<String> prevComps;
        for (String s : folderRep) {
            prevComps = appManager.folderRepToList(s);
            icon = getModificationIcon(prevComps.get(2));
            TreeItem newTreeItem = new TreeItem<>(new FileView(prevComps), icon);
            currRoot.getChildren().add(newTreeItem);
            if (prevComps.get(2).equals("FOLDER")) {
                showCommitRep(prevComps.get((1)), newTreeItem);
            }
        }
    }

    public TreeView getWcInfoList() {
        return WcInfoList;
    }

    @FXML
    private void showFileContent() {
        TreeItem item = (TreeItem) WcInfoList.getSelectionModel().getSelectedItem();
        if (item != null) {
            if (item.getValue() instanceof FileView) {
                Folder.folderComponents folderRep = ((FileView) item.getValue()).getCompList();
                if (folderRep.getType().equals("FOLDER")) {
                    return;
                }
                String sha1 = folderRep.getSha1();
                File f = appManager.findFileInFolderByName(PathConsts.OBJECTS_FOLDER(), sha1);
                textPlace.setText(unzipFileToString(f));
            }
        }
    }

    public void branchCheckout(String branchName) throws IOException {
        DiffHandler diff = manager.getDiff();
        if (!appManager.isCleanState(diff))
            if (askForYesNo(QuestionConsts.ASK_COMMIT))
                makeCommit(null);
        try {
            manager.makeCheckOut(branchName);
            mainController.updateUiRepoLabels();
            mainController.showWcStatus();
        } catch (Exception ex) {
            ExceptionHandler.showExceptionDialog(ex);
        }
    }






}
