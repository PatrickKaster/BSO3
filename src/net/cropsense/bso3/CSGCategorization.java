package net.cropsense.bso3;

import java.util.ArrayList;
import java.util.List;


/**
 * This is the CSG-Algorithm by v. Rossen/Baranowski
 * 
 * @author Patrick Kaster
 *
 */
public class CSGCategorization
{
	// Categorize the given inputPolygons as being inside/outside or
	// (reverse-)aligned
	// with the shape that is defined by the current brush or csg-branch.
	// When an inputPolygon crosses the node, it is split into pieces and every
	// individual
	// piece is then categorized.
	public static void Categorize(CSGNode processedNode, CSGMesh processedMesh,
			CSGNode categorizationNode, List<Polygon> inputPolygons,
			List<Polygon> inside, List<Polygon> aligned,
			List<Polygon> revAligned, List<Polygon> outside)
	{
		// When you go deep enough in the tree it's possible that all categories
		// point to the same destination. So we detect that and potentially
		// avoid a lot of wasted work.
		if (inside == revAligned && inside == aligned && inside == outside)
		{
			inside.addAll(inputPolygons);
			return;
		}

		boolean control = true;
		// Restart: // no goto in java; workaround via do-while
		do
		{
			if (processedNode == categorizationNode)
			{
				// When the currently processed node is the same node as we
				// categorize against, then
				// we know that all our polygons are visible and we set their
				// default category
				// (usually aligned, unless it's an instancing node in which
				// case it's precalculated)
				for (Polygon polygon : inputPolygons)
				{
					switch (polygon.Category)
					{
					case Aligned:
						aligned.add(polygon);
						break;
					case ReverseAligned:
						revAligned.add(polygon);
						break;
					case Inside:
						inside.add(polygon);
						break;
					case Outside:
						outside.add(polygon);
						break;
					}

					// When brushes overlap and they share the same surface area
					// we only want to keep
					// the polygons of the last brush in the tree, and skip all
					// others.
					// At this point in the tree we know that this polygon
					// belongs to this brush, so
					// we set it to visible. If the polygon is found to share
					// the surface area with another
					// brush further on in the tree it'll be set to invisible
					// again in mesh.Intersect.
					polygon.Visible = true;
				}
				return;
			}

			CSGNode leftNode = categorizationNode.Left;
			CSGNode rightNode = categorizationNode.Right;

			switch (categorizationNode.NodeType)
			{
			case Brush:
			{
				processedMesh.Intersect(categorizationNode.Bounds,
						categorizationNode.Planes,
						categorizationNode.Translation,
						processedNode.Translation, inputPolygons, inside,
						aligned, revAligned, outside);
				break;
			}
			case Addition:
			{
				// ( A || B)
				Vector3 relativeLeftTrans = Vector3.Subtract(
						processedNode.Translation, leftNode.Translation);
				Vector3 relativeRightTrans = Vector3.Subtract(
						processedNode.Translation, rightNode.Translation);
				if (AABB.IsOutside(processedNode.Bounds, relativeLeftTrans,
						leftNode.Bounds))
				{
					if (AABB.IsOutside(processedNode.Bounds,
							relativeRightTrans, rightNode.Bounds))
					{
						// When our polygons lie outside the bounds of both the
						// left and the right node, then
						// all the polygons can be categorized as being
						// 'outside'
						outside.addAll(inputPolygons);
					} else
					{
						// Categorize(processedNode, mesh, right,
						// inputPolygons,
						// inside, aligned, revAligned, outside);
						categorizationNode = rightNode;
						// goto Restart;
						continue;
					}
				} else if (AABB.IsOutside(processedNode.Bounds,
						relativeRightTrans, rightNode.Bounds))
				{
					// Categorize(processedNode, left, mesh,
					// inputPolygons,
					// inside, aligned, revAligned, outside);
					categorizationNode = leftNode;
					// goto Restart;
					continue;
				} else
				{
					LogicalOr(processedNode, processedMesh, categorizationNode,
							inputPolygons, inside, aligned, revAligned,
							outside, false, false);
				}
				break;
			}
			case Common:
			{
				// !(!A || !B)
				Vector3 relativeLeftTrans = Vector3.Subtract(
						processedNode.Translation, leftNode.Translation);
				Vector3 relativeRightTrans = Vector3.Subtract(
						processedNode.Translation, rightNode.Translation);
				if (AABB.IsOutside(processedNode.Bounds, relativeLeftTrans,
						leftNode.Bounds)
						|| AABB.IsOutside(processedNode.Bounds,
								relativeRightTrans, rightNode.Bounds))
				{
					// When our polygons lie outside the bounds of both the left
					// and the right node, then
					// all the polygons can be categorized as being 'outside'
					outside.addAll(inputPolygons);
				} else
				{
					LogicalOr(processedNode, processedMesh, categorizationNode,
							inputPolygons, outside, revAligned, aligned,
							inside, true, true);
				}
				break;
			}
			case Subtraction:
			{
				// !(!A || B)
				Vector3 relativeLeftTrans = Vector3.Subtract(
						processedNode.Translation, leftNode.Translation);
				Vector3 relativeRightTrans = Vector3.Subtract(
						processedNode.Translation, rightNode.Translation);
				if (AABB.IsOutside(processedNode.Bounds, relativeLeftTrans,
						leftNode.Bounds))
				{
					// When our polygons lie outside the bounds of both the left
					// node, then
					// all the polygons can be categorized as being 'outside'
					outside.addAll(inputPolygons);
				} else if (AABB.IsOutside(processedNode.Bounds,
						relativeRightTrans, rightNode.Bounds))
				{
					categorizationNode = leftNode;
					// goto Restart;
					continue;
				} else
				{
					LogicalOr(processedNode, processedMesh, categorizationNode,
							inputPolygons, outside, revAligned, aligned,
							inside, true, false);
				}
				break;
			}
			}
			control = false;
		} while (control == true);
	}

