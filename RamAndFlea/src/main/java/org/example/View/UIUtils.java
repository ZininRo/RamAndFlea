package org.example.View;

import javax.swing.*;
import java.awt.*;
import java.net.URL;
/**
 * Утилитарный класс для загрузки графических ресурсов (иконок, изображений)
 * Используется во всех окнах приложения для унификации загрузки иконок и создания кнопок.
 */
public class UIUtils {
    /**
     * Загружает иконку из папки ресурсов по указанному пути.
     * @param path путь к ресурсу
     * @return ImageIcon, если ресурс найден; null в противном случае
     */
    public static ImageIcon loadIcon(String path) {
        URL url = UIUtils.class.getClassLoader().getResource(path);
        if (url != null) {
            return new ImageIcon(url);
        } else {
            System.err.println("Resource not found: " + path);
            return null;
        }
    }
    /**
     * Создаёт кнопку-картинку с нормальным и наведённым состоянием.
     * @param normal  иконка в обычном состоянии (не может быть null)
     * @param rollover иконка при наведении (может быть null, тогда эффект отсутствует)
     * @return настроенная кнопка
     */
    public static JButton createImageButton(ImageIcon normal, ImageIcon rollover) {
        JButton button = new JButton(normal);
        if (rollover != null) button.setRolloverIcon(rollover);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setOpaque(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }
}