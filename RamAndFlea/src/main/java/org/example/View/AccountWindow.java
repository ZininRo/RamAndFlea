package org.example.View;

import org.example.Controller.WindowController;
import org.example.Model.User;

import javax.swing.*;
import java.awt.*;

/**
 * Окно аккаунта пользователя.
 * Отображает статистику: имя, рекорд, прогресс по уровням.
 * Позволяет выйти из профиля или удалить его.
 */
public class AccountWindow extends BaseWindow {
    // Компоненты интерфейса
    private JLabel nameLabel, recordLabel, levelsLabel;
    // Иконки кнопок
    private ImageIcon backIcon, backRollover, logoutIcon, logoutRollover, deleteIcon, deleteRollover;
    /**
     * Создаёт панель аккаунта.
     * @param controller контроллер приложения
     */
    public AccountWindow(WindowController controller) {
        super(controller);
        initUI();
    }
    /**
     * Инициализирует интерфейс окна: фон, информационную панель, кнопки.
     */
    private void initUI() {
        setLayout(null);
        setBackgroundImage("img/background_Account.png");
        // Загрузка иконок кнопок
        backIcon = loadIcon("img/Back_Button.png");
        backRollover = loadIcon("img/Back_Button_Active.png");
        logoutIcon = loadIcon("img/Logout_Button.png");
        logoutRollover = loadIcon("img/Logout_Button_Active.png");
        deleteIcon = loadIcon("img/Delete_Button.png");
        deleteRollover = loadIcon("img/Delete_Button_Active.png");
        // Панель для отображения информации об игроке (прозрачная)
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(null);
        infoPanel.setBounds(113, 33, 300, 280);
        infoPanel.setOpaque(false);
        add(infoPanel);
        // Имя пользователя
        nameLabel = new JLabel("");
        nameLabel.setFont(new Font("Arial", Font.BOLD, 24));
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setBounds(30, 30, 540, 35);
        infoPanel.add(nameLabel);
        // Рекорд (максимум звёзд)
        recordLabel = new JLabel("");
        recordLabel.setFont(new Font("Arial", Font.BOLD, 24));
        recordLabel.setForeground(Color.WHITE);
        recordLabel.setBounds(30, 80, 540, 35);
        infoPanel.add(recordLabel);
        // Прогресс по уровням
        levelsLabel = new JLabel("");
        levelsLabel.setFont(new Font("Arial", Font.PLAIN, 20));
        levelsLabel.setForeground(Color.WHITE);
        levelsLabel.setBounds(30, 130, 540, 120);
        infoPanel.add(levelsLabel);
        // Кнопка "Назад" (возврат в главное меню)
        JButton backButton = createImageButton(backIcon, backRollover);
        backButton.setBounds(37, 580, backIcon.getIconWidth(), backIcon.getIconHeight());
        backButton.addActionListener(e -> controller.switchTo(GameState.START));
        add(backButton);
        // Кнопка "Выйти из профиля"
        JButton logoutButton = createImageButton(logoutIcon, logoutRollover);
        logoutButton.setBounds(113, 324, logoutIcon.getIconWidth(), logoutIcon.getIconHeight());
        logoutButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Вы уверены, что хотите выйти из профиля?",
                    "Выход",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );
            if (confirm == JOptionPane.YES_OPTION) {
                controller.setCurrentUser(null);
                controller.switchTo(GameState.LOGIN);
            }
        });
        add(logoutButton);
        // Кнопка "Удалить профиль"
        JButton deleteButton = createImageButton(deleteIcon, deleteRollover);
        deleteButton.setBounds(113, 444, deleteIcon.getIconWidth(), deleteIcon.getIconHeight());
        deleteButton.addActionListener(e -> {
            // Получаем актуальные данные пользователя (на случай, если они изменились)
            User freshUser = controller.getCurrentUser();
            if (freshUser == null) {
                JOptionPane.showMessageDialog(this, "Пользователь не найден. Возможно, вы уже вышли.");
                return;
            }
            String username = freshUser.getUsername();
            // Проверяем, существует ли пользователь в хранилище
            User userFromMap = controller.getUserManager().getUserByUsername(username);
            if (userFromMap == null) {
                JOptionPane.showMessageDialog(this,
                        "Пользователь не найден в списке активных. Возможно, данные повреждены.\nВыйдите и зайдите заново.");
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "ВНИМАНИЕ! Удаление профиля безвозвратно сотрёт весь прогресс.\nПродолжить?",
                    "Удаление профиля",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );
            if (confirm == JOptionPane.YES_OPTION) {
                boolean deleted = controller.getUserManager().deleteUser(username);
                if (deleted) {
                    JOptionPane.showMessageDialog(this, "Профиль успешно удалён.");
                    controller.setCurrentUser(null);
                    controller.switchTo(GameState.LOGIN);
                } else {
                    JOptionPane.showMessageDialog(this, "Не удалось удалить профиль. Возможно, он уже удалён.",
                            "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        add(deleteButton);
    }
    /**
     * Обновляет информацию о пользователе в интерфейсе.
     * Вызывается при установке нового пользователя (setCurrentUser)
     * и при смене пользователя в контроллере.
     */
    @Override
    protected void updateUserInfo() {
        if (currentUser == null) return;
        nameLabel.setText("Игрок: " + currentUser.getUsername());
        recordLabel.setText("Максимум звезд: " + currentUser.getRecord());
        // Формируем HTML-строку с прогрессом по уровням
        StringBuilder sb = new StringBuilder("<html>Уровни:<br>");
        for (int i = 1; i <= 3; i++) {
            sb.append("Уровень ").append(i).append(" - ");
            if (currentUser.isLevelUnlocked(i)) {
                sb.append(currentUser.getLevelStars(i)).append(" ★");
            } else {
                sb.append("Заблокирован");
            }
            sb.append("<br>");
        }
        sb.append("</html>");
        levelsLabel.setText(sb.toString());
        revalidate();
        repaint();
    }
}