package net.cropsense.bso3;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;

/**
 * A class representing the geometry as a mesh from vertices and edges
 * It also defines filter functions to overcome possible gaps/holes in the
 * representation.
 * 
 * @author Patrick Kaster
 *
 */
public class CSGMesh
{
	public AABB Bounds;
	public List<Polygon> Polygons;
	public List<HalfEdge> Edges;
	public List<Vector3> Vertices;
	public Plane[] Planes;
	
	public List<HalfEdge> boundaryEdges;
	public List<HalfEdge> weldedEdges;
	public List<Vector3> boundaryVertices;

	public CSGMesh(Plane[] planes, List<Polygon> polygons,
			List<HalfEdge> edges, List<Vector3> vertices, AABB bounds)
	{
		this.Planes = planes;
		this.Polygons = polygons;
		this.Edges = edges;
		this.Vertices = vertices;
		this.Bounds = new AABB(bounds);
		/* debug !! */
		this.boundaryEdges = new ArrayList<HalfEdge>();
		//this.weldedEdges = new ArrayList<HalfEdge>();
		//this.boundaryVertices = new ArrayList<Vector3>();
	}

	// Creates a clone of the mesh
	public CSGMesh Clone()
	{
		Plane[] newPlanes = new Plane[Planes.length];
		for (int i = 0; i < Planes.length; i++)
		{
			Plane plane = Planes[i];
			newPlanes[i] = new Plane(plane.A, plane.B, plane.C, plane.D);
		}
		List<Polygon> newPolygons = new ArrayList<Polygon>(Polygons.size());
		for (Polygon polygon : Polygons)
		{
			Polygon newPolygon = new Polygon();
			newPolygon.FirstIndex = polygon.FirstIndex;
			newPolygon.Visible = polygon.Visible;
			newPolygon.Category = polygon.Category;
			newPolygon.PlaneIndex = polygon.PlaneIndex;
			newPolygon.Bounds.Set(polygon.Bounds);

			newPolygons.add(newPolygon);
		}

		List<HalfEdge> newEdges = new ArrayList<HalfEdge>(Edges.size());
		for (HalfEdge edge : Edges)
		{
			HalfEdge newEdge = new HalfEdge();
			newEdge.NextIndex = edge.NextIndex;
			newEdge.PolygonIndex = edge.PolygonIndex;
			newEdge.TwinIndex = edge.TwinIndex;
			newEdge.VertexIndex = edge.VertexIndex;
			newEdges.add(newEdge);
		}

		List<Vector3> newVertices = new ArrayList<Vector3>(Vertices.size());
		for (Vector3 vertex : Vertices)
		{
			Vector3 newVertex = new Vector3(vertex.X, vertex.Y, vertex.Z);
			newVertices.add(newVertex);
		}

		AABB newBounds = new AABB(Bounds);
		CSGMesh newMesh = new CSGMesh(newPlanes, newPolygons, newEdges,
				newVertices, newBounds);

		return newMesh;
	}

	// Creates a mesh from a brush (set of planes)
	static class EdgeIntersection
	{
		public EdgeIntersection(HalfEdge edge, int planeIndexA, int planeIndexB)
		{

			PlaneIndices[0] = planeIndexA;
			PlaneIndices[1] = planeIndexB;

			Edge = edge;
		}

		public int[] PlaneIndices = new int[2];
		public HalfEdge Edge;
	}

	static class PointIntersection
	{
		public PointIntersection(int vertexIndex, List<Integer> planes)
		{
			VertexIndex = vertexIndex;
			for (Integer plane : planes)
				PlaneIndices.add(plane);
		}

		public List<EdgeIntersection> Edges = new ArrayList<EdgeIntersection>();
		public HashSet<Integer> PlaneIndices = new HashSet<Integer>();
		public int VertexIndex;
	}

