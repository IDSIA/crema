package ch.idsia.crema.preprocess;

import ch.idsia.crema.core.Strides;
import ch.idsia.crema.factor.credal.vertex.VertexFactor;
import ch.idsia.crema.model.graphical.GraphicalModel;
import ch.idsia.crema.utility.IndexIterator;
import ch.idsia.crema.utility.RandomUtil;
import ch.idsia.crema.utility.hull.LPConvexHull;

public class LimitVertices implements TransformerModel<VertexFactor, GraphicalModel<VertexFactor>>,
		PreprocessorModel<VertexFactor, GraphicalModel<VertexFactor>> {

	protected int max = 10;

	/**
	 * @param max set the max value (default = 10).
	 */
	public void setMax(int max) {
		this.max = max;
	}

	/**
	 * @deprecated set the parameter {@link #max} with {@link #setMax(int)} and use method {@link #execute(GraphicalModel)}
	 * or {@link #executeInPlace(GraphicalModel)}
	 */
	@Deprecated
	public GraphicalModel<VertexFactor> apply(GraphicalModel<VertexFactor> model, int max) {
		setMax(max);
		return execute(model);
	}

	@Override
	public void executeInPlace(GraphicalModel<VertexFactor> model) {
		for (int variable : model.getVariables()) {
			VertexFactor factor = reduce(model.getFactor(variable), max);
			System.out.println("factor over " + factor.getDomain() + " has " + factor.getVertices().length + " vertices");
			model.setFactor(variable, factor);
		}
	}

	@Override
	public GraphicalModel<VertexFactor> execute(GraphicalModel<VertexFactor> model) {
		GraphicalModel<VertexFactor> copy = model.copy();
		executeInPlace(copy);
		return copy;
	}

	/**
	 * Probably generates max vertices!
	 *
	 * @param factor
	 * @param max
	 * @return
	 */
	private VertexFactor reduce(VertexFactor factor, int max) {

		// less than max points
		if (factor.getSeparatingDomain().getSize() == 0 && factor.getVertices().length <= max) return factor;

		double[][][] data = factor.getData();
		double[][] hull = new double[0][];

		for (int i = 0; i < max * 10; ++i) {

			//Strides T = EMPTY;
			//Strides Lt = factor.getSeparatingDomain();
			Strides Dl = factor.getDomain();

			// target data

			// lets first iterate over the part of the grouping that stays the same

			IndexIterator src_right_iter = factor.getSeparatingDomain().getIterator(Dl);
			IndexIterator src_left_iter = factor.getDataDomain().getIterator(Dl);

			double[] vertex = new double[Dl.getCombinations()];

			// prepare randoms
			int[] rands = new int[data.length];
			for (int j = 0; j < rands.length; ++j) {
				rands[j] = RandomUtil.getRandom().nextInt(data[j].length);
			}

			for (int j = 0; j < Dl.getCombinations(); ++j) {
				int src_l = src_left_iter.next();
				int src_r = src_right_iter.next();

				vertex[j] = data[src_r][rands[src_r]][src_l];
			}

			hull = LPConvexHull.add(hull, vertex);
			if (hull.length == max) break;
		}

		return new VertexFactor(factor.getDomain(), Strides.EMPTY, new double[][][]{hull});
	}
}
