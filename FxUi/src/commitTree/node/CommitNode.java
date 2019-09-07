package commitTree.node;

import com.fxgraph.cells.AbstractCell;
import com.fxgraph.graph.Graph;
import com.fxgraph.graph.IEdge;
import javafx.beans.binding.DoubleBinding;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

public class CommitNode extends AbstractCell {

    private CommitNodeController commitNodeController;
    private String timestamp;
    private String committer;
    private String message;
    private String sha1;
    private int pos;


    public CommitNode(String timestamp, String committer, String message, String sha1) {
        this.timestamp = timestamp;
        this.committer = committer;
        this.message = message;
        this.sha1 = sha1;
    }

    public int getPos() {
        return pos;
    }

    public String getSha1() {
        return sha1;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    @Override
    public Region getGraphic(Graph graph) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            URL url = getClass().getResource("/commitTree/node/commitNode.fxml");
            fxmlLoader.setLocation(url);
            GridPane root = fxmlLoader.load();

            commitNodeController = fxmlLoader.getController();
            commitNodeController.setCommitMessage(message);
            commitNodeController.setCommitter(committer);
            commitNodeController.setCommitTimeStamp(timestamp);

            return root;
        } catch (IOException e) {
            return new Label("Error when tried to create graphic node !");
        }
    }

    @Override
    public DoubleBinding getXAnchor(Graph graph, IEdge edge) {
        final Region graphic = graph.getGraphic(this);
        return graphic.layoutXProperty().add(commitNodeController.getCircleRadius());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CommitNode that = (CommitNode) o;

        return Objects.equals(timestamp, that.timestamp);
    }

    @Override
    public int hashCode() {
        return timestamp != null ? timestamp.hashCode() : 0;
    }
}