	// Logical OR set operation on polygons
	//
	// Table showing final output from combination of categorization of left and
	// right node
	//
	// | right node
	// | inside aligned r-aligned outside
	// -----------------+------------------------------------------
	// left inside | I I I I
	// node aligned | I A I A
	// r-aligned | I I R R
	// outside | I A R O
	//
	// I = inside A = aligned
	// O = outside R = reverse aligned
	//
	static void LogicalOr(CSGNode processedNode, CSGMesh processedMesh,
			CSGNode categorizationNode, List<Polygon> inputPolygons,
			List<Polygon> inside, List<Polygon> aligned,
			List<Polygon> revAligned, List<Polygon> outside,
			boolean inverseLeft, boolean inverseRight)
	{
		CSGNode leftNode = categorizationNode.Left;
		CSGNode rightNode = categorizationNode.Right;

		// ... Allocations are ridiculously cheap in .NET, there is a garbage
		// collection penalty however.
		// CSG can be performed without temporary buffers and recursion by using
		// flags,
		// which would increase performance and scalability (garbage collection
		// interfers with parallelization).
		// It makes the code a lot harder to read however.
		List<Polygon> leftAligned = new ArrayList<Polygon>(0);
		List<Polygon> leftRevAligned = new ArrayList<Polygon>(0);
		List<Polygon> leftOutside = new ArrayList<Polygon>(0);
		// var leftInside = new List<Polygon>(defaultCapacity); // everything
		// that's inside the left node
		// is always part of the inside category

		// First categorize polygons in left path ...
		if (inverseLeft)
			Categorize(processedNode, processedMesh, leftNode, inputPolygons,
					leftOutside, leftRevAligned, leftAligned, inside);
		else
			Categorize(processedNode, processedMesh, leftNode, inputPolygons,
					inside, leftAligned, leftRevAligned, leftOutside);

		// ... Then categorize the polygons in the right path
		// Note that no single polygon will go into more than one of the
		// Categorize methods below
		if (inverseRight)
		{
			if (leftAligned.size() > 0)
			{
				if (inside == aligned)
				{
					inside.addAll(leftAligned);
				} else
					Categorize(processedNode, processedMesh, rightNode,
							leftAligned, aligned, inside, aligned, inside);
			}

			if (leftRevAligned.size() > 0)
			{
				if (inside == revAligned)
				{
					inside.addAll(leftRevAligned);
				} else
					Categorize(processedNode, processedMesh, rightNode,
							leftRevAligned, revAligned, revAligned, inside,
							inside);
			}

			if (leftOutside.size() > 0)
			{
				Categorize(processedNode, processedMesh, rightNode,
						leftOutside, outside, revAligned, aligned, inside);
			}
		} else
		{
			if (leftAligned.size() > 0)
			{
				if (inside == aligned)
				{
					inside.addAll(leftAligned);
				} else
					Categorize(processedNode, processedMesh, rightNode,
							leftAligned, inside, aligned, inside, aligned);
			}

			if (leftRevAligned.size() > 0)
			{
				if (inside == revAligned)
				{
					inside.addAll(leftRevAligned);
				} else
					Categorize(processedNode, processedMesh, rightNode,
							leftRevAligned, inside, inside, revAligned,
							revAligned);
			}

			if (leftOutside.size() > 0)
			{
				Categorize(processedNode, processedMesh, rightNode,
						leftOutside, inside, aligned, revAligned, outside);
			}
		}
	}

