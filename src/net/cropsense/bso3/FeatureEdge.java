package net.cropsense.bso3;

import java.util.ArrayDeque;

/**
 * An edge feature in the implementation of
 * Borodin, Novotni, Klein: "progressive gap closing..." 
 * 
 * @author Patrick Kaster
 *
 */
public class FeatureEdge extends Feature
{
	public HalfEdge edge;
	public HalfEdge twin;
	public Vector3 edgeStart;
	public Vector3 edgeEnd;
	public ArrayDeque<FeatureVertex> correspondingFeatures;
	
	public FeatureEdge(HalfEdge edge, HalfEdge twin, Vector3 edgeStart, Vector3 edgeEnd)
	{
		this.edge = edge;
		this.twin = twin;
		this.edgeStart = edgeStart;
		this.edgeEnd = edgeEnd;
		this.correspondingFeatures = new ArrayDeque<FeatureVertex>();
	}
	
	@Override
	public double getDistanceToFeature(Feature feature)
	{
		if ( feature instanceof FeatureVertex )
		{
			/* the feature's Vertex */
			Vector3 point = ( ((FeatureVertex) feature).point );
			
			/*
			 *  construct edge directional vector from edgeStart, edgeEnd
			 */
			Vector3 edgeDirection = Vector3.minus(this.edgeStart, this.edgeEnd);
			edgeDirection.Normalize();
			
			/* see Schneider/Eberly "Geometric Tools for Computer Graphics ", sec. 10.2, for distance point to line calculation
			 * point is Q, edgeStart is P
			 */
			double t = edgeDirection.DotProduct( ( Vector3.minus(point, this.edgeStart) ) );
			Vector3 qPrime =  Vector3.plus(this.edgeStart, Vector3.mult(t, edgeDirection));
			Vector3 vec = Vector3.minus(point, qPrime);
			
			/* Schneider/Eberly are computing distance squared, not euclidean distance, so take square root */
			return Math.sqrt(Vector3.DotProduct(vec, vec));			
		}
		else throw new IllegalArgumentException("Illegal argument for distance calculation. Only matching feature for an edge is a vertex");
	}
	
	public Vector3 getOrthogonalProjectionOntoEdge(Vector3 vertex)
	{
		/*
		 *  construct edge directional vector from edgeStart, edgeEnd
		 */
		Vector3 edgeDirection = Vector3.minus(this.edgeStart, this.edgeStart);
		Vector3 edgeDirectionHat = new Vector3(edgeDirection);
		edgeDirectionHat.Normalize();
		
		/* see Schneider/Eberly "Geometric Tools for Computer Graphics ", sec. 10.2, for distance point to line calculation
		 * vertex is Q, edgeStart is P
		 */
		Vector3 QminusP = Vector3.minus(vertex, this.edgeStart);
		double t = Vector3.DotProduct(edgeDirectionHat, QminusP);
		Vector3 qPrime =  Vector3.plus(this.edgeEnd, Vector3.mult(t, edgeDirection));
		
		return qPrime;
	}
	

	/**
	 * Projects vertex onto the line starting in edgeStart. Projection falls onto edge, if the projected
	 * vertex falls between edgeStart and edgeEnd onto the line.
	 *  
	 * @param vertex to project onto edge
	 * @return true if orthogonal projection falls onto line, else false
	 */
	public boolean orthogonalProjectionOntoEdge(Vector3 vertex)
	{
		Vector3 qPrime = getOrthogonalProjectionOntoEdge(vertex);
		
		if ( 
				( this.edgeStart.Distance(qPrime) <= this.edgeStart.Distance(this.edgeEnd) )  &&
				( this.edgeEnd.Distance(qPrime) <= this.edgeEnd.Distance(this.edgeStart) ) 
		   )
		return true;
		
		return false;
	}
	
	public void addCorrespondingFeature(FeatureVertex vertex)
	{
		this.correspondingFeatures.add(vertex);
	}
	
	public Vector3 getNearestEdgeVertex(Vector3 point)
	{
		if (point.Distance(this.edgeStart) <= point.Distance(edgeEnd)) return this.edgeStart;
		else return this.edgeEnd;
	}
}
