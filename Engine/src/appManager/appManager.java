package appManager;


import org.apache.commons.codec.digest.DigestUtils;
import sun.dc.path.PathException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static appManager.Commit.getHeadCommitRootFolderSha1;
import static appManager.Commit.getHeadCommitSha1;
import static appManager.ZipHandler.unzipFolderToCompList;
import static appManager.ZipHandler.zipFile;

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

    public void deployXml(XmlRepo repo) throws IOException {
        Repository.createEmptyRepository(repo.getRepository().getLocation(), manager);
        depolyXmlBranches(repo);
        makeCheckOut(repo.getBranches().getHead());
    }

    public static void depolyXmlBranches(XmlRepo repo) throws IOException {
        Branch.initXmlBranches(repo, repo.getBranches().getHead());
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

    private Folder.folderComponents stringToFolComponents(List<String> compList) {
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
            throw new UnsupportedOperationException("Error eriting file.\nContent:\n"+toWrite+"\nName:\n" + name);
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

    private static boolean isEmptyFolder(File f) {
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

    public void deleteBranch(String name) {
        Branch.deleteBranch(name);
    }

    public List<Branch> getAllBranchesToList() {
        return Branch.allBranchesToList();
    }

    public static void deleteFileFromFolder(String path, String name) {
        File f = findFileInFolderByName(path, name);
        if (f.exists())
            try {
                Files.delete(f.toPath());
            } catch (IOException e) {
                e.getMessage();
            }
    }

    public void makeCheckOut(String branchName) {
        if (!fileExistsInFolder(PathConsts.BRANCHES_FOLDER(), branchName))
            throw new UnsupportedOperationException(branchName + " does not exist!");
        String commitSha1 = Branch.getCommitSha1ByBranchName(branchName);
        String folderToDeploySha1 = Commit.getCommitRootFolderSha1(commitSha1);
        deleteAllFilesFromFolder(workingPath);
        deployFolder(folderToDeploySha1, workingPath);
        Branch.changeActiveBranch(branchName);
    }

    private void deleteAllFilesFromFolder(Path path) {
        List<File> files = getFiles(path);
        for (File f : files) {
            if (f.isDirectory())
                deleteAllFilesFromFolder(Paths.get(f.getAbsolutePath()));
            try {
                Files.delete(f.toPath());
            } catch (IOException e) {
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

    public List<String> showHeadCommitRep() {
        String headFolderRepSha1 = getHeadCommitRootFolderSha1();
        return unzipFolderToCompList(headFolderRepSha1, PathConsts.OBJECTS_FOLDER());
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

    public List<Commit.commitComps> branchHistoryToListBySha1(String currSha1) {
        List<Commit.commitComps> commits = new LinkedList<>();
        List<String> commitRep;
        while (!(currSha1.equals("") || currSha1.equals("null"))) {
            commitRep = unzipFolderToCompList(currSha1, PathConsts.OBJECTS_FOLDER());
            commits.add(new Commit.commitComps(currSha1, commitRep.get(3), commitRep.get(4), commitRep.get(5)));
            currSha1 = commitRep.get(1);
        }
        return commits;
    }

    public boolean isMagitRepo(String path) {
        File[] files = new File(path).listFiles();
        for (File f : files)
            if (f.getName().equals(".magit") && f.isDirectory())
                return true;
        return false;
    }

    public void deleteRepository(String location) {
        deleteAllFilesFromFolder(Paths.get(location));
        deleteAllFilesFromFolder(Paths.get(location + "/.magit"));
        deleteFileFromFolder(location, ".magit");
        deleteFileFromFolder(Paths.get(location).getParent().toString(), Paths.get(location).getFileName().toString());
    }

    public String getHeadBranchName() {
        return unzipFolderToCompList("HEAD", PathConsts.BRANCHES_FOLDER()).get(0);
    }
}
