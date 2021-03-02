package ch.idsia.crema.tutorial;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;

import org.junit.Test;

import ch.idsia.crema.core.Domain;
import ch.idsia.crema.core.Strides;
import ch.idsia.crema.utility.IndexIterator;

public class IteratorsTutorial {


    @Test
    public void trivial() {
        // [iterate-trivial]
        Strides domain = Strides.var(0,2).and(3, 2).and(2,3);
        IndexIterator iterator = domain.getIterator();
        while(iterator.hasNext()) {
            System.out.print(iterator.next() + " ");
        }
        // [iterate-trivial]

        iterator = domain.getIterator();
        
        while(iterator.hasNext()) {
            int offset = iterator.next();
        // [iterate-trivial2]
        int[] states = domain.getStatesFor(offset);
        // [iterate-trivial2]

        for (int state : states) 
            System.out.print(state + " | ");
        System.out.println();    
        }
    }


    @Test 
    public void testFiltered() { 
        Strides domain = Strides.var(1, 3).and(4,4).and(5,2);
        IndexIterator iter = domain.getFiteredIndexIterator(new int[] {4}, new int[] {1});
        while(iter.hasNext()) {
            int index = iter.next();
            System.out.println(Arrays.toString(domain.getStatesFor(index)));
            System.out.println(index);
        }
    }

    @Test
    public void test() { 
        double[] data= new double[100];
        double[] input= new double[100];
        
        // [iterate-reordered]
        Strides domain = Strides.var(1,2).and(2,2);
        IndexIterator iter = domain.getReorderedIterator(new int[] {2,1});
        int original = 0;
        while (iter.hasNext()) {
            int offset = iter.next();
            // some operation using the updated order
            data[offset] = input[original++]; 
        }
        // [iterate-reordered]



        System.out.println("   digraph G {");
        System.out.println("      graph [pad=0.05, nodesep=1, ranksep=0.1];");
        System.out.println("      splines=false;");
        System.out.println("      clusterrank=local;");
        System.out.println("      node [shape=box];");
        System.out.println("      edge[style=invis];");

        iter = domain.getIterator(domain);
        System.out.println(toDotCluster(iter, "V1, V2", "p", "cl1", domain));
        
        iter = domain.getReorderedIterator(new int[] {2,1});
        System.out.println(toDotCluster(iter, "V2, V1","d", "cl2", domain));
        
        System.out.println("      edge[style=solid, penwidth=1, constraint=false];");
        for (int i =0; i < domain.getCombinations();++i){
            System.out.println("      p" + i + "->d" + i + ";");
        }
        System.out.println("}");

    }

    private String toDotCluster(IndexIterator iter, String title,  String base, String name, Strides original) {
        return toDotCluster(iter, title, base, name, original, false);
    }

    private String toDotCluster(IndexIterator iter, String title,  String base, String name, Strides original, boolean usestep) {
        StringWriter writer = new StringWriter();

        PrintWriter out = new PrintWriter(writer);
        
        out.println("      subgraph "+name+"{");
        out.println("         label=\"" + title + "\"");
        out.println("         style=filled;");
        out.println("         color=white;");
        out.println("         node [style=filled,color=lightgray];");
        
        String connection = "         "+base+"0";
        int step =0;
        while(iter.hasNext()) {
            int index = iter.next();
            int off = usestep ? step : index;
            String pos = " " + Arrays.toString(original.getStatesFor(off));

            String node_name = usestep ? (base + step) : (base + index);
            out.println("         " + node_name + "[label=\""+index+""+pos+ "\"];");
            if (step > 0) connection += "->" + node_name;
            step ++;
        }
        out.println(connection);
        out.println("      }");
        out.flush();
        return writer.toString();
    }


    @Test
    public void testMarginal() {
        Strides d1 = Strides.var(1,3);
        Strides d2 = Strides.var(0,3).and(1,2);

        IndexIterator iter = d2.getReorderedIterator(new int[] {1,0});
        while(iter.hasNext()){
            int offset = iter.next();
            System.out.println(offset + " " + Arrays.toString(d2.getStatesFor(offset)));
        }
    }



    @Test
    public void generateAugmentedDomain() { 
        double[] data= new double[100];
        double[] input= new double[100];
        
        // [iterate-extended]
        Strides domain = Strides.var(1,3);
        Strides bigger_domain = Strides.var(1,3).and(0,2);

        IndexIterator iter = domain.getIterator(bigger_domain);

        int target = 0;
        while (iter.hasNext()) {
            int offset = iter.next();
            // some operation using the offset
            data[target++] = input[offset]; 
        }
        // [iterate-extended]



        System.out.println("   digraph G {");
        System.out.println("      graph [pad=0.05, nodesep=1, ranksep=0.1];");
        System.out.println("      splines=false;");
        System.out.println("      clusterrank=local;");
        System.out.println("      node [shape=box];");
        System.out.println("      edge[style=invis];");

        iter = domain.getIterator(bigger_domain);
        System.out.println(toDotCluster(iter, "V1, V2", "p", "cl1", bigger_domain, true));
        
        System.out.println("}");

    }


    @Test
    public void fixedStates(){
        // [iterate-filtered]
        Strides domain = Strides.var(2,3).and(0,2).and(3,2);
        IndexIterator iter = domain.getFiteredIndexIterator(0,1);
        while (iter.hasNext()) {
            int offset = iter.next();
            System.out.println(offset);
        }
        // [iterate-filtered]


    }
}
