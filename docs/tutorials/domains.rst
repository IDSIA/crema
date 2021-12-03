.. highlight:: java

=======
Domains
=======

.. contents:: Table of Contents
    :local:


Domain interface
================

Domains in Crema are located in the :code:`ch.idsia.crema.model` package.
They are all instances of the :code:`Domain` interface. 
This simple interface declares basic methods to query the domain about variables and their cardinality.


::

    Domain domain = ...;
    domain.getSizes();
    domain.getVariables();

.. Note::
    Returned arrays should never be modified!


SimpleDomain
============

The simplest implementation of the :code:`Domain` interface is the :code:`SimpleDomain`.
This class encapsulates two integer arrays. One with the variable labels and one with their cardinality. 
 
.. literalinclude:: ../../src/test/java/ch/idsia/crema/tutorial/DomainsTutorial.java
    :start-after: [simple-domain]
    :end-before: [simple-domain]
    :dedent: 2

.. Warning::
    When creating a :code:`SimpleDomain` the list of variables must be sorted! 
    Crema will **not** automatically sort them, but for some operations will assume they are.


DomainBuilder
=============

While creating a :code:`SimpleDomain` by passing the arrays of variables and their sizes is possible and valid,
a slightly more friendly method is available using the :code:`DomainBuilder`. 
Laveraging the ellipses of Java the :code:`DomainBuilder` class avoids the explicit creation of the arrays as shown in the following example.

.. literalinclude:: ../../src/test/java/ch/idsia/crema/tutorial/DomainsTutorial.java
    :start-after: [domain-builder-1]
    :end-before: [domain-builder-1]
    :dedent: 2


Strides
=======

A more sophisticated and more frequently used implementation of the :code:`Domain` interface is the :code:`Strides` class.
In addition to the arrays of variables and their cardinality, this class caches the cumulative sizes of the variables in the provided order.
The access to this additional array is seldomly required by the end-user. They are mostly required 
to index parts of a probability table. 

The :code:`Strides` class offers a much richer set of functionalities both 
related to the domain itself and the aforementioned indexing of probability tables.


Creating Strides
~~~~~~~~~~~~~~~~

We we first look at how :code:`Strides` instances can be created conveniently. 

.. Note::
    The variable's cardinalities are accumlated starting from the variable at index 0.

.. literalinclude:: ../../src/test/java/ch/idsia/crema/tutorial/DomainsTutorial.java
    :start-after: [strides]
    :end-before: [strides]
    :dedent: 2

Again, just as with the :code:`SimpleDomain`, creating the object specifying the arrays is valid, but not the most readable solution.
The following example shows an alternative way of creation where variables are added along with their cardinality.

.. literalinclude:: ../../src/test/java/ch/idsia/crema/tutorial/DomainsTutorial.java
    :start-after: [domain-builder-strides]
    :end-before: [domain-builder-strides]
    :dedent: 2


Alternative ways to create strides are based on operations on them. Generally Domains are considered unmutable objects and any alteration will result in a new instance.

.. literalinclude:: ../../src/test/java/ch/idsia/crema/tutorial/DomainsTutorial.java
    :start-after: [strides-remove]
    :end-before: [strides-remove]
    :dedent: 2

A number of common set operations are available:

- union
- intersect
- remove

.. _Enhanced domain:

Working with Strides
--------------------
