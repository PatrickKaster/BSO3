package net.cropsense.bso3;


/**
 * a Plane with distance/intersection functions
 * 
 * @author Patrick Kaster
 *
 */
public class Plane
{
	public double A;
	public double B;
	public double C;
	public double D;

	public Plane()
	{
		A = 1f;
		B = 0f;
		C = 0f;
		D = 0f;
	}

	public Plane(Plane inPlane)
	{
		A = inPlane.A;
		B = inPlane.B;
		C = inPlane.C;
		D = inPlane.D;
	}

	public Plane(Vector3 inNormal, double inD)
	{
		A = inNormal.X;
		B = inNormal.Y;
		C = inNormal.Z;
		D = inD;
	}

	public Plane(double inA, double inB, double inC, double inD)
	{
		A = inA;
		B = inB;
		C = inC;
		D = inD;
	}

	public Vector3 Normal()
	{
		return new Vector3(A, B, C);
	}

	public void setNormal(Vector3 value)
	{
		A = value.X;
		B = value.Y;
		C = value.Z;
	}

	public Vector3 PointOnPlane()
	{
		return Vector3.mult(Normal(), D);
	}

	public static Vector3 Intersection(Plane inPlane1, Plane inPlane2,
			Plane inPlane3)
	{
		// intersection point with 3 planes
		// {
		// x = -( c2*b1*d3-c2*b3*d1+b3*c1*d2+c3*b2*d1-b1*c3*d2-c1*b2*d3)/
		// (-c2*b3*a1+c3*b2*a1-b1*c3*a2-c1*b2*a3+b3*c1*a2+c2*b1*a3),
		// y = ( c3*a2*d1-c3*a1*d2-c2*a3*d1+d2*c1*a3-a2*c1*d3+c2*d3*a1)/
		// (-c2*b3*a1+c3*b2*a1-b1*c3*a2-c1*b2*a3+b3*c1*a2+c2*b1*a3),
		// z = -(-a2*b1*d3+a2*b3*d1-a3*b2*d1+d3*b2*a1-d2*b3*a1+d2*b1*a3)/
		// (-c2*b3*a1+c3*b2*a1-b1*c3*a2-c1*b2*a3+b3*c1*a2+c2*b1*a3)
		// }

		double bc1 = (inPlane1.B * inPlane3.C) - (inPlane3.B * inPlane1.C);
		double bc2 = (inPlane2.B * inPlane1.C) - (inPlane1.B * inPlane2.C);
		double bc3 = (inPlane3.B * inPlane2.C) - (inPlane2.B * inPlane3.C);

		double ad1 = (inPlane1.A * inPlane3.D) - (inPlane3.A * inPlane1.D);
		double ad2 = (inPlane2.A * inPlane1.D) - (inPlane1.A * inPlane2.D);
		double ad3 = (inPlane3.A * inPlane2.D) - (inPlane2.A * inPlane3.D);

		double x = -((inPlane1.D * bc3) + (inPlane2.D * bc1) + (inPlane3.D * bc2));
		double y = -((inPlane1.C * ad3) + (inPlane2.C * ad1) + (inPlane3.C * ad2));
		double z = +((inPlane1.B * ad3) + (inPlane2.B * ad1) + (inPlane3.B * ad2));
		double w = -((inPlane1.A * bc3) + (inPlane2.A * bc1) + (inPlane3.A * bc2));

		// better to have detectable invalid values than to have reaaaaaaally
		// big values
		if (w > -VarsConstants.NormalEpsilon && w < VarsConstants.NormalEpsilon)
		{
			return new Vector3(Double.NaN, Double.NaN, Double.NaN);
		} else
			return new Vector3(x / w, y / w, z / w);
	}

	public static Vector3 Intersection(Vector3 start, Vector3 end,
			double sdist, double edist)
	{
		Vector3 vector = Vector3.minus(end, start);
		double length = edist - sdist;
		double delta = edist / length;

		return Vector3.minus(end, Vector3.mult(delta, vector));
	}

	public Vector3 Intersection(Vector3 start, Vector3 end)
	{
		// return Intersection(start, end, Distance(start), Distance(end));
		double edist = Distance(end);
		double sdist = Distance(start);
		Vector3 vector = Vector3.minus(end, start);
		double length = edist - sdist;
		double delta = edist / length;

		return Vector3.minus(end, Vector3.mult(delta, vector));
	}

