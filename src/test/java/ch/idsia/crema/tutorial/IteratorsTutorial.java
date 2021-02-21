package ch.idsia.crema.tutorial;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;

import org.junit.Test;

import ch.idsia.crema.core.Strides;
import ch.idsia.crema.utility.IndexIterator;

public class IteratorsTutorial {

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
        // [iterate-reordered]
        Strides domain = Strides.var(1,2).and(2,2);
        IndexIterator iter = domain.getReorderedIterator(new int[] {2,1});
        while (iter.hasNext()) {
            int offset = iter.next();
            data[offset] = 0; 
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
            String pos = " "+ Arrays.toString(original.getStatesFor(index));
            out.println("         " + base + index + "[label=\""+index+""+pos+ "\"];");
            if (step > 0) connection += "->" + base + index;
            step ++;
        }
        out.println(connection);
        out.println("      }");
        out.flush();
        return writer.toString();
    }
}
