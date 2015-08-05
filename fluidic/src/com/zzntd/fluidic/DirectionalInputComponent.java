package com.zzntd.fluidic;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.utils.XmlReader.Element;
import com.zzntd.fluidic.core.ActorComponent;
import com.zzntd.fluidic.core.ActorComposite;
import com.zzntd.fluidic.core.Priority;
import com.zzntd.fluidic.physics.PhysicsResponse;

public class DirectionalInputComponent extends ActorComponent {
	PhysicsResponse response;

	public DirectionalInputComponent() {
		super(Priority.HIGH);
	}

	@Override
	public void update(float dt) {
		checkListener();
		
		if (response != null) {			
			if (Gdx.input.isKeyPressed(Keys.LEFT)) {
				response.onLeft();
			}
			
			if (Gdx.input.isKeyPressed(Keys.RIGHT)) {
				response.onRight();
			}
			
			if (Gdx.input.isKeyPressed(Keys.UP)) {
				response.onUp();
			}
			
			if (Gdx.input.isKeyPressed(Keys.DOWN)) {
				response.onDown();
			}
		}
	}

	private void checkListener() {
		if (response == null) {
			response = (PhysicsResponse) parent.getListener(ListenerType.PhysicsResponse);
		}
	}

	@Override
	public void addTo(ActorComposite parent) {
		super.addTo(parent);
		
		checkListener();
	}

	@Override
	public void fromXml(Element compDef, ActorComposite parent) {
		super.fromXml(compDef, parent);
	}
}
