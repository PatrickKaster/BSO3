package net.cropsense.bso3;

import java.util.ArrayList;
import java.util.List;


/**
 * @author Patrick Kaster
 *
 *	This class implements an ear clipping triangulation algorithm, ported from C++, from the book
 * "Mathematics for 3D Game Programming and Computer Graphics, 3rd ed." by Eric Lengyel, Listing 9.2.
 */
public class Triangulator3
{
	private ArrayList<Vector3> inputPoints;
	private ArrayList<Integer> indices;	
	
	public Triangulator3()
	{
		this.inputPoints = new ArrayList<Vector3>();
		this.indices = new ArrayList<Integer>();
	}
	
	/**
	 * add a 3D vector as input point
	 * @param x x-value of 3D vector
	 * @param y y-value of 3D vector
	 * @param z z-value of 3D vector
	 */
	public void addPoint(double x, double y, double z)
	{
		Vector3 v = new Vector3(x, y, z);
		inputPoints.add(v);
	}
	
	/**
	 * add a 3D vector as input point
	 * @param in the Vector to add
	 */
	public void addPoint(Vector3 in)
	{
		Vector3 v = new Vector3(in);
		inputPoints.add(v);
	}
		
	int getNextActive(int x, int vertexCount, boolean[] active)
	{
		for (;;)
		{
			if (++x == vertexCount) x = 0;
			if (active[x]) return (x);
		}
	}
	
	int getPrevActive(int x, int vertexCount, boolean[] active)
	{
		for (;;)
		{
			if (--x == -1) x = vertexCount - 1;
			if (active[x]) return (x);
		}
	}
	

