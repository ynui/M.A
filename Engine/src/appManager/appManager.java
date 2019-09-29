package appManager;


import common.Consts;
import components.main.MAGitController;
import org.apache.commons.codec.digest.DigestUtils;
import puk.team.course.magit.ancestor.finder.AncestorFinder;
import sun.dc.path.PathException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

import static appManager.Commit.getHeadCommitRootFolderSha1;
import static appManager.ZipHandler.*;

public class appManager {

    final String EMPTY_SHA1 = "da39a3ee5e6b4b0d3255bfef95601890afd80709";
    private Repository repository;
    private String username;
    public static Path workingPath;
    public static appManager manager;

    public appManager() {
        username = "Administrator";
        workingPath = null;
        repository = null;
        manager = this;
    }

    public static void insertFileToWc(Path path, String name, String sha1) throws IOException {
        if (Files.exists(Paths.get(path + "/" + name))) {
            manager.deleteFileFromFolder(String.valueOf(path), name);
        }
        manager.deployFile(sha1, path);
    }

    public static String stringToSha1(String text) {
        return DigestUtils.sha1Hex(text);
    }

    public static void preformFFMerge(String bossCommitSha1, String sha1ToAdd, String branchName) throws IOException {
        Commit.commitComps c = Commit.sha1ToCommitComps(bossCommitSha1);
        if (c.getPrevCommit().equals(sha1ToAdd)) {
            Branch.changeBranchPointedCommit(Branch.getActiveBranch(), bossCommitSha1, false);
            Branch.changeBranchPointedCommit(branchName, bossCommitSha1, false);
            return;
        }
        String commitTxtRep = c.getHeadFolder() + "\n" +
                c.getPrevCommit() + "\n" +
                sha1ToAdd + "\n" +
                c.getNote() + "\n" +
                c.getDate() + "\n" +
                c.getAuthor();
        String newSha1 = stringToSha1(commitTxtRep);
        File newCommitRep = createTextRepresentation(commitTxtRep, newSha1);
        zipFile(newCommitRep, newSha1, PathConsts.OBJECTS_FOLDER());
        Files.delete(newCommitRep.toPath());
        Branch.changeBranchPointedCommit(Branch.getActiveBranch(), newSha1, false);
        Branch.changeBranchPointedCommit(branchName, newSha1, false);
    }


    public void deployXml(XmlRepo repo) throws IOException {
        Repository.createEmptyRepository(repo.getRepository().getLocation(), manager);
        if (repo.getRemoteReference() != null)
            if (repo.getRemoteReference().getLocation() != null)
                generateRemoteFlag(Paths.get(repo.getRemoteReference().getLocation()));
        depolyXmlBranches(repo);
        makeCheckOut(repo.getAllBranches().getHead());
    }

