package org.example.Model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.example.Model.User;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Управляет пользователями: регистрация, вход, обновление прогресса, сброс и удаление.
 * Данные сохраняются в JSON-файл.
 */
public class UserManager {
    private static final String FILE_NAME = "users.json";
    private Map<String, User> users;
    private Gson gson;
    private static int instanceCount = 0;

    /**
     * Создаёт менеджер пользователей и загружает существующие данные из файла.
     */
    public UserManager() {
        instanceCount++;
        System.out.println("UserManager instance #" + instanceCount);
        gson = new GsonBuilder().setPrettyPrinting().create(); // красивый JSON
        users = new HashMap<>();
        loadUsers();
    }

    /**
     * Регистрирует нового пользователя.
     *
     * @param username имя пользователя
     * @param password пароль
     * @return true, если регистрация успешна, false если имя уже занято
     */
    public boolean registerUser(String username, String password) {
        if (users.containsKey(username)) {
            return false;
        }

        User user = new User(username, password);
        users.put(username, user);
        System.out.println("registerUser: added " + username + ", keys now: " + users.keySet());
        saveUsers();
        return true;
    }

    /**
     * Выполняет вход пользователя.
     *
     * @param username имя пользователя
     * @param password пароль
     * @return объект User, если учётные данные верны, иначе null
     */
    public User loginUser(String username, String password) {
        User user = users.get(username);
        if (user != null && user.getPassword().equals(password)) {
            return user;
        }
        return null;
    }

    /**
     * Возвращает пользователя по имени.
     *
     * @param username имя пользователя
     * @return объект User или null, если не найден
     */
    public User getUserByUsername(String username) {
        return users.get(username);
    }

    /**
     * Обновляет количество звёзд на уровне. Если новое значение больше текущего,
     * сохраняет его и обновляет JSON.
     *
     * @param user   пользователь (может быть ссылка на currentUser)
     * @param level  номер уровня (1–3)
     * @param stars  новое количество звёзд
     */
    public void updateLevel(User user, int level, int stars) {
        User storedUser = users.get(user.getUsername());
        if (storedUser == null) return;

        int currentStars = storedUser.getLevelStars(level);

        if (stars > currentStars) {
            storedUser.setLevelStars(level, stars);
            // Синхронизация переданного объекта пользователя
            user.setLevelStars(level, stars);
            saveUsers();
        }
    }

    /**
     * Сохраняет текущий список пользователей в JSON-файл.
     */
    private void saveUsers() {
        try (Writer writer = new FileWriter(FILE_NAME)) {
            gson.toJson(users.values(), writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Загружает пользователей из JSON-файла (если он существует).
     */
    private void loadUsers() {
        File file = new File(FILE_NAME);
        if (!file.exists()) return;

        try (Reader reader = new FileReader(file)) {
            Type listType = new TypeToken<List<User>>() {}.getType();
            List<User> list = gson.fromJson(reader, listType);

            if (list != null) {
                for (User user : list) {
                    users.put(user.getUsername(), user);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Сбрасывает прогресс пользователя (обнуляет звёзды на всех уровнях)
     * и сохраняет изменения.
     *
     * @param user пользователь, чей прогресс сбрасывается
     */
    public void resetUserProgress(User user) {
        if (user == null) return;

        User storedUser = users.get(user.getUsername());
        if (storedUser == null) return;

        storedUser.resetProgress();

        user.getLevelStars().clear();
        user.getLevelStars().putAll(storedUser.getLevelStars());

        saveUsers();
    }

    /**
     * Удаляет пользователя по имени. Если пользователь не найден в памяти,
     * пытается перезагрузить данные из файла.
     *
     * @param username имя пользователя для удаления
     * @return true, если пользователь удалён, false – если не найден
     */
    public boolean deleteUser(String username) {
        if (!users.containsKey(username)) {
            // Перезагружаем из файла на случай рассинхронизации
            loadUsers();
            if (!users.containsKey(username)) {
                return false;
            }
        }
        users.remove(username);
        saveUsers();
        return true;
    }
}