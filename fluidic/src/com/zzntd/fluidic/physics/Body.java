package com.zzntd.fluidic.physics;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.zzntd.fluidic.core.Component;
import com.zzntd.fluidic.core.Priority;

public abstract class Body extends Component {
	AbstractPhysicsComponent<? extends Body> parent;
	
	public Body(AbstractPhysicsComponent<? extends Body> parent) {
		super(Priority.NONE);
		this.parent = parent;
	}

	public abstract void determineCorrection(Body other,
			Rectangle intersection, Vector2 correction);
	public abstract void setPosition(Vector2 position);
	public abstract void set(final Rectangle rec);
	public abstract float[] getVerts();
	
	// FIXME Will probably set this as deprecated at some point.
	public abstract Rectangle getBoundingRectangle();
	public abstract boolean overlaps(Body bodyB);
}
