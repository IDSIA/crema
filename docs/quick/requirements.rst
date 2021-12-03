Requirements
============

System
------

Build the Crema library requires Java 11 or higher and Maven (`https://maven.apache.org <https://maven.apache.org>`_).

Tests have been done under Linux Ubuntu, Windows 10, and macOS with openjdk 11, 12, and 16. Continuous integration tests
are done using Ubuntu Latest and JDK 11 via `GitHub Actions <https://github.com/IDSIA/crema/actions>`_.

Package Dependencies
--------------------

Crema contains the dependencies shown below which are managed using Maven.

- ch.javasoft.polco:polco:jar:4.7.1:compile
- colt:colt:jar:1.2.0:compile
- com.github.quickhull3d:quickhull3d:jar:1.0.0:compile
- com.google.code.findbugs:jsr305:jar:3.0.2:compile
- com.google.errorprone:error_prone_annotations:jar:2.3.4:compile
- com.google.guava:failureaccess:jar:1.0.1:compile
- com.google.guava:guava:jar:28.2-jre:compile
- com.google.guava:listenablefuture:jar:9999.0-empty-to-avoid-conflict-with-guava:compile
- com.google.j2objc:j2objc-annotations:jar:1.3:compile
- com.joptimizer:joptimizer:jar:3.5.1:compile
- com.opencsv:opencsv:jar:5.2:compile
- commons-beanutils:commons-beanutils:jar:1.9.4:compile
- commons-cli:commons-cli:jar:1.4:compile
- commons-collections:commons-collections:jar:3.2.2:compile
- commons-logging:commons-logging:jar:1.2:compile
- concurrent:concurrent:jar:1.3.4:compile
- javax.validation:validation-api:jar:1.1.0.Final:compile
- junit:junit:jar:4.13.1:compile
- log4j:log4j:jar:1.2.14:compile
- net.sf.lpsolve:lp_solve:jar:5.5.2:compile
- net.sf.trove4j:trove4j:jar:3.0.3:compile
- net.sourceforge.csparsej:csparsej:jar:1.1.1:compile
- org.apache.commons:commons-collections4:jar:4.4:compile
- org.apache.commons:commons-csv:jar:1.3:compile
- org.apache.commons:commons-lang3:jar:3.4:compile
- org.apache.commons:commons-math3:jar:3.6.1:compile
- org.apache.commons:commons-text:jar:1.8:compile
- org.apiguardian:apiguardian-api:jar:1.0.0:test
- org.checkerframework:checker-qual:jar:2.10.0:compile
- org.eclipse.persistence:org.eclipse.persistence.asm:jar:2.6.2:compile
- org.eclipse.persistence:org.eclipse.persistence.core:jar:2.6.2:compile
- org.glassfish:javax.json:jar:1.0.4:compile
- org.hamcrest:hamcrest-core:jar:1.3:compile
- org.jgrapht:jgrapht-core:jar:1.1.0:compile
- org.junit.jupiter:junit-jupiter-api:jar:5.4.2:test
- org.junit.jupiter:junit-jupiter-params:jar:5.4.2:test
- org.junit.platform:junit-platform-commons:jar:1.4.2:test
- org.opentest4j:opentest4j:jar:1.1.1:test
- org.slf4j:slf4j-api:jar:1.7.7:compile


External Dipendencies
-------------------------

In order to compile Crema from source code, two dependencies not available in Maven repositories need to be installed
manually.

lpsolve

.. code-block:: java

    mvn org.apache.maven.plugins:maven-dependency-plugin:3.1.2:get -DgroupId=net.sf.lpsolve -DartifactId=lp_solve -Dversion=5.5.2 -Dpackaging=jar -DremoteRepositories=https://raw.github.com/idsia/crema/mvn-repo/

polco

.. code-block:: java

    mvn org.apache.maven.plugins:maven-dependency-plugin:3.1.2:get -DgroupId=ch.javasoft.polco -DartifactId=polco -Dversion=4.7.1 -Dpackaging=jar -DremoteRepositories=https://raw.github.com/idsia/crema/mvn-repo/
