================
Domain Iterators
================

When interacting with factors and working with indexing of items in the library
you will definitely need to address the issue of the variables ordering and iterators. 

Modified variable order
=======================

Crema uses mostly a global ordering of the variables. This, however, is not always the most natural and confortable way to index data. To overcome this issue crema offers in some methods to iterate over the indices using a different ordering of the variable. 

.. graphviz::

    digraph G {
      graph [pad=0.05, nodesep=1, ranksep=0.1];
      splines=false;
      clusterrank=local;
      node [shape=box];
      edge[style=invis];
      subgraph cl1{
         label="V1, V2"
         style=filled;
         color=white;
         node [style=filled,color=lightgray];
         p0[label="0 [0, 0]"];
         p1[label="1 [1, 0]"];
         p2[label="2 [0, 1]"];
         p3[label="3 [1, 1]"];
         p0->p1->p2->p3
      }

      subgraph cl2{
         label="V2, V1"
         style=filled;
         color=white;
         node [style=filled,color=lightgray];
         d0[label="0 [0, 0]"];
         d2[label="2 [0, 1]"];
         d1[label="1 [1, 0]"];
         d3[label="3 [1, 1]"];
         d0->d2->d1->d3
      }

      edge[style=solid, penwidth=1, constraint=false];
      p0->d0;
      p1->d1;
      p2->d2;
      p3->d3;
    }