	/**
	 * Triangulates the polygon defined by the points added to this triangulator object.
	 * Resulting triangle vertex indices can be obtained via getIndices() methode.
	 * 
	 * @param normal the normal to the plane the polygon lies on
	 */
	public void triangulate(Vector3 normal)
	{
		if (inputPoints.size() < 3)
		{
			throw new IllegalArgumentException("polygon must have at least 3 vertices");
		}
		
		ArrayList<Triangle> triangles = new ArrayList<Triangle>();
		int vertexCount = this.inputPoints.size();
		
		boolean[] active = new boolean[vertexCount];
		for (int i = 0; i < vertexCount; ++i) active[i] = true;
		
		int start = 0;
		int p1 = 0;
		int p2 = 1;
		int m1 = vertexCount - 1;
		int m2 = vertexCount - 2;
		
		boolean lastPositive = false;
		
		for (;;)
		{
			Triangle currentTriangle = new Triangle();
			
			if (p2 == m2)
			{
				// Only three vertices remain.
				currentTriangle.index[0] = m1;
				currentTriangle.index[1] = p1;
				currentTriangle.index[2] = p2;
				triangles.add(currentTriangle);
				break;
			}
			
			Vector3 vp1 = this.inputPoints.get(p1);
			Vector3 vp2 = this.inputPoints.get(p2);
			Vector3 vm1 = this.inputPoints.get(m1);
			Vector3 vm2 = this.inputPoints.get(m2);
			boolean positive = false;
			boolean negative = false;
			
			// Determine whether vp1, vp2, and vm1 form a valid triangle.
			Vector3 n1 = Vector3.CrossProduct(normal, Vector3.minus(vm1, vp2));
			n1.Normalize();
			if ( n1.DotProduct(Vector3.minus(vp1, vp2)) > VarsConstants.epsilonTriangulator3 )
			{
				negative = true;
				Vector3 n2 = Vector3.CrossProduct(normal, Vector3.minus(vm1, vm2));
				n2.Normalize();
				Vector3 n3 = Vector3.CrossProduct(normal, Vector3.minus(vp1, vm1));
				n3.Normalize();
				
				for (int i = 0; i < vertexCount; ++i)
				{
					// Look for other vertices inside the triangle.
					if ( (active[i]) && (i != m1) && (i != m2) && (i != p1) )
					{
						Vector3 v = this.inputPoints.get(i);
						Vector3 vminusvp1 = Vector3.minus(v, vp1);
						vminusvp1.Normalize();
						Vector3 vminusvm2 = Vector3.minus(v, vm2);
						vminusvm2.Normalize();
						Vector3 vminusvm1 = Vector3.minus(v, vm1);
						vminusvm1.Normalize();
						
						if ( ( Vector3.DotProduct(n1, vminusvp1) > -VarsConstants.epsilonTriangulator3 )
							&& ( Vector3.DotProduct(n2, vminusvm2) > -VarsConstants.epsilonTriangulator3 )
							&& ( Vector3.DotProduct(n3, vminusvm1) > -VarsConstants.epsilonTriangulator3 ) )
						{
							negative = false;
							break;
						}
					}
				}
			}
			
			// Determine whether vm1, vm2, and vp1 form a valid triangle.
			n1 = Vector3.CrossProduct(normal, Vector3.minus(vm2, vp1));
			n1.Normalize();
			
			if ( n1.DotProduct(Vector3.minus(vm1, vp1)) > VarsConstants.epsilonTriangulator3 )
			{
				negative = true;
				Vector3 n2 = Vector3.CrossProduct(normal, Vector3.minus(vm1, vm2));
				n2.Normalize();
				Vector3 n3 = Vector3.CrossProduct(normal, Vector3.minus(vp1, vm1));
				n3.Normalize();
								
				for (int i = 0; i < vertexCount; ++i)
				{
					// Look for other vertices inside the triangle.
					if ( (active[i]) && (i != m1) && (i != m2) && (i != p1) )
					{
						Vector3 v = this.inputPoints.get(i);
						Vector3 vminusvp1 = Vector3.minus(v, vp1);
						vminusvp1.Normalize();
						Vector3 vminusvm2 = Vector3.minus(v, vm2);
						vminusvm2.Normalize();
						Vector3 vminusvm1 = Vector3.minus(v, vm1);
						vminusvm1.Normalize();
						if ( ( Vector3.DotProduct(n1, vminusvp1) > -VarsConstants.epsilonTriangulator3 )
								&& ( Vector3.DotProduct(n2, vminusvm2) > -VarsConstants.epsilonTriangulator3 )
								&& ( Vector3.DotProduct(n3, vminusvm1) > -VarsConstants.epsilonTriangulator3 ) )
						{
							negative = false;
							break;
						}
					}
				}
			}
			
			// If both triangles are valid, choose the one having the larger smallest angle.
			if ((positive) && (negative))
			{
				Vector3 vp2minusvm1 = Vector3.minus(vp2, vm1);
				vp2minusvm1.Normalize();
				Vector3 vm2minusvm1 = Vector3.minus(vm2, vm1);
				vm2minusvm1.Normalize();
				
				double pd = Vector3.DotProduct(vp2minusvm1, vm2minusvm1);
				
				Vector3 vm2minusvp1 = Vector3.minus(vm2, vp1);
				vm2minusvp1.Normalize();
				Vector3 vp2minusvp1 = Vector3.minus(vp2, vp1);
				vp2minusvp1.Normalize();
				
				double md = Vector3.DotProduct(vm2minusvp1, vp2minusvp1);
				
				if ( Math.abs(pd - md) < VarsConstants.epsilonTriangulator3 )
				{
					if ( lastPositive ) positive = false;
					else negative = false;
				}
				else
				{
					if ( pd < md ) negative = false;
					else positive = false;
				}
			}
			
			if (positive)
			{
				// Output the triangle m1, p1, p2.
				active[p1] = false;
				currentTriangle.index[0] = m1;
				currentTriangle.index[1] = p1;
				currentTriangle.index[2] = p2;
				triangles.add(currentTriangle);
				
				p1 = getNextActive(p1, vertexCount, active);
				p2 = getNextActive(p2, vertexCount, active);
				lastPositive = true;
				start = -1;
			}
			else if (negative)
			{
				// Output the triangle m2, m1, p1.
				active[m1] = false;
				currentTriangle.index[0] = m2;
				currentTriangle.index[1] = m1;
				currentTriangle.index[2] = p1;
				triangles.add(currentTriangle);
				
				m1 = getPrevActive(m1, vertexCount, active);
				m2 = getPrevActive(m2, vertexCount, active);
				lastPositive = false;
				start = -1;
			}
			else
			{
				// Exit if we've gone all the way around the
				// polygon without finding a valid triangle.
				if (start == -1) start = p2;
				else if (p2 == start) break;
				
				// Advance working set of vertices.
				m2 = m1;
				m1 = p1;
				p1 = p2;
				p2 = getNextActive(p2, vertexCount, active);
			}
		}
		
		// output triangles to index list
		for ( Triangle triangle : triangles )
		{
			this.indices.add(triangle.index[0]);
			this.indices.add(triangle.index[1]);
			this.indices.add(triangle.index[2]);
		}
	}
	
	public Vector3 getInputPoint(int index)
	{
		return inputPoints.get(index);
	}

	public List<Vector3> getInputPoints()
	{
		return inputPoints;
	}
	

	/**
	 * get the resulting triangulation
	 * 
	 * @return List holding the vertex indices of resulting triangulation of polygon
	 */
	public List<Integer> getIndices()
	{
		return this.indices;
	}
	
	/**
	 *	reset this triangulator object 
	 */
	public void reset()
	{
		this.inputPoints = new ArrayList<Vector3>();
		this.indices = new ArrayList<Integer>();
	}
}
