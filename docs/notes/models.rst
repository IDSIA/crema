.. highlight:: java


================
Graphical Models
================

Crema includes a few packages to work with probabilistc graphical models. 
These include support the network representations, algorithms and modifiers. 

Working with networks
=====================

As an exercise we will be creating a Bayesian Networks with 3 nodes connected in a V shape, as shown in the
following picture.

.. graphviz::
   :align: center

    digraph example1 {
        A -> C;
        B -> C;
    }


Graphical Networks are implemented in the :code:`models.graphical` package and they extend the :code:`Graph` class. 
The class has a generic parameter to specify the concrete Factor used in order to express the probability models that
parametrise the relationships between variables defined by the network.

There are currenlty 2 concrete implementations of graphical networks that differ in the underlying storage of the edges and nodes. 
From an inference and algorithmic point of view the actual implementation is irrelevant. 

DAG Models
-----------

The main implementation for directed acyclic graphs is the :code:`DAGModel` class. 
Crema uses `JGraphT`_ ``SimpleGraph`` object to store the actual graph.

.. _JGraphT: https://jgrapht.org/

For a Bayesian Network we will use a :code:`BayesianFactor`.

.. literalinclude:: ../../src/test/java/ch/idsia/crema/tutorial/NetworkTutorial.java
    :start-after: [creating-sparse-model]
    :end-before: [creating-sparse-model]
    :dedent: 2
    :language: java


.. Note::
    
    In its current implementation crema stores networks using a double adjacency lists. This is for each node in the network we store 
    the collection of parents and children. 