	// Create meshes for a given number of nodes and perform CSG on these.
	// We cache our base meshes here
	static KVPairs<CSGNode, CSGMesh> cachedBaseMeshes = new KVPairs<CSGNode, CSGMesh>();

	public static KVPairs<CSGNode, CSGMesh> ProcessCSGNodes(CSGNode root,
			List<CSGNode> nodes)
	{
		KVPairs<CSGNode, CSGMesh> meshes = new KVPairs<CSGNode, CSGMesh>();

		// for all CSGNodes in nodes: buildMesh
		for (CSGNode node : nodes)
		{
			CSGMesh mesh;

			// if ((mesh = cachedBaseMeshes.get(node)) == null) {
			if (!cachedBaseMeshes.containsKey(node))
			{
				if (node.NodeType != CSGNodeType.Brush)
				{
					List<CSGNode> childNodes = CSGUtility
							.FindChildBrushes(node);
					KVPairs<CSGNode, CSGMesh> brushMeshes = ProcessCSGNodes(
							node, childNodes);
					mesh = CSGMesh.Combine(node.Translation, brushMeshes);
				} else
				{
					mesh = CSGMesh.CreateFromPlanes(node.Planes);
				}

				cachedBaseMeshes.put(node, mesh);
			} else
			{
				mesh = cachedBaseMeshes.get(node);
			}

			CSGMesh clonedMesh = mesh.Clone();
			node.Bounds.Set(clonedMesh.Bounds);
			meshes.put(node, clonedMesh);
		}

		// update bounds
		CSGUtility.UpdateBounds(root);

		// for all above generated CSGMeshes: updateDelegate
		for (CSGNode processedNode : meshes.keys())
		{
			CSGMesh processedMesh = meshes.get(processedNode);

			List<Polygon> inputPolygons = processedMesh.Polygons;
			List<Polygon> insidePolygons = new ArrayList<Polygon>(0);
			List<Polygon> outsidePolygons = new ArrayList<Polygon>(0);
			List<Polygon> alignedPolygons = new ArrayList<Polygon>(0);
			List<Polygon> reversedPolygons = new ArrayList<Polygon>(0);

			CSGCategorization.Categorize(processedNode, processedMesh, root,
					inputPolygons, insidePolygons, alignedPolygons,
					reversedPolygons, outsidePolygons);

			for (Polygon polygon : insidePolygons)
			{
				polygon.Category = PolygonCategory.Inside;
				polygon.Visible = false;
			}

			for (Polygon polygon : outsidePolygons)
			{
				polygon.Category = PolygonCategory.Outside;
				polygon.Visible = false;
			}

			for (Polygon polygon : alignedPolygons)
				polygon.Category = PolygonCategory.Aligned;

			/* for inversion of reversed aligned polygons at root level of csg tree, see CSGMesh:Combine !! */
			for (Polygon polygon : reversedPolygons)
			{
				polygon.Category = PolygonCategory.ReverseAligned;
			}
		}

		return meshes;
	}
}
