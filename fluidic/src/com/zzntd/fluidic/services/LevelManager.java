package com.zzntd.fluidic.services;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader.Element;
import com.zzntd.fluidic.FluidicGame;
import com.zzntd.fluidic.MapScene;
import com.zzntd.fluidic.core.Component;
import com.zzntd.fluidic.core.Priority;

public class LevelManager extends Component {
	private Array<Element> levels;
	private FluidicGame context;
	private MapScene currentMap;
	private boolean nextMap;
	private int currentLevel = 0;
	private boolean forceStay = false;
	
	public LevelManager(FluidicGame context, Element levelSet) {
		super(Priority.NONE);
		this.context = context;
		nextMap = false;
		levels = new Array<Element>();
		
		for (int i = 0; i < levelSet.getChildCount(); i++) {
			levels.add(levelSet.getChild(i));
		}
		
		currentMap = new MapScene(context, levels.get(currentLevel));
	}
	
	public void triggerNext(boolean override) {
		if (override || !forceStay) {
			nextMap = true;
		}
	}
	
	public void reload() {
		currentLevel = (currentLevel - 1) % levels.size;
		nextMap = true;
	}

	private void nextMap(boolean override) {
			currentLevel = (currentLevel + 1) % levels.size;
			currentMap = new MapScene(context, levels.get(currentLevel));
	}
	
	public MapScene getMap() {
		return currentMap;
	}

	@Override
	public void update(float dt) {
		if (nextMap) {
			if (currentMap != null) {
				currentMap.dispose();
			}
			
			if (levels.size > 0) {
				nextMap(false);
			}
			else {
				
			}
			nextMap = false;
		}
	}

	public void toggleForceStay() {
		forceStay = !forceStay;
	}
}
