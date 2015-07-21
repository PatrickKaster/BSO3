package net.cropsense.bso3;

import java.util.Comparator;

/**
 * @author Patrick Kaster
 *
 * This class implements an comparator for two points on a line, comparing their ( precalculated ) weight ( distance ),
 * from the line's origin. 
 * Note: It is not checked, if the two points to be compared actually lie on the line. Only their
 * distance from the line's origin is regarded.
 * 
 */
public class PointOnLineComparator implements Comparator<PointOnLine>
{
	
	@Override
	public int compare(PointOnLine arg0, PointOnLine arg1)
	{
		if (arg0.weight < arg1.weight ) return -1;
		else if ( arg0.weight > arg1.weight ) return 1;
		
		return 0;
	}
}