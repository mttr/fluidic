package com.zzntd.fluidic.objects;

import com.zzntd.fluidic.AnimationResponse;
import com.zzntd.fluidic.ListenerType;
import com.zzntd.fluidic.core.ActorComponent;
import com.zzntd.fluidic.core.ActorComposite;
import com.zzntd.fluidic.core.Composite;
import com.zzntd.fluidic.core.Priority;
import com.zzntd.fluidic.physics.AbstractPhysicsComponent;
import com.zzntd.fluidic.physics.RectangleBody;
import com.zzntd.fluidic.services.CollisionManager;

public class SwitchObject extends ActorComposite {
	public static int switchCount; // FIXME nrfnrfnrfnrnfrnfn
	
	public static abstract class SwitchListener extends ActorComponent {
		protected SwitchListener() {
			super(Priority.MID);
		}
		
		public abstract void onSwitchActivated();
		public abstract void setSwitchCount(int count);
	}
	
	private boolean active;
	
	public SwitchObject() {
		this(Priority.MID);
	}

	public SwitchObject(Priority priority) {
		super(priority);
		
		active = false;
		
		AbstractPhysicsComponent<RectangleBody> phys = new AbstractPhysicsComponent<RectangleBody>() {			
			@Override
			public void postCollision(CollisionManager collisionManager, int collisionCount) {
			}
			
			@Override
			public void physicsUpdate(float dt) {
			}
			
			@Override
			public void onCollision(AbstractPhysicsComponent<?> collidableB,
					CollisionManager collisionManager) {
				
				if (!active) {
					active = true;
					AnimationResponse anim = (AnimationResponse) parent.getListener(ListenerType.Animation);
					anim.setAnimation("activated");
					
					notifySwitchListeners();
				}
			}

			@Override
			protected void createBody() {
				this.body = new RectangleBody(this);
			}
		};
		
		phys.isActive = false;
		phys.isSolid = false;
		phys.addTo(this);
	}

	@Override
	public void addTo(Composite parent) {
		super.addTo(parent);
		
		switchCount++;
	}
	
	public void notifySwitchListeners() {
		SwitchListener switchListener = (SwitchListener) parent.getListener(ListenerType.Switches);
		switchListener.onSwitchActivated();
	}
}
