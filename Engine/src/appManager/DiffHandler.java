package appManager;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static appManager.ZipHandler.unzipFolderToCompList;

public class DiffHandler {

    final String EMPTY_SHA1 = "da39a3ee5e6b4b0d3255bfef95601890afd80709";


    List<String> created;
    List<String> deleted;
    List<String> changed;
    List<String> modifiedBlobs;
    Set<String> modifiedFolders;
    Map<Path, List<String>> unmodifiedBlobs;

    public DiffHandler() {
        created = new LinkedList<>();
        deleted = new LinkedList<>();
        changed = new LinkedList<>();
        modifiedBlobs = new LinkedList<>();
        modifiedFolders = new HashSet<>();
        unmodifiedBlobs = new HashMap<>();
    }

    public List<String> getCreated() {
        return created;
    }

    public List<String> getDeleted() {
        return deleted;
    }

    public List<String> getChanged() {
        return changed;
    }

    public List<String> getModifiedBlobs() {
        return modifiedBlobs;
    }

    public Set<String> getModifiedFolders() {
        return modifiedFolders;
    }

    public Map<Path, List<String>> getUnmodifiedBlobs() {
        return unmodifiedBlobs;
    }

    public void createModifiedFilesListsBetween2FolderSha1(String firstSha1, String secSha1) {
        findModifiedFilesInFoldersBySha1(changed, created, deleted, unmodifiedBlobs, secSha1, firstSha1, appManager.workingPath);
        created.replaceAll(s -> Paths.get(s).toString());
        changed.replaceAll(s -> Paths.get(s).toString());
        deleted.replaceAll(s -> Paths.get(s).toString());
    }

    //setDiffs
    public void createModifiedFilesLists() {
        String commitHeadFolderSha1 = Commit.getHeadCommitRootFolderSha1();
        findModifiedFilesInFolder(changed, created, deleted, unmodifiedBlobs, commitHeadFolderSha1, appManager.workingPath);
        created.replaceAll(s -> Paths.get(s).toString());
        changed.replaceAll(s -> Paths.get(s).toString());
        deleted.replaceAll(s -> Paths.get(s).toString());
        modifiedBlobs.addAll(created);
        modifiedBlobs.addAll(changed);
        modifiedBlobs.addAll(deleted);
    }

    private void findModifiedFilesInFolder(List<String> changed, List<String> created, List<String> removed, Map<Path, List<String>> unmodified, String folderSha1, Path path) {
        List<String> prevComponents = new ArrayList<>();
        List<String> prevStateFolderRep = (folderSha1 == null || folderSha1.equals(EMPTY_SHA1)) ?
                new ArrayList<>()
                : unzipFolderToCompList(folderSha1, PathConsts.OBJECTS_FOLDER());
        List<File> currentFilesInFolder = appManager.getFiles(path);
        currentFilesInFolder.sort(Comparator.comparing(File::getName));
        int prevIndx = 0;
        int currIndx = 0;
        String prevName = "";
        String prevSha1 = "";
        String currName = "";
        String currSha1 = "";
        while (currIndx < currentFilesInFolder.size() && prevIndx < prevStateFolderRep.size()) {
            prevComponents = appManager.folderRepToList(prevStateFolderRep.get(prevIndx));
            prevName = prevComponents.get(0);
            prevSha1 = prevComponents.get(1);
            String prevType = prevComponents.get(2);
            currName = currentFilesInFolder.get(currIndx).getName();
            int diff = currName.compareTo(prevName);
            if (diff == 0) {
                if (prevType.equals("FOLDER")) {
                    findModifiedFilesInFolder(changed, created, removed, unmodified, prevComponents.get(1), Paths.get(path + "/" + prevName));
                } else {
                    Blob currBlob = new Blob(currentFilesInFolder.get(currIndx));
                    currSha1 = currBlob.getSha1();
                    if (!currSha1.equals(prevSha1))
                        changed.add(path + "/" + currName);
                    else
                        unmodified.put(Paths.get(path + "/" + currName), prevComponents);
                }
                currIndx++;
                prevIndx++;
            } else if (diff < 0) {
                if (currentFilesInFolder.get(currIndx).isDirectory())
                    addAllFolderFilesToList(created, currentFilesInFolder.get(currIndx).toPath());
                else
                    created.add(path + "/" + currName);
                currIndx++;
            } else {
                if (prevType.equals("FOLDER"))
                    addAllFolderRepFilesToList(removed, Paths.get(path + "/" + prevName), prevSha1);
                else
                    removed.add(path + "/" + prevName);
                prevIndx++;
            }
        }
        while (currIndx < currentFilesInFolder.size()) {
            if (currentFilesInFolder.get(currIndx).isDirectory())
                addAllFolderFilesToList(created, Paths.get(currentFilesInFolder.get(currIndx).getPath()));
            else
                created.add(path + "/" + currentFilesInFolder.get(currIndx).getName());
            currIndx++;
        }
        while (prevIndx < prevStateFolderRep.size()) {
            prevComponents = appManager.folderRepToList(prevStateFolderRep.get(prevIndx));
            if (prevComponents.get(2).equals("FOLDER"))
                addAllFolderRepFilesToList(removed, Paths.get(path + "/" + prevComponents.get(0)), prevComponents.get(1));//probably not working
            else
                removed.add(path + "/" + prevComponents.get(0));
            prevIndx++;
        }
    }


