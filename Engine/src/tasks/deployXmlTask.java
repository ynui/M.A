package tasks;

import appManager.*;
import components.main.MAGitController;
import javafx.concurrent.Task;

import static appManager.appManager.depolyXmlBranches;

public class deployXmlTask extends Task<Boolean> {
    private XmlRepo repo;
    private appManager manager;

    public deployXmlTask(XmlRepo xmlRepo, appManager manager) {
        this.repo = xmlRepo;
        this.manager = manager;
    }

    @Override
    protected Boolean call() throws Exception {
        Repository.createEmptyRepository(repo.getRepository().getLocation(), manager);
        depolyXmlBranches(repo);
        manager.makeCheckOut(repo.getBranches().getHead());
        return true;
    }
}
