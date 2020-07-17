Model Definition
==============================


Credal Set Specification
--------------------------------

For the definition of a credal set, the domains should be first specified.
Discrete variable domains in Crema are managed with objects of class ``Strides``.
Then, for the definition of a credal set defined by its vertices, create an object
of class ``VertexFactor`` as shown below.




.. literalinclude:: ../../examples/docs/FactorsDef.java
   :language: java
   :lines: 16-26


Similarly, a conditional credal set can be define as shown in the following code.


.. literalinclude:: ../../examples/docs/FactorsDef.java
   :language: java
   :lines: 32-46


Crema also allows the specification of credal sets by defining
its constraints. This is done with the class ``SeparateHalfspaceFactor``.

.. literalinclude:: ../../examples/docs/FactorsDef.java
   :language: java
   :lines: 51-63




Credal Network Specification
------------------------------------

For defining a credal network, create an object of class ``SparseModel``, specify
the structure of the graph and associate the factors.

.. literalinclude:: ../../examples/docs/FactorsDef.java
   :language: java
   :lines: 68-76










