package org.example.View;

import org.example.Controller.WindowController;
import org.example.Model.User;

import javax.swing.*;
import java.awt.*;
/**
 * Окно входа и регистрации пользователя.
 * Позволяет создать нового пользователя или войти в существующий аккаунт.
 */
public class LoginWindow extends BaseWindow {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JLabel messageLabel;
    // Иконки кнопок
    private ImageIcon registerIcon, registerRollover, loginIcon, loginRollover;
    /**
     * Создаёт окно входа/регистрации.
     * @param controller контроллер приложения
     */
    public LoginWindow(WindowController controller) {
        super(controller);
        initUI();
    }
    /**
     * Инициализирует компоненты окна: фон, поля ввода, кнопки.
     */
    private void initUI() {
        setLayout(null);
        setBackgroundImage("img/background_Login.png");
        // Загрузка иконок кнопок
        registerIcon = loadIcon("img/register_button.png");
        registerRollover = loadIcon("img/register_button_active.png");
        loginIcon = loadIcon("img/login_button.png");
        loginRollover = loadIcon("img/login_button_active.png");
        // Поле ввода имени пользователя
        usernameField = new JTextField();
        usernameField.setBounds(520, 242, 240, 40);
        usernameField.setFont(new Font("Arial", Font.PLAIN, 18));
        usernameField.setBackground(new Color(255, 245, 200));
        usernameField.setBorder(null);
        add(usernameField);
        // Поле ввода пароля
        passwordField = new JPasswordField();
        passwordField.setBounds(520, 336, 240, 40);
        passwordField.setFont(new Font("Arial", Font.PLAIN, 18));
        passwordField.setBackground(new Color(255, 245, 200));
        passwordField.setBorder(null);
        add(passwordField);
        // Метка сообщений
        messageLabel = new JLabel("");
        messageLabel.setFont(new Font("Arial", Font.BOLD, 14));
        messageLabel.setForeground(Color.WHITE);
        messageLabel.setBounds(500, 190, 280, 30);
        add(messageLabel);
        // Кнопка регистрации
        int buttonX = 500;
        int buttonYStart = 415;
        JButton registerButton = createImageButton(registerIcon, registerRollover);
        registerButton.setBounds(buttonX, buttonYStart, registerIcon.getIconWidth(), registerIcon.getIconHeight());
        registerButton.addActionListener(e -> registerUser());
        add(registerButton);
        // Кнопка входа
        JButton loginButton = createImageButton(loginIcon, loginRollover);
        loginButton.setBounds(buttonX, buttonYStart + loginIcon.getIconHeight() + 20,
                loginIcon.getIconWidth(), loginIcon.getIconHeight());
        loginButton.addActionListener(e -> loginUser());
        add(loginButton);
    }
    /**
     * Регистрирует нового пользователя.
     * Проверяет корректность введённых данных, затем передаёт запрос в UserManager.
     */
    private void registerUser() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();
        // Проверка на пустые поля
        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Поля не должны быть пустыми!");
            return;
        }
        // Ограничение длины
        if (username.length() > 10 || password.length() > 10) {
            messageLabel.setText("Максимальная длинна 10 символов");
            return;
        }
        // Попытка регистрации
        boolean success = controller.getUserManager().registerUser(username, password);
        if (!success) {
            messageLabel.setText("Имя занято!");
        } else {
            messageLabel.setText("Пользователь " + username + " зарегистрирован!");
        }
    }
    /**
     * Выполняет вход существующего пользователя.
     * Проверяет корректность введённых данных и передаёт запрос в UserManager.
     */
    private void loginUser() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();
        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Введите имя и пароль!");
            return;
        }
        User loggedIn = controller.getUserManager().loginUser(username, password);
        if (loggedIn != null) {
            controller.setCurrentUser(loggedIn);
            controller.switchTo(GameState.START);
        } else {
            messageLabel.setText("Неверное имя или пароль!");
        }
    }
    /**
     * Обновление информации о пользователе не требуется,
     * так как на этом экране нет отображения данных пользователя.
     */
    @Override
    protected void updateUserInfo() {
        // LoginWindow не требует отображения информации о пользователе
    }
    /**
     * При добавлении окна в контейнер очищает все поля ввода.
     */
    @Override
    public void addNotify() {
        super.addNotify();
        usernameField.setText("");
        passwordField.setText("");
        messageLabel.setText("");
    }
}