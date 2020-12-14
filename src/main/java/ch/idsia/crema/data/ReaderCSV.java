package ch.idsia.crema.data;

import ch.idsia.crema.utility.ArraysUtil;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import com.opencsv.exceptions.CsvValidationException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class ReaderCSV {

	private String fileName;

	private boolean dropUnnamed = true;

	private boolean withHeader = true;

	private char separator = ',';

	String[] varnames;

	double[][] data;

	CSVReader csvReader;

	public ReaderCSV(String fileName) {
		this.fileName = fileName;
	}

	public ReaderCSV read() throws IOException, CsvException {
		initReader();
		parseVarNames();
		parseData();
		dropUnnamed();
		csvReader.close();
		return this;

	}

	private void initReader() throws FileNotFoundException {
		csvReader = new CSVReaderBuilder(new FileReader(fileName))
				.withCSVParser(new CSVParserBuilder().withSeparator(separator).build())
				.build();
	}

	private void parseVarNames() throws IOException, CsvValidationException {
		if (withHeader)
			varnames = csvReader.readNext();
		else {
			varnames = IntStream.range(0, csvReader.readNext().length)
					.mapToObj(String::valueOf)
					.toArray(String[]::new);
			initReader();
		}

	}

	private void parseData() throws IOException, CsvException {
		data = csvReader.readAll()
				.stream()
				.map(line -> Stream.of(line)
						.mapToDouble(v -> {
							if (!v.isEmpty())
								return Double.parseDouble(v);
							return Double.NaN;

						}).toArray())
				.toArray(double[][]::new);
	}

	private void dropUnnamed() {
		int[] idx = ArraysUtil.where(varnames, s -> s.equals(""));

		data = ArraysUtil.dropColumns(data, idx);

		varnames = IntStream.range(0, varnames.length)
				.filter(i -> !ArraysUtil.contains(i, idx))
				.mapToObj(i -> varnames[i])
				.toArray(String[]::new);
	}


	public ReaderCSV dropUnnamed(boolean flag) {
		dropUnnamed = flag;
		return this;
	}

	public ReaderCSV withHeader(boolean flag) {
		withHeader = flag;
		return this;
	}

	public ReaderCSV withSeparator(char sep) {
		separator = sep;
		return this;
	}

	public double[][] getData() {
		return data;
	}

	public String[] getVarNames() {
		return varnames;
	}

	public static void main(String[] args) throws IOException, CsvException {
		ReaderCSV reader = new ReaderCSV("./datasets/simple.csv")
				.dropUnnamed(true)
				.withSeparator(',')
				.withHeader(true)
				.read();

		System.out.println(Arrays.toString(reader.getVarNames()));
		System.out.println("------------");
		for (double[] d : reader.getData())
			System.out.println(Arrays.toString(d));
	}
}
