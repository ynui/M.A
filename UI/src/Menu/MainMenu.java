package Menu;

import appManager.*;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static appManager.Commit.getHeadCommitSha1;

public class MainMenu {
    private ArrayList<MenuItem> menuItems;
    private Scanner scanner;
    private appManager manager;

    public MainMenu(appManager m) {
        this.menuItems = new ArrayList<>();
        this.scanner = new Scanner(System.in);
        this.manager = m;
        initMenu();
    }

    private void addMenuItem(String key, String name) {
        MenuItem menuItem = new MenuItem(key, name);
        menuItems.add(menuItem);
    }

    public void addMenuItem(String key, String name, Runnable runnable) {
        MenuItem menuItem = new MenuItem(key, name, runnable);
        menuItems.add(menuItem);
    }

    private void printMenuItems() {
        for (MenuItem menuItem : menuItems) {
            System.out.println("[" + menuItem.getKey() + "]: " + menuItem.getName());
        }
    }

    private void runCommand(String key) throws Exception {
        ArrayList<MenuItem> filteredMenuItems = new ArrayList<MenuItem>();

        // filter through the Menu items, checking if the given key corresponds
        // to a Menu.MenuItem
        for (MenuItem i : menuItems) {
            if (i.getKey().equals(key))
                filteredMenuItems.add(i);
        }

        if (filteredMenuItems.size() > 0) {
            // if there are any Menu items with the given key, run their
            // runnables
            for (MenuItem i : filteredMenuItems) {
                i.getRunnable().run();
            }
        } else
            // if not, throw an exception that the key doesn't exist
            throw new Exception("No valid option for '" + key + "' found, try again.");
    }

    private String scanLine() {
        System.out.print("Choose an action: ");
        return scanner.nextLine();
    }

    private void addDefaultMenuItems() {
        addMenuItem("Q", "Quit");
    }

