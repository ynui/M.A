package components;

import appManager.Folder;

import java.util.List;

public class FileView {
    public Folder.folderComponents getCompList() {
        return compList;
    }

    private Folder.folderComponents compList;

    public FileView(List<String> compList) {
        this.compList = new Folder.folderComponents(compList.get(0), compList.get(1), compList.get(2), compList.get(4), compList.get(3));
    }

    @Override
    public String toString() {
        return compList.getName();
    }
}