	public static CSGMesh CreateFromPlanes(Plane[] brushPlanes)
	{
		Plane[] planes = new Plane[brushPlanes.length];
		for (int i = 0; i < brushPlanes.length; i++)
		{
			Plane plane = brushPlanes[i];
			planes[i] = new Plane(plane.A, plane.B, plane.C, plane.D);
		}

		List<PointIntersection> pointIntersections = new ArrayList<PointIntersection>(
				planes.length * planes.length);
		List<Integer> intersectingPlanes = new ArrayList<Integer>();
		List<Vector3> vertices = new ArrayList<Vector3>();
		List<HalfEdge> edges = new ArrayList<HalfEdge>();

		// Find all point intersections where 3 (or more planes) intersect
		for (int planeIndex1 = 0; planeIndex1 < planes.length - 2; planeIndex1++)
		{
			Plane plane1 = planes[planeIndex1];
			for (int planeIndex2 = planeIndex1 + 1; planeIndex2 < planes.length - 1; planeIndex2++)
			{
				Plane plane2 = planes[planeIndex2];
				for (int planeIndex3 = planeIndex2 + 1; planeIndex3 < planes.length; planeIndex3++)
				{
					Plane plane3 = planes[planeIndex3];

					// Calculate the intersection
					Vector3 vertex = Plane.Intersection(plane1, plane2, plane3);

					// Check if the intersection is valid
					if (Double.isNaN(vertex.X) || Double.isNaN(vertex.Y)
							|| Double.isNaN(vertex.Z)
							|| Double.isInfinite(vertex.X)
							|| Double.isInfinite(vertex.Y)
							|| Double.isInfinite(vertex.Z))
						continue;

					intersectingPlanes.clear();
					intersectingPlanes.add(planeIndex1);
					intersectingPlanes.add(planeIndex2);
					intersectingPlanes.add(planeIndex3);

					SkipIntersection:
					{
						for (int planeIndex4 = 0; planeIndex4 < planes.length; planeIndex4++)
						{
							if (planeIndex4 == planeIndex1
									|| planeIndex4 == planeIndex2
									|| planeIndex4 == planeIndex3)
								continue;

							Plane plane4 = planes[planeIndex4];
							PlaneSideResult side = plane4.OnSide(vertex);
							if (side == PlaneSideResult.Intersects)
							{
								if (planeIndex4 < planeIndex3)
									// Already found this vertex
									break SkipIntersection;

								// We've found another plane which goes trough
								// our found intersection point
								intersectingPlanes.add(planeIndex4);
							} else if (side == PlaneSideResult.Outside)
								// Intersection is outside of brush
								break SkipIntersection;
						}

						Integer vertexIndex = vertices.size();
						vertices.add(vertex);

						// Add intersection point to our list
						pointIntersections.add(new PointIntersection(
								vertexIndex, intersectingPlanes));
					}
					// SkipIntersection:
					// ;
				}
			}
		}

		int[] foundPlanes = new int[2];
		// Find all our intersection edges which are formed by a pair of planes
		// (this could probably be done inside the previous loop)
		for (int i = 0; i < pointIntersections.size(); i++)
		{
			PointIntersection pointIntersectionA = pointIntersections.get(i);
			for (int j = i + 1; j < pointIntersections.size(); j++)
			{
				PointIntersection pointIntersectionB = pointIntersections
						.get(j);
				HashSet<Integer> planesIndicesA = pointIntersectionA.PlaneIndices;
				HashSet<Integer> planesIndicesB = pointIntersectionB.PlaneIndices;

				int foundPlaneIndex = 0;
				for (Integer currentPlaneIndex : planesIndicesA)
				{
					if (!planesIndicesB.contains(currentPlaneIndex))
						continue;

					foundPlanes[foundPlaneIndex] = currentPlaneIndex;
					foundPlaneIndex++;

					if (foundPlaneIndex == 2)
						break;
				}

				// If foundPlaneIndex is 0 or 1 then either this combination
				// does not exist,
				// or only goes trough one point
				if (foundPlaneIndex < 2)
					continue;

				// Create our found intersection edge
				HalfEdge halfEdgeA = new HalfEdge();
				int halfEdgeAIndex = edges.size();
				edges.add(halfEdgeA);

				HalfEdge halfEdgeB = new HalfEdge();
				int halfEdgeBIndex = edges.size();
				edges.add(halfEdgeB);

				halfEdgeA.TwinIndex = halfEdgeBIndex;
				halfEdgeB.TwinIndex = halfEdgeAIndex;

				halfEdgeA.VertexIndex = pointIntersectionA.VertexIndex;
				halfEdgeB.VertexIndex = pointIntersectionB.VertexIndex;

				// Add it to our points
				pointIntersectionA.Edges.add(new EdgeIntersection(halfEdgeA,
						foundPlanes[0], foundPlanes[1]));
				pointIntersectionB.Edges.add(new EdgeIntersection(halfEdgeB,
						foundPlanes[0], foundPlanes[1]));
			}
		}

		List<Polygon> polygons = new ArrayList<Polygon>();
		for (int i = 0; i < planes.length; i++)
		{
			Polygon polygon = new Polygon();
			polygon.PlaneIndex = i;
			polygons.add(polygon);
		}

		AABB bounds = new AABB();
		Vector3 direction = new Vector3();
		for (int i = pointIntersections.size() - 1; i >= 0; i--)
		{
			PointIntersection pointIntersection = pointIntersections.get(i);
			List<EdgeIntersection> pointEdges = pointIntersection.Edges;

			// Make sure that we have at least 2 edges ...
			// This may happen when a plane only intersects at a single edge.
			if (pointEdges.size() <= 2)
			{
				pointIntersections.remove(i);
				continue;
			}

			int vertexIndex = pointIntersection.VertexIndex;
			Vector3 vertex = vertices.get(vertexIndex);

			for (int j = 0; j < pointEdges.size() - 1; j++)
			{
				EdgeIntersection edge1 = pointEdges.get(j);
				for (int k = j + 1; k < pointEdges.size(); k++)
				{
					EdgeIntersection edge2 = pointEdges.get(k);

					int planeIndex1 = -1;
					int planeIndex2 = -1;

					// Determine if and which of our 2 planes are identical
					if (edge1.PlaneIndices[0] == edge2.PlaneIndices[0])
					{
						planeIndex1 = 0;
						planeIndex2 = 0;
					} else if (edge1.PlaneIndices[0] == edge2.PlaneIndices[1])
					{
						planeIndex1 = 0;
						planeIndex2 = 1;
					} else if (edge1.PlaneIndices[1] == edge2.PlaneIndices[0])
					{
						planeIndex1 = 1;
						planeIndex2 = 0;
					} else if (edge1.PlaneIndices[1] == edge2.PlaneIndices[1])
					{
						planeIndex1 = 1;
						planeIndex2 = 1;
					} else
						continue;

					HalfEdge ingoing;
					HalfEdge outgoing;
					int outgoingIndex;

					Plane shared_plane = planes[edge1.PlaneIndices[planeIndex1]];
					Plane edge1_plane = planes[edge1.PlaneIndices[1 - planeIndex1]];
					Plane edge2_plane = planes[edge2.PlaneIndices[1 - planeIndex2]];

					direction = Vector3.CrossProduct(shared_plane.Normal(),
							edge1_plane.Normal());

					// Determine the orientation of our two edges to determine
					// which edge is in-going, and which one is out-going
					if (Vector3.DotProduct(direction, edge2_plane.Normal()) < 0)
					{
						ingoing = edge2.Edge;
						outgoingIndex = edge1.Edge.TwinIndex;
						outgoing = edges.get(outgoingIndex);
					} else
					{
						ingoing = edge1.Edge;
						outgoingIndex = edge2.Edge.TwinIndex;
						outgoing = edges.get(outgoingIndex);
					}

					// Link the out-going half-edge to the in-going half-edge
					ingoing.NextIndex = outgoingIndex;

					// Add reference to polygon to half-edge, and make sure our
					// polygon has a reference to a half-edge
					// Since a half-edge, in this case, serves as a circular
					// linked list this just works.
					int polygonIndex = edge1.PlaneIndices[planeIndex1];

					ingoing.PolygonIndex = polygonIndex;
					outgoing.PolygonIndex = polygonIndex;

					Polygon polygon = polygons.get(polygonIndex);
					polygon.FirstIndex = outgoingIndex;
					polygon.Bounds.Add(vertex.X, vertex.Y, vertex.Z);
				}
			}

			// Add the intersection point to the area of our bounding box
			bounds.Add(vertex.X, vertex.Y, vertex.Z);
		}

		return new CSGMesh(planes, polygons, edges, vertices, bounds);
	}

	// Splits a half edge
	HalfEdge EdgeSplit(HalfEdge edge, Vector3 vertex)
	{
		/*
		 * original:
		 * 
		 * edge<====================== ---------------------->* twin
		 * 
		 * split:
		 * 
		 * newEdge thisEdge<=========*<=========== --------->*----------->*
		 * thisTwin newTwin
		 */

		HalfEdge thisEdge = edge;
		int thisTwinIndex = edge.TwinIndex;
		HalfEdge thisTwin = Edges.get(thisTwinIndex);
		int thisEdgeIndex = thisTwin.TwinIndex;

		HalfEdge newEdge = new HalfEdge();
		int newEdgeIndex = Edges.size();

		HalfEdge newTwin = new HalfEdge();
		int newTwinIndex = newEdgeIndex + 1;
		int vertexIndex = Vertices.size();

		newEdge.PolygonIndex = thisEdge.PolygonIndex;
		newTwin.PolygonIndex = thisTwin.PolygonIndex;

		newEdge.VertexIndex = thisEdge.VertexIndex;
		thisEdge.VertexIndex = vertexIndex;

		newTwin.VertexIndex = thisTwin.VertexIndex;
		thisTwin.VertexIndex = vertexIndex;

		newEdge.NextIndex = thisEdge.NextIndex;
		thisEdge.NextIndex = newEdgeIndex;

		newTwin.NextIndex = thisTwin.NextIndex;
		thisTwin.NextIndex = newTwinIndex;

		newEdge.TwinIndex = thisTwinIndex;
		thisTwin.TwinIndex = newEdgeIndex;

		thisEdge.TwinIndex = newTwinIndex;
		newTwin.TwinIndex = thisEdgeIndex;

		Edges.add(newEdge);
		Edges.add(newTwin);
		Vertices.add(vertex);
		return newEdge;
	}

