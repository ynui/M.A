package appManager;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class PathConsts {

    public static final String HUB_FOLDER = "c:/magit-ex3/";

    public static String OBJECTS_FOLDER() {
        return appManager.workingPath + "/.magit/objects/";
    }

    public static String OBJECTS_FOLDER(Path remotePath) {
        return remotePath + "/.magit/objects/";
    }

    public static String BRANCHES_FOLDER() {
        return appManager.workingPath + "/.magit/branches/";
    }

    public static String REMOTE_BRANCHES_FOLDER() {
        List<File> files = appManager.getFiles(Paths.get(BRANCHES_FOLDER()));
        for (File f : files)
            if (f.isDirectory())
                return appManager.workingPath + "/.magit/branches/" + f.getName();
        return null;
    }

    public static String BRANCHES_FOLDER(Path remotePath) {
        return remotePath + "/.magit/branches/";
    }

    public static String TEMP_FOLDER() {
        return appManager.workingPath + "/.magit/temp/";
    }

    public static String TEMP_FOLDER(Path remotePath) {
        return remotePath + "/.magit/temp/";
    }

    public static String REMOTE_FOLDER() { return appManager.workingPath + "/.magit/remote/"; }
}
