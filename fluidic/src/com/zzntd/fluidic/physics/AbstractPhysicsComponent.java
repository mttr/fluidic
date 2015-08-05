package com.zzntd.fluidic.physics;

import com.badlogic.gdx.math.Vector2;
import com.zzntd.fluidic.Services;
import com.zzntd.fluidic.core.ActorComponent;
import com.zzntd.fluidic.core.Priority;

public abstract class AbstractPhysicsComponent<T extends Body> extends ActorComponent implements CollisionInterface {

	public T body;
	public boolean isActive;
	public boolean isSolid;
	public Vector2 velocity;

	public AbstractPhysicsComponent(Priority priority) {
		super(priority);
		
		//body = new Rectangle();
		velocity = new Vector2();
		isActive = false;		
		isSolid = true;
		
		// FIXME -- We should wait until we know we're out in the world
		Services.getCollisionManager().add(this);
		
		this.createBody();
		assert body != null : "createBody() must instantiate body";
	}

	public AbstractPhysicsComponent() {
		this(Priority.MID);
	}

	@Override
	public void update(float dt) {
		if (parent != null) {			
			if (isActive) {
				physicsUpdate(dt);
			}
			
			// FIXME I think this has its own method now.
			parent.rec.setPosition(parent.position);
			body.update(dt);
		}		
	}

	public abstract void physicsUpdate(float dt);
	protected abstract void createBody();

	public void velocityUpdate(float dt) {
	}

	@Override
	public boolean preCorrection(AbstractPhysicsComponent<? extends Body> collidableB) {
		return isSolid;
	}
}