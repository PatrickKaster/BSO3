package net.cropsense.bso3;

import java.util.ArrayList;
import java.util.Collections;

/**
 /* @author Patrick Kaster
 * 
 * This class implements an infiniteLine, the intersection of two perpendicular planes, and an ordered set of points
 * lying on this line. Points are ordered by their distance from the line's origin.
 */
public class InfiniteLine
{
	public Vector3 origin;
	private Vector3 direction;
	private Plane Plane1, Plane2;
	private ArrayList<PointOnLine> Points;
		
	private boolean pointsSorted;
	
	/**
	 * Constructs a new infinite line with initially empty point set.
	 * 
	 * @param origin vector where the originates ( start if edge this line is derived from )
	 * @param direction direction vector for the line in the planes' intersection ( start - end of edge this line is derived from )
	 */
	public InfiniteLine(Vector3 origin, Vector3 direction)
	{
		this.origin = origin;
		this.direction = direction;
		
		/* Construct two vectors perpendicular to the edge's direction and to each other and normalizing them, getting
		 * the normals on the two planes, who's intersection is the line.
		 * This is done by construction of a perpendicular vector to the edge's direction and calculating the cross product
		 * of these two to gain the third vector.
		 */
		direction.Normalize();
		Vector3 Normal1 = direction.getPerpendicular();
		Normal1.Normalize();
		Vector3 Normal2 = direction.CrossProduct(Normal1);
		Normal2.Normalize();
		
		/* Construct the intersecting planes from their normals and a scalar d, 
		 * with d := -normal_{1|2} * point on plane */
		this.Plane1 = new Plane(Normal1, Vector3.DotProduct(Normal1.Negated(), origin));
		this.Plane2 = new Plane(Normal2, Vector3.DotProduct(Normal2.Negated(), origin));
		
		this.Points = new ArrayList<PointOnLine>(2);
	}
	
	/**
	 * Check if an Edge falls onto this InfiniteLine.
	 * @param edgeStart start vertex of Edge
	 * @param edgeEnd end vertex of Edge
	 * @return true if start and end vertex lie within tolerance of both intersecting planes, else false.
	 */
	public boolean EdgeOnLine(Vector3 edgeStart, Vector3 edgeEnd)
	{
		if ( Math.abs( this.Plane1.Distance(edgeStart) ) > VarsConstants.PointOnLineTolerance ) return false;
		if ( Math.abs( this.Plane2.Distance(edgeStart) ) > VarsConstants.PointOnLineTolerance ) return false;
		if ( Math.abs( this.Plane1.Distance(edgeEnd) ) > VarsConstants.PointOnLineTolerance ) return false;
		if ( Math.abs( this.Plane2.Distance(edgeEnd) ) > VarsConstants.PointOnLineTolerance ) return false;
		
		return true;
	}
	
	/**
	 * Adds a point to this line. Make sure to check, if your point is lying on this line by calling EdgeOnLine first!
	 * 
	 * @param point Point to add to this InfiniteLine. Points are ordered by their distance from the line's origin.
	 */
	public void addPoint(Vector3 point)
	{
		/* project point onto this infinite line */
		Vector3 projPoint = projectPointOntoLine(point);
		
		/* calculate distance from line's origin, weighted with (opposite) line's direction of projected point*/
		double distance  = Math.signum(this.direction.DotProduct(point)) * this.origin.Distance(point);
		PointOnLine linePoint = new PointOnLine(point, projPoint, distance);
		
		this.Points.add(linePoint);
		this.pointsSorted = false;
	}
	
	/**
	 * get all points lying on this line
	 * 
	 * @return Points on this line in order of their distance from the line's origin.
	 */
	ArrayList<PointOnLine> getPointsOnLine()
	{
		if ( !pointsSorted )
		{
			Collections.sort(this.Points, new PointOnLineComparator());
			this.pointsSorted = true;
		}
		
		return this.Points;
	}
	
	/**
	 * project a given vertex onto this line
	 *
	 * @param point vertex to be projected onto this infinite line
	 * @return projection of vertex onto line
	 */
	public Vector3 projectPointOntoLine(Vector3 point)
	{
		/* construct vector that's orthogonal to plane normals by cross product */
		Vector3 newPlaneNormal = Vector3.CrossProduct(this.Plane1.Normal(), this.Plane2.Normal());
		newPlaneNormal.Normalize();
		
		/* construct new Plane with newNormal's orientation, going through point */
		Plane newPlane = new Plane(newPlaneNormal, Vector3.DotProduct(newPlaneNormal.Negated(), point));
		
		/* intersection of these three planes is projection of point onto line defined by
		 * the intersection of Plane1/Plane2 */
		Vector3 pointProjected = Plane.Intersection(this.Plane1, this.Plane2, newPlane);
		
		return pointProjected;
	}
	
	public void testProjection(Vector3 point)
	{
		System.out.println("distance to plane1: "+this.Plane1.Distance(point));
		System.out.println("distance to plane2: "+this.Plane2.Distance(point));
		System.out.println("distance to origin: "+this.origin.Distance(point));
	}
}