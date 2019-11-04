package components.main;


import appManager.*;
import com.fxgraph.edges.Edge;
import com.fxgraph.graph.Graph;
import com.fxgraph.graph.ICell;
import com.fxgraph.graph.Model;
import commitTree.layout.CommitTreeLayout;
import commitTree.node.CommitNode;
import commitTree.node.CommitNodeController;
import common.Consts;
import common.QuestionConsts;
import components.FileView;
import dialogs.CloneController;
import dialogs.CommitDataController;
import dialogs.ConflictsController;
import dialogs.newRepoController;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import puk.team.course.magit.ancestor.finder.AncestorFinder;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.*;

import static appManager.Commit.*;
import static appManager.ZipHandler.*;
import static appManager.appManager.*;
import static common.ExceptionHandler.ALERT;
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
    private Button commitBtn;
    @FXML
    private Button homeBtn;
    @FXML
    private MenuItem openInExplorerItem;

    public static String CSS_PATH = null;
    public static MAGitController mainController;
    private SimpleStringProperty repoNameProp;
    private SimpleStringProperty repoPathProp;
    private SimpleStringProperty usernameProp;
    private SimpleStringProperty branchNameProp;
    private SimpleBooleanProperty isRepoLoaded;
    private SimpleBooleanProperty isCleanState;
    private SimpleBooleanProperty isRemoteRepo;
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
    private TextArea textPlace;
    @FXML
    private ScrollPane commitTree;
    @FXML
    private Menu checkoutToAction;
    @FXML
    private Menu mergeWithAction;
    @FXML
    private Menu deleteBranchAction;
    @FXML
    private Menu branchActions;
    @FXML
    private MenuItem cloneOption;
    @FXML
    private MenuItem fetchOption;
    @FXML
    private MenuItem pullOption;
    @FXML
    private MenuItem pushOption;
    private Graph treeContent;
    @FXML
    private Menu markBranchAction;


    public static String getFileDataFromSha1(String sha1) {
        return appManager.getFileDataFromSha1(sha1);
    }

    public static String stringToSha1(String text) {
        return appManager.stringToSha1(text);
    }

    public static void createAndZipNewFile(String text, String name) {
        File newFile = appManager.createFileRepresentation(text, name);
        try {
            zipFile(newFile, MAGitController.stringToSha1(text), PathConsts.OBJECTS_FOLDER());
        } catch (IOException e) {
            showExceptionDialog(e);
            return;
        }
    }

    public void showCommitData(CommitNodeController commitNodeController) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/dialogs/CommitDataDialog.fxml"));
        Parent parent = fxmlLoader.load();
        CommitDataController controller = fxmlLoader.getController();
        Scene scene = new Scene(parent);
        Stage stage = new Stage();
        controller.setPrimaryStage(stage);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(scene);
        setTheme(scene);

        List<String> prevCommits = getPrevCommitsBySha1(commitNodeController.getSha1());
        DiffHandler diff = new DiffHandler();
        if (prevCommits.size() == 0) {
            controller.setFirstSha1Exists(false);
            controller.setSecSha1Exists(false);
        } else if (prevCommits.size() == 1) {
            controller.setFirstSha1Exists(true);
            controller.setSecSha1Exists(false);
            controller.setFirstSha1Prop(prevCommits.get(0));
            diff.createModifiedFilesListsBetween2FolderSha1(Commit.getCommitRootFolderSha1(prevCommits.get(0)), Commit.getCommitRootFolderSha1(commitNodeController.getSha1()));
            controller.setDiff1(diff);
        } else if (prevCommits.size() == 2) {
            DiffHandler diff2 = new DiffHandler();
            controller.setFirstSha1Exists(true);
            controller.setSecSha1Exists(true);
            controller.setFirstSha1Prop(prevCommits.get(0));
            controller.setSecSha1Prop(prevCommits.get(1));
            diff.createModifiedFilesListsBetween2FolderSha1(Commit.getCommitRootFolderSha1(prevCommits.get(0)), Commit.getCommitRootFolderSha1(commitNodeController.getSha1()));
            diff2.createModifiedFilesListsBetween2FolderSha1(Commit.getCommitRootFolderSha1(prevCommits.get(1)), Commit.getCommitRootFolderSha1(commitNodeController.getSha1()));
            controller.setDiff1(diff);
            controller.setDiff2(diff2);
        }
        controller.setSha1Prop(commitNodeController.getSha1());
        controller.setAuthorProp(commitNodeController.getCommitterLabel().getText());
        controller.setNoteProp(commitNodeController.getMessageLabel().getText());
        controller.setDateProp(commitNodeController.getCommitTimeStampLabel().getText());
        stage.showAndWait();
    }

    public static void setTheme(Scene s) {
        s.getStylesheets().clear();
        s.getRoot().getStyleClass().add("Background");
        if (CSS_PATH != null)
            s.getStylesheets().add(CSS_PATH);
    }

    public static void setTheme(Alert a) {
        a.getDialogPane().getStylesheets().clear();
        a.getDialogPane().getStyleClass().add("Background");
        if (MAGitController.mainController.CSS_PATH != null) {
            a.getDialogPane().getStylesheets().add(MAGitController.mainController.CSS_PATH);
        }
    }

