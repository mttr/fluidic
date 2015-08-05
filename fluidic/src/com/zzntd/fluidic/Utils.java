package com.zzntd.fluidic;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Logger;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pools;
import com.zzntd.fluidic.physics.AbstractPhysicsComponent;
import com.zzntd.fluidic.physics.RectangleBody;
import com.zzntd.fluidic.services.CollisionManager;

public class Utils {
	private static Vector2[] corners;
	private static float EPSILON = 0.00001f;
	private final static Matrix3 matrixA = new Matrix3();
	private final static Matrix3 matrixB = new Matrix3();
	private final static Array<Vector2> vectorList = new Array<Vector2>(10);
	private final static Vector2 util = new Vector2();
	
	public final static Pool<Vector2> vectorPool = Pools.get(Vector2.class); // FIXME This will not be a permanent resident.
	public final static Pool<TextureRegion> regionPool = Pools.get(TextureRegion.class);
	public static Logger logger = new Logger("fluidic");
		
	static {
		init();
	}
	
	private static void init() {
		corners = new Vector2[4];
		for (int i = 0; i < 4; i++) {
			corners[i] = vectorPool.obtain();
		}		
	}
	
	public static boolean checkFlags(int i, int flags) {
		return (i & flags) == flags;
	}
	
	public static Array<Vector2> getCorners(Rectangle... recs) {
		if (vectorList.size >= 0) {
			vectorPool.freeAll(vectorList);
			vectorList.clear();
		}
		for (Rectangle rec : recs) {
	
			for (int i = 0; i < 4; i++) {
				Vector2 v = vectorPool.obtain();
				v.set(rec.x + rec.width * (i / 2), rec.y + rec.height * (((i + 1) / 2) % 2));
				vectorList.add(v);
			}
		}
		
		return vectorList;
	}
	
	public static boolean floatEqual(float a, float b, float epsilon) {
		return (Math.abs(a - b) < epsilon);
	}

	public static boolean floatEqual(float a, float b) {
		return (Math.abs(a - b) < EPSILON);
	}
	
	/**
	 * Scale a rectangle by `scalar`.
	 * @param rec Rectangle to scale.
	 * @param scalar Constant to scale by.
	 * @return Rectangle for chaining.
	 */
	public static Rectangle scaleRec(Rectangle rec, float scalar) {
		rec.width *= scalar;
		rec.height *= scalar;
		rec.x *= scalar;
		rec.y *= scalar;
		
		return rec;
	}
	
	public static void rotateRecPositionVector(final Vector2 vec, Rectangle rec, float degrees) {
		// FIXME This method can possibly be optimized. If degrees hasn't changed, relativePos doesn't
		// change (well, before making it un-relative again).
		final Vector2 center = corners[0];
		final Vector2 relativePos = corners[1];
		rec.getCenter(center);
		
		relativePos.set(vec);
		relativePos.sub(center);
		
		matrixA.setToRotation(degrees);
		matrixB.val[Matrix3.M00] = relativePos.x;
		matrixB.val[Matrix3.M10] = relativePos.y;
		
		matrixA.mul(matrixB);
		
		relativePos.x = matrixA.val[Matrix3.M00];
		relativePos.y = matrixA.val[Matrix3.M10];
		
		relativePos.add(center);
		vec.set(relativePos);
	}

	public static boolean collidableContains(AbstractPhysicsComponent<RectangleBody> collidable, Vector2 point) {
		return collidable.body.poly.contains(point.x, point.y);
	}
	
	public static Vector2 cornerInRec(Rectangle rec, Rectangle comp) {
		Array<Vector2> corners = getCorners(rec);
		
		for (Vector2 v : corners) {
			if (comp.contains(v)) return v;
		}
		
		return null;
	}

