package com.zzntd.fluidic.goop;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ObjectMap;
import com.zzntd.fluidic.FluidicGame;
import com.zzntd.fluidic.Services;
import com.zzntd.fluidic.Utils;
import com.zzntd.fluidic.goop.Enums.CollidableType;
import com.zzntd.fluidic.goop.Enums.GoopType;
import com.zzntd.fluidic.physics.AbstractPhysicsComponent;
import com.zzntd.fluidic.physics.PhysicsComponent;
import com.zzntd.fluidic.services.CollisionManager;
import com.zzntd.fluidic.services.CollisionManagerInterface;

public class GoopPhysicsComponent extends FluidicPhysicsComponent {
	static class GoopPhysicsComponentStates {
		private final ObjectMap<GoopType, GoopState> states = new ObjectMap<Enums.GoopType, GoopState>();
		private final GoopType[] stateList;
		
		private int index = 0;

		public GoopPhysicsComponentStates(GoopPhysicsComponent parent) {
			states.put(GoopType.NORMAL, new NormalGoop(parent));
			states.put(GoopType.STICKY, new WallGoop(parent));
			states.put(GoopType.BRIDGE, new BridgeGoop(parent));
			
			stateList = new GoopType[] { 
					GoopType.NORMAL, 
					GoopType.STICKY,
					GoopType.BRIDGE
			};
		}
		
		public GoopState get(GoopType key) {
			for (int i = 0; i < stateList.length; i++) {
				if (stateList[i] == key) index = i;
			}
			return states.get(key);
		}

		public GoopState next() {
			index = (index + 1) % stateList.length;
			return getCurrent();
		}

		private GoopState getCurrent() {
			return states.get(stateList[index]);
		}
		
		public GoopType nextType() {
			return stateList[(index + 1) % stateList.length];
		}
		
		public int size() {
			return stateList.length;
		}

		public boolean nextAvailableState(GoopPhysicsComponent goopPhysicsComponent) {
			if (FluidicGame.unlimitedGoop) {
				goopPhysicsComponent.setState(next());
				return true;
			} 
			else {
				int newIndex = nextAvailableIndex();
				
				if (newIndex >= 0) {
					index = newIndex;
					goopPhysicsComponent.setState(getCurrent());
					
					return true;
				}
				else return false;
			}
		}
		
		public GoopType nextAvailableType() {
			if (FluidicGame.unlimitedGoop) {
				return nextType();
			}
			else {
				int newIndex = nextAvailableIndex();
				
				if (newIndex >= 0) {
					return stateList[newIndex];
				}
				else return GoopType.NONE; 
			}
		}

		private int nextAvailableIndex() {
			int newIndex;

			for (int i = 0; i < size(); i++) {
				newIndex = (index + i + 1) % stateList.length;
				float typeRemaining = Services.getGoopManager().typeRemaining(stateList[newIndex]);
				
				if (typeRemaining >= 1f) {
					return newIndex;
				}
			}
			return -1;
		}
	}

	private GoopState state;
	private GoopPhysicsComponentStates states;
	GoopSegment currentSegment;
	PhysicsComponent lastCollidable;

	private final Vector2 outward = new Vector2();
	private GoopSegment lastCollidingSegment = null;
	private CollidableType collidableType;
	
	// FIXME Flag hell.
	private boolean typeSwap = false;
	private boolean transition = false;
	private boolean segmentSwap = false;private boolean lockMovement = false;
	private boolean bridgeRelease;

	public GoopPhysicsComponent() {
		super();
		
		states = new GoopPhysicsComponentStates(this);
		setState(states.get(GoopType.NORMAL));
		
		Services.getGoopManager().setGoopPhysics(this);
	}

	public GoopPhysicsComponent(boolean active) {
		super(active);
	}
	
	public boolean onNextGoopSelected() {
		GoopType nextType = states.nextAvailableType();
		Utils.logger.debug("Next type: " + nextType.toString());
		
		if (nextType != GoopType.NONE) {
			getState().onRemove(nextType);
			setState(states.get(nextType));
			typeSwap = true;
			
			if (moveDir == MoveDir.FALLING) {
				Services.getCollisionManager().clearStatus();
				bridgeRelease = true;
			}
			return true;
		}
		else return false;
	}

	private boolean goopRemaining(GoopType nextType) {
		return Services.getGoopManager().typeRemaining(nextType) >= 1f || FluidicGame.unlimitedGoop;
	}

	@Override
	public void update(float dt) {
		super.update(dt);
		getState().update(dt);
		
		checkLimits();	

		if (currentSegment != null) {
			currentSegment.update(dt);
		}
	}

	private void checkLimits() {
		// FIXME Need to straighten out FluidicGame.unlimitedGoop checks. 
		if (!goopRemaining(getState().getType())) {
			if (!lockMovement) {
				if (states.nextAvailableType() == GoopType.NONE) {
					Utils.logger.debug("Out of goop.");
					lockMovement = true;
				}
				else {
					Utils.logger.debug("Moar goop remaining.");
					onNextGoopSelected();
				}
			}
		}
		else if (FluidicGame.unlimitedGoop) {
			lockMovement = false;
		}
	}

	@Override
	protected void applyMovement() {
		if (!lockMovement) {
			super.applyMovement();
		}
	}

	@Override
	public void postCollision(CollisionManager collisionManager, int collisionCount) {
		getState().postCollision(collisionManager, 0);
		if (collisionCount < 2 && transition) {
			transition = false;
			setOutward(collisionManager.getTotalCorrection());
		}
		
		if (currentSegment != null) {
			currentSegment.overrideOutward(outward);
		}
		
		getState().determineFalling(collisionCount);
		getState().setLastCollisionCount(collisionCount);
	}

