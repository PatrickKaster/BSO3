package net.cropsense.bso3;

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

import de.grogra.graph.impl.Node;
import de.grogra.imp3d.objects.Box;
import de.grogra.imp3d.objects.Cone;
import de.grogra.imp3d.objects.Cylinder;
import de.grogra.imp3d.objects.Frustum;
import de.grogra.imp3d.objects.GlobalTransformation;
import de.grogra.imp3d.objects.ShadedNull;
import de.grogra.imp3d.objects.Sphere;


/**
 * class transforming a graphical primitive from GroIMP to a plane constrained
 * representation as a brush
 * 
 * @author Florian Schoeler, Patrick Kaster
 *
 */
public class Transformer
{

	public static CSGNode getNodeFor(Node n)
	{
		if (!(n instanceof Box || n instanceof Cone || n instanceof Cylinder
				|| n instanceof Frustum || n instanceof Sphere))
			throw new IllegalArgumentException(
					"n is not an instance of one of the known geometric primitives.");

		if (n instanceof Box)
		{
			return toBox((Box) n);
		} else if (n instanceof Cone)
		{
			return toCone((Cone) n);
		} else if (n instanceof Cylinder)
		{
			return toCylinder((Cylinder) n);
		} else if (n instanceof Frustum)
		{
			return toFrustum((Frustum) n);
		} else if (n instanceof Sphere)
		{
			return toSphere((Sphere) n);
		}

		return null;
	}

	/**
	 * Gets the global transformation (translation + rotation) for the given
	 * object.
	 * 
	 * @param object
	 *            The object to get the global transformation for.
	 * @return A {@link javax.vecmath.Matrix4d} describing the global
	 *         transformation (translation + rotation) of the given object.
	 */
	private static Matrix4d getTransformation(ShadedNull object)
	{
		return GlobalTransformation.get(object, true,
				object.getCurrentGraphState(), false).toMatrix4d();
	}

	public static void transformPlanes(List<Plane> planes,
			Matrix4d transformation)
	{
		for (Plane p : planes)
		{
			Vector3 norm3 = p.Normal();
			Vector3d norm3d = new Vector3d(norm3.X, norm3.Y, norm3.Z);
			transformation.transform(norm3d);
			p.setNormal(new Vector3(norm3d.x, norm3d.y, norm3d.z));
			p.Translate(new Vector3((float) transformation.m03,
					(float) transformation.m13, (float) transformation.m23));
		}
	}

	private static CSGNode toBox(Box object)
	{
		List<Plane> planes = boxPlanes(object);
		transformPlanes(planes, getTransformation(object));
		return new CSGNode("box", planes);
	}

	public static List<Plane> boxPlanes(Box object)
	{
		float length = object.getLength(); // = z-axis
		float halfWidth = object.getWidth() / 2f; // = x-axis
		float halfHeight = object.getHeight() / 2f; // = y-axis

		List<Plane> planes = new ArrayList<Plane>();
		planes.add(new Plane(1., 0., 0., halfWidth));
		planes.add(new Plane(-1., 0., 0., halfWidth));
		planes.add(new Plane(0., 1., 0., halfHeight));
		planes.add(new Plane(0., -1., 0., halfHeight));
		planes.add(new Plane(0., 0., 1., length));
		planes.add(new Plane(0., 0., -1., 0.));

		return planes;
	}

	private static CSGNode toCylinder(Cylinder object)
	{
		List<Plane> planes = cylinderPlanes(object, 10);
		transformPlanes(planes, getTransformation(object));
		return new CSGNode("cylinder", planes);
	}

	public static List<Plane> cylinderPlanes(Cylinder object, int n)
	{
		float length = object.getLength();
		float radius = object.getRadius();

		List<Plane> planes = new ArrayList<Plane>();
		planes.add(new Plane(0f, 0f, 1f, length));
		planes.add(new Plane(0f, 0f, -1f, 0f));

		double x, y;
		for (int i = 0; i < n; ++i)
		{
			x = Math.cos(2 * i * Math.PI / n);
			y = Math.sin(2 * i * Math.PI / n);
			planes.add(new Plane(x, y, 0, radius));
		}

		return planes;
	}

	private static CSGNode toSphere(Sphere object)
	{
		List<Plane> planes = spherePlanes(object, VarsConstants.PlanesSamplingSpheres);
		transformPlanes(planes, getTransformation(object));
		return new CSGNode("sphere", planes);
	}

