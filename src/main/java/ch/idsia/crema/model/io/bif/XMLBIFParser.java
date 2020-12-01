package ch.idsia.crema.model.io.bif;

import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.model.graphical.DAGModel;
import ch.idsia.crema.model.graphical.GraphicalModel;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * parser for the XMLBIF format (<a href=
 * "http://www.cs.cmu.edu/~fgcozman/Research/InterchangeFormat/">http://www.cs.cmu.edu/~fgcozman/Research/InterchangeFormat/</a>)
 *
 * @author huber
 */
public class XMLBIFParser {

	/**
	 * load the variables from the DOM and return a Name - ID mapping
	 *
	 * @param model
	 * @param document
	 * @return
	 */
	private Map<String, Integer> loadVariables(GraphicalModel<BayesianFactor> model, Document document) {
		NodeList variables = document.getElementsByTagName("VARIABLE");
		HashMap<String, Integer> vars = new HashMap<>();

		for (int index = 0; index < variables.getLength(); ++index) {
			Node var_node = variables.item(index);
			int states = 0;
			NodeList children = var_node.getChildNodes();
			String name = null;
			for (int child_index = 0; child_index < children.getLength(); ++child_index) {
				Node child = children.item(child_index);
				String tag = child.getNodeName();
				if ("OUTCOME".equals(tag)) {
					states++;
				} else if ("NAME".equals(tag)) {
					name = child.getTextContent();
				}
			}
			vars.put(name, model.addVariable(states));
		}
		return vars;
	}

	/**
	 * Load the model from the provided stream.
	 *
	 * @param stream
	 * @return
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 */
	public GraphicalModel<BayesianFactor> parse(InputStream stream)
			throws SAXException, IOException, ParserConfigurationException {
		// load document
		Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(stream);
		GraphicalModel<BayesianFactor> model = new DAGModel<>();

		// load variables
		Map<String, Integer> variables = loadVariables(model, document);

		// load factor definitions
		NodeList tables = document.getElementsByTagName("DEFINITION");
		for (int index = 0; index < tables.getLength(); ++index) {
			int variable = 0;
			Node def_node = tables.item(index);
			double[] table = null;

			NodeList children = def_node.getChildNodes();
			ArrayList<Integer> conditionong = new ArrayList<>();

			for (int child_index = 0; child_index < children.getLength(); ++child_index) {
				Node child = children.item(child_index);
				String tag = child.getNodeName();
				String text = child.getTextContent();

				if ("FOR".equals(tag)) {
					variable = variables.get(text);
				} else if ("GIVEN".equals(tag)) {
					conditionong.add(variables.get(text));
				} else if ("TABLE".equals(tag)) {
					String[] params = text.split("\\s");
					table = Arrays.stream(params).mapToDouble(Double::parseDouble).toArray();
				}
			}

			int[] parents = conditionong.stream().mapToInt(x -> x).toArray();
			int[] sorted = Arrays.copyOf(parents, parents.length + 1);
			System.arraycopy(parents, 0, sorted, 1, parents.length);
			sorted[0] = variable;
			parents = sorted.clone();
			Arrays.sort(sorted);

			BayesianFactor factor = new BayesianFactor(model.getDomain(sorted), false);
			factor.setData(parents, table);
			model.setFactor(variable, factor);
		}

		return model;
	}

//	public static void main(String[] args) throws SAXException, IOException, ParserConfigurationException {
//		XMBIFParser parser = new XMBIFParser();
//		FileInputStream fio = new FileInputStream("/Users/huber/Work/workspaces/CrEDU/maven.1445866298504/library/CreMA/src/test/resources/xmlbif/3nodes.xml");
//		GraphicalModel<BayesianFactor> model = parser.parse(fio);
//		
//		DotSerialize ds = new DotSerialize();
//		System.out.println(ds.run(model));
//		fio.close();
//	}
}
