package com.zzntd.fluidic.services;

import com.badlogic.gdx.utils.Array;
import com.zzntd.fluidic.LoadsAssets;
import com.zzntd.fluidic.Services;

public class AssetLoader {
	private Array<LoadsAssets> assetLoaders;
	
	public void finishLoading() {
		Services.getAssetManager().finishLoading();
		
		for (LoadsAssets loader: assetLoaders) {
			loader.onLoaded();
		}
		clear();
	}
	
	public void add(LoadsAssets value) {
		assetLoaders.add(value);
	}

	public boolean removeValue(LoadsAssets value) {
		return assetLoaders.removeValue(value, true);
	}

	public void clear() {
		assetLoaders.clear();
	}

	public AssetLoader() {
		assetLoaders = new Array<LoadsAssets>();
	}
}
