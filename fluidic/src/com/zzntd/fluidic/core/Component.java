package com.zzntd.fluidic.core;

public abstract class Component implements Comparable<Component> {
	private Priority priority;
	protected Composite parent;
	
	protected Component(Priority priority) {
		this.priority = priority;
	}
	
	public abstract void update(float dt);

	@Override
	public int compareTo(Component o) {
		return this.priority.compareTo(o.priority);
	}
	
	public void addTo(Composite parent) {
		this.parent = parent;
		this.parent.add(this);
	}
	
	public Composite getParent() {
		return parent;
	}

	public boolean isValid() {
		if (priority != Priority.NONE) {
			return true;
		}
		else {
			return false;
		}
	}

	public Priority getPriority() {
		return priority;
	}

	public void setPriority(Priority priority) {
		this.priority = priority;
	}
}
