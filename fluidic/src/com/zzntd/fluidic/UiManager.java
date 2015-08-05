package com.zzntd.fluidic;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.zzntd.fluidic.core.Component;
import com.zzntd.fluidic.core.Priority;
import com.zzntd.fluidic.goop.Enums.GoopType;

public class UiManager extends Component {
	private BitmapFont bitmapFont;
	private OrthographicCamera camera;
	private SpriteBatch batch;
	private Vector3 projection = new Vector3();
	private final FluidicGame context;

	public UiManager(float viewportWidth, float viewportHeight, FluidicGame context) {
		super(Priority.NONE);
		
		bitmapFont = new BitmapFont();
		camera = new OrthographicCamera(viewportWidth, viewportHeight);
		batch = new SpriteBatch();
		batch.setProjectionMatrix(camera.combined);
		
		this.context = context;
	}
	
	public void renderText() {
		
		GoopType type = Services.getGoopManager().getType();
		final String typeString = type.toString();
		
		batch.begin();
		projection.set(0, 20, 0);
		drawText(0, 20, "selected: " + typeString + " TAB");
		drawText(0, 35, "Total Length: " + Float.toString(Services.getGoopManager().totalLength()));
		conditionalText(0, 50, lengthUsed(type, typeString), lengthRemaining(type, typeString), FluidicGame.unlimitedGoop);
		conditionalText(0, 65, "Render Mode: Debug ", "Render Mode: Normal", context.showDebug);
		conditionalText(0, 80, "Frame Step SPACE", "Frame Step Off BACKSPACE", context.debugFrameMode);
		conditionalText(0, 95, "Limited L", "Unlimited L", !FluidicGame.unlimitedGoop);

		batch.end();
	}

	private String lengthUsed(GoopType type, final String typeString) {
		return typeString + " used: " + Float.toString(Services.getGoopManager().typeLength(type));
	}

	private String lengthRemaining(GoopType type, final String typeString) {
		return typeString + " remaining: " + Float.toString(Services.getGoopManager().typeRemaining(type));
	}
	

	private void drawText(int x, int y, String Text) {
		projection.set(x, y, 0);
		camera.unproject(projection);
		bitmapFont.draw(batch, Text, projection.x, projection.y);
	}
	
	private void conditionalText(int x, int y, String trueText, String falseText, boolean condition) {
		if (condition) drawText(x, y, trueText);
		else drawText(x, y, falseText);
	}

	@Override
	public void update(float dt) {
	}
}
