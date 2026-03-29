package org.example.Physics;
/**
 * Пользовательские данные, привязанные к физическому телу в JBox2D.
 * Хранит информацию о типе объекта, его состоянии (жив/мёртв, запущен ли снаряд)
 * и параметры прочности (здоровье, разрушаемость).
 */
public class PhysicsUserData {
    public PhysicsType type;
    public boolean destructible, launched;
    public boolean activated = false;
    public boolean dead = false;
    public float hp;

    /**
     * Конструктор с явным указанием здоровья.
     * @param type         тип объекта
     * @param destructible разрушаемый ли объект
     * @param hp           начальное здоровье (если > 0, используется заданное значение,
     *                     иначе устанавливается значение по умолчанию в зависимости от типа)
     */
    public PhysicsUserData(PhysicsType type, boolean destructible, float hp) {
        this.type = type;
        this.destructible = destructible;
        this.launched = false;
        if (hp > 0) {
            this.hp = hp;
        } else {
            switch (type) {
                case RAM -> this.hp = 8f;
                case BLOCK -> this.hp = 15f;
            }
        }
    }
    /**
     * Конструктор для объектов, у которых здоровье не требуется.
     * @param type         тип объекта
     * @param destructible разрушаемый ли объект
     */
    public PhysicsUserData(PhysicsType type, boolean destructible) {
        this.type = type;
        this.destructible = destructible;
        this.launched = false;
        this.hp = 15f;
    }
}