package appManager;

import XMLgenerated.MagitSingleBranch;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static appManager.Commit.getHeadCommitSha1;
import static appManager.ZipHandler.*;
import static appManager.appManager.*;

public class Branch implements Comparable<Branch> {


    public static boolean doesLocalBranchExists(String name) {
        List<Branch> localBranches = getLocalBranches();
        for (Branch b : localBranches) {
            if (b.getName().equals(name))
                return true;
        }
        return false;
    }

    private static List<Branch> getLocalBranches() {
        List<Branch> out = new LinkedList<>();
        List<File> files = getFiles(Paths.get(PathConsts.BRANCHES_FOLDER()));
        for (File f : files) {
            if (f.isDirectory()) {
                continue;
            }
            if (!f.getName().equals("HEAD")) {
                if (checkRTB(f.getName()))
                    out.add(new Branch(f.getName(), false, true));
                else
                    out.add(new Branch(f.getName(), false, false));
            }
        }
        return setBranchListPositioning(out);
    }

    public String getName() {
        return name;
    }

    public String getCommitNote() {
        return commitNote;
    }

    private String name;

    public String getCommitSha1() {
        return commitSha1;
    }

    private String commitSha1;
    private String commitNote;

    public boolean isRemote() {
        return isRemote;
    }

    private boolean isRemote;

    private boolean isRTB;

    public boolean isActive() {
        return isActive;
    }

    private boolean isActive;
    private static String activeSignal = "Active --> ";


    public Branch(String name, boolean isRemote, boolean isRTB) {
        if (name.equals("HEAD")) {
            this.name = name;
            this.commitSha1 = "";
            this.commitNote = "";
            this.isActive = true;
            this.isRemote = isRemote;
            this.isRTB = isRTB;
        } else {
            String sha1 = isRemote ? unzipFileToString(findFileInFolderByName(PathConsts.REMOTE_BRANCHES_FOLDER(), name)) :
                    unzipFileToString(findFileInFolderByName(PathConsts.BRANCHES_FOLDER(), name));
            if (sha1.contains("\n"))
                sha1 = sha1.substring(0, sha1.indexOf("\n"));
            List<String> commitComponents = getCommitComponents(sha1);
            this.name = name;
            this.commitSha1 = sha1;
            this.commitNote = Commit.getCommitNote(commitComponents);
            this.isActive = isBranchActive(name);
            this.isRemote = isRemote;
        }
    }


    public static void createEmptyBranch(String path) {
        File head = createTextRepresentation("master", "HEAD");
        File masterBranch = appManager.createTextRepresentation("", "master");
        try {
            zipFile(head, "HEAD", path);
            zipFile(masterBranch, "master", path);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            head.delete();
            masterBranch.delete();
        }
    }

    public static void updateHeadBrunch(String sha1) {
        String prevBranch = unzipFolderToCompList("HEAD", PathConsts.BRANCHES_FOLDER()).get(0);
        File currBranch = findFileInFolderByName(PathConsts.BRANCHES_FOLDER(), prevBranch);
        changeBranchPointedCommit(currBranch.getName(), sha1, false);
    }

    public static void changeBranchPointedCommit(String name, String sha1, boolean isRemote) {
        String toWrite = sha1;
        String target = isRemote ?
                PathConsts.REMOTE_BRANCHES_FOLDER() :
                PathConsts.BRANCHES_FOLDER();
        if (Branch.checkRTB(name) && !isRemote)
            toWrite += "\n" + getRemotePath();
        File textRep = createTextRepresentation(toWrite, name);
        File f = isRemote ? findFileInFolderByName(PathConsts.REMOTE_BRANCHES_FOLDER(), name) :
                findFileInFolderByName(PathConsts.BRANCHES_FOLDER(), name);
        try {
            Files.delete(f.toPath());
        } catch (IOException e) {
            e.getMessage();
        }
        try {
            zipFile(textRep, name, target);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            textRep.delete();
        }
    }


    public static List<Branch> allBranchesToList() {
        boolean RRExist = false;
        String RRName = "";
        List<Branch> out = new LinkedList<>();
        List<File> files = getFiles(Paths.get(PathConsts.BRANCHES_FOLDER()));
        for (File f : files) {
            if (f.isDirectory()) {
                RRExist = true;
                RRName = f.getName();
                continue;
            } else {
                if (!f.getName().equals("HEAD")) {
                    if (checkRTB(f.getName()))
                        out.add(new Branch(f.getName(), false, true));
                    else
                        out.add(new Branch(f.getName(), false, false));
                }
            }
        }
        if (RRExist) {
            files = getFiles(Paths.get(PathConsts.BRANCHES_FOLDER() + "/" + RRName));
            for (File f : files) {
                if (!f.getName().equals("HEAD"))
                    out.add(new Branch(f.getName(), true, false));
            }
        }
        return setBranchListPositioning(out);
    }

    public static boolean checkRTB(String name) {
        return (unzipFolderToCompList(name, PathConsts.BRANCHES_FOLDER()).size() > 1);
    }

    public boolean checkRTB() {
        return (unzipFolderToCompList(name, PathConsts.BRANCHES_FOLDER()).size() > 1);
    }

    private static List<Branch> setBranchListPositioning(List<Branch> out) {
        //not working in debug??
        Collections.sort(out);
        List<Branch> res = new LinkedList<Branch>();
        for (Branch b : out) {
            if (b.getName().equals("master"))
                res.add(0, b);
            else
                res.add(b);
        }
        return res;
    }


