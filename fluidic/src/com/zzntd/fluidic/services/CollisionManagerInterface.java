package com.zzntd.fluidic.services;

import com.badlogic.gdx.math.Vector2;
import com.zzntd.fluidic.physics.AbstractPhysicsComponent;
import com.zzntd.fluidic.physics.Body;

public interface CollisionManagerInterface {

	public static final int IS_ABOVE = 1;
	public static final int IS_BELOW = 1 << 1;
	public static final int IS_LEFT = 1 << 2;
	public static final int IS_RIGHT = 1 << 3;
	public static final int IS_CENTER_HOR = 1 << 4;
	public static final int IS_CENTER_VER = 1 << 5;

	public abstract void checkAgainstCollidable(
			AbstractPhysicsComponent<? extends Body> collidable, float dt);

	public abstract int getStatus();

	public abstract void setStatus(int status);

	public abstract void clearStatus();

	public abstract void add(AbstractPhysicsComponent<? extends Body> abstractPhysicsComponent);

	public abstract Vector2 getTotalCorrection();

	public abstract Vector2 getCorrection();

}