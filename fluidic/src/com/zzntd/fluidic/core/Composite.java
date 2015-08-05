package com.zzntd.fluidic.core;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.zzntd.fluidic.ListenerType;

public class Composite extends Component {
	private Array<Component	> children;
	protected ObjectMap<ListenerType, Object> listeners;
	
	public Composite(Priority priority) {
		super(priority);
		this.children = new Array<Component>();
		listeners = new ObjectMap<ListenerType, Object>();
	}

	@Override
	public
	void update(float dt) {
		for (Component c : children) {
			if (c.isValid()) {
				c.update(dt);
			}
		}
	}
	
	public void add(Component c) {
		children.add(c);
	}
	
	public void remove(Component c) {
		children.removeValue(c, true);
	}

	public void clear() {
		children.clear();
	}

	public void sort() {
		children.sort();
	}
	
	public Array<Component> getChildren() {
		return children;
	}

	public Object putListener(ListenerType key, Object value) {
		return listeners.put(key, value);
	}

	public Object getListener(ListenerType key) {
		return listeners.get(key);
	}
}
