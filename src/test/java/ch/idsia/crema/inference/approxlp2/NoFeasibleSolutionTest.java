package ch.idsia.crema.inference.approxlp2;

import ch.idsia.crema.core.Strides;
import ch.idsia.crema.factor.credal.linear.IntervalFactor;
import ch.idsia.crema.model.graphical.DAGModel;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import org.junit.Ignore;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    02.03.2021 14:30
 */
public class NoFeasibleSolutionTest {

	static class Credal4x4x4 {

		DAGModel<IntervalFactor> model;

		public Credal4x4x4(int nQuestions) {
			model = new DAGModel<>();

			// skill-chain
			// S0 -> S1 -> S2 -> S3
			//  v     v     v     v
			// Q0    Q1    Q2    Q3
			int S0 = addSkillNode(model);
			int S1 = addSkillNode(model, S0);
			int S2 = addSkillNode(model, S1);
			int S3 = addSkillNode(model, S2);

			// for each skill...
			for (int s = S0; s <= S3; s++) {
				// ...add question nodes
				for (int i = 0; i < nQuestions; i++) {
					addQuestionNodeEasy(model, s);
					addQuestionNodeMediumEasy(model, s);
					addQuestionNodeMediumHard(model, s);
					addQuestionNodeHard(model, s);
				}
			}
		}

		int addSkillNode(DAGModel<IntervalFactor> model) {
			int s = model.addVariable(4);
			final IntervalFactor fS = new IntervalFactor(model.getDomain(s), Strides.EMPTY);
			fS.setLower(new double[]{
					.1, .3, .3, .1
			});
			fS.setUpper(new double[]{
					.2, .4, .4, .2
			});

			model.setFactor(s, fS);
			return s;
		}

		/**
		 * Add a skill node a single parent.
		 *
		 * @param model  add to this model
		 * @param parent parent node
		 * @return the new variable added
		 */
		int addSkillNode(DAGModel<IntervalFactor> model, int parent) {
			int s = model.addVariable(4);
			model.addParent(s, parent);

			final IntervalFactor fS = new IntervalFactor(model.getDomain(s), model.getDomain(parent));
			fS.setLower(new double[]{.30, .20, .10, .01}, 0); // lP(S1|S0=0)
			fS.setLower(new double[]{.20, .30, .20, .10}, 1); // lP(S1|S0=1)
			fS.setLower(new double[]{.10, .20, .30, .20}, 2); // lP(S1|S0=2)
			fS.setLower(new double[]{.01, .10, .20, .30}, 3); // lP(S1|S0=3)

			fS.setUpper(new double[]{.40, .30, .30, .20}, 0);   // uP(S1|S0=0)
			fS.setUpper(new double[]{.30, .40, .30, .20}, 1);   // uP(S1|S0=1)
			fS.setUpper(new double[]{.20, .30, .40, .30}, 2);   // uP(S1|S0=2)
			fS.setUpper(new double[]{.20, .30, .30, .40}, 3);   // uP(S1|S0=3)

			model.setFactor(s, fS);
			return s;
		}

		public void addQuestionNodeEasy(DAGModel<IntervalFactor> model, int parent) {
			final int q = model.addVariable(2);
			model.addParent(q, parent);
			final IntervalFactor fQ = new IntervalFactor(model.getDomain(q), model.getDomain(parent));

			fQ.setLower(new double[]{.600, .375}, 0); // lP(Q=right|S=0)
			fQ.setLower(new double[]{.750, .225}, 1); // lP(Q=right|S=1)
			fQ.setLower(new double[]{.850, .125}, 2); // lP(Q=right|S=2)
			fQ.setLower(new double[]{.950, .025}, 3); // lP(Q=right|S=3)

			fQ.setUpper(new double[]{.625, .400}, 0); // uP(Q=right|S=0)
			fQ.setUpper(new double[]{.775, .250}, 1); // uP(Q=right|S=1)
			fQ.setUpper(new double[]{.875, .150}, 2); // uP(Q=right|S=2)
			fQ.setUpper(new double[]{.975, .050}, 3); // uP(Q=right|S=3)

			model.setFactor(q, fQ);
		}

		public void addQuestionNodeMediumEasy(DAGModel<IntervalFactor> model, int parent) {
			final int q = model.addVariable(2);
			model.addParent(q, parent);
			final IntervalFactor fQ = new IntervalFactor(model.getDomain(q), model.getDomain(parent));

			fQ.setLower(new double[]{.325, .650}, 0);// lP(Q=right|S=0)
			fQ.setLower(new double[]{.600, .375}, 1);// lP(Q=right|S=1)
			fQ.setLower(new double[]{.750, .225}, 2);// lP(Q=right|S=2)
			fQ.setLower(new double[]{.850, .125}, 3);// lP(Q=right|S=3)

			fQ.setUpper(new double[]{.350, .675}, 0); // uP(Q=right|S=0)
			fQ.setUpper(new double[]{.625, .400}, 1); // uP(Q=right|S=1)
			fQ.setUpper(new double[]{.775, .250}, 2); // uP(Q=right|S=2)
			fQ.setUpper(new double[]{.875, .150}, 3); // uP(Q=right|S=3)

			model.setFactor(q, fQ);
		}

