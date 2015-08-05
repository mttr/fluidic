package com.zzntd.fluidic.physics;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.zzntd.fluidic.Services;
import com.zzntd.fluidic.goop.GoopSegment;
import com.zzntd.fluidic.goop.SubSegment;

public class DebugRenderer {
	private ShapeRenderer renderer;
	
	public DebugRenderer() {
		renderer = new ShapeRenderer();
	}
	
	public void render() {
		Array<AbstractPhysicsComponent<? extends Body>> collidables = Services.getCollisionManager().getCollidables();
		
		renderer.begin(ShapeType.Line);
		for (AbstractPhysicsComponent<? extends Body> collidable : collidables) {
			final float[] vertices = collidable.body.getVerts();
			
			if (vertices.length > 3) {
				if (collidable.isActive) {
					renderer.setColor(Color.RED);
					renderer.polygon(vertices);
				}
				else {
					renderer.setColor(Color.WHITE);
					renderer.polygon(vertices);
				}
			}
		}
		
		for (GoopSegment goopSegment : Services.getGoopManager().getSegments()) {
			SubSegment current = goopSegment.getCurrentSubSegment();
			
			if (current != null) drawSegment(current);

			for (SubSegment sub : goopSegment.getSegments()) {
				drawSegment(sub);
			}
		}
		renderer.end();
	}

	private void drawSegment(SubSegment sub) {
		renderer.setColor(Color.WHITE);
		final Vector2 origin = sub.origin;
		final Vector2 endPoint = sub.endPoint;
		renderer.line(origin.x, origin.y, endPoint.x, endPoint.y, Color.GREEN, Color.GREEN);
		renderer.x(endPoint.x, endPoint.y, 0.25f);
		renderer.x(origin.x, origin.y, 0.125f);
	}

	public void setProjectionMatrix(Matrix4 matrix) {
		renderer.setProjectionMatrix(matrix);
	}
}
