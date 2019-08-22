package appManager;

import java.io.File;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Repository {
    private Path path;
    private Folder headFolder;

    public Repository() {
    }

    public Repository(Path path) {
        this.path = path;
        this.headFolder = new Folder();
    }

    public Folder getHeadFolder() {
        return headFolder;
    }

    public void setHeadFolder(Folder headFolder) {
        this.headFolder = headFolder;
    }


    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }


    public static Repository createEmptyRepository(String path, appManager manager) throws FileAlreadyExistsException {
        if (!Files.exists(Paths.get(path))) {
            new File(path + "/.magit/objects").mkdirs();
            new File(path + "/.magit/branches").mkdirs();
            new File(path + "/.magit/temp").mkdirs();
            appManager.workingPath = Paths.get(path);
            Branch.createEmptyBranch(path + "/.magit/branches");
        } else
            throw new FileAlreadyExistsException("Error! This file already exist\n" + Paths.get(path));
        Repository newRepo = new Repository(appManager.workingPath);
        newRepo.headFolder = new Folder();
        return newRepo;
    }

}
