package com.zzntd.fluidic.goop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pool.Poolable;
import com.badlogic.gdx.utils.Pools;
import com.zzntd.fluidic.Services;
import com.zzntd.fluidic.Utils;
import com.zzntd.fluidic.core.Component;
import com.zzntd.fluidic.core.Priority;
import com.zzntd.fluidic.goop.Enums.GoopType;
import com.zzntd.fluidic.physics.PhysicsComponent;
import com.zzntd.fluidic.physics.PhysicsComponent.MoveDir;
import com.zzntd.fluidic.physics.RectangleBody;

public class GoopSegment extends Component implements Poolable {
	static final Pool<SubSegment> segmentPool = Pools.get(SubSegment.class);

	public enum Direction {
		NONE, VERTICAL, HORIZONTAL,
	}
	
	// Default
	
	// FIXME Replace temps with pooled vectors
	final Vector2 temp = new Vector2();
	final Vector2 temp2 = new Vector2();
	final Array<SubSegment> segments = new Array<SubSegment>(true, 100);
	final ExpansionHelper expansion = new ExpansionHelper();
	
	
	SubSegment currentSubSegment;
	GoopPhysicsComponent goopComponent;
	boolean released = false;
	PhysicsComponent lastCollidable;
	boolean reconnect = false;

	// Private

	private boolean swapDir = false;
	private MoveDir moveDir = MoveDir.NONE;
	
	public GoopSegment(GoopPhysicsComponent parent, Array<Vector2> array, Vector2 outward, PhysicsComponent collidable) {
		// FIXME Should be (x, y) -> Center, and expand xWidth from there.
		super(Priority.NONE);
		Utils.logger.debug("new GoopSegment");
		final Vector2 origin = temp;
		final Vector2 endPoint = temp2;

		origin.set(array.first());
		endPoint.set(array.peek()); // FIXME
		
		if (!Utils.axisAligned(origin, endPoint)) {
			final Vector2 corner = Utils.cornerInRec(collidable.body.getBoundingRectangle(), parent.body.getBoundingRectangle());
			
			if (origin.dst2(corner) < endPoint.dst2(corner)) {
				origin.set(corner);
			}
			else {
				endPoint.set(corner);
			}
		}

		this.goopComponent = parent;

		currentSubSegment = segmentPool.obtain();
		currentSubSegment.initialize(origin, endPoint, outward, collidable, parent.getType(), MoveDir.NONE);

		Services.getGoopManager().add(this);
	}
	
	public boolean isBridgeCollidable(PhysicsComponent collidable) {
		if (currentSubSegment.bridgeCollidable == null) return true; // FIXME doing this for a reason, but WTF?

		return collidable == currentSubSegment.bridgeCollidable;
	}
	
	GoopSegment() {
		super(Priority.NONE);
		Services.getGoopManager().add(this);
	}

	public void setOrigin(final Vector2 origin) {
		currentSubSegment.origin.set(origin);
	}
	
	public void setEndPoint(final Vector2 endPoint) {
		currentSubSegment.endPoint.set(endPoint);
	}
	
	public void expand(MoveDir move, PhysicsComponent collidable, boolean isNew, Vector2 outward) {
		expansion.expand(this, move, collidable, isNew, outward);
	}

	@Override
	public void update(float dt) {
		if (currentSubSegment == null) return;
	
		// FIXME might not be needed anymore.
		if (segments.size > 0 && !released) {
			SubSegment sub = segments.peek();
			currentSubSegment.adjustConnector(sub);
		}
		
		if (currentSubSegment.dst2() > 1 && !released) {
			released = true;
		}
		
		if (released && !currentSubSegment.isOutwardFinalized()) {
			currentSubSegment.setOutwardFinalized(true);
		}
		
		if (released && segments.size > 0) {
			SubSegment res = segments.first();
			if (Utils.collidableContains(goopComponent, res.origin)) {
				if (!swapDir) {
					reverseSegments(false);
				}
				return;
			}
			else if (swapDir) {
				swapDir = false;
				return;
			}
	
			res = getTouchingSegment();
			
			if (res != null) {
				releaseSegments(res);
			}
		}
	}