	// Splits a polygon into two pieces, or categorizes it as outside, inside or
	// aligned
	// Note: This method is not optimized! Code is simplified for clarity!
	// for example: Plane.Distance / Plane.OnSide should be inlined manually and
	// shouldn't use enums, but floating point values directly!
	public PolygonSplitResult PolygonSplit(Plane cuttingPlane,
			Vector3 translation, Polygon inputPolygon, Polygon outsidePolygon)
	{
		HalfEdge prev = Edges.get(inputPolygon.FirstIndex);
		HalfEdge current = Edges.get(prev.NextIndex);
		HalfEdge next = Edges.get(current.NextIndex);
		HalfEdge last = next;
		HalfEdge enterEdge = null;
		HalfEdge exitEdge = null;

		Vector3 prevVertex = Vertices.get(prev.VertexIndex);
		// distance to previous vertex
		double prevDistance = cuttingPlane.Distance(prevVertex);
		// side of plane of previous vertex
		PlaneSideResult prevSide = Plane.OnSide(prevDistance);

		Vector3 currentVertex = Vertices.get(current.VertexIndex);
		// distance to current vertex
		double currentDistance = cuttingPlane.Distance(currentVertex);
		// side of plane of current vertex
		PlaneSideResult currentSide = Plane.OnSide(currentDistance);

		do // while (next != last)
		{
			Vector3 nextVertex = Vertices.get(next.VertexIndex);
			// distance to next vertex
			double nextDistance = cuttingPlane.Distance(nextVertex);
			// side of plane of next vertex
			PlaneSideResult nextSide = Plane.OnSide(nextDistance);

			if (prevSide != currentSide) // check if edge crossed the plane ...
			{
				// prev:inside/outside - current:inside/outside - next:??
				if (currentSide != PlaneSideResult.Intersects)
				{
					// prev:inside/outside - current:outside - next:??
					if (prevSide != PlaneSideResult.Intersects)
					{
						// Calculate intersection of edge with plane split the
						// edge into two, inserting the new vertex
						Vector3 newVertex = Plane.Intersection(prevVertex,
								currentVertex, prevDistance, currentDistance);
						HalfEdge newEdge = EdgeSplit(current, newVertex);
					
						// prev:inside - current:outside - next:??
						if (prevSide == PlaneSideResult.Inside)
						{
							// edge01 exits:
							//
							// outside
							// 1
							// *
							// ......./........ intersect
							// /
							// 0
							// inside

							exitEdge = current;

						}
						// prev:outside - current:inside - next:??
						else if (prevSide == PlaneSideResult.Outside)
						{
							// edge01 enters:
							//
							// outside
							// 0
							// \
							// .......\........ intersect
							// *
							// 1
							// inside

							enterEdge = current;
						}

						prevDistance = 0;
						prev = Edges.get(prev.NextIndex);
						prevSide = PlaneSideResult.Intersects;

						if (exitEdge != null && enterEdge != null)
							break;

						current = Edges.get(prev.NextIndex);
						currentVertex = Vertices.get(current.VertexIndex);

						next = Edges.get(current.NextIndex);
						nextVertex = Vertices.get(next.VertexIndex);
					}
				} else
				// prev:?? - current:intersects - next:??
				{
					// prev:intersects - current:intersect - next:??
					if (prevSide == PlaneSideResult.Intersects ||
					// prev:?? - current:intersects - next:intersects
							nextSide == PlaneSideResult.Intersects ||
							// prev:inside/outside - current:intersects -
							// next:inside/outside
							prevSide == nextSide)
					{
						// prev:inside - current:intersects -
						// next:intersects/inside
						if (prevSide == PlaneSideResult.Inside ||
						// prev.Intersects/inside - current:intersect -
								// next:inside
								nextSide == PlaneSideResult.Inside)
						{
							// outside
							// 0 1
							// --------*....... intersect
							// \
							// 2
							// inside
							//
							// outside
							// 1 2
							// ........*------- intersect
							// /
							// 0
							// inside
							//
							// outside
							// 1
							// ........*....... intersect
							// / \
							// 0 2
							// inside
							//

							prevSide = PlaneSideResult.Inside;
							enterEdge = exitEdge = null;
							break;
						}
						// prev:outside - current:intersects -
						// next:intersects/outside
						else if (prevSide == PlaneSideResult.Outside ||
						// prev:intersects/outside - current:intersects -
								// next:outside
								nextSide == PlaneSideResult.Outside)
						{
							// outside
							// 2
							// /
							// ..------*....... intersect
							// 0 1
							// inside
							//
							// outside
							// 0
							// \
							// ........*------- intersect
							// 1 2
							// inside
							//
							// outside
							// 0 2
							// \ /
							// ........*....... intersect
							// 1
							// inside
							//

							prevSide = PlaneSideResult.Outside;
							enterEdge = exitEdge = null;
							break;
						}
					}
					// prev:inside/outside - current:intersects -
					// next:inside/outside
					else
					{
						// prev:inside - current:intersects - next:outside
						if (prevSide == PlaneSideResult.Inside)
						{
							// find exit edge:
							//
							// outside
							// 2
							// 1 /
							// ........*....... intersect
							// /
							// 0
							// inside

							exitEdge = current;
							if (enterEdge != null)
								break;
						}
						// prev:outside - current:intersects - next:inside
						else
						{
							// find enter edge:
							//
							// outside
							// 0
							// \ 1
							// ........*....... intersect
							// \
							// 2
							// inside

							enterEdge = current;
							if (exitEdge != null)
								break;
						}
					}
				}
			}

			prev = current;
			current = next;
			next = Edges.get(next.NextIndex);

			prevDistance = currentDistance;
			currentDistance = nextDistance;
			prevSide = currentSide;
			currentSide = nextSide;
			prevVertex = currentVertex;
			currentVertex = nextVertex;
		} while (next != last);

		// We should never have only one edge crossing the plane ..
		// Debug.Assert((enterEdge == null) == (exitEdge == null));
		if (!((enterEdge == null) == (exitEdge == null)))
		{
			throw new IllegalStateException();
		}

		// Check if we have an edge that exits and an edge that enters the plane
		// and split the polygon into two if we do
		if (enterEdge != null && exitEdge != null)
		{
			// enter .
			// .
			// =====>*----->
			// .
			//
			// outside . inside
			// .
			// <-----*<=====
			// .
			// . exit

			// outsidePolygon = new Polygon();
			int outsidePolygonIndex = this.Polygons.size();
			this.Polygons.add(outsidePolygon);

			HalfEdge outsideEdge = new HalfEdge();
			int outsideEdgeIndex = Edges.size();

			HalfEdge insideEdge = new HalfEdge();
			int insideEdgeIndex = outsideEdgeIndex + 1;

			outsideEdge.TwinIndex = insideEdgeIndex;
			insideEdge.TwinIndex = outsideEdgeIndex;

			// insideEdge.PolygonIndex = inputPolygonIndex;// index does not
			// change
			outsideEdge.PolygonIndex = outsidePolygonIndex;

			outsideEdge.VertexIndex = exitEdge.VertexIndex;
			insideEdge.VertexIndex = enterEdge.VertexIndex;

			outsideEdge.NextIndex = exitEdge.NextIndex;
			insideEdge.NextIndex = enterEdge.NextIndex;

			exitEdge.NextIndex = insideEdgeIndex;
			enterEdge.NextIndex = outsideEdgeIndex;

			outsidePolygon.FirstIndex = outsideEdgeIndex;
			inputPolygon.FirstIndex = insideEdgeIndex;

			outsidePolygon.Visible = inputPolygon.Visible;
			outsidePolygon.Category = inputPolygon.Category;
			outsidePolygon.PlaneIndex = inputPolygon.PlaneIndex;

			Edges.add(outsideEdge);
			Edges.add(insideEdge);

			// calculate the bounds of the polygons
			outsidePolygon.Bounds.Clear();
			HalfEdge first = Edges.get(outsidePolygon.FirstIndex);
			HalfEdge iterator = first;
			do
			{
				outsidePolygon.Bounds.Add(Vertices.get(iterator.VertexIndex));
				iterator.PolygonIndex = outsidePolygonIndex;
				iterator = Edges.get(iterator.NextIndex);
			} while (iterator != first);

			inputPolygon.Bounds.Clear();
			first = Edges.get(inputPolygon.FirstIndex);
			iterator = first;
			do
			{
				inputPolygon.Bounds.Add(Vertices.get(iterator.VertexIndex));
				iterator = Edges.get(iterator.NextIndex);
			} while (iterator != first);

			return PolygonSplitResult.Split;
		} else
		{
			// outsidePolygon = null;
			// Polygon.counter--;
			switch (prevSide)
			{
			case Inside:
				return PolygonSplitResult.CompletelyInside;
			case Outside:
				return PolygonSplitResult.CompletelyOutside;
			default:
			case Intersects:
			{
				Plane polygonPlane = Planes[inputPolygon.PlaneIndex];
				Double result = Vector3.DotProduct(polygonPlane.Normal(),
						cuttingPlane.Normal());
				if (result > 0)
					return PolygonSplitResult.PlaneAligned;
				else
					return PolygonSplitResult.PlaneOppositeAligned;
			}
			}
		}
	}

