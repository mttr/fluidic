package com.zzntd.fluidic.goop;

import com.zzntd.fluidic.Utils;
import com.zzntd.fluidic.goop.Enums.GoopType;
import com.zzntd.fluidic.physics.AbstractPhysicsComponent;
import com.zzntd.fluidic.physics.Body;
import com.zzntd.fluidic.physics.PhysicsComponent.MoveDir;
import com.zzntd.fluidic.services.CollisionManager;

public class BridgeGoop extends WallGoop {
	protected BridgeGoop(GoopPhysicsComponent parent) {
		super(parent);
		// TODO Auto-generated constructor stub
	}

	@Override
	public GoopType getType() {
		return GoopType.BRIDGE;
	}

	@Override
	public void onCollision(
			AbstractPhysicsComponent<? extends Body> collidableB,
			CollisionManager collisionManager) {
		super.onCollision(collidableB, collisionManager);
	}

	@Override
	public void onRemove(GoopType nextState) {
		super.onRemove(nextState);
		
		if (lastCollisionCount == 1) {
			Utils.logger.debug("lastCollisionCount = 1; falling from bridge goop");
			goopParent.setMoveDir(MoveDir.FALLING);
		}
	}
}
