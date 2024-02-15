package ch.idsia.crema.core;

import ch.idsia.crema.utility.ArraysMath;
import ch.idsia.crema.utility.ArraysUtil;
import ch.idsia.crema.utility.IndexIterator;
import com.google.common.primitives.Ints;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntShortHashMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.apache.commons.math3.util.FastMath;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * An implementation of the domain that stores also the strides of the
 * variables. As in every other place we assume a global ordering of the
 * variables.
 *
 * <p>
 * Methods are provided to get an index iterator that traverses the domain in a
 * non sorted way. This is mostly used for user interfaces where the user might
 * want a specific ordering (for instance the conditioning order: var |
 * conditioning)
 * </p>
 *
 * <p>
 * While the class should be considered unmutable, when the factor is part of a
 * model and we delete a variable from it, the indices of the variables could be
 * updated accordingly. This applies only to models that do not allow gaps in
 * variable labels.
 * </p>
 *
 * @author davidhuber
 */
public final class Strides implements StridedDomain {
	private final int[] strides;
	final private int combinations;

	final private int[] variables;
	final private int[] sizes;
	final private int size;

	public Strides(Variable[] variables) {

		Arrays.sort(variables);

		strides = new int[variables.length + 1];

		this.variables = Arrays.stream(variables).mapToInt(Variable::getLabel).toArray();
		this.sizes = Arrays.stream(variables).mapToInt(Variable::getCardinality).toArray();
		this.size = variables.length;

		int cumulative = 1;
		strides[0] = 1;

		for (int i = 0; i < size; ++i) {
			strides[i + 1] = cumulative *= sizes[i];
		}
		combinations = cumulative;
	}

	public Strides(int[] variables, int[] sizes, int[] strides) {
		this.variables = variables;
		this.sizes = sizes;
		this.strides = strides;
		this.size = variables.length;
		this.combinations = strides[size];
	}

	/**
	 * create the domain based on the list of variables and their cardinality
	 *
	 * @param variables
	 * @param sizes
	 */
	public Strides(int[] variables, int[] sizes) {
		this.variables = variables;
		this.sizes = sizes;
		this.size = variables.length;
		this.strides = new int[size + 1];

		int cumulative = this.strides[0] = 1;

		for (int i = 0; i < size; ++i) {
			strides[i + 1] = cumulative *= sizes[i];
		}
		this.combinations = cumulative;
	}

	@Override
	public final int indexOf(int variable) {
		// return Arrays.binarySearch(variables, variable);
		return ArraysUtil.indexOf(variable, variables);
	}

	@Override
	public final boolean contains(int variable) {
		return indexOf(variable) >= 0;
	}

	@Override
	public int[] getVariables() {
		return variables;
	}

	@Override
	public int[] getSizes() {
		return sizes;
	}

	@Override
	public int getSize() {
		return size;
	}

	@Override
	public int getCardinality(int variable) {
		int offset = ArrayUtils.indexOf(variables, variable);
		return sizes[offset];
	}

	@Override
	public int getSizeAt(int index) {
		return sizes[index];
	}

	@Override
	public void removed(int variable) {
		int index = -indexOf(variable) - 1;

		// int index = 0;
		// // find where vars > variable start
		// for (; index < variables.length; ++index) {
		// if (variables[index] > variable) break;
		// }

		// change their index
		for (; index < variables.length; ++index) {
			--variables[index];
		}
	}

	public int[] getStrides() {
		return strides;
	}

	public int[] statesOf(int offset) {
		int left_over = offset;
		int[] result = new int[size];
		for (int i = 0; i < size && left_over != 0; ++i) {
			result[i] = left_over % sizes[i];
			left_over = (left_over - result[i]) / (sizes[i]);
		}
		return result;
	}

	public ObservationBuilder observationOf(int offset) {
		return ObservationBuilder.observe(this.getVariables(), this.statesOf(offset));
	}

	/**
	 * Get the offset of the specified variable states using the strides of this
	 * object.
	 *
	 * @param states
	 * @return
	 */
	public int getOffset(int... states) {
		int offset = 0;
		for (int index = 0; index < states.length; ++index) {
			offset += this.strides[index] * states[index];
		}
		return offset;
	}

