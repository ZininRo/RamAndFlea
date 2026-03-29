package org.example.View;

import org.example.Controller.WindowController;
import javax.swing.*;
/**
 * Главное меню игры (StartWindow).
 * Содержит кнопки: "Новая игра", "Продолжить", "Выйти", а также кнопку "Профиль"
 * для перехода к просмотру достижений.
 */
public class StartWindow extends BaseWindow {
    private JButton continueButton;
    // Иконки кнопок
    private ImageIcon newGameIcon, newGameRollover, newGamePressed;
    private ImageIcon continueIcon, continueRollover, continuePressed;
    private ImageIcon exitIcon, exitRollover, exitPressed;
    private ImageIcon youIcon, youRollover, youPressed;
    /**
     * Создаёт главное меню.
     * @param controller контроллер приложения
     */
    public StartWindow(WindowController controller) {
        super(controller);
        initUI();
    }
    /**
     * Инициализирует компоненты окна: фон, кнопки-картинки.
     */
    private void initUI() {
        setLayout(null);
        setBackgroundImage("img/background_menu.png");
        // Загрузка иконок для кнопок
        newGameIcon = loadIcon("img/New_Game.png");
        newGameRollover = loadIcon("img/New_Game_Active.png");
        newGamePressed = loadIcon("img/New_Game_Active.png");
        continueIcon = loadIcon("img/Continue.png");
        continueRollover = loadIcon("img/Continue_Active.png");
        continuePressed = loadIcon("img/Continue_Active.png");
        exitIcon = loadIcon("img/Exit.png");
        exitRollover = loadIcon("img/Exit_Active.png");
        exitPressed = loadIcon("img/Exit_Active.png");
        youIcon = loadIcon("img/YOU.png");
        youRollover = loadIcon("img/YOU_Active.png");
        // Кнопка "Новая игра"
        JButton newGameButton = createImageButton(newGameIcon, newGameRollover);
        newGameButton.setBounds(105, 260, newGameIcon.getIconWidth(), newGameIcon.getIconHeight());
        newGameButton.addActionListener(e -> {
            // Если у пользователя есть прогресс, спрашиваем подтверждение на сброс
            if (currentUser.getRecord() > 0) {
                int choice = JOptionPane.showConfirmDialog(
                        this,
                        "Начать новую игру? Прогресс текущего игрока будет сброшен.",
                        "Новая игра",
                        JOptionPane.YES_NO_OPTION
                );
                if (choice == JOptionPane.YES_OPTION && currentUser != null) {
                    controller.getUserManager().resetUserProgress(currentUser);
                    controller.setCurrentUser(currentUser);
                    controller.switchTo(GameState.GAME);
                }
            } else {
                controller.switchTo(GameState.GAME);
            }
        });
        add(newGameButton);
        // Кнопка "Продолжить"
        continueButton = createImageButton(continueIcon, continueRollover);
        continueButton.setBounds(105, 355, continueIcon.getIconWidth(), continueIcon.getIconHeight());
        continueButton.setEnabled(false); // по умолчанию отключена, активируется при наличии прогресса
        continueButton.addActionListener(e -> controller.switchTo(GameState.GAME));
        add(continueButton);
        // Кнопка "Выйти"
        JButton exitButton = createImageButton(exitIcon, exitRollover);
        exitButton.setBounds(105, 450, exitIcon.getIconWidth(), exitIcon.getIconHeight());
        exitButton.addActionListener(e -> System.exit(0));
        add(exitButton);
        // Кнопка "Профиль"
        JButton youButton = createImageButton(youIcon, youRollover);
        youButton.addActionListener(e -> controller.switchTo(GameState.ACCOUNT));
        add(youButton);
        // Позиционирование кнопки "Профиль" после того, как панель получит размеры
        SwingUtilities.invokeLater(() -> youButton.setBounds(46, 11, youIcon.getIconWidth(), youIcon.getIconHeight()));
    }
    /**
     * Обновляет состояние кнопки "Продолжить" в зависимости от наличия сохранённого прогресса.
     * Вызывается при смене пользователя или после изменения прогресса.
     */
    @Override
    protected void updateUserInfo() {
        if (currentUser == null) return;
        continueButton.setEnabled(currentUser.hasProgress());
        revalidate();
        repaint();
    }
}