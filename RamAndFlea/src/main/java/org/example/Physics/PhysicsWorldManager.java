package org.example.Physics;

import org.jbox2d.callbacks.*;
import org.jbox2d.collision.Manifold;
import org.jbox2d.collision.shapes.*;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.*;
import org.jbox2d.dynamics.contacts.Contact;
import java.util.*;

/**
 * Управляет физическим миром JBox2D: создание объектов, шаг симуляции, обработка коллизий.
 * Все координаты задаются в пикселях, внутренний движок работает в метрах (коэффициент PPM).
 */
public class PhysicsWorldManager {
    // Константы перевода координат
    public static final float PPM = 30f;
    public static final float PLAYFIELD_HEIGHT = 550f;

    // Физический мир и списки для удаления
    private final World world = new World(new Vec2(0, -10f));
    private final List<Body> bodiesToRemove = new ArrayList<>();
    private final List<Body> removedFleas = new ArrayList<>();

    /**
     * Создаёт менеджер физики с гравитацией 10 м/с² вниз.
     */
    public PhysicsWorldManager() {
        world.setContactListener(new GameContactListener());
    }
    /**
     * Возвращает ссылку на физический мир (для доступа к телам).
     */
    public World getWorld() { return world; }

    /**
     * Возвращает список снарядов, удалённых за последний шаг, и очищает внутренний список.
     */
    public List<Body> consumeRemovedFleas() {
        List<Body> copy = new ArrayList<>(removedFleas);
        removedFleas.clear();
        return copy;
    }
    /**
     * Выполняет один шаг симуляции и уничтожает тела, помеченные на удаление.
     */
    public void step() {
        world.step(1f / 60f, 8, 3);
        bodiesToRemove.forEach(world::destroyBody);
        bodiesToRemove.clear();
    }
    /**
     * Переводит экранные координаты в мировые (метры).
     */
    private Vec2 toWorld(float x, float y) {
        return new Vec2(x / PPM, (PLAYFIELD_HEIGHT - y) / PPM);
    }
    /**
     * Создаёт статическое прямоугольное тело (стена, пол, потолок).
     * @param x     координата центра по X (пиксели)
     * @param y     координата центра по Y (пиксели)
     * @param w     ширина (пиксели)
     * @param h     высота (пиксели)
     * @param type  тип объекта (GROUND, WALL, CEILING)
     * @return      созданное тело
     */
    public Body createStaticBox(float x, float y, float w, float h, PhysicsType type) {
        Body body = createBody(BodyType.STATIC, x, y);
        createBoxFixture(body, w, h, 0.8f, 0f, 0);
        body.setUserData(new PhysicsUserData(type, false));
        return body;
    }
    /**
     * Добавляет круглую фикстуру к телу.
     * @param x     координата центра по X (пиксели)
     * @param y     координата центра по Y (пиксели)
     * @param w     ширина (пиксели)
     * @param h     высота (пиксели)
     * @param destructible  разрушаемость блока
     * @return      созданное тело
     */
    public Body createBlock(float x, float y, float w, float h, boolean destructible) {
        Body body = createBody(BodyType.DYNAMIC, x, y);
        body.setBullet(false);
        body.setLinearDamping(0.8f);
        body.setAngularDamping(0.2f);
        createBoxFixture(body, w, h,
                1.5f, 0f,
                destructible ? 0.6f : 1f);
        resetMotion(body);
        body.setUserData(new PhysicsUserData(PhysicsType.BLOCK, destructible));
        body.setSleepingAllowed(true);
        return body;
    }
    /**
     * Создаёт барана (основной разрушаемый персонаж)
     * @param x     координата центра по X (пиксели)
     * @param y     координата центра по Y (пиксели)
     * @param r     радиус
     * @param hp    жизни
     * @return      созданное тело
     */
    public Body createRam(float x, float y, float r, float hp) {
        Body body = createBody(BodyType.DYNAMIC, x, y);
        body.setBullet(true);
        body.setLinearDamping(0.2f);
        body.setAngularDamping(0.6f);
        createCircleFixture(body, r, 4f, 0.6f, 0.1f);
        resetMotion(body);
        body.setUserData(new PhysicsUserData(PhysicsType.RAM, false, hp));
        body.setSleepingAllowed(true);
        return body;
    }
    /**
     * Создаёт блоху (снаряд)
     * @param x     координата центра по X (пиксели)
     * @param y     координата центра по Y (пиксели)
     * @param r     радиус
     * @return      созданное тело
     */
    public Body createFlea(float x, float y, float r) {
        Body body = createBody(BodyType.DYNAMIC, x, y);
        body.setBullet(true);
        body.setGravityScale(0f);
        body.setLinearDamping(0.05f);
        body.setAngularDamping(0.2f);
        createCircleFixture(body, r, 4f, 0.4f, 0.3f);
        body.setUserData(new PhysicsUserData(PhysicsType.FLEA, false));
        return body;
    }
    /**
     * Помещает снаряд в рогатку.
     */
    public void placeFlea(Body flea, float x, float y) {
        flea.setType(BodyType.KINEMATIC);
        flea.setTransform(toWorld(x, y), 0);
        resetMotion(flea);
        flea.setGravityScale(0);
        flea.setAwake(true);
        PhysicsUserData d = (PhysicsUserData) flea.getUserData();
        if (d != null) d.launched = false;
    }
    /**
     * Запускает снаряд с заданным импульсом.
     */
    public void releaseFlea(Body flea, float dx, float dy) {
        flea.setType(BodyType.DYNAMIC);
        flea.setGravityScale(3f);
        flea.setAwake(true);
        flea.applyLinearImpulse(
                new Vec2(dx / PPM * 3f, dy / PPM * 3f),
                flea.getWorldCenter()
        );
        PhysicsUserData d = (PhysicsUserData) flea.getUserData();
        if (d != null) d.launched = true;
    }

