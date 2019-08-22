package Main;

import Menu.MainMenu;
import appManager.appManager;

public class Main {
    public static void main(String[] args) {
        MainMenu menu = new MainMenu(new appManager());
        menu.runMenu();
    }
}
