package UserManager;

import appManager.Repository;

import java.util.*;

/*
Adding and retrieving users is synchronized and in that manner - these actions are thread safe
Note that asking if a user exists (isUserExists) does not participate in the synchronization and it is the responsibility
of the user of this class to handle the synchronization of isUserExists with other methods here on it's own
 */
public class UserManager {

    public static User getUser(String username) {
        return usersList.stream()
                .filter(user -> user.getName().equals(username))
                .findAny()
                .orElse(null);
    }


    public static List<User> usersList;

    public UserManager() {
        usersList = new LinkedList<>();
    }

    public synchronized void addUser(String username) {
        usersList.add(new User(username));
    }

    public synchronized void removeUser(String username) {
        usersList.remove(username);
    }

    public synchronized List<User> getUsers() {
        return Collections.unmodifiableList(usersList);
    }

    public synchronized List<String> getUsernames() {
        List<String> out = new LinkedList<>();
        for(User u : usersList){
            out.add(u.getName());
        }
        return out;
    }

    public boolean isUserExists(String username) {
        return getUser(username) != null;
    }
}
