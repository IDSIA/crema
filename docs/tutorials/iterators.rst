================
Domain Iterators
================

When interacting with factors and working with indexing of items in the library
you will definitely need to address the issue of the variables ordering and iterators. 

In our implementation when an iterator over a domain is requested it will return an instance of 
an :code:`IndexIterator`. This Java iterator will visit the different 
instantiations of the variables by means of an integer index. This index enumerates all
the possible configurations of the domain's variables, sorted order with the variable
at index 0 being the least significant. 

In its original ordering and domain the index will simply be an increasing integer value.
In the following example we show this on a domain defined on the binary variables 0 and 2 and
the ternary variable 3.

.. literalinclude:: ../../src/test/java/ch/idsia/crema/tutorial/IteratorsTutorial.java
    :start-after: [iterate-trivial]
    :end-before: [iterate-trivial]
    :dedent: 8
    :language: java
    
which will output:

``0 1 2 3 4 5 6 7 8 9 10 11``

Asking the domain we can olso convert this index to the actual states for the variables. 
This can be achieved with a call to ``getStatesFor`` and for the code above will generate 
the sequence of states configurations shown in the following table:

.. _Table 1:

+--------+-----------+
|        |  Variable |
|        +---+---+---+
| Offset | 0 | 2 | 3 |
+========+===+===+===+
|    0   | 0 | 0 | 0 | 
+--------+---+---+---+
|    1   | 1 | 0 | 0 | 
+--------+---+---+---+ 
|    2   | 0 | 1 | 0 | 
+--------+---+---+---+ 
|    3   | 1 | 1 | 0 | 
+--------+---+---+---+ 
|    4   | 0 | 2 | 0 | 
+--------+---+---+---+ 
|    5   | 1 | 2 | 0 | 
+--------+---+---+---+ 
|    6   | 0 | 0 | 1 | 
+--------+---+---+---+ 
|    7   | 1 | 0 | 1 | 
+--------+---+---+---+ 
|    8   | 0 | 1 | 1 | 
+--------+---+---+---+ 
|    9   | 1 | 1 | 1 | 
+--------+---+---+---+ 
|   10   | 0 | 2 | 1 | 
+--------+---+---+---+ 
|   11   | 1 | 2 | 1 | 
+--------+---+---+---+ 

This is obvioulsy quite a usesless use of an iterator. An simple increasing integer would be enough.
The really interesting use of iterators arises when we want to index a domain fixing some variable, 
reordering them or even using a larger domain. These uses are all explored in further detail 
hereafter. 


Modified variable order
=======================

Crema uses mostly a global ordering of the variables. This, however, is not always the most natural and confortable way to index data. To overcome this issue crema offers in some methods to iterate over the indices using a different ordering of the variable. 

So if one has a domain that is defined over two binary variables :math:`\{1, 2\}`, a reordered iterator 
over the same domain but with inverse order, will visit all the indices in the order shown below. 


.. graphviz::

    digraph G {
      graph [pad=0.05, nodesep=1, ranksep=0.1];
      splines=false;
      clusterrank=local;
      node [shape=box,style=filled,color=lightgray];
      edge[style=invis];

      subgraph cluster_cl1{
         label = "{1,2}";
         style = "dashed";
         color = "red";
         
         p0[label="0"];
         p1[label="1"];
         p2[label="2"];
         p3[label="3"];
         p0->p1->p2->p3
      }

      subgraph cluster_cl2{
         label = "{2,1}";
         style = "dashed";
         color = "green";

         d0[label="0"];
         d2[label="2"];
         d1[label="1"];
         d3[label="3"];
         d0->d2->d1->d3
      }

      edge[style=solid, penwidth=1, constraint=false];
      p0->d0;
      p1->d1;
      p2->d2;
      p3->d3;
    }

In code creating this iterator can be done directly from the Strides class, as shown in the following
code snippet: 

.. literalinclude:: ../../src/test/java/ch/idsia/crema/tutorial/IteratorsTutorial.java
    :start-after: [iterate-reordered]
    :end-before: [iterate-reordered]
    :dedent: 8
    :language: java


Wider domain
============
Another useful way to traverse a domain is to expand it with attitional variables. In such configuration
the iterator will not move for different instantiations of these additional variables. 

In the following code snipped a domain over variable 0 is visited moving both variable 1 and variable 0. 

.. literalinclude:: ../../src/test/java/ch/idsia/crema/tutorial/IteratorsTutorial.java
    :start-after: [iterate-extended]
    :end-before: [iterate-extended]
    :dedent: 8
    :language: java

In the following table we show the evolution of the :code:`offset` within the original domain
for the different states configurations of the two variables of the extended domain. 

+--------+-------+-------+--------+
| target | Var 0 | Var 1 | offset |
+========+=======+=======+========+
|  0     | 0     | 0     |  0     |
+--------+-------+-------+--------+
|  1     | 1     | 0     |  0     |
+--------+-------+-------+--------+
|  2     | 0     | 1     |  1     |
+--------+-------+-------+--------+
|  3     | 1     | 1     |  1     |
+--------+-------+-------+--------+
|  4     | 0     | 2     |  2     |
+--------+-------+-------+--------+
|  5     | 1     | 2     |  2     |
+--------+-------+-------+--------+


Filtered Iterators
==================

One final way to address indexing is by conditioning on some variables. In this setting the domain will 
have some variables fixed to some state while the others are going to be iteratred. 

In the following example we will take a domain over 3 variables (2 binary and a ternary one) and iterate
over it blocking one of the binary variable. 

.. literalinclude:: ../../src/test/java/ch/idsia/crema/tutorial/IteratorsTutorial.java
    :start-after: [iterate-filtered]
    :end-before: [iterate-filtered]
    :dedent: 8
    :language: java
 

+--------+-----------+
|        |  Variable |
|        +---+---+---+
| Offset | 0 | 2 | 3 |
+========+===+===+===+
|    1   | 1 | 0 | 0 | 
+--------+---+---+---+ 
|    3   | 1 | 1 | 0 | 
+--------+---+---+---+ 
|    5   | 1 | 2 | 0 | 
+--------+---+---+---+ 
|    7   | 1 | 0 | 1 | 
+--------+---+---+---+ 
|    9   | 1 | 1 | 1 | 
+--------+---+---+---+ 
|   11   | 1 | 2 | 1 | 
+--------+---+---+---+ 