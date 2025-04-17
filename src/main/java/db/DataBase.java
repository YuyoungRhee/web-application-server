package db;

import java.util.Collection;
import java.util.Map;

import com.google.common.collect.Maps;

import java.util.Optional;
import model.User;

public class DataBase {
    private static Map<String, User> users = Maps.newHashMap();

    public static void addUser(User user) {
        users.put(user.getUserId(), user);
        System.out.println("데이터베이스: " + users);
    }

    public static Optional<User> findUserById(String userId) {
        User result = users.get(userId);

        if(result == null) {
            return Optional.empty();
        }
        return Optional.of(result);
    }

    public static Collection<User> findAll() {
        return users.values();
    }
}