	public void typeSwap(Array<Vector2> array, Vector2 outward, PhysicsComponent collidable, boolean bridgeRelease) {
		GoopType type = goopComponent.getType();

		Utils.logger.debug("Type: " + type.toString());
		
		if (bridgeRelease) {
			Utils.logger.debug("typeSwap -> bridgeRelease: updateSubSegment (and release?)");
			Utils.closest(array, currentSubSegment.origin, currentSubSegment.endPoint);
			currentSubSegment.update(0f);
		}
		else if (currentSubSegment.dst() <= 1.0) {
			currentSubSegment.setType(type);
		}
		else {
			SubSegment newSegment = segmentPool.obtain();
			newSegment.initialize(Utils.closest(array, currentSubSegment.origin, temp),
					              Utils.farthest(array, currentSubSegment.origin, temp2), 
					              outward, collidable, type, MoveDir.NONE);

			currentSubSegment.endPoint.set(temp);
			
			segments.add(currentSubSegment);
		
			setCurrentSubSegment(newSegment);
			released = false;
		}
	}

	public GoopSegment swapSegment(GoopPhysicsComponent newPhys) {
		Utils.logger.debug("swapSeg");
		goopComponent = newPhys;
		
		SubSegment segment = getCollidingSegment(newPhys.body);
		
		if (segment == segments.first() && segment == segments.peek()) {
			if (newPhys.getMoveDir() == segment.moveDir) {
				reverseSegments(false);
			}
			else {
				setCurrentSubSegment(segments.pop());
			}
		}
		else if (segment == segments.first()) {
			reverseSegments(false);
		}
		else if (segment == segments.peek()) {
			setCurrentSubSegment(segments.pop());
		}
		else if (segments.size == 1) {
			// FIXME Let's just see what happens!
			final SubSegment sub = segments.peek();
			
			Array<Vector2> array = Utils.getCorners(newPhys.body.getBoundingRectangle());
			
			Utils.closest(array, sub.endPoint, temp);
			if (temp.dst2(sub.endPoint) < temp.dst2(sub.origin)) {
				reverseSegments(false);
			}
			else {
				setCurrentSubSegment(segments.pop());
			}
		}
		else {
			throw new RuntimeException("Can't swap to new segment if we're not colliding with an extremity.");
		}
		
		reconnect = false;
	
		return this;
	}

	public void connect(GoopPhysicsComponent goopPhysicsComponent) {
		goopComponent = goopPhysicsComponent;
		reconnect = true;
	}

	public void onDisconnect() {
		updateMoveDir();
		segments.add(currentSubSegment);
		currentSubSegment = null;
		goopComponent = null;
		
		if (totalLength() <= 1.1f) {
			// FIXME This is a bad solution.
			Utils.logger.debug("disconnecting with a length <= 1");
			reset();
			Services.getGoopManager().remove(this);
		}
	}

	public void reverseSegments(boolean swap) {
		if (currentSubSegment != null) {
			segments.add(currentSubSegment);
		}
		reverseUpdate(swap);
		setCurrentSubSegment(segments.pop());
		swapDir = true;
	}

	public boolean checkCollisions(RectangleBody body) {
		for (SubSegment segment : segments) {
			if (segment.checkCollision(body)) {
				return true;
			}
		}
		return false;
	}

	public SubSegment getCollidingSegment(RectangleBody body) {
		for (SubSegment segment : segments) {
			if (segment.checkCollision(body)) {
				return segment;
			}
		}
		return null;
	}

	/**
	 * Returns true if origin from first subsegment or endpoint of last subsegment
	 * are contained in collidable, as long as this segment is not active.
	 * @param goopPhysics
	 * @return
	 */
	public boolean extremityContainedBy(GoopPhysicsComponent collidable) {
		if (goopComponent != null && !reconnect) {
			if (segments.size == 0) {
				return Utils.collidableContains(collidable, getOrigin()) ||
						Utils.collidableContains(collidable, getEndPoint());
			}
			else {
				return Utils.collidableContains(collidable, segments.first().origin) ||
						Utils.collidableContains(collidable, currentSubSegment.endPoint);
			}
		}
	
		return Utils.collidableContains(collidable, segments.first().origin) || 
				Utils.collidableContains(collidable, segments.peek().endPoint);
	}

