package com.zzntd.fluidic.goop;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.zzntd.fluidic.Utils;
import com.zzntd.fluidic.goop.Enums.CollidableType;
import com.zzntd.fluidic.goop.Enums.GoopType;
import com.zzntd.fluidic.physics.PhysicsComponent;
import com.zzntd.fluidic.physics.PhysicsComponent.MoveDir;

public class ExpansionHelper {
	GoopSegment goopSegment;
	// FIXME This might work as a static class or a singleton.

	/**
	 * Expand to a point given by collision points from the goopComponent attached
	 * to goopSegment on collidable.  
	 * @param goopSegment
	 * @param move
	 * @param collidable
	 * @param isNew 
	 * @param outward
	 */
	public void expand(GoopSegment goopSegment, MoveDir move, PhysicsComponent collidable, boolean isNew, Vector2 outward) {
		Array<Vector2> array = Utils.getProjectedVertices(collidable.body.poly, goopSegment.goopComponent.body.poly);
		
		this.goopSegment = goopSegment;

		Vector2 temp = Utils.vectorPool.obtain();

		temp.set(0, 0);
		
		if (isBridge(collidable)) {
			Utils.vectorPool.free(temp);
			return;
		}
		
		if (!goopSegment.reconnect) {
			if (isNew && goopSegment.getMoveDir() != MoveDir.NONE) newSeg(array, collidable, temp, outward);
			else continueExpansion(array, move, collidable, isNew, temp, outward);
		}
		else if (goopSegment.reconnect && move != MoveDir.NONE) {
			splitExpansion(array, move);
		}

		if (goopSegment.lastCollidable == null) {
			goopSegment.lastCollidable = collidable;
		}

		Utils.vectorPool.free(temp);
	}

	public void newConnectingSegment(PhysicsComponent collidable, Vector2 newEndPoint, Vector2 oldEndPoint, Vector2 outward) {
		Utils.logger.debug("newConnectingSegment; collidable " + collidable.toString() + " last: " + goopSegment.lastCollidable.toString());
	
		SubSegment sub = goopSegment.getTouchingSegment();
		//if (currentSubSegment.attachedCollidable == collidable) return;
		if (sub != null) {
			if (sub.attachedCollidable == collidable) return;
		}
		if (goopSegment.lastCollidable != null) { // FIXME is this even possible? It shouldn't be.
			// Grab the closest vertex out of the projections from player on the last
			// collidable and use that for our endpoint/new origin.
			// In other words, this ensures that the segment we're switching out of is still
			// aligned properly with the previous collidable.
			Array<Vector2> newverts = Utils.getCorners(goopSegment.lastCollidable.body.poly.getBoundingRectangle(),
					collidable.body.poly.getBoundingRectangle());
			Utils.closest(newverts, newEndPoint, oldEndPoint);
			GoopSegment.adjustCorner(newEndPoint, oldEndPoint);
		}
		if (goopSegment.currentSubSegment != null) {
			goopSegment.currentSubSegment.moveDir = goopSegment.goopComponent.getMoveDir();
		}
		if (collidable.isActive != goopSegment.lastCollidable.isActive) {
			Utils.logger.debug("active collidable transition");
			goopSegment.goopComponent.releaseCurrentSegment(false);
			return;
		}
		
		newSubSegment(collidable, newEndPoint, oldEndPoint, outward);
		
		return;
	}

	void newSubSegment(PhysicsComponent collidable, Vector2 newEndPoint, Vector2 oldEndPoint, Vector2 outward) {
		goopSegment.lastCollidable = collidable;
	
		if (goopSegment.currentSubSegment != null) goopSegment.segments.add(goopSegment.currentSubSegment);
		goopSegment.currentSubSegment = GoopSegment.segmentPool.obtain();
		goopSegment.currentSubSegment.initialize(oldEndPoint, newEndPoint, outward, collidable, goopSegment.goopComponent.getType(), MoveDir.NONE);
		
		goopSegment.currentSubSegment.setOutwardFinalized(false);
		goopSegment.released = false;
	}