//    public void showBranchCommits(String branchName) throws IOException {
//        SingleBranchController b = new SingleBranchController();
//        VBox target = MAGitController.mainController.getCommitsVbox();
//        target.getChildren().clear();
//        b.setCommitList(appManager.manager.branchHistoryToListByCommitSha1(Branch.getCommitSha1ByBranchName(branchName)));
//        for (Commit.commitComps c : b.getCommitList()) {
//            FXMLLoader loader = new FXMLLoader();
//            URL url = getClass().getResource("../singleCommit/singleCommit.fxml");
//            loader.setLocation(url);
//            Node singleCommit = loader.load();
//            SingleCommitController singleCommitController = loader.getController();
//            singleCommitController.setNoteProp(c.getNote());
//            singleCommitController.setAuthorProp("By: " + c.getAuthor());
//            singleCommitController.setSha1Prop(c.getSha1());
//            singleCommitController.getCommitBtn().setTooltip(new Tooltip(c.getNote()));
//            target.getChildren().add(singleCommit);
//        }
//    }

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
        isRemoteRepo = new SimpleBooleanProperty(false);
        repoLocationTooltip = new Tooltip();
        WcInfoList = new TreeView();
        treeContent = null;
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
        fetchOption.disableProperty().bind(isRemoteRepo.not());
        pushOption.disableProperty().bind(isRemoteRepo.not());
        pullOption.disableProperty().bind(isRemoteRepo.not());
        WcInfoList.setRoot(new TreeItem("root"));
    }

    @FXML
    private void initEmptyRepo() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/dialogs/newRepoDialog.fxml"));
        Parent parent = fxmlLoader.load();
        newRepoController controller = fxmlLoader.getController();
        Scene scene = new Scene(parent, 361, 206);
        Stage stage = new Stage();
        controller.setPrimaryStage(stage);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(scene);
        setTheme(scene);
        stage.showAndWait();
        updateUI();
    }

    public void updateUI() {
        if (appManager.workingPath == null) return;
        try {
            String repoPath = isRemoteRepo() ?
                    (appManager.workingPath + "\nRemote: " + getRemotePath()) :
                    String.valueOf(appManager.workingPath);
            isRepoLoaded.set(true);
            repoPathProp.set(repoPath);
            repoNameProp.set(String.valueOf(appManager.workingPath.getName(appManager.workingPath.getNameCount() - 1)));
            branchNameProp.set(manager.getHeadBranchName());
            setRemoteOptions();
            showCommitTree();
            showWcStatus();
        } catch (Exception ex) {
            showExceptionDialog(ex);
        }
    }

    private void setRemoteOptions() {
        if (appManager.isRemoteRepo())
            isRemoteRepo.set(true);
        else
            isRemoteRepo.set(false);
    }

    public static TextInputDialog setNewDialog(String title, String headerText, String contentText) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(title);
        dialog.setHeaderText(headerText);
        dialog.setContentText(contentText);
        dialog.getDialogPane().getStylesheets().clear();
        if (MAGitController.mainController.CSS_PATH != null) {
            dialog.getDialogPane().getStyleClass().add("Background");
            dialog.getDialogPane().getStylesheets().add(MAGitController.mainController.CSS_PATH);
        }
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
                        updateUI();
                        //System.out.println("Done!");

                    } else {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setHeaderText("XML loading terminated\nmoving to " + Paths.get(location));
                        setTheme(alert);
                        alert.show();
                        manager.switchRepo(location);
                        updateUI();
                    }
                } else {
                    showExceptionDialog(new UnsupportedOperationException("the target folder in this XML '" + Paths.get(location) + "' is not supported by MAGit\nOperation terminated"));
                }
            } else if (Files.notExists(Paths.get(xmlRepo.getRepository().getLocation()))) {
                manager.deployXml(xmlRepo);
                updateUI();
            } else {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setHeaderText("Target location " + Paths.get(xmlRepo.getRepository().getLocation()) + "does not belong to MAGit. Loading terminated.");
                setTheme(alert);
                alert.show();
            }
            showWcStatus();
        } catch (Exception e) {
            showExceptionDialog(e);
        }
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
            updateUI();
        } catch (Exception e) {
            showExceptionDialog(e);
        }
    }

    @FXML
    private void showAllBranches(ActionEvent actionEvent) {
        List<Branch> branches = Branch.allBranchesToList();
        List<String> local = new LinkedList<>();
        List<String> remote = new LinkedList<>();
        List<String> rtb = new LinkedList<>();
        getBranchNames(branches, local, remote, rtb);
        if (WcInfoList.getRoot() != null)
            WcInfoList.getRoot().getChildren().clear();
        addToModifiedListView(local, "LOCAL", WcInfoList);
        addToModifiedListView(remote, "REMOTE", WcInfoList);
        addToModifiedListView(rtb, "REMOTE-TRACKING", WcInfoList);
    }

    private void getBranchNames(List<Branch> branches, List<String> local, List<String> remote, List<String> rtb) {
        List<String> out = new LinkedList<>();
        for (Branch b : branches) {
            if (b.getName().equals("HEAD")) continue;
            if (b.isRemote())
                remote.add(b.getName());
            else if (b.checkRTB())
                rtb.add(b.getName());
            else
                local.add(b.getName());
        }
        Collections.sort(out);
    }

    @FXML
    private String createNewBranch(ActionEvent actionEvent) {
        TextInputDialog dialog = setNewDialog("Create new branch", "Enter a name for the new branch", "");
        dialog.showAndWait();
        appManager.manager.createNewBranch(dialog.getResult());
        if (actionEvent != null) {
            if (Branch.doesBranchExists(Branch.allBranchesToList(), dialog.getResult())) {
                showExceptionDialog(new UnsupportedOperationException(dialog.getResult() + " already exists"));
                return null;
            }
            try {
                if (askForYesNo(QuestionConsts.ASK_CHECKOUT)) {
                    DiffHandler diff;
                    diff = manager.getDiff();
                    if (!appManager.isCleanState(diff)) {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setHeaderText("This repository is not in \"Clean State\"\nCheckout was not preformed");
                        setTheme(alert);
                        alert.show();
                    } else {
                        manager.makeCheckOut(dialog.getResult());
                        updateUI();
                    }
                }
//                updateUI();
            } catch (Exception ex) {
                showExceptionDialog(ex);
            }
        }
        return dialog.getResult();
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
        addToModifiedListView(diff.getCreated(), "CREATED", WcInfoList);
        addToModifiedListView(diff.getChanged(), "CHANGED", WcInfoList);
        addToModifiedListView(diff.getDeleted(), "REMOVED", WcInfoList);
    }

    public static void addToModifiedListView(List<String> lst, String title, TreeView tree) {
        if (lst.size() == 0) return;
        TreeItem root = new TreeItem(title);
        for (String s : lst) {
            TreeItem newItem = new TreeItem(s, getModificationIcon(title));
            root.getChildren().add(newItem);
        }
        tree.getRoot().getChildren().add(root);
    }

    private static ImageView getModificationIcon(String title) {
        Image newImage;
        ImageView outImage;
        switch (title) {
            case "CREATED":
                newImage = new Image(Consts.PLUS_SIGN, 16, 16, true, false);
                break;
            case "CHANGED":
                newImage = new Image(Consts.DOT_SIGN, 16, 16, true, false);
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
                return null;
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
        updateUI();
    }

    public List<String> getCommitRep(String sha1) {
        return manager.getCommitRep(sha1);
    }

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
            updateUI();
        } catch (Exception ex) {
            showExceptionDialog(ex);
        }
    }

    public void showCommitTree() {
        Graph tree = new Graph();
        List<Commit.commitComps> allCommits = manager.getAllCommits();
        addCommitsToGraph(tree, allCommits);
        addEdgesToGraph(tree);
        addBranchesToGraph(tree);
        tree.endUpdate();
        setTreePositioning(tree.getModel(), Branch.allBranchesToList());
        this.treeContent = tree;
        showTreeDialog(tree);
    }

    private void addBranchesToGraph(Graph tree) {
        Map<String, List<Branch>> branchesHeads = new HashMap<>();
        Map<String, List<Branch>> remoteBranchesHeads = new HashMap<>();
        Model model = tree.getModel();
        List<Branch> branches = Branch.allBranchesToList();
        for (Branch b : branches) {
            if (b.getName().equals("HEAD")) continue;
            if (b.isRemote()) {
                if (!remoteBranchesHeads.containsKey(b.getCommitSha1()))
                    remoteBranchesHeads.put(b.getCommitSha1(), new LinkedList<Branch>());
                remoteBranchesHeads.get(b.getCommitSha1()).add(b);
            } else {
                if (!branchesHeads.containsKey(b.getCommitSha1()))
                    branchesHeads.put(b.getCommitSha1(), new LinkedList<Branch>());
                branchesHeads.get(b.getCommitSha1()).add(b);
            }
        }
        for (ICell cell : model.getAllCells()) {
            CommitNode commitNode = (CommitNode) cell;
            if (branchesHeads.containsKey(commitNode.getSha1())) {
                for (Branch b : branchesHeads.get(commitNode.getSha1()))
                    commitNode.setBranch(b.getName());
            }
            if (remoteBranchesHeads.containsKey(commitNode.getSha1()))
                for (Branch b : remoteBranchesHeads.get(commitNode.getSha1()))
                    commitNode.setRemoteBranch(b.getName());
        }
    }

    @FXML
    private void markTree(ActionEvent actionEvent) {
        if (treeContent == null) return;
        Model model = treeContent.getModel();
        for (ICell cell : model.getAllCells()) {
            CommitNode commitNode = (CommitNode) cell;
            commitNode.mark();
        }
    }

    private void addEdgesToGraph(Graph tree) {
        Model model = tree.getModel();
        for (ICell cell : model.getAllCells()) {
            CommitNode commitNode = (CommitNode) cell;
            List<String> commitRepList = unzipFolderToCompList(commitNode.getSha1(), PathConsts.OBJECTS_FOLDER());
            Commit.commitComps commitRep = new Commit.commitComps(commitNode.getSha1(), commitRepList.get(0), commitRepList.get(1), commitRepList.get(2), commitRepList.get(3), commitRepList.get(4), commitRepList.get(5));
            List<String> prevCommits = getPrevCommits(commitRep);
            for (String sha1 : prevCommits)
                connectGraphNodes(tree, cell, sha1);
        }
    }

    private void connectGraphNodes(Graph tree, ICell originCell, String sha1) {
        Model model = tree.getModel();
        for (ICell cell : model.getAllCells()) {
            CommitNode commitNode = (CommitNode) cell;
            if (commitNode.getSha1().equals(sha1)) {
                model.addEdge(new Edge(originCell, commitNode));
                break;
            }
        }
    }

    private void showTreeDialog(Graph tree) {
        try {
            tree.layout(new CommitTreeLayout());
            showGraph(tree);
        } catch (Exception ex) {
            showExceptionDialog(ex);
            return;
        }
    }


    private void addCommitsToGraph(Graph tree, List<Commit.commitComps> allCommits) {
        List<Commit.commitComps> reversedAllCommits = new LinkedList<>(allCommits);
        Collections.reverse(reversedAllCommits);
        Model model = tree.getModel();
        tree.beginUpdate();
        for (Commit.commitComps c : reversedAllCommits) {
            ICell cell = new CommitNode(c.getDate(), c.getAuthor(), c.getNote(), c.getSha1());
            model.addCell(cell);
        }
        tree.endUpdate();
    }

    private void setTreePositioning(Model model, List<Branch> branchesTreeOrdered) {
        List<String> usedSha1 = new LinkedList<>();
        for (ICell cell : model.getAllCells()) {
            CommitNode commitNode = (CommitNode) cell;
            for (int i = 0; i < branchesTreeOrdered.size(); i++) {
                List<Commit.commitComps> commits = Branch.getAllCommits(branchesTreeOrdered.get(i));
                for (Commit.commitComps c : commits) {
                    if (c.getSha1().equals(commitNode.getSha1())) {
                        if (usedSha1.contains(c.getSha1()))
                            commitNode.setPos(i);
                        else {
                            usedSha1.add(c.getSha1());
                            commitNode.setPos(i);
                            break;
                        }
                    }
                }
            }
        }
        //initial commit is always pos(0)
        if (model.getAllCells().size() > 0) {
            ICell firstCommit = model.getAllCells().get(model.getAllCells().size() - 1);
            CommitNode node = (CommitNode) firstCommit;
            node.setPos(0);
        }
    }

    private Map<String, Boolean> getCommitsMap(List<Commit.commitComps> allCommits) {
        Map<String, Boolean> out = new HashMap();
        for (Commit.commitComps c : allCommits)
            out.put(c.getSha1(), false);
        return out;
    }

    private void showGraph(Graph tree) throws Exception {
        commitTree.setContent(tree.getCanvas());
        Platform.runLater(() -> {
            tree.getUseNodeGestures().set(false);
            tree.getUseViewportGestures().set(false);
        });
    }

    public void mergeBranch(String branchName, boolean pullMerge) throws IOException {
        if (!pullMerge) {
            if (Branch.isBranchActive(branchName)) {
                showExceptionDialog(new UnsupportedOperationException("Cannot merge with the active branch"));
                return;
            }
            DiffHandler diff = manager.getDiff();
            if (!manager.isCleanState(diff)) {
                showExceptionDialog(new UnsupportedOperationException("Local changes apply\nPlease commit and try again\n\nOperation terminated."));
                return;
            }
        }
        String oursCommitSha1 = Branch.getCommitSha1ByBranchName(Branch.getActiveBranch());
        String theirCommitSha1 = Branch.getCommitSha1ByBranchName(branchName);
        String commonFatherSha1 = getCommonFatherSha1(oursCommitSha1, theirCommitSha1);
        if (pullMerge)
            theirCommitSha1 = getRRBranchOriginalSha1ByName(getRemotePath(), branchName);
        try {
            if (checkForFFMerge(oursCommitSha1, theirCommitSha1, commonFatherSha1)) {
                if (pullMerge)
                    Branch.changeBranchPointedCommit(branchName, theirCommitSha1, true);
                Branch.changeBranchPointedCommit(Branch.getActiveBranch(), theirCommitSha1, false);
//              preformFFMerge(theirCommitSha1, oursCommitSha1, branchName);
                manager.makeCheckOut(Branch.getActiveBranch());
                updateUI();
                return;
            }
        } catch (Exception e) {
            showExceptionDialog(e);
            return;
        }
        TextInputDialog dialog = setNewDialog("Merging Commit", "Merging commit to: " + manager.getHeadBranchName() + "\nEnter note:", "");
        dialog.showAndWait();
        if (dialog.getResult() == null) return;
        Commit newCommit = new Commit(dialog.getResult(), usernameProp.getValue());
        MergeHandler mergeHandler = new MergeHandler();
        mergeHandler.getMergedHeadFolderAndSaveFiles(commitSha1ToHeadFolderFilesList(oursCommitSha1), commitSha1ToHeadFolderFilesList(theirCommitSha1), commitSha1ToHeadFolderFilesList(commonFatherSha1), appManager.workingPath);
        resolveConflicts(mergeHandler, newCommit.getAuthor(), newCommit.getDateCreated());
        if (mergeHandler.getConflicts().size() > 0) {
            showExceptionDialog(new UnsupportedOperationException("You did not resolve all of the conflicts\nOperation Terminated"));
            return;
        }
        deployAndRemoveMergedFiles(mergeHandler.getFilesToAdd(), mergeHandler.getFilesToRemove());
        createMergedCommit(manager, newCommit, branchName, mergeHandler);
        updateUI();
    }

    private String getHeadBranchCommitSha1() {
        return manager.getHeadBranchCommitSha1();
    }

    private void preformFFMerge(String bossCommitSha1, String sha1ToAdd, String branchName) {
        try {
            appManager.preformFFMerge(bossCommitSha1, sha1ToAdd, branchName);
        } catch (IOException ex) {
            showExceptionDialog(ex);
            return;
        }
    }

    private boolean checkForFFMerge(String oursCommitSha1, String theirCommitSha1, String commonFatherSha1) {
        boolean found = false;
        if (oursCommitSha1.equals(theirCommitSha1))
            throw new UnsupportedOperationException("These branches are equal! Nothing to merge\nOperation terminated");
        if (theirCommitSha1.equals(commonFatherSha1))
            throw new UnsupportedOperationException(headBranchLabel.getText() + " Is a straight evolution of this branch!\nNothing to merge");
        if (oursCommitSha1.equals(commonFatherSha1))
            found = true;
        return found;
    }

    private void deployAndRemoveMergedFiles(List<MergeHandler.FileToAdd> filesToAdd, List<MergeHandler.FileToRemove> filesToRemove) {
        try {
            for (MergeHandler.FileToRemove f : filesToRemove) {
                if (Paths.get(f.getPath() + "/" + f.getName()).toFile().isDirectory()) {
                    appManager.deleteAllFilesFromFolder(f.getPath());
                } else
                    appManager.deleteFileFromFolder(f.getPath().toString(), f.getName());
            }
        } catch (Exception ex) {
            showExceptionDialog(ex);
            return;
        }
        for (MergeHandler.FileToAdd f : filesToAdd) {
            try {
                appManager.insertFileToWc(f.getPath(), f.getName(), f.getSha1());
            } catch (IOException ex) {
                showExceptionDialog(ex);
                return;
            }
        }
    }

    private void resolveConflicts(MergeHandler mergeHandler, String yourUsername, String yourDateCreated) throws
            IOException {
        if (mergeHandler.getConflicts().isEmpty()) return;
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/dialogs/conflictsDialog.fxml"));
        Parent parent = fxmlLoader.load();
        ConflictsController controller = fxmlLoader.getController();
        Scene scene = new Scene(parent);
        Stage stage = new Stage();
        controller.setPrimaryStage(stage);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(scene);
        setTheme(scene);
        controller.setMergeHandler(mergeHandler);
        controller.setYourDate(yourDateCreated);
        controller.setYourUsername(yourUsername);
        controller.setConflicts(mergeHandler.getConflicts());
        stage.showAndWait();
    }

    private String getCommonFatherSha1(String oursCommitSha1, String theirCommitSha1) {
        return appManager.getCommonFatherSha1(oursCommitSha1, theirCommitSha1);
    }

    public void insertFileToWc(Path path, String name, String sha1) throws IOException {
        if (Files.exists(Paths.get(path + "/" + name))) {
            manager.deleteFileFromFolder(String.valueOf(path), name);
        }
        manager.deployFile(sha1, path);
    }

    public void createBranchToCommit(CommitNodeController commitNodeController) {
        String sha1 = commitNodeController.getSha1();
        String newBranchName = createNewBranch(null);
        Branch.changeBranchPointedCommit(newBranchName, sha1, false);
        try {
            if (askForYesNo(QuestionConsts.ASK_CHECKOUT)) {
                DiffHandler diff;
                diff = manager.getDiff();
                if (!appManager.isCleanState(diff)) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setHeaderText("This repository is not in \"Clean State\"\nCheckout was not preformed");
                    setTheme(alert);
                    alert.show();
                } else {
                    manager.makeCheckOut(newBranchName);
                }
            }
        } catch (Exception ex) {
            showExceptionDialog(ex);
        }
        updateUI();
    }

    public void resetHeadBranchToCommit(CommitNodeController commitNodeController) {
        String sha1 = commitNodeController.getSha1();
        Branch.changeBranchPointedCommit(Branch.getActiveBranch(), sha1, false);
        try {
            manager.makeCheckOut(Branch.getActiveBranch());
        } catch (FileSystemException ex) {
            showExceptionDialog(ex);
            return;
        }
        updateUI();
    }

    public void fillBranchOptions(CommitNodeController commitNodeController, ObservableList<MenuItem> mergingItems, ObservableList<MenuItem> deletingItems) {
        if (!commitNodeController.hasBranches() && !commitNodeController.hasRemoteBranches()) return;
        List<String> branchNames = commitNodeController.getBranchNames();
        List<String> remoteBranchNames = commitNodeController.getRemoteBranchNames();
        List<List<String>> allNames = new LinkedList<>();
        allNames.add(branchNames);
        allNames.add(remoteBranchNames);
        mergingItems.clear();
        deletingItems.clear();
        MenuItem itemToMerging;
        MenuItem itemToDeleting;
        for (List<String> lst : allNames) {
            for (String branchName : lst) {
                if (branchName.equals(Branch.getActiveBranch())) continue;
                itemToMerging = new MenuItem(branchName);
                itemToDeleting = new MenuItem(branchName);
                itemToMerging.setOnAction(e -> {
                    try {
                        if (branchName.endsWith(" - REMOTE"))
                            mergeBranch(branchName.split(" - REMOTE")[0], false);
                        else
                            mergeBranch(branchName, false);
                    } catch (IOException ex) {
                        showExceptionDialog(ex);
                        return;
                    }
                });
                itemToDeleting.setOnAction(e -> {
                    try {
                        if (branchName.endsWith(" - REMOTE"))
                            manager.deleteBranch(branchName.split(" - REMOTE")[0]);
                        else
                            manager.deleteBranch(branchName);
                        updateUI();
                    } catch (Exception ex) {
                        showExceptionDialog(ex);
                    }
                });
                mergingItems.add(itemToMerging);
                if (!branchName.endsWith(" - REMOTE"))
                    deletingItems.add(itemToDeleting);
            }
        }
    }


    @FXML
    private void manuallyResetBranch(ActionEvent actionEvent) {
        DiffHandler diff = manager.getDiff();
        if (!manager.isCleanState(diff)) {
            if (!askForYesNo("Open changes apply.\n" +
                    "All open changes will be lost forever.\n" +
                    "Would you like to continue?")) return;
        }
        TextInputDialog dialog = MAGitController.setNewDialog("Reset branch", "Enter new Sha-1 for " + Branch.getActiveBranch(), "");
        dialog.showAndWait();
        try {
            manager.manuallyChangeBranch(dialog.getResult());
            manager.makeCheckOut(Branch.getActiveBranch());
            updateUI();
        } catch (Exception ex) {
            showExceptionDialog(ex);
        }
    }

    @FXML
    private void changeTheme1(ActionEvent actionEvent) {
        primaryStage.getScene().getStylesheets().clear();
        CSS_PATH = null;
    }

    @FXML
    private void changeTheme2(ActionEvent actionEvent) {
        primaryStage.getScene().getStylesheets().clear();
        primaryStage.getScene().getStylesheets().add("/components/main/theme2.css");
        CSS_PATH = "/components/main/theme2.css";
    }

    @FXML
    private void changeTheme3(ActionEvent actionEvent) {
        primaryStage.getScene().getStylesheets().clear();
        primaryStage.getScene().getStylesheets().add("/components/main/theme3.css");
        CSS_PATH = "/components/main/theme3.css";
    }

    @FXML
    private void setBranchActionsMenu(Event event) {
        mergeWithAction.getItems().clear();
        deleteBranchAction.getItems().clear();
        checkoutToAction.getItems().clear();
        markBranchAction.getItems().clear();
        MenuItem mergeMenuItem;
        MenuItem deleteMenuItem;
        MenuItem checkoutMenuItem;
        MenuItem markBranchMenuItem;
        List<Branch> allBranches = Branch.allBranchesToList();
        List<Branch> toRemove = new LinkedList<Branch>();
        for (Branch b : allBranches) {
            if (b.getName().equals("HEAD")) continue;
            String branchName = b.isRemote() ? b.getName() + " - REMOTE" : b.getName();
            mergeMenuItem = new MenuItem(branchName);
            deleteMenuItem = new MenuItem(branchName);
            checkoutMenuItem = new MenuItem(branchName);
            markBranchMenuItem = new MenuItem(branchName);

            mergeMenuItem.setOnAction(e -> {
                try {
                    if (branchName.endsWith(" - REMOTE"))
                        mergeBranch(branchName.split(" - REMOTE")[0], false);
                    else
                        mergeBranch(branchName, false);
                } catch (IOException ex) {
                    showExceptionDialog(ex);
                    return;
                }
            });
            deleteMenuItem.setOnAction(e -> {
                try {
                    if (branchName.endsWith(" - REMOTE"))
                        manager.deleteBranch(branchName.split(" - REMOTE")[0]);
                    else
                        manager.deleteBranch(branchName);
                    updateUI();
                } catch (Exception ex) {
                    showExceptionDialog(ex);
                    return;
                }
            });
            checkoutMenuItem.setOnAction(e -> {
                try {
                    if (branchName.endsWith(" - REMOTE"))
                        askToCreateRTB(branchName.split(" - REMOTE")[0]);
                    else
                        branchCheckout(branchName);
                } catch (IOException ex) {
                    showExceptionDialog(ex);
                    return;
                }
            });
            markBranchMenuItem.setOnAction(e -> markCommits(branchName));
            if (!b.isActive()) {
                mergeWithAction.getItems().add(mergeMenuItem);
                checkoutToAction.getItems().add(checkoutMenuItem);
            }
            if (!b.isRemote() && !b.isActive())
                deleteBranchAction.getItems().add(deleteMenuItem);
            markBranchAction.getItems().add(markBranchMenuItem);
        }
        mergeWithAction.disableProperty().setValue(mergeWithAction.getItems().isEmpty());
        checkoutToAction.disableProperty().setValue(checkoutToAction.getItems().isEmpty());
        deleteBranchAction.disableProperty().setValue(deleteBranchAction.getItems().isEmpty());
        markBranchAction.disableProperty().setValue(mergeWithAction.getItems().isEmpty());
    }

    private void markCommits(String branchName) {
        Branch b = (branchName.endsWith(" - REMOTE") ?
                new Branch(branchName.split(" - REMOTE")[0], true, false) :
                new Branch(branchName, false, Branch.checkRTB(branchName)));
        List<Commit.commitComps> commits = Branch.getAllCommits(b);
        for (ICell cell : treeContent.getModel().getAllCells()) {
            CommitNode commitNode = (CommitNode) cell;
            commitNode.unMark();
            for (Commit.commitComps c : commits) {
                if (c.getSha1().equals(commitNode.getSha1())) {
                    commitNode.mark();
                    break;
                }
            }
        }

    }

    private void askToCreateRTB(String branchName) {
        if (Branch.doesLocalBranchExists(branchName)) {
            showExceptionDialog(new UnsupportedOperationException("A branch named " + branchName + " already exists locally\n" +
                    "Operation terminated"));
            return;
        }
        if (askForYesNo("You are trying to Checkout to a Remote Branch\n" +
                "Would you like to create a new Remote Tracking Branch?")) {
            createNewRTB(branchName);
            if (askForYesNo(QuestionConsts.ASK_CHECKOUT)) {
                DiffHandler diff;
                diff = manager.getDiff();
                if (!appManager.isCleanState(diff)) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setHeaderText("This repository is not in \"Clean State\"\nCheckout was not preformed");
                    setTheme(alert);
                    alert.show();
                } else {
                    try {
                        manager.makeCheckOut(branchName);
                    } catch (FileSystemException ex) {
                        showExceptionDialog(ex);
                        return;
                    }
                }
            }
            updateUI();
        }
    }

    private void createNewRTB(String branchName) {
        Path remotePath = manager.getRemotePath();
        String RRSha1 = unzipFileToString(findFileInFolderByName(PathConsts.REMOTE_BRANCHES_FOLDER(), branchName));
        manager.createNewRTB(branchName, remotePath, RRSha1);
    }


    public void cloneRepository(Path localPath, Path clonePath) {
        try {
            manager.cloneRepository(localPath, clonePath);
            manager.makeCheckOut(Branch.getActiveBranch());
            updateUI();
        } catch (Exception ex) {
            showExceptionDialog(ex);
            return;
        }
    }

    @FXML
    private void showCloneDialog(ActionEvent actionEvent) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/dialogs/cloneDialog.fxml"));
        Parent parent = fxmlLoader.load();
        CloneController controller = fxmlLoader.getController();
        Scene scene = new Scene(parent);
        Stage stage = new Stage();
        controller.setPrimaryStage(stage);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(scene);
        setTheme(scene);
        stage.showAndWait();
        updateUI();
    }

    @FXML
    private void remoteFetch(ActionEvent actionEvent) {
        try {
            manager.fetchRepo();
        } catch (IOException ex) {
            showExceptionDialog(ex);
            return;
        }
        updateUI();
    }

    @FXML
    private void remotePull(ActionEvent actionEvent) {
        if (!Branch.checkRTB(Branch.getActiveBranch())) {
            showExceptionDialog(new UnsupportedOperationException("Your head branch is not a remote tracking branch.\nOperation terminated."));
            return;
        }
        if (localRepoChangedComparedToRemoteRepo()) {
            showExceptionDialog(new UnsupportedOperationException("The local branch was modified.\nTry to push your changes first.\nOperation terminated."));
            return;
        }
        DiffHandler diff = manager.getDiff();
        if (!appManager.isCleanState(diff)) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText("This repository is not in \"Clean State\"\nOperation terminated");
            setTheme(alert);
            alert.show();
        } else {
            try {
                manager.remotePull();
                mergeBranch(Branch.getActiveBranch(), true);
                updateUI();
            } catch (Exception e) {
                showExceptionDialog(e);
            }
        }
    }

    private boolean localRepoChangedComparedToRemoteRepo() {
        AncestorFinder fatherFinder = new AncestorFinder((Commit::sha1ToCommitRepresentative));
        String ourSha1 = getHeadCommitSha1();
        String theirSha1 = getOurRRBranchSha1ByName(Branch.getActiveBranch());
        String commonFatherSha1 = fatherFinder.traceAncestor(ourSha1, theirSha1);
        return !commonFatherSha1.equals(ourSha1);
    }

    @FXML
    private void remotePush(ActionEvent actionEvent) {
//        if (!Branch.checkRTB(Branch.getActiveBranch())) {
////            showExceptionDialog(new UnsupportedOperationException("Your head branch is not a remote tracking branch.\nOperation terminated."));
////            return;
        if (wasRemoteBranchModified(Branch.getActiveBranch())) {
            showExceptionDialog(new UnsupportedOperationException("The remote branch was modified since the last sync.\nOperation terminated."));
            return;
        }
        try {
            manager.remotePush();
            if (!Branch.checkRTB(Branch.getActiveBranch()))
                pushNewLocalBranch();
            updateUI();
        } catch (IOException e) {
            showExceptionDialog(e);
            return;
        }
    }

    //accept PR
    private void pushNewLocalBranch() {
        String activeBranchName = Branch.getActiveBranch();
        String activeBranchSha1 = Branch.getCommitSha1ByBranchName(activeBranchName);
        createNewRBLocally(activeBranchName, activeBranchSha1);
        createNewRBinRemote(activeBranchName, activeBranchSha1);
        Branch.changeBranchPointedCommit(activeBranchName, activeBranchSha1, true);
        makeBranchRTB(activeBranchName, activeBranchSha1);
    }

    private void makeBranchRTB(String branchName, String branchSha1) {
        try {
            manager.makeBranchRTB(branchName, branchSha1);
        } catch (IOException e) {
            showExceptionDialog(e);
        }
    }

    private void createNewRBinRemote(String branchName, String branchSha1) {
        try {
            manager.createNewRBinRemote(branchName, branchSha1);
        } catch (IOException e) {
            showExceptionDialog(e);
        }
    }

    private void createNewRBLocally(String branchName, String branchSha1) {
        try {
            manager.createNewRBLocally(branchName, branchSha1);
        } catch (IOException e) {
            showExceptionDialog(e);
        }
    }

    private boolean wasRemoteBranchModified(String branchName) {
        File f = findFileInFolderByName(PathConsts.BRANCHES_FOLDER(getRemotePath()), branchName);
        if (f == null || !f.exists())
            return false;
        String ourRemoteBranchCommit = appManager.getOurRRBranchSha1ByName(branchName);
        String remoteBranchCommit = getRRBranchOriginalSha1ByName(getRemotePath(), branchName);
        return !(ourRemoteBranchCommit.equals(remoteBranchCommit));
    }

    @FXML
    private void showAbout(ActionEvent actionEvent) {
        ALERT("My Amazing Git - Version 1.0\n\nYuval Niezni\nynui12@gmail.com", "About");
    }
}
