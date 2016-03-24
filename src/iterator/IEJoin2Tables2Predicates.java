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

	public ArrayList<MyTuple[]> run() {
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
		if (op1 == 4 || op1 == 2) {
			Arrays.sort(L1, descDuration);
			Arrays.sort(L1p, descDuration);
		} else if (op1 == 1 || op1 == 3) {
			Arrays.sort(L1, ascDuration);
			Arrays.sort(L1p, ascDuration);
		}

		if (op2 == 4 || op2 == 2) {
			Arrays.sort(L2, ascCost);
			Arrays.sort(L2p, ascCost);
		} else if (op2 == 1 || op2 == 3) {
			Arrays.sort(L2, descCost);
			Arrays.sort(L2p, descCost);
		}

		// Compute permutation arrays - P, P'
		int i = 0;
		for (MyTuple myTuple : L2)
			p[i++] = findId(L1, myTuple.id); // MyTuple implements Comparable
		i = 0;
		for (MyTuple myTuple : L2p)
			pp[i++] = findId(L1p, myTuple.id); // MyTuple implements Comparable

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
			// int off2 = o2[i];
			for (int j = o2[Math.max(0, i - 1)]; j <= Math.min(o2[i], n - 1); j++) {
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

	private int findId(MyTuple[] a, int id) {
		int i = 0;
		for (MyTuple myTuple : a) {
			if (myTuple.id == id)
				return i;
			i++;
		}
		return -1;
	}

	/**
	 * Finds the position of val in a if found, else position where it would have been found
	 * Checks duration attribute of the tuple
	 * 
	 * @param a Array in which val has to be found
	 * @param val value to be searched
	 * @return index of val in a (if it would have been present)
	 */
	private int findDurationOffset(MyTuple[] a, int val) {
		// Assumption: 'a' has at least one element
		if (a[0].duration <= a[a.length - 1].duration) {
			// a is sorted in ascending order
			int i = 0;
			for (MyTuple myTuple : a) {
				if (val <= myTuple.duration) {
					return i;
				}
				i++;
			}
			return i;
		} else {
			int i = 0;
			for (MyTuple myTuple : a) {
				if (val >= myTuple.duration) {
					return i;
				}
				i++;
			}
			return i;
		}
	}

	/**
	 * Finds the position of val in a if found, else position where it would have been found
	 * Checks cost attribute of the tuple
	 * 
	 * @param a Array in which val has to be found
	 * @param val value to be searched
	 * @return index of val in a (if it would have been present)
	 */
	private int findCostOffset(MyTuple[] a, int val) {
		// Assumption: 'a' has at least one element
		if (a[0].cost <= a[a.length - 1].cost) {
			// a is sorted in ascending order
			int i = 0;
			for (MyTuple myTuple : a) {
				if (val <= myTuple.cost) {
					return i;
				}
				i++;
			}
			return i;
		} else {
			int i = 0;
			for (MyTuple myTuple : a) {
				if (val >= myTuple.cost) {
					return i;
				}
				i++;
			}
			return i;
		}
	}

	public static void main(String[] args) {
		// Table T
		MyTuple t1 = new MyTuple(100, 140, 9);
		MyTuple t2 = new MyTuple(101, 100, 12);
		MyTuple t3 = new MyTuple(102, 90, 5);

		// Table T'
		MyTuple tp1 = new MyTuple(404, 100, 6);
		MyTuple tp2 = new MyTuple(498, 140, 11);
		MyTuple tp3 = new MyTuple(676, 80, 10);
		MyTuple tp4 = new MyTuple(742, 90, 7);

		// Operators Map: 1 for <, 2 for <=, 3 for >= and 4 for >
		IEJoin2Tables2Predicates iejoin = new IEJoin2Tables2Predicates(new MyTuple[] { t1, t2, t3 }, new MyTuple[] { t1, t2, t3 },
				new MyTuple[] { tp1, tp2, tp3, tp4 }, new MyTuple[] { tp1, tp2, tp3, tp4 }, 3, 4, 1, 4);
		// TODO: Incorrect answer for the below case
		iejoin = new IEJoin2Tables2Predicates(new MyTuple[] { t1, t2, t3 }, new MyTuple[] { t1, t2, t3 },
				new MyTuple[] { tp1, tp2, tp3, tp4 }, new MyTuple[] { tp1, tp2, tp3, tp4 }, 3, 4, 4, 1);

		ArrayList<MyTuple[]> result = iejoin.run();
		for (MyTuple[] myTuples : result) {
			System.out.format("[%s, %s]\n", myTuples[0], myTuples[1]);
		}

	}

}