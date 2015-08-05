package com.zzntd.fluidic;

import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.XmlReader.Element;
import com.zzntd.fluidic.core.ActorComposite;
import com.zzntd.fluidic.core.Component;
import com.zzntd.fluidic.core.Composite;
import com.zzntd.fluidic.core.Priority;
import com.zzntd.fluidic.goop.GoopManager;
import com.zzntd.fluidic.objects.SwitchObject;
import com.zzntd.fluidic.physics.PhysicsComponent;
import com.zzntd.fluidic.xml.XmlObjectLoader;

public class MapScene extends Component implements Disposable {
	private TiledMap map;
	private OrthogonalTiledMapRenderer renderer;
	private SpriteBatch batch;
	private Composite gameWorld;
	private String mapName;
	private LoadsAssets mapLoader;
	
	public MapScene(FluidicGame game, Element mapData) {
		super(Priority.NONE);
		gameWorld = new Composite(Priority.NONE);
		this.mapName = "maps/" + mapData.getText();
		
		Services.getAssetManager().setLoader(TiledMap.class, new TmxMapLoader(new InternalFileHandleResolver()));
		
		loadAssets();		
		gameWorld.sort();
		
		GoopManager goopManager = Services.getGoopManager();

		// FIXME Normal limit must be > 0
		int normalLimit = mapData.getInt("normalLimit", 0); 
		int stickyLimit = mapData.getInt("stickyLimit", 0);
		int bridgeLimit = mapData.getInt("bridgeLimit", 0);
		
		goopManager.setLimits(normalLimit, stickyLimit, bridgeLimit);
	}

	private void loadAssets() {
		mapLoader = new LoadsAssets() {
			
			@Override
			public void onLoaded() {
				initMap();
			}
			
			@Override
			protected void declareAssets() {
				Services.getAssetManager().load(mapName, TiledMap.class);
			}
		};
		
		mapLoader.loadAssets();		
		Services.getAssetLoader().finishLoading();
		loadMapObjects();
		Services.getAssetLoader().finishLoading();
		Services.getDrawableManager().sortDrawables();
	}
	
	private void initMap() {
		map = Services.getAssetManager().get(mapName);
		renderer = new OrthogonalTiledMapRenderer(map, FluidicGame.TILE_UNIT_CONVERSION);
		
		MapLayer layer = map.getLayers().get("collision_zones");
		
		for (MapObject obj : layer.getObjects()) {
			loadCollidableBox((RectangleMapObject) obj);
		}
		
		// Get SpriteBatch from renderer.
		batch = renderer.getSpriteBatch();
	}

	private void loadCollidableBox(RectangleMapObject obj) {
		ActorComposite actor = new ActorComposite(Priority.NONE);
		PhysicsComponent physics = new PhysicsComponent();
		
		actor.rec.set(obj.getRectangle());
		
		Utils.scaleRec(actor.rec, FluidicGame.TILE_UNIT_CONVERSION);
		
		actor.position.set(actor.rec.x, actor.rec.y);
		
		physics.addTo(actor);
	}

	private void loadMapObjects() {
		XmlObjectLoader xmlObjectLoader = Services.getXmlObjectLoader();
		
		MapLayer obj_layer = map.getLayers().get("objects");
		MapObjects objects = obj_layer.getObjects();
		
		// Start loading all the object definitions we need.
		for (MapObject obj : objects) {
			xmlObjectLoader.loadObject(obj.getName());
		}
		
		// Wait for object definitions to load, then create them.
		for (MapObject obj : objects) {
			spawnObject(xmlObjectLoader, (RectangleMapObject) obj);
		}
	}

	private void spawnObject(XmlObjectLoader xmlObjectLoader,
			RectangleMapObject obj) {
		ActorComposite actor = xmlObjectLoader.getObject(obj.getName());
		actor.rec.set(obj.getRectangle());
		actor.loadMapProperties(obj.getProperties());
		
		Utils.scaleRec(actor.rec, FluidicGame.TILE_UNIT_CONVERSION);
		
		actor.position.set(actor.rec.x, actor.rec.y); // FIXME Really?
		actor.updateRec();
		actor.addTo(gameWorld);
	}

	@Override
	public void update(float dt) {
		gameWorld.update(dt);
		Services.getCollisionManager().update(dt);
		
		render(dt);
	}

	void render(float dt) {
		OrthographicCamera camera = Services.getCamera();		
		camera.update();
		renderer.setView(camera);
		renderer.render();
		
		batch.begin();
		Services.getDrawableManager().setBatch(batch);
		Services.getDrawableManager().update(dt);
		Services.getGoopManager().renderGoop(batch);
		batch.end();
	}

	@Override
	public void dispose() {
		renderer.dispose(); // This also disposes the spritebatch
		Services.getDrawableManager().clear();
		Services.getCollisionManager().clear();
		Services.getGoopManager().clear();
		SwitchObject.switchCount = 0; // FIXME AWFUL.
	}
}
