package ch.idsia.crema.model.io.bif;

import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.factor.bayesian.BayesianFactorFactory;
import org.apache.commons.lang3.NotImplementedException;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Scanner;
import java.util.stream.IntStream;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    25.01.2021 09:22
 * <p>
 * Source of the BIF structure: http://www.cs.washington.edu/dm/vfml/appendixes/bif.htm
 */
public class BIFParser {

	private static final String DEFAULT_DELIMITER = "[\\r\\n]+|//.*$";

	public static BIFObject read(String filename) throws IOException {
		if (!(filename.endsWith(".bif"))) {
			throw new IllegalArgumentException("Unknown file extension");
		}

		return new BIFParser(filename)
				.scan()
				.getResult();
	}

	String filename;
	BIFObject result;

	public BIFParser(String filename) {
		this.filename = filename;
	}

	public BIFObject getResult() {
		return result;
	}

	public BIFParser scan() throws IOException {
		result = new BIFObject();

		try (Scanner scanner = new Scanner(Paths.get(filename))) {
			scanner.useDelimiter(DEFAULT_DELIMITER);

			while (scanner.hasNext()) {
				final BIFBlock block = block(scanner);
				process(block);
			}
		}

		// complete network with factors
		result.factors = new BayesianFactor[result.network.getVariables().length];
		result.variableFactors.forEach((varName, f) -> {
			final Integer varId = result.variableName.get(varName);
			result.factors[varId] = f;
		});
		result.network.setFactors(result.factors);

		return this;
	}

	private BIFBlock block(Scanner scanner) {
		BIFBlock block = new BIFBlock();
		String line = scanner.nextLine();

		block.type = line.substring(0, line.indexOf(" ")).trim();
		block.name = line.substring(line.indexOf(" "), line.lastIndexOf(" ")).trim();

		if (line.startsWith("//"))
			return null;

		while (!line.equals("}")) {
			line = scanner.nextLine().trim();

			BIFAttribute attribute = new BIFAttribute();

			if (line.startsWith("(")) {
				attribute.name = "";
				attribute.value = line;
				block.attributes.add(attribute);
			} else if (line.endsWith(";")) {
				final String[] tokens = line.trim().split(" ", 2);
				attribute.name = tokens[0].trim();
				attribute.value = tokens[1];
				block.attributes.add(attribute);
			}
		}

		return block;
	}

	private void process(BIFBlock block) {
		if (block == null)
			return;

		switch (block.type) {
			case "network":
				parseForNetwork(block);
				break;
			case "variable":
				parseForVariable(block);
				break;
			case "probability":
				parseForProbability(block);
				break;
			default:
				throw new IllegalArgumentException("This library does not support the BIF block type \"" + block.name + "\"");
		}
	}

	private void parseForNetwork(BIFBlock block) {
		result.name = String.join(" ", block.name);
		// TODO: add support for properties
	}

	private void parseForVariable(BIFBlock block) {
		String varName = block.name;
		int varId;

		for (BIFAttribute attribute : block.attributes) {
			switch (attribute.name) {
				case "type":
					final String[] tokens = attribute.value.split(" ", 2);
					final String type = tokens[0];
					final String v = tokens[1];
					if (!type.equals("discrete")) {
						throw new NotImplementedException("This library supports only \"discrete\" variables for Bayesian Networks. Found unsupported type \"" + type + "\" in \"variable\" block \"" + block.name + "\".");
					}
					int states = Integer.parseInt(v.substring(v.indexOf('[') + 1, v.indexOf(']')).trim());
					varId = result.network.addVariable(states);
					result.variableName.put(varName, varId);

					// map states
					String[] s = v.substring(v.indexOf('{') + 1, v.indexOf('}')).split(",");
					for (int i = 0; i < s.length; i++) {
						result.variableStates.put(varName + "$" + s[i].trim(), i);
					}

					break;
				case "property":
					// TODO: add support for other properties
					break;
				default:
					throw new NotImplementedException("Attribute \"" + attribute.name + "\" in block \"" + block.name + "\" not supported yet.");
			}
		}

	}

	private void parseForProbability(BIFBlock block) {
		final String line = block.name;
		final String[] varNames = Arrays.stream(line.split("[\\s|,()]+"))
				.filter(x -> !x.isEmpty())
				.toArray(String[]::new);
		final int[] varIds = Arrays.stream(varNames)
				.mapToInt(result.variableName::get)
				.toArray();

		final String varName = varNames[0];

		final BayesianFactorFactory bff = BayesianFactorFactory.factory().domain(result.network.getDomain(varIds));

		// create parents
		for (int i = 1; i < varIds.length; i++) {
			result.network.addParent(varIds[0], varIds[i]);
		}

		if (varNames.length == 1) {
			// table
			for (BIFAttribute attribute : block.attributes) {
				switch (attribute.name) {
					case "table":
						final double[] values = Arrays.stream(attribute.value.split("[ ,;]"))
								.filter(x -> !x.isEmpty())
								.mapToDouble(Double::parseDouble)
								.toArray();
						bff.data(values);
						break;
					case "property":
						// TODO: add support for other properties
						break;
					case "default":
						// TODO: add support for default value in data
					default:
						throw new NotImplementedException("Found unsupported attribute \"" + attribute.name + "\" in \"probability\" block \"" + line + "\".");
				}
			}

		} else {
			// conditioned
			for (BIFAttribute attribute : block.attributes) {
				if (!attribute.value.startsWith("(")) {
					// TODO: add support for other properties
					continue;
				}

				// parent variables
				final String[] vars = Arrays.stream(attribute.value.substring(attribute.value.indexOf('(') + 1, attribute.value.indexOf(')')).split("[,]"))
						.filter(x -> !x.isEmpty())
						.map(String::trim)
						.toArray(String[]::new);

				// parent states
				int[] states = IntStream.range(1, varNames.length)
						.mapToObj(i -> varNames[i] + "$" + vars[i - 1])
						.mapToInt(result.variableStates::get)
						.toArray();

				// data values
				final double[] vals = Arrays.stream(attribute.value.substring(attribute.value.indexOf(')') + 1).split("[,;]"))
						.filter(x -> !x.isEmpty())
						.map(String::trim)
						.mapToDouble(Double::parseDouble)
						.toArray();

				int[] ints = new int[varNames.length];
				System.arraycopy(states, 0, ints, 1, states.length);

				for (int i = 0; i < vals.length; i++) {
					ints[0] = i;
					bff.value(vals[i], ints);
				}
			}
		}
		result.variableFactors.put(varName, bff.get());
	}

}
