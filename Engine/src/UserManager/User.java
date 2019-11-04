package UserManager;

import appManager.Repository;
import appManager.appManager;
import appManager.PullRequest;

import java.util.LinkedList;
import java.util.List;

public class User implements Comparable<User>{

    private String name;
    private List<Repository> repositories;
    private List<String> repositoriesNames;
    private List<PullRequest> pullRequests;
    private List<String> messagesUnread;
    private List<String> messagesRead;
    private appManager appManager;

    public User(String name) {
        this.name = name;
        this.repositories = new LinkedList<>();
        this.repositoriesNames = new LinkedList<>();
        this.pullRequests = new LinkedList<>();
        this.messagesUnread = new LinkedList<>();
        this.messagesRead = new LinkedList<>();
        this.appManager = new appManager();
        appManager.setUsername(name);
    }

    public List<String> getMessagesUnread() {
        return messagesUnread;
    }

    public List<String> getMessagesRead() {
        return messagesRead;
    }

    public List<PullRequest> getPullRequests() {
        return pullRequests;
    }

    public appManager getAppManager() {
        return appManager;
    }

    public String getName() {
        return name;
    }

    public List<Repository> getRepositories() {
        return repositories;
    }

    public List<String> getRepositoriesNames() {
        return repositoriesNames;
    }

    @Override
    public int compareTo(User o) {
        return this.getName().compareTo(o.getName());
    }
}
