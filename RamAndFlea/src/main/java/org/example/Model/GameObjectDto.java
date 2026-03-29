package org.example.Model;

import org.example.Physics.PhysicsType;
/**
 * DTO (Data Transfer Object) для описания игрового объекта в конфигурации уровня.
 * Содержит координаты, размеры, тип, разрушаемость и начальное здоровье.
 */
public class GameObjectDto {
    public int x, y, width, height;
    public PhysicsType type;
    public boolean destructible;
    public float hp = -1;
    public GameObjectDto() {}
}