	/**
	 * If two polygons are intersecting, get the projection(?) of the intersecting points
	 * of `b` on `a`. (OK, at the time of writing this, might not be 100% true in all cases)
	 * @param a
	 * @param b
	 * @return
	 */
	public static Array<Vector2> getProjectedVertices(Polygon a, Polygon b) {
		float[] verts1 = a.getTransformedVertices();
		float[] verts2 = b.getTransformedVertices();
		
		if (vectorList.size >= 0) {
			vectorPool.freeAll(vectorList);
			vectorList.clear();
		}
		
		util.set(Vector2.Zero);
		
		for (int i = 0; i < verts1.length; i += 2) {
			float ax1 = verts1[i];
			float ay1 = verts1[i + 1];
			float ax2 = verts1[(i + 2) % verts1.length];
			float ay2 = verts1[(i + 3) % verts1.length];
			
			for (int j = 0; j < verts2.length; j += 2) {
				float bx1 = verts2[j];
				float by1 = verts2[j + 1];
				float bx2 = verts2[(j + 2) % verts2.length];
				float by2 = verts2[(j + 3) % verts2.length];
				
				if (Utils.intersectSegments(ax1, ay1, ax2, ay2, bx1, by1, bx2, by2, util)) {
					Vector2 newVert = vectorPool.obtain();
					newVert.set(util);
					
					vectorList.add(newVert);
				}
			}
		}
		
		return vectorList;
	}

	public static Array<Vector2> getLineIntersections(Polygon a, Vector2 p1, Vector2 p2) {
		if (vectorList.size >= 0) {
			vectorPool.freeAll(vectorList);
			vectorList.clear();
		}
		
		float[] verts = a.getTransformedVertices();
		
		
		for (int i = 0; i < verts.length; i += 2) {
			float ax1 = verts[i];
			float ay1 = verts[i + 1];
			float ax2 = verts[(i + 2) % verts.length];
			float ay2 = verts[(i + 3) % verts.length];
			
			if (Intersector.intersectLines(p1.x, p1.y, p2.x, p2.y, ax1, ay1, ax2, ay2, util)) {
				Vector2 newPoint = vectorPool.obtain();
				newPoint.set(util);
				vectorList.add(newPoint);
			}
		}
		
		return vectorList;
	}
			
	public static boolean farthestAligned(Array<Vector2> array, Vector2 p1,
			Vector2 p2, Vector2 dest) {
		Vector2 res = null;
		
		float max = 0;
		for (Vector2 v : array) {
			if (Intersector.pointLineSide(p1, p2, v) == 0) {
				float dst = p1.dst2(v);
				if (dst > max) {
					max = dst;
					res = v;
				}
			}
			else if (Utils.axisAligned(p1, p2, v)) {
				float dst = p1.dst2(v);
				if (dst > max) {
					max = dst;
					res = v;
				}
			}
		}
		if (res == null) {
			return false;
		}
		dest.set(res);
		
		return true;
	}

	public static boolean axisAligned(Vector2 a, Vector2 b, Vector2 c) {
		final float aaEps = 0.0001f;
		if (floatEqual(a.x, b.x, aaEps) && floatEqual(b.x, c.x, aaEps)) return true;
		else if (floatEqual(a.y, b.y, aaEps) && floatEqual(b.y, c.y, aaEps)) return true;
		else return false;
	}
	
	public static boolean axisAligned(Vector2 a, Vector2 b) {
		final float aaEps = 0.0001f;
		if (floatEqual(a.x, b.x, aaEps)) return true;
		else if (floatEqual(a.y, b.y, aaEps)) return true;
		else return false;
	}

	public static Vector2 farthest(Array<Vector2> array, Vector2 comp, Vector2 dest) {
		Vector2 res = null;
		
		float max = 0;
		for (Vector2 v : array) {
			float dst = comp.dst2(v);
			if (dst > max) {
				max = dst;
				res = v;
			}
		}
		dest.set(res);
		
		return res;
	}

	public static Vector2 closest(Array<Vector2> array, Vector2 comp, Vector2 dest) {
		Vector2 res = null;
		
		float min = Float.POSITIVE_INFINITY;
		for (Vector2 v : array) {
			float dst = comp.dst2(v);
			if (dst < min) {
				min = dst;
				res = v;
			}
		}
		dest.set(res);
		
		return res;
	}

