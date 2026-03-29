package org.example.View;

import org.example.Controller.WindowController;
import org.example.Model.LevelConfig;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
/**
 * Окно выбора уровня (карта уровней).
 * Отображает кнопки уровней, их состояние (разблокирован/заблокирован)
 * и количество заработанных звёзд под каждой кнопкой.
 */
public class GameWindow extends BaseWindow {
    //  Константы
    private static final int MAX_LEVELS = 3;
    //  Компоненты интерфейса
    private final Map<Integer, JButton> levelButtons;
    private JButton backButton;
    // Иконки кнопок
    private ImageIcon level1Icon, level1Rollover, level2Icon, level2Rollover, level3Icon, level3Rollover, lockedIcon;
    private ImageIcon backIcon, backRollover;
    private ImageIcon star1Icon, star2Icon, star3Icon;
    /**
     * Создаёт панель выбора уровня.
     * @param controller контроллер приложения
     */
    public GameWindow(WindowController controller) {
        super(controller);
        levelButtons = new HashMap<>();
        initUI();
    }
    /**
     * Инициализирует компоненты окна: фон, кнопки уровней, кнопку возврата.
     */
    private void initUI() {
        setLayout(null);
        setBackgroundImage("img/background_Game.png");
        // Загрузка иконок уровней
        level1Icon = loadIcon("img/Level_Button_1.png");
        level1Rollover = loadIcon("img/Level_Button_1_Active.png");
        level2Icon = loadIcon("img/Level_Button_2.png");
        level2Rollover = loadIcon("img/Level_Button_2_Active.png");
        level3Icon = loadIcon("img/Level_Button_3.png");
        level3Rollover = loadIcon("img/Level_Button_3_Active.png");
        lockedIcon = loadIcon("img/Level_Button_Locked.png");
        // Загрузка иконок кнопки "Назад"
        backIcon = loadIcon("img/Back_Button.png");
        backRollover = loadIcon("img/Back_Button_Active.png");
        // Загрузка иконок звёзд
        star1Icon = loadIcon("img/1_star.png");
        star2Icon = loadIcon("img/2_star.png");
        star3Icon = loadIcon("img/3_star.png");
        // Создание и размещение кнопок уровней
        for (int i = 1; i <= MAX_LEVELS; i++) {
            JButton levelButton = createLevelButton(i);
            int x = 113 + (i - 1) * 361; // позиция по горизонтали
            int y = 130;                 // позиция по вертикали
            levelButton.setBounds(x, y, 315, 421);
            add(levelButton);
            levelButtons.put(i, levelButton);
        }
        // Кнопка возврата
        backButton = createImageButton(backIcon, backRollover);
        backButton.addActionListener(e -> controller.switchTo(GameState.START));
        backButton.setBounds(37, 580, backIcon.getIconWidth(), backIcon.getIconHeight());
        add(backButton);
    }
    /**
     * Создаёт кнопку для указанного уровня.
     * @param level номер уровня (1, 2, 3)
     * @return настроенная кнопка
     */
    private JButton createLevelButton(int level) {
        boolean unlocked = currentUser != null && currentUser.isLevelUnlocked(level);
        ImageIcon icon = getIconForLevel(level, unlocked);
        JButton button = new JButton(icon);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setOpaque(false);
        button.setText(null);
        button.setFocusable(false); // предотвращаем захват фокуса
        if (unlocked) {
            button.addActionListener(e -> startLevel(level));
            ImageIcon rollover = getRolloverIconForLevel(level);
            if (rollover != null) button.setRolloverIcon(rollover);
            button.setEnabled(true);
        } else {
            button.setEnabled(false); // кнопка заблокирована
        }
        return button;
    }
    /**
     * Возвращает иконку для кнопки уровня.
     * @param level номер уровня
     * @param unlocked true, если уровень разблокирован
     * @return соответствующая иконка
     */
    private ImageIcon getIconForLevel(int level, boolean unlocked) {
        if (!unlocked) return lockedIcon;
        return switch (level) {
            case 1 -> level1Icon;
            case 2 -> level2Icon;
            case 3 -> level3Icon;
            default -> null;
        };
    }
    /**
     * Возвращает иконку для состояния наведения (rollover) кнопки уровня.
     * @param level номер уровня
     * @return иконка наведения или null, если её нет
     */
    private ImageIcon getRolloverIconForLevel(int level) {
        return switch (level) {
            case 1 -> level1Rollover;
            case 2 -> level2Rollover;
            case 3 -> level3Rollover;
            default -> null;
        };
    }
    /**
     * Обновляет внешний вид кнопок в соответствии с прогрессом текущего пользователя.
     * Вызывается при смене пользователя или после завершения уровня.
     */
    @Override
    protected void updateUserInfo() {
        if (currentUser == null) return;
        for (int i = 1; i <= MAX_LEVELS; i++) {
            JButton btn = levelButtons.get(i);
            if (btn == null) continue;
            boolean unlocked = currentUser.isLevelUnlocked(i);
            ImageIcon icon = getIconForLevel(i, unlocked);
            btn.setIcon(icon);
            if (unlocked) {
                btn.setEnabled(true);
                btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                btn.setRolloverEnabled(true);
                btn.setRolloverIcon(getRolloverIconForLevel(i));
                // Удаляем старых слушателей, чтобы не накапливались
                for (ActionListener al : btn.getActionListeners()) btn.removeActionListener(al);
                int finalI = i;
                btn.addActionListener(e -> startLevel(finalI));
            } else {
                // Заблокированная кнопка не должна быть серой, но и не реагировать на наведение
                btn.setEnabled(true);   // важно: не серый
                btn.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                btn.setRolloverEnabled(false);
                btn.setRolloverIcon(null);
                for (ActionListener al : btn.getActionListeners()) btn.removeActionListener(al);
                btn.addActionListener(e -> {}); // пустой слушатель, чтобы не было ошибок
            }
        }
        repaint();
    }
    /**
     * Запускает указанный уровень.
     * @param levelNumber номер уровня
     */
    private void startLevel(int levelNumber) {
        LevelConfig config = LevelConfig.loadLevels()
                .stream()
                .filter(l -> l.getLevelNumber() == levelNumber)
                .findFirst()
                .orElse(null);
        if (config == null) {
            JOptionPane.showMessageDialog(this, "Уровень не найден!");
            return;
        }
        LevelPanel levelPanel = new LevelPanel(controller, config);
        controller.switchTo(levelPanel);
    }
    /**
     * Сбрасывает состояние наведения (rollover) у всех кнопок.
     * Используется при появлении окна, чтобы кнопки не оставались подсвеченными.
     */
    private void resetButtonsRollover() {
        for (JButton btn : levelButtons.values()) {
            if (btn != null && btn.getModel().isRollover()) {
                btn.getModel().setRollover(false);
            }
        }
    }
    /**
     * Определяет текущее положение мыши и вручную устанавливает состояние наведения
     * для кнопок, над которыми находится курсор. Это решает проблему, когда мышь
     * уже находится над кнопкой в момент появления окна.
     */
    private void updateButtonsRolloverFromMouse() {
        try {
            PointerInfo info = MouseInfo.getPointerInfo();
            Point mousePos = info.getLocation();
            SwingUtilities.convertPointFromScreen(mousePos, this);
            for (JButton btn : levelButtons.values()) {
                if (btn != null && btn.isEnabled() && btn.isRolloverEnabled()) {
                    Rectangle bounds = btn.getBounds();
                    boolean isMouseOver = bounds.contains(mousePos);
                    ButtonModel model = btn.getModel();
                    if (isMouseOver != model.isRollover()) {
                        model.setRollover(isMouseOver);
                        btn.repaint();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * Вызывается, когда панель добавляется в контейнер.
     * Обновляет состояние кнопок и корректирует rollover в соответствии с позицией мыши.
     */
    @Override
    public void addNotify() {
        super.addNotify();
        if (currentUser != null) {
            updateUserInfo();
        }
        SwingUtilities.invokeLater(() -> {
            resetButtonsRollover();
            updateButtonsRolloverFromMouse();
        });
    }
    /**
     * Рисует компоненты окна.
     * Помимо стандартной отрисовки, выводит иконки звёзд под кнопками уровней.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (currentUser != null) {
            for (int i = 1; i <= MAX_LEVELS; i++) {
                if (!currentUser.isLevelUnlocked(i)) continue;
                int stars = currentUser.getLevelStars(i);
                if (stars <= 0) continue;
                ImageIcon starIcon = switch (stars) {
                    case 1 -> star1Icon;
                    case 2 -> star2Icon;
                    case 3 -> star3Icon;
                    default -> null;
                };
                if (starIcon == null) continue;
                JButton btn = levelButtons.get(i);
                if (btn == null) continue;
                int buttonCenterX = btn.getX() + btn.getWidth() / 2;
                starIcon.paintIcon(this, g,
                        buttonCenterX - starIcon.getIconWidth() / 2,
                        btn.getY() + btn.getHeight() + 10);
            }
        }
    }
}