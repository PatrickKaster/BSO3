package net.cropsense.bso3;

import java.util.Comparator;

/**
 * @author Patrick Kaster
 *
 * This class implements an comparator for two Features, comparing their ( precalculated ) distances to their respective
 * nearest Feature. 
 */
public class FeatureComparator implements Comparator<Feature>
{
	
	@Override
	public int compare(Feature arg0, Feature arg1)
	{
		if (arg0.distanceToNearestFeature < arg1.distanceToNearestFeature ) return -1;
		else if ( arg0.distanceToNearestFeature > arg1.distanceToNearestFeature ) return 1;
		
		return 0;
	}
}