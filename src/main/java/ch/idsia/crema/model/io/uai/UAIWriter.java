package ch.idsia.crema.model.io.uai;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

public abstract class UAIWriter<T extends  Object> extends UAI {

    protected BufferedWriter writer;
    protected UAITypes TYPE;

    protected T target;

    public BufferedWriter initWriter(String file) throws IOException {
        return new BufferedWriter(new FileWriter(file));
    }

    public BufferedWriter getWriter() {
        return writer;
    }

    protected void print(String s) throws IOException {
        writer.write(s);
    }
    protected void print(double... values) throws IOException {
        print(DoubleStream.of(values).mapToObj(v -> v+" ").collect(Collectors.joining()));
    }
    protected void print(int... values) throws IOException {
        print(IntStream.of(values).mapToObj(v -> v+" ").collect(Collectors.joining()));
    }


    protected void println(String s) throws IOException {
        writer.write(s+"\n");
    }
    protected void println(double... values) throws IOException {
        println(DoubleStream.of(values).mapToObj(v -> v+" ").collect(Collectors.joining()));
    }
    protected void println(int... values) throws IOException {
        println(IntStream.of(values).mapToObj(v -> v+" ").collect(Collectors.joining()));
    }

    public void writeToFile() throws IOException {
        sanityChecks();
        writeTarget();
    }

    public void writeType() throws IOException {
        println(this.TYPE.label);
    }

    protected abstract void sanityChecks();
    protected abstract void writeTarget() throws IOException;




}


