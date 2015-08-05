package com.zzntd.fluidic.physics;

import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.zzntd.fluidic.Utils;

public class CircleBody extends Body {
	private Circle circle;
	private Rectangle boundingRec;

	public CircleBody(AbstractPhysicsComponent<CircleBody> parent) {
		super(parent);
		circle = new Circle();
		boundingRec = new Rectangle();
	}

	@Override
	public void determineCorrection(Body other, Rectangle intersection,
			Vector2 correction) {
		// FIXME And the repetition begins...
		if (CircleBody.class.isInstance(other)) {
		}
		else if (RectangleBody.class.isInstance(other)) {
		}
		else {
			throw new RuntimeException("Unhandled collision type.");
		}
	}

	@Override
	public void setPosition(Vector2 position) {
		circle.setPosition(position);
	}

	@Override
	public void set(Rectangle rec) {
		assert Utils.floatEqual(rec.width, rec.height) : "Reference rec must have equal dimensions";
		circle.radius = rec.width / 2f;
		circle.x = rec.x + circle.radius;
		circle.y = rec.y + circle.radius;
		boundingRec.set(rec);
	}

	@Override
	public Rectangle getBoundingRectangle() {
		boundingRec.x = circle.x - circle.radius;
		boundingRec.y = circle.x - circle.radius;
		return boundingRec;
	}

	@Override
	public boolean overlaps(Body bodyB) {
		if (CircleBody.class.isInstance(bodyB)) {
			return circle.overlaps(((CircleBody)bodyB).circle);
		}
		else if (RectangleBody.class.isInstance(bodyB)) {
			return Intersector.overlaps(circle, ((RectangleBody)bodyB).rec);
		}
		else {
			throw new RuntimeException("Unhandled collision type.");
		}
	}

	@Override
	public float[] getVerts() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void update(float dt) {
		// TODO Auto-generated method stub
		
	}

}
