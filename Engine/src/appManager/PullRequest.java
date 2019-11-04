package appManager;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PullRequest {
    private static int counter = 1;

    private String repoName;
    private String creator;
    private String baseBranchName;
    private String targetBranchName;
    private String message;
    private String rejectMessage;
    private state requestStatus;
    private String dateCreated;
    private int id;

    public enum state {OPEN, ACCEPTED, REJECTED}

    ;

    public PullRequest(String repoName, String creator, String baseBranchName, String targetBranchName, String message) {
        this.repoName = repoName;
        this.creator = creator;
        this.baseBranchName = baseBranchName;
        this.targetBranchName = targetBranchName;
        this.message = message;
        this.requestStatus = state.OPEN;
        this.dateCreated = setDate();
        this.id = counter++;
    }

    public String getRepoName() {
        return repoName;
    }

    private String setDate() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy-HH:mm:ss:SSS"));
    }

    public String getRejectMessage() {
        return rejectMessage;
    }

    public void setRejectMessage(String rejectMessage) {
        this.rejectMessage = rejectMessage;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public String getCreator() {
        return creator;
    }

    public state getRequestStatus() {
        return requestStatus;
    }

    public void setRequestStatus(state requestStatus) {
        this.requestStatus = requestStatus;
    }

    public int getId() {
        return id;
    }

    public String getBaseBranchName() {
        return baseBranchName;
    }

    public void setBaseBranchName(String baseBranchName) {
        this.baseBranchName = baseBranchName;
    }

    public String getTargetBranchName() {
        return targetBranchName;
    }

    public void setTargetBranchName(String targetBranchName) {
        this.targetBranchName = targetBranchName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "On Repository: " + repoName + "\n" +
                "Date: " + dateCreated + "\n" +
                "Creator: " + creator + "\n" +
                "Message: " + message + "\n" +
                "Base Branch: " + targetBranchName + "\n" +
                "Target Branch: " + baseBranchName;
    }
}
