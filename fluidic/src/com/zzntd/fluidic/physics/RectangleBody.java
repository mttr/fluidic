package com.zzntd.fluidic.physics;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Intersector.MinimumTranslationVector;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.zzntd.fluidic.Services;
import com.zzntd.fluidic.Utils;
import com.zzntd.fluidic.core.ActorComposite;
import com.zzntd.fluidic.services.CollisionManager;

public class RectangleBody extends Body {
	
	public RectangleBody(AbstractPhysicsComponent<? extends Body> parent) {
		super(parent);
	}

	public final Rectangle rec = new Rectangle();
	public final Polygon poly = new Polygon();
	
	private final Vector2 tmp = new Vector2();
	private final MinimumTranslationVector translation = new MinimumTranslationVector();
	private boolean isSet = false;

	@Override
	public void update(float dt) {
		final ActorComposite phParent = parent.getParent();
		//poly.setRotation(phParent.angle); // FIXME?
		poly.setPosition(phParent.position.x + rec.width / 2f, 
						 phParent.position.y + rec.height / 2f);
		rec.set(phParent.rec);
		
		if (!isSet) {
			set(phParent.rec);
			isSet = true;
		}
	}

	@Override
	public void setPosition(Vector2 position) {
		rec.setPosition(position);
		poly.setPosition(position.x, position.y);
	}
	
	public void setRotation(float rotation) {
		poly.setRotation(rotation);
	}

	@Override
	public void set(Rectangle rec) {
		this.rec.set(rec);		
		rec.getCenter(tmp);		
		poly.setPosition(tmp.x, tmp.y);
		
		// FIXME If an object is loaded from xml, the physics comp will set the rec
		// before the rec has been given dimensions, which I think happens w/ texture
		// data.
		if (rec.width == 0) {
			return;
		}
		final float hwidth = rec.width / 2f;
		final float hheight = rec.height / 2f;
			
		float[] verts = poly.getVertices();
		if (verts.length == 0) {
			verts = new float[]{
				-hwidth, -hheight,
				-hwidth, hheight,
				hwidth, hheight,
				hwidth, -hheight
			};
		}
		else {
			verts = poly.getVertices();
			
			verts[0] = -hwidth;
			verts[1] = -hheight;

			verts[2] = -hwidth;
			verts[3] = hheight;

			verts[4] = hwidth;
			verts[5] = hheight;
			
			verts[6] = hwidth;
			verts[7] = -hheight;
		}

		poly.setVertices(verts);
	}

	@Override
	public Rectangle getBoundingRectangle() {
		return rec;
	}
	
	@Override
	public boolean overlaps(Body bodyB) {
		if (RectangleBody.class.isInstance(bodyB))	{
			final RectangleBody recB = (RectangleBody) bodyB;
			if (recB.poly.getVertices().length > 0) {
				return Intersector.overlapConvexPolygons(poly, recB.poly);			
			}
		}
		return false;
	}

	@Override
	public void determineCorrection(Body other, Rectangle intersection, Vector2 correction) {
		if (RectangleBody.class.isInstance(other))	{
			final RectangleBody bodyB = (RectangleBody) other;
			Intersector.overlapConvexPolygons(poly, bodyB.poly, translation);
			Intersector.intersectRectangles(rec, bodyB.rec, intersection);
			tmp.set(translation.normal).scl(translation.depth);
			correction.set(tmp);
		}
		else {
			// FIXME
			throw new RuntimeException("I'M NOT READY FOR THIS!!!!");
		}
		
		this.verticalCorrection(intersection, correction);
		this.horizontalCorrection(intersection, correction);							
	}
	
	public void horizontalCorrection(Rectangle intersection, Vector2 correction) {
		CollisionManager manager = Services.getCollisionManager();
		int status = manager.getStatus();
		int is_side = Utils.isSide(rec, intersection);
		correction.x = tmp.x * is_side;
		
		status = Utils.horizontalComp(is_side, status);
		manager.setStatus(status);
		
	}

	public void verticalCorrection(Rectangle intersection, Vector2 correction) {
		CollisionManager manager = Services.getCollisionManager();
		int status = manager.getStatus();
		int is_above = Utils.isAbove(rec, intersection);
		correction.y = -tmp.y * is_above;
		
		status = Utils.verticalComp(is_above, status);
		manager.setStatus(status);
	}

	@Override
	public float[] getVerts() {
		return poly.getTransformedVertices();
	}
	
	public boolean intersectsSegment(Vector2 p1, Vector2 p2) {
		Rectangle rec = poly.getBoundingRectangle();
		
		int cornerPoints = 0;
		for (int i = 0; i < 4; i++) {
			cornerPoints += Intersector.pointLineSide(p1.x, p1.y, p2.x, p2.y, 
					rec.x + rec.width * (i / 2), rec.y + rec.height * (((i + 1) / 2) % 2));
		}
		
		// If all of the corners are above, or all of them are below the line,
		// no intersection.
		if (Math.abs(cornerPoints) == 4) return false;
		
		float xTR = rec.x + rec.width, yTR = rec.y + rec.height;
		
		if (p1.x > xTR && p2.x > xTR) return false;
		if (p1.x < rec.x && p2.x < rec.x) return false;
		if (p1.y > yTR && p2.y > yTR) return false;
		if (p1.y < rec.y && p2.y < rec.y) return false;
		
		return true;
	}
}
