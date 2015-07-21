package net.cropsense.bso3;


/**
 * class representing a half-edge in the mesh
 * 
 * @author Patrick Kaster
 *
 */
public class HalfEdge
{
	public int NextIndex;
	public int TwinIndex;
	public int VertexIndex;
	public int PolygonIndex;
}
