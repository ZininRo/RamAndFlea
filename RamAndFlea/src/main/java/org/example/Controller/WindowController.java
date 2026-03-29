package org.example.Controller;

import org.example.Model.User;
import org.example.Model.UserManager;
import org.example.View.*;

import javax.swing.*;
/**
 * Главный контроллер приложения.
 * Управляет переключением между окнами (экранами) и хранит глобальное состояние,
 * такое как текущий пользователь и экземпляры окон.
 */
public class WindowController {

    //  Окна приложения
    private StartWindow startWindow;
    private GameWindow gameWindow;
    private LoginWindow loginWindow;
    private AccountWindow accountWindow;

    // Управление пользователями
    private UserManager userManager;
    private User currentUser;

    // Графический интерфейс
    public JFrame frame;
    private GameState state;

    /**
     * Создаёт контроллер, инициализирует менеджер пользователей,
     * создаёт окна и отображает окно входа.
     */
    public WindowController() {
        userManager = new UserManager();

        frame = new JFrame("Rams and fleas");
        frame.setSize(1280, 720);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false); // Запрещает изменение размера окна

        startWindow = new StartWindow(this);
        gameWindow = new GameWindow(this);
        loginWindow = new LoginWindow(this);
        accountWindow = new AccountWindow(this);

        switchTo(GameState.LOGIN);

        frame.setVisible(true);
    }

    /**
     * Переключает приложение на указанное состояние (окно).
     *
     * @param newState новое состояние (LOGIN, START, GAME, ACCOUNT)
     */
    public void switchTo(GameState newState) {
        this.state = newState;
        frame.getContentPane().removeAll();
        switch (state) {
            case START -> frame.add(startWindow);
            case GAME -> frame.add(gameWindow);
            case LOGIN -> frame.add(loginWindow);
            case ACCOUNT -> frame.add(accountWindow);
        }
        frame.revalidate();
        frame.repaint();
    }

    /**
     * Переключает приложение на произвольную панель (например, LevelPanel).
     *
     * @param panel панель для отображения
     */
    public void switchTo(JPanel panel){
        frame.getContentPane().removeAll();
        frame.add(panel);
        frame.revalidate();
        frame.repaint();
    }

    /**
     * Устанавливает текущего пользователя и уведомляет все окна о смене пользователя.
     *
     * @param user новый текущий пользователь (может быть null для выхода)
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
        // Обновляем окна, чтобы они знали пользователя
        startWindow.setCurrentUser(user);
        gameWindow.setCurrentUser(user);
        accountWindow.setCurrentUser(user);
    }
    // Геттеры и сеттеры
    public User getCurrentUser() {
        return currentUser;
    }
    public UserManager getUserManager() {
        return userManager;
    }
    public StartWindow getStartWindow() {
        return startWindow;
    }
    public void setStartWindow(StartWindow startWindow) {
        this.startWindow = startWindow;
    }
    public GameWindow getGameWindow() {
        return gameWindow;
    }
    public void setGameWindow(GameWindow gameWindow) {
        this.gameWindow = gameWindow;
    }
    public LoginWindow getLoginWindow() {
        return loginWindow;
    }
    public void setLoginWindow(LoginWindow loginWindow) {
        this.loginWindow = loginWindow;
    }
    public GameState getState() {
        return state;
    }
    public void setState(GameState state) {
        this.state = state;
    }
}