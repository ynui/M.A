package appManager;


import XMLgenerated.*;
import XMLgenerated.MagitRepository.MagitRemoteReference;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class XmlRepo {

    private List<MagitSingleBranch> localBranches;
    private List<MagitSingleBranch> remoteBranches;
    private List<MagitSingleBranch> remoteTrackingBranches;
    private String name;


    private MagitRepository repository;
    private MagitBranches allBranches;
    private MagitCommits commits;
    private MagitFolders folders;
    private MagitBlobs blobs;
    private MagitRemoteReference remoteReference;

    public String getName() {
        return name;
    }

    public MagitRemoteReference getRemoteReference() {
        return remoteReference;
    }

    public List<MagitSingleBranch> getLocalBranches() {
        return localBranches;
    }

    public List<MagitSingleBranch> getRemoteBranches() {
        return remoteBranches;
    }

    public List<MagitSingleBranch> getRemoteTrackingBranches() {
        return remoteTrackingBranches;
    }

    public MagitRepository getRepository() {
        return repository;
    }

    public MagitBranches getAllBranches() {
        return allBranches;
    }

    public MagitCommits getCommits() {
        return commits;
    }

    public MagitFolders getFolders() {
        return folders;
    }

    public MagitBlobs getBlobs() {
        return blobs;
    }

    public XmlRepo(String xmlPath) throws FileNotFoundException {
        this.remoteTrackingBranches = new LinkedList<>();
        this.remoteBranches = new LinkedList<>();
        this.localBranches = new LinkedList<>();
        try {
            validatePath(xmlPath);
            File file = new File(xmlPath);
            JAXBContext jaxbContext = JAXBContext.newInstance(MagitRepository.class);

            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            MagitRepository repo = (MagitRepository) jaxbUnmarshaller.unmarshal(file);
            this.repository = repo;
            this.allBranches = repo.getMagitBranches();
            this.commits = repo.getMagitCommits();
            this.folders = repo.getMagitFolders();
            this.blobs = repo.getMagitBlobs();
            this.remoteReference = repo.getMagitRemoteReference();
            this.name = repo.getName();
            createBranchesLists();
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }
    public XmlRepo(String content, boolean hub) throws FileNotFoundException {
        this.remoteTrackingBranches = new LinkedList<>();
        this.remoteBranches = new LinkedList<>();
        this.localBranches = new LinkedList<>();
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(MagitRepository.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            StringReader sr = new StringReader(content);
            MagitRepository repo = (MagitRepository) jaxbUnmarshaller.unmarshal(sr);
            this.repository = repo;
            this.allBranches = repo.getMagitBranches();
            this.commits = repo.getMagitCommits();
            this.folders = repo.getMagitFolders();
            this.blobs = repo.getMagitBlobs();
            this.remoteReference = repo.getMagitRemoteReference();
            this.name = repo.getName();
            createBranchesLists();
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }

    private void createBranchesLists() {
        List<MagitSingleBranch> branchesList = allBranches.getMagitSingleBranch();
        for (MagitSingleBranch b : branchesList) {
            if (b.isTracking())
                remoteTrackingBranches.add(b);
            else if (b.isIsRemote())
                remoteBranches.add(b);
            else
                localBranches.add(b);

        }

    }

    private void validatePath(String xmlPath) throws FileNotFoundException {
        if (Files.notExists(Paths.get(xmlPath)))
            throw new FileNotFoundException("Path: '" + xmlPath + "' does not exist");
        if (!xmlPath.endsWith(".xml"))
            throw new UnsupportedOperationException("The file must be XML typed");
    }

    public void checkIfLegalRepo(XmlRepo xmlRepo) throws NoSuchMethodException, IllegalAccessException {
        MagitRepository repo = xmlRepo.getRepository();
        Map<String, Boolean> folderIds = new HashMap();
        Map<String, Boolean> blobsIds = new HashMap();
        Map<String, Boolean> commitsIds = new HashMap();
        Map<String, Boolean> branchNames = new HashMap();
        checkIfRemoteLegal();
        MagitBlobs blobs = repo.getMagitBlobs();
        MagitFolders folders = repo.getMagitFolders();
        MagitCommits commits = repo.getMagitCommits();
        MagitBranches branches = repo.getMagitBranches();
        setValidationMaps(folderIds, blobsIds, commitsIds, branchNames, folders.getMagitSingleFolder(), blobs.getMagitBlob(), commits.getMagitSingleCommit(), branches.getMagitSingleBranch());
        validateSingleId(blobs, folders, commits, branches);
        validateFolderElementsId(folders.getMagitSingleFolder(), blobsIds, folderIds);
        validateCommits(commits.getMagitSingleCommit(), folderIds);
        validateBranches(branches, commitsIds, branchNames);
    }

    private void checkIfRemoteLegal() {
        if (remoteReference != null) {
            if (remoteReference.getLocation() != null)
                if (!appManager.isMagitRepo(remoteReference.getLocation()))
                    throw new UnsupportedOperationException("You are trying to load a repository that is remote to: " + remoteReference.getLocation() + "\nThis path is not a MAGit repository.\nOperation terminated");
        }
    }

    private void validateBranches(MagitBranches branches, Map<String, Boolean> commitsIds, Map<String, Boolean> branchNames) {
        List<MagitSingleBranch> branchesLst = branches.getMagitSingleBranch();
        if (!branchNames.containsKey(branches.getHead()))
            throw new UnsupportedOperationException("HEAD branch points to branch name that doesn't exist: " + branches.getHead());
        for (MagitSingleBranch b : branchesLst) {
            if (!commitsIds.containsKey(b.getPointedCommit().getId()))
                throw new UnsupportedOperationException("MagitSingleBranch: " + b.getName() + " points to MagitSingleCommit that doesn't exist: " + b.getPointedCommit().getId());
            if (b.isTracking()) {
                if (!branchNames.containsKey(b.getTrackingAfter()))
                    throw new UnsupportedOperationException("MagitSingleBranch: " + b.getName() + " tracking after MagitSingleCommit that doesn't exist: " + b.getTrackingAfter());
                else {
                    if (!branchIsRemote(b.getName()))
                        throw new UnsupportedOperationException("MagitSingleBranch: " + b.getName() + " tracking after MagitSingleCommit that is not remote: " + b.getTrackingAfter());
                }
            }
        }
    }

    private boolean branchIsRemote(String name) {
        String compareName = getRemoteName(remoteReference) + "\\" + name;
        for (MagitSingleBranch b : remoteBranches) {
            if (b.getName().equals(compareName))
                return b.isIsRemote();
        }
        return false;
    }

    private String getRemoteName(MagitRemoteReference remoteReference) {
        if (remoteReference == null) return "";
        if (remoteReference.getLocation() == null) return "";
        return Paths.get(remoteReference.getLocation()).getFileName().toString();
    }

    private void validateCommits(List<MagitSingleCommit> commits, Map<String, Boolean> folderIds) {
        for (MagitSingleCommit c : commits) {
            if (!folderIds.containsKey(c.getRootFolder().getId()))
                throw new UnsupportedOperationException("MagitSingleCommit: " + c.getId() + " points to MagitSingleFolder that doesn't exist: " + c.getRootFolder().getId());
            MagitSingleFolder fol = Folder.getXmlFolder(this, c.getRootFolder().getId());
            if (!fol.isIsRoot())
                throw new UnsupportedOperationException("MagitSingleCommit: " + c.getId() + "'s root folder, MagitSingleFolder Id: " + fol.getId() + " isn't marked as root");
        }
    }

    private void validateFolderElementsId(List<MagitSingleFolder> folders, Map<String, Boolean> blobsIds, Map<String, Boolean> folderIds) {
        for (MagitSingleFolder f : folders) {
            List<Item> folderItems = f.getItems().getItem();
            for (Item i : folderItems) {
                if (i.getType().equals("blob")) {
                    if (!blobsIds.containsKey(i.getId()))
                        throw new UnsupportedOperationException("MagitSingleFolder id: " + f.getId() + " has blob with id: " + i.getId() + " which doesn't exist");
                } else if (i.getType().equals("folder")) {
                    if (!folderIds.containsKey(i.getId()))
                        throw new UnsupportedOperationException("MagitSingleFolder id: " + f.getId() + " has folder with id: " + i.getId() + " which doesn't exist");
                    else if (f.getId().equals(i.getId()))
                        throw new UnsupportedOperationException("MagitSingleFolder id: " + f.getId() + " has the same folder id: '" + f.getId() + "' as itself");
                } else
                    throw new UnsupportedOperationException("The type of the item is neither Folder or Blob");
            }
        }
    }

    private void setValidationMaps(Map<String, Boolean> folderIds, Map<String, Boolean> blobsIds, Map<String, Boolean> commitsIds, Map<String, Boolean> branchNames, List<MagitSingleFolder> folders, List<MagitBlob> blobs, List<MagitSingleCommit> commits, List<MagitSingleBranch> branchs) {
        for (MagitBlob b : blobs)
            blobsIds.put(b.getId(), true);
        for (MagitSingleFolder f : folders)
            folderIds.put(f.getId(), true);
        for (MagitSingleCommit c : commits)
            commitsIds.put(c.getId(), true);
        for (MagitSingleBranch b : branchs)
            branchNames.put(b.getName(), true);
    }


    private void validateSingleId(MagitBlobs blobs, MagitFolders folders, MagitCommits commits, MagitBranches branches) throws NoSuchMethodException, IllegalAccessException {
        List<MagitBlob> blobsList = blobs.getMagitBlob();
        validateNoDups(blobsList, "getId");
        List<MagitSingleFolder> foldersList = folders.getMagitSingleFolder();
        validateNoDups(foldersList, "getId");
        List<MagitSingleCommit> commitsList = commits.getMagitSingleCommit();
        validateNoDups(commitsList, "getId");
        List<MagitSingleBranch> branchesList = branches.getMagitSingleBranch();
        validateNoDups(branchesList, "getName");
    }

    private <T> void validateNoDups(List<T> lst, String methodName) throws IllegalAccessException, NoSuchMethodException {
        Map<String, Boolean> validMap = new HashMap<>();
        for (T curr : lst) {
            Method meth = null;
            meth = curr.getClass().getDeclaredMethod(methodName);
            meth.setAccessible(true);
            Object value = null;
            try {
                value = meth.invoke(curr);
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
            Boolean idCount = validMap.get(value);
            if (idCount == null)
                validMap.put((String) value, true);
            else
                throw new UnsupportedOperationException(lst.get(0).getClass().getSimpleName() + " with Id: " + value + " appears more then once");
        }
    }
}

