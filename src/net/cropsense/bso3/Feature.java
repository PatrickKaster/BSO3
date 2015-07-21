package net.cropsense.bso3;

/**
 * base class for features in the implementation of
 * Borodin, Novotni, Klein: "progressive gap closing..." 
 * 
 * @author Patrick Kaster
 *
 */
public abstract class Feature
{
	public Feature nearestFeature;
	public double distanceToNearestFeature=Double.POSITIVE_INFINITY;;
	
	public abstract double getDistanceToFeature(Feature feature);
}
