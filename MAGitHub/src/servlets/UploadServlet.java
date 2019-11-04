package servlets;

import UserManager.User;
import appManager.Repository;
import appManager.XmlRepo;
import appManager.appManager;
import com.google.gson.Gson;
import constants.Constants;
import utils.ServletUtils;
import utils.SessionUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;


public class UploadServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String repoContent = request.getParameter("file");
        User user = SessionUtils.getUser(request);
        String username = SessionUtils.getUsername(request);
        response.setContentType("application/json");
        appManager manager = ServletUtils.getAppManager(getServletContext());
        List<Object> outLst = new LinkedList<>();
        try {
            Gson gson = new Gson();
            PrintWriter out = response.getWriter();
            XmlRepo repo = new XmlRepo(repoContent, true);
            String location = repo.getRepository().getLocation();
            try {
                repo.checkIfLegalRepo(repo);
            } catch (Exception e) {
                outLst.add(false);
                outLst.add(e.getMessage());
                out.println(gson.toJson(outLst));
                out.flush();
                return;
            }
            if (repoAlreadyExists(user, repo.getName())) {
                outLst.add(false);
                outLst.add(repo.getName() + "already Exists!");
                String json = gson.toJson(outLst);
                out.println(json);
                out.flush();
            } else {
                manager.deployXmlHub(repo, username);
                Repository newRepo = new Repository(getRepoPathInServer(username, repo.getName()), repo.getName());
                user.getRepositories().add(newRepo);
                user.getRepositoriesNames().add(repo.getName());
                outLst.add(true);
                outLst.add(repo.getName());
                String json = gson.toJson(outLst);
                out.println(json);
                out.flush();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    private boolean repoAlreadyExists(User user, String name) {
        return user.getRepositoriesNames().contains(name);
    }

    private Path getRepoPathInServer(String username, String repoName) {
        return Paths.get(Constants.HUB_FOLDER + "/" + username + "/" + repoName);
    }

//    private File createXmlInServer(String repoContent, String username) {
//        try {
//            FileWriter fw = new FileWriter(Constants.HUB_FOLDER + "/" +username + "/XML_REPO.xml");
//            fw.write(repoContent);
//            fw.close();
//            return new File(Constants.HUB_FOLDER + "/" +username + "/XML_REPO.xml");
//        } catch (Exception e) {
//            throw new UnsupportedOperationException("Error writing XML.\nContent:\n" + repoContent);
//        }
//    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }
}
