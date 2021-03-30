package ch.idsia.crema.alessandro;

import ch.idsia.crema.core.Strides;
import ch.idsia.crema.factor.GenericFactor;
import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.factor.convert.VertexToInterval;
import ch.idsia.crema.factor.credal.linear.IntervalFactor;
import ch.idsia.crema.factor.credal.vertex.VertexFactor;
import ch.idsia.crema.factor.credal.vertex.generator.CNGenerator;
import ch.idsia.crema.inference.approxlp.ApproxLP1;
import ch.idsia.crema.inference.sepolyve.MaxMemoryException;
import ch.idsia.crema.inference.sepolyve.MaxTimeException;
import ch.idsia.crema.inference.sepolyve.NodeInfo;
import ch.idsia.crema.inference.sepolyve.SePolyVE;
import ch.idsia.crema.model.graphical.DAGModel;
import ch.idsia.crema.model.graphical.GraphicalModel;
import ch.idsia.crema.model.io.bif.XMLBIFParser;
import ch.idsia.crema.model.io.dot.DotSerialize;
import ch.idsia.crema.preprocess.BinarizeEvidence;
import ch.idsia.crema.preprocess.LimitVertices;
import ch.idsia.crema.preprocess.RemoveBarren;
import ch.idsia.crema.search.ISearch;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.stream.Collectors;

public class RandomNetworks {
	private static final String FIELD_SEP = ",";
	private static final String INNER_SEP = ";";

	final static long max_time = 3600000; // ms
	final static long max_mem = 2000000000; // something like 1GB ;-)

	static final String path = "src/test/resources/polytrees/";
	static final String prefix = "Benchmark-long";

	private static void serialize(DAGModel<VertexFactor> vmodel, String filename) {
		try (FileWriter output = new FileWriter(filename)) {
			for (int var : vmodel.getVariables()) {
				// serialize var model
				VertexFactor factor = vmodel.getFactor(var);
				for (int conditioning_index = 0; conditioning_index < factor.getSeparatingDomain()
						.getCombinations(); ++conditioning_index) {
					double[][] vertices = factor.getVerticesAt(conditioning_index);
					for (double[] vertex : vertices) {
						output.write("" + var);
						output.write(FIELD_SEP);
						output.write("" + conditioning_index);
						output.write(FIELD_SEP);
						output.write(Arrays.stream(vertex).mapToObj(Double::toString).collect(Collectors.joining(FIELD_SEP)));
						output.write("\n");
					}
				}
			}
		} catch (IOException ignored) {

		}
	}

	private static String intervalToString(IntervalFactor interval) {
		return Arrays.stream(interval.getLower())
				.mapToObj(Double::toString)
				.collect(Collectors.joining(INNER_SEP)) +
				FIELD_SEP +
				Arrays.stream(interval.getUpper()).mapToObj(Double::toString)
						.collect(Collectors.joining(INNER_SEP));
	}

	private static long processSePolyVE(String modelname, DAGModel<VertexFactor> vmodel2, int query,
	                                    TIntIntMap evidence, Writer ve_output, Writer summary_output, Writer stats_output) throws IOException {
		SePolyVE ve = new SePolyVE();
		ve.init(new HashMap<String, Long>() {
			{
				put(SePolyVE.MAX_TIME_MS, max_time);
				put(SePolyVE.MAX_MEM_BYTE, max_mem);
			}
		});

		long start_time = System.currentTimeMillis();
		String error = "";
		VertexFactor marg = null;
		try {
			marg = ve.query(vmodel2, evidence, query);
			marg = marg.normalize();
		} catch (MaxTimeException mte) {
			error = "-1";
		} catch (MaxMemoryException mme) {
			error = "-2";
			System.out.println(query + ": " + new DotSerialize().run(vmodel2));

		} catch (Throwable stuff) {
			error = stuff.toString();
		}
		long duration = System.currentTimeMillis() - start_time;

		StringBuilder preamb = new StringBuilder();
		preamb.append(modelname);
		preamb.append(FIELD_SEP);
		preamb.append(query);
		preamb.append(FIELD_SEP);

		String evidence_string = evidenceToString(evidence);
		if (evidence_string != null) {
			preamb.append(evidence_string);
		}

		preamb.append(FIELD_SEP);
		preamb.append("SePolyVE");
		preamb.append(FIELD_SEP);

		printStats(ve, preamb.toString(), duration, stats_output);
		summary_output.append(preamb.toString());

		if (marg != null) {
			IntervalFactor ifac = new VertexToInterval().apply(marg);
			summary_output.append(intervalToString(ifac));
			summary_output.append("\n");

			for (double[] vertex : marg.getVertices()) {
				ve_output.append(preamb.toString());
				ve_output.append(Arrays.stream(vertex).mapToObj(Double::toString).collect(Collectors.joining(INNER_SEP)));
				ve_output.append("\n");
			}
		} else {
			ve_output.append(preamb.toString());
			ve_output.append("ERROR ").append(error);
			ve_output.append("\n");

			summary_output.append("Error ").append(error);
			summary_output.append("\n");
		}
		ve_output.flush();

		return duration;
	}