	/**
	 * get the offset for the var/state pairs specified in the params
	 *
	 * @param vars
	 * @param states
	 * @return
	 */
	public int getPartialOffset(int[] vars, int[] states) {
		int offset = 0;
		int vindex = 0;
		for (int index = 0; index < variables.length; ++index) {
			while (vindex < vars.length && vars[vindex] < variables[index])
				++vindex;
			if (vindex < vars.length && vars[vindex] == variables[index]) {
				offset += this.strides[index] * states[vindex];
				++vindex;
			}
		}
		return offset;
	}

	/**
	 * Get the stride of the specified variable in the domain.
	 *
	 * @param variable
	 * @return
	 */
	public int getStride(int variable) {
		int offset = indexOf(variable);
		return strides[offset];
	}

	/**
	 * Get the stride of the variable of the domain at the specified offset.
	 *
	 * @param index
	 * @return
	 */
	public final int getStrideAt(int index) {
		return strides[index];
	}

	public final int getCombinations() {
		return combinations;
	}

	/**
	 * Create an iterator over an enlarged domain but with same strides and sizes.
	 * 
	 * @param targetDomain
	 * @return
	 * @deprecated please use {@link Strides::getIterator(targetDomain)}
	 */
	@Deprecated
	public final IndexIterator getSupersetIndexIterator(Strides targetDomain) {
		return getSupersetIndexIterator(targetDomain.getVariables(), targetDomain.getSizes(),
				targetDomain.getCombinations());
	}

	private IndexIterator getSupersetIndexIterator(final int[] over, final int[] target_size, int combinations) {
		final int[] target_strides = new int[over.length];

		for (int vindex = 0; vindex < variables.length; ++vindex) {
			int offset = Arrays.binarySearch(over, variables[vindex]);
			if (offset >= 0) {
				target_strides[offset] = strides[vindex];
			}
		}

		return new IndexIterator(target_strides, target_size, combinations);
	}

	/**
	 * Create an iterator over an smaller domain with same strides and sizes and 1
	 * (one) variable set to a specific state.
	 *
	 * @param variable the variable
	 * @param state    the desired state
	 * @return an iterator
	 */
	public IndexIterator getFiteredIndexIterator(final int variable, final int state) {
		int[] new_strides = new int[strides.length - 1];
		int[] new_sizes = new int[sizes.length - 1];

		int var_offset = indexOf(variable);
		System.arraycopy(strides, 0, new_strides, 0, var_offset);
		System.arraycopy(strides, var_offset + 1, new_strides, var_offset, new_strides.length - var_offset);

		System.arraycopy(sizes, 0, new_sizes, 0, var_offset);
		System.arraycopy(sizes, var_offset + 1, new_sizes, var_offset, new_sizes.length - var_offset);

		int offset = strides[var_offset] * state;
		int count = getCombinations() / sizes[var_offset];

		return new IndexIterator(new_strides, new_sizes, offset, count);
	}

	/**
	 * Create an iterator over an smaller domain with same strides and sizes and
	 * some variables set to specific states.
	 *
	 * @param vars   ordered list of variables that the states are for (MUST EXIST
	 *               IN DOMAIN).
	 * @param states list of states for the variable in variables
	 * @return an iterator
	 */
	public IndexIterator getFiteredIndexIterator(final int[] vars, final int[] states) {
		int offset = 0;

		int[] new_strides = new int[strides.length - vars.length];
		int[] new_sizes = new int[sizes.length - vars.length];

		int current_var = 0;
		int size = 1;
		for (int i = 0; i < this.variables.length; ++i) {
			int fixed = current_var < vars.length ? vars[current_var] : -1;
			int variable = this.variables[i];
			int stride = strides[i];

			if (fixed == variable) {
				offset += stride * states[current_var];
				current_var++;
			} else {
				new_strides[i - current_var] = stride;
				new_sizes[i - current_var] = sizes[i];
				size *= sizes[i];
			}
		}
		return new IndexIterator(new_strides, new_sizes, offset, size);
	}

	/**
	 * A helper method that will reuse a domain if it is of some specific type
	 *
	 * @param domain
	 * @return
	 */
	public static Strides fromDomain(Domain domain) {
		if (domain instanceof Strides) {
			return (Strides) domain;
		} else {
			return new Strides(domain.getVariables(), domain.getSizes());
		}
	}

