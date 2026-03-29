package org.example.Model;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.List;
/**
 * Конфигурация игрового уровня, загружаемая из JSON-файла.
 * Содержит номер уровня, количество блох и список объектов (блоков, баранов).
 */
public class LevelConfig {
    private int levelNumber, totalFleas;
    private List<GameObjectDto> objects;
    public LevelConfig() {}
    /**
     * Загружает все уровни из файла levels.json, находящегося в ресурсах.
     *
     * @return список конфигураций уровней
     * @throws RuntimeException если не удалось загрузить JSON
     */
    public static List<LevelConfig> loadLevels() {
        try {
            ObjectMapper mapper = new ObjectMapper();

            InputStream is = LevelConfig.class
                    .getClassLoader()
                    .getResourceAsStream("levels.json");

            var root = mapper.readTree(is);

            return mapper.readValue(
                    root.get("levels").toString(),
                    mapper.getTypeFactory().constructCollectionType(List.class, LevelConfig.class)
            );

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Ошибка загрузки уровней");
        }
    }
    // Геттеры
    public int getLevelNumber() { return levelNumber; }
    public int getTotalFleas() { return totalFleas; }
    public List<GameObjectDto> getObjects() { return objects; }
}