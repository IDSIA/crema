package ch.idsia.crema.model.io.uai;

import ch.idsia.crema.model.io.TypesIO;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Generic parser class for UAI format
 *
 * @author Rafael Cabañas
 */

public abstract class UAIParser<T> {

	protected String[] elements = null;
	private int offset = 0;
	private String parsedType = "";

	protected TypesIO TYPE;

	protected List<String> lines;

	@SuppressWarnings("unchecked")
	public static <T> T read(String filename) throws IOException {
		if (!(filename.endsWith(".uai") || filename.endsWith(".uai.evid") || filename.endsWith(".uai.do"))) {
			throw new IllegalArgumentException("Unknown file extension");
		}

		List<String> lines = readLines(filename);
		TypesIO type = UAITypes.valueOfLabel(getIOTypeStr(lines.get(0)));

		Object parsedObject;

		// Parse the file
		if (type == UAITypes.HCREDAL) {
			parsedObject = new HCredalUAIParser(lines).parse();
		} else if (type == UAITypes.VCREDAL) {
			parsedObject = new VCredalUAIParser(lines).parse();
		} else if (type == UAITypes.BAYES) {
			parsedObject = new BayesUAIParser(lines).parse();
		} else if (type == UAITypes.EVID) {
			parsedObject = new EvidUAIParser(lines).parse();
		} else {
			throw new IllegalArgumentException("Unknown type to be parsed");
		}

		return (T) parsedObject;
	}

	public static List<String> readLines(String filename) throws IOException {
		return Files.lines(Path.of(filename))
				// skip comment lines
				.filter(line -> !(line.startsWith("//") || line.startsWith("#")))
				.collect(Collectors.toList());
	}

	public static String getIOTypeStr(String firstLine) {
		// Extract the type to know the required parser
		String str = firstLine.replaceAll("[ \\t\\n]+", "");

		int i = str.indexOf(" ");

		if (i > 0)
			return str.substring(0, i);
		return str;
	}

	public UAIParser(String filename) throws IOException {
		lines = readLines(filename);
		TYPE = UAITypes.valueOfLabel(getIOTypeStr(lines.get(0)));
	}

	public UAIParser(List<String> lines) {
		this.lines = lines;
	}

	protected void readContent() {
		String content = String.join(" ", lines);

		// Splitting file elements (spaces, tabs and newlines have the same effect)
		elements = content.split("[ \\t\\n]+");
	}

	protected abstract void processFile();

	protected abstract T build();

	public T parse() {
		T parsedObj;
		readContent();
		processFile();
		sanityChecks();
		parsedObj = build();
		return parsedObj;
	}

	protected void sanityChecks() {
		if (!TYPE.getLabel().equals(parsedType))
			throw new IllegalArgumentException("Wrong type " + parsedType + " instead of " + TYPE);
	}

	protected void incrementOffset(int n) {
		this.offset += n;
	}

	protected int getOffset() {
		return offset;
	}

	protected void setOffset(int offset) {
		this.offset = offset;
	}

	protected String popElement() {
		incrementOffset(1);
		return elements[getOffset() - 1];
	}

	protected void parseType() {
		parsedType = popElement();
	}

	protected int popInteger() {
		return Integer.parseInt(popElement());
	}

	protected double popDouble() {
		return Double.parseDouble(popElement());
	}

}
