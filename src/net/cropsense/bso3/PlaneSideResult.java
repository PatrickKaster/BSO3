package net.cropsense.bso3;



/**
 * enumeration of plane side results
 * 
 * @author Patrick Kaster
 *
 */
public enum PlaneSideResult
{
	Intersects, // On plane
	Inside, // Negative side of plane / 'inside' half-space
	Outside
	// Positive side of plane / 'outside' half-space
}