	public float totalLength() {
		float dist = 0;
		if (currentSubSegment != null) {
			dist += currentSubSegment.dst();
		}
		for (SubSegment segment : segments) {
			dist += segment.dst();
		}
		
		return dist;
	}

	public float typeLength(GoopType type) {
		float dist = 0;
		if (currentSubSegment != null) {
			if (currentSubSegment.getType() == type) dist += currentSubSegment.dst();
		}
		for (SubSegment segment : segments) {
			if (segment.getType() == type) dist += segment.dst();
		}
		
		return dist;
	}

	private void updateMoveDir() {
		if (segments.size > 0) {
			currentSubSegment.moveDir = segments.peek().moveDir;
		}
		else {
			currentSubSegment.moveDir = goopComponent.getMoveDir();
		}
	}

	void reverseUpdate(boolean swap) {
		segments.reverse();
		
		for (SubSegment segment : segments) {
			temp.set(segment.endPoint);
			segment.endPoint.set(segment.origin);
			segment.origin.set(temp);
			if (swap) segment.moveDir = MoveDir.opposite(segment.moveDir);
		}
	}

	/**
	 * With the most recent segment first, release each segment until 
	 * res is reached.
	 * @param res
	 */
	private void releaseSegments(SubSegment res) {
		Gdx.app.log("fluidic", "releaseSegments");
		SubSegment segment = currentSubSegment;
		
		while (segment != res) {
			if (segment.getType() == GoopType.BRIDGE) {
				goopComponent.lastCollidable = segment.attachedCollidable;
			}
			segmentPool.free(segment);
			Gdx.app.log("fluidic", segment.toString() + " released");
	
			segment = segments.pop();
		}
	
		setCurrentSubSegment(segment);
		lastCollidable = res.attachedCollidable;
	}

	SubSegment getTouchingSegment() {
		for (SubSegment segment: segments) {
			if (segment.checkCollision(goopComponent.body)) {
				return segment;
			}
		}
		return null;
	}

	@Override
	public void reset() {
		if (currentSubSegment != null) segments.add(currentSubSegment);
		
		segmentPool.freeAll(segments);
		segments.clear();
	}

	/**
	 * Used when switching back to an older segment.
	 * FIXME Might want to enforce that...
	 * FIXME If currentSubSeg != null....?
	 * @param segment
	 */
	void setCurrentSubSegment(SubSegment segment) {
		currentSubSegment = segment;
		
		if (segment.getType() != goopComponent.getType()) {
			Utils.logger.debug("Switching goopComp type to " + segment.getType().toString());
			goopComponent.setType(segment.getType());
		}
	}

	public SubSegment getCurrentSubSegment() {
		return currentSubSegment;
	}

	public Array<SubSegment> getSegments() {
		return segments;
	}
	
	public Vector2 getOrigin() {
		return currentSubSegment.origin;
	}

	public Vector2 getEndPoint() {
		return currentSubSegment.endPoint;
	}
	
	public boolean isReleased() {
		return released;
	}

	public void overrideOutward(Vector2 outward) {
		if (currentSubSegment != null) 	currentSubSegment.setOutward(outward);
	}

	public MoveDir getMoveDir() {
		return moveDir;
	}

	public void setMoveDir(MoveDir moveDir) {
		this.moveDir = moveDir;
	}

	public boolean isOutwardFinalized() {
		return currentSubSegment.isOutwardFinalized();
	}

	public static void adjustCorner(Vector2 v, Vector2 dest) {
		if (Math.abs(dest.x - v.x) < Math.abs(dest.y - v.y)) {
			dest.x = v.x;
		} else {
			dest.y = v.y;
		}
	}
}