	public double Distance(double x, double y, double z)
	{
		return ((A * x) + (B * y) + (C * z) - (D));
	}

	public double Distance(Vector3 vertex)
	{
		return ((A * vertex.X) + (B * vertex.Y) + (C * vertex.Z) - (D));
	}

	static public PlaneSideResult OnSide(double distance, double epsilon)
	{
		if (distance > epsilon)
			return PlaneSideResult.Outside;
		else if (distance < -epsilon)
			return PlaneSideResult.Inside;
		else
			return PlaneSideResult.Intersects;
	}

	public static PlaneSideResult OnSide(double distance)
	{
		if (distance > VarsConstants.DistanceEpsilon)
			return PlaneSideResult.Outside;
		else if (distance < -VarsConstants.DistanceEpsilon)
			return PlaneSideResult.Inside;
		else
			return PlaneSideResult.Intersects;
	}

	public PlaneSideResult OnSide(double x, double y, double z)
	{
		return OnSide(Distance(x, y, z));
	}

	public PlaneSideResult OnSide(Vector3 vertex)
	{
		return OnSide(Distance(vertex));
	}

	public PlaneSideResult OnSide(AABB bounds)
	{
		int x = A >= 0 ? bounds.MinX : bounds.MaxX;
		int y = B >= 0 ? bounds.MinY : bounds.MaxY;
		int z = C >= 0 ? bounds.MinZ : bounds.MaxZ;
		return OnSide(Distance(x, y, z));
	}

	public PlaneSideResult OnSide(AABB bounds, Vector3 translation)
	{
		int backward_x = A <= 0 ? bounds.MinX : bounds.MaxX;
		int backward_y = B <= 0 ? bounds.MinY : bounds.MaxY;
		int backward_z = C <= 0 ? bounds.MinZ : bounds.MaxZ;
		double distance = Distance(backward_x + translation.X, backward_y
				+ translation.Y, backward_z + translation.Z);
		PlaneSideResult side = OnSide(distance);
		if (side == PlaneSideResult.Inside)
			return PlaneSideResult.Inside;
		int forward_x = A >= 0 ? bounds.MinX : bounds.MaxX;
		int forward_y = B >= 0 ? bounds.MinY : bounds.MaxY;
		int forward_z = C >= 0 ? bounds.MinZ : bounds.MaxZ;
		distance = Distance(forward_x + translation.X, forward_y
				+ translation.Y, forward_z + translation.Z);
		side = OnSide(distance);
		if (side == PlaneSideResult.Outside)
			return PlaneSideResult.Outside;
		return PlaneSideResult.Intersects;
	}

	public Plane Negated()
	{
		return new Plane(-A, -B, -C, -D);
	}

	public void Translate(Vector3 translation)
	{
		// translated offset = plane.Normal.Dotproduct(translation)
		// normal = A,B,C
		D += (A * translation.X) + (B * translation.Y) + (C * translation.Z);
	}

	public static Plane Translated(Plane plane, Vector3 translation)
	{
		return new Plane(plane.A, plane.B, plane.C,
		// translated offset = plane.Normal.Dotproduct(translation)
				// normal = A,B,C
				plane.D + (plane.A * translation.X) + (plane.B * translation.Y)
						+ (plane.C * translation.Z));
	}

	public static Plane Translated(Plane plane, float translateX,
			float translateY, float translateZ)
	{
		return new Plane(plane.A, plane.B, plane.C,
		// translated offset = plane.Normal.Dotproduct(translation)
				// normal = A,B,C
				plane.D + (plane.A * translateX) + (plane.B * translateY)
						+ (plane.C * translateZ));
	}

	public int GetHashCode()
	{
		return new Float(A).hashCode() ^ new Float(B).hashCode()
				^ new Float(C).hashCode() ^ new Float(D).hashCode();
	}

	public boolean equals(Object obj)
	{
		if (obj == null)
			return false;
		if (!(obj instanceof Plane))
			return false;
		if (this == obj)
			return true;

		Plane other = (Plane) obj;
		return D == other.D && A == other.A && B == other.B && C == other.C;
	}

	public String toString()
	{
		return "(" + A + ", " + B + ", " + C + ")" + ", " + D;
	}
}
