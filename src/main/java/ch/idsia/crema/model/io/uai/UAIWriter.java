package ch.idsia.crema.model.io.uai;

import ch.idsia.crema.factor.OperableFactor;
import ch.idsia.crema.factor.credal.vertex.separate.VertexFactor;
import ch.idsia.crema.model.graphical.BayesianNetwork;
import ch.idsia.crema.model.graphical.DAGModel;
import ch.idsia.crema.model.io.TypesIO;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public abstract class UAIWriter<T> {

	protected TypesIO TYPE;

	protected T target;

	protected List<String> lines = new ArrayList<>();

	protected String filename;

	@SuppressWarnings("unchecked")
	public static void write(Object target, String filename) throws IOException {
		if (HCredalUAIWriter.isCompatible(target))
			new HCredalUAIWriter((DAGModel<? extends OperableFactor<?>>) target, filename).write();
		else if (VCredalUAIWriter.isCompatible(target))
			new VCredalUAIWriter((DAGModel<VertexFactor>) target, filename).write();
		else if (BayesUAIWriter.isCompatible(target))
			new BayesUAIWriter((BayesianNetwork) target, filename).write();
		else
			throw new IllegalArgumentException("Unknown type to write");
	}

	public UAIWriter(T target, String filename) {
		this.target = target;
		this.filename = filename;
	}

	protected String str(String... values) {
		return StringUtils.join(values, " ");
	}

	protected String str(double... values) {
		return StringUtils.join(ArrayUtils.toObject(values), " ");
	}

	protected String str(int... values) {
		return StringUtils.join(ArrayUtils.toObject(values), " ");
	}

	protected void append(String... line) {
		lines.add(String.join(" ", line));
	}

	protected void append(double... values) {
		append(str(values));
	}

	protected void append(int... values) {
		append(str(values));
	}

	public void write() throws IOException {
		sanityChecks();
		writeTarget();
		writeToFile();
	}

	public List<String> serialize() {
		sanityChecks();
		writeTarget();
		return lines;
	}

	protected void writeToFile() throws IOException {
		Files.write(Path.of(filename), lines);
	}

	protected void writeType() {
		append(this.TYPE.getLabel());
	}

	protected abstract void sanityChecks();

	protected abstract void writeTarget();

}


