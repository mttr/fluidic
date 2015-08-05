package com.zzntd.fluidic.objects;

import com.zzntd.fluidic.AnimationResponse;
import com.zzntd.fluidic.ListenerType;
import com.zzntd.fluidic.Services;
import com.zzntd.fluidic.core.ActorComposite;
import com.zzntd.fluidic.core.Composite;
import com.zzntd.fluidic.core.Priority;
import com.zzntd.fluidic.objects.SwitchObject.SwitchListener;
import com.zzntd.fluidic.physics.AbstractPhysicsComponent;
import com.zzntd.fluidic.physics.RectangleBody;
import com.zzntd.fluidic.services.CollisionManager;

public class DoorObject extends ActorComposite {
	private int totalCount;
	private int activeCount;
	private boolean open;
	private SwitchListener switchListener;

	public DoorObject() {
		super(Priority.MID);
		open = false;
		
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
				
				if (open) {
					Services.getLevelManager().triggerNext(false);
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
		
		switchListener = new SwitchListener() {			
			@Override
			public void update(float dt) {
				totalCount = SwitchObject.switchCount;

				if (activeCount == totalCount) {
					open = true;
					
					AnimationResponse anim = (AnimationResponse) parent.getListener(ListenerType.Animation);
					
					if (anim != null) {
						anim.setAnimation("open");
					}
				}
			}
			
			@Override
			public void setSwitchCount(int new_count) {
				totalCount = new_count;
			}
			
			@Override
			public void onSwitchActivated() {
				activeCount++;
			}
		};
		
		switchListener.addTo(this);
	}

	@Override
	public void addTo(Composite parent) {
		super.addTo(parent);
		
		parent.putListener(ListenerType.Switches, switchListener);
	}	
}