    public void runMenu() {
        addDefaultMenuItems();

        Boolean quit = false;

        while (!quit) {
            // print instructions
            System.out.println("User: " + manager.getUsername());

            // print the Menu items every time
            printMenuItems();
            System.out.println("------------");

            // allow for input
            String option = scanLine();

            // show the input

            // act depending on the input
            try {
                switch (option) {
                    case "Q":
                        System.out.println("Quitting application...");
                        quit = true;
                        break;
                    default:
                        try {
                            runCommand(option);
                        } catch (Exception ex) {
                            System.out.println("An error has occured: " + ex.getMessage());
                        }
                        break;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            System.out.println();
        }
    }

    private void initMenu() {
        this.addMenuItem("1", "Change Username", () -> {
            System.out.print("Current user: " + manager.getUsername() + "\nChoose a new username: ");
            manager.setUsername(scanner.nextLine());
            System.out.println("Done!");
        });
        this.addMenuItem("2", "Load repository from XML", () -> {
            try {
                System.out.println("Enter full-path to the xml file:");
                XmlRepo xmlRepo = new XmlRepo(scanner.nextLine());
                String location = xmlRepo.getRepository().getLocation();
                xmlRepo.checkIfLegalRepo(xmlRepo);
                if (Files.exists(Paths.get(location))) {
                    if (manager.isMagitRepo(location)) {
                        System.out.println("Target location: " + Paths.get(location));
                        if (askForYesNo(QuestionConsts.ASK_XML_OVERRIDE)) {
                            System.out.println("Loading XML...");
                            manager.deleteRepository(location);
                            manager.deployXml(xmlRepo);
                            System.out.println("Done!");
                        } else {
                            System.out.println("XML loading terminated\nmoving to " + Paths.get(location));
                            manager.switchRepo(location);
                            System.out.println("Done!");
                        }
                    } else {
                        throw new UnsupportedOperationException("the target folder in this XML '" + Paths.get(location) + "' is not supported by MAGit\nOperation terminated");
                    }
                } else if (Files.notExists(Paths.get(xmlRepo.getRepository().getLocation()))) {
                    System.out.println("Loading XML...");
                    manager.deployXml(xmlRepo);
                    System.out.println("Done!");
                } else {
                    System.out.println("Target location " + Paths.get(xmlRepo.getRepository().getLocation()) + "does not belong to MAGit. Loading terminated.");
                }
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (FileAlreadyExistsException e) {
                e.printStackTrace();
            } catch (IOException e) {
                System.out.println(e.getLocalizedMessage());
            }
        });
        this.addMenuItem("3", "Switch repository", () -> {
            System.out.println("Enter the full path to the new repository:");
            try {
                manager.switchRepo(scanner.nextLine());
                System.out.println("Done!");
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        });
        this.addMenuItem("4", "Show all comitt's files and historical info", () -> {
            if (appManager.workingPath == null)
                throw new UnsupportedOperationException("No repository has been loaded yet!");
            List<String> headFolderRep = manager.getHeadCommitRep();
            System.out.println("Current commit status:\nFile Name | File's Sha1 | Type | Last Changer | Last Change Date");
            headFolderRep.forEach(System.out::println);
        });
        this.addMenuItem("5", "Show WC status", () -> {
            if (appManager.workingPath == null)
                throw new UnsupportedOperationException("No repository has been loaded yet!");
            System.out.println("Current repo:" +
                    "\nName: " + appManager.workingPath.getName(appManager.workingPath.getNameCount() - 1) +
                    "\nPath: " + appManager.workingPath +
                    "\nActive user: " + manager.getUsername());
            DiffHandler diff = manager.getDiff();
            if (appManager.isCleanState(diff))
                System.out.println("This repository is in \"Clean State!\"");
            else {
                System.out.println("Current files status on WC:\n");
                System.out.println("________________");
                System.out.println("Created Files:\n");
                printList(diff.getCreated());
                System.out.println("Deleted Files:\n");
                printList(diff.getDeleted());
                System.out.println("Changed Files:\n");
                printList(diff.getChanged());
            }
        });
        this.addMenuItem("6", "Commit", () -> executeCommit());

        this.addMenuItem("7", "Show all branches", () -> {
            if (appManager.workingPath == null)
                throw new UnsupportedOperationException("No repository has been loaded yet!");
            List<Branch> allBranches = manager.getAllBranchesToList();
            allBranches.forEach(System.out::println);

        });
        this.addMenuItem("8", "Create a new branch", () -> {
            if (appManager.workingPath == null)
                throw new UnsupportedOperationException("No repository has been loaded yet!");
            DiffHandler diff;
            String newBranchName;
            System.out.println("Choose a name for the new branch:");
            newBranchName = scanner.nextLine();
            manager.createNewBranch(newBranchName);
            System.out.println("Done!");
            if (askForYesNo(QuestionConsts.ASK_CHECKOUT)) {
                diff = manager.getDiff();
                if (!appManager.isCleanState(diff))
                    System.out.println("This repository is not in \"Clean State\"\nCheckout was not preformed");
                else {
                    System.out.println("Checking out to " + newBranchName);
                    try {
                        manager.makeCheckOut(newBranchName);
                    } catch (FileSystemException e) {
                        e.printStackTrace();
                    }
                    System.out.println("Done!");
                }

            }
        });
        this.addMenuItem("9", "Delete a branch", () -> {
            if (appManager.workingPath == null)
                throw new UnsupportedOperationException("No repository has been loaded yet!");
            System.out.println("Enter name of a branch to delete:");
            try {
                manager.deleteBranch(scanner.nextLine());
            } catch (Exception e) {
                e.printStackTrace();
            }

        });
        this.addMenuItem("10", "Choose a new head branch (Checkout)", () -> {
            String branchName;
            if (appManager.workingPath == null)
                throw new UnsupportedOperationException("No repository has been loaded yet!");
            DiffHandler diff = manager.getDiff();
            if (!appManager.isCleanState(diff))
                if (askForYesNo(QuestionConsts.ASK_COMMIT))
                    executeCommit();
            System.out.println("Enter the name of the new desired head branch:");
            branchName = scanner.nextLine();
            try {
                manager.makeCheckOut(branchName);
            } catch (FileSystemException e) {
                e.printStackTrace();
            }
            System.out.println("Done!");
        });
        this.addMenuItem("11", "Show active branch's history", () -> {
            if (appManager.workingPath == null)
                throw new UnsupportedOperationException("No repository has been loaded yet!");
            List<Commit.commitComps> commits = manager.branchHistoryToListBySha1(getHeadCommitSha1());
            for (int i = 0; i < commits.size(); i++) {
                System.out.println(i + 1 + ":");
                System.out.println(commits.get(i));
                System.out.println("===============");
            }
        });
        this.addMenuItem("b1", "Init empty repository", () -> {
            String repPath;
            System.out.println("Enter the full path for the new repository: ");
            repPath = scanner.nextLine();
            try {
                manager.createEmptyRepository(repPath);
            } catch (Exception ex) {
                System.out.println(ex.getLocalizedMessage());
            }
        });
        this.addMenuItem("b2", "Manually set branch", () -> {
            if (appManager.workingPath == null)
                throw new UnsupportedOperationException("No repository has been loaded yet!");
            DiffHandler diff = manager.getDiff();
            if (!appManager.isCleanState(diff)) {
                if (askForYesNo(QuestionConsts.ASK_COMMIT))
                    executeCommit();
            }
            System.out.println("Enter new Sha-1 for this branch:");
            manager.manuallyChangeBranch(scanner.nextLine());
            if (askForYesNo(QuestionConsts.ASK_CHECKOUT)) {
                try {
                    manager.makeCheckOut(manager.getHeadBranchName());
                } catch (FileSystemException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void printList(List<String> strings) {
        for (String s : strings)
            System.out.println(Paths.get(s));
        System.out.println("________________");
    }

    private boolean askForYesNo(String question) {
        String choice;
        System.out.println(question);
        System.out.println(QuestionConsts.YES_NO);
        choice = scanner.nextLine();
        while (!choice.equals("Y") && !choice.equals("N")) {
            System.out.println("No valid option for '" + choice + "' found, try again.");
            choice = scanner.nextLine();
        }
        return choice.equals("Y");
    }

    private void executeCommit() {
        if (appManager.workingPath == null)
            throw new UnsupportedOperationException("No repository has been loaded yet!");
        System.out.println("Enter note for this new commit: ");
        try {
            String note = scanner.nextLine();
            System.out.println("Committing...");
            manager.createNewCommit(note);
            System.out.println("Done!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
