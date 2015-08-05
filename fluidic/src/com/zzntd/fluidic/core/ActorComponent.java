package com.zzntd.fluidic.core;

import com.badlogic.gdx.utils.XmlReader.Element;
import com.zzntd.fluidic.xml.XmlSerializable;

public abstract class ActorComponent extends Component implements XmlSerializable {
	protected ActorComposite parent;

	protected ActorComponent(Priority priority) {
		super(priority);
	}

	public void addTo(ActorComposite parent) {
		this.parent = parent;
		super.addTo(parent);
	}
	
	public ActorComposite getParent() {
		return parent;
	}

	@Override
	public void fromXml(Element compDef, ActorComposite parent) {
		addTo(parent);
	}
}
