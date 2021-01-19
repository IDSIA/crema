package ch.idsia.crema.data;

import ch.idsia.crema.utility.ArraysUtil;
import com.opencsv.CSVWriter;
import com.opencsv.CSVWriterBuilder;
import com.opencsv.exceptions.CsvException;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class WriterCSV {

	private String[] varnames;
	private String[][] data;
	private boolean addIndex = true;
	private char separator = ',';
	private String fileName;

	public WriterCSV(double[][] data, String fileName) {
		this.fileName = fileName;

		this.data = Stream.of(data)
				.map(v -> DoubleStream.of(v).mapToObj(d -> {
					if (Double.isNaN(d)) return "";
					if (d == (int) d)
						return String.valueOf((int) d);
					return String.valueOf(d);
				}).toArray(String[]::new))
				.toArray(String[][]::new);
	}


	public WriterCSV(int[][] data, String fileName) {
		this.fileName = fileName;
		this.data = Stream.of(data)
				.map(v -> IntStream.of(v)
						.mapToObj(String::valueOf).toArray(String[]::new))
				.toArray(String[][]::new);

	}

	public WriterCSV write() throws IOException {
		CSVWriter writer = (CSVWriter) new CSVWriterBuilder(new FileWriter(fileName))
				.withSeparator(',')
				.withQuoteChar(CSVWriter.NO_QUOTE_CHARACTER)
				.build();

		if (addIndex) {
			if (isWithHeader())
				varnames = ArraysUtil.preAppend(varnames, "");
			for (int i = 0; i < data.length; i++) {
				data[i] = ArraysUtil.preAppend(data[i], String.valueOf(i));
			}
		}

		if (isWithHeader())
			writer.writeNext(varnames);
		writer.writeAll(List.of(data));
		writer.close();
		return this;
	}

	public WriterCSV setVarNames(String... varnames) {
		this.varnames = varnames;
		return this;
	}

	public WriterCSV withIndex(boolean flag) {
		this.addIndex = flag;
		return this;
	}

	public WriterCSV withSeparator(char sep) {
		this.separator = sep;
		return this;
	}

	public boolean isWithHeader() {
		return varnames != null && varnames.length > 0;
	}


	public static void main(String[] args) throws IOException, CsvException {
		ReaderCSV reader = new ReaderCSV("./datasets/simple.csv")
				.dropUnnamed(true)
				.withSeparator(',')
				.withHeader(true)
				.read();

		WriterCSV writer = new WriterCSV(reader.getData(), "./datasets/simple2.csv")
				.setVarNames(reader.getVarNames())
				.withIndex(true)
				.withSeparator(',')
				.write();
	}

}