	@Override
	public String toString() {
		return Arrays.toString(variables) + " - " + Arrays.toString(strides);
	}

	/**
	 * Get an IndexIterator for this stride object with a different order for the
	 * variables.
	 *
	 * @param unsorted_vars - int[] the new order for the variables
	 * @return an iterator
	 */
	public IndexIterator getReorderedIterator(int[] unsorted_vars) {

		int[] new_stride = new int[size];
		int[] new_sizes = new int[size];

		for (int i = 0; i < size; ++i) {
			int offset = indexOf(unsorted_vars[i]);
			new_stride[i] = strides[offset];
			new_sizes[i] = sizes[offset];
		}

		return new IndexIterator(new_stride, new_sizes, combinations);
	}

	/**
	 * Get an IndexIterator for this stride object with a different order for the
	 * variables. Alias of intersection.
	 *
	 * @param someVars int[] - the new order for the variables
	 * @return an iterator
	 */
	public Strides retain(int[] someVars) {
		return intersection(someVars);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Remove some variable from this domain. The returned domain will have his own
	 * strides. Variables do not necessarily have to be part of this domain.
	 * Convenience method that calls the {@link Strides#remove(int...)}
	 *
	 * @param toremove Strides - the domain to be removed from the first domain
	 * @return the new, possibly smaller, domain
	 */
	public Strides remove(Strides toremove) {
		return remove(toremove.variables);
	}

	/**
	 * Remove some variable from this domain. The returned domain will have his own
	 * strides. Variables do not necessarily have to be part of "domain".
	 *
	 * @param toremove
	 * @return
	 */
	public Strides remove(int... toremove) {
		Set<Integer> to_rem = Arrays.stream(toremove).boxed().collect(Collectors.toSet());

		int[] vars = new int[this.size];
		int[] sizes = new int[this.size];

		int ti = 0;
		for (int v = 0; v < this.variables.length; ++v) {
			if (!to_rem.contains(this.variables[v])) {
				sizes[ti] = this.sizes[v];
				vars[ti] = this.variables[v];
				ti++;
			} else {
				to_rem.remove(v);
			}
		}

		vars = Arrays.copyOf(vars, ti);
		sizes = Arrays.copyOf(sizes, ti);

		return new Strides(vars, sizes);
	}

	public Strides remove_sorted(int... toremove) {
		int di = 0; // domain index
		int ti = 0; // toremove index
		int index = 0; // target index

		int[] vars = new int[this.size];
		int[] sizes = new int[this.size];

		while (di < this.size && ti < toremove.length) {
			int diff = this.variables[di] - toremove[ti];
			if (diff < 0) {
				vars[index] = this.variables[di];
				sizes[index] = this.sizes[di];
				++index;
				++di;
			} else if (diff > 0) {
				// move to next remve
				++ti;
			} else { // ==0 equal
				++ti;
				++di;
			}
		}

		// all same
		if (di == index)
			return this;

		// copy remaining
		if (di < this.size) {
			int length = this.size - di;

			System.arraycopy(this.variables, di, vars, index, length);
			System.arraycopy(this.sizes, di, sizes, index, length);
			index += length;
		}

		// FIXME: why not use arrays.copyOf
		int[] variables2 = new int[index];
		int[] sizes2 = new int[index];
		System.arraycopy(vars, 0, variables2, 0, index);
		System.arraycopy(sizes, 0, sizes2, 0, index);

		return new Strides(variables2, sizes2);
	}

	/**
	 * Create a new Strides object result of the intersection of this domain with
	 * the given one.
	 *
	 * @param domain2 another domain
	 * @return the intersection of domain1 and domain2
	 */
	public Strides intersection(Strides domain2) {
		return intersection(domain2.variables);
	}

	/**
	 * Create a domain resulting from the intersection of this domain and the
	 * variables domain2.
	 *
	 * @param domain2 int[] - the second domain as a sorted array of variables
	 * @return a new {@link Strides} instance intersection of the two domains
	 */
	public Strides intersection(int... domain2) {

		final int s1 = this.size;
		final int s2 = domain2.length;

		// size the target arrays assuming no overlap
		final int max = FastMath.min(s1, s2);
		int[] intersect_vars = new int[max];
		int[] intersect_sizes = new int[max];

		// (pt1) c1 and c2 are the positions in the two arrays
		int c1 = 0;
		int c2 = 0;

		int t = 0;
		while (c1 < s1 && c2 < s2) {
			int v1 = this.variables[c1];
			int v2 = domain2[c2];

			if (v1 < v2) {
				c1++;
			} else if (v1 > v2) {
				c2++;
			} else {
				intersect_sizes[t] = this.sizes[c1];
				intersect_vars[t++] = v1;
				c1++;
				c2++;
			}
		}

		// fix array size
		if (t < max) {
			intersect_vars = Arrays.copyOf(intersect_vars, t);
			intersect_sizes = Arrays.copyOf(intersect_sizes, t);
		}
		return new Strides(intersect_vars, intersect_sizes);
	}

	public Strides union3(Strides domain2) {

		int[] vars = ArraysUtil.append(variables, domain2.variables);
		int[] sz = ArraysUtil.append(sizes, domain2.sizes);

		TIntSet un = new TIntHashSet(vars);
		// Set<Integer> un = Arrays.stream(vars).boxed().collect(Collectors.toSet());

		int[] union_vars = new int[un.size()];
		int[] union_sizes = new int[un.size()];
		int[] union_strides = new int[un.size() + 1];
		int i = 0;
		union_strides[0] = 1;

		for (int vindex = 0; vindex < vars.length; ++vindex) {
			int v = vars[vindex];
			int s = sz[vindex];

			if (un.contains(v)) {
				union_vars[i] = v;
				union_sizes[i] = s;
				union_strides[i + 1] = union_strides[i] * s;
				++i;
				un.remove(v);
			}
		}

		return new Strides(union_vars, union_sizes, union_strides);
	}

	public Strides union_unsorted(Strides domain2) {

		int s1 = variables.length;
		int s2 = domain2.variables.length;

		int[] vars = ArraysUtil.append(variables, domain2.variables);
		int[] sz = ArraysUtil.append(sizes, domain2.sizes);

		int s = s1 + s2;

		int[] union_vars = new int[s];
		int[] union_sz = new int[s];
		int[] union_str = new int[s + 1];

		union_str[0] = 1;
		int t = 0;
		external: for (int v1 = 0; v1 < vars.length; ++v1) {
			for (int v2 = v1 - 1; v2 >= 0; --v2) {
				if (vars[v1] == vars[v2])
					continue external;
			}
			union_vars[t] = vars[v1];
			union_sz[t] = sz[v1];
			union_str[t + 1] = union_str[t] * sz[v1];
			t++;
		}

		return new Strides(Arrays.copyOf(union_vars, t), Arrays.copyOf(union_sz, t), Arrays.copyOf(union_str, t + 1));
	}

	public Strides union_new(Strides domain2) {

		int s1 = variables.length;
		int s2 = domain2.variables.length;

		int[] vars = ArraysUtil.append(variables, domain2.variables);
		int[] order = ArraysUtil.order(vars);

		vars = ArraysUtil.at(vars, order);
		int[] sz = ArraysUtil.append(sizes, domain2.sizes);
		sz = ArraysUtil.at(sz, order);

		int prev = vars[0];
		int s = 1;

		for (int v : vars) {
			if (v != prev)
				++s;
			prev = v;
		}

		int[] union_vars = new int[s];
		int[] union_sz = new int[s];
		int[] union_str = new int[s + 1];

		union_str[0] = 1;

		int t = 0;

		prev = vars[0] ^ 1; // make sure the value in prev is different than the first item
		for (int i = 0; i < vars.length; ++i) {
			if (prev != vars[i]) {
				union_vars[t] = vars[i];
				union_sz[t] = sz[i];
				union_str[t + 1] = union_str[t] * sz[i];
				t++;
			}
		}

		return new Strides(union_vars, union_sz, union_str);
	}

	/**
	 * Create a new Strides result of the union of domain1 and domain2.
	 *
	 * <p>
	 * The implementation requires ordering but allows overlaps
	 * </p>
	 *
	 * <p>
	 * As we can assume ordering we will traverse the domains in parallel (pt1 in
	 * the code) and copy to a target domain the values. When one of the domains
	 * reached it's end the remaing variable of the other domain can be added to the
	 * union in bulk (pt2 in code).
	 * </p>
	 *
	 * @param domain2
	 * @return
	 */
	public Strides union(Strides domain2) {

		final int s1 = this.size;
		final int s2 = domain2.size;

		// size the target arrays assuming no overlap
		final int max = s1 + s2;
		int[] union_vars = new int[max];
		int[] union_sizes = new int[max];

		// (pt1) c1 and c2 are the positions in the two domains
		int c1 = 0;
		int c2 = 0;

		int t = 0;
		while (c1 < s1 && c2 < s2) {
			int v1 = this.variables[c1];
			int v2 = domain2.variables[c2];

			if (v1 < v2) {
				union_sizes[t] = this.sizes[c1];
				union_vars[t] = v1;
				c1++;
			} else if (v1 > v2) {
				union_sizes[t] = domain2.sizes[c2];
				union_vars[t] = v2;
				c2++;
			} else {
				union_sizes[t] = this.sizes[c1];
				union_vars[t] = v1;
				c1++;
				c2++;
			}
			++t;
		}

		// (pt2) check if there is one domain not completely copied that can be
		// moved over in bulk.
		if (c1 < s1) {
			System.arraycopy(this.variables, c1, union_vars, t, s1 - c1);
			System.arraycopy(this.sizes, c1, union_sizes, t, s1 - c1);
			t += s1 - c1;
		} else if (c2 < s2) {
			System.arraycopy(domain2.variables, c2, union_vars, t, s2 - c2);
			System.arraycopy(domain2.sizes, c2, union_sizes, t, s2 - c2);
			t += s2 - c2;
		}

		// fix array sizes if there was overlap (we assumed no overlap while
		// sizing)
		if (t < max) {
			union_sizes = Arrays.copyOf(union_sizes, t);
			union_vars = Arrays.copyOf(union_vars, t);
		}
		return new Strides(union_vars, union_sizes);
	}

	/**
	 * Concatenate 2 strides
	 *
	 * @param right
	 * @return
	 */
	public Strides concat(Strides right) {
		Strides left = this;

		int[] strides_left = new int[left.getSize() * 2];
		int[] strides_right = new int[right.getSize() * 2];

		for (int i = 0; i < left.getSize(); i++) {
			int v = left.getVariables()[i];
			strides_left[i * 2] = v;
			strides_left[i * 2 + 1] = left.getCardinality(v);
		}

		for (int i = 0; i < right.getSize(); i++) {
			int v = right.getVariables()[i];
			strides_right[i * 2] = v;
			strides_right[i * 2 + 1] = right.getCardinality(v);

		}

		return Strides.as(Ints.concat(strides_left, strides_right));

	}

	/**
	 * Creates a stride with one or more variables excluded. Variables may be
	 * missing
	 *
	 * @param offset
	 */
	public Strides removeAt(int offset) {

		int[] tvariables = new int[size - 1];
		int[] tsizes = new int[size - 1];
		int[] tstrides = new int[size];

		System.arraycopy(variables, 0, tvariables, 0, offset);
		System.arraycopy(sizes, 0, tsizes, 0, offset);

		if (offset < size - 1) {
			System.arraycopy(variables, offset + 1, tvariables, offset, size - offset - 1);
			System.arraycopy(sizes, offset + 1, tsizes, offset, size - offset - 1);
		}

		System.arraycopy(strides, 0, tstrides, 0, offset + 1);
		System.arraycopy(strides, offset + 2, tstrides, offset + 1, size - offset - 1);
		int fix = sizes[offset];
		for (int o = offset + 1; o < size; ++o) {
			tstrides[o] /= fix;
		}
		return new Strides(tvariables, tsizes, tstrides);
	}

	/**
	 * Creates a stride with one or more variables excluded. Variables may be
	 * missing
	 *
	 * @param offset
	 */
	public Strides removeAt(int... offset) {
		int tsize = this.size - offset.length;
		int[] tvariables = new int[tsize];
		int[] tsizes = new int[tsize];
		int o = 0, t = 0;

		for (int i = 0; i < this.size; ++i) {
			while (o < offset.length && offset[o] < i)
				++o;

			if (o < offset.length && offset[o] == i)
				continue;

			tvariables[t] = this.variables[i];
			tsizes[t] = this.sizes[i];
			++t;
		}
		return new Strides(tvariables, tsizes);
	}

	/**
	 * Get an iterator of this domain with the specified variables lock in state 0.
	 * <p>
	 * Convenience method when the target domain is this domain. same as calling
	 * getIterator(this, locked);
	 *
	 * @param locked
	 * @return
	 */
	public IndexIterator getIterator(int... locked) {
		return getIterator(this, locked);
	}

	/**
	 * Iterate over another domain. If a variable is not present in this domain it
	 * will not move the index but it will take the step. If a variable is not in
	 * the specified domain the variable is not considered or is assumed fixed to 0.
	 * Please use getPartialOffset(vars, state) for a different offset. If also
	 * present in str, variable in the locked array will be counted but fixed at
	 * zero. This allows us to keep them in the target domain while not moving.
	 *
	 * @param str    the target domain
	 * @param locked the variables that should be locked
	 * @return
	 */
	public IndexIterator getIterator(Strides str, int... locked) {
		int[] new_strides = new int[str.getSize()];

		int source = 0;
		int lock = 0;
		for (int target = 0; target < str.getSize(); ++target) {
			// align with target
			while (source < variables.length && variables[source] < str.variables[target])
				++source;
			while (lock < locked.length && locked[lock] < str.variables[target])
				++lock;

			// they match!
			if (source < variables.length && variables[source] == str.variables[target]) {
				if (lock >= locked.length || locked[lock] != str.variables[target]) {
					// not locked
					new_strides[target] = this.strides[source];
				} else { // var was locked so lock actually matched target so we
					// move lock
					++lock;
				}
				++source; // in any case we move the source pointer
			} // else {
				// source var is greater than target (var is not found in this
				// domain)
				// so no need to copy strides, we can leave them at the default
				// value of 0
				// }
		}

		return new IndexIterator(new_strides, str.getSizes(), 0, str.getCombinations());
	}

	/**
	 * Define a stride as a sequence of variable, state pairs. Eg. to define a
	 * domain over two ternary variables 4 and 3 one would call:
	 * {@code Strides.as(3,3, 4,3);}
	 * <p>
	 * Note that the variables must be ordered!
	 * </p>
	 *
	 * @param data
	 * @return
	 */
	public static Strides as(int... data) {
		if (data.length % 2 == 1)
			throw new IllegalArgumentException("Need pars of var,size");

		int size = data.length / 2;
		int[] variables = new int[size];
		int[] sizes = new int[size];

		for (int i = 0; i < size; ++i) {
			variables[i] = data[i * 2];
			sizes[i] = data[i * 2 + 1];
		}
		return new Strides(variables, sizes);
	}

	/**
	 * Define a stride as a sequence of variable/size pairs
	 */
	public static Strides var(int var, int size) {
		return new Strides(new int[] { var }, new int[] { size });
	}

	/**
	 * Helper to allow concatenation of var().add().add() Insertion is sorted.
	 * Resulting domain is ordered!
	 * 
	 * @param var
	 * @param size
	 * @return
	 */
	public Strides and(int var, int size) {
		int pos = Arrays.binarySearch(variables, var);
		if (pos >= 0)
			return this; // no need to change anything
		pos = -(pos + 1);

		int[] newvar = new int[variables.length + 1];
		System.arraycopy(variables, 0, newvar, 0, pos);
		newvar[pos] = var;
		System.arraycopy(variables, pos, newvar, pos + 1, variables.length - pos);

		int[] newsize = new int[variables.length + 1];
		System.arraycopy(sizes, 0, newsize, 0, pos);
		newsize[pos] = size;
		System.arraycopy(sizes, pos, newsize, pos + 1, sizes.length - pos);

		return new Strides(newvar, newsize);
	}

	/**
	 * The empty stride
	 */
	public static Strides EMPTY = Strides.as();

	/**
	 * Return an empty Stride with no variables and a single entry in the strides
	 * array. This only stride value is set to 1.
	 *
	 * @return {@link Strides} - the empty stride
	 */
	public static Strides empty() {
		return EMPTY;
	}

	/**
	 * Return a new Stride sorted by the variables.
	 *
	 * @return
	 */
	public Strides sort() {
		int[] order = IntStream.range(0, size).boxed()
				.sorted((a, b) -> Strides.this.variables[a] - Strides.this.variables[b]).mapToInt(x -> x).toArray();
		int[] variables = Arrays.stream(order).map(x -> Strides.this.variables[x]).toArray();
		int[] sizes = Arrays.stream(order).map(x -> Strides.this.sizes[x]).toArray();
		return new Strides(variables, sizes);
	}

	public IndexIterator getIterator(Strides domain, TIntIntMap observation) {
		int[] states = new int[size];

		for (int var : observation.keys()) {
			int index = indexOf(var);
			if (index >= 0) {
				states[index] = observation.get(var);
			}
		}

		int offset = getOffset(states);
		IndexIterator it = getIterator(domain, ArraysUtil.sort(observation.keys()));
		it.offset(offset);

		return it;
	}

	/**
	 * Determines if this Strides object is consistent other. Two Strides objects
	 * are consistent if all the common variables have the same cardinality.
	 *
	 * @param other {@link Strides} - other Strides object to compare with.
	 * @return boolean variable indicating the compatibility.
	 */
	public boolean isConsistentWith(Strides other) {
		for (int v : this.intersection(other).getVariables())
			if (this.getCardinality(v) != other.getCardinality(v))
				return false;
		return true;
	}

	/**
	 * get the list of states for the specified offset within all the combinations
	 * of the domain
	 */
	public int[] getStatesFor(int offset) {
		int[] states = new int[getSize()];
		int index = 0;
		for (int cardinality : sizes) {
			states[index++] = offset % cardinality;
			offset = offset / cardinality;
		}
		return states;
	}

	/**
	 * Test wether the specified index (within the exanded domain) is present in the
	 * provied observations map
	 * 
	 * @param index
	 * @param obs
	 * @return
	 */
	public boolean isCompatible(int index, TIntIntMap obs) {
		int[] obsfiltered = IntStream.of(this.getVariables()).sorted().map(x -> {
			if (obs.containsKey(x))
				return obs.get(x);
			else
				return -1;
		}).toArray();

		int[] states = this.statesOf(index);

		boolean compatible = true;
		for (int i = 0; i < this.getVariables().length; i++) {
			if (obsfiltered[i] >= 0 && obsfiltered[i] != states[i]) {
				compatible = false;
				break;
			}
		}

		return compatible;
	}

	/**
	 * Get all indices that are addressed by the specified observations map
	 * 
	 * @param obs
	 * @return
	 */
	public int[] getCompatibleIndexes(TIntIntMap obs) {
		return IntStream.range(0, this.getCombinations()).filter(i -> this.isCompatible(i, obs)).toArray();
	}

	public static Strides reverseDomain(Strides domain) {
		int[] vars = domain.getVariables().clone();
		int[] sizes = domain.getSizes().clone();
		ArrayUtils.reverse(vars);
		ArrayUtils.reverse(sizes);
		return new Strides(vars, sizes);
	}

	public Strides reverseDomain() {
		return reverseDomain(this);
	}

	public static void main(String[] args) {
		RandomDataGenerator generator = new RandomDataGenerator();
		int[] var1 = generator.nextPermutation(20, 10);
		int[] var2 = generator.nextPermutation(20, 10);
		int[] s1 = IntStream.range(0, 10).map(x -> 2).toArray();
		int[] s2 = IntStream.range(0, 10).map(x -> 2).toArray();
		Strides st1 = new Strides(var1, s1);
		Strides st2 = new Strides(var2, s2);

		int runs = 10000;
		int reps = 20;
		long[] times = new long[reps];
		long useless = 0;
		for (int rep = 0; rep < reps * 2; ++rep) {
			long time = System.nanoTime();
			for (int i = 0; i < runs; ++i) {
				var ss3 = st1.union(st2);
				int[] xx = ss3.getVariables();
				int x= ArraysMath.min(xx);
				useless +=x;
			}
			long delta = System.nanoTime() - time;
			if (rep > reps) {
				times[rep - reps] = delta;
				
			}
		}
		System.out.println(ArraysMath.mean(times) + " " + ArraysMath.sd(times, 1));
	}
}
