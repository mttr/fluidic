package com.zzntd.fluidic.goop;

import com.badlogic.gdx.math.Vector2;
import com.zzntd.fluidic.goop.Enums.GoopType;
import com.zzntd.fluidic.physics.AbstractPhysicsComponent;
import com.zzntd.fluidic.physics.Body;
import com.zzntd.fluidic.physics.PhysicsComponent.MoveDir;
import com.zzntd.fluidic.services.CollisionManager;
import com.zzntd.fluidic.services.CollisionManagerInterface;

public class WallGoop extends GoopState {
	private static final GoopType type = GoopType.STICKY;
	private boolean hasCollided = false;

	protected WallGoop(GoopPhysicsComponent parent) {
		super(parent);
	}

	@Override
	public void onRemove(GoopType nextState) {
		if (nextState != GoopType.STICKY && nextState != GoopType.BRIDGE) {
			goopParent.resetGravity();
		}
	}

	@Override
	public void update(float dt) {
		// TODO Auto-generated method stub

	}

	@Override
	public void postCollision(CollisionManager collisionManager, int collisionCount) {
		if (!hasCollided) return;

		hasCollided = false;
		
		Vector2 correction = collisionManager.getTotalCorrection();
		
		if (!correction.equals(Vector2.Zero)) {
			correction.scl(-1);

			goopParent.gravByVec(correction);
		}
		
		// If there's no correction, we're falling.
		else {
			if (goopParent.getMoveDir() == MoveDir.LEFT) {
				goopParent.adjustRotation(90f);
			}
			else if (goopParent.getMoveDir() == MoveDir.RIGHT) {
				goopParent.adjustRotation(-90f);
			}
		}
	}

	@Override
	public GoopType getType() {
		return type;
	}

	@Override
	public boolean validCollidable(CollisionManagerInterface collisionManager) {
		return true;
	}

	@Override
	public void onCollision(
			AbstractPhysicsComponent<? extends Body> collidableB,
			CollisionManager collisionManager) {
		hasCollided = true;
		// TODO Auto-generated method stub
		
	}

	@Override
	public void velocityUpdate(float dt) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean preCorrection(
			AbstractPhysicsComponent<? extends Body> collidableB) {
		// TODO Auto-generated method stub
		return false;
	}

}
