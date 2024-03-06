package ch.idsia.crema.inference.sepolyve;

import ch.idsia.crema.factor.algebra.SeparateDefaultAlgebra;
import ch.idsia.crema.factor.credal.vertex.separate.VertexFactor;
import ch.idsia.crema.inference.Inference;
import ch.idsia.crema.model.graphical.GraphicalModel;
import ch.idsia.crema.model.precondition.NetworkReduction;
import ch.idsia.crema.search.impl.DepthFirst;
import ch.idsia.crema.search.impl.Utils;
import it.unimi.dsi.fastutil.ints.Int2IntMap;

import java.util.ArrayList;
import java.util.Map;

/**
 * Precise Separately specified polytree inference algorithm.
 * <p>
 * During the execution the algorithm collects stats about vertices and timings.
 * </p>
 * <p>
 * The algorithm supports HARD limints on memory usage and execution time.
 * Reaching such a limit will trigger a runtime exception.
 * </p>
 *
 * @author david huber
 * @author Alessandro Antonucci
 * @author Jasper De Bock
 */
@NetworkReduction
public class SePolyVE implements Inference<GraphicalModel<VertexFactor>, VertexFactor> {
	public static final String MAX_MEM_BYTE = "maxMem";
	public static final String MAX_TIME_MS = "maxTimeMillis"; // double in seconds

	private SeparateDefaultAlgebra algebra = null;
	private SePolyController collector;

	private long maxTime = Long.MAX_VALUE;
	private long maxMem = Long.MAX_VALUE;

	/**
	 * Run the algorithm round the factors to the specified tolerance.
	 *
	 * @param tolerance the specified tolerance value
	 */
	public SePolyVE(double tolerance) {
		algebra = new SeparateDefaultAlgebra(tolerance);
	}

	/**
	 * Construct the algorithm without rounding
	 */
	public SePolyVE() {
	}

	public ArrayList<Integer> getOrder() {
		return collector.getOrder();
	}

	/**
	 * Initialize the algorithm with limits. Supported keys are:
	 * <ul>
	 * <li>{@link SePolyVE#MAX_MEM_BYTE}: approximate max memory usage in
	 * bytes</li>
	 * <li>{@link SePolyVE#MAX_TIME_MS}: max execution time in milliseconds</li>
	 * </ul>
	 * Values may be expressed as strings or numerics.
	 *
	 * <p>
	 * NOTE: a call to init is not required for the algorithm to function!
	 * </p>
	 *
	 * @param params a map with initialization options (can be NULL)
	 */
	public void init(Map<String, ?> params) {
		if (params != null) {
			if (params.containsKey(MAX_MEM_BYTE)) {
				maxMem = Utils.tryParse(params.get(MAX_MEM_BYTE), maxMem);
			}
			if (params.containsKey(MAX_TIME_MS)) {
				maxTime = Utils.tryParse(params.get(MAX_TIME_MS), maxTime);
			}
		}
	}

	/**
	 * Compute the marginal or posterior probability of query given evidence in the model.
	 *
	 * @param model    the model to use for inference
	 * @param query    the variable that will be queried
	 * @param evidence the observed variable as a map of variable-states
	 * @return the posterior or marginal extensive {@link VertexFactor}
	 * @throws MaxTimeException   - when maximum execution time is reached
	 * @throws MaxMemoryException - when maximum memory usage is reached
	 */
	@Override
	public VertexFactor query(GraphicalModel<VertexFactor> model, Int2IntMap evidence, int query) {
		collector = new SePolyController(model, evidence, algebra, maxTime, maxMem);

		DepthFirst ndf = new DepthFirst(model);
		ndf.setController(collector);
		ndf.visit(query);

		return collector.getPosterior();
	}

	public ArrayList<NodeInfo> getNodeStats() {
		return collector.getNodeStats();
	}
}
