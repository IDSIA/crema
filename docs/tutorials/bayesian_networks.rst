.. highlight:: java

================
Bayesian Network
================
 
Lets start with an example of *Bayesian Network*. We will create a very small model and perform some simple query.

The network will contain 3 variables connected in a V-shape as shown in the following figure:

.. graphviz::
   :align: center

   digraph bn {
      "A" -> "C";
      "B" -> "C";
   }

The first thing to do is to declare our Bayesian Network, the variables, and assign the parents for each variable. In
this case, all variables are binary.

.. note::
   The ``BayesianNetwork`` is just a wrapper class of the ``DAGModel<BayesianFactor>`` class.

.. literalinclude:: ../../examples/BayesianNetworkExample.java
   :language: java
   :start-after: [1]
   :end-before: [2]
   :dedent: 2

For each variable, the model assign a domain. We need such information to build the factors.

.. literalinclude:: ../../examples/BayesianNetworkExample.java
   :language: java
   :start-after: [2]
   :end-before: [3]
   :dedent: 2

Finally, there we build a factors for each of the variables. In this example, we show an overview of the many possible
ways to instantiate a factor.

.. literalinclude:: ../../examples/BayesianNetworkExample.java
   :language: java
   :start-after: [3]
   :end-before: [4]
   :dedent: 2

Factor A
    We instantiate a new ``BayesianDefaultFactor`` from the domain and an array of double values.

Factor B
    We use the factory to set the probabilities for states ``0`` and ``1``.

Factor C
    We use the factory to set the whole joint probability table for variable ``C`` using the states of all the variables
    in the domain. In order: ``C``, ``A``, ``B``. Compare the order with the variable order in the definition of ``domC``.


Full example:

.. literalinclude:: ../../examples/BayesianNetworkExample.java
   :language: java
