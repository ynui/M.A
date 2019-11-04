package servlets;

import appManager.appManager;
import org.apache.commons.io.FileDeleteStrategy;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@WebListener
public class ContextListener implements ServletContextListener {


    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
            System.out.println("Woo Hoo! MAGit server starts!");
            try {
                Files.createDirectories(Paths.get("c:/magit-ex3"));
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        System.out.println("Oh no... MAGit server is going down...");
        try {
            FileDeleteStrategy.FORCE.delete(new File("c:/magit-ex3"));
//            appManager.deleteAllFilesFromFolder(Paths.get("c:/magit-ex3"));
//            Files.delete(Paths.get("c:/magit-ex3"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