    public static void createNewBranch(String name) {
        String currCommitSha1 = getHeadCommitSha1();
        File newBranch = appManager.createTextRepresentation(currCommitSha1, name);
        try {
            zipFile(newBranch, name, PathConsts.BRANCHES_FOLDER());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            newBranch.delete();
        }
    }

    public static void createNewRTB(String name, Path remotePath, String RRSha1) {
        File newBranch = appManager.createTextRepresentation(RRSha1 + "\n" + remotePath, name);
        try {
            zipFile(newBranch, name, PathConsts.BRANCHES_FOLDER());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            newBranch.delete();
        }
    }


    public static boolean doesBranchExists(List<Branch> allBranches, String name) {
        for (Branch b : allBranches) {
            if (b.name.equals(name))
                return true;
        }
        return false;
    }

    public static void deleteBranch(String name) throws Exception {
        List<Branch> allBranches = allBranchesToList();
        for (Branch b : allBranches) {
            if (b.name.equals(name)) {
                if (b.isActive)
                    throw new UnsupportedOperationException("This is the active branch. Cannot delete it");
                else {
                    deleteFileFromFolder(PathConsts.BRANCHES_FOLDER(), name);
                    return;
                }
            }
        }
        throw new UnsupportedOperationException("A branch named " + name + "does not exist");

    }

    public static void initXmlBranches(XmlRepo repo, String head) throws IOException {
        String sha1;
        String target;
        String name;
        String remotePath = String.valueOf(getRemotePath());
        List<MagitSingleBranch> branchesList = repo.getAllBranches().getMagitSingleBranch();
        changeActiveBranch(head);
        for (MagitSingleBranch b : branchesList) {
            if (b.isIsRemote()) {
                sha1 = Commit.getXmlCommitSha1(repo, b.getPointedCommit().getId());
                target = PathConsts.REMOTE_BRANCHES_FOLDER();
                name = getActualRemoteBranchName(b.getName());
            } else if (b.isTracking()) {
                sha1 = Commit.getXmlCommitSha1(repo, b.getPointedCommit().getId()).concat("\n" + remotePath);
                target = PathConsts.BRANCHES_FOLDER();
                name = b.getName();
            } else {
                sha1 = Commit.getXmlCommitSha1(repo, b.getPointedCommit().getId());
                target = PathConsts.BRANCHES_FOLDER();
                name = b.getName();
            }
            File newBranch = appManager.createTextRepresentation(sha1, name);
            try {
                zipFile(newBranch, name, target);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                newBranch.delete();
            }
        }
    }

    private static String getActualRemoteBranchName(String name) {
        return name.split("\\\\")[1];
    }

    @Override
    public String toString() {
        String out = "";
        if (isActive)
            out += (activeSignal);
        out += (name + "\n");
        out += ("Commit sha1: " + commitSha1 + "\n");
        out += ("Commit note: " + commitNote + "\n");
        return out;
    }

    public static boolean isBranchActive(String name) {
        String headBranchName = unzipFolderToCompList("HEAD", PathConsts.BRANCHES_FOLDER()).get(0);
        return name.equals(headBranchName);
    }

    public static List<String> getCommitComponents(String commitSha1) {
        return unzipFolderToCompList(commitSha1, PathConsts.OBJECTS_FOLDER());
    }

    public static String getCommitSha1ByBranchName(String name) {
        boolean isRemote = false;
        String relevantSha1 = "";
        File f = findFileInFolderByName(PathConsts.BRANCHES_FOLDER(), name);
        if (f == null || !f.exists()) {
            f = findFileInFolderByName(PathConsts.REMOTE_BRANCHES_FOLDER(), name);
            isRemote = true;
            if (f == null || !f.exists())
                throw new UnsupportedOperationException("There is no branch named " + name);
        }
        if (isRemote)
            relevantSha1 = unzipFolderToCompList(name, PathConsts.REMOTE_BRANCHES_FOLDER()).get(0);
        else
            relevantSha1 = unzipFolderToCompList(name, PathConsts.BRANCHES_FOLDER()).get(0);
        return relevantSha1;
    }

    public static void changeActiveBranch(String newActiveBranchName) {
        File f = findFileInFolderByName(PathConsts.BRANCHES_FOLDER(), "HEAD");
        try {
            Files.delete(f.toPath());
        } catch (IOException e) {
            e.getMessage();
        }
        f = createTextRepresentation(newActiveBranchName, "HEAD");
        try {
            zipFile(f, "HEAD", PathConsts.BRANCHES_FOLDER());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            f.delete();
        }
    }

    public static List<Commit.commitComps> getAllCommits(Branch b) {
        if (b.getName().equals("HEAD")) return new LinkedList<>();
        String headCommitSha1 = b.getCommitSha1();
        return branchHistoryToListByCommitSha1(headCommitSha1);
    }

    @Override
    public int compareTo(Branch o) {
        List<Commit.commitComps> ourCommits = branchHistoryToListByCommitSha1(this.commitSha1);
        List<Commit.commitComps> theirCommits = branchHistoryToListByCommitSha1(o.getCommitSha1());
        if (ourCommits.size() == 0 || theirCommits.size() == 0) {
            if (ourCommits.size() == 0) return -1;
            if (theirCommits.size() == 0) return 1;
            else return 0;
        }
        return ourCommits.get(0).compareTo(theirCommits.get(0));
    }

    public static String getActiveBranch() {
        return unzipFolderToCompList("HEAD", PathConsts.BRANCHES_FOLDER()).get(0);
    }
}
