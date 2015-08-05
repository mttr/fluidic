package com.zzntd.fluidic.core;

import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class ActorComposite extends Composite {
	public Vector2 position;
	public Rectangle rec;
	public float angle;
	
	public ActorComposite(Priority priority) {
		super(priority);
		
		position = new Vector2();
		rec = new Rectangle();
		angle = 0;
	}

	public void updateRec() {
		// FIXME I know there are other places that can be updated to use this...
		rec.setPosition(position);
	}

	public void loadMapProperties(MapProperties properties) {
	}	
}
