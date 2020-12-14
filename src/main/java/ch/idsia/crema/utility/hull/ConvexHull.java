package ch.idsia.crema.utility.hull;

import com.github.quickhull3d.Point3d;
import com.github.quickhull3d.QuickHull3D;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.geometry.euclidean.twod.hull.ConvexHull2D;
import org.apache.commons.math3.geometry.euclidean.twod.hull.MonotoneChain;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

public class ConvexHull {

	/**
	 * @param vertices must contain at least one vertex
	 * @param simplex
	 * @return
	 * @throws ArrayIndexOutOfBoundsException if 0 vertices are provided
	 */
	public static double[][] compute(double[][] vertices, boolean simplex) {
		final int dims = vertices[0].length - (simplex ? 1 : 0);

		double[][] result;
		switch (dims) {
			case 1:
				result = convex1d(vertices);
				break;
			case 2:
				result = convex2d(vertices);
				break;
			case 3:
				result = convex3d(vertices);
				break;
			default:
				throw new UnsupportedOperationException();
		}
		if (simplex) {
			return Arrays.stream(result).map(x -> {
				double[] xx = Arrays.copyOf(x, dims + 1);
				xx[dims] = 1 - Arrays.stream(x).sum();
				return xx;
			}).toArray(double[][]::new);
		} else {
			return result;
		}
	}

	public static double[][] convex2d(double[][] data) {
		Collection<Vector2D> vertices = Arrays.stream(data)
				.map(d -> new Vector2D(new double[]{d[0], d[1]}))
				.collect(Collectors.toList());
		ConvexHull2D c2 = new MonotoneChain(false).generate(vertices);
		return Arrays.stream(c2.getVertices()).map(Vector2D::toArray).toArray(double[][]::new);
	}

	public static double[][] convex1d(double[][] data) {
		double[][] newdata = new double[2][1];
		newdata[0][0] = Double.POSITIVE_INFINITY;
		newdata[1][0] = Double.NEGATIVE_INFINITY;

		for (double[] v : data) {
			newdata[0][0] = Math.min(newdata[0][0], v[0]);
			newdata[1][0] = Math.max(newdata[1][0], v[0]);
		}
		return newdata;
	}


	public static double[][] convex3d(double[][] data) {
		Point3d[] points = Arrays.stream(data)
				.map(x -> new Point3d(x[0], x[1], x[2]))
				.toArray(Point3d[]::new);

		QuickHull3D hull = new QuickHull3D();
		hull.build(points);

		Point3d[] vertices = hull.getVertices();
		return Arrays.stream(vertices)
				.map(p -> new double[]{p.x, p.y, p.z})
				.toArray(double[][]::new);
	}

}
