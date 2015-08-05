/**
 * 
 */
package com.zzntd.fluidic.goop;


import junit.framework.Assert;

import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.badlogic.gdx.math.Vector2;
import com.zzntd.fluidic.Services;
import com.zzntd.fluidic.goop.Enums.GoopType;
import com.zzntd.fluidic.physics.AbstractPhysicsComponent;
import com.zzntd.fluidic.physics.Body;
import com.zzntd.fluidic.physics.PhysicsComponent;
import com.zzntd.fluidic.services.CollisionManager;
import com.zzntd.fluidic.services.CollisionManagerInterface;

/**
 * @author toofarapart
 *
 */
public class GoopPhysicsComponentTest {
	@Rule public JUnitRuleMockery context = new JUnitRuleMockery();

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		Services.provideCollisionManager(new CollisionManager());
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link com.zzntd.fluidic.goop.GoopPhysicsComponent#goopAllowed(com.zzntd.fluidic.physics.AbstractPhysicsComponent, com.zzntd.fluidic.services.CollisionManager)}.
	 */
	@Test
	public void testGoopAllowed() throws Exception {
		final CollisionManagerInterface collisionManager = context.mock(CollisionManagerInterface.class);
		GoopPhysicsComponent goopPhys = new GoopPhysicsComponent();
		GoopState state = new GoopState(goopPhys) {
			@Override
			public void velocityUpdate(float dt) {
			}
			
			@Override
			public boolean preCorrection(
					AbstractPhysicsComponent<? extends Body> collidableB) {
				return false;
			}
			
			@Override
			public void postCollision(CollisionManager collisionManager,
					int collisionCount) {
			}
			
			@Override
			public void onCollision(
					AbstractPhysicsComponent<? extends Body> collidableB,
					CollisionManager collisionManager) {
			}
			
			@Override
			public void update(float dt) {
			}
			
			@Override
			public boolean validCollidable(CollisionManagerInterface collisionManager) {
				return false;
			}
			
			@Override
			public void onRemove(GoopType nextType) {
			}
			
			@Override
			public GoopType getType() {
				return null;
			}
		};
		PhysicsComponent collidable = new PhysicsComponent();
		
		context.checking(new Expectations() {{
			oneOf (collisionManager).getStatus(); will(returnValue(CollisionManagerInterface.IS_ABOVE));
			oneOf (collisionManager).getCorrection(); will(returnValue(new Vector2(1, 0)));
			allowing (collisionManager).getStatus(); will(returnValue(CollisionManagerInterface.IS_BELOW));
			allowing (collisionManager).getCorrection(); will(returnValue(new Vector2(0, 0)));
		}});
		
		Assert.assertFalse(goopPhys.goopAllowed(null, collisionManager));
		Assert.assertFalse(goopPhys.goopAllowed(collidable, collisionManager));
		Assert.assertTrue(goopPhys.goopAllowed(collidable, collisionManager));
		
		goopPhys.setState(state);
		
		Assert.assertFalse(goopPhys.goopAllowed(collidable, collisionManager));
	}

}