    private void findModifiedFilesInFoldersBySha1(List<String> changed, List<String> created, List<String> removed, Map<Path, List<String>> unmodified, String secFolderSha1, String firstFolderSha1, Path path) {
        List<String> firstPrevComponents = new ArrayList<>();
        List<String> secPrevComponents = new ArrayList<>();
        List<String> firstPrevStateFolderRep = (firstFolderSha1 == null || firstFolderSha1.equals(EMPTY_SHA1)) ?
                new ArrayList<>()
                : unzipFolderToCompList(firstFolderSha1, PathConsts.OBJECTS_FOLDER());
        List<String> secPrevStateFolderRep = (secFolderSha1 == null || secFolderSha1.equals(EMPTY_SHA1)) ?
                new ArrayList<>()
                : unzipFolderToCompList(secFolderSha1, PathConsts.OBJECTS_FOLDER());
        int firstIndx = 0;
        int secIndx = 0;
        String firstName = "";
        String firstSha1 = "";
        String firstType = "";
        String secName = "";
        String secSha1 = "";
        String secType = "";
        while (secIndx < secPrevStateFolderRep.size() && firstIndx < firstPrevStateFolderRep.size()) {
            firstPrevComponents = appManager.folderRepToList(firstPrevStateFolderRep.get(firstIndx));
            secPrevComponents = appManager.folderRepToList(secPrevStateFolderRep.get(secIndx));
            firstName = firstPrevComponents.get(0);
            firstSha1 = firstPrevComponents.get(1);
            firstType = firstPrevComponents.get(2);
            secName = secPrevComponents.get(0);
            secSha1 = secPrevComponents.get(1);
            secType = secPrevComponents.get(2);
            int diff = secName.compareTo(firstName);
            if (diff == 0) {
                if (firstType.equals("FOLDER")) {
                    findModifiedFilesInFoldersBySha1(changed, created, removed, unmodified, secSha1, firstSha1, Paths.get(path + "/" + firstName));
                } else {
                    if (!secSha1.equals(firstSha1))
                        changed.add(path + "/" + secName);
                    else
                        unmodified.put(Paths.get(path + "/" + secName), firstPrevComponents);
                }
                secIndx++;
                firstIndx++;
            } else if (diff < 0) {
                if (secType.equals("FOLDER"))
                    addAllFolderRepFilesToList(created, Paths.get(path + "/" + secName), secSha1);
                else
                    created.add(path + "/" + secName);
                secIndx++;
            } else {
                if (firstType.equals("FOLDER"))
                    addAllFolderRepFilesToList(removed, Paths.get(path + "/" + firstName), firstSha1);
                else
                    removed.add(path + "/" + firstName);
                firstIndx++;
            }
        }
        while (secIndx < secPrevStateFolderRep.size()) {
            secPrevComponents = appManager.folderRepToList(secPrevStateFolderRep.get(secIndx));
            secName = secPrevComponents.get(0);
            secSha1 = secPrevComponents.get(1);
            secType = secPrevComponents.get(2);
            if (secType.equals("FOLDER"))
                addAllFolderRepFilesToList(created, Paths.get(path + "/" + secName), secSha1);
            else
                created.add(path + "/" + secName);
            secIndx++;
        }
        while (firstIndx < firstPrevStateFolderRep.size()) {
            firstPrevComponents = appManager.folderRepToList(firstPrevStateFolderRep.get(firstIndx));
            firstName = firstPrevComponents.get(0);
            firstSha1 = firstPrevComponents.get(1);
            firstType = firstPrevComponents.get(2);
            if (firstType.equals("FOLDER"))
                addAllFolderRepFilesToList(removed, Paths.get(path + "/" + firstName), firstSha1);//probably not working
            else
                removed.add(path + "/" + firstName);
            firstIndx++;
        }
    }


