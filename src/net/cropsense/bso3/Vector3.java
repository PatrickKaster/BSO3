package net.cropsense.bso3;


/**
 * a 3D vector with all necessary utility functions, representing a vertex in a
 * mesh data structure
 * 
 * @author Patrick Kaster
 *
 */
public class Vector3
{
	public double X;
	public double Y;
	public double Z;
	
	
	public Vector3()
	{
		X = 0;
		Y = 0;
		Z = 0;
	}

	public Vector3(Vector3 inVector)
	{
		X = inVector.X;
		Y = inVector.Y;
		Z = inVector.Z;
	}

	public Vector3(double inX, double inY, double inZ)
	{
		X = inX;
		Y = inY;
		Z = inZ;
	}

	public boolean IsValid()
	{
		return (!Double.isNaN(X) && !Double.isInfinite(X) && !Double.isNaN(Y)
				&& !Double.isInfinite(Y) && !Double.isNaN(Z) && !Double
				.isInfinite(Z));
	}

	public void Multiply(double scalar)
	{
		this.X *= scalar;
		this.Y *= scalar;
		this.Z *= scalar;
	}

	public static Vector3 mult(double scalar, Vector3 vector)
	{
		return new Vector3((scalar * vector.X), (scalar * vector.Y),
				(scalar * vector.Z));
	}

	public static Vector3 mult(Vector3 vector, double scalar)
	{
		return new Vector3((scalar * vector.X), (scalar * vector.Y),
				(scalar * vector.Z));
	}

	public static Vector3 minus(Vector3 left, Vector3 right)
	{
		return new Vector3((left.X - right.X), (left.Y - right.Y),
				(left.Z - right.Z));
	}

	public static Vector3 plus(Vector3 left, Vector3 right)
	{
		return new Vector3((left.X + right.X), (left.Y + right.Y),
				(left.Z + right.Z));
	}

	public double DotProduct(Vector3 right)
	{
		return (X * right.X) + (Y * right.Y) + (Z * right.Z);
	}

	public static double DotProduct(Vector3 left, Vector3 right)
	{
		return (left.X * right.X) + (left.Y * right.Y) + (left.Z * right.Z);
	}

	public static Vector3 CrossProduct(Vector3 left, Vector3 right)
	{
		return new Vector3((left.Y * right.Z) - (left.Z * right.Y),
				(left.Z * right.X) - (left.X * right.Z), (left.X * right.Y)
						- (left.Y * right.X));
	}

	public Vector3 CrossProduct(Vector3 right)
	{
		return new Vector3((Y * right.Z) - (Z * right.Y), (Z * right.X)
				- (X * right.Z), (X * right.Y) - (Y * right.X));
	}

	public static Vector3 Add(Vector3 left, Vector3 right)
	{
		return new Vector3((left.X + right.X), (left.Y + right.Y),
				(left.Z + right.Z));
	}

	public static Vector3 Subtract(Vector3 left, Vector3 right)
	{
		return new Vector3((left.X - right.X), (left.Y - right.Y),
				(left.Z - right.Z));
	}

	public Vector3 Negated()
	{
		return new Vector3(-this.X, -this.Y, -this.Z);
	}
	

	/**
	 * Returns a vector perpendicular to this one by "nulling" the smallest element and (skew-)swapping the other two.
	 * @return vector perpendicular to this one.
	 */
	public Vector3 getPerpendicular()
	{
		Vector3 perp = new Vector3();
		
		double X = Math.abs(this.X);
		double Y = Math.abs(this.Y);
		double Z = Math.abs(this.Z);
		
		// X is smallest entry
		if ( ( X <= Y ) && ( X <= Z ) )
		{
			perp.X = 0;
			perp.Y = -this.Z;
			perp.Z = this.Y;
		}
		// Y is smallest entry
		else if ( ( Y <= X ) && ( Y <= Z ) )
		{
			perp.X = -this.Z;
			perp.Y = 0;
			perp.Z = this.X;
		}
		// Z is smallest entry
		else 
		{
			perp.X = -this.Y;
			perp.Y = this.X;
			perp.Z = 0;
		}
		
		return perp;
	}

	public double Length()
	{
		double lengthSquare = X * X + Y * Y + Z * Z;
		return Math.sqrt(lengthSquare);
	}

	public double Distance(Vector3 v)
	{
		double lengthSquare = (X - v.X) * (X - v.X) + (Y - v.Y) * (Y - v.Y)
				+ (Z - v.Z) * (Z - v.Z);
		return Math.sqrt(lengthSquare);
	}

	public void Normalize()
	{
		double length = Length();
		X /= length;
		Y /= length;
		Z /= length;
	}
}