package appManager;

import XMLgenerated.Item;
import XMLgenerated.MagitBlob;
import XMLgenerated.MagitSingleFolder;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class Folder {
    private List<folderComponents> filesInFolder;

    public static String getXmlFolderSha1(Folder currFolder, XmlRepo repo, String folderId) throws IOException {
        String folderSha1;
        String type;
        File fileRep;
        //maybe repo.getFolders().getSingleFolders()
        List<MagitSingleFolder> foldersList = repo.getFolders().getMagitSingleFolder();
        MagitSingleFolder f = getXmlFolder(repo, folderId);
        List<Item> folderItems = f.getItems().getItem();
        for (Item i : folderItems) {
            type = i.getType();
            if (type.equals("blob")) {
                MagitBlob newXmlBlob = Blob.getXmlBlob(repo, i.getId());
                folderSha1 = DigestUtils.sha1Hex(newXmlBlob.getContent());
                fileRep = appManager.createFileFromXmlRepresentation(newXmlBlob.getContent(), newXmlBlob.getName());
                ZipHandler.zipFile(fileRep, folderSha1, PathConsts.OBJECTS_FOLDER());
                appManager.addElementRepToList(currFolder,
                        newXmlBlob.getName(),
                        folderSha1,
                        "FILE",
                        newXmlBlob.getLastUpdateDate(),
                        newXmlBlob.getLastUpdater());

            } else {
                Folder folder = new Folder();
                MagitSingleFolder newXmlFolder = Folder.getXmlFolder(repo, i.getId());
                folderSha1 = getXmlFolderSha1(folder, repo, newXmlFolder.getId());
                fileRep = appManager.createTextRepresentation(folder.toString(), folderSha1);
                ZipHandler.zipFile(fileRep, folderSha1, PathConsts.OBJECTS_FOLDER());
                appManager.addElementRepToList(currFolder,
                        newXmlFolder.getName(),
                        folderSha1,
                        "FOLDER",
                        newXmlFolder.getLastUpdateDate(),
                        newXmlFolder.getLastUpdater());
            }
        }
        Collections.sort(currFolder.getFilesInFolder());
        return DigestUtils.sha1Hex(currFolder.toString());
    }

    protected static MagitSingleFolder getXmlFolder(XmlRepo repo, String folderId) {
        String sha1;
        List<MagitSingleFolder> foldersList = repo.getFolders().getMagitSingleFolder();
        for (MagitSingleFolder f : foldersList) {
            if (f.getId().equals(folderId)) {
                return f;
            }
        }
        return null;
    }

    public List<folderComponents> getFilesInFolder() {
        return filesInFolder;
    }

    public void setFilesInFolder(List<folderComponents> filesInFolder) {
        this.filesInFolder = filesInFolder;
    }

    public Folder() {
        filesInFolder = new ArrayList<>();
    }


    @Override
    public String toString() {
        String out = "";
        for (folderComponents fc : filesInFolder)
            out += fc.toString() + "\n";
        return out;
    }

    public static class folderComponents implements Comparable<folderComponents> {
        private String name;
        private String sha1;
        private String lastChangerName;
        private String lastChangeDate;

        public folderComponents(String name, String sha1, String type, String date, String username) {
            this.name = name;
            this.sha1 = sha1;
            this.type = eType.valueOf(type);
            this.lastChangeDate = date;
            this.lastChangerName = username;
        }


        public String getType() {
            return type.toString();
        }

        public void setType(String type) {
            this.type = eType.valueOf(type);
        }

        private eType type;


        @Override
        public int compareTo(folderComponents o) {
            return this.name.compareTo(o.name);
        }

        private enum eType {FOLDER, FILE}


        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getSha1() {
            return sha1;
        }

        public void setSha1(String sha1) {
            this.sha1 = sha1;
        }

        public String getLastChangerName() {
            return lastChangerName;
        }

        public void setLastChangerName(String lastChangerName) {
            this.lastChangerName = lastChangerName;
        }

        public String getLastChangeDate() {
            return lastChangeDate;
        }

        public void setLastChangeDate(String lastChangeDate) {
            this.lastChangeDate = lastChangeDate;
        }

//        public String getSign(){
//            String out = "";
//            if(this.type == eType.FOLDER)
//                out = "() " + this.name;
//            else
//                out = this.name;
//            return out;
//        }

        @Override
        public String toString() {
            String delimiter = "|";
            return (this.name + delimiter +
                    this.sha1 + delimiter +
                    this.type + delimiter +
                    this.lastChangerName + delimiter +
                    this.lastChangeDate);
        }
    }

}
