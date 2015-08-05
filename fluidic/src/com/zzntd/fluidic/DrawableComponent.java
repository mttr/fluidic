package com.zzntd.fluidic;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.XmlReader.Element;
import com.zzntd.fluidic.core.ActorComponent;
import com.zzntd.fluidic.core.ActorComposite;
import com.zzntd.fluidic.core.Priority;

public class DrawableComponent extends ActorComponent implements AnimationResponse {
	public Sprite sprite;
	
	private String textureName;
	private TextureRegion textureRegion;
	private ObjectMap<String, Animation> animationMap;
	private Animation currentAnimation;
	private float stateTime;

	public DrawableComponent() {
		super(Priority.LOW); // Draw after everything else is done.
		
		Services.getDrawableManager().add(this);
		
		textureRegion = new TextureRegion();
		stateTime = 0;
	}
	
	public void loadTexture(String file) {

	}

	@Override
	public void update(float dt) {
		stateTime += dt;
		
		if (currentAnimation != null) {
			sprite.setRegion(currentAnimation.getKeyFrame(stateTime));
		}
		
		sprite.setSize(parent.rec.width, parent.rec.height);
		sprite.setOrigin(parent.rec.width / 2f, parent.rec.height / 2f);
		sprite.setRotation(parent.angle);
		sprite.setPosition(parent.rec.x, parent.rec.y);
	}

	@Override
	public void addTo(ActorComposite parent) {
		super.addTo(parent);
		
		parent.putListener(ListenerType.Animation, this);
	}

	@Override
	public void fromXml(final Element compDef, final ActorComposite parent) {
		super.fromXml(compDef, parent);
		
		textureName = compDef.get("texture");
		
		LoadsAssets drawableLoader = new LoadsAssets() {			
			@Override
			public void onLoaded() {
				loadStuff(compDef, parent);
			}

			@Override
			protected void declareAssets() {
				Services.getAssetManager().load(textureName, Texture.class);
			}
		};
		
		drawableLoader.loadAssets();		
	}

	@Override
	public void setAnimation(String name) {
		currentAnimation = animationMap.get(name);
		stateTime = 0;
	}	
	
	private Animation getAnimation(Element element, Texture texture) {
		Array<TextureRegion> frames = new Array<TextureRegion>();
		int frameCount = element.getInt("frame_count");				
		int width = element.getInt("frame_width");
		int height = element.getInt("frame_height");
		int x = element.getInt("start_x");
		int y = element.getInt("start_y");
		float frameDuration = element.getFloat("frame_duration");
		
		for (int i = 0; i < frameCount; i++) {
			TextureRegion region = new TextureRegion(texture);

			region.setRegion(x + ((i * width) % texture.getWidth()),
							 y + (height * (i / (texture.getWidth() / width))),
							 width,
							 height);					
			frames.add(region);
		}
		
		Animation animation = new Animation(frameDuration, frames);
		String playMode = element.get("play_mode");
		
		if (playMode.equals("LOOP")) animation.setPlayMode(Animation.LOOP);
		else if (playMode.equals("LOOP_PINGPONG")) animation.setPlayMode(Animation.LOOP_PINGPONG);
		else if (playMode.equals("LOOP_RANDOM")) animation.setPlayMode(Animation.LOOP_RANDOM);
		else if (playMode.equals("LOOP_REVERSED")) animation.setPlayMode(Animation.LOOP_REVERSED);
		else if (playMode.equals("NORMAL")) animation.setPlayMode(Animation.NORMAL);
		else if (playMode.equals("REVERSED")) animation.setPlayMode(Animation.REVERSED);				
		
		return animation;
	}

	private void loadStuff(final Element compDef, final ActorComposite parent) {
		Texture texture = Services.getAssetManager().get(textureName);
		
		// FIXME What happens to this texture region if we have animations...?
		textureRegion.setTexture(texture);
		textureRegion.setRegion(compDef.getInt("x", 0), 
				compDef.getInt("y", 0), 
				compDef.getInt("width", 32), 
				compDef.getInt("height", 32));
		
		sprite = new Sprite(textureRegion);
		
		parent.rec.width = FluidicGame.TILE_UNIT_CONVERSION * textureRegion.getRegionWidth();
		parent.rec.height = FluidicGame.TILE_UNIT_CONVERSION * textureRegion.getRegionHeight();
		
		String defaultAnim = compDef.get("default_animation", "");
		
		getAnimations(compDef, texture, defaultAnim);
	}

	private void getAnimations(final Element compDef, Texture texture,
			String defaultAnim) {
		animationMap = new ObjectMap<String, Animation>();
		
		for (int i = 0; i < compDef.getChildCount(); i++) {
			Element element = compDef.getChild(i);
			animationMap.put(element.get("name"), getAnimation(element, texture));
		}
		
		if (!defaultAnim.equals("")) {
			currentAnimation = animationMap.get(defaultAnim);
		}
	}
}