	// Intersects a mesh with a brush (set of planes)
	public void Intersect(AABB cuttingNodeBounds, Plane[] cuttingNodePlanes,
			Vector3 cuttingNodeTranslation, Vector3 inputPolygonTranslation,

			List<Polygon> inputPolygons,

			List<Polygon> inside, List<Polygon> aligned,
			List<Polygon> revAligned, List<Polygon> outside)
	{
		// PolygonSplitResult[] categories = new
		// PolygonSplitResult[cuttingNodePlanes.length];
		Plane[] translatedPlanes = new Plane[cuttingNodePlanes.length];
		Vector3 translation = Vector3.Subtract(cuttingNodeTranslation,
				inputPolygonTranslation);

		// translate the planes we cut our polygons with so that they're located
		// at the same
		// relative distance from the polygons as the brushes are from each
		// other.
		for (int i = 0; i < cuttingNodePlanes.length; i++)
			translatedPlanes[i] = Plane.Translated(cuttingNodePlanes[i],
					translation);

		// List<Vector3> vertices = this.Vertices;
		// List<HalfEdge> edges = this.Edges;
		// Plane[] planes = this.Planes;
		for (int i = inputPolygons.size() - 1; i >= 0; i--)
		{
			Polygon inputPolygon = inputPolygons.get(i);
			if (inputPolygon.FirstIndex == -1)
				continue;

			AABB bounds = inputPolygon.Bounds;
			PolygonSplitResult finalResult = PolygonSplitResult.CompletelyInside;

			// A quick check if the polygon lies outside the planes we're
			// cutting our polygons with.
			if (!AABB.IsOutside(cuttingNodeBounds, translation, bounds))
			{
				PolygonSplitResult intermediateResult;
				for (int otherIndex = 0; otherIndex < translatedPlanes.length; otherIndex++)
				{
					Polygon outsidePolygon = new Polygon();
					Plane translatedCuttingPlane = translatedPlanes[otherIndex];

					PlaneSideResult side = cuttingNodePlanes[otherIndex]
							.OnSide(bounds, translation.Negated());
					if (side == PlaneSideResult.Outside)
					{
						finalResult = PolygonSplitResult.CompletelyOutside;
						break; // nothing left to process, so we exit
					} else if (side == PlaneSideResult.Inside)
						continue;

					Polygon polygon = inputPolygon;
					intermediateResult = PolygonSplit(translatedCuttingPlane,
							inputPolygonTranslation, polygon, outsidePolygon);
					inputPolygon = polygon;

					if (intermediateResult == PolygonSplitResult.CompletelyOutside)
					{
						finalResult = PolygonSplitResult.CompletelyOutside;
						break; // nothing left to process, so we exit
					} else if (intermediateResult == PolygonSplitResult.Split)
					{
						if (outside != null)
							outside.add(outsidePolygon);
						// Note: left over is still completely inside,
						// or plane (opposite) aligned
					} else if (intermediateResult != PolygonSplitResult.CompletelyInside)
						finalResult = intermediateResult;
				}
			} else
				finalResult = PolygonSplitResult.CompletelyOutside;

			switch (finalResult)
			{
			case CompletelyInside:
				inside.add(inputPolygon);
				break;
			case CompletelyOutside:
				outside.add(inputPolygon);
				break;

			// The polygon can only be visible if it's part of the last brush
			// that shares it's surface area,
			// otherwise we'd get overlapping polygons if two brushes overlap.
			// When the (final) polygon is aligned with one of the cutting
			// planes, we know it lies on the surface of
			// the CSG node we're cutting the polygons with. We also know that
			// this node is not the node this polygon belongs to
			// because we've done that check earlier on. So we flag this polygon
			// as being invisible.
			case PlaneAligned:
				inputPolygon.Visible = false;
				aligned.add(inputPolygon);
				break;
			case PlaneOppositeAligned:
				inputPolygon.Visible = false;
				revAligned.add(inputPolygon);
				break;
			}
		}
	}

