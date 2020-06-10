package ch.idsia.crema.model.io.uai;

import org.springframework.util.Assert;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;


public abstract class UAIParser<T extends  Object> {

    protected String[] elements = null;
    private int offset = 0;
    protected String fileName;

    private String parsedType;

    protected String TYPE;

    protected void readFile() throws IOException {

        StringBuilder stringBuilder = new StringBuilder();

        String line = null;

        // Opening the .cn file
        FileReader fileReader =
                new FileReader(fileName);
        BufferedReader bufferedReader =
                new BufferedReader(fileReader);

        while ((line = bufferedReader.readLine()) != null) {
            stringBuilder.append(line + " ");
        }

        bufferedReader.close();

        // Splitting file elements (spaces, tabs and newlines have the same effect)
        elements = stringBuilder.toString().split("[ \\t\\n]+");

    }

    protected abstract void processFile();

    protected abstract T build();

    public T parse() {

        T parsedObj = null;

        try {
            readFile();
            processFile();
            sanityChecks();
            parsedObj = build();
        }catch (FileNotFoundException ex) {
            System.out.println("Unable to open file '" + fileName + "'");
        } catch (IOException ex) {
            System.out.println("Error reading file '" + fileName + "'");
        } catch (Exception e){
            System.out.println(e);
        }
        return parsedObj;
    }

    protected void sanityChecks(){
        Assert.isTrue(TYPE.equals(parsedType), "Wrong type "+parsedType+" instead of "+TYPE);
    }


    protected void incrementOffset(int n){
        this.offset += n;
    }

    protected int getOffset() {
        return offset;
    }

    protected void setOffset(int offset) {
        this.offset = offset;
    }

    protected String popElement(){
        incrementOffset(1);
        return elements[getOffset()-1];
    }

    protected void parseType(){
        parsedType = popElement();
    }

}
