package com.zzntd.fluidic.physics;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.XmlReader.Element;
import com.zzntd.fluidic.ListenerType;
import com.zzntd.fluidic.Services;
import com.zzntd.fluidic.Utils;
import com.zzntd.fluidic.core.ActorComposite;
import com.zzntd.fluidic.core.Priority;
import com.zzntd.fluidic.services.CollisionManager;
import com.zzntd.fluidic.services.CollisionManagerInterface;

public class PhysicsComponent extends AbstractPhysicsComponent<RectangleBody> implements PhysicsResponse {
	public enum MoveDir {
		NONE,
		LEFT,
		RIGHT,
		FALLING;
		
		public static MoveDir opposite(MoveDir m) {
			switch(m) {
			case LEFT: return RIGHT;
			case RIGHT: return LEFT;
			case FALLING: return FALLING;
			default: return NONE;
			}
		}
	}
	
    static float MAX_VELOCITY = 5f;
    static float JUMP_VELOCITY = 40f;
    static float DAMPING = 0.90f;
    static float ACCELERATION = 1f;
    public static float GRAVITY = 1f;
    
	public Vector2 acceleration;
	public Vector2 gravity;
	
	public float rotation;
	protected MoveDir moveDir;
	
	public PhysicsComponent() {
		super(Priority.MID);
		
		acceleration = new Vector2();		
		gravity = new Vector2(0, -GRAVITY);
		moveDir = MoveDir.NONE;
	}
	
	public PhysicsComponent(boolean active) {
		this();
		
		isActive = active;
	}

	@Override
	public void physicsUpdate(float dt) {
		rotateParent(); // FIXME This shouldn't be a physicsComponent thing.

		acceleration.rotate(rotation);
		
		applyMovement();
		velocity.add(gravity);
		
		Services.getCamera().position.set(parent.position, 0); // FIXME Should actually go to InputComponent
		
		acceleration.set(0, 0);
	}

	protected void applyMovement() {
		velocity.add(acceleration);
	}

	@Override
	public void velocityUpdate(float dt) {
		velocity.scl(dt);
		velocity.clamp(0, MAX_VELOCITY);
		
		parent.position.add(velocity);
		parent.updateRec();
		body.update(dt);
		
		velocity.scl(DAMPING);
		
		velocity.scl(1 / dt);
	}

	private void rotateParent() {
		if (!Utils.floatEqual(acceleration.len(), 0)) {
			switch (moveDir) {
			case LEFT: parent.angle += 5; break;
			case RIGHT: parent.angle -= 5; break;
			default: break;
			}
		}
	}

	@Override
	public void addTo(ActorComposite parent) {
		super.addTo(parent);
		
		body.set(parent.rec);
		parent.putListener(ListenerType.PhysicsResponse, this);
	}

	@Override
	public void onCollision(AbstractPhysicsComponent<? extends Body> collidableB,
			CollisionManager collisionManager) {
	}
	
	protected boolean hasLanded(CollisionManagerInterface collisionManager) {
		if (moveDir == MoveDir.FALLING) {
			Utils.logger.debug("hasLanded(): FALLING");
			int status = collisionManager.getStatus();
			
			if ((status & CollisionManager.IS_BELOW) != CollisionManager.IS_BELOW) {
				return false;
			}
			else {
				onLanding();
			}
		}
		return true;
	}

	public void onLanding() {
		moveDir = MoveDir.NONE;
	}
	
	/**
	 * Called after all collisions have been determined for this object.
	 * Keep in mind that it's possible for this to be called even if there
	 * was no collision this tick.
	 * @param collisionManager
	 */
	@Override
	public void postCollision(CollisionManager collisionManager, int collisionCount) {
	}

	public MoveDir getMoveDir() {
		return moveDir;
	}

	public void setMoveDir(MoveDir moveDir) {
		this.moveDir = moveDir;
	}

	@Override
	public void onUp() {
		acceleration.y = ACCELERATION;
	}

	@Override
	public void onDown() {
		acceleration.y = -ACCELERATION;
	}

	@Override
	public void onLeft() {
		acceleration.x = -ACCELERATION;
		if (moveDir != MoveDir.FALLING)	moveDir = MoveDir.LEFT;
	}

	@Override
	public void onRight() {
		acceleration.x = ACCELERATION;
		if (moveDir != MoveDir.FALLING)	moveDir = MoveDir.RIGHT;
	}

	@Override
	public void fromXml(Element compDef, ActorComposite parent) {
		isActive = compDef.getBoolean("active");
		isSolid = compDef.getBoolean("solid", true);
		
		super.fromXml(compDef, parent);
	}

	@Override
	protected void createBody() {
		body = new RectangleBody(this);
	}
}
