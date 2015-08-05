package com.zzntd.fluidic;

public abstract class LoadsAssets {
	public abstract void onLoaded();
	protected abstract void declareAssets();
	
	public final void loadAssets() {
		declareAssets();
		Services.getAssetLoader().add(this);
	}
}
