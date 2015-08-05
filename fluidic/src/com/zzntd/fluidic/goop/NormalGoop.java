package com.zzntd.fluidic.goop;

import com.zzntd.fluidic.Utils;
import com.zzntd.fluidic.goop.Enums.GoopType;
import com.zzntd.fluidic.physics.AbstractPhysicsComponent;
import com.zzntd.fluidic.physics.Body;
import com.zzntd.fluidic.services.CollisionManager;
import com.zzntd.fluidic.services.CollisionManagerInterface;

public class NormalGoop extends GoopState {
	private static final GoopType type = GoopType.NORMAL;

	protected NormalGoop(GoopPhysicsComponent parent) {
		super(parent);
	}

	@Override
	public void postCollision(CollisionManager collisionManager,
			int collisionCount) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onCollision(
			AbstractPhysicsComponent<? extends Body> collidableB,
			CollisionManager collisionManager) {
		// TODO Auto-generated method stub

	}

	@Override
	public void update(float dt) {
		// TODO Auto-generated method stub

	}

	@Override
	public GoopType getType() {
		return type;
	}

	@Override
	public boolean validCollidable(CollisionManagerInterface collisionManager) {
		int status = collisionManager.getStatus();
		return Utils.floatEqual(collisionManager.getCorrection().x, 0) && Utils.checkFlags(status, CollisionManager.IS_BELOW);
	}
		

	@Override
	public void velocityUpdate(float dt) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onRemove(GoopType nextType) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean preCorrection(
			AbstractPhysicsComponent<? extends Body> collidableB) {
		// TODO Auto-generated method stub
		return false;
	}

}