	// Combines multiple meshes into one
	public static CSGMesh Combine(Vector3 offset,
			KVPairs<CSGNode, CSGMesh> brushMeshes)
	{
		KVPairs<Plane, Integer> planeLookup = new KVPairs<Plane, Integer>();
		KVPairs<Vector3, Integer> vertexLookup = new KVPairs<Vector3, Integer>();

		List<Plane> planes = new ArrayList<Plane>();
		List<Polygon> polygons = new ArrayList<Polygon>();
		List<HalfEdge> edges = new ArrayList<HalfEdge>();
		List<Vector3> vertices = new ArrayList<Vector3>();

		AABB bounds = new AABB();

		bounds.Clear();
		int edgeIndex = 0;
		int polygonIndex = 0;
		// for (Set<CSGNode> keys = brushMeshes.keySet(); keys
		// .hasMoreElements();) {
		for (CSGNode node : brushMeshes.keys())
		{
			// CSGNode node = keys.nextElement();
			Vector3 translation = Vector3.Subtract(node.Translation, offset);
			CSGMesh mesh = brushMeshes.get(node);
			for (HalfEdge edge : mesh.Edges)
			{
				Vector3 vertex = Vector3.Add(mesh.Vertices
						.get(edge.VertexIndex), translation);
				Integer vertexIndex = vertexLookup.get(vertex);
				if (vertexIndex == null)
				{
					vertexIndex = vertices.size();
					vertices.add(vertex);
					vertexLookup.put(vertex, vertexIndex);
				}

				HalfEdge newEdge = new HalfEdge();
				newEdge.VertexIndex = vertexIndex;
				newEdge.NextIndex = edge.NextIndex + edgeIndex;
				newEdge.TwinIndex = edge.TwinIndex + edgeIndex;
				newEdge.PolygonIndex = edge.PolygonIndex + polygonIndex;

				edges.add(newEdge);
			}

			for (Polygon polygon : mesh.Polygons)
			{
				if (polygon.FirstIndex == -1)
					continue;

				Plane plane = mesh.Planes[polygon.PlaneIndex];
				Integer planeIndex = planeLookup.get(plane);
				if (planeIndex == null)
				{
					planeIndex = planes.size();
					planes.add(plane);
					planeLookup.put(plane, planeIndex);
				}

				Polygon newPolygon = new Polygon();
				newPolygon.PlaneIndex = planeIndex;
				newPolygon.FirstIndex = polygon.FirstIndex + edgeIndex;
				newPolygon.Category = polygon.Category;
				newPolygon.Visible = polygon.Visible;
				newPolygon.Bounds.Set(polygon.Bounds, translation);

				polygons.add(newPolygon);
				
				if (newPolygon.Visible)
				{
					/* reverse vertex order and invert normal of reversed aligned polygons at root, so we are not looking at the backside */
					if (newPolygon.Category == PolygonCategory.ReverseAligned)
					{
						CSGUtility.reverseVertexOrder(newPolygon, edges, vertices);
						Plane polygonsPlane = planes.get(planeIndex);
						Vector3 planeNormal = polygonsPlane.Normal();
						polygonsPlane.setNormal(planeNormal.Negated());	
						newPolygon.Category = PolygonCategory.Aligned;
					}
					
					HalfEdge first = edges.get(newPolygon.FirstIndex);
					HalfEdge iterator = first;
					do
					{
						bounds.Add(vertices.get(iterator.VertexIndex));
						iterator = edges.get(iterator.NextIndex);
					} while (iterator != first);
				}
			}
			edgeIndex = edges.size();
			polygonIndex = polygons.size();
		}
		return new CSGMesh(planes.toArray(new Plane[0]), polygons, edges,
				vertices, bounds);
	}

	/**
	 * Implements collinearity filter, see v. Rossen & Baranowski, section mesh optimization 
	 */
	public void filterMesh()
	{
		int nonEmptyLines = 0;
		int linesWithAdditionalPoints = 0;
		int totalPointsInserted = 0;
		
		/*
		 * List storing all InfiniteLines and KVPairs look-up-table(LUT)
		 * edge->InfiniteLine
		 */
		ArrayList<InfiniteLine> InfiniteLines = new ArrayList<InfiniteLine>();
		KVPairs<HalfEdge, InfiniteLine> edgeToLineLUT = new KVPairs<HalfEdge, InfiniteLine>();

		/*
		 * construct LUT for edges, points of edges will be registered on
		 * infinite lines, lines will be stored in ArrayList above, indices of
		 * which edge falls onto what line can be found in the KVPairs
		 */
		for (int i=0; i<Edges.size(); ++i)
		{
			HalfEdge currentEdge = Edges.get(i);
			
			debugOut("constructing LUT for Edge #"+(i+1));
			/*
			 * get start vertex from current edge, end vertex from it's twin,
			 * construct edge directional vector from these two.
			 */
			Vector3 edgeStart = Vertices.get(currentEdge.VertexIndex);
			Vector3 edgeEnd = Vertices.get(Edges.get(currentEdge.TwinIndex).VertexIndex);
			Vector3 edgeDirection = Vector3.minus(edgeStart, edgeEnd);

			/* degenerate edge, skip */
			if (edgeDirection.Length() < VarsConstants.EdgeLengthEpsilon) continue;
			/* twin edge already registered, so this edge's points are already on an InfiniteLine, skip */
			if ( edgeToLineLUT.get(Edges.get(currentEdge.TwinIndex)) != null ) continue;

			boolean registered = false;

			/* find the InfiniteLine, this edge lies on */
			for (int j=0; j<InfiniteLines.size(); ++j)
			{
				InfiniteLine currentLine = InfiniteLines.get(j);
				
				if (currentLine.EdgeOnLine(edgeStart, edgeEnd))
				{
					currentLine.addPoint(edgeStart);
					currentLine.addPoint(edgeEnd);
					
					edgeToLineLUT.put(currentEdge, currentLine);
					registered = true;
					//debugOut("!!!!!!!!!!!! Edge no " + i + " added to infinite Line no " + j + " !!!!!!!!!!!!!");
					break;
				}
			}

			/*
			 * no InfiniteLine for currentEdge was found, construct new
			 * InfiniteLine from currentEdge
			 */
			if (!registered)
			{
				InfiniteLine newLine = new InfiniteLine(edgeStart, edgeDirection);
				newLine.addPoint(edgeStart);
				newLine.addPoint(edgeEnd);
				
				InfiniteLines.add(newLine);
				edgeToLineLUT.put(currentEdge, newLine);
				//debugOut("new Infinite Line constructed from Edge #"+(i+1)+", #of Lines: "+InfiniteLines.size());
			}
			++i;
		}

		debugOut("======================");
		debugOut("constructing LUT done.");
		debugOut("======================");
		
		ArrayList<SplitInfo> edgesToSplit = new ArrayList<SplitInfo>();
		/*
		 * for all edges, check on which line they fall and if there are
		 * additional points between start and end
		 */
		for (int i=0; i<Edges.size(); ++i)
		{
			HalfEdge currentEdge = Edges.get(i);
			
			/* twin edge already registered, so this edge's points are already on an InfiniteLine, skip */
			if ( edgeToLineLUT.get(Edges.get(currentEdge.TwinIndex)) != null ) continue;
			
			InfiniteLine line = edgeToLineLUT.get(currentEdge);
			
			/*
			 * line might be null, in case edge wasn't registered, i.e. it is
			 * degenerate
			 */
			if (line != null)
			{
				++nonEmptyLines;
				/* get start vertex from current edge, end vertex from it's twin */
				Vector3 edgeStart = Vertices.get(currentEdge.VertexIndex);
				Vector3 edgeEnd = Vertices.get(Edges.get(currentEdge.TwinIndex).VertexIndex);

				ArrayList<PointOnLine> pointList = line.getPointsOnLine();
				int indexStart = CSGUtility.indexOfVertexInPoLL(edgeStart, pointList);
				int indexEnd = CSGUtility.indexOfVertexInPoLL(edgeEnd, pointList);
				
				/* check for fatal error */
				if ((indexStart < 0) || (indexEnd < 0))
				{
					throw new IllegalArgumentException("Edge start or end not found on line in meshFilter for edge #"+ i + ". Edge startIndex: " + indexStart+ ", Edge endIndex: " + indexEnd);
				}

				/*
				 * swap Start and End index, in case they are in wrong order
				 * (maybe) due to numerical imprecision
				 */
				if (indexStart > indexEnd)
				{
					int h = indexStart; indexStart = indexEnd; indexEnd = h;
				}

				/*
				 * split candidate found, there are further points between start
				 * and end, save info, split later to avoid concurrent modification of Edges list
				 */
				if (indexStart != (indexEnd - 1))
				{
					++linesWithAdditionalPoints;
					
					debugOut("intermediate points found for edge #"+i+", #ofPoints "+(indexEnd-indexStart));
					SplitInfo info = new SplitInfo();
					info.halfEdgeIndex = i;
					info.startIndex = indexStart;
					info.endIndex = indexEnd;
					edgesToSplit.add(info);
				}
			}
		}
		
		/* do the actual splitting on all edges holding intermediate point */
		for ( SplitInfo entry : edgesToSplit )
		{
			HalfEdge splitEdge = Edges.get(entry.halfEdgeIndex);
			int indexStart = entry.startIndex;
			int indexEnd = entry.endIndex;
			
			InfiniteLine line = edgeToLineLUT.get(splitEdge);
			ArrayList<PointOnLine> pointList = line.getPointsOnLine();

			/*
			 * repeatedly split the edge at all intermediate vertices
			 */
			for (int j = indexStart + 1; j < indexEnd; ++j)
			{
				Vector3 point = pointList.get(j).point;
				//Vector3 projPoint = pointList.get(j).pointProjection;
				Vector3 insertPoint = new Vector3(point);
				/* snap vertex onto other edge */
				//point = projPoint;
				
				splitEdge = EdgeSplit(splitEdge, insertPoint);
				++totalPointsInserted;
			}
		}
		
		debugOut("non empty lines: "+nonEmptyLines+", lines with intermediate points: "+linesWithAdditionalPoints+", totalPointsInserted: "+totalPointsInserted+", total #of Edges: "+Edges.size());
	}
	
