package commitTree.layout;

import com.fxgraph.graph.Graph;
import com.fxgraph.graph.ICell;
import com.fxgraph.layout.Layout;
import commitTree.node.CommitNode;

import java.util.List;

public class CommitTreeLayout implements Layout {

    @Override
    public void execute(Graph graph) {
        final List<ICell> cells = graph.getModel().getAllCells();
        int startX = 10;
        int startY = 10;
        for (ICell cell : cells) {
            CommitNode c = (CommitNode) cell;
                graph.getGraphic(c).relocate(startX + (c.getPos() * 25), startY);
            startY += 50;
        }
    }
}