    public static void depolyXmlBranches(XmlRepo repo) throws IOException {
        Branch.initXmlBranches(repo, repo.getAllBranches().getHead());
        //   Branch.initXmlRemoteBranches(repo, repo.getAllBranches().getHead());
        //  Branch.initXmlRemoteTrackingBranches(repo, repo.getAllBranches().getHead());
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void createEmptyRepository(String path) throws FileAlreadyExistsException, FileNotFoundException, PathException {
        if (!isValidPath(path))
            throw new PathException("The path '" + path + "' is not legal");
        this.repository = Repository.createEmptyRepository(path, this);
    }

    public boolean isValidPath(String path) {
        return Paths.get(path).getParent() != null;
    }

    public void createNewCommit(String note) throws IOException {
        Commit.createCommit(this, note, username);
    }


    public String commitAndSaveFiles(Folder currFolder, Path currPath, String date, Set<String> modifiedFolders, List<String> modifiedBlobs, Map<Path, List<String>> unmodifiedBlobs) throws IOException {
        List<File> files = getFiles(currPath);
        for (File f : files) {
            String name = f.getName();
            String sha1;
            if (!f.isDirectory()) {
                Blob newBlob = new Blob(f);
                sha1 = newBlob.getSha1();
                if (wasElementModified(f.toPath(), modifiedBlobs)) {
                    zipFile(f, sha1, PathConsts.OBJECTS_FOLDER());
                    addElementRepToList(currFolder, name, sha1, "FILE", date, username);
                } else
                    addElementFromPrevRep(f.toPath(), unmodifiedBlobs, currFolder);
            } else {
                if (wasElementModified(f.toPath(), modifiedFolders)) {
                    Folder fol = new Folder();
                    sha1 = commitAndSaveFiles(fol, Paths.get(f.getPath()), date, modifiedFolders, modifiedBlobs, unmodifiedBlobs);
                    File folRep = createTextRepresentation(fol.toString(), sha1);
                    zipFile(folRep, sha1, PathConsts.OBJECTS_FOLDER());
                    folRep.delete();
                    addElementRepToList(currFolder, name, sha1, "FOLDER", date, username);
                } else
                    addPrevFolRep(f.toPath(), currFolder);
            }
        }
        Collections.sort(currFolder.getFilesInFolder());
        return DigestUtils.sha1Hex(currFolder.toString());
    }

    private void addPrevFolRep(Path finalPath, Folder currFolder) {
        Folder.folderComponents currComponents = getFolderComponentsByPath(workingPath, finalPath, getHeadCommitRootFolderSha1());
        currFolder.getFilesInFolder().add(currComponents);
    }

    private Folder.folderComponents getFolderComponentsByPath(Path workingPath, Path finalPath, String headCommitSha1) {
        List<String> compLst = getFolderListComponentsByPath(workingPath, finalPath, headCommitSha1);
        return stringToFolComponents(compLst);
    }

    private List<String> getFolderListComponentsByPath(Path currPath, Path finalPath, String currSha1) {
        List<String> prevComponents;
        List<String> prevStateFolderRep = currPath == null ? new ArrayList<>() : unzipFolderToCompList(currSha1, PathConsts.OBJECTS_FOLDER());
        Path nextPath = getNextPath(currPath, finalPath);
        for (int i = 0; i < prevStateFolderRep.size(); i++) {
            prevComponents = appManager.folderRepToList(prevStateFolderRep.get(i));
            if (prevComponents.get(2).equals("FOLDER")) {
                if ((Paths.get(currPath + "/" + prevComponents.get(0)).equals(nextPath)))
                    if (nextPath.equals(finalPath))
                        return prevComponents;
                    else
                        return getFolderListComponentsByPath(nextPath, finalPath, prevComponents.get(1));
            }
        }
        return null;
    }

    private Path getNextPath(Path currPath, Path finalPath) {
        if (currPath.equals(finalPath)) return finalPath;
        return Paths.get(currPath.toString() + "/" + finalPath.getName(currPath.getNameCount()));
    }

    private void addElementFromPrevRep(Path path, Map<Path, List<String>> unmodifiedBlobs, Folder folder) {
        if (unmodifiedBlobs.containsKey(path)) {
            List<Folder.folderComponents> filesInFolder = folder.getFilesInFolder();
            Folder.folderComponents components = stringToFolComponents(unmodifiedBlobs.get(path));
            filesInFolder.add(components);
        }
    }

    public static Folder.folderComponents stringToFolComponents(List<String> compList) {
        Folder.folderComponents out = new Folder.folderComponents(compList.get(0), compList.get(1), compList.get(2), compList.get(4), compList.get(3));
        return out;
    }

    private boolean wasElementModified(Path path, Collection<String> modifiedElements) {
        for (String s : modifiedElements)
            if (Paths.get(s).equals(path))
                return true;
        return false;
    }

    public static void addElementRepToList(Folder folder, String name, String sha1, String type, String date, String username) {
        boolean found = false;
        List<Folder.folderComponents> filesInFolder = folder.getFilesInFolder();
        for (Folder.folderComponents f : filesInFolder) {
            if (f.getName().equals(name) && f.getType().equals(type)) {
                found = true;
                f.setSha1(sha1);
                f.setLastChangerName(username);
                f.setLastChangeDate(date);
                break;
            }
        }
        if (!found) {
            if (type.equals("FILE"))
                filesInFolder.add(new Folder.folderComponents(name, sha1, "FILE", date, username));
            else
                filesInFolder.add(new Folder.folderComponents(name, sha1, "FOLDER", date, username));
        }
    }

    public static File createTextRepresentation(String toWrite, String name) {
        try {
            FileWriter fw = new FileWriter(PathConsts.TEMP_FOLDER() + "/" + name + ".txt");
            fw.write(toWrite);
            fw.close();
            return new File(PathConsts.TEMP_FOLDER() + "/" + name + ".txt");
        } catch (Exception e) {
            throw new UnsupportedOperationException("Error writing file.\nContent:\n" + toWrite + "\nName:\n" + name);
        }
    }

    public static File createFileRepresentation(String toWrite, String name) {
        try {
            FileWriter fw = new FileWriter(PathConsts.TEMP_FOLDER() + "/" + name);
            fw.write(toWrite);
            fw.close();
            return new File(PathConsts.TEMP_FOLDER() + "/" + name);
        } catch (Exception e) {
            throw new UnsupportedOperationException("Error writing file.\nContent:\n" + toWrite + "\nName:\n" + name);
        }
    }

    public static File createFileFromXmlRepresentation(String toWrite, String name) {
        try {
            FileWriter fw = new FileWriter(PathConsts.TEMP_FOLDER() + "/" + name);
            fw.write(toWrite);
            fw.close();
            return new File(PathConsts.TEMP_FOLDER() + "/" + name);
        } catch (Exception e) {
        }
        return null;
    }

    public static boolean fileExistsInFolder(String path, String sha1) {
        return (findFileInFolderByName(path, sha1) != null);
    }

    public static File findFileInFolderByName(String path, String name) {
        File[] files = Paths.get(path).toFile().listFiles();
        for (File f : files) {
            if (f.getName().equals(name))
                return f;
        }
        return null;
    }

    public static List getFiles(Path path) {
        List<File> out = new LinkedList<>();
        File[] files = path.toFile().listFiles();
        if (files != null) {
            Arrays.sort(files);
            for (File f : files) {
                if (!f.getName().equals(".magit"))
                    out.add(f);
            }
        }
        out.sort(Comparator.comparing(File::getName));
        return out;
    }

    public static boolean isEmptyFolder(File f) {
        if (f.isDirectory()) {
            File[] listOfFiles = f.listFiles();
            return listOfFiles == null || listOfFiles.length == 0;
        }
        return false;
    }

    public void switchRepo(String path) throws UnsupportedOperationException, FileNotFoundException {
        if (Files.notExists(Paths.get(path)))
            throw new FileNotFoundException("The path '" + path + "' does not exist");
        File[] files = new File(path).listFiles();
        for (File f : files)
            if (f.getName().equals(".magit") && f.isDirectory()) {
                workingPath = Paths.get(path);
                this.repository = new Repository(workingPath);
                return;
            } else
                throw new UnsupportedOperationException("This folder is not supported by Magit");
    }

    public DiffHandler getDiff() {
        DiffHandler diff = new DiffHandler();
        diff.createModifiedFilesLists();
        return diff;
    }

    public static List<String> folderRepToList(String s) {
        return new ArrayList<>(Arrays.asList(s.split("\\|")));
    }


    public static boolean isCleanState(DiffHandler diff) {
        return diff.getModifiedBlobs().isEmpty();
    }

    public void createNewBranch(String name) {
        Branch.createNewBranch(name);
    }

    public void deleteBranch(String name) throws Exception {
        Branch.deleteBranch(name);
    }

    public List<Branch> getAllBranchesToList() {
        return Branch.allBranchesToList();
    }

    public static void deleteFileFromFolder(String path, String name) throws IOException {
        File f = findFileInFolderByName(path, name);
        if (f != null)
            try {
                if (f.exists())
                    Files.delete(f.toPath());
            } catch (FileSystemException e) {
                throw new FileSystemException(e.getMessage() + "\nDelete this repository manually and try again");
            }
    }

    public void makeCheckOut(String branchName) throws FileSystemException {
        if (!fileExistsInFolder(PathConsts.BRANCHES_FOLDER(), branchName))
            throw new UnsupportedOperationException(branchName + " does not exist!");
        String commitSha1 = Branch.getCommitSha1ByBranchName(branchName);
        String folderToDeploySha1 = Commit.getCommitRootFolderSha1(commitSha1);
        deleteAllFilesFromFolder(workingPath);
        //if(!isEmptyFolder(workingPath.toFile())) throw new UnsupportedOperationException("Could not delete the content in '"+ workingPath+"'\nPlease delete manually and try again");
        deployFolder(folderToDeploySha1, workingPath);
        Branch.changeActiveBranch(branchName);
    }

    public static void deleteAllFilesFromFolder(Path path) throws FileSystemException {
        List<File> files = getFiles(path);
        for (File f : files) {
            if (f.isDirectory())
                deleteAllFilesFromFolder(Paths.get(f.getAbsolutePath()));
            try {
                Files.delete(f.toPath());
            } catch (FileSystemException e) {
                throw new FileSystemException(e.getMessage() + "\nDelete this repository manually and try again");
            } catch (Exception e) {
                e.getMessage();
            }
        }
    }

    private void deployFolder(String folderToDeploySha1, Path currPath) {
        List<String> fullFolderRep = folderToDeploySha1 == null || folderToDeploySha1.equals("") || folderToDeploySha1.equals(EMPTY_SHA1) ?
                new ArrayList<>()
                : unzipFolderToCompList(folderToDeploySha1, PathConsts.OBJECTS_FOLDER());
        List<String> prevComponents;
        for (int i = 0; i < fullFolderRep.size(); i++) {
            prevComponents = appManager.folderRepToList(fullFolderRep.get(i));
            if (prevComponents.get(2).equals("FOLDER"))
                deployFolder(prevComponents.get(1), Paths.get(currPath.toString() + "\\" + prevComponents.get(0)));
            else {
                File f = findFileInFolderByName(PathConsts.OBJECTS_FOLDER(), prevComponents.get(1));
                try {
                    ZipHandler.extract(f, currPath.toFile());
                } catch (IOException e) {
                    throw new UnsupportedOperationException("cannot extract file");
                }
            }
        }
    }

    public static void deployFile(String sha1, Path deployTo) {
        File f = findFileInFolderByName(PathConsts.OBJECTS_FOLDER(), sha1);
        try {
            ZipHandler.extract(f, deployTo.toFile());
        } catch (IOException e) {
            throw new UnsupportedOperationException("cannot extract file");
        }
    }

    public List<String> getCommitRep(String sha1) {
        return unzipFolderToCompList(sha1, PathConsts.OBJECTS_FOLDER());
    }

    public List<String> getHeadCommitRep() {
        return getCommitRep(getHeadCommitRootFolderSha1());
    }

    public void manuallyChangeBranch(String sha1) {
        sha1 = sha1.toLowerCase();
        if (!containsOnlyHex(sha1))
            throw new UnsupportedOperationException("Your input\n'" + sha1
                    + "'\ndoesn't represent a legal Sha-1");
        if (!fileExistsInFolder(PathConsts.OBJECTS_FOLDER(), sha1))
            throw new UnsupportedOperationException("The Sha-1 you entered doesn't exist.\nOperation terminated.");
        Branch.updateHeadBrunch(sha1);
    }

    private boolean containsOnlyHex(String value) {
        return value.matches("^[a-f0-9]{40}$");
    }

    public static List<Commit.commitComps> branchHistoryToListByCommitSha1(String currSha1) {
        List<Commit.commitComps> out = new LinkedList<>();
        getPrevCommitsToList(currSha1, out, new LinkedList<String>());
        return out;
    }

    private static void getPrevCommitsToList(String currSha1, List<Commit.commitComps> commits, LinkedList<String> sha1s) {
        if (sha1s.contains(currSha1))
            return;
        if (currSha1.equals("") || currSha1.equals("null") || currSha1.equals(Consts.EMPTY_SHA1))
            return;
        List<String> commitRep = unzipFolderToCompList(currSha1, PathConsts.OBJECTS_FOLDER());
        String firstSha1 = commitRep.get(1);
        String secSha1 = commitRep.get(2);
        commits.add(new Commit.commitComps(currSha1, commitRep.get(0), commitRep.get(1), commitRep.get(2), commitRep.get(3), commitRep.get(4), commitRep.get(5)));
        sha1s.add(currSha1);
        getPrevCommitsToList(firstSha1, commits, sha1s);
        getPrevCommitsToList(secSha1, commits, sha1s);
    }

    public static boolean isMagitRepo(String path) {
        if (Files.exists(Paths.get(path))) {
            File[] files = new File(path).listFiles();
            for (File f : files)
                if (f.getName().equals(".magit") && f.isDirectory())
                    return true;
        }
        return false;
    }

    public void deleteRepository(String location) throws IOException {
        deleteAllFilesFromFolder(Paths.get(location));
        deleteAllFilesFromFolder(Paths.get(location + "/.magit"));
        deleteFileFromFolder(location, ".magit");
        deleteFileFromFolder(Paths.get(location).getParent().toString(), Paths.get(location).getFileName().toString());
    }

    public String getHeadBranchName() {
        return unzipFolderToCompList("HEAD", PathConsts.BRANCHES_FOLDER()).get(0);
    }

    public List<Commit.commitComps> getAllCommits() {
        List<Commit.commitComps> out = new LinkedList<>();
        List<Branch> allBranches = Branch.allBranchesToList();
        for (Branch b : allBranches) {
            out.addAll(b.getAllCommits(b));
        }
        Collections.sort(out);
        return removeDuplicatesFromList(out);
    }

    private List<Commit.commitComps> removeDuplicatesFromList(List<Commit.commitComps> lst) {
        List<Commit.commitComps> out = new LinkedList();
        if (lst.size() > 0) out.add(lst.get(0));
        for (Commit.commitComps c : lst) {
            if (!c.getSha1().equals(out.get(out.size() - 1).getSha1()))
                out.add(c);
        }
        return out;
    }

    public static String getCommonFatherSha1(String firstSha1, String secSha1) {
        AncestorFinder fatherFinder = new AncestorFinder((Commit::sha1ToCommitRepresentative));
        return fatherFinder.traceAncestor(firstSha1, secSha1);
    }


    public static List<String> commitSha1ToHeadFolderFilesList(String folderSha1) {
        String EMPTY_SHA1 = "da39a3ee5e6b4b0d3255bfef95601890afd80709";
        List<String> commitRep = unzipFolderToCompList(folderSha1, PathConsts.OBJECTS_FOLDER());
        return (folderSha1 == null || folderSha1.equals(EMPTY_SHA1) || folderSha1.equals("")) ?
                new ArrayList<>()
                : unzipFolderToCompList(commitRep.get(0), PathConsts.OBJECTS_FOLDER());
    }

    public static List<String> folderSha1ToFilesList(String folderSha1) {
        String EMPTY_SHA1 = "da39a3ee5e6b4b0d3255bfef95601890afd80709";
        return (folderSha1 == null || folderSha1.equals(EMPTY_SHA1)) ?
                new ArrayList<>()
                : unzipFolderToCompList(folderSha1, PathConsts.OBJECTS_FOLDER());
    }

    public static List<String> initEmptyList() {
        List<String> out = new LinkedList<>();
        out.add("");
        out.add("");
        out.add("");
        out.add("");
        out.add("");
        return out;
    }

    public String scanWcForMergeCommit(Folder currFolder, Path currPath, String date, Map<Path, List<String>> representationMap) throws IOException {
        List<File> files = getFiles(currPath);
        List<Folder.folderComponents> filesInFolder = currFolder.getFilesInFolder();
        for (File f : files) {
            String name = f.getName();
            String sha1;
            Path relevantPath = Paths.get(currPath + "/" + name);
            if (!f.isDirectory()) {
                if (representationMap.containsKey(relevantPath)) {
                    filesInFolder.add(new Folder.folderComponents(representationMap.get(relevantPath)));
                }
                //else it was deleted?
            } else {
                if (representationMap.containsKey(relevantPath)) {
                    filesInFolder.add(new Folder.folderComponents(representationMap.get(relevantPath)));
                } else {
                    Folder fol = new Folder();
                    sha1 = scanWcForMergeCommit(fol, Paths.get(f.getPath()), date, representationMap);
                    File folRep = createTextRepresentation(fol.toString(), sha1);
                    zipFile(folRep, sha1, PathConsts.OBJECTS_FOLDER());
                    folRep.delete();
                    filesInFolder.add(new Folder.folderComponents(name, sha1, "FOLDER", date, username));
                }
            }
        }
        Collections.sort(currFolder.getFilesInFolder());
        return DigestUtils.sha1Hex(currFolder.toString());
    }

    public static String getFileDataFromSha1(String sha1) {
        File f = appManager.findFileInFolderByName(PathConsts.OBJECTS_FOLDER(), sha1);
        String dataFromZipped = unzipFileToString(f);
        return dataFromZipped;
    }

    public void cloneRepository(Path localPath, Path remotePath) throws IOException, PathException {
        if (!isMagitRepo(remotePath.toString()))
            throw new UnsupportedOperationException("Target folder is not a MAGit repository!");
        createEmptyRepository(localPath.toString());
        addRemoteBranchesFromCloned(remotePath);
        addRemoteObjectsFromCloned(remotePath);
        createHeadRTB(remotePath);
        generateRemoteFlag(remotePath);
    }

    private void generateRemoteFlag(Path remotePath) throws IOException {
        File f = createTextRepresentation(String.valueOf(remotePath), "REMOTE REMOTE");
        Files.createDirectories(Paths.get(PathConsts.BRANCHES_FOLDER() + "/" + remotePath.getFileName()));
        try {
            zipFile(f, "REMOTE REMOTE", PathConsts.REMOTE_FOLDER());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            f.delete();
        }
    }

    private void createHeadRTB(Path remotePath) throws IOException {
        String RRHead = getRRHeadBranch(remotePath);
        String RRSha1 = getRRHeadSha1(remotePath);
        deleteFileFromFolder(PathConsts.BRANCHES_FOLDER(), RRHead);
        deleteFileFromFolder(PathConsts.BRANCHES_FOLDER(), "master");
        createNewRTB(RRHead, remotePath, RRSha1);
        Branch.changeActiveBranch(RRHead);
    }

    private String getRRHeadSha1(Path remotePath) {
        String prevBranch = unzipFolderToCompList("HEAD", PathConsts.BRANCHES_FOLDER(remotePath)).get(0);
        String sha1 = null;
        File f = findFileInFolderByName(PathConsts.BRANCHES_FOLDER(remotePath), prevBranch);
        if (f == null || !f.exists())
            return null;
        sha1 = unzipFileToString(f);
        return sha1;
    }

    public void createNewRTB(String name, Path remotePath, String RRSha1) {
        Branch.createNewRTB(name, remotePath, RRSha1);
    }

    private String getRRHeadBranch(Path remotePath) {
        return unzipFolderToCompList("HEAD", PathConsts.BRANCHES_FOLDER(remotePath)).get(0);
    }

    public static String getOurRRBranchSha1ByName(String branchName) {
        File f = findFileInFolderByName(PathConsts.REMOTE_BRANCHES_FOLDER(), branchName);
        return unzipFileToString(f);
    }

    public static String getRRBranchOriginalSha1ByName(Path remotePath, String name) {
        File f = findFileInFolderByName(PathConsts.BRANCHES_FOLDER(remotePath), name);
        return unzipFileToString(f);
    }

    private void addRemoteObjectsFromCloned(Path clonePath) throws IOException {
        List<File> branches = getRemoteObjects(clonePath);
        Path path = Paths.get(workingPath + "/.magit/objects");
        //Files.createDirectories(path);
        for (File file : branches) {
            if (!Files.exists(Paths.get(path + "/" + file.getName())))
                Files.copy(file.toPath(), Paths.get(path + "/" + file.getName()));
        }
    }

    private List<File> getRemoteObjects(Path remotePath) {
        List<File> objects = getFiles(Paths.get(remotePath + "/.magit/objects"));
        return objects;
    }

    private void addRemoteBranchesFromCloned(Path clonePath) throws IOException {
        List<File> branches = getRemoteBranches(clonePath);
        Path path = Paths.get(workingPath + "/.magit/branches/" + clonePath.getName(clonePath.getNameCount() - 1) + "/");
        Files.createDirectories(path);
        for (File file : branches) {
            if (!Files.exists(Paths.get(path + "/" + file.getName())))
                Files.copy(file.toPath(), Paths.get(path + "/" + file.getName()));
        }
    }

    private List<File> getRemoteBranches(Path remotePath) throws IOException {
        List<File> branches = getFiles(Paths.get(remotePath + "/.magit/branches"));
        return branches;
    }

    public static boolean isRemoteRepo() {
        File f = findFileInFolderByName(PathConsts.REMOTE_FOLDER(), "REMOTE REMOTE");
        if (f == null || !f.exists()) return false;
        return true;
    }

    public static Path getRemotePath() {
        File f = findFileInFolderByName(PathConsts.REMOTE_FOLDER(), "REMOTE REMOTE");
        if (f == null || !f.exists()) return null;
        String path = unzipFileToString(f);
        return Paths.get(path);
    }

    public void fetchRepo() throws IOException {
        Path remotePath = getRemotePath();
        deleteAllFilesFromFolder(Paths.get(PathConsts.REMOTE_BRANCHES_FOLDER()));
        addRemoteBranchesFromCloned(remotePath);
        addRemoteObjectsFromCloned(remotePath);
    }

    public void remotePull() throws IOException {
        String currBranchName = Branch.getActiveBranch();
        Path remotePath = getRemotePath();
        // addRemoteObjectsFromCloned(remotePath);
        String RRSha1 = getRRBranchOriginalSha1ByName(remotePath, currBranchName);
        List<String> relevantSha1s = new LinkedList<>();
        getRelevantCommitSha1s(RRSha1, relevantSha1s, true);
        copyRelevantSha1sPull(relevantSha1s, remotePath);
        deleteFileFromFolder(PathConsts.REMOTE_BRANCHES_FOLDER(), currBranchName);
        File f = createTextRepresentation(RRSha1, currBranchName);
        zipFile(f, currBranchName, PathConsts.REMOTE_BRANCHES_FOLDER());
        f.delete();


//        deleteFileFromFolder(PathConsts.BRANCHES_FOLDER(getRemotePath()), currBranchName);
//        deleteFileFromFolder(PathConsts.REMOTE_BRANCHES_FOLDER(), currBranchName);
//        File f = createTextRepresentation(ourBranchSha1, currBranchName);
//        zipFile(f, currBranchName, PathConsts.BRANCHES_FOLDER(getRemotePath()));
//        zipFile(f, currBranchName, PathConsts.REMOTE_BRANCHES_FOLDER());

        //need to merge
    }

    private void copyRelevantSha1sPull(List<String> relevantSha1s, Path remotePath) throws IOException {
        for (String sha1 : relevantSha1s) {
            File f = findFileInFolderByName(PathConsts.OBJECTS_FOLDER(remotePath), sha1);
            if (!Files.exists(Paths.get(PathConsts.OBJECTS_FOLDER() + "/" + f.getName())))
                Files.copy(f.toPath(), Paths.get(PathConsts.OBJECTS_FOLDER() + "/" + f.getName()));
        }
    }

    private void getRelevantCommitSha1s(String currCommitSha1, List<String> relevantSha1s, boolean isRemote) {
        List<String> commitComps;
        commitComps = isRemote ?
                unzipFolderToCompList(currCommitSha1, PathConsts.OBJECTS_FOLDER(getRemotePath()))
                : unzipFolderToCompList(currCommitSha1, PathConsts.OBJECTS_FOLDER());
        String headFolderSha1 = commitComps.get(0);
        String prevCommit = commitComps.get(1);
        String secPrevCommit = commitComps.get(2);
        if (!(prevCommit == null || prevCommit.equals("") || prevCommit.equals("null")))
            getRelevantCommitSha1s(prevCommit, relevantSha1s, isRemote);
        if (!(secPrevCommit == null || secPrevCommit.equals("") || secPrevCommit.equals("null")))
            getRelevantCommitSha1s(secPrevCommit, relevantSha1s, isRemote);
        if (!relevantSha1s.contains(headFolderSha1))
            getFolderSha1s(headFolderSha1, relevantSha1s, isRemote);
        if (!relevantSha1s.contains(currCommitSha1))
            relevantSha1s.add(currCommitSha1);
    }

    private void getFolderSha1s(String headFolderSha1, List<String> relevantSha1s, boolean isRemote) {
        List<String> lst;
        lst = isRemote ?
                unzipFolderToCompList(headFolderSha1, PathConsts.OBJECTS_FOLDER(getRemotePath()))
                : unzipFolderToCompList(headFolderSha1, PathConsts.OBJECTS_FOLDER());
        List<String> comps = null;
        if (lst.get(0).equals(EMPTY_SHA1) || lst.get(0).equals("")) {
            relevantSha1s.add(EMPTY_SHA1);
            return;
        }
        for (String s : lst) {
            comps = folderRepToList(s);
            String currSha1 = comps.get(1);
            if (comps.get(2).equals("FOLDER")) {
                if (!relevantSha1s.contains(currSha1))
                    getFolderSha1s(currSha1, relevantSha1s, isRemote);
                if (!relevantSha1s.contains(currSha1))
                    relevantSha1s.add(currSha1);
            } else {
                if (!relevantSha1s.contains(currSha1))
                    relevantSha1s.add(currSha1);
            }
        }
        if (!relevantSha1s.contains(headFolderSha1))
            relevantSha1s.add(headFolderSha1);
    }

    public void remotePush() throws IOException {
        String currBranchName = Branch.getActiveBranch();
        String ourBranchSha1 = Branch.getCommitSha1ByBranchName(Branch.getActiveBranch());
        List<String> relevantSha1s = new LinkedList<>();
        getRelevantCommitSha1s(ourBranchSha1, relevantSha1s, false);
        copyRelevantSha1sPush(relevantSha1s, getRemotePath());
        deleteFileFromFolder(PathConsts.BRANCHES_FOLDER(getRemotePath()), currBranchName);
        deleteFileFromFolder(PathConsts.REMOTE_BRANCHES_FOLDER(), currBranchName);
        File f = createTextRepresentation(ourBranchSha1, currBranchName);
        zipFile(f, currBranchName, PathConsts.BRANCHES_FOLDER(getRemotePath()));
        zipFile(f, currBranchName, PathConsts.REMOTE_BRANCHES_FOLDER());
        f.delete();


        //Will be changed for EX3 - requests
        if (isRRBranchActive(currBranchName)) {
            makeRRCheckOut();
        }
    }

    private void makeRRCheckOut() {
        Path temp = workingPath;
        workingPath = getRemotePath();
        try {
            makeCheckOut(Branch.getActiveBranch());
        } catch (FileSystemException e) {
            e.printStackTrace();
        } finally {
            workingPath = temp;
        }
    }

    private boolean isRRBranchActive(String branchName) {
        return branchName.equals(getRRHeadBranch(getRemotePath()));
    }

    private void copyRelevantSha1sPush(List<String> relevantSha1s, Path remotePath) throws IOException {
        for (String sha1 : relevantSha1s) {
            File f = findFileInFolderByName(PathConsts.OBJECTS_FOLDER(), sha1);
            if (!Files.exists(Paths.get(PathConsts.OBJECTS_FOLDER(remotePath) + "/" + f.getName())))
                Files.copy(f.toPath(), Paths.get(PathConsts.OBJECTS_FOLDER(remotePath) + "/" + f.getName()));
        }
    }

    public void updateRBtoSha1(String branchName) {
        String sha1 = Branch.getCommitSha1ByBranchName(branchName);

    }

    public String getHeadBranchCommitSha1() {
        File f = findFileInFolderByName(PathConsts.BRANCHES_FOLDER(), Branch.getActiveBranch());
        if (f == null || !f.exists())
            throw new UnsupportedOperationException("Branch not found " + Branch.getActiveBranch());
        if (Branch.checkRTB(Branch.getActiveBranch()))
            return unzipFileToString(f).split("\n")[0];
        return unzipFileToString(f);
    }

    public void createNewRBLocally(String branchName, String branchSha1) throws IOException {
        File f = createTextRepresentation(branchSha1, branchName);
        deleteFileFromFolder(PathConsts.REMOTE_BRANCHES_FOLDER(), branchName);
        zipFile(f, branchName, PathConsts.REMOTE_BRANCHES_FOLDER());
        f.delete();
    }

    public void createNewRBinRemote(String branchName, String branchSha1) throws IOException {
        File f = createTextRepresentation(branchSha1, branchName);
        deleteFileFromFolder(PathConsts.BRANCHES_FOLDER(getRemotePath()), branchName);
        zipFile(f, branchName, PathConsts.BRANCHES_FOLDER(getRemotePath()));
        f.delete();
    }

    public void makeBranchRTB(String branchName, String branchSha1) throws IOException {
        String toWrite = (branchSha1 + "\n" + getRemotePath());
        deleteFileFromFolder(PathConsts.BRANCHES_FOLDER(), branchName);
        File f = createTextRepresentation(toWrite, branchName);
        zipFile(f, branchName, PathConsts.BRANCHES_FOLDER());
        f.delete();
    }
}