	/* filtering for boundary edges only */
	public void filterMesh2()
	{
		int nonEmptyLines = 0;
		int linesWithAdditionalPoints = 0;
		int totalPointsInserted = 0;
		
		int edgeIndex = 0;
		for ( HalfEdge currentEdge : this.Edges)
		{
			HalfEdge currentEdgesTwin = this.Edges.get(currentEdge.TwinIndex);
			/* find Polygon, this half edge lies in */
			Polygon polygonOfCurrentEdge = findPolygonofEdge(edgeIndex);
			Polygon polygonOfCurrentEdgeTwin = findPolygonofEdge(currentEdge.TwinIndex);
			
			if ( (polygonOfCurrentEdge == null) || (polygonOfCurrentEdgeTwin == null) )
			{
				throw new IllegalArgumentException("edges don't lie in any polygon");
			}
			
			if ( polygonOfCurrentEdge.Visible && !polygonOfCurrentEdgeTwin.Visible ) this.boundaryEdges.add(currentEdge);
			else if ( !polygonOfCurrentEdge.Visible && polygonOfCurrentEdgeTwin.Visible ) this.boundaryEdges.add(currentEdgesTwin);
		}
		
		debugOut("#of boundaryEdges in filter: "+this.boundaryEdges.size()+" / ("+this.Edges.size()+" total)");
		
		/*
		 * List storing all InfiniteLines and KVPairs look-up-table(LUT)
		 * edge->InfiniteLine
		 */
		ArrayList<InfiniteLine2> InfiniteLines = new ArrayList<InfiniteLine2>();
		KVPairs<HalfEdge, InfiniteLine2> edgeToLineLUT = new KVPairs<HalfEdge, InfiniteLine2>();
		
		/* construct infinite lines for boundary edges only, to check which additional points are on collinear edges */
		for ( HalfEdge edge : this.boundaryEdges )
		{
			/*
			 * get start vertex from current edge, end vertex from it's twin,
			 * construct edge directional vector from these two.
			 */
			Vector3 edgeStart = Vertices.get(edge.VertexIndex);
			Vector3 edgeEnd = Vertices.get(Edges.get(edge.TwinIndex).VertexIndex);
			Vector3 edgeDirection = Vector3.minus(edgeStart, edgeEnd);
	
			InfiniteLine2 newLine = new InfiniteLine2(edgeStart, edgeDirection, edge);
			newLine.addPoint(edgeStart);
			newLine.addPoint(edgeEnd);
			
			InfiniteLines.add(newLine);
			edgeToLineLUT.put(edge, newLine);
			//debugOut("new Infinite Line constructed from Edge #"+(i+1)+", #of Lines: "+InfiniteLines.size());
		}

		/*
		 * construct LUT for edges, points of edges will be registered on
		 * infinite lines, lines will be stored in ArrayList above, indices of
		 * which edge falls onto what line can be found in the KVPairs
		 */
		for ( HalfEdge currentEdge : this.Edges )
		{
			/*
			 * get start vertex from current edge, end vertex from it's twin,
			 * construct edge directional vector from these two.
			 */
			Vector3 edgeStart = Vertices.get(currentEdge.VertexIndex);
			Vector3 edgeEnd = Vertices.get(Edges.get(currentEdge.TwinIndex).VertexIndex);
			Vector3 edgeDirection = Vector3.minus(edgeStart, edgeEnd);

			/* degenerate edge, skip */
			if (edgeDirection.Length() < VarsConstants.EdgeLengthEpsilon) continue;
			/* twin edge already registered, so this edge's points are already on an InfiniteLine, skip */
			if ( edgeToLineLUT.get(Edges.get(currentEdge.TwinIndex)) != null ) continue;

			/* find the InfiniteLine, this edge lies on */
			for (int j=0; j<InfiniteLines.size(); ++j)
			{
				InfiniteLine2 currentLine = InfiniteLines.get(j);
				
				if (currentLine.EdgeOnLine(edgeStart, edgeEnd))
				{
					currentLine.addEdge(currentEdge);
					currentLine.addPoint(edgeStart);
					currentLine.addPoint(edgeEnd);
					
					edgeToLineLUT.put(currentEdge, currentLine);
					//debugOut("!!!!!!!!!!!! Edge no " + i + " added to infinite Line no " + j + " !!!!!!!!!!!!!");
					break;
				}
			}
		}

		debugOut("======================");
		debugOut("constructing LUT done.");
		debugOut("======================");
		
		int i = 0;
		ArrayList<SplitInfo> edgesToSplit = new ArrayList<SplitInfo>();
		
		for ( InfiniteLine2 infiniteLine : InfiniteLines )
		{
			List<HalfEdge> collinearEdges = infiniteLine.getEdgesOnLine();
			HalfEdge inducingEdge = infiniteLine.getInducingEdge();
						
			int noOfBoundaryCollinearEdges = 0;
			
			/* 
			 * find collinear edges that are also boundary for welding step
			 *
			for ( HalfEdge collinearEdge : collinearEdges )
			{
				/* check if one of the boundary edges is collinear to this one *
				for ( HalfEdge boundaryEdge : this.boundaryEdges )
				{
					if ( collinearEdge.equals(boundaryEdge) )
					{
						/* check if collinear edge is not self *
						if ( !inducingEdge.equals(collinearEdge) );
						{
							/* found another boundary edge collinear to this one *
							++noOfBoundaryCollinearEdges;
							/* try to "weld" edges *
							boolean weldingMatch = weldEdges(inducingEdge, collinearEdge);
							if ( weldingMatch ) debugOut("found welding match!");
						}
					}
				}
			}
			
			debugOut("#of collinear edges: "+collinearEdges.size()+"  --  found #"+noOfBoundaryCollinearEdges+" collinear edge that are boundary");*/
			
			/* 
			 * vertex snapping step
			 */
			
			/* get start vertex from inducing edge, end vertex from it's twin */
			Vector3 edgeStart = Vertices.get(inducingEdge.VertexIndex);
			Vector3 edgeEnd = Vertices.get(Edges.get(inducingEdge.TwinIndex).VertexIndex);
			
			ArrayList<PointOnLine> pointList = infiniteLine.getPointsOnLine();
			int indexStart = CSGUtility.indexOfVertexInPoLL(edgeStart, pointList);
			int indexEnd = CSGUtility.indexOfVertexInPoLL(edgeEnd, pointList);
			
			debugOut("indexStart, indexEnd: "+indexStart+", "+indexEnd);
			
			/* check for fatal error */
			if ((indexStart < 0) || (indexEnd < 0))
			{
				throw new IllegalArgumentException("Edge start or end not found on line in meshFilter");
			}
			/*
			 * swap Start and End index, in case they are in wrong order
			 * (maybe) due to numerical imprecision
			 */
			if (indexStart > indexEnd)
			{
				int h = indexStart; indexStart = indexEnd; indexEnd = h;
			}
			/*
			 * split candidate found, there are further points between start
			 * and end, save info, split later to avoid concurrent modification of Edges list
			 */
			if (indexStart != (indexEnd - 1))
			{
				++linesWithAdditionalPoints;
				
				debugOut("intermediate points found for edge, #ofPoints "+(indexEnd-indexStart));
				SplitInfo info = new SplitInfo();
				info.halfEdgeIndex = i;
				info.startIndex = indexStart;
				info.endIndex = indexEnd;
				edgesToSplit.add(info);
			}
			
			++i;
		}
		
		
		/* do the actual splitting on all edges holding intermediate point */
		for ( SplitInfo entry : edgesToSplit )
		{
			InfiniteLine2 line = InfiniteLines.get(entry.halfEdgeIndex);
			
			HalfEdge splitEdge = line.getInducingEdge();
			int indexStart = entry.startIndex;
			int indexEnd = entry.endIndex;
			
			ArrayList<PointOnLine> pointList = line.getPointsOnLine();

			/*
			 * repeatedly split the edge at all intermediate vertices
			 */
			for (int j = indexStart + 1; j < indexEnd; ++j)
			{
				Vector3 point = pointList.get(j).point;
				//Vector3 projPoint = pointList.get(j).pointProjection;
				Vector3 insertPoint = new Vector3(point);
				/* snap vertex onto other edge */
				//point = projPoint;
				
				splitEdge = EdgeSplit(splitEdge, insertPoint);
				++totalPointsInserted;
			}
		}
		debugOut("non empty lines: "+nonEmptyLines+", lines with intermediate points: "+linesWithAdditionalPoints+", totalPointsInserted: "+totalPointsInserted+", total #of Edges: "+Edges.size());
	}
	
