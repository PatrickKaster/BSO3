package net.cropsense.bso3;

import java.util.ArrayList;
import java.util.List;
import java.util.ArrayDeque;


/**
 * various utility functions
 * 
 * @author Patrick Kaster
 *
 */
public class CSGUtility
{
	public static List<CSGNode> FindChildNodes(CSGNode node)
	{
		List<CSGNode> result = new ArrayList<CSGNode>();
		result.add(node);

		if (node.NodeType != CSGNodeType.Brush)
		{
			result.addAll(FindChildNodes(node.Left));
			result.addAll(FindChildNodes(node.Right));
		}
		return result;
	}

	public static List<CSGNode> FindChildBrushes(CSGNode node)
	{
		List<CSGNode> result = new ArrayList<CSGNode>();
		if (node.NodeType != CSGNodeType.Brush)
		{
			result.addAll(FindChildBrushes(node.Left));
			result.addAll(FindChildBrushes(node.Right));
		} else
		{
			result.add(node);
		}
		return result;
	}

	public static List<CSGNode> FindChildBrushes(CSGTree tree)
	{
		return FindChildBrushes(tree.RootNode);
	}

	public static void UpdateChildTransformations(CSGNode node,
			Vector3 parentTranslation)
	{
		node.Translation = Vector3
				.Add(parentTranslation, node.LocalTranslation);
		if (node.NodeType == CSGNodeType.Brush)
			return;
		UpdateChildTransformations(node.Left, node.Translation);
		UpdateChildTransformations(node.Right, node.Translation);
	}

	public static void UpdateChildTransformations(CSGNode node)
	{
		if (node.NodeType == CSGNodeType.Brush)
			return;
		UpdateChildTransformations(node.Left, node.Translation);
		UpdateChildTransformations(node.Right, node.Translation);
	}

	public static void UpdateBounds(CSGNode node)
	{
		if (node.NodeType != CSGNodeType.Brush)
		{
			CSGNode leftNode = node.Left;
			CSGNode rightNode = node.Right;
			UpdateBounds(leftNode);
			UpdateBounds(rightNode);

			node.Bounds.Clear();
			node.Bounds.Add(leftNode.Bounds.Translated(Vector3.Subtract(
					leftNode.Translation, node.Translation)));
			node.Bounds.Add(rightNode.Bounds.Translated(Vector3.Subtract(
					rightNode.Translation, node.Translation)));
		}
	}
	
	/**
	 * reverses the vertex order of the given polygon
	 * 
	 * @param polygon the polygon to be inverted
	 * @param edges the edge list of the polygon's mesh
	 * @param vertices the vertex list of the polygon's mesh
	 */
	public static void reverseVertexOrder(Polygon polygon, List<HalfEdge> edges, List<Vector3> vertices)
	{
		ArrayDeque<Integer> stack = new ArrayDeque<Integer>();
		
		HalfEdge firstEdge = edges.get(polygon.FirstIndex);
		stack.push(firstEdge.VertexIndex);
		
		HalfEdge currentEdge = edges.get(firstEdge.NextIndex);
				
		while (firstEdge != currentEdge)
		{	 
			stack.push(currentEdge.VertexIndex);
			currentEdge = edges.get(currentEdge.NextIndex);
		}
		
		firstEdge.VertexIndex = stack.pop();
		currentEdge = edges.get(firstEdge.NextIndex);
		
		while (firstEdge != currentEdge)
		{	 
			currentEdge.VertexIndex = stack.pop();
			currentEdge = edges.get(currentEdge.NextIndex);
		}
	}
	
	/**
	 * @param vertex the vertex to find in the Point on line list
	 * @param points the PointOnLine list
	 * @return index of the point on line that holds identical vertex, or -1 if vertex is not found in list
	 */
	public static int indexOfVertexInPoLL( Vector3 vertex, ArrayList<PointOnLine> points )
	{
		for ( int i=0; i<points.size(); ++i )
		{
			if ( points.get(i).point == vertex ) return i;
		}
		
		return -1;
	}
	
}