    private void addAllFolderFilesToList(List<String> lst, Path p) {
        List<File> currFiles = appManager.getFiles(p);
        for (File f : currFiles) {
            if (f.isDirectory()) {
                addAllFolderFilesToList(lst, Paths.get(p + "/" + f.getName()));
            } else {
                lst.add(p + "/" + f.getName());
            }
        }
    }

//    public void setUnmodifiedFolders(Set<String> modifiedFolders) {
//        boolean wasModified = false;
//        List<File> files = appManager.getFiles(appManager.workingPath);
//        for(File f : files){
//            if(f.isDirectory()){
//                if(!modifiedFolders.contains(f.getPath()))
//                    this.unmodifiedFolders.put()
//            }
//        }
//    }

    public void addAllFolderRepFilesToList(List<String> lst, Path path, String prevSha1) {
        List<String> prevComponents = new ArrayList<>();
        List<String> prevStateFolderRep = (prevSha1 == null || prevSha1.equals(EMPTY_SHA1)) ? new ArrayList<>() : unzipFolderToCompList(prevSha1, PathConsts.OBJECTS_FOLDER());
        for (String fileRep : prevStateFolderRep) {
            prevComponents = appManager.folderRepToList(fileRep);
            if (prevComponents.get(2).equals("FOLDER"))
                addAllFolderRepFilesToList(lst, Paths.get(path + "/" + prevComponents.get(0)), prevComponents.get(1));
            else
                lst.add(path + "/" + prevComponents.get(0));
        }
    }

    protected void addAllFolderRepFilesToMap(Map<Path, List<String>> map, Path path, String prevSha1) {
        List<String> prevComponents = new ArrayList<>();
        List<String> prevStateFolderRep = (prevSha1 == null || prevSha1.equals(EMPTY_SHA1)) ? new ArrayList<>() : unzipFolderToCompList(prevSha1, PathConsts.OBJECTS_FOLDER());
        for (String fileRep : prevStateFolderRep) {
            prevComponents = appManager.folderRepToList(fileRep);
            if (prevComponents.get(2).equals("FOLDER"))
                addAllFolderRepFilesToMap(map, Paths.get(path + "/" + prevComponents.get(0)), prevComponents.get(1));
            else
                map.put(Paths.get(path + "/" + prevComponents.get(0)), prevComponents);
        }
    }

    //i can send modified files and check there instead of checking 3 lists again
    public void setModifiedFolders() {
        addModifiedFoldersToList(modifiedBlobs, modifiedFolders, appManager.workingPath);
    }

    protected void addModifiedFoldersToList(List<String> lst, Set<String> out, Path rootPath) {
        for (String s : lst) {
            Path p = Paths.get(s).getParent();
            while (!p.equals(rootPath)) {
                out.add(p.toString());
                p = p.getParent();
            }
        }
    }


}
