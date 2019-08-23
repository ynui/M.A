import appManager.appManager;
import components.main.MAGitController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.net.URL;

public class FxMain extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        FXMLLoader loader = new FXMLLoader();

        // load main fxml
        URL mainFXML = getClass().getResource("components/main/MAGit.fxml");
        loader.setLocation(mainFXML);
        BorderPane root = loader.load();

        // wire up controller
        MAGitController controller = loader.getController();
        appManager manager = new appManager();
        controller.setPrimaryStage(primaryStage);
        controller.setAppManager(manager);

        // set stage
        primaryStage.setTitle("My Amazing Git!");
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();


    }

    public static void main(String[] args) {
        launch(args);
    }}
