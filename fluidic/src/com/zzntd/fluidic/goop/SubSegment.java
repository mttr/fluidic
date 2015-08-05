package com.zzntd.fluidic.goop;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool.Poolable;
import com.zzntd.fluidic.Services;
import com.zzntd.fluidic.Utils;
import com.zzntd.fluidic.core.ActorComposite;
import com.zzntd.fluidic.core.Priority;
import com.zzntd.fluidic.goop.Enums.CollidableType;
import com.zzntd.fluidic.goop.Enums.GoopType;
import com.zzntd.fluidic.goop.GoopSegment.Direction;
import com.zzntd.fluidic.physics.PhysicsComponent;
import com.zzntd.fluidic.physics.PhysicsComponent.MoveDir;
import com.zzntd.fluidic.physics.RectangleBody;

public class SubSegment extends ActorComposite implements Poolable {
		public final Vector2 origin = new Vector2();
		public final Vector2 endPoint = new Vector2();
		public final Vector2 outward = new Vector2();
		private GoopType type = GoopType.NONE;
		private boolean outwardFinalized = false;
		public Direction direction = Direction.NONE;
		public MoveDir moveDir = MoveDir.NONE;
		public PhysicsComponent attachedCollidable;
		
		private final Vector2 lastPosition = new Vector2();
		
		FluidicPhysicsComponent bridgeCollidable = null;
		
		public SubSegment() {
			super(Priority.LOW);
		}
		
		public SubSegment(Vector2 origin, Vector2 endPoint, Vector2 outward, PhysicsComponent collidable, GoopType type, MoveDir moveDir) {
			super(Priority.MID);
			
			initialize(origin, endPoint, outward, collidable, type, MoveDir.NONE);
		}

		@Override
		public void reset() {
			origin.set(Vector2.Zero);
			endPoint.set(Vector2.Zero);
			setType(GoopType.NONE);
			direction = Direction.NONE;
			
			releaseCollidable();
		}

		private void releaseCollidable() {
			if (bridgeCollidable != null) {
				// FIXME Once pooled, proper release here.
				Utils.logger.debug("releaseCollidable");
				Services.getCollisionManager().remove(bridgeCollidable);
				bridgeCollidable = null;
			}
		}

		// FIXME Uses arbitrary SubSegment. What if this SubSegment isn't anywhere CLOSE to us?
		void adjustConnector(SubSegment sub) {
			GoopSegment.adjustCorner(endPoint, sub.endPoint);
			origin.set(sub.endPoint);
		}

		SubSegment initialize(final Vector2 origin, final Vector2 endPoint, Vector2 outward, PhysicsComponent collidable, GoopType type, MoveDir move) {
			Utils.logger.debug("SubSegment initialized: " + this.toString());

			this.origin.set(origin);
			this.endPoint.set(endPoint);
			this.setType(type);
			moveDir = move;

			this.outward.set(outward);
			this.attachedCollidable = collidable;
			
			releaseCollidable();
			
			if (collidable.isActive) {
				addTo(collidable.getParent());
				collidable.getParent().sort();
			}
			return this;
		}

		public boolean checkCollision(RectangleBody body) {
			return body.intersectsSegment(origin, endPoint);
		}

		public void draw(Texture goopTexture, SpriteBatch batch) {
			TextureRegion region = Utils.regionPool.obtain();
			region.setRegion(goopTexture);
			
			// FIXME obvious stuff
			int angle = (int) Math.round(outward.angle());
//			float rotation = outward.angle() + 90f;

			float width = ((angle) % 180 == 0) ? 0.5f : endPoint.x - origin.x;
			float height = ((angle) % 180 == 0) ? endPoint.y - origin.y : 0.5f;
			float x = origin.x - (0.5f * (angle % 270) / 180);
			float y = origin.y - (0.5f * (angle / 270));
			
			// FIXME Shouldn't even BE here.
			if (getType() == GoopType.BRIDGE) {
				updateBridgeCollidable(width, height, x, y);
			}

			batch.draw(region, 
					   x, 
					   y, 
//					   x + width / 2f, 
//					   y + height / 2f, 
					   width,
					   height
//					   1f, 1f,
//					   rotation
					   );

			Utils.regionPool.free(region);
		}

		private void updateBridgeCollidable(float width, float height, float x,
				float y) {
			if (bridgeCollidable == null) {
				// FIXME Should be pooled.
				bridgeCollidable = new FluidicPhysicsComponent();
				bridgeCollidable.isActive = false;
				bridgeCollidable.isSolid = true;
				bridgeCollidable.setCollidableType(CollidableType.BRIDGE);
				bridgeCollidable.addTo(this);
			}

			rec.set(x, y, width, height);
			bridgeCollidable.body.set(rec);
		}
		
		public float dst2() {
			return origin.dst2(endPoint);
		}
		
		public float dst() {
			return origin.dst(endPoint);
		}

		@Override
		public void update(float dt) {
			assert (bridgeCollidable != attachedCollidable && attachedCollidable != null) : "Attached collidable can't be bridge";
			ActorComposite parent = (ActorComposite) this.parent;
			
			if (parent != null && attachedCollidable.isActive) {
				if (lastPosition.equals(Vector2.Zero)) {
					lastPosition.set(parent.position);
					return;
				}

				final Vector2 deltaPosition = Utils.vectorPool.obtain();
				
				deltaPosition.set(parent.position).sub(lastPosition);
				adjustPosition(deltaPosition);
				
				Utils.vectorPool.free(deltaPosition);
				
				lastPosition.set(parent.position);
			}

			if (getType() == GoopType.BRIDGE) {
				int angle = (int) Math.round(outward.angle());
//				float rotation = outward.angle() + 90f;

				float width = ((angle) % 180 == 0) ? 0.5f : endPoint.x - origin.x;
				float height = ((angle) % 180 == 0) ? endPoint.y - origin.y : 0.5f;
				float x = origin.x - (0.5f * (angle % 270) / 180);
				float y = origin.y - (0.5f * (angle / 270));

				updateBridgeCollidable(width, height, x, y);
			}
		}
		
		private void adjustPosition(Vector2 delta) {
			endPoint.add(delta);
			origin.add(delta);
		}

		public GoopType getType() {
			return type;
		}

		public void setType(GoopType type) {
			if (this.type == GoopType.BRIDGE && type != GoopType.BRIDGE) {
				releaseCollidable();
			}
			this.type = type;
		}
		
		public void setOutward(Vector2 v) {
			if (!outwardFinalized) outward.set(v);
		}

		public boolean isOutwardFinalized() {
			return outwardFinalized;
		}

		public void setOutwardFinalized(boolean outwardFinalized) {
			if (!outwardFinalized && this.outwardFinalized) {
				Utils.logger.debug("outwardfinalized: true->false");
			}
			this.outwardFinalized = outwardFinalized;
		}
	}