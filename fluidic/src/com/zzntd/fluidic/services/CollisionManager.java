package com.zzntd.fluidic.services;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.DelayedRemovalArray;
import com.zzntd.fluidic.core.Component;
import com.zzntd.fluidic.core.Priority;
import com.zzntd.fluidic.physics.AbstractPhysicsComponent;
import com.zzntd.fluidic.physics.Body;

public class CollisionManager extends Component implements CollisionManagerInterface {
	
	// FIXME Can I overcome some of the GOTTA TYPE CHECK annoyances with generics?
	private DelayedRemovalArray<AbstractPhysicsComponent<? extends Body>> collidables;
	
	@Deprecated // FIXME Should be moved to RecBody
	private Rectangle intersection;
	
	private final Vector2 correction;
	private final Vector2 totalCorrection = new Vector2();
	private int status;
	
	public static Vector2 utilityVector = new Vector2();

	public CollisionManager() {
		super(Priority.NONE); // FIXME ??
		
		collidables = new DelayedRemovalArray<AbstractPhysicsComponent<? extends Body>>();
		intersection = new Rectangle();
		correction = new Vector2();
		status = 0;
	}

	@Override
	public void update(float dt) {
		// FIXME Parent might be null.
		
		collidables.begin();
		for (int i = 0; i < collidables.size; i++) {
			// FIXME For some reason using for each syntax breaks
			// the junit test........
			AbstractPhysicsComponent<? extends Body> collidable = collidables.get(i);
			
			if (collidable.isActive) {
				checkAgainstCollidable(collidable, dt);
			}
		}
		collidables.end();
	}

	/* (non-Javadoc)
	 * @see com.zzntd.fluidic.services.CollisionManagerInterface#checkAgainstCollidable(com.zzntd.fluidic.physics.AbstractPhysicsComponent, float)
	 */
	@Override
	public void checkAgainstCollidable(AbstractPhysicsComponent<? extends Body> collidable, float dt) {
		correction.set(0, 0);
		totalCorrection.set(0, 0);
		
		int collisions = 0;
		
		collidable.velocityUpdate(dt);
		
		for (AbstractPhysicsComponent<? extends Body> collidableB : collidables) {
			status = 0;
			if (collidable == collidableB) {
				continue;				
			}
			
			Body bodyA = collidable.body;
			Body bodyB = collidableB.body;
			
			// FIXME This gets a bit redundant now that we have more indirection.
			if (bodyA.overlaps(bodyB)) {
				if (collidableB.isSolid) {
					bodyA.determineCorrection(bodyB, intersection, correction);

					if (!correction.epsilonEquals(totalCorrection, 0.0001f) && collidable.preCorrection(collidableB)) {
						collisionCorrect(collidable);
					}
					collisions++;
				}

				collidable.onCollision(collidableB, this);
				collidableB.onCollision(collidable, this);						
			}
		}
		
		totalCorrection.x = Math.abs(totalCorrection.x) > Math.abs(totalCorrection.y) ? totalCorrection.x : 0;
		totalCorrection.y = Math.abs(totalCorrection.y) > Math.abs(totalCorrection.x) ? totalCorrection.y : 0;
		
		collidable.getParent().position.add(totalCorrection);
		collidable.getParent().updateRec();
		collidable.body.update(dt);
		collidable.postCollision(this, collisions);
	}

	private void collisionCorrect(AbstractPhysicsComponent<? extends Body> collidable) {
		totalCorrection.add(correction);
		collidable.getParent().updateRec();
	}

	public Array<AbstractPhysicsComponent<? extends Body>> getCollidables() {
		return collidables;
	}

	@Override
	public void add(AbstractPhysicsComponent<? extends Body> abstractPhysicsComponent) {
		collidables.add(abstractPhysicsComponent);
	}

	public boolean remove(AbstractPhysicsComponent<? extends Body> value) {
		return collidables.removeValue(value, true);
	}
	
	@Deprecated // FIXME Regular rectangular intersections are no longer guaranteed.
	public Rectangle getIntersection() {
		return intersection;
	}

	@Override
	public Vector2 getTotalCorrection() {
		return totalCorrection;
	}
	
	/* (non-Javadoc)
	 * @see com.zzntd.fluidic.services.CollisionManagerInterface#getStatus()
	 */
	@Override
	public int getStatus() {
		return status;
	}

	public void clear() {
		collidables.clear();
	}

	/* (non-Javadoc)
	 * @see com.zzntd.fluidic.services.CollisionManagerInterface#setStatus(int)
	 */
	@Override
	public void setStatus(int status) {
		this.status = status;
	}

	@Override
	public Vector2 getCorrection() {
		return correction;
	}

	/* (non-Javadoc)
	 * @see com.zzntd.fluidic.services.CollisionManagerInterface#clearStatus()
	 */
	@Override
	public void clearStatus() {
		status = 0;
	}
}
