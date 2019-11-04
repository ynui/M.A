package servlets;

import UserManager.User;
import UserManager.UserManager;
import appManager.*;
import com.google.gson.Gson;
import constants.Constants;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import utils.ServletUtils;
import utils.SessionUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.FileSystemException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class DataServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        User user = SessionUtils.getUser(request);
//        appManager manager = ServletUtils.getAppManager(getServletContext());
        appManager manager = user.getAppManager();
        switch (action) {
            case "getUsername":
                getUsername(request, response, manager);
                break;
            case "getUserList":
                getUserList(request, response, manager);
                break;
            case "getUserRepos":
                getUserRepos(request, response, manager);
                break;
            case "getUserReposByName":
                getUserReposByName(request, response, manager);
                break;
            case "getBranches":
                getBranches(request, response, manager);
                break;
            case "getHeadBranchCommits":
                getHeadBranchCommits(request, response, manager);
                break;
            case "getCommitRep":
                getCommitRep(request, response, manager);
                break;
            case "getFolderRep":
                getFolderRep(request, response, manager);
                break;
            case "getFileData":
                getFileData(request, response, manager);
                break;
            case "getFileDataWC":
                getFileDataWC(request, response, manager);
                break;
            case "getFilesFromWC":
                getFilesFromWC(request, response, manager);
                break;
            case "getWCFolderRep":
                getWCFolderRep(request, response, manager);
                break;
            case "editFile":
                editFile(request, response, manager);
                break;
            case "deleteFile":
                deleteFile(request, response, manager);
                break;
            case "createFile":
                createFile(request, response, manager);
                break;
            case "getWcStatus":
                getWcStatus(request, response, manager);
                break;
            case "makeCommit":
                makeCommit(request, response, manager);
                break;
            case "makeCheckout":
                makeCheckout(request, response, manager);
                break;
            case "deleteBranch":
                deleteBranch(request, response, manager);
                break;
            case "createNewBranch":
                createNewBranch(request, response, manager);
                break;
            case "pushBranch":
                pushBranch(request, response, manager);
                break;
            case "pullBranch":
                pullBranch(request, response, manager);
                break;
            case "forkRepo":
                forkRepo(request, response, manager);
                break;
            case "createPR":
                createPR(request, response, manager);
                break;
            case "getRemoteRepo":
                getRemoteRepo(request, response, manager);
                break;
            case "getRemoteRepoBranches":
                getRemoteRepoBranches(request, response, manager);
                break;
            case "getPullRequests":
                getPullRequests(request, response, manager);
                break;
            case "acceptPR":
                acceptPR(request, response, manager);
                break;
            case "rejectPR":
                rejectPR(request, response, manager);
                break;
            case "getPrCommits":
                getPrCommits(request, response, manager);
                break;
            case "showCommitDiff":
                showCommitDiff(request, response, manager);
                break;
            case "showNotifications":
                showNotifications(request, response, manager);
                break;
            case "clearNotifications":
                clearNotifications(request, response, manager);
                break;
        }
    }

    private void clearNotifications(HttpServletRequest request, HttpServletResponse response, appManager manager) {
        User user = SessionUtils.getUser(request);
        if (user != null) {
            user.getMessagesRead().addAll(user.getMessagesUnread());
            user.getMessagesUnread().clear();
        }
    }

    private void showNotifications(HttpServletRequest request, HttpServletResponse response, appManager manager) {
        User user = SessionUtils.getUser(request);
        List<String> outList = new LinkedList<>(user.getMessagesUnread());
        sendResponse(response, outList);
    }

    private void showCommitDiff(HttpServletRequest request, HttpServletResponse response, appManager manager) {
        User user = SessionUtils.getUser(request);
        String repoName = request.getParameter("repository");
        String commitSha1 = request.getParameter("commitSha1");
        manager.workingPath = Paths.get(Constants.HUB_FOLDER + user.getName() + "/" + repoName);
        DiffHandler diff = new DiffHandler();
        diff.createModifiedFilesListsBetween2FolderSha1(manager.getCommitRootFolderSha1M(manager.getPrevCommitsBySha1M(commitSha1).get(0)), manager.getCommitRootFolderSha1M(commitSha1));
        List<Object> outLst = new LinkedList<>();
        outLst.add(diff.getCreated());
        outLst.add(diff.getChanged());
        outLst.add(diff.getDeleted());
        sendResponse(response, outLst);
    }

    private void getPrCommits(HttpServletRequest request, HttpServletResponse response, appManager manager) {
        User user = SessionUtils.getUser(request);
        String repoName = request.getParameter("repository");
        manager.workingPath = Paths.get(Constants.HUB_FOLDER + user.getName() + "/" + repoName);
        String baseBranch = request.getParameter("baseBranch");
        String targetBranch = request.getParameter("targetBranch");
        List<Commit.commitComps> commits = manager.getPrCommits(baseBranch, targetBranch);
        List<Object> outLst = new LinkedList<>();
        outLst.addAll(commits);
        sendResponse(response, outLst);
    }

    private void rejectPR(HttpServletRequest request, HttpServletResponse response, appManager manager) {
        User user = SessionUtils.getUser(request);
        String repoName = request.getParameter("repository");
        String reason = request.getParameter("reason");
        String id = request.getParameter("id");
        manager.workingPath = Paths.get(Constants.HUB_FOLDER + user.getName() + "/" + repoName);
        List<Object> outLst = new LinkedList<>();
        try {
            PullRequest PR = manager.rejectPR(user, id, repoName, reason);
            User creatorUser = UserManager.getUser(PR.getCreator());
            creatorUser.getMessagesUnread().add(
                    "Pull request rejected!\n" + PR + "\n" +
                            user.getName() + "'s excuse:\n" + PR.getRejectMessage());
            outLst.add(true);
        } catch (Exception e) {
            e.printStackTrace();
            outLst.add(e.getMessage());
        } finally {
            sendResponse(response, outLst);
        }
    }

    private void acceptPR(HttpServletRequest request, HttpServletResponse response, appManager manager) {
        User user = SessionUtils.getUser(request);
        String repoName = request.getParameter("repository");
        String baseBranch = request.getParameter("baseBranch");
        String targetBranch = request.getParameter("targetBranch");
        String id = request.getParameter("id");
        manager.workingPath = Paths.get(Constants.HUB_FOLDER + user.getName() + "/" + repoName);
        List<Object> outLst = new LinkedList<>();
        try {
            String temp = manager.getHeadBranchName();
            if (!temp.equals(targetBranch))
                manager.makeCheckOut(targetBranch);
            manager.mergeBranch(baseBranch, false);
            if (!temp.equals(targetBranch))
                manager.makeCheckOut(temp);
            PullRequest PR = manager.changeStatusPR(user, id, repoName, "ACCEPTED");
            User creatorUser = UserManager.getUser(PR.getCreator());
            creatorUser.getMessagesUnread().add(
                    "Pull request accepted!\n" + PR + "\nOhh Yeah!");
            outLst.add(true);
        } catch (Exception e) {
            e.printStackTrace();
            outLst.add(e.getMessage());
        } finally {
            sendResponse(response, outLst);
        }
    }

    private void getPullRequests(HttpServletRequest request, HttpServletResponse response, appManager manager) {
        User user = SessionUtils.getUser(request);
        String repoName = request.getParameter("repository");
        List<PullRequest> PRs = user.getPullRequests();
        List<Object> outLst = new LinkedList<>();
        for (PullRequest PR : PRs) {
            if (PR.getRepoName().equals(repoName))
                outLst.add(PR);
        }
        sendResponse(response, outLst);
    }

    private void getRemoteRepoBranches(HttpServletRequest request, HttpServletResponse response, appManager manager) {
        String repoOwner = request.getParameter("owner");
        String repoName = request.getParameter("repository");
        User owner = UserManager.getUser(repoOwner);
        owner.getAppManager().workingPath = Paths.get(Constants.HUB_FOLDER + owner.getName() + "/" + repoName);
        List<Object> outLst = new LinkedList<>();
        List<Branch> branches = manager.getLocalBranchesM();
        for (Branch b : branches)
            outLst.add(b.getName());
        sendResponse(response, outLst);
    }

    private void getRemoteRepo(HttpServletRequest request, HttpServletResponse response, appManager manager) {
        User user = SessionUtils.getUser(request);
        String repoName = request.getParameter("repository");
        manager.workingPath = Paths.get(Constants.HUB_FOLDER + user.getName() + "/" + repoName);
        List<Object> outLst = new LinkedList<>();
        String remotePath = String.valueOf(manager.getRemotePath());
        if (remotePath == "null") {
            outLst.add(false);
        } else {
            List<String> remoteName = Arrays.asList(remotePath.split("\\\\"));
            outLst.add(remoteName.get(2));
            outLst.add(remoteName.get(3));
        }
        sendResponse(response, outLst);
    }

    private void createPR(HttpServletRequest request, HttpServletResponse response, appManager manager) {
        User user = SessionUtils.getUser(request);
        String repoOwner = request.getParameter("remoteRepoOwner");
        String repoName = request.getParameter("repository");
        String baseName = request.getParameter("baseName");
        String targetName = request.getParameter("targetName");
        String note = request.getParameter("note");
        manager.workingPath = Paths.get(Constants.HUB_FOLDER + user.getName() + "/" + repoName);
        User owner = UserManager.getUser(repoOwner);
        PullRequest newPR = new PullRequest(repoName, user.getName(), baseName, targetName, note);
        owner.getPullRequests().add(newPR);
        owner.getMessagesUnread().add(
                "New pull request!\n" + newPR);
    }

    private void forkRepo(HttpServletRequest request, HttpServletResponse response, appManager manager) {
        User user = SessionUtils.getUser(request);
        String toFork = request.getParameter("repoToFork");
        String owner = request.getParameter("owner");
        User ownerUser = UserManager.getUser(owner);
        Path clonePath = Paths.get(Constants.HUB_FOLDER + owner + "/" + toFork);
        manager.workingPath = Paths.get(Constants.HUB_FOLDER + user.getName() + "/" + toFork);
        List<Object> outLst = new LinkedList<>();
        try {
            manager.cloneRepository(manager.workingPath, clonePath);
            manager.makeCheckOut(manager.getActiveBranchM());
            user.getRepositories().add(new Repository(manager.workingPath, toFork));
            user.getRepositoriesNames().add(toFork);
            ownerUser.getMessagesUnread().add(user.getName() + " forked your repository " + toFork);
            outLst.add(true);
        } catch (Exception e) {
            outLst.add(e.getMessage());
        } finally {
            sendResponse(response, outLst);
        }
    }

    private void pullBranch(HttpServletRequest request, HttpServletResponse response, appManager manager) {
        User user = SessionUtils.getUser(request);
        String repoName = request.getParameter("repository");
        String branchName = manager.getHeadBranchName();
        manager.workingPath = Paths.get(Constants.HUB_FOLDER + user.getName() + "/" + repoName);
        try {
            List<String> relevantSha1s = new LinkedList<>();
            String RRSha1 = manager.getRRBranchOriginalSha1ByName(manager.getRemotePath(), branchName);
            manager.getRelevantCommitSha1s(RRSha1, relevantSha1s, true);
            manager.copyRelevantSha1sPull(relevantSha1s, manager.getRemotePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<Object> outLst = new LinkedList<>();

        if (!manager.checkRTBM(branchName, Constants.HUB_FOLDER + user.getName() + "/" + repoName + "/.magit/branches")) {
            outLst.add("Your head branch is not a remote tracking branch.\nOperation terminated.");
            sendResponse(response, outLst);
            return;
        } else if (manager.localRepoChangedComparedToRemoteRepo(branchName)) {
            outLst.add("The local branch was modified.\nTry to push your changes first.\nOperation terminated.");
            sendResponse(response, outLst);
            return;
        }
        DiffHandler diff = manager.getDiff();
        if (!manager.isCleanState(diff)) {
            outLst.add("This repository is not in \"Clean State\"\nOperation terminated");
            sendResponse(response, outLst);
        } else {
            try {
                manager.remotePull();
                manager.mergeBranch(branchName, true);
                outLst.add(true);
            } catch (Exception e) {
                outLst.add(e.getMessage());
            } finally {
                sendResponse(response, outLst);
            }
        }
    }

    private boolean wasRemoteBranchModified(String branchName, appManager manager) {
        File f = manager.findFileInFolderByName(PathConsts.BRANCHES_FOLDER(manager.getRemotePath()), branchName);
        if (f == null || !f.exists())
            return false;
        String ourRemoteBranchCommit = manager.getOurRRBranchSha1ByName(branchName);
        String remoteBranchCommit = manager.getRRBranchOriginalSha1ByName(manager.getRemotePath(), branchName);
        return !(ourRemoteBranchCommit.equals(remoteBranchCommit));
    }

    private void createPrBranch(appManager manager) throws IOException {
        String activeBranchName = manager.getActiveBranchM();
        String activeBranchSha1 = manager.getCommitSha1ByBranchNameM(activeBranchName);
        manager.createNewRBLocally(activeBranchName, activeBranchSha1);
        manager.createNewRBinRemote(activeBranchName, activeBranchSha1);
        manager.makeBranchRTB(activeBranchName, activeBranchSha1);
    }


    private void pushBranch(HttpServletRequest request, HttpServletResponse response, appManager manager) {
        User user = SessionUtils.getUser(request);
        String repoName = request.getParameter("repository");
        manager.workingPath = Paths.get(Constants.HUB_FOLDER + user.getName() + "/" + repoName);
        String branchName = manager.getHeadBranchName();
        List<Object> outLst = new LinkedList<>();
        if (wasRemoteBranchModified(branchName, manager)) {
            outLst.add("The remote branch was modified since the last sync.\nOperation terminated.");
        }
        try {
            manager.remotePush();
            if (!manager.checkRTBM(branchName, Constants.HUB_FOLDER + user.getName() + "/" + repoName + "/.magit/branches"))
                createPrBranch(manager);
            outLst.add(true);
        } catch (IOException e) {
            outLst.add(e.getMessage());
        } finally {
            sendResponse(response, outLst);
        }
    }

    private void createNewBranch(HttpServletRequest request, HttpServletResponse response, appManager manager) {
        User user = SessionUtils.getUser(request);
        String repoName = request.getParameter("repository");
        String branchName = request.getParameter("branchName");
        manager.workingPath = Paths.get(Constants.HUB_FOLDER + user.getName() + "/" + repoName);
        List<Object> outLst = new LinkedList<>();
        try {
            manager.createNewBranch(branchName);
            outLst.add(true);
        } catch (Exception e) {
            e.printStackTrace();
            outLst.add(e.getMessage());
        } finally {
            sendResponse(response, outLst);
        }
    }

    private void deleteBranch(HttpServletRequest request, HttpServletResponse response, appManager manager) {
        User user = SessionUtils.getUser(request);
        String repoName = request.getParameter("repository");
        String branchName = request.getParameter("branchName");
        manager.workingPath = Paths.get(Constants.HUB_FOLDER + user.getName() + "/" + repoName);
        List<Object> outLst = new LinkedList<>();
        try {
            if (manager.checkRTBM(branchName)) {
                manager.deleteRTBbranch(branchName);
                String owner = manager.getRepoOwner(manager.getRemotePath());
                User ownerUser = UserManager.getUser(owner);
                ownerUser.getMessagesUnread().add("Your Branch " + branchName + " was deleted by " + user.getName());
            } else {
                manager.deleteBranch(branchName);
            }
            outLst.add(true);
        } catch (Exception e) {
            e.printStackTrace();
            outLst.add(e.getMessage());
        } finally {
            sendResponse(response, outLst);
        }
    }

    private void makeCheckout(HttpServletRequest request, HttpServletResponse response, appManager manager) {
        User user = SessionUtils.getUser(request);
        String repoName = request.getParameter("repository");
        String branchName = request.getParameter("branchName");
        manager.workingPath = Paths.get(Constants.HUB_FOLDER + user.getName() + "/" + repoName);
        List<Object> outLst = new LinkedList<>();
        try {
            manager.makeCheckOut(branchName);
            outLst.add(true);
        } catch (FileSystemException e) {
            e.printStackTrace();
            outLst.add(e.getMessage());
        } finally {
            sendResponse(response, outLst);
        }
    }

    private void makeCommit(HttpServletRequest request, HttpServletResponse response, appManager manager) {
        User user = SessionUtils.getUser(request);
        String repoName = request.getParameter("repository");
        String note = request.getParameter("note");
        manager.workingPath = Paths.get(Constants.HUB_FOLDER + user.getName() + "/" + repoName);
        List<Object> outLst = new LinkedList<>();
        try {
            manager.createNewCommit(note);
            outLst.add(true);
        } catch (IOException e) {
            e.printStackTrace();
            outLst.add(e.getMessage());
        } finally {
            sendResponse(response, outLst);
        }
    }

    private void getWcStatus(HttpServletRequest request, HttpServletResponse response, appManager manager) {
        User user = SessionUtils.getUser(request);
        String repoName = request.getParameter("repository");
        user.getAppManager().workingPath = Paths.get(Constants.HUB_FOLDER + user.getName() + "/" + repoName);
        if (manager.workingPath == null) return;
        DiffHandler diff = manager.getDiff();
        List<Object> outLst = new LinkedList<>();
        List<String> created = diff.getCreated();
        List<String> changed = diff.getChanged();
        List<String> deleted = diff.getDeleted();
        outLst.add(created);
        outLst.add(changed);
        outLst.add(deleted);
        sendResponse(response, outLst);
    }

    private void createFile(HttpServletRequest request, HttpServletResponse response, appManager manager) {
        response.setContentType("application/json");
        User user = SessionUtils.getUser(request);
        String repoName = request.getParameter("repository");
        String content = request.getParameter("content");
        String name = request.getParameter("name");
        Path path = Paths.get(Constants.HUB_FOLDER + user.getName() + "/" + repoName);
        List<Object> outLst = new LinkedList<>();
        try {
            manager.createFileInPath(content, name, path);
            outLst.add(true);
        } catch (Exception e) {
            e.printStackTrace();
            outLst.add(e.getMessage());
        } finally {
            sendResponse(response, outLst);
        }
    }

    private void deleteFile(HttpServletRequest request, HttpServletResponse response, appManager manager) {
        response.setContentType("application/json");
        String path = request.getParameter("path");
        List<Object> outLst = new LinkedList<>();
        try {
            FileUtils.forceDelete(new File(path));
            outLst.add(true);
        } catch (IOException e) {
            e.printStackTrace();
            outLst.add(e.getMessage());
        } finally {
            sendResponse(response, outLst);
        }
    }

    private void editFile(HttpServletRequest request, HttpServletResponse response, appManager manager) {
        response.setContentType("application/json");
        String path = request.getParameter("path");
        String content = request.getParameter("content");
        List<Object> outLst = new LinkedList<>();
        try {
            manager.editFile(path, content);
            outLst.add(true);
        } catch (IOException e) {
            e.printStackTrace();
            outLst.add(e.getMessage());
        } finally {
            sendResponse(response, outLst);
        }
    }

    private void getWCFolderRep(HttpServletRequest request, HttpServletResponse response, appManager manager) {
        response.setContentType("application/json");
        User user = SessionUtils.getUser(request);
        String sha1 = request.getParameter("sha1");
        String repoName = request.getParameter("repository");
        user.getAppManager().workingPath = Paths.get(Constants.HUB_FOLDER + user.getName() + "/" + repoName);
        getCommitRepRealWC(sha1, response, manager);
    }

    private void getCommitRepRealWC(String sha1, HttpServletResponse response, appManager manager) {
        List<Object> outLst = new LinkedList<>();
        List<String> prevComps = manager.unzipFolderToCompListM(sha1, PathConsts.TEMP_FOLDER());
        List<String> prevComponents;
        for (String s : prevComps) {
            prevComponents = manager.folderRepToList(s);
            outLst.add(prevComponents.get(0));
            outLst.add(prevComponents.get(1));
            outLst.add(prevComponents.get(2));
            outLst.add(prevComponents.get(4));
        }
        sendResponse(response, outLst);
    }

    private void getFilesFromWC(HttpServletRequest request, HttpServletResponse response, appManager manager) throws IOException {
        User user = SessionUtils.getUser(request);
        String repoName = request.getParameter("repository");
        user.getAppManager().workingPath = Paths.get(Constants.HUB_FOLDER + user.getName() + "/" + repoName);
        manager.deleteAllFilesFromFolder(Paths.get(PathConsts.TEMP_FOLDER()));
        Folder f = new Folder();
        String sha1 = manager.makeFolderRep(f, user.getAppManager().workingPath, "Fake Date", Collections.emptySet(), Collections.emptyList(), Collections.emptyMap());
        String commitTxtRep = sha1 + "\n" +
                "a" + "\n" +
                "b" + "\n" +
                "c" + "\n" +
                "d" + "\n" +
                "e";
        File headFolderRep = manager.createTextRepresentation(f.toString(), sha1);
        File commitRep = manager.createTextRepresentation(commitTxtRep, DigestUtils.sha1Hex(commitTxtRep));
        manager.zipFileM(headFolderRep, sha1, PathConsts.TEMP_FOLDER());
        manager.zipFileM(commitRep, DigestUtils.sha1Hex(commitTxtRep), PathConsts.TEMP_FOLDER());
        headFolderRep.delete();
        commitRep.delete();
        List<Object> outLst = new LinkedList<>();
        List<String> prevComps = manager.unzipFolderToCompListM(sha1, PathConsts.TEMP_FOLDER());
        List<String> prevComponents;
        for (String s : prevComps) {
            prevComponents = manager.folderRepToList(s);
            outLst.add(prevComponents.get(0));
            outLst.add(prevComponents.get(1));
            outLst.add(prevComponents.get(2));
            outLst.add(prevComponents.get(4));
        }
//        appManager.deleteAllFilesFromFolder(Paths.get(PathConsts.TEMP_FOLDER()));
        sendResponse(response, outLst);
    }

    private void getFileData(HttpServletRequest request, HttpServletResponse response, appManager manager) {
        response.setContentType("application/json");
        User user = SessionUtils.getUser(request);
        String sha1 = request.getParameter("sha1");
        String repoName = request.getParameter("repository");
        user.getAppManager().workingPath = Paths.get(Constants.HUB_FOLDER + user.getName() + "/" + repoName);
        List<Object> outLst = new LinkedList<>();
        File f = manager.findFileInFolderByName(PathConsts.OBJECTS_FOLDER(), sha1);
        outLst.add(manager.unzipFileToStringM(f));
        sendResponse(response, outLst);
    }

    private void getFileDataWC(HttpServletRequest request, HttpServletResponse response, appManager manager) {
        response.setContentType("application/json");
        User user = SessionUtils.getUser(request);
        String sha1 = request.getParameter("sha1");
        String repoName = request.getParameter("repository");
        user.getAppManager().workingPath = Paths.get(Constants.HUB_FOLDER + user.getName() + "/" + repoName);
        List<Object> outLst = new LinkedList<>();
        File f = manager.findFileInFolderByName(PathConsts.TEMP_FOLDER(), sha1);
        outLst.add(manager.unzipFileToStringM(f));
        sendResponse(response, outLst);
    }

    private void getFolderRep(HttpServletRequest request, HttpServletResponse response, appManager manager) {
        response.setContentType("application/json");
        User user = SessionUtils.getUser(request);
        String sha1 = request.getParameter("sha1");
        String repoName = request.getParameter("repository");
        user.getAppManager().workingPath = Paths.get(Constants.HUB_FOLDER + user.getName() + "/" + repoName);
        getCommitRepReal(sha1, response, manager);
    }

    private void getCommitRep(HttpServletRequest request, HttpServletResponse response, appManager manager) {
        response.setContentType("application/json");
        User user = SessionUtils.getUser(request);
        String sha1 = request.getParameter("sha1");
        String repoName = request.getParameter("repository");
        user.getAppManager().workingPath = Paths.get(Constants.HUB_FOLDER + user.getName() + "/" + repoName);
        List<String> commitRep = manager.getCommitRep(sha1);
        getCommitRepReal(commitRep.get(0), response, manager);
    }

    private void getCommitRepReal(String sha1, HttpServletResponse response, appManager manager) {
        List<Object> outLst = new LinkedList<>();
        List<String> prevComps = manager.unzipFolderToCompListM(sha1, PathConsts.OBJECTS_FOLDER());
        List<String> prevComponents;
        for (String s : prevComps) {
            prevComponents = manager.folderRepToList(s);
            outLst.add(prevComponents.get(0));
            outLst.add(prevComponents.get(1));
            outLst.add(prevComponents.get(2));
        }
        sendResponse(response, outLst);
    }

    private void getHeadBranchCommits(HttpServletRequest request, HttpServletResponse response, appManager manager) {
        response.setContentType("application/json");
        User user = SessionUtils.getUser(request);
        String repoName = request.getParameter("repository");
        user.getAppManager().workingPath = Paths.get(Constants.HUB_FOLDER + user.getName() + "/" + repoName);
        List<Object> outLst = new LinkedList<>();
        List<Commit.commitComps> commits = manager.getAllCommitsHubM(manager.getActiveBranchM());
        outLst.addAll(commits);
        sendResponse(response, outLst);
    }

    private void sendResponse(HttpServletResponse response, List outLst) {
        try {
            response.setContentType("application/json");
            Gson gson = new Gson();
            PrintWriter out = response.getWriter();
            String json = gson.toJson(outLst);
            out.println(json);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getBranches(HttpServletRequest request, HttpServletResponse response, appManager manager) {
        response.setContentType("application/json");
        User user = SessionUtils.getUser(request);
        String repoName = request.getParameter("repository");
        user.getAppManager().workingPath = Paths.get(Constants.HUB_FOLDER + user.getName() + "/" + repoName);
        List<Object> outLst = new LinkedList<>();
        List<Branch> branchList = manager.getLocalBranchesM();
        for (Branch b : branchList) {
            if (b.isActive())
                outLst.add(true);
            else
                outLst.add(false);
            if (manager.checkRTBM(b.getName(), Constants.HUB_FOLDER + user.getName() + "/" + repoName + "/.magit/branches"))
                outLst.add(true);
            else
                outLst.add(false);
            outLst.add(b.getName());
            outLst.add(b.getCommitSha1());
        }
        sendResponse(response, outLst);
    }

    private void getUserReposByName(HttpServletRequest request, HttpServletResponse response, appManager manager) {
        response.setContentType("application/json");
        String username = request.getParameter("username");
        User user = UserManager.getUser(username);
        getRepos(response, manager, user);
    }

    private void getRepos(HttpServletResponse response, appManager manager, User user) {
        List<Repository> repos = user.getRepositories();
        List<Object> outLst = new LinkedList<>();
        if (repos.isEmpty()) return;
        for (Repository repo : repos) {
            user.getAppManager().workingPath = Paths.get(Constants.HUB_FOLDER + user.getName() + "/" + repo.getName());
            outLst.add(repo.getName());
            outLst.add(manager.getActiveBranchM());
            outLst.add(manager.getLocalBranchesM().size());
            List<Commit.commitComps> allCommits = manager.getAllCommits();
            Commit.commitComps lastCommit = allCommits.get(allCommits.size() - 1);
            outLst.add(lastCommit.getDate());
            outLst.add(lastCommit.getNote());
        }
        outLst.add(user.getName());
        sendResponse(response, outLst);
    }

    private void getUserRepos(HttpServletRequest request, HttpServletResponse response, appManager manager) {
        response.setContentType("application/json");
        User user = SessionUtils.getUser(request);
        getRepos(response, manager, user);
    }

    private void getUserList(HttpServletRequest request, HttpServletResponse response, appManager manager) {
        User activeUser = SessionUtils.getUser(request);
        UserManager userManager = ServletUtils.getUserManager(getServletContext());
        List<User> userList = userManager.getUsers();
        List<Object> outLst = new ArrayList<>();
        for (User user : userList) {
            if (!user.getName().equals(activeUser.getName()) && user.getRepositories().size() > 0)
                outLst.add(user.getName());
        }
        response.setContentType("application/json");
        try (PrintWriter out = response.getWriter()) {
            Gson gson = new Gson();
            String json = gson.toJson(outLst);
            out.println(json);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getUsername(HttpServletRequest request, HttpServletResponse response, appManager manager) {
        response.setContentType("application/json");
        List<Object> listForReturn = new ArrayList<>();
        try (PrintWriter out = response.getWriter()) {
            Gson gson = new Gson();
            listForReturn.add(SessionUtils.getUsername(request));
            String json = gson.toJson(listForReturn);
            out.println(json);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