	/**
	 * Implements Borodin, Novotni, Klein: "progressive gap closing..." 
	 */
	public void filterMesh3()
	{	
		int edgeIndex = 0;
		for ( HalfEdge currentEdge : this.Edges)
		{
			HalfEdge currentEdgesTwin = this.Edges.get(currentEdge.TwinIndex);
			/* find Polygon, this half edge and it's twin lie in */
			Polygon polygonOfCurrentEdge = findPolygonofEdge(edgeIndex);
			Polygon polygonOfCurrentEdgeTwin = findPolygonofEdge(currentEdge.TwinIndex);
			
			if ( (polygonOfCurrentEdge == null) || (polygonOfCurrentEdgeTwin == null) )
			{
				throw new IllegalArgumentException("edges don't lie in any polygon");
			}
			
			if ( polygonOfCurrentEdge.Visible && !polygonOfCurrentEdgeTwin.Visible ) this.boundaryEdges.add(currentEdge);
			else if ( !polygonOfCurrentEdge.Visible && polygonOfCurrentEdgeTwin.Visible ) this.boundaryEdges.add(currentEdgesTwin);
		}
		
		//debugOut("#of boundaryEdges in filter: "+this.boundaryEdges.size()+" / ("+this.Edges.size()+" total)");
		
		/* construct feature from boundary edges */
		ArrayList<Feature> boundaryFeatures = new ArrayList<Feature>();
		int boundaryVertices = 0;
		//int boundaryEdgeCounter = 0;
		
		for ( HalfEdge edge : this.boundaryEdges )
		{
			HalfEdge edgesTwin = this.Edges.get(edge.TwinIndex);
			Vector3 edgeStart = Vertices.get(edge.VertexIndex);
			Vector3 edgeEnd = Vertices.get(Edges.get(edge.TwinIndex).VertexIndex);
			
			FeatureEdge featureEdge = new FeatureEdge(edge, edgesTwin, edgeStart, edgeEnd);
			FeatureVertex featureStart = new FeatureVertex(edgeStart);
			FeatureVertex featureEnd = new FeatureVertex(edgeEnd);
			
			boundaryFeatures.add(featureEdge);
			boundaryFeatures.add(featureStart);
			boundaryFeatures.add(featureEnd);
			
			boundaryVertices+=2;
			//++boundaryEdgeCounter;
			
			//debugOut("building new Feature for Edge number: "+boundaryEdgeCounter);
		}
		
		/* priority queue, ordering features by distance to nearest feature */
		PriorityQueue<Feature> pqVertexFeatures = new PriorityQueue<Feature>(boundaryVertices, new FeatureComparator());
		
		//boundaryVertices = 0;
		/* preprocessing, for every feature vertex, find nearest feature */
		for ( Feature feature : boundaryFeatures)
		{
			if ( feature instanceof FeatureVertex )
			{ 
				((FeatureVertex) feature).findNearestBoundaryEdge(boundaryFeatures);
				pqVertexFeatures.add(feature);
				//++boundaryVertices;
				//debugOut("features found for boundary vertex no: "+boundaryVertices);
			}
		}		
		
		debugOut("preprocessing done.");
		
		ArrayDeque<FeatureEdge> modifiedEdges = new ArrayDeque<FeatureEdge>();

		//debugOut("pqVertexFeatures.size(): "+pqVertexFeatures.size());
		
		/* decimation step */
		while ( !pqVertexFeatures.isEmpty() )
		{
			FeatureVertex min = (FeatureVertex) pqVertexFeatures.poll();
			Feature f = min.nearestFeature;
			
			/* if distance > threshold STOP */
			if ( min.distanceToNearestFeature > VarsConstants.DistanceEpsilonWelding ) {debugOut("minDistance: "+min.distanceToNearestFeature+", min greater than threshold, stop."); return;}
			
			if ( f instanceof FeatureVertex )
			{
				FeatureVertex fFeatureVertex = (FeatureVertex) f;
				vertexVertexContraction(min.point, fFeatureVertex.point, 0.5);
			}
			else if ( f instanceof FeatureEdge )
			{
				FeatureEdge fFeatureEdge = (FeatureEdge) f;
				vertexEdgeContraction(min, fFeatureEdge , modifiedEdges, boundaryFeatures);
			}
			
			while ( !modifiedEdges.isEmpty() )
			{
				/* update corresponding features information for all vertices of all modified edges */
				FeatureEdge featureEdge = modifiedEdges.poll();
				ArrayDeque<FeatureVertex> correspondingFeatures = (ArrayDeque<FeatureVertex>) featureEdge.correspondingFeatures.clone();
				/* reset vertices registered for this edge, since their correspondences are calculated anew */
				featureEdge.correspondingFeatures.clear();
				for ( FeatureVertex featureVertex : correspondingFeatures )
				{
					if (pqVertexFeatures.remove(featureVertex))
					{
						featureVertex.findNearestBoundaryEdge(boundaryFeatures);
						pqVertexFeatures.add(featureVertex);
					}
				}
			}
		}
	}
	
