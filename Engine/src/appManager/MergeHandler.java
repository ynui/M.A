package appManager;

import java.io.IOException;
import java.nio.file.FileSystemException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class MergeHandler {

    public void addToFilesToRemove(String name, Path path) {
        this.filesToRemove.add(new FileToRemove(name, path));
    }

    public class FileToRemove {
        String name;
        Path path;

        public FileToRemove(String name, Path path) {
            this.name = name;
            this.path = path;
        }

        public String getName() {
            return name;
        }

        public Path getPath() {
            return path;
        }
    }

    public class FileToAdd {
        String name;
        String sha1;
        Path path;

        public String getName() {
            return name;
        }

        public String getSha1() {
            return sha1;
        }

        public Path getPath() {
            return path;
        }

        public FileToAdd(String name, String sha1, Path path) {
            this.name = name;
            this.sha1 = sha1;
            this.path = path;
        }
    }


    public class Conflict {

        Folder.folderComponents ourFile;
        Folder.folderComponents theirFile;
        Folder.folderComponents fatherFile;
        Path path;
        String name;


        public Path getPath() {
            return path;
        }


        public String getName() {
            return name;
        }


        public Folder.folderComponents getOurFile() {
            return ourFile;
        }

        public Folder.folderComponents getTheirFile() {
            return theirFile;
        }

        public Folder.folderComponents getFatherFile() {
            return fatherFile;
        }

        public Conflict(Folder.folderComponents ourFile, Folder.folderComponents theirFile, Folder.folderComponents fatherFile, Path path) {
            this.ourFile = ourFile;
            this.theirFile = theirFile;
            this.fatherFile = fatherFile;
            this.path = path;
            this.name = findFileName(ourFile, theirFile, fatherFile);
        }

        private String findFileName(Folder.folderComponents ourFile, Folder.folderComponents theirFile, Folder.folderComponents fatherFile) {
            if (ourFile != null)
                return ourFile.getName();
            if (theirFile != null)
                return theirFile.getName();
            if (fatherFile != null)
                return fatherFile.getName();
            return null;
        }

        public List<String> compToFileRep(Folder.folderComponents comps) {
            List<String> out = new LinkedList<>();
            out.add(comps.getName());
            out.add(comps.getSha1());
            out.add(comps.getType());
            out.add(comps.getLastChangerName());
            out.add(comps.getLastChangeDate());
            return out;
        }


        @Override
        public String toString() {
            return path + "\\" + name;
        }
    }

    private List<Conflict> conflicts;
    private Map<Path, List<String>> representationMap;
    private List<FileToAdd> filesToAdd;
    private List<FileToRemove> filesToRemove;

    public List<FileToRemove> getFilesToRemove() {
        return filesToRemove;
    }

    public List<FileToAdd> getFilesToAdd() {
        return filesToAdd;
    }

    public Map<Path, List<String>> getRepresentationMap() {
        return representationMap;
    }

    public List<Conflict> getConflicts() {
        return conflicts;
    }

    public MergeHandler() {
        conflicts = new LinkedList<>();
        filesToAdd = new LinkedList<>();
        filesToRemove = new LinkedList<>();
        representationMap = new HashMap<>();
    }

    public void getMergedHeadFolderAndSaveFiles(List<String> ourFilesList, List<String> theirFilesList, List<String> fatherFilesList, Path path) throws IOException {
        List<String> fileRep;
        String type;
        List<String> fileNames = getExclusiveNames(ourFilesList, theirFilesList, fatherFilesList);
        for (String name : fileNames) {
            int state = getState(name, ourFilesList, theirFilesList, fatherFilesList);
            switch (state) {
                case 0:
                    //delete father
                    break;
                case 1:
                case 11:
                    fileRep = getFileRepList(name, theirFilesList);
                    representationMap.put(Paths.get(path + "/" + name), fileRep);
                    addFileToWc(name, path, theirFilesList, fileRep);
                    break;
                case 2:
                    type = getFileRepType(name, theirFilesList);
                    if (type.equals("FOLDER"))
                        getMergedHeadFolderAndSaveFiles(new LinkedList<>(), getFolderCompsInFolderByName(name, theirFilesList), getFolderCompsInFolderByName(name, fatherFilesList), Paths.get(path + "/" + name));
                    else
                        addFileToConflicts(name, path, ourFilesList, theirFilesList, fatherFilesList);
                    break;
                case 3:
                    //delete theirs
                    break;
                case 4:
                case 8:
                case 10:
                case 12:
                case 13:
                    fileRep = getFileRepList(name, ourFilesList);
                    representationMap.put(Paths.get(path + "/" + name), fileRep);
                    break;
                case 5:
                    type = getFileRepType(name, ourFilesList);
                    if (type.equals("FOLDER"))
                        getMergedHeadFolderAndSaveFiles(getFolderCompsInFolderByName(name, ourFilesList), new LinkedList<>(), getFolderCompsInFolderByName(name, fatherFilesList), Paths.get(path + "/" + name));
                    else
                        addFileToConflicts(name, path, ourFilesList, theirFilesList, fatherFilesList);
                    break;
                case 6:
                    filesToRemove.add(new FileToRemove(name, path));
                    break;
                case 7:
                    type = getFileRepType(name, ourFilesList);
                    if (type.equals("FOLDER"))
                        getMergedHeadFolderAndSaveFiles(getFolderCompsInFolderByName(name, ourFilesList), getFolderCompsInFolderByName(name, theirFilesList), new LinkedList<>(), Paths.get(path + "/" + name));
                    else
                        addFileToConflicts(name, path, ourFilesList, theirFilesList, fatherFilesList);
                    break;
                case 9:
                    type = getFileRepType(name, ourFilesList);
                    if (type.equals("FOLDER"))
                        getMergedHeadFolderAndSaveFiles(getFolderCompsInFolderByName(name, ourFilesList), getFolderCompsInFolderByName(name, theirFilesList), getFolderCompsInFolderByName(name, fatherFilesList), Paths.get(path + "/" + name));
                    else
                        addFileToConflicts(name, path, ourFilesList, theirFilesList, fatherFilesList);
                    break;
            }
        }
    }

    private List<String> getFolderCompsInFolderByName(String name, List<String> filesList) {
        return appManager.folderSha1ToFilesList(getFileRepSha1(name, filesList));
    }


    private void deleteFolderFromWc(Path path) throws FileSystemException {
        appManager.deleteAllFilesFromFolder(path);
    }

    private List<String> getFileRepList(String name, List<String> filesList) {
        for (String fileName : filesList) {
            if (fileName.split("\\|")[0].equals(name))
                return new LinkedList<String>(Arrays.asList(fileName.split("\\|")));
        }
        return null;
    }

    private String getFileRepByName(String name, List<String> filesList) {
        for (String fileName : filesList) {
            if (fileName.split("\\|")[0].equals(name))
                return fileName;
        }
        return null;
    }

    private void deleteFileFromWc(String name, Path path) throws IOException {
        appManager.deleteFileFromFolder(String.valueOf(path), name);
    }

    private String getFileRepType(String name, List<String> filesList) {
        for (String fileName : filesList) {
            if (fileName.split("\\|")[0].equals(name))
                return fileName.split("\\|")[2];
        }
        return null;
    }

    private String getFileRepSha1(String name, List<String> filesList) {
        for (String fileName : filesList) {
            if (fileName.split("\\|")[0].equals(name))
                return fileName.split("\\|")[1];
        }
        return null;
    }

    private void addFileToConflicts(String fileName, Path path, List<String> ourFilesList, List<String> theirFilesList, List<String> fatherFilesList) {
        List<String> ourRep = null, theirRep = null, fatherRep = null;
        Folder.folderComponents ourComps = null, theirComps = null, fatherComps = null;
        List<String> temp = getExclusiveNames(ourFilesList, new LinkedList<>(), new LinkedList<>());
        if (temp.contains(fileName)) {
            ourRep = getFileRepList(fileName, ourFilesList);
        }
        temp = getExclusiveNames(new LinkedList<>(), theirFilesList, new LinkedList<>());
        if (temp.contains(fileName)) {
            theirRep = getFileRepList(fileName, theirFilesList);
        }
        temp = getExclusiveNames(new LinkedList<>(), new LinkedList<>(), fatherFilesList);
        if (temp.contains(fileName)) {
            fatherRep = getFileRepList(fileName, fatherFilesList);
        }
        if (ourRep != null) ourComps = new Folder.folderComponents(ourRep);
        if (theirRep != null) theirComps = new Folder.folderComponents(theirRep);
        if (fatherRep != null) fatherComps = new Folder.folderComponents(fatherRep);
        this.conflicts.add(new Conflict(ourComps, theirComps, fatherComps, path));
    }

    private void addFileToWc(String name, Path path, List<String> filesList, List<String> fileRep) throws IOException {
        if (fileRep.get(0).equals(name)) {
            if (fileRep.get(2).equals("FOLDER")) {
                representationMap.put(Paths.get(path + "/" + name), fileRep);
                List<String> newFilesList = appManager.folderSha1ToFilesList(fileRep.get(1));
                List<String> newPrevComponents = new LinkedList<>();
                for (String newFileName : newFilesList) {
                    newPrevComponents = appManager.folderRepToList(newFileName);
                    addFileToWc(newPrevComponents.get(0), Paths.get(path + "/" + fileRep.get(0)), newFilesList, newPrevComponents);
                }
            } else {
                filesToAdd.add(new FileToAdd(name, fileRep.get(1), path));
                representationMap.put(Paths.get(path + "/" + name), fileRep);
            }
        }
    }

    private int getState(String fileName, List<String> ourFilesList, List<String> theirFilesList, List<String> fatherFilesList) {
        boolean[] boolState = new boolean[6];
        List<String> temp;
        String ourSha1 = null, theirSha1 = null, fatherSha1 = null;

        temp = getExclusiveNames(ourFilesList, new LinkedList<>(), new LinkedList<>());
        if (temp.contains(fileName)) {
            boolState[0] = true;
            ourSha1 = getSha1FromCompListByName(ourFilesList, fileName);
        }
        temp = getExclusiveNames(new LinkedList<>(), theirFilesList, new LinkedList<>());
        if (temp.contains(fileName)) {
            boolState[1] = true;
            theirSha1 = getSha1FromCompListByName(theirFilesList, fileName);
        }
        temp = getExclusiveNames(new LinkedList<>(), new LinkedList<>(), fatherFilesList);
        if (temp.contains(fileName)) {
            boolState[2] = true;
            fatherSha1 = getSha1FromCompListByName(fatherFilesList, fileName);
        }

        if (ourSha1 != null && theirSha1 != null)
            if (ourSha1.equals(theirSha1)) boolState[3] = true;
        if (ourSha1 != null && fatherSha1 != null)
            if (ourSha1.equals(fatherSha1)) boolState[4] = true;
        if (theirSha1 != null && fatherSha1 != null)
            if (theirSha1.equals(fatherSha1)) boolState[5] = true;

        return getMergeStateFromBoolArray(boolState);
    }

    private int getMergeStateFromBoolArray(boolean[] boolState) {
        List<boolean[]> presets = new LinkedList<>();
        presets.add(new boolean[]{false, false, true, false, false, false});
        presets.add(new boolean[]{false, true, false, false, false, false});
        presets.add(new boolean[]{false, true, true, false, false, false});
        presets.add(new boolean[]{false, true, true, false, false, true});
        presets.add(new boolean[]{true, false, false, false, false, false});
        presets.add(new boolean[]{true, false, true, false, false, false});
        presets.add(new boolean[]{true, false, true, false, true, false});
        presets.add(new boolean[]{true, true, false, false, false, false});
        presets.add(new boolean[]{true, true, false, true, false, false});
        presets.add(new boolean[]{true, true, true, false, false, false});
        presets.add(new boolean[]{true, true, true, false, false, true});
        presets.add(new boolean[]{true, true, true, false, true, false});
        presets.add(new boolean[]{true, true, true, true, false, false});
        presets.add(new boolean[]{true, true, true, true, true, true});
        for (int i = 0; i < presets.size(); i++) {
            if (Arrays.equals(presets.get(i), boolState))
                return i;
        }
        throw new UnsupportedOperationException("Merging boolean state not found\n" + boolState);
    }

    private String getSha1FromCompListByName(List<String> filesList, String name) {
        List<String> temp;
        for (String fileRep : filesList) {
            temp = appManager.folderRepToList(fileRep);
            if (temp.get(0).equals(name))
                return temp.get(1);
        }
        return null;
    }


    private List<String> getExclusiveNames(List<String> ourFilesList, List<String> theirFilesList, List<String> fatherFilesList) {
        List<String> out = new LinkedList<>();
        List<String> allFromAll = new LinkedList<>();
        allFromAll.addAll(ourFilesList);
        allFromAll.addAll(theirFilesList);
        allFromAll.addAll(fatherFilesList);
        Collections.sort(allFromAll);
        allFromAll.replaceAll(s -> s.split("\\|")[0]);
        if (allFromAll.size() > 0) out.add(allFromAll.get(0));
        for (String s : allFromAll) {
            if (!s.equals(out.get(out.size() - 1)))
                out.add(s);
        }
        return out;
    }
}
