package net.cropsense.bso3;

import java.util.ArrayList;
import java.util.List;

/**
 * a simple triangle fanning based triangulator for a mesh data structure
 * 
 * @author Florian Schoeler
 */
public class Triangulator2
{
	List<Vector3> mInputPoints;
	//List<Vector3> mPoints;
	List<Integer> mIndices;

	public Triangulator2()
	{
		mInputPoints = new ArrayList<Vector3>();
//		mPoints = new ArrayList<Vector3>();
		mIndices = new ArrayList<Integer>();
	}

	public List<Integer> getIndices()
	{
		return mIndices;
	}

	/*public Vector3 getPoint(int id)
	{
		return mPoints.get(id);
	}

	public List<Vector3> getPoints()
	{
		return mPoints;
	}*/

	public Vector3 getInputPoint(int index)
	{
		return mInputPoints.get(index);
	}

	public List<Vector3> getInputPoints()
	{
		return mInputPoints;
	}

	public void reset()
	{
		mInputPoints = new ArrayList<Vector3>();
		//mPoints = new ArrayList<Vector3>();
		mIndices = new ArrayList<Integer>();
	}

	public void addPoint(double x, double y, double z)
	{
		Vector3 v = new Vector3(x, y, z);
		mInputPoints.add(v);
	}

	public void triangulate()
	{
		if (mInputPoints.size() < 3)
		{
			throw new IllegalArgumentException(
					"polygon must have at least 3 vertices");
		} else if (mInputPoints.size() == 3)
		{
			mIndices.add(0);
			mIndices.add(1);
			mIndices.add(2);
		} else
		{
			mIndices.add(0);
			mIndices.add(1);
			mIndices.add(2);
			for (int i = 2; i < mInputPoints.size() - 1; ++i)
			{
				mIndices.add(0);
				mIndices.add(i);
				mIndices.add(i + 1);
			}
		}
	}

	public int numInputPoints()
	{
		return mInputPoints.size();
	}

	/*public int numPoints()
	{
		return mPoints.size();
	}*/
}
