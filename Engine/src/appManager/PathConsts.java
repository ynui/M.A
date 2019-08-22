package appManager;

public class PathConsts {

    public static String OBJECTS_FOLDER() {
        return appManager.workingPath + "/.magit/objects/";
    }

    public static String BRANCHES_FOLDER() {
        return appManager.workingPath + "/.magit/branches/";
    }

    public static String TEMP_FOLDER() {
        return appManager.workingPath + "/.magit/temp/";
    }
}
