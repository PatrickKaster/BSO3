package net.cropsense.bso3;

import java.util.ArrayList;
import java.util.List;


/**
 * a triangulator for a mesh data structure
 * 
 * @author Florian Schoeler
 */
public class Triangulator
{
	List<Vector3> mInputPoints;
	List<Vector3> mPoints;
	List<Integer> mIndices;
	float mEpsilon;
	Vector3 mMin;
	Vector3 mMax;

	public Triangulator()
	{
		mInputPoints = new ArrayList<Vector3>();
		mPoints = new ArrayList<Vector3>();
		mIndices = new ArrayList<Integer>();
	}

	public List<Integer> getIndices()
	{
		return mIndices;
	}

	public Vector3 getPoint(int id)
	{
		return mPoints.get(id);
	}

	public List<Vector3> getPoints()
	{
		return mPoints;
	}

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
		mPoints = new ArrayList<Vector3>();
		mIndices = new ArrayList<Integer>();
	}

	public void addPoint(double x, double y, double z)
	{
		Vector3 v = new Vector3(x, y, z);
		if (mInputPoints.size() == 0)
		{
			mMin = new Vector3(v);
			mMax = new Vector3(v);
		} else
		{
			if (x < mMin.X)
				mMin.X = x;
			if (y < mMin.Y)
				mMin.Y = y;
			if (z < mMin.Z)
				mMin.Z = z;
			if (x > mMax.X)
				mMax.X = x;
			if (y > mMax.Y)
				mMax.Y = y;
			if (z > mMax.Z)
				mMax.Z = z;
		}
		mInputPoints.add(v);
	}

	public int numInputPoints()
	{
		return mInputPoints.size();
	}

	public int numPoints()
	{
		return mPoints.size();
	}

	public void triangulate(float epsilon)
	{
		mEpsilon = epsilon;

		if (mInputPoints.size() > 0)
		{
			double dx = mMax.X - mMin.X;
			double dy = mMax.Y - mMin.Y;
			double dz = mMax.Z - mMin.Z;

			int i1, i2, i3;

			if (dx >= dy && dx >= dz)
			{
				i1 = 0;
				if (dy >= dz)
				{
					i2 = 1;
					i3 = 2;
				} else
				{
					i2 = 2;
					i3 = 1;
				}
			} else if (dy >= dx && dy >= dz)
			{
				i1 = 1;
				if (dx >= dz)
				{
					i2 = 0;
					i3 = 2;
				} else
				{
					i2 = 2;
					i3 = 0;
				}
			} else
			{
				i1 = 2;
				if (dx >= dy)
				{
					i2 = 0;
					i3 = 1;
				} else
				{
					i2 = 1;
					i3 = 0;
				}
			}

			double[] points = new double[3];
			for (int i = 0; i < mInputPoints.size(); ++i)
			{
				Vector3 w = mInputPoints.get(i);
				points = new double[] { w.X, w.Y, w.Z };
				Vector3 v = new Vector3(points[i1], points[i2], points[i3]);
				mPoints.add(v);
			}

			process();
		}
	}

	private void process()
	{
		int n = mPoints.size();
		if (n < 3)
			return;

		int[] V = new int[n];
		boolean flipped = false;

		if (0f < area())
		{
			for (int v = 0; v < n; v++)
			{
				V[v] = v;
			}
		} else
		{
			for (int v = 0; v < n; v++)
			{
				V[v] = (n - 1) - v;
			}
			flipped = true;
		}

		int nv = n;
		int count = 2 * nv;
		for (int m = 0, v = nv - 1; nv > 2;)
		{
			if (0 >= (count--))
				return;

			int u = v;
			if (nv <= u)
				u = 0;
			v = u + 1;
			if (nv <= v)
				v = 0;
			int w = v + 1;
			if (nv <= w)
				w = 0;

			if (snip(u, v, w, nv, V))
			{
				int a, b, c, s, t;
				a = V[u];
				b = V[v];
				c = V[w];

				if (flipped)
				{
					mIndices.add(a);
					mIndices.add(b);
					mIndices.add(c);
				} else
				{
					mIndices.add(c);
					mIndices.add(b);
					mIndices.add(a);
				}
				m++;

				for (s = v, t = v + 1; t < nv; s++, t++)
				{
					V[s] = V[t];
				}
				nv--;
				count = 2 * nv;
			}
		}
	}

	private float area()
	{
		int n = mPoints.size();
		float A = 0f;
		for (int p = n - 1, q = 0; q < n; p = q++)
		{
			Vector3 pval = mPoints.get(p);
			Vector3 qval = mPoints.get(q);
			A += pval.X * qval.Y - qval.X * pval.Y;
		}

		return A * 0.5f;
	}

	private boolean snip(int u, int v, int w, int n, int[] V)
	{
		int p;

		Vector3 A = mPoints.get(V[u]);
		Vector3 B = mPoints.get(V[v]);
		Vector3 C = mPoints.get(V[w]);

		if (mEpsilon > (((B.X - A.X) * (C.Y - A.Y)) - ((B.Y - A.Y) * (C.X - A.X))))
			return false;

		for (p = 0; p < n; p++)
		{
			if ((p == u) || (p == v) || (p == w))
				continue;

			if (insideTriangle(A, B, C, mPoints.get(V[p])))
				return false;
		}

		return true;
	}

	private boolean insideTriangle(Vector3 A, Vector3 B, Vector3 C, Vector3 P)
	{
		double ax, ay, bx, by, cx, cy, apx, apy, bpx, bpy, cpx, cpy;
		double cCROSSap, bCROSScp, aCROSSbp;

		ax = C.X - B.X;
		ay = C.Y - B.Y;
		bx = A.X - C.X;
		by = A.Y - C.Y;
		cx = B.X - A.X;
		cy = B.Y - A.Y;
		apx = P.X - A.X;
		apy = P.Y - A.Y;
		bpx = P.X - B.X;
		bpy = P.Y - B.Y;
		cpx = P.X - C.X;
		cpy = P.Y - C.Y;

		aCROSSbp = ax * bpy - ay * bpx;
		cCROSSap = cx * apy - cy * apx;
		bCROSScp = bx * cpy - by * cpx;

		return ((aCROSSbp >= 0f) && (bCROSScp >= 0f) && (cCROSSap >= 0f));
	}
}