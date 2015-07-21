package net.cropsense.bso3;


/**
 * @author Patrick Kaster
 * 
 * 	Class holding information about edges to split in the mesh filtering step. Purpose is
 *  to detect edges to split in one step and then split them later in another step to avoid
 *  concurrent modification of Edges list.
 */
public class SplitInfo
{
	int halfEdgeIndex;
	int startIndex;
	int endIndex;
}
