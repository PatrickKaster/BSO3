package net.cropsense.bso3;

/**
 * @author Patrick Kaster
 * 
 * this class represents a vertex/point and it's projection onto an infinite line, with a specific
 *  weight/distance
 */
public class PointOnLine
{
	public Vector3 point;
	public Vector3 pointProjection;
	public double weight;
	
	public PointOnLine(Vector3 point, Vector3 pointProjection, double weight)
	{
		this.point = point;
		this.pointProjection = pointProjection;
		this.weight = weight;
	}
}