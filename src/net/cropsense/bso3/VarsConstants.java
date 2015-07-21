package net.cropsense.bso3;


/**
 * @author Patrick Kaster
 * 
 * class defining global vars and constants
 */
public class VarsConstants
{
	public static final double DistanceEpsilon = 0.000001;
	public static final double NormalEpsilon = (1.0 / 65535.0);
	public static double PointOnLineTolerance = DistanceEpsilon;
	public static final double EdgeLengthEpsilon = DistanceEpsilon;
	public static int PlanesSamplingSpheres = 8;
	public static int PlanesSamplingCones = 25;
	public static double DistanceEpsilonWelding = DistanceEpsilon;
	public static final double epsilonTriangulator3 = 0.001;
}
