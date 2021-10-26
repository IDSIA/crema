.. highlight:: java

================
Bayesian Network
================
 
Lets start with an example of Bayesian Network. Later we will look into more detail  
how to create Credal Networks and how to work with factors directly. 

We will create a vary small Bayesian Network and perform some simple query. The network 
will contain 3 variables connected in a V-shape as shown in the following figure 

.. graphviz::

   digraph foo {
      "bar" -> "baz";
   }