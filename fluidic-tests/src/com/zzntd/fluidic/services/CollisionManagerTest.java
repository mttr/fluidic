package com.zzntd.fluidic.services;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.zzntd.fluidic.Services;
import com.zzntd.fluidic.core.ActorComposite;
import com.zzntd.fluidic.core.Priority;
import com.zzntd.fluidic.physics.AbstractPhysicsComponent;
import com.zzntd.fluidic.physics.PhysicsComponent;

public class CollisionManagerTest {
	private static class MockPhysicsComponent extends PhysicsComponent {
		public int status;
		public Array<Vector2> corrections;

		public MockPhysicsComponent(boolean active) {
			super(active);
			corrections = new Array<Vector2>();			
		}

		@Override
		public void onCollision(AbstractPhysicsComponent<?> collidableB,
				CollisionManager collisionManager) {
			status = collisionManager.getStatus();
			Vector2 newCorrection = new Vector2(collisionManager.getTotalCorrection());
			corrections.add(newCorrection);
		}

		@Override
		public void postCollision(CollisionManager collisionManager, int collisionCount) {

		}
	}
	static MockPhysicsComponent genCollidable(float x, float y, boolean active) {
		MockPhysicsComponent collidable = new MockPhysicsComponent(active);
		ActorComposite actor = new ActorComposite(Priority.NONE);
		
		actor.rec.width = 1f;
		actor.rec.height = 1f;
		actor.position.set(x, y);
		actor.rec.setPosition(actor.position);
		
		collidable.addTo(actor);
		
		return collidable;
	}

	private CollisionManager collisionMan;
	
	@Before
	public void setUp() {
		collisionMan = new CollisionManager();
		Services.provideCollisionManager(collisionMan);
	}	
	
	private boolean testDir(float px, float py, float cx, float cy, int flags) {
		MockPhysicsComponent active = genCollidable(px, py, true);
		genCollidable(cx, cy, false);
		
		collisionMan.update(0);
		
		return (active.status & flags) == flags;
	}

	@Test
	public void testIsRight() {
		assertTrue(testDir(0, 0, 0.8f, 0f, CollisionManager.IS_RIGHT));
	}
	
	@Test
	public void testIsLeft() {
		assertTrue(testDir(1f, 0, 0.2f, 0f, CollisionManager.IS_LEFT));
	}
	
	@Test
	public void testIsUp() {
		assertTrue(testDir(0, 0, 0, 0.8f, CollisionManager.IS_ABOVE));
	}
	
	@Test
	public void testIsDown() {
		assertTrue(testDir(0, 1f, 0, 0.2f, CollisionManager.IS_BELOW));
	}
}
