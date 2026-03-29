package org.example.View;

import org.example.Controller.WindowController;
import org.example.Model.*;
import org.example.Model.LevelConfig;
import org.example.Model.User;
import org.example.Model.UserManager;
import org.example.Physics.PhysicsType;
import org.example.Physics.PhysicsUserData;
import org.example.Physics.PhysicsWorldManager;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
/**
 * Панель для отображения и управления игровым уровнем.
 * Содержит физический движок, обработку выстрелов, игровую логику и отрисовку.
 */
public class LevelPanel extends JPanel {
    // Максимальное расстояние натяжения рогатки (в пикселях)
    private static final float MAX_PULL_DISTANCE = 90f;
    // Игровые компоненты
    private final WindowController controller;
    private final UserManager userManager;
    private User currentUser;
    private final LevelConfig config;
    private final int levelNumber;
    private int fleasRemaining;
    // Состояние игры
    private boolean gameStarted = false;
    private boolean levelFinished = false;
    private boolean winPending = false;
    private long winTime = 0;
    private boolean paused = false;
    private final Timer timer;
    // Лук и блохи
    private final Point bowPoint = new Point(150, 470);
    private Point dragPoint;
    private boolean dragging = false;
    private boolean fleaLaunched = false;
    private final PhysicsWorldManager physics;
    private Body fleaBody;
    // Объекты уровня
    private final Map<Body, Dimension> blockSizes = new HashMap<>(); // размеры блоков (для отрисовки)
    private final Map<Body, Integer> ramRadii = new HashMap<>();     // радиусы баранов (для отрисовки)
    // Ресурсы
    private Image ramImage, ramDeadImage, bowImage, backgroundImage, gardenImage, fleaImage;
    private JButton pauseButton;
    /**
     * Создаёт панель уровня.
     * @param controller контроллер приложения
     * @param config конфигурация уровня (объекты, блохи и т.д.)
     */
    public LevelPanel(WindowController controller, LevelConfig config) {
        this.controller = controller;
        this.userManager = controller.getUserManager();
        this.currentUser = controller.getCurrentUser();
        this.config = config;
        this.levelNumber = config.getLevelNumber();
        this.fleasRemaining = config.getTotalFleas();
        // Загрузка ресурсов через утилитарный класс UIUtils
        ramImage = UIUtils.loadIcon("img/V.png").getImage();
        ramDeadImage = UIUtils.loadIcon("img/V-A.png").getImage();
        bowImage = UIUtils.loadIcon("img/Luk.png").getImage();
        backgroundImage = UIUtils.loadIcon("img/background.png").getImage();
        gardenImage = UIUtils.loadIcon("img/garden.png").getImage();
        fleaImage = UIUtils.loadIcon("img/flea.png").getImage();
        setFocusable(true);
        // Инициализация физического мира
        physics = new PhysicsWorldManager();
        initPhysics();
        // Обработчики мыши для лука
        initMouseHandlers();
        // Таймер игрового цикла (≈60 FPS)
        timer = new Timer(16, e -> updateGame());
        timer.start();
        // Кнопка паузы
        initPauseButton();
    }
    /**
     * Создаёт статические границы уровня и загружает объекты из конфигурации.
     */
    private void initPhysics() {
        // Невидимые границы, чтобы объекты не улетали за экран
        physics.createStaticBox(640, 570, 1280, 40, PhysicsType.GROUND);   // пол
        physics.createStaticBox(-20, 275, 40, 550, PhysicsType.WALL);      // левая стена
        physics.createStaticBox(1300, 275, 40, 550, PhysicsType.WALL);     // правая стена
        physics.createStaticBox(640, -20, 1280, 40, PhysicsType.CEILING);  // потолок
        // Создание блоков и свиней из конфигурации
        for (GameObjectDto obj : config.getObjects()) {
            if (obj.type == PhysicsType.BLOCK) {
                Body body = physics.createBlock(
                        obj.x, obj.y, obj.width, obj.height, obj.destructible
                );
                blockSizes.put(body, new Dimension(obj.width, obj.height));
            }
            if (obj.type == PhysicsType.RAM) {
                int r = obj.width / 2;
                Body body = physics.createRam(obj.x, obj.y, r, obj.hp);
                ramRadii.put(body, r);
            }
        }
        spawnFlea(); // создание первой блохи
    }
    /**
     * Создаёт блоху и размещает её в луке.
     */
    private void spawnFlea() {
        if (fleaBody == null) {
            fleaBody = physics.createFlea(bowPoint.x, bowPoint.y, 12);
        }
        physics.placeFlea(fleaBody, bowPoint.x, bowPoint.y);
        dragging = false;
        fleaLaunched = false;
        dragPoint = null;
    }
    /**
     * Инициализирует кнопку паузы.
     */
    private void initPauseButton() {
        setLayout(null); // абсолютное позиционирование
        ImageIcon pauseNormal = UIUtils.loadIcon("img/pause.png");
        ImageIcon pauseActive = UIUtils.loadIcon("img/pause_Active.png");
        pauseButton = UIUtils.createImageButton(pauseNormal, pauseActive);
        pauseButton.setBounds(1165, 35, pauseNormal.getIconWidth(), pauseNormal.getIconHeight());
        pauseButton.addActionListener(e -> openPauseMenu());
        add(pauseButton);
    }
    /**
     * Открывает меню паузы.
     */
    private void openPauseMenu() {
        paused = true;
        timer.stop();
        Object[] options = {"Продолжить", "Заново", "Выйти"};
        int choice = JOptionPane.showOptionDialog(
                this,
                "Игра на паузе",
                "Пауза",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                options,
                options[0]
        );
        if (choice == 0) {
            resumeGame();
        } else if (choice == 1) {
            restartLevel();
        } else if (choice == 2) {
            exitLevel();
        } else {
            resumeGame();
        }
    }
    private void resumeGame() {
        paused = false;
        timer.start();
    }
    /**
     * Выход с уровня на карту уровней.
     * Уничтожает все физические тела и останавливает таймер.
     */
    private void exitLevel() {
        levelFinished = true;
        paused = false;
        timer.stop();
        physics.getWorld().clearForces();
        // Удаление всех тел из физического мира
        Body body = physics.getWorld().getBodyList();
        while (body != null) {
            Body next = body.getNext();
            physics.getWorld().destroyBody(body);
            body = next;
        }
        goToMap();
    }
    /**
     * Инициализирует обработчики мыши для управления луком.
     */
    private void initMouseHandlers() {
        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (paused) return;
                if (levelFinished || fleasRemaining <= 0 || fleaLaunched) return;
                dragging = true;
                dragPoint = clampDragPoint(e.getPoint());
                physics.placeFlea(fleaBody, dragPoint.x, dragPoint.y);
            }
            @Override
            public void mouseDragged(MouseEvent e) {
                if (paused) return;
                if (!dragging || levelFinished || fleaLaunched) return;
                dragPoint = clampDragPoint(e.getPoint());
                physics.placeFlea(fleaBody, dragPoint.x, dragPoint.y);
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                if (paused) return;
                if (!dragging || levelFinished || fleaLaunched) return;
                dragging = false;
                Point releasePoint = clampDragPoint(e.getPoint());
                // Вычисление силы выстрела по разности координат
                float pullX = bowPoint.x - releasePoint.x;
                float pullY = releasePoint.y - bowPoint.y;
                physics.releaseFlea(fleaBody, pullX * 10f, pullY * 10f);
                fleasRemaining--;
                fleaLaunched = true;
                gameStarted = true; // игра началась, теперь физика будет обновляться
            }
        };
        addMouseListener(mouseAdapter);
        addMouseMotionListener(mouseAdapter);
    }
    /**
     * Ограничивает точку натяжения лука максимальным расстоянием и положением выше земли.
     * @param p исходная точка (координаты мыши)
     * @return скорректированная точка
     */
    private Point clampDragPoint(Point p) {
        double dx = p.x - bowPoint.x;
        double dy = p.y - bowPoint.y;
        double len = Math.sqrt(dx * dx + dy * dy);
        Point clamped;
        if (len > MAX_PULL_DISTANCE) {
            double k = MAX_PULL_DISTANCE / len;
            int x = (int) Math.round(bowPoint.x + dx * k);
            int y = (int) Math.round(bowPoint.y + dy * k);
            clamped = new Point(x, y);
        } else {
            clamped = p;
        }
        // Ограничение по Y: не ниже земли (550)
        if (clamped.y > 550) {
            clamped.y = 550;
            double newDx = clamped.x - bowPoint.x;
            double newDy = clamped.y - bowPoint.y;
            double newLen = Math.sqrt(newDx * newDx + newDy * newDy);
            if (newLen > MAX_PULL_DISTANCE) {
                double k = MAX_PULL_DISTANCE / newLen;
                clamped.x = (int) Math.round(bowPoint.x + newDx * k);
                clamped.y = (int) Math.round(bowPoint.y + newDy * k);
            }
        }
        return clamped;
    }
    /**
     * Удаляет все блоки и баранов, которые находятся ниже уровня земли.
     * Это предотвращает их падение в бесконечность.
     */
    private void removeObjectsBelowGround() {
        java.util.List<Body> toRemove = new ArrayList<>();
        for (Body body = physics.getWorld().getBodyList(); body != null; body = body.getNext()) {
            Object data = body.getUserData();
            if (!(data instanceof PhysicsUserData d)) continue;
            // Проверяем только блоки и баранов (живых или мёртвых)
            if (d.type == PhysicsType.BLOCK || d.type == PhysicsType.RAM) {
                Vec2 pos = body.getPosition();
                float screenY = toScreenY(pos.y);
                if (screenY > 550) { // ниже уровня земли
                    toRemove.add(body);
                }
            }
        }
        for (Body body : toRemove) {
            physics.getWorld().destroyBody(body);
        }
    }
    /**
     * Обновление состояния игры: физика, проверка победы/поражения, спавн новой птицы.
     */
    private void updateGame() {
        if (paused) return;
        if (levelFinished) {
            repaint();
            return;
        }
        // Обновление физики только после первого выстрела
        if (gameStarted) {
            physics.step();
            removeObjectsBelowGround();
        }
        // Обработка удалённых блох
        for (Body removed : physics.consumeRemovedFleas()) {
            if (removed == fleaBody) {
                fleaBody = null;
                fleaLaunched = false;
            }
        }
        int currentRams = countRamsAlive();
        if (currentRams == 0 && !winPending) {
            winPending = true;
            winTime = System.currentTimeMillis();
        }
        if (winPending) {
            long now = System.currentTimeMillis();
            if (now - winTime >= 400) {
                finishLevel(true);
                return;
            }
        }
        // Спавн следующей блохи, если предыдущая исчезла и есть еще блохи
        if (fleaBody == null && fleasRemaining > 0) {
            spawnFlea();
        }
        // Поражение: нет блох, есть живые бараны, все объекты остановились
        if (!winPending && fleasRemaining == 0 && fleaBody == null && currentRams > 0 && allObjectsStoppedIncludingAliveRams()) {
            finishLevel(false);
            return;
        }
        repaint();
    }
    /**
     * Проверяет, все ли движущиеся объекты остановились.
     * @return true, если скорости всех блоков, блохи и живых баранов ниже порога
     */
    private boolean allObjectsStoppedIncludingAliveRams() {
        for (Body body = physics.getWorld().getBodyList(); body != null; body = body.getNext()) {
            Object data = body.getUserData();
            if (!(data instanceof PhysicsUserData d)) continue;
            if (d.type == PhysicsType.BLOCK || d.type == PhysicsType.FLEA || (d.type == PhysicsType.RAM && !d.dead)) {
                Vec2 v = body.getLinearVelocity();
                if (Math.abs(v.x) > 0.03f || Math.abs(v.y) > 0.03f) {
                    return false;
                }
            }
        }
        return true;
    }
    /**
     * Подсчитывает количество живых баранов.
     */
    private int countRamsAlive() {
        int alive = 0;
        for (Body body = physics.getWorld().getBodyList(); body != null; body = body.getNext()) {
            Object data = body.getUserData();
            if (data instanceof PhysicsUserData d && d.type == PhysicsType.RAM && !d.dead) {
                alive++;
            }
        }
        return alive;
    }
    /**
     * Завершает уровень с результатом победы или поражения.
     * @param win true – победа, false – поражение
     */
    private void finishLevel(boolean win) {
        if (levelFinished) return;
        levelFinished = true;
        timer.stop();
        int stars = 0;
        if (win) {
            if (fleasRemaining >= 2) {
                stars = 3;
            } else if (fleasRemaining == 1) {
                stars = 2;
            } else {
                stars = 1;
            }
            if (currentUser != null) {
                userManager.updateLevel(currentUser, levelNumber, stars);
                currentUser = userManager.getUserByUsername(currentUser.getUsername());
                controller.setCurrentUser(currentUser);
            }
        }
        String message = win
                ? "Уровень пройден! Ты получил " + stars + " ★"
                : "Птицы закончились! Попробовать снова?";
        Object[] options = {"Переиграть", "К карте уровней"};
        int choice = JOptionPane.showOptionDialog(
                this,
                message,
                "Результат уровня",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                options,
                options[0]
        );
        if (choice == 0) {
            restartLevel();
        } else {
            goToMap();
        }
    }
    private void restartLevel() {
        controller.switchTo(new LevelPanel(controller, config));
    }
    /**
     * Переход на карту уровней.
     */
    private void goToMap() {
        if (controller.getGameWindow() != null && currentUser != null) {
            controller.getGameWindow().setCurrentUser(currentUser);
        }
        controller.switchTo(GameState.GAME);
    }
    private float toScreenX(float worldX) {return worldX * PhysicsWorldManager.PPM;}
    private float toScreenY(float worldY) {return 550f - worldY * PhysicsWorldManager.PPM;}
    // Отрисовка
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Фон и земля
        g.drawImage(backgroundImage, 0, 0, 1280, 720, null);
        g.drawImage(gardenImage, 0, 550, 1280, 170, null);
        // Лук
        g.drawImage(bowImage, bowPoint.x - 40, bowPoint.y - 60, 80, 120, null);
        // Линии натяжения
        if (dragging && dragPoint != null) {
            g.setColor(Color.DARK_GRAY);
            g.drawLine(bowPoint.x, bowPoint.y, dragPoint.x, dragPoint.y);
            g.drawLine(bowPoint.x, bowPoint.y + 35, dragPoint.x, dragPoint.y);
            g.drawLine(bowPoint.x - 15, bowPoint.y - 50, dragPoint.x, dragPoint.y);
        }
        // Отрисовка всех физических тел
        for (Body body = physics.getWorld().getBodyList(); body != null; body = body.getNext()) {
            Object data = body.getUserData();
            if (!(data instanceof PhysicsUserData d)) continue;
            Vec2 pos = body.getPosition();
            int sx = Math.round(toScreenX(pos.x));
            int sy = Math.round(toScreenY(pos.y));
            switch (d.type) {
                case FLEA:
                    if (fleaImage != null) {
                        Graphics2D g2d = (Graphics2D) g;
                        double angle = body.getAngle();
                        g2d.translate(sx, sy);
                        g2d.rotate(-angle);
                        g2d.drawImage(fleaImage, -12, -12, 24, 24, null);
                        g2d.rotate(angle);
                        g2d.translate(-sx, -sy);
                    } else {
                        g.setColor(Color.BLACK);
                        g.fillOval(sx - 12, sy - 12, 24, 24);
                    }
                    break;
                case RAM:
                    int r = ramRadii.getOrDefault(body, 15);
                    Image imgToDraw = d.dead && ramDeadImage != null ? ramDeadImage : ramImage;
                    if (imgToDraw != null) {
                        Graphics2D g2d = (Graphics2D) g;
                        double angle = body.getAngle();
                        g2d.translate(sx, sy);
                        g2d.rotate(-angle);
                        g2d.drawImage(imgToDraw, -r, -r, r * 2, r * 2, null);
                        g2d.rotate(angle);
                        g2d.translate(-sx, -sy);
                    } else {
                        g.setColor(d.dead ? Color.GRAY : Color.GREEN.darker());
                        g.fillOval(sx - r, sy - r, r * 2, r * 2);
                    }
                    break;
                case BLOCK:
                    Dimension size = blockSizes.getOrDefault(body, new Dimension(40, 20));
                    Graphics2D g2d = (Graphics2D) g;
                    double angle = body.getAngle();
                    g2d.translate(sx, sy);
                    g2d.rotate(-angle);
                    g2d.setColor(d.destructible ? Color.CYAN : Color.GRAY);
                    g2d.fillRect(-size.width / 2, -size.height / 2, size.width, size.height);
                    g2d.rotate(angle);
                    g2d.translate(-sx, -sy);
                    break;
                case GROUND:
                    break;
            }
        }
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 24));
        g.drawString("Уровень: " + levelNumber, 20, 30);
        g.drawString("Блохи: " + fleasRemaining, 20, 60);
        g.drawString("Бараны: " + countRamsAlive(), 20, 90);
    }
}