		public void addQuestionNodeMediumHard(DAGModel<IntervalFactor> model, int parent) {
			final int q = model.addVariable(2);
			model.addParent(q, parent);
			final IntervalFactor fQ = new IntervalFactor(model.getDomain(q), model.getDomain(parent));

			fQ.setLower(new double[]{.225, .750}, 0); // lP(Q=right|S=0)
			fQ.setLower(new double[]{.325, .650}, 1); // lP(Q=right|S=1)
			fQ.setLower(new double[]{.600, .375}, 2); // lP(Q=right|S=2)
			fQ.setLower(new double[]{.750, .225}, 3); // lP(Q=right|S=3)

			fQ.setUpper(new double[]{.250, .775}, 0); // uP(Q=right|S=0)
			fQ.setUpper(new double[]{.350, .675}, 1); // uP(Q=right|S=1)
			fQ.setUpper(new double[]{.625, .400}, 2); // uP(Q=right|S=2)
			fQ.setUpper(new double[]{.775, .250}, 3); // uP(Q=right|S=3)

			model.setFactor(q, fQ);
		}

		public void addQuestionNodeHard(DAGModel<IntervalFactor> model, int parent) {
			final int q = model.addVariable(2);
			model.addParent(q, parent);
			final IntervalFactor fQ = new IntervalFactor(model.getDomain(q), model.getDomain(parent));

			fQ.setLower(new double[]{.175, .800}, 0); // lP(Q=right|S=0)
			fQ.setLower(new double[]{.225, .750}, 1); // lP(Q=right|S=1)
			fQ.setLower(new double[]{.325, .650}, 2); // lP(Q=right|S=2)
			fQ.setLower(new double[]{.600, .375}, 3); // lP(Q=right|S=3)

			fQ.setUpper(new double[]{.200, .825}, 0); // uP(Q=right|S=0)
			fQ.setUpper(new double[]{.250, .775}, 1); // uP(Q=right|S=1)
			fQ.setUpper(new double[]{.350, .675}, 2); // uP(Q=right|S=2)
			fQ.setUpper(new double[]{.625, .400}, 3); // uP(Q=right|S=3)

			model.setFactor(q, fQ);
		}

	}

	static class Params {
		TIntIntMap obs = new TIntIntHashMap();
		int question;
		int skill;
		int state;

		@Override
		public String toString() {
			return "question=" + question +
					" skill=" + skill +
					" state=" + state +
					" obs=" + obs;
		}
	}

	@Ignore
	@Test
	public void testingNoFeasibleSolution() throws Exception {
		final ApproxLP2 approx = new ApproxLP2();
		final Credal4x4x4 builder = new Credal4x4x4(5);
		final DAGModel<IntervalFactor> model = builder.model;

		final Pattern pattern = Pattern.compile("([a-z0-9]+=\\d+\\s*)");

		final List<Params> params = Files.readAllLines(Paths.get("NoFeasibleSolutions.txt")).stream()
				.filter(line -> line.startsWith("No Feasible Solution"))
				.limit(1)
				.map(line -> line.substring(line.indexOf(": ") + 2))
				.map(line -> {
					// question=8 skill=0 state=1 obs={30=1, 29=1, 28=0, 27=1, 26=0, 25=0, 24=0, 9=1, 8=1, 7=1, 6=0, 5=1, 4=1, 31=0}
					Params p = new Params();

					final Matcher matcher = pattern.matcher(line);
					int i = 0;
					while (matcher.find()) {
						final String group = matcher.group(i).trim();
						final String[] tokens = group.split("=");
						switch (tokens[0]) {
							case "question":
								p.question = Integer.parseInt(tokens[1]);
								break;
							case "skill":
								p.skill = Integer.parseInt(tokens[1]);
								break;
							case "state":
								p.state = Integer.parseInt(tokens[1]);
								break;
							default:
								p.obs.put(Integer.parseInt(tokens[0]), Integer.parseInt(tokens[1]));
						}
					}

					return p;
				})
				.collect(Collectors.toList());


		final Params p = params.get(0);
		System.out.println("PARAMS: " + p);

		for (int i = 0; i < 10; i++) {
			try {
				final IntervalFactor ignored = approx.query(model, p.skill, p.obs);
			} catch (Exception ignored) {
				System.err.println();
			}
		}
	}
}
