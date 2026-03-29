package org.example.Model;

import java.util.HashMap;
import java.util.Map;
/**
 * Представляет пользователя приложения.
 * Хранит учётные данные (имя, пароль) и прогресс прохождения уровней
 * (количество звёзд для каждого уровня).
 */
public class User {
    private String username, password;
    private Map<Integer, Integer> levelStars;
    /**
     * Создаёт нового пользователя с указанным именем и паролем.
     * Изначально все уровни имеют 0 звёзд (уровень 1 доступен, остальные заблокированы).
     *
     * @param username имя пользователя
     * @param password пароль
     */
    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.levelStars = new HashMap<>();
        levelStars.put(1, 0);
        levelStars.put(2, 0);
        levelStars.put(3, 0);
    }
    /**
     * Проверяет, разблокирован ли указанный уровень.
     * Уровень 1 всегда разблокирован. Для уровней 2 и 3 требуется,
     * чтобы предыдущий уровень был пройден (имел хотя бы 1 звезду).
     *
     * @param level номер уровня
     * @return true, если уровень доступен для игры
     */
    public boolean isLevelUnlocked(int level) {
        if (level == 1) return true;
        return getLevelStars(level - 1) > 0;
    }
    /**
     * Сбрасывает прогресс пользователя: все уровни получают 0 звёзд.
     * Уровень 1 остаётся доступным, остальные блокируются.
     */
    public void resetProgress() {
        levelStars.clear();
        levelStars.put(1, 0);
        levelStars.put(2, 0);
        levelStars.put(3, 0);
    }

    // Геттеры и Сеттеры
    public String getUsername() {
        return username;
    }
    public String getPassword() {
        return password;
    }
    public int getRecord() {return levelStars.values().stream().mapToInt(Integer::intValue).sum();}
    public Map<Integer, Integer> getLevelStars() {
        return levelStars;
    }
    public int getLevelStars(int level) {
        return levelStars.getOrDefault(level, 0);
    }
    public void setLevelStars(int level, int stars) {
        levelStars.put(level, stars);
    }
    public int getStars(int level) {
        return levelStars.getOrDefault(level, 0);
    }
    public boolean hasProgress() {
        return levelStars.values().stream().anyMatch(stars -> stars > 0);
    }
}