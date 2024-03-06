package ch.idsia.crema.inference.sepolyve;

import ch.idsia.crema.core.Strides;
import ch.idsia.crema.factor.algebra.SeparateDefaultAlgebra;
import ch.idsia.crema.factor.bayesian.BayesianDefaultFactor;
import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.factor.credal.vertex.separate.VertexDefaultFactor;
import ch.idsia.crema.factor.credal.vertex.separate.VertexFactor;
import ch.idsia.crema.model.graphical.GraphicalModel;
import ch.idsia.crema.search.SearchOperation;
import ch.idsia.crema.utility.ArraysUtil;
import ch.idsia.crema.utility.CombinationsIterator;
import ch.idsia.crema.utility.hull.LPConvexHull;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import java.util.*;

public class SePolyController extends SearchOperation {

	private final Int2ObjectMap<List<VertexFactor>> queue;
	private final Int2IntMap evidence;
	private final GraphicalModel<VertexFactor> model;
	private SeparateDefaultAlgebra algebra = null;
	private final ArrayList<NodeInfo> nodeStats;
	private final ArrayList<Integer> order;

	private long maxSize = Long.MAX_VALUE;
	private long maxTime = Long.MAX_VALUE;

	/**
	 * Search controller with possibility to specify the rounding algebra.
	 * As we are running in online mode, we do not use the other operations of the
	 * algebra!
	 *
	 * @param model    the model used for inference
	 * @param evidence the observed variable as a map of variable-states
	 * @param rounding
	 * @param maxTime
	 * @param maxMem
	 */
	public SePolyController(GraphicalModel<VertexFactor> model, Int2IntMap evidence, SeparateDefaultAlgebra rounding, long maxTime, long maxMem) {
		this(model, evidence);
		this.algebra = rounding;
		this.maxSize = maxMem;
		this.maxTime = maxTime;
	}

	public SePolyController(GraphicalModel<VertexFactor> model, Int2IntMap evidence) {
		this.evidence = evidence;
		this.model = model;

		this.queue = new Int2ObjectOpenHashMap<>();
		this.queue.put(-1, new ArrayList<VertexFactor>());

		this.nodeStats = new ArrayList<>();
		this.order = new ArrayList<>();
	}

	public ArrayList<Integer> getOrder() {
		return order;
	}

	public ArrayList<NodeInfo> getNodeStats() {
		return nodeStats;
	}

	@Override
	public void opening(int node, int from) {
		queue.put(node, new ArrayList<VertexFactor>());
	}

	@Override
	public void closing(int node, int from) {
		NodeInfo info = new NodeInfo();
		info.setNode(node);
		nodeStats.add(info);
		order.add(node);

		VertexFactor vf = model.getFactor(node);

		// estimate memory usage
		long size = 1;
		for (double[][] vals : vf.getData()) {
			size *= vals.length;
		}
		size *= vf.getSeparatingDomain().getCombinations() * vf.getDataDomain().getCombinations() * 8L /* bytes per double */;

		if (size > maxSize) {
			info.setTime(-2);
			throw new MaxMemoryException(node, size);
		}

		// TODO this will explode if we have many parents
		// we should iterate over the vertices
		vf = vf.reseparate(Strides.empty());

		// filter-out evidence
		if (evidence != null && evidence.containsKey(node)) {
			// observed nodes are to be filtered
			vf = vf.filter(node, evidence.get(node));
		}

		ArrayList<Collection<double[]>> vertices = new ArrayList<>();
		vertices.add(Arrays.asList(vf.getVertices()));

		Strides domain = vf.getDomain();
		Strides first = domain;

		List<VertexFactor> factors = queue.get(node);
		List<Strides> factorDomains = new ArrayList<>(factors.size());

		for (VertexFactor factor : factors) { // get the unified domain
			// will set the data later
			factorDomains.add(factor.getDomain());

			domain = domain.union(factor.getDomain());

			// if there is an algebra use it to round
			if (algebra != null) {
				factor = algebra.round(factor);
			}
			vertices.add(Arrays.asList(factor.getVertices()));
		}

		// we're done with the node's queue (free)
		queue.put(node, null);

		// we can marginalize when we are the bottom node in a v-structure
		// as we locally explode, we do not need to distinguish between the 
		// observed v-strcture

		int[] tomarginalize;
		if (from >= 0) { // not closing the query
			int[] p = model.getParents(node);
			if (Arrays.binarySearch(p, from) >= 0) { // node is child of from
				tomarginalize = ArraysUtil.removeFromSortedArray(domain.getVariables(), from);
			} else { // node is a parent of from

				// No additional steps before we push the factor in from's queue
				tomarginalize = ArraysUtil.removeFromSortedArray(domain.getVariables(), from);
				tomarginalize = ArraysUtil.removeFromSortedArray(tomarginalize, node);
			}
		} else { // node query
			tomarginalize = ArraysUtil.removeFromSortedArray(domain.getVariables(), node);
		}

		// inline convex hull
		CombinationsIterator<double[]> iterator = new CombinationsIterator<>(vertices);
		double[][] chull = new double[0][];

		// marginalize domain
		domain = domain.remove(tomarginalize);
		long time = System.currentTimeMillis();
		long count = 0;

		while (iterator.hasNext()) {
			Collection<double[]> config = iterator.next();
			Iterator<double[]> data = config.iterator();

			BayesianFactor current = new BayesianDefaultFactor(first, data.next());
			for (Strides otherDomain : factorDomains) {
				BayesianFactor other = new BayesianDefaultFactor(otherDomain, data.next());
				current = current.combine(other);
			}

			// marginalize
			if (tomarginalize.length > 0) {
				for (int marg : tomarginalize) {
					current = current.marginalize(marg);
				}
			}
			++count;

			chull = LPConvexHull.add(chull, current.getData());

			// check whether the maximum time has been reached
			long elapsed = System.currentTimeMillis() - time;
			if (elapsed > maxTime) {
				info.setVerticesIn(count);
				info.setTime(-1);
				throw new MaxTimeException(node, elapsed);
			}
		}
		info.setVerticesIn(count);
		info.setVerticesOut(chull.length);
		info.setTime(System.currentTimeMillis() - time);

		// if we can marginalize, we are in the situation that we will end up
		// with a single factor in the domain and we will be allowed to
		// convexify as well

		vf = new VertexDefaultFactor(domain, Strides.empty(), new double[][][]{chull});

		// push the factor in from's queue
		queue.get(from).add(vf);
	}

	public VertexFactor getPosterior() {
		return queue.get(-1).get(0);
	}
}
