package iterator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

/**
 *
 * This file contains an implementation of the inequality join
 * algorithm as described in the Pappoti paper.
 * 
 * This implementation handles the algroithm mentioned in section 3.1 of the paper
 * 
 * Assumption: All fields in the join predicate are of INTEGER type
 */

public class IEJoin2Tables2Predicates {
	MyTuple[] L1;
	MyTuple[] L2;
	MyTuple[] L1p;
	MyTuple[] L2p;
	int m;
	int n;
	int op1;
	int op2;

	private int[] p, pp, o1, o2, bp;

	public IEJoin2Tables2Predicates(MyTuple[] l1, MyTuple[] l2, MyTuple[] l1p, MyTuple[] l2p, int m, int n, int op1, int op2) {
		super();
		L1 = l1;
		L2 = l2;
		L1p = l1p;
		L2p = l2p;
		this.m = m;
		this.n = n;
		this.op1 = op1;
		this.op2 = op2;

		p = new int[l1.length];
		pp = new int[l1p.length];
		o1 = new int[l1.length];
		o2 = new int[l2.length];
		bp = new int[l1p.length];
	}

	public ArrayList<MyTuple[]> iejoin() {
		ArrayList<MyTuple[]> join_result = new ArrayList<>();
		Comparator<MyTuple> ascDuration = new Comparator<MyTuple>() {
			@Override
			public int compare(MyTuple o1, MyTuple o2) {
				// sort on X
				int diff = o1.duration - o2.duration;
				if (diff > 1)
					return 1;
				else if (diff == 0)
					return 0;
				else
					return -1;
			}
		};
		Comparator<MyTuple> descDuration = new Comparator<MyTuple>() {
			@Override
			public int compare(MyTuple o1, MyTuple o2) {
				// sort on X
				int diff = o1.duration - o2.duration;
				if (diff > 1)
					return -1;
				else if (diff == 0)
					return 0;
				else
					return 1;
			}
		};
		Comparator<MyTuple> ascCost = new Comparator<MyTuple>() {
			@Override
			public int compare(MyTuple o1, MyTuple o2) {
				// sort on X
				int diff = o1.cost - o2.cost;
				if (diff > 1)
					return 1;
				else if (diff == 0)
					return 0;
				else
					return -1;
			}
		};
		Comparator<MyTuple> descCost = new Comparator<MyTuple>() {
			@Override
			public int compare(MyTuple o1, MyTuple o2) {
				// sort on X
				int diff = o1.cost - o2.cost;
				if (diff > 1)
					return -1;
				else if (diff == 0)
					return 0;
				else
					return 1;
			}
		};

		// Compute L1, L1', L2, L2'
		if (op1 == 3 || op1 == 2) {
			Arrays.sort(L1, descDuration);
			Arrays.sort(L1p, descDuration);
		} else if (op1 == 1 || op1 == 4) {
			Arrays.sort(L1, ascDuration);
			Arrays.sort(L1p, ascDuration);
		}

		if (op2 == 3 || op2 == 2) {
			Arrays.sort(L2, ascCost);
			Arrays.sort(L2p, ascCost);
		} else if (op2 == 1 || op2 == 4) {
			Arrays.sort(L2, descCost);
			Arrays.sort(L2p, descCost);
		}

		// Compute permutation arrays - P, P'
		int i = 0;
		for (MyTuple myTuple : L2)
			p[i++] = Arrays.binarySearch(L1, myTuple.id); // MyTuple implements Comparable
		i = 0;
		for (MyTuple myTuple : L2p)
			pp[i++] = Arrays.binarySearch(L1p, myTuple.id); // MyTuple implements Comparable

		// Compute offset arrays - O1, O2
		i = 0;
		for (MyTuple myTuple : L1)
			o1[i++] = findDurationOffset(L1p, myTuple.duration); // find myTuple.duration in L1p
		i = 0;
		for (MyTuple myTuple : L2)
			o2[i++] = findCostOffset(L2p, myTuple.cost); // find myTuple.cost in L2p

		// intialize the bit array, set all to zero

		int eqOff = 0;
		if ((op1 == 2 || op1 == 4) && (op2 == 2 || op2 == 4))
			eqOff = 0;
		else
			eqOff = 1;

		// Visit
		for (i = 0; i < m; i++) {
			int off2 = o2[i];
			for (int j = Math.max(0, o2[i - 1]); j <= o2[i]; j++) { // TODO: Check if equality is required
				bp[pp[j]] = 1;
			}
			int off1 = o1[p[i]];
			for (int k = off1 + eqOff; k < n; k++) { // TODO: check initialization
				if (bp[k] == 1) {
					// add tuples w.r.t. (L2[i],L2p[k]) to join result
					join_result.add(new MyTuple[] { L2[i], L2p[k] });
				}
			}
		}

		return join_result;

	}

	/**
	 * Finds the val in a
	 * 
	 * @param a
	 * @param val
	 * @return
	 */
	private int findDurationOffset(MyTuple[] a, int val) {
		int i = 0;
		for (MyTuple myTuple : a) {
			if (val < myTuple.duration) {
				return i;
			}
			i++;
		}
		return i;
	}

	/**
	 * Finds the val in a
	 * 
	 * @param a
	 * @param val
	 * @return
	 */
	private int findCostOffset(MyTuple[] a, int val) {
		int i = 0;
		for (MyTuple myTuple : a) {
			if (val < myTuple.cost) {
				return i;
			}
			i++;
		}
		return i;
	}

	public static void main(String[] args) {
		// Test the class here
	}

}