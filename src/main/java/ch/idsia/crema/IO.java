package ch.idsia.crema;

import ch.idsia.crema.model.io.uai.UAIParser;
import ch.idsia.crema.model.io.uai.UAIWriter;

import java.io.IOException;
import java.util.stream.Stream;

/**
 * Access point to all the implemented parsers
 *
 * @author Rafael Cabañas
 */
// TODO: this class should go with model.io.uai classes or removed in favor of the classes in that package
public class IO {

	public static final String[] UAIextensions = {".uai", ".uai.do", "uai.evid"};

	public static <T> T readUAI(String filename) throws IOException {
		return UAIParser.read(filename);
	}

	public static <T> void writeUAI(T target, String filename) throws IOException {
		UAIWriter.write(target, filename);
	}

	public static <T> T read(String filename) throws IOException {
		if (Stream.of(UAIextensions).anyMatch(filename::endsWith)) {
			return readUAI(filename);
		} else {
			throw new IllegalArgumentException("Unknown file extension");
		}
	}

	public static <T> void write(T target, String filename) throws IOException {

		if (Stream.of(UAIextensions).anyMatch(filename::endsWith)) {
			writeUAI(target, filename);
		} else {
			throw new IllegalArgumentException("Unknown file extension");
		}
	}

}
