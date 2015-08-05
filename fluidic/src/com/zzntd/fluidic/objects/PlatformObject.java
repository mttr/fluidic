package com.zzntd.fluidic.objects;

import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.math.Vector2;
import com.zzntd.fluidic.Utils;
import com.zzntd.fluidic.core.ActorComposite;
import com.zzntd.fluidic.core.Priority;
import com.zzntd.fluidic.physics.AbstractPhysicsComponent;
import com.zzntd.fluidic.physics.Body;
import com.zzntd.fluidic.physics.PhysicsComponent;
import com.zzntd.fluidic.physics.RectangleBody;
import com.zzntd.fluidic.services.CollisionManager;

public class PlatformObject extends ActorComposite {

	public PlatformObject() {
		super(Priority.MID);
	}

	@Override
	public void loadMapProperties(MapProperties properties) {
		final boolean pUp = !Boolean.parseBoolean(properties.get("up", "true", String.class));
		final float pDist = Float.parseFloat(properties.get("travel_distance", "4", String.class));

		PhysicsComponent phys = new PhysicsComponent() {
			private float elapsed = 0f;
			private float travelDistance = pDist;
			private boolean moving = false;
			private boolean up = pUp;
			private final Vector2 startPosition = new Vector2();

			@Override
			public void postCollision(CollisionManager collisionManager,
					int collisionCount) {
			}

			@Override
			public boolean preCorrection(
					AbstractPhysicsComponent<? extends Body> collidableB) {
				return false;
			}

			@Override
			public void onCollision(
					AbstractPhysicsComponent<? extends Body> collidableB,
					CollisionManager collisionManager) {
			}

			@Override
			public void physicsUpdate(float dt) {
				elapsed += dt;

				if (moving) {
					position.y += up ? 0.1f : -0.1f;
					
					float dst = position.dst(startPosition);
					// FIXME Should I just only have one?
					if (dst >= travelDistance || Utils.floatEqual(dst, travelDistance)) {
						moving = false;
						elapsed = 0;
					}
				}
				else {
					if (elapsed >= 3f) {
						moving = true;
						elapsed = 0;
						startPosition.set(position);
						up = !up;
					}
				}
			}

			@Override
			protected void createBody() {
				body = new RectangleBody(this);
			}
		};
		phys.isActive = true;
		phys.isSolid = true;

		phys.addTo(this);
	}
}