    /**
     * Помещает снаряд в рогатку (без физики).
     */
    private Body createBody(BodyType type, float x, float y) {
        BodyDef def = new BodyDef();
        def.type = type;
        def.position.set(toWorld(x, y));
        return world.createBody(def);
    }
    /**
     * Создаёт прямоугольный fixture.
     */
    private void createBoxFixture(Body body, float w, float h, float friction, float restitution, float density) {
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(w / 2f / PPM, h / 2f / PPM);
        FixtureDef fd = new FixtureDef();
        fd.shape = shape;
        fd.friction = friction;
        fd.restitution = restitution;
        if (density > 0) fd.density = density;
        body.createFixture(fd);
    }
    /**
     * Создаёт круглый fixture.
     */
    private void createCircleFixture(Body body, float r, float density, float friction, float restitution) {
        CircleShape shape = new CircleShape();
        shape.m_radius = r / PPM;
        FixtureDef fd = new FixtureDef();
        fd.shape = shape;
        fd.density = density;
        fd.friction = friction;
        fd.restitution = restitution;
        body.createFixture(fd);
    }
    /**
     * Обнуляет линейную и угловую скорости тела.
     */
    private void resetMotion(Body body) {
        body.setLinearVelocity(new Vec2());
        body.setAngularVelocity(0);
    }
    /**
     * Слушатель контактов JBox2D, обрабатывающий столкновения
     * и наносящий урон разрушаемым объектам.
     */
    private class GameContactListener implements ContactListener {
        @Override
        public void beginContact(Contact c) {}
        @Override
        public void endContact(Contact c) {}
        @Override
        public void preSolve(Contact c, Manifold m) {}
        @Override
        public void postSolve(Contact c, ContactImpulse impulse) {
            Body a = c.getFixtureA().getBody();
            Body b = c.getFixtureB().getBody();
            float impact = a.getLinearVelocity().sub(b.getLinearVelocity()).length();
            if (impact < 1f) return;
            handle(a, b, impact);
            handle(b, a, impact);
        }
        /**
         * Вычисляет урон на основе массы и квадрата скорости удара.
         */
        private float dmg(Body b, float v) {
            return b.getMass() * v * v * 0.009f;
        }
        /**
         * Обрабатывает столкновение пары тел (один из участников).
         * @param self  первое тело (участник столкновения, для которого вычисляется урон)
         * @param other второе тело (партнёр по столкновению)
         * @param v относительная скорость в момент контакта
         */
        private void handle(Body self, Body other, float v) {
            if (!(self.getUserData() instanceof PhysicsUserData s)) return;
            if (!(other.getUserData() instanceof PhysicsUserData o)) return;
            if (s.type == PhysicsType.FLEA || s.activated) o.activated = true;
            if (s.type == PhysicsType.RAM) {
                if (v < 0.5f) return;
                float d = dmg(self, v);
                if (o.type == PhysicsType.FLEA) d *= 0.7f;
                else if (o.type == PhysicsType.BLOCK)
                    d *= o.destructible ? (v < 0.5f ? 0.01f : 1f) : 2f;
                else if (o.type == PhysicsType.GROUND) d *= 4f;
                s.hp -= d;
                if (!s.dead && s.hp <= 0) {
                    s.dead = true;
                    resetMotion(self);
                    new Thread(() -> {
                        try { Thread.sleep(300); } catch (Exception ignored) {}
                        bodiesToRemove.add(self);
                    }).start();
                }
                return;
            }
            if (s.type == PhysicsType.BLOCK && s.destructible) {
                float d = dmg(other, v);
                if (o.type == PhysicsType.FLEA || o.type == PhysicsType.RAM) d *= 6f;
                if ((s.hp -= d) <= 0) bodiesToRemove.add(self);
            }
            if (s.type == PhysicsType.FLEA && s.launched) {
                if (o.type == PhysicsType.WALL || o.type == PhysicsType.CEILING || o.type == PhysicsType.GROUND
                        || self.getLinearVelocity().length() < 4f) {
                    bodiesToRemove.add(self);
                    removedFleas.add(self);
                }
            }
        }
    }
}