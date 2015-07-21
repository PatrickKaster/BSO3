package net.cropsense.bso3;


/**
 * enumeration of polygon split results after split of polygon by plane
 * 
 * @author Patrick Kaster
 *
 */
public enum PolygonSplitResult
{
	CompletelyInside, // Polygon is completely inside half-space defined by
	// plane
	CompletelyOutside, // Polygon is completely outside half-space defined by
	// plane
	Split, // Polygon has been split into two parts by plane
	PlaneAligned, // Polygon is aligned with cutting plane and the polygons'
	// normal points in the same direction
	PlaneOppositeAligned
	// Polygon is aligned with cutting plane and the polygons' normal points in
	// the opposite direction
}
