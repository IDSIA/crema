package ch.idsia.crema.model.io.uai;

import ch.idsia.crema.model.graphical.SparseModel;
import ch.idsia.crema.model.io.TypesIO;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

public abstract class UAIWriter<T extends  Object> {

    protected BufferedWriter writer;
    protected TypesIO TYPE;

    protected T target;


    public static void write(Object target, String fileName) throws IOException {

        UAIWriter writer = null;
        try{
            if(HCredalUAIWriter.isCompatible(target))
                writer =  new HCredalUAIWriter((SparseModel) target, fileName);
            else if(VCredalUAIWriter.isCompatible(target))
                writer =  new VCredalUAIWriter((SparseModel) target, fileName);
            else
                throw new IllegalArgumentException("Unknown type to write");
            writer.writeToFile();
        }catch (Exception e){
            if(writer!=null) writer.getWriter().close();
            throw e;
        }
        if(writer!=null) writer.getWriter().close();
    }




    public BufferedWriter initWriter(String file) throws IOException {
        return new BufferedWriter(new FileWriter(file));
    }

    public BufferedWriter getWriter() {
        return writer;
    }

    protected void tofile(String s) throws IOException {
        writer.write(s);
    }
    protected void tofile(double... values) throws IOException {
        tofile(DoubleStream.of(values).mapToObj(v -> v+" ").collect(Collectors.joining()));
    }
    protected void tofile(int... values) throws IOException {
        tofile(IntStream.of(values).mapToObj(v -> v+" ").collect(Collectors.joining()));
    }


    protected void tofileln(String s) throws IOException {
        writer.write(s+"\n");
    }
    protected void tofileln(double... values) throws IOException {
        tofileln(DoubleStream.of(values).mapToObj(v -> v+" ").collect(Collectors.joining()));
    }
    protected void tofileln(int... values) throws IOException {
        tofileln(IntStream.of(values).mapToObj(v -> v+" ").collect(Collectors.joining()));
    }

    public void writeToFile() throws IOException {
        sanityChecks();
        writeTarget();
    }

    public void writeType() throws IOException {
        tofileln(this.TYPE.getLabel()+" ");
    }

    protected abstract void sanityChecks();
    protected abstract void writeTarget() throws IOException;

    protected static boolean isCompatible(Object object){
        throw new IllegalStateException(
                "isCompatible hasn't been set up in the subclass");
    }




}


