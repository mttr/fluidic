package com.zzntd.fluidic.services;

import java.util.Comparator;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import com.zzntd.fluidic.DrawableComponent;
import com.zzntd.fluidic.core.Component;
import com.zzntd.fluidic.core.Priority;

public class DrawableManager extends Component {
	private Array<DrawableComponent> drawables;
	private SpriteBatch batch;

	public void setBatch(SpriteBatch batch) {
		this.batch = batch;
	}

	public DrawableManager() {
		super(Priority.NONE);
		
		drawables = new Array<DrawableComponent>();
	}

	public void add(DrawableComponent drawable) {
		drawables.add(drawable);
	}

	public boolean removeValue(DrawableComponent drawable) {
		return drawables.removeValue(drawable, true);
	}

	public void clear() {
		drawables.clear();
		batch = null;
	}
	
	public void sortDrawables() {
		drawables.sort(new Comparator<DrawableComponent>() {			
			@Override
			public int compare(DrawableComponent arg0, DrawableComponent arg1) {
				return arg0.getParent().compareTo(arg1.getParent());
			}
		});
	}

	@Override
	public void update(float dt) {
		for (DrawableComponent drawable : drawables) {
			drawable.sprite.draw(batch);
		}
	}
}