	void continueExpansion(Array<Vector2> array, MoveDir move, PhysicsComponent collidable, boolean isNew, Vector2 newPoint, Vector2 outward) {
		// FIXME IN DANGEROUS NEED OF REFACTORING, SRSLY
		if (goopSegment.currentSubSegment.getType() == GoopType.BRIDGE) {
			Utils.farthest(Utils.getLineIntersections(goopSegment.goopComponent.body.poly, goopSegment.currentSubSegment.origin, goopSegment.currentSubSegment.endPoint), goopSegment.currentSubSegment.origin, newPoint);
		}
		else if (collidable != goopSegment.currentSubSegment.attachedCollidable) {
			Utils.logger.debug("collidable != attached");
			
			if (goopSegment.lastCollidable == null) {
				// FIXME To the best of my knowledge, this currently only happens when transitioning between actives/inactives.
				goopSegment.lastCollidable = goopSegment.currentSubSegment.attachedCollidable;
				final Vector2 newEndPoint = Utils.vectorPool.obtain().set(goopSegment.currentSubSegment.endPoint);
	
				newConnectingSegment(collidable, newPoint, newEndPoint, outward);
				
				goopSegment.reset();
				return;
			}
		}
		if (!Utils.farthestAligned(array, goopSegment.currentSubSegment.origin, goopSegment.currentSubSegment.endPoint, newPoint))
		{
			// FIXME Can be refactored into something prettier (the dst2 stuff)
			if (goopSegment.currentSubSegment.origin.dst2(goopSegment.currentSubSegment.endPoint) < .5f) {
				Utils.farthest(array, goopSegment.currentSubSegment.origin, newPoint);
				GoopSegment.adjustCorner(newPoint, goopSegment.currentSubSegment.origin);
			}
			else if (goopSegment.currentSubSegment.getType() == GoopType.STICKY) {
				Utils.logger.debug("yarr");
				Utils.farthest(array, goopSegment.currentSubSegment.endPoint, newPoint);
				newSubSegment(collidable, newPoint, goopSegment.currentSubSegment.endPoint, outward);
			}
		}
	
		// We don't know which direction we're expanding the goop until we move
		// for the first time.
		if (goopSegment.getMoveDir() == MoveDir.NONE && move != MoveDir.NONE) {
			Utils.logger.debug("Initializing direction based stuff.");
	
			Vector2 vA = Utils.vectorPool.obtain().set(0, 0);
			Vector2 vB = Utils.vectorPool.obtain().set(0, 0);
			
			Utils.farthestAligned(array, goopSegment.currentSubSegment.origin, goopSegment.currentSubSegment.endPoint, vA);
			Utils.farthestAligned(array, goopSegment.currentSubSegment.endPoint, goopSegment.currentSubSegment.origin, vB);
			
			final Vector2 origin_v = vA.dst2(goopSegment.currentSubSegment.origin) > vB.dst2(goopSegment.currentSubSegment.endPoint) ? goopSegment.currentSubSegment.origin : goopSegment.currentSubSegment.endPoint;
			final Vector2 endpoint_v = Utils.farthest(origin_v, vA, vB);
	
			goopSegment.currentSubSegment.initialize(origin_v, endpoint_v, outward, collidable, goopSegment.goopComponent.getType(), move);
			goopSegment.setMoveDir(move);
	
			Utils.vectorPool.free(vA);
			Utils.vectorPool.free(vB);
		}
	
		goopSegment.currentSubSegment.endPoint.set(newPoint);
	}

	void newSeg(Array<Vector2> array, PhysicsComponent collidable,
			Vector2 newPoint, Vector2 outward) {
		Utils.farthest(array, goopSegment.currentSubSegment.origin, newPoint);
		newConnectingSegment(collidable, newPoint, goopSegment.currentSubSegment.endPoint, outward);
		return;
	}

	void splitExpansion(Array<Vector2> array, MoveDir move) {
		SubSegment res = goopSegment.getTouchingSegment();
		
		if (res == null) return;
		
		if (goopSegment.getMoveDir() == MoveDir.NONE) throw new RuntimeException("MoveDir should never be 'NONE' when splitting.");
	
		GoopSegment newSegment = new GoopSegment();
		
		if (move != goopSegment.getMoveDir()) {
			splitOpposed(array, res, newSegment, move);
		}
		else {
			splitContinues(array, res, newSegment, move);				
		}
		goopSegment.reconnect = false;
	}

	void splitOpposed(Array<Vector2> array, SubSegment res, GoopSegment newSegment, MoveDir move) {
		SubSegment last = GoopSegment.segmentPool.obtain();
		last.initialize(res.endPoint, res.origin, res.outward, res.attachedCollidable, res.getType(), MoveDir.NONE);
	
		// Set endpoint of currently overlapping segment to match farthest intersect point
		Utils.farthest(array, res.origin, res.endPoint);
		Utils.closest(array, last.origin, last.endPoint);
	
		last.endPoint.set(res.endPoint);
		
		boolean allowAdds = false;
		for (SubSegment segment : goopSegment.segments) {
			if (allowAdds) newSegment.segments.add(segment);
			if (segment == res) allowAdds = true;
		}
		goopSegment.segments.removeAll(newSegment.segments, true);
		newSegment.reverseUpdate(true);
		newSegment.setMoveDir(MoveDir.opposite(move));
		
		goopSegment.setCurrentSubSegment(goopSegment.segments.pop());
	
		newSegment.segments.add(last);
	}

	void splitContinues(Array<Vector2> array, SubSegment res, GoopSegment newSegment, MoveDir move) {
		goopSegment.setCurrentSubSegment(GoopSegment.segmentPool.obtain().initialize(res.endPoint, 
				Utils.farthest(array, res.endPoint, goopSegment.temp), res.outward,
				res.attachedCollidable, res.getType(), MoveDir.NONE));
	
		// Set endpoint to match closest intersect point
		Utils.closest(array, res.origin, res.endPoint);
		
		for (SubSegment segment : goopSegment.segments) {
			newSegment.segments.add(segment);
			if (segment == res) break;
		}
		newSegment.setMoveDir(move);
		
		goopSegment.segments.removeAll(newSegment.segments, true);
		goopSegment.reverseUpdate(false);
	}

	boolean isBridge(PhysicsComponent collidable) {
		// FIXME This is starting to look like it belongs in subSegment.
		if (goopSegment.currentSubSegment != null) {
			if (goopSegment.currentSubSegment.getType() != GoopType.BRIDGE && FluidicPhysicsComponent.class.isInstance(collidable)) {
				FluidicPhysicsComponent fCollidable = (FluidicPhysicsComponent) collidable;
				if (fCollidable.getCollidableType() == CollidableType.BRIDGE) {
					return true;
				}
			}
		}
		return false;
	}

}
