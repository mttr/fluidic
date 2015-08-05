package com.zzntd.fluidic.goop;

import com.zzntd.fluidic.goop.Enums.CollidableType;
import com.zzntd.fluidic.physics.PhysicsComponent;

public class FluidicPhysicsComponent extends PhysicsComponent {

	public FluidicPhysicsComponent() {
		super();
		
		collidableType = CollidableType.SOLID;
	}

	public FluidicPhysicsComponent(boolean active) {
		super(active);

		collidableType = CollidableType.SOLID;
	}

	private CollidableType collidableType;

	public CollidableType getCollidableType() {
		return collidableType;
	}

	public void setCollidableType(CollidableType collidableType) {
		this.collidableType = collidableType;
	}

}
