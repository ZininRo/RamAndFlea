import org.example.Physics.PhysicsType;
import org.example.Physics.PhysicsUserData;
import org.example.Physics.PhysicsWorldManager;
import org.jbox2d.dynamics.Body;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PhysicsTest {

    @Test
    void testPhysicsTypeEnum() {
        assertNotNull(PhysicsType.valueOf("FLEA"));
        assertNotNull(PhysicsType.valueOf("RAM"));
        assertNotNull(PhysicsType.valueOf("BLOCK"));
        assertNotNull(PhysicsType.valueOf("GROUND"));
        assertNotNull(PhysicsType.valueOf("WALL"));
        assertNotNull(PhysicsType.valueOf("CEILING"));
    }

    @Test
    void testPhysicsWorldManagerCreation() {
        PhysicsWorldManager manager = new PhysicsWorldManager();
        assertNotNull(manager.getWorld());
        assertDoesNotThrow(() -> manager.step()); // просто проверим, что нет ошибок
    }

    @Test
    void testCreateStaticBox() {
        PhysicsWorldManager manager = new PhysicsWorldManager();
        Body body = manager.createStaticBox(100, 100, 50, 50, PhysicsType.GROUND);
        assertNotNull(body);
        PhysicsUserData data = (PhysicsUserData) body.getUserData();
        assertEquals(PhysicsType.GROUND, data.type);
    }

    @Test
    void testCreateBlock() {
        PhysicsWorldManager manager = new PhysicsWorldManager();
        Body block = manager.createBlock(200, 300, 40, 40, true);
        assertNotNull(block);
        PhysicsUserData data = (PhysicsUserData) block.getUserData();
        assertEquals(PhysicsType.BLOCK, data.type);
        assertTrue(data.destructible);
    }

    @Test
    void testCreatePig() {
        PhysicsWorldManager manager = new PhysicsWorldManager();
        Body pig = manager.createRam(400, 400, 20, 8);
        assertNotNull(pig);
        PhysicsUserData data = (PhysicsUserData) pig.getUserData();
        assertEquals(PhysicsType.RAM, data.type);
        assertEquals(8f, data.hp);
    }

    @Test
    void testCreateBird() {
        PhysicsWorldManager manager = new PhysicsWorldManager();
        Body bird = manager.createFlea(100, 100, 12);
        assertNotNull(bird);
        PhysicsUserData data = (PhysicsUserData) bird.getUserData();
        assertEquals(PhysicsType.FLEA, data.type);
        assertEquals(0f, bird.getGravityScale());
    }

    @Test
    void testPlaceAndReleaseBird() {
        PhysicsWorldManager manager = new PhysicsWorldManager();
        Body bird = manager.createFlea(100, 100, 12);
        manager.placeFlea(bird, 150, 470);
        assertEquals(0f, bird.getGravityScale());
        manager.releaseFlea(bird, 30, 50);
        assertTrue(bird.getGravityScale() > 0);
        PhysicsUserData data = (PhysicsUserData) bird.getUserData();
        assertTrue(data.launched);
    }
}