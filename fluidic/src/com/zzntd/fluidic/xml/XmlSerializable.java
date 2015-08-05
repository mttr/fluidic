package com.zzntd.fluidic.xml;

import com.badlogic.gdx.utils.XmlReader.Element;
import com.zzntd.fluidic.core.ActorComposite;

public interface XmlSerializable {
	public void fromXml(Element compDef, ActorComposite parent);
}
