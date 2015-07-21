package net.cropsense.bso3;



/**
 * a Polygon consisting of an index to the first half-edge of the polygon
 * and the plane the polygon lies in
 * 
 * @author Patrick Kaster
 *
 */
public class Polygon
{
	public int FirstIndex;
	public int PlaneIndex;
	public PolygonCategory Category;
	public boolean Visible;
	public AABB Bounds;

	public Polygon()
	{
		FirstIndex = -1;
		PlaneIndex = -1;
		Category = PolygonCategory.Aligned;
		Visible = false;
		Bounds = new AABB();
	}
}