	@Override
	public void onCollision(AbstractPhysicsComponent<?> collidableB,
			CollisionManager collisionManager) {

		getState().onCollision(collidableB, collisionManager);
		setOutward(collisionManager.getCorrection());
		
		if (!goopAllowed(collidableB, collisionManager)) return;

		PhysicsComponent collidable = (PhysicsComponent) collidableB;
		
		GoopSegment newSeg = Services.getGoopManager().getOverlappingEndPoints(this);

		if (currentSegment != null) {
			if (!bridgeCheck(collidable)) return;

			// If we're colliding with another segment, release and wait until we're only colliding with one again.
			if (newSeg != null) {
				releaseCurrentSegment(true);
			}
			else {
				determineSegmentExpansion(collidable, true);
			}
		}
		else if (segmentSwap) {
			if (!handleSegmentSwap(collidable, newSeg)) return;
		}
		else {
			if (newSeg != null) return;

			attachCollidingSegment();
			if (currentSegment == null) {
				// FIXME attachSegment doesn't work with this, but I want to get rid of direct currentSegment assignment.
				currentSegment = new GoopSegment(this, Utils.getProjectedVertices(collidable.body.poly, body.poly), outward, collidable);
			}
		}
		lastCollidable = collidable;
	}

	private boolean bridgeCheck(PhysicsComponent collidable) {
		// FIXME I don't have any idea wtf is going on here, but it's important.
		if (getState().getType() == GoopType.BRIDGE) {
			if (!currentSegment.isBridgeCollidable(collidable) && currentSegment.getMoveDir() != MoveDir.NONE) {
				return false;
			} else {
				lastCollidable = collidable;
			}
		}
		return true;
	}

	boolean goopAllowed(AbstractPhysicsComponent<?> collidable,
			CollisionManagerInterface collisionManager) {
		
		return PhysicsComponent.class.isInstance(collidable) && hasLanded(collisionManager)
				&& getState().validCollidable(collisionManager);
	}

	private boolean handleSegmentSwap(PhysicsComponent collidable, GoopSegment newSeg) {
		if (newSeg != null) {
			lastCollidingSegment = newSeg;
			return false;
		}
		
		if (attachCollidingSegment()) {
			currentSegment.swapSegment(this); // FIXME Redundant code AA
			determineSegmentExpansion(collidable, false);
			segmentSwap = false;
		}
		else if (lastCollidingSegment != null) {
			attachSegment(lastCollidingSegment); // FIXME Redundant code AB
			currentSegment.swapSegment(this);
			determineSegmentExpansion(collidable, false);
			lastCollidingSegment = null;
		}
		return true;
	}

	@Override
	public void onLanding() {
		super.onLanding();
		rotation = 0;
		Utils.logger.debug("hasLanded() -> true");
	}
	
	private void setOutward(Vector2 correction) {
		outward.set(correction).nor().scl(-1);
	}

	private boolean attachCollidingSegment() {
		GoopSegment newSeg = Services.getGoopManager().getCollision(this);
		if (newSeg != null) {
			attachSegment(newSeg);
			return true;
		}
		return false;
	}

	private void attachSegment(GoopSegment newSeg) {
		if (currentSegment != null) {
			releaseCurrentSegment(false);
		}
		currentSegment = newSeg;
		currentSegment.connect(this);
		typeSwap = false;
	}

	public void releaseCurrentSegment(boolean swap) {
		segmentSwap = swap;
		currentSegment.onDisconnect();
		
		// FIXME This is where I free the poolable segment object.
		float totalLength = currentSegment.totalLength();

		if (Utils.floatEqual(totalLength, 0, 0.1f)) {
			currentSegment.reset();
			Services.getGoopManager().remove(currentSegment);
		}
		currentSegment = null;
	}

	private void determineSegmentExpansion(PhysicsComponent collidable, boolean allowTransition) {
		if (typeSwap(collidable)) return;

		// FIXME Should expand be allowed if lastCollid = currentSegment.collid?
		if (!transition) {
			checkTransition(collidable, allowTransition);
			currentSegment.expand(moveDir, collidable, transition, outward);
		}
	}

	private void checkTransition(PhysicsComponent collidable,
			boolean allowTransition) {
		if (collidable != lastCollidable && lastCollidable != null && allowTransition) {
			Utils.logger.debug("transition = true");
			transition = true;
		}
	}

	private boolean typeSwap(PhysicsComponent collidable) {
		if (typeSwap && moveDir != MoveDir.FALLING) {
			currentSegment.typeSwap(Utils.getProjectedVertices(collidable.body.poly, body.poly), outward, collidable, bridgeRelease);
			typeSwap = false;
			bridgeRelease = false;
			return true;
		}
		return false;
	}
	
	public void setType(GoopType type) {
		setState(states.get(type));
	}

	public GoopType getType() {
		return getState().getType();
	}

	public void gravByVec(Vector2 v) {
		float last_rotation = gravity.angle();		
		float deltaRotation = v.angle() - last_rotation;
		adjustRotation(deltaRotation);
	}

	public void adjustRotation(float deltaRotation) {
		rotation += deltaRotation;
		
		gravity.rotate(deltaRotation);
		velocity.rotate(deltaRotation);
	}

	public void resetGravity() {
		gravity.set(0, -PhysicsComponent.GRAVITY);
	}

	public GoopSegment getSegment() {
		return currentSegment;
	}

	public CollidableType getCollidableType() {
		return collidableType;
	}

	public void setCollidableType(CollidableType collidableType) {
		this.collidableType = collidableType;
	}

	public Vector2 getOutward() {
		return outward;
	}

	GoopState getState() {
		return state;
	}

	void setState(GoopState state) {
		this.state = state;
	}
}
