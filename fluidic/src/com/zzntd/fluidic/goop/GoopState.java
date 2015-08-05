package com.zzntd.fluidic.goop;

import com.zzntd.fluidic.Utils;
import com.zzntd.fluidic.core.Component;
import com.zzntd.fluidic.core.Priority;
import com.zzntd.fluidic.goop.Enums.GoopType;
import com.zzntd.fluidic.physics.CollisionInterface;
import com.zzntd.fluidic.physics.PhysicsComponent.MoveDir;
import com.zzntd.fluidic.services.CollisionManagerInterface;

public abstract class GoopState extends Component implements CollisionInterface {
	protected int lastCollisionCount = 0;
	
	protected GoopPhysicsComponent goopParent;

	protected GoopState(GoopPhysicsComponent parent) {
		super(Priority.NONE);
		
		this.goopParent = parent;
	}
	
	public abstract void onRemove(GoopType nextType);
	public abstract GoopType getType();
	public abstract boolean validCollidable(CollisionManagerInterface collisionManager);

	protected void determineFalling(int collisionCount) {
		if (collisionCount == 0 && goopParent.currentSegment != null && goopParent.getMoveDir() != MoveDir.FALLING) {
			Utils.logger.debug("we're fallin'");
			goopParent.releaseCurrentSegment(false);
			goopParent.setMoveDir(MoveDir.FALLING);
		}
	}
	
	public void setLastCollisionCount(int c) {
		lastCollisionCount = c;
	}
}