	public static List<Plane> spherePlanes(Sphere object, int n)
	{
		float radius = object.getRadius();

		List<Plane> planes = new ArrayList<Plane>();
		double phi = 0d;
		double phi_increment = 2d * Math.PI / n;
		double theta_increment = Math.PI / (n - 1);
		for (int i = 0; i < n; ++i, phi += phi_increment)
		{
			double theta = theta_increment;
			for (int j = 1; j < n - 1; ++j, theta += theta_increment)
			{
				double x = Math.sin(theta) * Math.cos(phi);
				double y = Math.sin(theta) * Math.sin(phi);
				double z = Math.cos(theta);
				planes.add(new Plane(x, y, z, radius));
			}
		}

		return planes;
	}

	private static CSGNode toFrustum(Frustum object)
	{
		List<Plane> planes = frustumPlanes(object, 50);
		transformPlanes(planes, getTransformation(object));
		return new CSGNode("frustum", planes);
	}

	public static CSGNode toFrustum(Frustum object, Matrix4d transformation,
			int numPlanes)
	{
		List<Plane> planes = frustumPlanes(object, numPlanes);
		transformPlanes(planes, transformation);
		return new CSGNode("frustum", planes);
	}

	public static List<Plane> frustumPlanes(Frustum object, int n)
	{
		float length = object.getLength();
		float topRadius = object.getTopRadius();
		float baseRadius = object.getBaseRadius();

		Vector3 x_base = new Vector3(baseRadius, 0, 0);
		Vector3 x_top = new Vector3(topRadius, 0, length);
		Vector3 dir = Vector3.Subtract(x_top, x_base);
		Vector3 cross = Vector3.CrossProduct(x_base.Negated(), x_top.Negated());
		double dist = cross.Length() / dir.Length();

		List<Plane> planes = new ArrayList<Plane>();
		planes.add(new Plane(0f, 0f, 1f, length));
		planes.add(new Plane(0f, 0f, -1f, 0f));

		double base_x_1, base_y_1, base_x_2, base_y_2, top_x, top_y;
		for (double i = 0.5; i < n; i += 1)
		{
			base_x_1 = baseRadius * Math.cos(2 * i * Math.PI / n);
			base_y_1 = baseRadius * Math.sin(2 * i * Math.PI / n);
			base_x_2 = baseRadius * Math.cos(2 * ((i + 1) % n) * Math.PI / n);
			base_y_2 = baseRadius * Math.sin(2 * ((i + 1) % n) * Math.PI / n);
			top_x = topRadius * Math.cos(2 * i * Math.PI / n);
			top_y = topRadius * Math.sin(2 * i * Math.PI / n);

			Vector3 a = new Vector3(base_x_1, base_y_1, 0);
			Vector3 b = new Vector3(base_x_2, base_y_2, 0);
			Vector3 c = new Vector3(top_x, top_y, length);
			Vector3 d = Vector3.Subtract(b, a);
			Vector3 e = Vector3.Subtract(c, a);
			Vector3 normal = Vector3.CrossProduct(d, e);
			normal.Normalize();

			planes.add(new Plane(normal, dist));
		}

		return planes;
	}

	private static CSGNode toCone(Cone object)
	{
		List<Plane> planes = conePlanes(object, VarsConstants.PlanesSamplingCones);
		transformPlanes(planes, getTransformation(object));
		return new CSGNode("cone", planes);
	}

	public static List<Plane> conePlanes(Cone object, int n)
	{
		float length = object.getLength();
		float radius = object.getRadius();

		Vector3 x_base = new Vector3(radius, 0, 0);
		Vector3 x_top = new Vector3(0, 0, length);
		Vector3 dir = Vector3.Subtract(x_top, x_base);
		Vector3 cross = Vector3.CrossProduct(x_base.Negated(), x_top.Negated());
		double dist = cross.Length() / dir.Length();

		List<Plane> planes = new ArrayList<Plane>();
		planes.add(new Plane(0f, 0f, -1f, 0f));

		double base_x_1, base_y_1, base_x_2, base_y_2;
		Vector3 c = new Vector3(0, 0, length);
		for (double i = 0.5; i < n; ++i)
		{
			base_x_1 = radius * Math.cos(2 * i * Math.PI / n);
			base_y_1 = radius * Math.sin(2 * i * Math.PI / n);
			base_x_2 = radius * Math.cos(2 * ((i + 1) % n) * Math.PI / n);
			base_y_2 = radius * Math.sin(2 * ((i + 1) % n) * Math.PI / n);

			Vector3 a = new Vector3(base_x_1, base_y_1, 0);
			Vector3 b = new Vector3(base_x_2, base_y_2, 0);
			Vector3 d = Vector3.Subtract(b, a);
			Vector3 e = Vector3.Subtract(c, a);
			Vector3 normal = Vector3.CrossProduct(d, e);
			normal.Normalize();

			planes.add(new Plane(normal, dist));
		}

		return planes;
	}
}