	/**
	 * Vertex-Vertex contraction. Contracts to vertices into the exact same spot as a convex combination 
	 * 
	 * @param a vertex 1 to contract
	 * @param b vertex 2 to contract
	 * @param lambda new vertex position is defined as lambda*a + (1-lambda*b)
	 */
	private void vertexVertexContraction(Vector3 a, Vector3 b, double lambda)
	{
		Vector3 v = Vector3.mult(lambda, a);
		Vector3 vPrime = Vector3.mult(1.0-lambda, b);
		Vector3 vnew = Vector3.Add(v, vPrime);
		
		/* drag both initial vertices into same position */
		a.X = b.X = vnew.X;
		a.Y = b.Y = vnew.Y;
		a.Z = b.Z = vnew.Z;
		debugOut("vertex-vertex contraction\n");
	}
	
	/**
	 * Vertex-Edge contraction. Contracts a vertex and an edge. If orthogonal projection of vertex falls
	 * into epsilon vicinity of one of the edges end vertices, vertex-vertex contraction of vertex and edge
	 * end vertex is performed instead. Else orthogonal projection is snapped to the edge, edge marked as
	 * modified and vertex-orthogonal projection contraction is performed.
	 * @param a vertex to contract
	 * @param b edge to contract vertex to
	 * @param modifiedEdges edges that were modified
	 * @param boundaryFeatures all features on the boundary
	 */
	private void vertexEdgeContraction(FeatureVertex a, FeatureEdge b, ArrayDeque<FeatureEdge> modifiedEdges, ArrayList<Feature> boundaryFeatures)
	{
		Vector3 point = a.point;
		/* check if orthogonal projection is near edge vertex */
		Vector3 orthogonalProjection = b.getOrthogonalProjectionOntoEdge(point);
		/* if near edge vertex perform vertex-vertex instead */
		if ( orthogonalProjection.Distance(b.edgeStart) <= VarsConstants.DistanceEpsilonWelding )
		{
			debugOut("vertex-vertex by vertex-edge contraction (start)");
			vertexVertexContraction(point, b.edgeStart, 0.5);
			return;
		}
		else if ( orthogonalProjection.Distance(b.edgeEnd) <= VarsConstants.DistanceEpsilonWelding )
		{
			debugOut("vertex-vertex by vertex-edge contraction (end)");
			vertexVertexContraction(point, b.edgeEnd, 0.5);
			return;
		}
		
		debugOut("vertex-edge contraction");
		/* else split edge, mark as modified, what to do with new edge?, finally move vertices into new position by vertex-vertex contraction */
		HalfEdge featureEdge = b.edge;
		HalfEdge newEdge = EdgeSplit(featureEdge, orthogonalProjection);
		modifiedEdges.add(b);
		HalfEdge newEdgeTwin = this.Edges.get(newEdge.TwinIndex);
		Vector3 newEdgeStart = this.Vertices.get(newEdge.VertexIndex);
		Vector3 newEdgeEnd = this.Vertices.get(newEdgeTwin.VertexIndex);
		FeatureEdge newFeature = new FeatureEdge(newEdge, newEdgeTwin, newEdgeStart, newEdgeEnd);
		boundaryFeatures.add(newFeature);
		vertexVertexContraction(point, orthogonalProjection, 0.5);
		
		//debugOut("\n");
	}
	

	/**
	 * finds the polygon a given half edge lies in
	 * 
	 * @param edgeIndex index of the half edge to be looked up
	 * @return the polygon this half edge lies in
	 */
	private Polygon findPolygonofEdge(int edgeIndex)
	{
		for ( Polygon currentPolygon : this.Polygons )
		{
			if ( currentPolygon.FirstIndex == edgeIndex ) return currentPolygon;
			
			HalfEdge firstEdge = this.Edges.get(currentPolygon.FirstIndex);
			HalfEdge currentEdge = this.Edges.get(firstEdge.NextIndex);
			int currentIndex = firstEdge.NextIndex;
			
			while (firstEdge != currentEdge)
			{
				if ( currentIndex == edgeIndex ) return currentPolygon;
				 
				currentIndex = currentEdge.NextIndex;
				currentEdge = this.Edges.get(currentEdge.NextIndex);
			}
		}
		
		return null;
	}
	
	/*
	 * helper methode, delete to identify debugMessages
	 */
	static void debugOut(String message)
	{
		System.out.println(message);
	}
	
}
