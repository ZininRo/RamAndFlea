package org.example.View;

import org.example.Controller.WindowController;
import org.example.Model.User;

import javax.swing.*;
import java.awt.*;
/**
 * Абстрактный базовый класс для всех окон (панелей) приложения.
 * Предоставляет общие возможности: загрузку фона, иконок, создание кнопок-картинок,
 * управление текущим пользователем и обновление информации о нём.
 * <p>
 * Все окна (StartWindow, GameWindow, LoginWindow, AccountWindow) наследуют этот класс
 */
public abstract class BaseWindow extends JPanel {
    // Поля
    protected WindowController controller;  // контроллер приложения
    protected User currentUser;             // текущий авторизованный пользователь
    protected Image backgroundImage;        // фоновое изображение окна
    /**
     * Создаёт базовое окно.
     * @param controller контроллер приложения
     */
    public BaseWindow(WindowController controller) {
        this.controller = controller;
    }
    /**
     * Устанавливает фоновое изображение по указанному пути.
     * Изображение загружается через ClassLoader из папки ресурсов.
     * @param path путь к изображению (например, "img/background.png")
     */
    protected void setBackgroundImage(String path) {
        java.net.URL url = getClass().getClassLoader().getResource(path);
        if (url != null) {
            backgroundImage = new ImageIcon(url).getImage();
        }
    }
    /**
     * Загружает иконку по указанному пути.
     * @param path путь к изображению (например, "img/button.png")
     * @return ImageIcon или null, если ресурс не найден
     */
    protected ImageIcon loadIcon(String path) {
        return UIUtils.loadIcon(path);
    }
    /**
     * Создаёт кнопку-картинку с нормальным и наведённым состоянием.
     * @param normal иконка в обычном состоянии
     * @param rollover иконка при наведении (может быть null)
     * @return настроенная кнопка
     */
    protected JButton createImageButton(ImageIcon normal, ImageIcon rollover) {
        return UIUtils.createImageButton(normal, rollover);
    }
    /**
     * Устанавливает текущего пользователя и обновляет информацию в окне.
     * @param user объект пользователя (может быть null для выхода)
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
        updateUserInfo();
    }
    /**
     * Абстрактный метод для обновления информации о пользователе в конкретном окне.
     */
    protected abstract void updateUserInfo();
    /**
     * Рисует фоновое изображение, если оно задано, а затем вызывает отрисовку дочерних компонентов.
     * @param g графический контекст
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
    }
    /**
     * Вызывается, когда панель добавляется в контейнер.
     * Устанавливает текущего пользователя из контроллера (если он есть).
     */
    @Override
    public void addNotify() {
        super.addNotify();
        if (controller.getCurrentUser() != null) {
            setCurrentUser(controller.getCurrentUser());
        }
    }
}