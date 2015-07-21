package net.cropsense.bso3;

import java.util.List;


/**
 * This class represents a node in the CSG-tree. A node can be a graphical
 * primitive or a CSG-Operation
 * 
 * @author Patrick Kaster
 *
 */
public class CSGNode
{

	public AABB Bounds;
	public CSGNodeType NodeType;

	public CSGNode Left;
	public CSGNode Right;
	public CSGNode Parent;

	public Vector3 LocalTranslation;
	public Vector3 Translation;
	public Plane[] Planes;

	public CSGNode(String id, CSGNodeType branchOperator)
	{
		this.Bounds = new AABB();
		this.NodeType = branchOperator;
		this.Left = null;
		this.Right = null;
		this.Parent = null;
		this.LocalTranslation = new Vector3();
		this.Translation = new Vector3();
		this.Planes = new Plane[0];
	}

	public CSGNode(String id, CSGNodeType branchOperator, CSGNode left,
			CSGNode right)
	{
		this.Bounds = new AABB();
		this.NodeType = branchOperator;
		this.Left = left;
		this.Right = right;
		this.Parent = null;
		this.LocalTranslation = new Vector3();
		this.Translation = new Vector3();
		this.Planes = new Plane[0];
	}

	public CSGNode(String id, List<Plane> planes)
	{
		this.Bounds = new AABB();
		this.NodeType = CSGNodeType.Brush;
		this.Left = null;
		this.Right = null;
		this.Parent = null;
		this.LocalTranslation = new Vector3();
		this.Translation = new Vector3();
		this.Planes = planes.toArray(new Plane[0]);
	}
}