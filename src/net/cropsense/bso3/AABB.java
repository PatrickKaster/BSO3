package net.cropsense.bso3;


/**
 * This class represents an axis aligned bounding box in a bounding
 *  volume hierachy
 * 
 * @author Patrick Kaster
 *
 */
public class AABB
{
	public int MinX;
	public int MaxX;
	public int MinY;
	public int MaxY;
	public int MinZ;
	public int MaxZ;

	public AABB()
	{
		MinX = Integer.MAX_VALUE;
		MaxX = Integer.MIN_VALUE;
		MinY = Integer.MAX_VALUE;
		MaxY = Integer.MIN_VALUE;
		MinZ = Integer.MAX_VALUE;
		MaxZ = Integer.MIN_VALUE;
	}

	public AABB(double minX, double minY, double minZ, double maxX,
			double maxY, double maxZ)
	{
		Add(minX, minY, minZ);
		Add(maxX, maxY, maxZ);
	}

	public AABB(AABB other)
	{
		Clear();
		Set(other);
	}

	public int Width()
	{
		return MaxX - MinX;
	}

	public int Height()
	{
		return MaxY - MinY;
	}

	public int Depth()
	{
		return MaxZ - MinZ;
	}

	public int X()
	{
		return (MaxX + MinX) / 2;
	}

	public int Y()
	{
		return (MaxY + MinY) / 2;
	}

	public int Z()
	{
		return (MaxZ + MinZ) / 2;
	}

	public boolean IsEmpty()
	{
		return (MinX >= MaxX || MinY >= MaxY || MinZ >= MaxZ);
	}

	public void Clear()
	{
		MinX = Integer.MAX_VALUE;
		MaxX = Integer.MIN_VALUE;

		MinY = Integer.MAX_VALUE;
		MaxY = Integer.MIN_VALUE;

		MinZ = Integer.MAX_VALUE;
		MaxZ = Integer.MIN_VALUE;
	}

	public void Add(Vector3 inCoordinate)
	{
		Add(inCoordinate.X, inCoordinate.Y, inCoordinate.Z);
	}

	public void Add(double inX, double inY, double inZ)
	{
		if (Double.isInfinite(inX) || Double.isNaN(inX)
				|| Double.isInfinite(inY) || Double.isNaN(inY)
				|| Double.isInfinite(inZ) || Double.isNaN(inZ))
			throw new IllegalArgumentException();

		MinX = Math.min(MinX, (int) Math.floor(inX));
		MinY = Math.min(MinY, (int) Math.floor(inY));
		MinZ = Math.min(MinZ, (int) Math.floor(inZ));

		MaxX = Math.max(MaxX, (int) Math.ceil(inX));
		MaxY = Math.max(MaxY, (int) Math.ceil(inY));
		MaxZ = Math.max(MaxZ, (int) Math.ceil(inZ));
	}

	public void Add(AABB bounds)
	{
		MinX = Math.min(MinX, bounds.MinX);
		MinY = Math.min(MinY, bounds.MinY);
		MinZ = Math.min(MinZ, bounds.MinZ);

		MaxX = Math.max(MaxX, bounds.MaxX);
		MaxY = Math.max(MaxY, bounds.MaxY);
		MaxZ = Math.max(MaxZ, bounds.MaxZ);
	}

	public void Set(AABB bounds)
	{
		this.MinX = bounds.MinX;
		this.MinY = bounds.MinY;
		this.MinZ = bounds.MinZ;

		this.MaxX = bounds.MaxX;
		this.MaxY = bounds.MaxY;
		this.MaxZ = bounds.MaxZ;
	}

	public void Translate(int X, int Y, int Z)
	{
		this.MinX = this.MinX + X;
		this.MinY = this.MinY + Y;
		this.MinZ = this.MinZ + Z;

		this.MaxX = this.MaxX + X;
		this.MaxY = this.MaxY + Y;
		this.MaxZ = this.MaxZ + Z;
	}

	public void Translate(Vector3 translation)
	{
		this.MinX = (int) Math.floor(this.MinX + translation.X);
		this.MinY = (int) Math.floor(this.MinY + translation.Y);
		this.MinZ = (int) Math.floor(this.MinZ + translation.Z);

		this.MaxX = (int) Math.ceil(this.MaxX + translation.X);
		this.MaxY = (int) Math.ceil(this.MaxY + translation.Y);
		this.MaxZ = (int) Math.ceil(this.MaxZ + translation.Z);
	}

	public AABB Translated(Vector3 translation)
	{
		return new AABB(this.MinX + translation.X, this.MinY + translation.Y,
				this.MinZ + translation.Z,

				this.MaxX + translation.X, this.MaxY + translation.Y, this.MaxZ
						+ translation.Z);

	}

	public void Set(AABB other, Vector3 translation)
	{
		this.MinX = (int) Math.floor(other.MinX + translation.X);
		this.MinY = (int) Math.floor(other.MinY + translation.Y);
		this.MinZ = (int) Math.floor(other.MinZ + translation.Z);

		this.MaxX = (int) Math.ceil(other.MaxX + translation.X);
		this.MaxY = (int) Math.ceil(other.MaxY + translation.Y);
		this.MaxZ = (int) Math.ceil(other.MaxZ + translation.Z);
	}

	public boolean IsOutside(AABB other)
	{
		return (this.MaxX - other.MinX) < 0 || (this.MinX - other.MaxX) > 0
				|| (this.MaxY - other.MinY) < 0 || (this.MinY - other.MaxY) > 0
				|| (this.MaxZ - other.MinZ) < 0 || (this.MinZ - other.MaxZ) > 0;
	}

	public static boolean IsOutside(AABB left, AABB right)
	{
		return (left.MaxX - right.MinX) < 0 || (left.MinX - right.MaxX) > 0
				|| (left.MaxY - right.MinY) < 0 || (left.MinY - right.MaxY) > 0
				|| (left.MaxZ - right.MinZ) < 0 || (left.MinZ - right.MaxZ) > 0;
	}

	public static boolean IsOutside(AABB left, Vector3 translation, AABB right)
	{
		return ((left.MaxX + translation.X) - right.MinX) < 0
				|| ((left.MinX + translation.X) - right.MaxX) > 0
				|| ((left.MaxY + translation.Y) - right.MinY) < 0
				|| ((left.MinY + translation.Y) - right.MaxY) > 0
				|| ((left.MaxZ + translation.Z) - right.MinZ) < 0
				|| ((left.MinZ + translation.Z) - right.MaxZ) > 0;
	}
}