	private static String evidenceToString(TIntIntMap evidence) {
		if (evidence != null) {
			int[] keys = evidence.keys();
			Arrays.sort(keys);
			return Arrays.stream(keys).mapToObj(x -> x + "=" + evidence.get(x)).collect(Collectors.joining(INNER_SEP));
		}
		return "";
	}

	/**
	 * print VE STATS to file
	 *
	 * @param sepolyve
	 * @param preamble
	 * @param duration
	 * @param output
	 * @throws IOException
	 */
	private static void printStats(SePolyVE sepolyve, String preamble, double duration, Writer output) throws IOException {
		output.append(preamble);
		output.append(String.valueOf(duration));
		output.append(FIELD_SEP);

		String vin = sepolyve.getNodeStats().stream().sorted(Comparator.comparingInt(NodeInfo::getNode)).map(x -> x.getNode() + "=" + x.getVerticesIn()).collect(Collectors.joining(INNER_SEP));
		output.append(vin);
		output.append(FIELD_SEP);

		String vout = sepolyve.getNodeStats().stream().sorted(Comparator.comparingInt(NodeInfo::getNode)).map(x -> x.getNode() + "=" + x.getVerticesOut()).collect(Collectors.joining(INNER_SEP));
		output.append(vout);
		output.append(FIELD_SEP);

		String times = sepolyve.getNodeStats().stream().sorted(Comparator.comparingInt(NodeInfo::getNode)).map(x -> x.getNode() + "=" + x.getTime()).collect(Collectors.joining(INNER_SEP));
		output.append(times);
		output.append(FIELD_SEP);

		String order = sepolyve.getOrder().stream().map(Object::toString).collect(Collectors.joining(INNER_SEP));
		output.append(order);
		output.append("\n");
		output.flush();
	}

	private static void processApproxLP(String modelname, DAGModel<VertexFactor> vmodel2, int query, TIntIntMap evidence, long duration, FileWriter summary_output) throws IOException {
		String error = "";
		double state_time = duration / (vmodel2.getSize(query) * 2.0 * 1000.0 /* in seconds */);

		DAGModel<GenericFactor> imodel = vmodel2.convert(new VertexToInterval()).convert((x, v) -> x);

		ApproxLP1<GenericFactor> approxlp = new ApproxLP1<>();
		approxlp.initialize(new HashMap<>() {
			{
				put(ISearch.MAX_TIME, state_time);
			}
		});

		IntervalFactor interval = null;

		try {
			if (evidence != null) {
				BinarizeEvidence<GenericFactor> bin = new BinarizeEvidence<>();
				bin.setSize(2);

				GraphicalModel<GenericFactor> gmodel = bin.execute(imodel, evidence);
				approxlp.setEvidenceNode(bin.getEvidenceNode());
				interval = approxlp.query(gmodel, query);
			} else {
				interval = approxlp.query(imodel, query);
			}
		} catch (Throwable e) {
			error = e.toString();

		}

		// write
		summary_output.append(modelname);
		summary_output.append(FIELD_SEP);
		summary_output.append(String.valueOf(query));
		summary_output.append(FIELD_SEP);

		String evidence_string = evidenceToString(evidence);
		if (evidence_string != null) summary_output.append(evidence_string);

		summary_output.append(FIELD_SEP);
		summary_output.append("ApproxLP");
		summary_output.append(FIELD_SEP);
		if (interval != null) {
			summary_output.append(intervalToString(interval));

		} else {
			summary_output.append(error);
		}
		summary_output.append("\n");
		summary_output.flush();
	}

	public static void processLeaf(String modelname, DAGModel<VertexFactor> vmodel, int query,
	                               FileWriter ve_output, FileWriter summary_output, FileWriter stats_output) throws IOException {
		System.out.println("K_VE(" + query + ")");
		// remove disconnected stuff
		final RemoveBarren<VertexFactor> removeBarren = new RemoveBarren<>();
		DAGModel<VertexFactor> vmodel2 = (DAGModel<VertexFactor>) removeBarren.execute(vmodel, new TIntIntHashMap() , query);

		// run polyve
		long duration = processSePolyVE(modelname, vmodel2, query, null, ve_output, summary_output, stats_output);

		///////////////////////////////////////////////// APPROXLP
		System.out.println("K_LP(" + query + ")");
		processApproxLP(modelname, vmodel2, query, null, duration, summary_output);
	}

