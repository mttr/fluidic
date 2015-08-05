package com.zzntd.fluidic.physics;

import com.zzntd.fluidic.services.CollisionManager;

public interface CollisionInterface {

	public abstract void postCollision(CollisionManager collisionManager, int collisionCount);

	public boolean preCorrection(AbstractPhysicsComponent<? extends Body> collidableB);
	public abstract void onCollision(AbstractPhysicsComponent<? extends Body> collidableB,
			CollisionManager collisionManager);

	public abstract void velocityUpdate(float dt);
}