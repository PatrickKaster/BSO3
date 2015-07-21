package net.cropsense.bso3;

import java.util.ArrayList;

/**
 * a vertex feature in the implementation of
 * Borodin, Novotni, Klein: "progressive gap closing..." 
 * 
 * @author Patrick Kaster
 *
 */
public class FeatureVertex extends Feature
{
	public Vector3 point;
	
	public FeatureVertex(Vector3 point)
	{
		this.point = point;
	}
	
	@Override
	public double getDistanceToFeature(Feature feature)
	{
		if ( feature instanceof FeatureVertex )
		{
			/* cast to FeatureVertex as calculate distance to it's point */
			return point.Distance( ((FeatureVertex) feature).point );
		}
		else if ( feature instanceof FeatureEdge )
		{
			/* cast to FeatureEdge and let that class do the calculation */
			return ((FeatureEdge) feature).getDistanceToFeature(this);
		}

		return 0;
	}
	
	public void findNearestBoundaryEdge(ArrayList<Feature> boundaryFeatures)
	{
		for (Feature feature : boundaryFeatures)
		{
			if ( feature instanceof FeatureEdge )
			{
				FeatureEdge featureEdge = (FeatureEdge) feature;
				
				/* incidence check, if edge is incident to vertex, skip */
				if ( featureEdge.edgeStart == this.point || featureEdge.edgeEnd == this.point  ) {continue;}
				/* also continue if vertices of other edge are nearly the same */
				//if ( featureEdge.edgeStart.Distance(this.point) < VarsConstants.DistanceEpsilon || featureEdge.edgeEnd.Distance(this.point) < VarsConstants.DistanceEpsilon ) {continue;}
				
				double distance = getDistanceToFeature(featureEdge);
				
				if ( distance <= this.distanceToNearestFeature )
				{
					this.distanceToNearestFeature = distance;
					this.nearestFeature = featureEdge;
				}
			}
		}
		
		/* note: Nearest feature is always if type edge, unless following check determines orthogonal projection is not possible, so nearest vertex of edge is set
		 * as nearest feature.
		 */
		FeatureEdge nearestEdge = (FeatureEdge) this.nearestFeature;
		/* orthogonal projection doesn't fall onto edge, set nearest vertex of edge as nearest feature instead */
		if ( !nearestEdge.orthogonalProjectionOntoEdge(this.point) )
		{
			//System.out.println("projection doesn't fall onto edge.");
				
			/* get nearest vertex incident to edge */
			Vector3 nearestEdgeVertex = nearestEdge.getNearestEdgeVertex(this.point);
			/* look up FeatureVertex for this vertex */
			for ( Feature feature2 : boundaryFeatures )
			{
				if ( feature2 instanceof FeatureVertex )
				{
					FeatureVertex featureVertex = (FeatureVertex) feature2;
					if ( featureVertex.point == nearestEdgeVertex )
					{
						this.distanceToNearestFeature = this.point.Distance(nearestEdgeVertex);
						this.nearestFeature = featureVertex;
						break;
					}
				}
			}
		}
		
		/* in the end, when nearest feature was found, set this vertex as corresponding feature for boundaryEdge */
		if ( this.nearestFeature instanceof FeatureEdge )
		{
			FeatureEdge featureEdge = (FeatureEdge) this.nearestFeature;
			featureEdge.addCorrespondingFeature(this);
		}
	}
	
	public void resetCorrespondingFeatures()
	{
		this.distanceToNearestFeature = Double.POSITIVE_INFINITY;
		this.nearestFeature = null;
	}
}