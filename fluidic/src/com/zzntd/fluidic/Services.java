package com.zzntd.fluidic;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import com.zzntd.fluidic.goop.Enums.GoopType;
import com.zzntd.fluidic.goop.GoopManager;
import com.zzntd.fluidic.goop.GoopPhysicsComponent;
import com.zzntd.fluidic.goop.GoopSegment;
import com.zzntd.fluidic.services.AssetLoader;
import com.zzntd.fluidic.services.CollisionManager;
import com.zzntd.fluidic.services.DrawableManager;
import com.zzntd.fluidic.services.LevelManager;
import com.zzntd.fluidic.xml.XmlObjectLoader;


public class Services {
	public static class NullGoopManager extends GoopManager {

		public NullGoopManager() {
			super(true);
		}

		@Override
		public void renderGoop(SpriteBatch batch) {
		}

		@Override
		public GoopType getType() {
			return GoopType.NONE;
		}

		@Override
		public void add(GoopSegment value) {
		}

		@Override
		public boolean remove(GoopSegment value) {
			return true;
		}

		@Override
		public void clear() {
		}

		@Override
		public Array<GoopSegment> getSegments() {
			return super.getSegments();
		}

		@Override
		public GoopPhysicsComponent getGoopPhysics() {
			return null;
		}

		@Override
		public void setGoopPhysics(GoopPhysicsComponent goopPhysics) {
		}

		@Override
		public void onNextGoopSelected() {
		}

		@Override
		public float typeLength(GoopType type) {
			return 0f;
		}

		@Override
		public float totalLength() {
			return 0f;
		}

		@Override
		public GoopSegment getCollision(GoopPhysicsComponent goopPhysics) {
			return null;
		}

		@Override
		public GoopSegment getOverlappingEndPoints(
				GoopPhysicsComponent goopPhysics) {
			return null;
		}
	}

	private static CollisionManager collisionManager;
	private static OrthographicCamera camera;
	private static DrawableManager drawableManager;
	private static AssetManager assetManager;
	private static AssetLoader assetLoader;
	private static XmlObjectLoader xmlObjectLoader;
	private static LevelManager levelManager;
	private static GoopManager goopManager;
	private static UiManager uiManager;

	public static AssetManager getAssetManager() {
		return assetManager;
	}

	public static void provideAssetManager(AssetManager assetManager) {
		Services.assetManager = assetManager;
	}

	public static DrawableManager getDrawableManager() {
		return drawableManager;
	}

	public static void provideDrawableManager(DrawableManager drawableManager) {
		Services.drawableManager = drawableManager;
	}

	public static CollisionManager getCollisionManager() {
		return collisionManager;
	}

	public static void provideCollisionManager(CollisionManager collisionManager) {
		Services.collisionManager = collisionManager;
	}

	public static void provideCamera(OrthographicCamera camera) {
		Services.camera = camera;
	}

	public static OrthographicCamera getCamera() {
		return camera;
	}

	public static AssetLoader getAssetLoader() {
		return assetLoader;
	}

	public static void provideAssetLoader(AssetLoader assetLoader) {
		Services.assetLoader = assetLoader;
	}

	public static XmlObjectLoader getXmlObjectLoader() {
		return xmlObjectLoader;
	}

	public static void provideXmlObjectLoader(XmlObjectLoader xmlObjectLoader) {
		Services.xmlObjectLoader = xmlObjectLoader;
	}

	public static LevelManager getLevelManager() {
		return levelManager;
	}

	public static void provideLevelManager(LevelManager levelManager) {
		Services.levelManager = levelManager;
	}

	public static GoopManager getGoopManager() {
		if (goopManager == null) {
			goopManager = new NullGoopManager();
		}
		return goopManager;
	}

	public static void provideGoopManager(GoopManager goopManager) {
		Services.goopManager = goopManager;
	}

	public static UiManager getUiManager() {
		return uiManager;
	}

	public static void provideUiManager(UiManager uiManager) {
		Services.uiManager = uiManager;
	}
}