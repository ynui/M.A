package appManager;

import XMLgenerated.MagitSingleBranch;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static appManager.Commit.getHeadCommitSha1;
import static appManager.ZipHandler.*;
import static appManager.appManager.*;

public class Branch implements Comparable<Branch> {
    public static List<Branch> getBranchesInTreeOrder() {
        List<Branch> allBranches = allBranchesToList();
        for (Branch b : allBranches) {
            if (b.getName().equals("master")) {
                allBranches.remove(b);
                break;
            }
        }
        List<File> files = getFiles(Paths.get(PathConsts.BRANCHES_FOLDER()));
        if (files.stream().filter(o -> o.getName().equals("master")).findFirst().isPresent())
            allBranches.add(0, new Branch("master"));
        Collections.sort(allBranches);
        return allBranches;
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
    private boolean isActive;
    private static String activeSignal = "Active --> ";


    public Branch(String name) {
        String sha1 = unzipFileToString(findFileInFolderByName(PathConsts.BRANCHES_FOLDER(), name));
        List<String> commitComponents = getCommitComponents(sha1);
        this.name = name;
        this.commitSha1 = sha1;
        this.commitNote = Commit.getCommitNote(commitComponents);
        this.isActive = isBranchActive(name);
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
        setNewHeadBranch(currBranch.getName(), sha1);
    }

    private static void setNewHeadBranch(String name, String sha1) {
        File textRep = createTextRepresentation(sha1, name);
        File f = findFileInFolderByName(PathConsts.BRANCHES_FOLDER(), name);
        try {
            Files.delete(f.toPath());
        } catch (IOException e) {
            e.getMessage();
        }
        try {
            zipFile(textRep, name, PathConsts.BRANCHES_FOLDER());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            textRep.delete();
        }
    }


    public static List<Branch> allBranchesToList() {
        List<Branch> out = new LinkedList<>();
        List<File> files = getFiles(Paths.get(PathConsts.BRANCHES_FOLDER()));
        for (File f : files) {
            if (!f.getName().equals("HEAD"))
                out.add(new Branch(f.getName()));
        }
        return setBranchListPositioning(out);
    }

    private static List<Branch> setBranchListPositioning(List<Branch> out) {
        //not working in debug??
        for(Branch b : out){
            if(b.getName().equals("master")){
                Branch temp = b;
                out.remove(b);
                out.add(0,temp);
                break;
            }
        }
        return  out;
    }


    public static void createNewBranch(String name) {
        String currCommitSha1 = getHeadCommitSha1();
        List<Branch> allBranches = allBranchesToList();
//        if(doesBranchExists(allBranches, name);
        File newBranch = appManager.createTextRepresentation(currCommitSha1, name);
        try {
            zipFile(newBranch, name, PathConsts.BRANCHES_FOLDER());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            newBranch.delete();
        }
    }

    private static boolean doesBranchExists(List<Branch> allBranches, String name) {
        for (Branch b : allBranches) {
//            if (b.name.equals(name))
//                throw new UnsupportedOperationException("This branch name is taken");
//            if (name.contains(" "))
//                throw new UnsupportedOperationException("Branch name cannot contain space");
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
        List<MagitSingleBranch> branchesList = repo.getBranches().getMagitSingleBranch();
        changeActiveBranch(head);
        for (MagitSingleBranch b : branchesList) {
            sha1 = Commit.getXmlCommitSha1(repo, b.getPointedCommit().getId());
            File newBranch = appManager.createTextRepresentation(sha1, b.getName());
            try {
                zipFile(newBranch, b.getName(), PathConsts.BRANCHES_FOLDER());
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                newBranch.delete();
            }
        }
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
        File f = findFileInFolderByName(PathConsts.BRANCHES_FOLDER(), name);
        if (!f.exists())
            throw new UnsupportedOperationException("There is no branch named " + name);
        String relevantSha1 = unzipFolderToCompList(name, PathConsts.BRANCHES_FOLDER()).get(0);
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
        }
    }

    public static List<Commit.commitComps> getAllCommits(String branchName) {
        String headCommitSha1 = getCommitSha1ByBranchName(branchName);
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
        Collections.reverse(ourCommits);
        Collections.reverse(theirCommits);
        return ourCommits.get(0).compareTo(theirCommits.get(0));
    }

    public static String getActiveBranch(){
        return unzipFolderToCompList("HEAD", PathConsts.BRANCHES_FOLDER()).get(0);
    }
}
