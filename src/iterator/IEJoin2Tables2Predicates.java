package iterator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Comparator;

import global.AttrType;
import heap.FieldNumberOutOfBoundException;
import heap.InvalidTupleSizeException;
import heap.InvalidTypeException;
import heap.Tuple;
import index.BloomFilter;

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
	Tuple[] L1;
	Tuple[] L2;
	Tuple[] L1p;
	Tuple[] L2p;
	int m;
	int n;
	int op1;
	int op2;

	private int[] p, pp, o1, o2;
	private BitSet bp;

	public IEJoin2Tables2Predicates(Tuple[] l1, Tuple[] l2, Tuple[] l1p, Tuple[] l2p, int m, int n, int op1, int op2) {
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
		bp = new BitSet(l1p.length);
	}

	// [1] => id, [2] => duration, [3] => cost, [4] and above => others
	public ArrayList<Tuple[]> run() throws FieldNumberOutOfBoundException, IOException {
		// TODO: Decide what to do when exception is raised
		ArrayList<Tuple[]> join_result = new ArrayList<>();
		Comparator<Tuple> ascDuration = new Comparator<Tuple>() {
			@Override
			public int compare(Tuple o1, Tuple o2) {
				// sort on X
				int diff = 0;
				try {
					diff = o1.getIntFld(2) - o2.getIntFld(2);

				} catch (FieldNumberOutOfBoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				if (diff > 1)
					return 1;
				else if (diff == 0)
					return 0;
				else
					return -1;
			}
		};
		Comparator<Tuple> descDuration = new Comparator<Tuple>() {
			@Override
			public int compare(Tuple o1, Tuple o2) {
				// sort on X
				int diff = 0;
				try {
					diff = o1.getIntFld(2) - o2.getIntFld(2);
				} catch (FieldNumberOutOfBoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				if (diff > 1)
					return -1;
				else if (diff == 0)
					return 0;
				else
					return 1;
			}
		};
		Comparator<Tuple> ascCost = new Comparator<Tuple>() {
			@Override
			public int compare(Tuple o1, Tuple o2) {
				// sort on X
				int diff = 0;
				try {
					diff = o1.getIntFld(3) - o2.getIntFld(3);
				} catch (FieldNumberOutOfBoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				if (diff > 1)
					return 1;
				else if (diff == 0)
					return 0;
				else
					return -1;
			}
		};
		Comparator<Tuple> descCost = new Comparator<Tuple>() {
			@Override
			public int compare(Tuple o1, Tuple o2) {
				// sort on X
				int diff = 0;
				try {
					diff = o1.getIntFld(3) - o2.getIntFld(3);
				} catch (FieldNumberOutOfBoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				if (diff > 1)
					return -1;
				else if (diff == 0)
					return 0;
				else
					return 1;
			}
		};

		// Compute L1, L1', L2, L2'
		if (op1 == 3 || op1 == 4) {
			Arrays.sort(L1, descDuration);
			Arrays.sort(L1p, descDuration);
		} else if (op1 == 1 || op1 == 2) {
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
		for (Tuple myTuple : L2)
			p[i++] = findId(L1, myTuple.getIntFld(1)); // Tuple implements Comparable
		i = 0;
		for (Tuple myTuple : L2p)
			pp[i++] = findId(L1p, myTuple.getIntFld(1)); // Tuple implements Comparable

		// Compute offset arrays - O1, O2
		i = 0;
		for (Tuple myTuple : L1)
			o1[i++] = findDurationOffset(L1p, myTuple.getIntFld(2)); // find myTuple.duration in L1p
		i = 0;
		for (Tuple myTuple : L2)
			o2[i++] = findCostOffset(L2p, myTuple.getIntFld(3)); // find myTuple.cost in L2p

		// intialize the bit array, set all to zero

		int eqOff = 0;
		if ((op1 == 2 || op1 == 3) && (op2 == 2 || op2 == 3))
			eqOff = 0;
		else
			eqOff = 1;

		// Visit
		// usingBitsetNaive(join_result, eqOff);
		// usingBitsetOptimized(join_result, eqOff);
		usingBloomFilter(join_result, eqOff, 2);

		return join_result;

	}

	private void usingBitsetNaive(ArrayList<Tuple[]> join_result, int eqOff) {
		for (int i = 0; i < m; i++) {
			int off2 = o2[i];
			for (int j = 0; j <= Math.min(off2, n - 1); j++) {
				bp.set(pp[j]); // = 1;
			}
			int off1 = o1[p[i]];
			for (int k = off1 + eqOff; k < n; k++) { // TODO: check initialization
				if (bp.get(k)) {
					// add tuples w.r.t. (L2[i],L2p[k]) to join result
					join_result.add(new Tuple[] { L2[i], L2p[k] });
				}
			}
		}
	}

	private void usingBloomFilter(ArrayList<Tuple[]> join_result, int eqOff, int reduction_factor) {
		BloomFilter b = new BloomFilter(L1p.length, reduction_factor);
		for (int i = 0; i < m; i++) {
			int off2 = o2[i];
			for (int j = 0; j <= Math.min(off2, n - 1); j++) {
				bp.set(pp[j]); // = 1;
				b.setBit(pp[j]);
			}
			int off1 = o1[p[i]];
			int k = off1 + eqOff;
			int c = b.nextSetChunk(k); // TODO: check initialization
			while (c >= 0) {
				// traverse the chunk
				k = Math.max(k, c);
				int limit = Math.min(c + reduction_factor + 1, L1p.length);
				while (k < limit) {
					if (bp.get(k)) {
						join_result.add(new Tuple[] { L2[i], L2p[k] });
					}
					k++;
				}
				c = b.nextSetChunk(k);
			}
		}
	}

	private void usingBitsetOptimized(ArrayList<Tuple[]> join_result, int eqOff) {
		for (int i = 0; i < m; i++) {
			int off2 = o2[i];
			for (int j = 0; j <= Math.min(off2, n - 1); j++) {
				bp.set(pp[j]); // = 1;
			}
			int off1 = o1[p[i]];
			int k = bp.nextSetBit(off1 + eqOff); // TODO: check initialization
			while (k >= 0) {
				// add tuples w.r.t. (L2[i],L2p[k]) to join result
				join_result.add(new Tuple[] { L2[i], L2p[k] });
				k = bp.nextSetBit(k + 1);
			}
		}
	}

	private int findId(Tuple[] a, int id) throws FieldNumberOutOfBoundException, IOException {
		int i = 0;
		for (Tuple myTuple : a) {
			if (myTuple.getIntFld(1) == id)
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
	 * @throws IOException
	 * @throws FieldNumberOutOfBoundException
	 */
	private int findDurationOffset(Tuple[] a, int val) throws FieldNumberOutOfBoundException, IOException {
		// Assumption: 'a' has at least one element
		if (a[0].getIntFld(2) <= a[a.length - 1].getIntFld(2)) {
			// a is sorted in ascending order
			int i = 0;
			for (Tuple myTuple : a) {
				if (val <= myTuple.getIntFld(2)) {
					return i;
				}
				i++;
			}
			return i;
		} else {
			int i = 0;
			for (Tuple myTuple : a) {
				if (val >= myTuple.getIntFld(2)) {
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
	 * @throws IOException
	 * @throws FieldNumberOutOfBoundException
	 */
	private int findCostOffset(Tuple[] a, int val) throws FieldNumberOutOfBoundException, IOException {
		// Assumption: 'a' has at least one element
		if (a[0].getIntFld(3) <= a[a.length - 1].getIntFld(3)) {
			// a is sorted in ascending order
			int i = 0;
			for (Tuple myTuple : a) {
				if (val <= myTuple.getIntFld(3)) {
					return i;
				}
				i++;
			}
			return i;
		} else {
			int i = 0;
			for (Tuple myTuple : a) {
				if (val >= myTuple.getIntFld(3)) {
					return i;
				}
				i++;
			}
			return i;
		}
	}

	public static Tuple create(int id, int duration, int cost, AttrType[] Stypes)
			throws FieldNumberOutOfBoundException, IOException, InvalidTypeException, InvalidTupleSizeException {
		Tuple t = new Tuple();
		t.setHdr((short) 4, Stypes, null);
		t.setIntFld(1, id);
		t.setIntFld(2, duration);
		t.setIntFld(3, cost);
		return t;
	}

	public static String tupleToString(Tuple t) throws FieldNumberOutOfBoundException, IOException {
		return String.format("[%d, %d, %d]", t.getIntFld(1), t.getIntFld(2), t.getIntFld(3));
	}

	public static void main(String[] args)
			throws FieldNumberOutOfBoundException, IOException, InvalidTypeException, InvalidTupleSizeException {
		// Setting the types
		AttrType[] Stypes = new AttrType[4];
		Stypes[0] = new AttrType(AttrType.attrInteger);
		Stypes[1] = new AttrType(AttrType.attrInteger);
		Stypes[2] = new AttrType(AttrType.attrInteger);
		Stypes[3] = new AttrType(AttrType.attrInteger);

		// Table T
		Tuple t1 = create(100, 140, 9, Stypes);
		Tuple t2 = create(101, 100, 12, Stypes);
		Tuple t3 = create(102, 90, 5, Stypes);

		// Table T'
		Tuple tp1 = create(404, 100, 6, Stypes);
		Tuple tp2 = create(498, 140, 11, Stypes);
		Tuple tp3 = create(676, 80, 10, Stypes);
		Tuple tp4 = create(742, 90, 7, Stypes);

		// Operators Map: 1 for <, 2 for <=, 3 for >= and 4 for >
		System.out.println("Query: Less time, More cost");
		IEJoin2Tables2Predicates iejoin = new IEJoin2Tables2Predicates(new Tuple[] { t1, t2, t3 }, new Tuple[] { t1, t2, t3 },
				new Tuple[] { tp1, tp2, tp3, tp4 }, new Tuple[] { tp1, tp2, tp3, tp4 }, 3, 4, 1, 4);
		// TODO: Incorrect answer for the below case
		// System.out.println("Query: More time, Less cost");
		// iejoin = new IEJoin2Tables2Predicates(new Tuple[] { t1, t2, t3 }, new Tuple[] { t1, t2, t3 }, new Tuple[] { tp1, tp2, tp3, tp4 },
		// new Tuple[] { tp1, tp2, tp3, tp4 }, 3, 4, 4, 1);

		ArrayList<Tuple[]> result = null;
		try {
			result = iejoin.run();
		} catch (FieldNumberOutOfBoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		for (Tuple[] myTuples : result) {
			System.out.format("[%s, %s]\n", tupleToString(myTuples[0]), tupleToString(myTuples[1]));
		}

	}

}