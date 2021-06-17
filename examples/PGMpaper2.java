package examples;

import ch.idsia.crema.IO;
import ch.idsia.crema.core.ObservationBuilder;
import ch.idsia.crema.core.Strides;
import ch.idsia.crema.factor.credal.linear.interval.IntervalFactor;
import ch.idsia.crema.factor.credal.linear.separate.SeparateHalfspaceFactor;
import ch.idsia.crema.factor.credal.linear.separate.SeparateHalfspaceFactorFactory;
import ch.idsia.crema.inference.approxlp.CredalApproxLP;
import ch.idsia.crema.model.graphical.DAGModel;
import org.apache.commons.math3.optim.linear.Relationship;

import java.io.IOException;


public class PGMpaper2 {
	public static void main(String[] args) throws InterruptedException, IOException {

		// define the structure
		DAGModel<SeparateHalfspaceFactor> cnet = new DAGModel<>();
		int a = cnet.addVariable(2);
		int b = cnet.addVariable(3);
		cnet.addParent(a, b);

		// add credal set K(B)
		SeparateHalfspaceFactor fb = SeparateHalfspaceFactorFactory.factory().domain(cnet.getDomain(b), Strides.empty())
				.constraint(new double[]{1, 0, 0}, Relationship.GEQ, 0.2)
				.constraint(new double[]{1, 0, 0}, Relationship.LEQ, 0.3)
				.constraint(new double[]{0, 1, 0}, Relationship.GEQ, 0.4)
				.constraint(new double[]{0, 1, 0}, Relationship.LEQ, 0.5)
				.constraint(new double[]{0, 0, 1}, Relationship.GEQ, 0.2)
				.constraint(new double[]{0, 0, 1}, Relationship.LEQ, 0.3)
				.get();


		cnet.setFactor(b, fb);

		// add credal set K(A|B)
		SeparateHalfspaceFactor fa = SeparateHalfspaceFactorFactory.factory().domain(cnet.getDomain(a), cnet.getDomain(b))
				.constraint(new double[]{1, 0}, Relationship.GEQ, 0.5, 0)
				.constraint(new double[]{1, 0}, Relationship.LEQ, 0.6, 0)
				.constraint(new double[]{0, 1}, Relationship.GEQ, 0.4, 0)
				.constraint(new double[]{0, 1}, Relationship.LEQ, 0.5, 0)
				.constraint(new double[]{1, 0}, Relationship.GEQ, 0.3, 1)
				.constraint(new double[]{1, 0}, Relationship.LEQ, 0.4, 1)
				.constraint(new double[]{0, 1}, Relationship.GEQ, 0.6, 1)
				.constraint(new double[]{0, 1}, Relationship.LEQ, 0.7, 1)
				.constraint(new double[]{1, 0}, Relationship.GEQ, 0.1, 2)
				.constraint(new double[]{1, 0}, Relationship.LEQ, 0.2, 2)
				.constraint(new double[]{0, 1}, Relationship.GEQ, 0.8, 2)
				.constraint(new double[]{0, 1}, Relationship.LEQ, 0.9, 2)
				.get();


		cnet.setFactor(a, fa);


		IO.write(cnet, "./models/pgm-hcredal.uai");

		// set up the inference and run the queries
		CredalApproxLP<SeparateHalfspaceFactor> inf = new CredalApproxLP<>();
		IntervalFactor res1 = inf.query(cnet, ObservationBuilder.observe(a, 0), b);
		IntervalFactor res2 = inf.query(cnet, b);

		System.out.println(res1);
		System.out.println(res2);


	}
}
//68