	public static final Vector2 farthest(final Vector2 comp, final Vector2 a, final Vector2 b) {
		float ac = comp.dst2(a);
		float bc = comp.dst2(b);
		
		if (ac > bc) return a;
		if (bc > ac) return b;
		else return Vector2.Zero; // FIXME ...?!?!?!
	}

	public static final Vector2 farthest(float ax, float ay, float bx, float by, final Vector2 comp, final Vector2 dest) {
		float ac = comp.dst2(ax, ay);
		float bc = comp.dst2(bx, by);
		
		if (ac > bc) return dest.set(ax, ay);
		if (bc > ac) return dest.set(bx, by);
		else return Vector2.Zero; // FIXME ...?!?!?!
	}

	public static final Vector2 closest(float ax, float ay, float bx, float by, final Vector2 comp, final Vector2 dest) {
		float ac = comp.dst2(ax, ay);
		float bc = comp.dst2(bx, by);
		
		if (ac < bc) return dest.set(ax, ay);
		if (bc < ac) return dest.set(bx, by);
		else return Vector2.Zero; // FIXME ...?!?!?!
	}

	/**
	 * Vertical comparison between boxA and boxB
	 * @param boxA
	 * @param boxB
	 * @return 1 for above; 0 if on point; -1 if below (relative to boxA).
	 */
	public static int isAbove(Rectangle boxA, Rectangle boxB) {
		boxA.getCenter(CollisionManager.utilityVector);
		return Intersector.pointLineSide(boxA.x, CollisionManager.utilityVector.y,
							  			  boxA.x + boxA.width, CollisionManager.utilityVector.y,
							  			  boxB.x, boxB.y);
	}

	/**
	 * Horizontal comparison between boxA and boxB.
	 * @param boxA
	 * @param boxB
	 * @return -1 for left; 0 if on point; 1 if right (relative to boxA).
	 */
	public static int isSide(Rectangle boxA, Rectangle boxB) {
		boxA.getCenter(CollisionManager.utilityVector);
		
		return -Intersector.pointLineSide(CollisionManager.utilityVector.x, boxA.y,
	  			   						   CollisionManager.utilityVector.x, boxA.y + boxA.height,
	  			   						   boxB.x, boxB.y);		
	}

	public static int horizontalComp(int is_side, int status) {
		switch (is_side) {
		case 0: status = status | CollisionManager.IS_CENTER_VER; break;
		case -1: status = status | CollisionManager.IS_LEFT; break;
		case 1: status = status | CollisionManager.IS_RIGHT; break;
		}
		return status;
	}

	public static int verticalComp(int is_above, int status) {
		switch (is_above) {
		case 0: status = status | CollisionManager.IS_CENTER_HOR; break;
		case -1: status = status | CollisionManager.IS_BELOW; break;
		case 1: status = status | CollisionManager.IS_ABOVE; break;
		}
		
		return status;
	}

	/**
	 * Compare boxA and boxB and return a status integer with relevant flags
	 * set.
	 * @param boxA
	 * @param boxB
	 * @return
	 */
	public static int getLocationStatus(Rectangle boxA, Rectangle boxB) {
		int status = 0;
		status = horizontalComp(isAbove(boxA, boxB), status);
		status = verticalComp(isSide(boxA, boxB), status);
		return status;
	}

	public static int getLocationStatus(int isAbove, int isSide) {
		int status = 0;
		status = horizontalComp(isSide, status);
		status = verticalComp(isAbove, status);
		return status;
	}

	public static boolean intersectSegments(float x1, float y1, float x2,
			float y2, float x3, float y3, float x4, float y4,
			Vector2 inter) {
		
		float d = (y4 - y3) * (x2 - x1) - (x4 - x3) * (y2 - y1);
		if (d == 0) return false;
	
	    float ua = ((x4 - x3) * (y1 - y3) - (y4 - y3) * (x1 - x3)) / d;
	    float ub = ((x2 - x1) * (y1 - y3) - (y2 - y1) * (x1 - x3)) / d;
	
	    if (ua < 0 || ua > 1) return false;
	    if (ub < 0 || ub > 1) return false;
	
	    if (inter != null) inter.set(x1 + (x2 - x1) * ua, y1 + (y2 - y1) * ua);
	    return true;
	}
}
