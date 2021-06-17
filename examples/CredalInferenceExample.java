package examples;

import ch.idsia.crema.core.Strides;
import ch.idsia.crema.factor.GenericFactor;
import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.factor.bayesian.BayesianFactorFactory;
import ch.idsia.crema.factor.convert.HalfspaceToVertex;
import ch.idsia.crema.factor.credal.linear.interval.IntervalFactor;
import ch.idsia.crema.factor.credal.linear.interval.IntervalFactorFactory;
import ch.idsia.crema.factor.credal.linear.separate.SeparateHalfspaceFactor;
import ch.idsia.crema.factor.credal.linear.separate.SeparateHalfspaceFactorFactory;
import ch.idsia.crema.factor.credal.vertex.separate.VertexFactor;
import ch.idsia.crema.factor.credal.vertex.separate.VertexFactorFactory;
import ch.idsia.crema.inference.approxlp.ApproxLP1;
import ch.idsia.crema.inference.approxlp.CredalApproxLP;
import ch.idsia.crema.inference.approxlp2.ApproxLP2;
import ch.idsia.crema.inference.ve.FactorVariableElimination;
import ch.idsia.crema.inference.ve.VariableElimination;
import ch.idsia.crema.model.graphical.DAGModel;
import ch.idsia.crema.model.graphical.MixedModel;
import org.apache.commons.math3.optim.linear.Relationship;

import java.util.Arrays;

public class CredalInferenceExample {
	public static void main(String[] args) throws InterruptedException {

		double p = 0.2;
		double eps = 0.0001;

		/*  CN defined with vertex Factor  */

		// Define the model (with vertex factors)
		final DAGModel<VertexFactor> modelVF = new DAGModel<>();
		int u = modelVF.addVariable(3);
		int x = modelVF.addVariable(2);
		modelVF.addParent(x, u);

		// Define a credal set of the partent node
		VertexFactor fu = VertexFactorFactory.factory().domain(modelVF.getDomain(u), Strides.empty())
				.addVertex(new double[]{0., 1 - p, p})
				.addVertex(new double[]{1 - p, 0., p})
				.get();
		modelVF.setFactor(u, fu);


		System.out.println(p + " " + (1 - p));

		// Define the credal set of the child
		VertexFactor fx = VertexFactorFactory.factory().domain(modelVF.getDomain(x), modelVF.getDomain(u))
				.addVertex(new double[]{1., 0.,}, 0)
				.addVertex(new double[]{1., 0.,}, 1)
				.addVertex(new double[]{0., 1.,}, 2)
				.get();

		modelVF.setFactor(x, fx);

		// Run exact inference inference
		VariableElimination<VertexFactor> ve = new FactorVariableElimination<>(modelVF.getVariables());
		ve.setFactors(modelVF.getFactors());
		System.out.println(ve.run(x));

		// AproxLP will not work

		ApproxLP1<VertexFactor> alp1vf = new ApproxLP1<>();
		double[] upper = (alp1vf.query(modelVF, x)).getUpper();
		System.out.println(Arrays.toString(upper));         // [NaN, NaN]

		ApproxLP2<VertexFactor> alp2vf = new ApproxLP2<>();
		upper = (alp2vf.query(modelVF, x)).getUpper();
		System.out.println(Arrays.toString(upper));         // [NaN, NaN]


		/* Interval Factors  */
		// ApproxlP works but with numerical stability problems

		final MixedModel modelMx = new MixedModel();
		x = modelMx.addVariable(2);
		u = modelMx.addVariable(3);
		modelMx.addParent(x, u);

		BayesianFactor ifx = BayesianFactorFactory.factory().domain(modelMx.getDomain(x, u))
				.data(new double[]{
						1., 0.,
						1., 0.,
						0., 1.,
				})
				.get();
		modelMx.setFactor(x, ifx);

		IntervalFactor ifu = IntervalFactorFactory.factory().domain(modelMx.getDomain(u), modelMx.getDomain())
				.set(new double[]{0, 0, p - eps}, new double[]{1 - p, 1 - p, p})
				.get();
		modelMx.setFactor(u, ifu);

		ApproxLP2<GenericFactor> alp2Mx = new ApproxLP2<>();
		upper = alp2Mx.query(modelMx, x).getUpper();
		System.out.println(Arrays.toString(upper));         // [0.8001, 0.19999999999999996]



		/* CN with factors specified with constraints  */

		DAGModel<SeparateHalfspaceFactor> modelSH = new DAGModel<>();
		x = modelSH.addVariable(2);
		u = modelSH.addVariable(3);
		modelSH.addParent(x, u);


		SeparateHalfspaceFactor cfu = SeparateHalfspaceFactorFactory.factory().domain(modelSH.getDomain(u), Strides.empty())

				.constraint(new double[]{0, 0, 1}, Relationship.EQ, p)
				.constraint(new double[]{1, 1, 0}, Relationship.EQ, 1 - p)

				.constraint(new double[]{1, 1, 1}, Relationship.EQ, 1)

				.constraint(new double[]{1, 0, 0}, Relationship.GEQ, 0)
				.constraint(new double[]{0, 1, 0}, Relationship.GEQ, 0)
				.constraint(new double[]{0, 0, 1}, Relationship.GEQ, 0)
				.get();


		HalfspaceToVertex conversor = new HalfspaceToVertex();
		double[][] vertices = conversor.apply(cfu, 0).getData()[0];

		SeparateHalfspaceFactor cfx = SeparateHalfspaceFactorFactory.factory().domain(modelSH.getDomain(x), modelSH.getDomain(u))

				.constraint(new double[]{1, 0}, Relationship.EQ, 1, 0)
				.constraint(new double[]{1, 0}, Relationship.EQ, 1, 1)
				.constraint(new double[]{0, 1}, Relationship.EQ, 1, 2)

				.constraint(new double[]{1, 0}, Relationship.GEQ, 0, 0)
				.constraint(new double[]{0, 1}, Relationship.GEQ, 0, 0)

				.constraint(new double[]{1, 0}, Relationship.GEQ, 0, 1)
				.constraint(new double[]{0, 1}, Relationship.GEQ, 0, 1)

				.constraint(new double[]{1, 0}, Relationship.GEQ, 0, 2)
				.constraint(new double[]{0, 1}, Relationship.GEQ, 0, 2)


				.constraint(new double[]{1, 1}, Relationship.EQ, 1, 0)
				.constraint(new double[]{1, 1}, Relationship.EQ, 1, 1)
				.constraint(new double[]{1, 1}, Relationship.EQ, 1, 2)
				.get();


		modelSH.setFactor(x, cfx);
		modelSH.setFactor(u, cfu);

		conversor.apply(cfx, 0).getData();

		CredalApproxLP<SeparateHalfspaceFactor> calp1 = new CredalApproxLP<>();

		System.out.println(Arrays.toString(calp1.query(modelSH, x).getUpper()));
		System.out.println(Arrays.toString(calp1.query(modelSH, x).getLower()));

		CredalApproxLP<SeparateHalfspaceFactor> calp2 = new CredalApproxLP<>();

		System.out.println(Arrays.toString((calp2.query(modelSH, x)).getUpper()));
		System.out.println(Arrays.toString((calp2.query(modelSH, x)).getLower()));


	}
}
