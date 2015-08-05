package com.zzntd.fluidic;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.Logger;
import com.badlogic.gdx.utils.XmlReader.Element;
import com.zzntd.fluidic.goop.GoopManager;
import com.zzntd.fluidic.physics.DebugRenderer;
import com.zzntd.fluidic.services.AssetLoader;
import com.zzntd.fluidic.services.CollisionManager;
import com.zzntd.fluidic.services.DrawableManager;
import com.zzntd.fluidic.services.LevelManager;
import com.zzntd.fluidic.xml.XmlObjectLoader;

public class FluidicGame implements ApplicationListener, InputProcessor {
	
	public static final float TILE_UNIT_CONVERSION = 1f / 32f;
	public static boolean unlimitedGoop = false;
	private static final float dt = 0.01f;

	public boolean showDebug = true;
	public boolean debugFrameMode = false;
	
	private DebugRenderer debugRenderer;
	private LevelManager levelManager;
	private float accumulator;
	private boolean allowFrame = false;
	private UiManager uiManager;
	
	@Override
	public void create() {		
		float w = Gdx.graphics.getWidth();
		float h = Gdx.graphics.getHeight();
		
		AssetManager assetManager = new AssetManager();
		
		OrthographicCamera camera = new OrthographicCamera();
		camera.setToOrtho(false, w * TILE_UNIT_CONVERSION, h * TILE_UNIT_CONVERSION);
		Gdx.input.setInputProcessor(this);
		Gdx.app.setLogLevel(Application.LOG_DEBUG);
		Utils.logger = new Logger("fluidic");
		Utils.logger.setLevel(Logger.DEBUG);
		Services.provideCamera(camera);
		
		Services.provideCollisionManager(new CollisionManager());
		Services.provideDrawableManager(new DrawableManager());	
		Services.provideAssetManager(assetManager);
		Services.provideAssetLoader(new AssetLoader());	
		Services.provideXmlObjectLoader(new XmlObjectLoader());
		Services.provideGoopManager(new GoopManager());
		
		uiManager = new UiManager(w, h, this);
		Services.provideUiManager(uiManager);
		
		assetManager.load("data/LevelSet.xml", Element.class);
		assetManager.finishLoading();
		
		Element levelSet = assetManager.get("data/LevelSet.xml");
		levelManager = new LevelManager(this, levelSet);
		
		Services.provideLevelManager(levelManager);

		debugRenderer = new DebugRenderer();
		accumulator = 0f;
	}

	@Override
	public void dispose() {
		Services.getAssetManager().dispose();
	}

	@Override
	public void render() {		
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		
		float elapsed = Gdx.graphics.getDeltaTime();
		
		MapScene mapScene = Services.getLevelManager().getMap();
		
		accumulator += elapsed;
		
		if (!debugFrameMode) {
			updateNormal(mapScene);
		}
		else if (allowFrame) {
			mapScene.update(dt);
			allowFrame = false;
		}
		else {
			mapScene.render(dt);
		}
		
		if (showDebug) {
			debugRenderer.setProjectionMatrix(Services.getCamera().combined);
			debugRenderer.render();
		}
		
		uiManager.renderText();
		
		levelManager.update(dt);
	}

	private void updateNormal(MapScene mapScene) {
		while (accumulator >= dt) {		
			mapScene.update(dt);
			accumulator -= dt;
		}
	}
	
	@Override
	public void resize(int width, int height) {
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}

	// InputProcessorBlah
	@Override
	public boolean keyDown(int keycode) {
		switch (keycode) {
		case Keys.RIGHT_BRACKET: showDebug = !showDebug; break;
		case Keys.BACKSPACE: debugFrameMode = !debugFrameMode; break;
		case Keys.SPACE: allowFrame = true; break;
		case Keys.ESCAPE: Gdx.app.exit(); break;
		case Keys.TAB: onTab(); break;
		case Keys.N: Services.getLevelManager().triggerNext(true); break;
		case Keys.R: Services.getLevelManager().reload(); break;
		case Keys.LEFT_BRACKET: Services.getLevelManager().toggleForceStay(); break;
		case Keys.L: unlimitedGoop = !unlimitedGoop; break;
		}
		return false;
	}

	private void onTab() {
		GoopManager goopManager = Services.getGoopManager();
		if (goopManager != null) {
			goopManager.onNextGoopSelected();
		}
	}

	@Override
	public boolean keyUp(int keycode) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		// TODO Auto-generated method stub
		return false;
	}
}