	private static void processRoot(String modelname, DAGModel<VertexFactor> vmodel, int query,
	                                FileWriter inference_output, FileWriter summary_output, FileWriter stats_output) throws IOException {
		TIntIntMap evidence = new TIntIntHashMap();
		for (int var : vmodel.getVariables()) {
			if (vmodel.getChildren(var).length == 0) {
				evidence.put(var, 0);
			}
		}

		RemoveBarren<VertexFactor> rb = new RemoveBarren<>();
		DAGModel<VertexFactor> vmodel2 = (DAGModel<VertexFactor>) rb.execute(vmodel, evidence, query);
		rb.filter(evidence); // remove barren from evidence (not really needed)
		System.out.println("K_VE_R(" + query + ")");

		long duration = processSePolyVE(modelname, vmodel2, query, evidence, inference_output, summary_output, stats_output);

		System.out.println("K_LP_R(" + query + ")");
		processApproxLP(modelname, vmodel2, query, evidence, duration, summary_output);
	}

	public static VertexFactor converti(BayesianFactor bf, int variable) {
		CNGenerator generator = new CNGenerator();

		Strides conditioning = bf.getDomain().remove(variable);

		Strides left = bf.getDomain().remove(conditioning);
		int left_size = left.getCombinations();

		double[][][] data = new double[conditioning.getCombinations()][][];
		for (int cond = 0; cond < data.length; ++cond) {
			// generate random CredalSets over left given conditioning
			// and add them to the vertices list
			data[cond] = generator.linvac(left_size, 0.1);
		}
		return new VertexFactor(left, conditioning, data);
	}

	/*
	 * TODO: see below for other todos
	 * TODO: measure SePolyVE Execution time (with max time set)
	 * TODO: run ApproxLP with max time = SePolyVE_time / (nstates * 2)
	 * TODO: eventualmente run ApproxLP con un min time giusto per ottenere qualcosa quando seployve era super veloce
	 * TODO ev salvare in CSV i vertici dei credal set usati nella benchamrk
	 */

	/**
	 * tipo: node name;conditioning index;vertex data(values separated by
	 * different char)?
	 * <p>
	 * Inference Output CSV network name;node number;evidence list (formato
	 * var=state,var=state);vertex data
	 * <p>
	 * Stats output csv network name;node number;evidence list (formato
	 * var=state,var=state);total time (-1 if max time reached, -2 if max mem
	 * reached);local vertex count in (var=count);local vertex count out
	 * (var=count)
	 *
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		try (FileWriter inference_output = new FileWriter(path + prefix + "-inference.csv");
		     FileWriter summary_output = new FileWriter(path + prefix + "-overview.csv");
		     FileWriter stats_output = new FileWriter(path + prefix + "-stats.csv")) {

			// Read the XML network
			XMLBIFParser parse = new XMLBIFParser();
			int sample = 1;
			//	for (sample = 1; sample < 10; sample++) {

			// load model
			String filename = prefix + sample + ".xml";
			DAGModel<BayesianFactor> model = (DAGModel<BayesianFactor>) parse.parse(new FileInputStream(path + filename));

			// Convert the XML network into a credal model
			DAGModel<VertexFactor> vmodel = model.convert(RandomNetworks::converti);
			serialize(vmodel, path + prefix + sample + "-model.csv");

			final LimitVertices limitVertices = new LimitVertices();
			limitVertices.setMax(10);
			vmodel = (DAGModel<VertexFactor>) limitVertices.execute(vmodel);
			System.out.println(new DotSerialize().run(vmodel));


			processLeaf(filename, vmodel, 40, inference_output, summary_output, stats_output);
			//processRoot(filename, vmodel, 61, inference_output, summary_output, stats_output);

//				// query leafs without evidence
//				for (int query : vmodel.getVariables()) {
//					if (vmodel.getChildren(query).length == 0) {
//						// leaf nodes have no children
//						System.out.println("PRocessing a leaf :" + query);
//						processLeaf(filename, vmodel, query, inference_output, summary_output, stats_output);
//					} else if (vmodel.getParents(query).length == 0) {
//						// root node
//						System.out.println("PRocessing a root :" + query);
//						processRoot(filename, vmodel, query, inference_output, summary_output, stats_output);
//					}
//				}
			//	}
		}
	}
}
