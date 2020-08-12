package ch.idsia.crema.model.io.uai;

import ch.idsia.crema.factor.credal.linear.SeparateHalfspaceFactor;
import ch.idsia.crema.model.graphical.SparseModel;

import java.io.BufferedWriter;
import java.io.IOException;

public class HCredalUAIWriter extends NetUAIWriter<SparseModel>{

    public HCredalUAIWriter(SparseModel target, String file) throws IOException {
        this.target = target;
        TYPE = UAITypes.HCREDAL;
        this.writer = initWriter(file);

    }
    public HCredalUAIWriter(SparseModel target, BufferedWriter writer){
        this.target = target;
        TYPE = UAITypes.HCREDAL;
        this.writer = writer;
    }


    @Override
    protected void sanityChecks() {
        super.sanityChecks();

    }

    @Override
    protected void writeFactors(){

    }

    @Override
    protected void writeTarget() throws IOException {
        writeType();
        writeVariablesInfo();
        writeDomains();
        writeFactors();
    }



    public static void main(String[] args) throws IOException {
        String fileName = "./models/simple-hcredal.uai";
        String fileName2 = "./models/simple-hcredal-copy.uai";
        SparseModel model = (SparseModel) UAIParser.read(fileName);

        HCredalUAIWriter writer = null;
        try{
        writer =  new HCredalUAIWriter(model, fileName2);
        writer.writeToFile();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(writer!=null)
             writer.getWriter().close();
        }




    }



}
