package appManager;

import XMLgenerated.MagitSingleCommit;
import org.apache.commons.codec.digest.DigestUtils;
import puk.team.course.magit.ancestor.finder.CommitRepresentative;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import static appManager.ZipHandler.*;
import static appManager.appManager.*;

public class Commit {
    public String getNote() {
        return note;
    }

    private String note;
    private String author;
    private String dateCreated;
    private String sha1;

    public Commit(String inNote, String inAuthor) {
        note = inNote;
        author = inAuthor;
        dateCreated = setDate();
    }


    public static void createCommit(appManager m, String note, String username) {
        Commit newCommit = new Commit(note, username);
        //Where to use?
        String prevSha1 = getHeadCommitSha1();
        String secPrevSha1 = "";
        DiffHandler diff = new DiffHandler();
        diff.createModifiedFilesLists();
        diff.setModifiedFolders();
        if (diff.modifiedBlobs.isEmpty())
            throw new UnsupportedOperationException("There were no changes since the last commit");
        try {
            Folder head = new Folder();
            String headFolderSha1 = m.commitAndSaveFiles(head, workingPath, newCommit.dateCreated, diff.getModifiedFolders(), diff.getModifiedBlobs(), diff.getUnmodifiedBlobs());
            String commitTxtRep = headFolderSha1 + "\n" +
                    prevSha1 + "\n" +
                    secPrevSha1 + "\n" +
                    note + "\n" +
                    newCommit.getDateCreated() + "\n" +
                    newCommit.getAuthor();
            File headFolderRep = createTextRepresentation(head.toString(), headFolderSha1);
            File commitRep = createTextRepresentation(commitTxtRep, DigestUtils.sha1Hex(commitTxtRep));
            zipFile(headFolderRep, headFolderSha1, PathConsts.OBJECTS_FOLDER());
            zipFile(commitRep, DigestUtils.sha1Hex(commitTxtRep), PathConsts.OBJECTS_FOLDER());
            headFolderRep.delete();
            commitRep.delete();
            Branch.updateHeadBrunch(DigestUtils.sha1Hex(commitTxtRep));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getHeadCommitSha1() {
        String prevCommitSha1 = null;
        String prevBranch = unzipFolderToCompList("HEAD", PathConsts.BRANCHES_FOLDER()).get(0);
        File f = findFileInFolderByName(PathConsts.BRANCHES_FOLDER(), prevBranch);
        if (f.exists())
            prevCommitSha1 = unzipFileToString(f);
        if (prevCommitSha1.contains("\n"))
            prevCommitSha1 = prevCommitSha1.substring(0, prevCommitSha1.indexOf("\n"));
        return prevCommitSha1;
    }

    public static String getSecHeadCommitSha1(String branchName) {
        String prevCommitSha1 = "";
        File f = findFileInFolderByName(PathConsts.BRANCHES_FOLDER(), branchName);
        if (f == null || !f.exists())
            f = findFileInFolderByName(PathConsts.REMOTE_BRANCHES_FOLDER(), branchName);
        prevCommitSha1 = unzipFileToString(f);
        if (prevCommitSha1.contains("\n"))
            prevCommitSha1 = prevCommitSha1.substring(0, prevCommitSha1.indexOf("\n"));
        return prevCommitSha1;
    }

    public static String getCommitRootFolderSha1(String commitSha1) {
        File f = findFileInFolderByName(PathConsts.OBJECTS_FOLDER(), commitSha1);
        if (f == null)
            return null;
//            throw new UnsupportedOperationException("File not found. Filename: " + commitSha1 + " Path: " + PathConsts.OBJECTS_FOLDER);
        String commitRep = unzipFileToString(f);
        return commitRep.substring(0, 40);//sha1 is 40 chars long
    }

    public static String getHeadCommitRootFolderSha1() {
        String headSha1 = getHeadCommitSha1();
        String rootFolderSha1 = getCommitRootFolderSha1(headSha1);
        return rootFolderSha1;
    }

    public static String getCommitNote(List<String> commitComponents) {
        String out = "";
        if (commitComponents.size() > 0)
            out = commitComponents.get(3);
        return out;
    }

    public static String getXmlCommitSha1(XmlRepo repo, String commitId) throws IOException {
        String headFolderSha1 = null;
        String prevSha1 = "";
        String secPrevSha1 = "";
        MagitSingleCommit c = getMagitSingleCommit(repo, commitId);
        Folder head = new Folder();
        headFolderSha1 = Folder.getXmlFolderSha1(head, repo, c.getRootFolder().getId());
        File headFolderRep = appManager.createTextRepresentation(head.toString(), headFolderSha1);
        ZipHandler.zipFile(headFolderRep, headFolderSha1, PathConsts.OBJECTS_FOLDER());
        if (c.getPrecedingCommits() != null) {
            if (c.getPrecedingCommits().getPrecedingCommit().size() == 1) {
                prevSha1 = getXmlCommitSha1(repo, c.getPrecedingCommits().getPrecedingCommit().get(0).getId());
            } else if (c.getPrecedingCommits().getPrecedingCommit().size() == 2) {
                prevSha1 = getXmlCommitSha1(repo, c.getPrecedingCommits().getPrecedingCommit().get(0).getId());
                secPrevSha1 = getXmlCommitSha1(repo, c.getPrecedingCommits().getPrecedingCommit().get(1).getId());
            }
        }
        String commitTxtRep = headFolderSha1 + "\n" +
                prevSha1 + "\n" +
                secPrevSha1 + "\n" +
                c.getMessage() + "\n" +
                c.getDateOfCreation() + "\n" +
                c.getAuthor();
        headFolderSha1 = DigestUtils.sha1Hex(commitTxtRep);
        File commitRep = createTextRepresentation(commitTxtRep, headFolderSha1);
        zipFile(commitRep, DigestUtils.sha1Hex(commitTxtRep), PathConsts.OBJECTS_FOLDER());
        headFolderRep.delete();
        commitRep.delete();
        return headFolderSha1;
    }

    private static MagitSingleCommit getMagitSingleCommit(XmlRepo repo, String commitId) {
        String sha1;
        List<MagitSingleCommit> foldersList = repo.getCommits().getMagitSingleCommit();
        for (MagitSingleCommit c : foldersList) {
            if (c.getId().equals(commitId)) {
                return c;
            }
        }
        return null;
    }

    public static List<String> getPrevCommits(Commit.commitComps c) {
        List<String> out = new LinkedList();
        if (!c.getPrevCommit().equals("")) out.add(c.getPrevCommit());
        if (!c.getSecPrevCommit().equals("")) out.add(c.getSecPrevCommit());
        return out;
    }

    public static List<String> getPrevCommitsBySha1(String sha1) {
        return getPrevCommits(sha1ToCommitComps(sha1));
    }

    public static CommitRepresentative sha1ToCommitRepresentative(String sha1) {
        List<String> commitRep = unzipFolderToCompList(sha1, PathConsts.OBJECTS_FOLDER());
        return (new Commit.commitComps(sha1, commitRep.get(0), commitRep.get(1), commitRep.get(2), commitRep.get(3), commitRep.get(4), commitRep.get(5)));
    }

    public static Commit.commitComps sha1ToCommitComps(String sha1) {
        List<String> commitRep = unzipFolderToCompList(sha1, PathConsts.OBJECTS_FOLDER());
        return (new Commit.commitComps(sha1, commitRep.get(0), commitRep.get(1), commitRep.get(2), commitRep.get(3), commitRep.get(4), commitRep.get(5)));
    }

    public static void createMergedCommit(appManager m, Commit commit, String branchName, MergeHandler mergeHandler) {
        Commit newCommit = commit;
        //Where to use?
        String prevSha1 = getHeadCommitSha1();
        String secPrevSha1 = getSecHeadCommitSha1(branchName);
        try {
            Folder head = new Folder();
            String headFolderSha1 = m.scanWcForMergeCommit(head, workingPath, newCommit.dateCreated, mergeHandler.getRepresentationMap());
            String commitTxtRep = headFolderSha1 + "\n" +
                    prevSha1 + "\n" +
                    secPrevSha1 + "\n" +
                    newCommit.getNote() + "\n" +
                    newCommit.getDateCreated() + "\n" +
                    newCommit.getAuthor();
            File headFolderRep = createTextRepresentation(head.toString(), headFolderSha1);
            File commitRep = createTextRepresentation(commitTxtRep, DigestUtils.sha1Hex(commitTxtRep));
            zipFile(headFolderRep, headFolderSha1, PathConsts.OBJECTS_FOLDER());
            zipFile(commitRep, DigestUtils.sha1Hex(commitTxtRep), PathConsts.OBJECTS_FOLDER());
            headFolderRep.delete();
            commitRep.delete();
            Branch.updateHeadBrunch(DigestUtils.sha1Hex(commitTxtRep));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String setDate() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy-HH:mm:ss:SSS"));
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public String getAuthor() {
        return author;
    }

    public static class commitComps implements Comparable<commitComps>, CommitRepresentative {
        public String getSha1() {
            return sha1;
        }

        @Override
        public String getFirstPrecedingSha1() {
            return this.prevCommit;
        }

        @Override
        public String getSecondPrecedingSha1() {
            return this.secPrevCommit;
        }

        public String getNote() {
            return note;
        }

        public String getDate() {
            return date;
        }

        public String getAuthor() {
            return author;
        }

        public String getHeadFolder() {
            return headFolder;
        }

        public String getPrevCommit() {
            return prevCommit;
        }

        public String getSecPrevCommit() {
            return secPrevCommit;
        }

        String sha1;
        String note;
        String date;
        String author;
        String headFolder;
        String prevCommit;
        String secPrevCommit;

        public commitComps(String sha1, String headFolder, String prevCommit, String secPrevCommit, String note, String date, String author) {
            this.sha1 = sha1;
            this.note = note;
            this.date = date;
            this.author = author;
            this.headFolder = headFolder;
            this.prevCommit = prevCommit;
            this.secPrevCommit = secPrevCommit;
        }

//        public static commitComps sha1ToCommitComps(String sha1){
//            List<String> commitRep = unzipFolderToCompList(sha1, PathConsts.OBJECTS_FOLDER());
//
//        }

        @Override
        public String toString() {
            return "sha1: " + sha1 + '\n' +
                    "note: " + note + '\n' +
                    "date: " + date + '\n' +
                    "author: " + author + '\n';
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof commitComps) {
                return this.sha1.equals(((commitComps) obj).getSha1());
            }
            return false;
        }

        @Override
        public int compareTo(commitComps o) {
            Date thisDate = null;
            Date otherDate = null;
            try {
                thisDate = new SimpleDateFormat("dd.MM.yyyy-HH:mm:ss:SSS").parse(this.date);
                otherDate = new SimpleDateFormat("dd.MM.yyyy-HH:mm:ss:SSS").parse(o.getDate());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return thisDate.compareTo(otherDate);

        }
    }

}
