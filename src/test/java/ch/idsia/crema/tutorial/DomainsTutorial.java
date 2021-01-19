package ch.idsia.crema.tutorial;

import ch.idsia.crema.core.Domain;
import ch.idsia.crema.core.DomainBuilder;
import ch.idsia.crema.core.SimpleDomain;
import ch.idsia.crema.core.Strides;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotSame;

public class DomainsTutorial {

	@Test
	public void testDomain() {
		Domain domain = new SimpleDomain(new int[]{1, 2}, new int[]{2, 4});
		domain.getSizes();
		domain.getVariables();
	}

	@Test
	public void testSimpleDomain() {
		// [simple-domain]
		Domain domain = new SimpleDomain(
				new int[]{1, 4, 6}, // variables 1, 4, 6
				new int[]{3, 2, 3}  // the corresponding cardinalities
		);

		assertTrue(domain.contains(6));
		// [simple-domain]


		// [domain-builder-1]
		Domain dom = DomainBuilder.var(1, 4, 6).size(3, 2, 3);
		// [domain-builder-1]

	}

	@Test
	public void testStridesCreation() {
		// [strides]
		Strides domain = new Strides(
				new int[]{1, 4, 6}, // variables 1, 4, 6
				new int[]{3, 2, 3}  // the corresponding cardinalities
		);
		// [strides]

		// [domain-builder-strides]
		Strides other = Strides.as(1, 3).and(4, 2).and(6, 3);
		// [domain-builder-strides]

		// [strides-remove]
		// remove variable 4 and 6
		Strides smaller = domain.remove(4, 6);
		// [strides-remove]

		assertNotSame(smaller, domain);
	}
}
