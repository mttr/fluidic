package com.zzntd.fluidic.goop;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectFloatMap;
import com.zzntd.fluidic.LoadsAssets;
import com.zzntd.fluidic.Services;
import com.zzntd.fluidic.goop.Enums.GoopType;

public class GoopManager {
	private final Array<GoopSegment> segments = new Array<GoopSegment>();
	private GoopPhysicsComponent goopPhysics;
	private Texture goopTexture;
	private Texture wallGoopTexture;
	private Texture bridgeGoopTexture;
	private final ObjectFloatMap<GoopType> floatMap = new ObjectFloatMap<Enums.GoopType>();
	private int normalLimit;
	private int stickyLimit;
	private int bridgeLimit;

	public GoopManager() {
		LoadsAssets loads = new LoadsAssets() {
			@Override
			public void onLoaded() {
				goopTexture = Services.getAssetManager().get("images/simplenormalgoop.png");
				wallGoopTexture = Services.getAssetManager().get("images/simplewallgoop.png");
				bridgeGoopTexture = Services.getAssetManager().get("images/simplebridgegoop.png");
			}
			
			@Override
			protected void declareAssets() {
				// FIXME I should do this somewhere else.
				Services.getAssetManager().load("images/simplenormalgoop.png", Texture.class);
				Services.getAssetManager().load("images/simplewallgoop.png", Texture.class);
				Services.getAssetManager().load("images/simplebridgegoop.png", Texture.class);
			}
		};
		loads.loadAssets();
	}
	
	/**
	 * FIXME
	 * This constructor exists for the sole purpose of getting my unit tests
	 * working.
	 * @param forTesting
	 */
	public GoopManager(boolean forTesting) {
		if (forTesting) return;
	}
	
	public void renderGoop(SpriteBatch batch) {
		for (GoopSegment goopSeg : segments) {
			for (SubSegment subSeg : goopSeg.getSegments()) {
				drawSubSegment(batch, subSeg);
			}
			SubSegment cur = goopSeg.getCurrentSubSegment();
			if (cur != null) {
				drawSubSegment(batch, cur);
			}
		}
	}

	private void drawSubSegment(SpriteBatch batch, SubSegment subSeg) {
		switch (subSeg.getType()) {
		case NORMAL: subSeg.draw(goopTexture, batch); break;
		case STICKY: subSeg.draw(wallGoopTexture, batch); break;
		case BRIDGE: subSeg.draw(bridgeGoopTexture, batch); break;
		default: subSeg.draw(goopTexture, batch); break;
		}
	}

	public GoopType getType() {
		return goopPhysics.getType();
	}

	public void add(GoopSegment value) {
		segments.add(value);
	}

	public boolean remove(GoopSegment value) {
		return segments.removeValue(value, true);
	}

	public void clear() {
		segments.clear();
	}

	public Array<GoopSegment> getSegments() {
		return segments;
	}

	public GoopPhysicsComponent getGoopPhysics() {
		return goopPhysics;
	}

	public void setGoopPhysics(GoopPhysicsComponent goopPhysics) {
		this.goopPhysics = goopPhysics;
	}

	public void onNextGoopSelected() {
		if (goopPhysics != null) {
			goopPhysics.onNextGoopSelected();
		}
	}
	
	public float typeLength(GoopType type) {
		float dist = 0;

		floatMap.clear();
		for (GoopSegment segment : segments) {
			dist += segment.typeLength(type);
		}
		
		return dist;	
	}
	
	public float typeRemaining(GoopType type) {
		return getLimit(type) - typeLength(type);
	}
	
	// FIXME Should just map it.
	public int getLimit(GoopType type) {
		switch(type) {
		case NORMAL: return normalLimit;
		case STICKY: return stickyLimit;
		case BRIDGE: return bridgeLimit;
		default: return 0;
		}
	}

	public float totalLength() {
		float dist = 0;
		
		floatMap.clear();
		for (GoopSegment segment : segments) {
			float length = segment.totalLength();
			dist += length;
		}
		
		return dist;	
	}
	
	public GoopSegment getCollision(GoopPhysicsComponent goopPhysics) {
		for (GoopSegment segment : segments) {
			if (segment.checkCollisions(goopPhysics.body)) {
				return segment;
			}
		}
		return null;
	}
	
	public GoopSegment getOverlappingEndPoints(GoopPhysicsComponent goopPhysics) {
		for (GoopSegment segment : segments) {
			if (segment.extremityContainedBy(goopPhysics) && segment != goopPhysics.getSegment()) {
				return segment;
			}
		}
		return null;
	}

	public void setLimits(int normalLimit, int stickyLimit, int bridgeLimit) {
		this.normalLimit = normalLimit;
		this.stickyLimit = stickyLimit;
		this.bridgeLimit = bridgeLimit;
	}
}
