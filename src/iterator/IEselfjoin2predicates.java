package iterator;

import global.AttrType;
import global.GlobalConst;
import global.RID;
import global.SystemDefs;
import heap.FieldNumberOutOfBoundException;
import heap.Heapfile;
import heap.InvalidTupleSizeException;
import heap.InvalidTypeException;
import heap.Tuple;
import index.BloomFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Comparator;
import java.util.Vector;

public class IEselfjoin2predicates {
	Tuple[] L1;
	Tuple[] L2;
	int n;
	int op1;
	int op2;

	private int[] p;
	private BitSet bp;
	public IEselfjoin2predicates(Tuple[] l1, Tuple[] l2,int op1, int op2) {
		super();
		L1 = l1;
		L2 = l2;
		this.n = l1.length;
		this.op1 = op1;
		this.op2 = op2;

		p = new int[l1.length];
		System.out.println("Lengath of p is "+p.length);
		bp = new BitSet(l1.length);
	}
	// [1] => id, [2] => duration, [3] => cost, [4] and above => others
	public ArrayList<Tuple[]> run() throws FieldNumberOutOfBoundException, IOException {
		// TODO: Decide what to do when exception is raised
		long startTime1 = System.nanoTime();
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

		// Compute L1,L2
		long startTime2 = System.nanoTime();
		if (op1 == 2 || op1 == 1) {
			Arrays.sort(L1, descDuration);
		} else if (op1 == 3 || op1 == 4) {
			Arrays.sort(L1, ascDuration);
		}
	

		if (op2 == 1 || op2 == 2) {
			Arrays.sort(L2, ascCost);
		} else if (op2 == 3 || op2 == 4) {
			Arrays.sort(L2, descCost);
		}
		long endTime2 = System.nanoTime();
		// Compute permutation arrays - P
		int i = 0;
		for (Tuple myTuple : L2) {
			//System.out.println("My tuple is:"+Arrays.toString(myTuple.getTupleByteArray()));
			p[i++] = findId(L1, myTuple.getIntFld(1)); // Tuple implements Comparable// MyTuple implements Comparable
			//System.out.println("test");
			//System.out.println(p[i-1]);
		}	

		
		// intialize the bit array, set all to zero

		int eqOff = 0;
		if ((op1 == 2 || op1 == 3) && (op2 == 2 || op2 == 3))
			eqOff = 0;
		else
			eqOff = 1;
		// Visit
 		long startTime3 = System.nanoTime();
		//usingBitsetNaive(join_result, eqOff);
		usingBitsetOptimized(join_result, eqOff);
		//usingBloomFilter(join_result, eqOff, 2);
		long endTime3 = System.nanoTime();
		long endTime1 = System.nanoTime();
		System.out.println("Time taken Total = "+(endTime1 - startTime1) + " ns"); 
		System.out.println("Time taken preprocessing = "+(endTime2 - startTime2) + " ns"); 
		System.out.println("Time taken Visiting = "+(endTime3 - startTime3) + " ns"); 
		return join_result;
	}
	private void usingBitsetNaive(ArrayList<Tuple[]> join_result, int eqOff) {
		for (int i = 0; i < n; i++) {
			int off2 = p[i];
			bp.set(off2);
			for (int k = off2 + eqOff; k < n; k++) { // TODO: check initialization
				if (bp.get(k)) {
					// add tuples w.r.t. (L2[i],L2p[k]) to join result
					join_result.add(new Tuple[] { L1[k], L1[p[i]] });
				}
			}
		}
	}

	private void usingBloomFilter(ArrayList<Tuple[]> join_result, int eqOff, int reduction_factor) {
		BloomFilter b = new BloomFilter(L1.length, reduction_factor);
		for (int i = 0; i < n; i++) {
			int off2 = p[i];
			bp.set(off2); // = 1;
			b.setBit(off2);
			int k = off2 + eqOff;
			int c = b.nextSetChunk(k); // TODO: check initialization
			while (c >= 0) {
				// traverse the chunk
				k = Math.max(k, c);
				int limit = Math.min(c + reduction_factor + 1, L1.length);
				while (k < limit) {
					if (bp.get(k)) {
						join_result.add(new Tuple[] { L1[k], L1[p[i]] });
					}
					k++;
				}
				c = b.nextSetChunk(k);
			}
		}
	}

	private void usingBitsetOptimized(ArrayList<Tuple[]> join_result, int eqOff) {
		for (int i = 0; i < n; i++) {
			int off2 = p[i];
			bp.set(off2); // = 1;
			int k = bp.nextSetBit(off2 + eqOff); // TODO: check initialization
			while (k >= 0) {
				// add tuples w.r.t. (L1[i],L1[k]) to join result
				join_result.add(new Tuple[] { L1[k], L1[p[i]] });
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
		final int NUMBUF = 50;
		// String dbpath = "/tmp/" + System.getProperty("user.name") + ".minibase.jointestdb";
		// SystemDefs sysdef = new SystemDefs(dbpath, 1000, NUMBUF, "Clock");

		// Setting the types
		AttrType[] Stypes = new AttrType[4];
		Stypes[0] = new AttrType(AttrType.attrInteger);
		Stypes[1] = new AttrType(AttrType.attrInteger);
		Stypes[2] = new AttrType(AttrType.attrInteger);
		Stypes[3] = new AttrType(AttrType.attrInteger);

		// Table T
		Tuple t1 = create(404, 100, 6, Stypes);
		Tuple t2 = create(498, 140, 11, Stypes);
		Tuple t3 = create(676, 80, 10, Stypes);
		Tuple t4 = create(742, 90, 9, Stypes);

		// Operators Map: 1 for <, 2 for <=, 3 for >= and 4 for >
		IEselfjoin2predicates ieselfjoin = new IEselfjoin2predicates(new Tuple[] { t1, t2, t3,t4 }, new Tuple[] { t1, t2, t3,t4 },4,1);
		ieselfjoin.printResults();
		}
	public void printResults() throws FieldNumberOutOfBoundException, IOException {
		ArrayList<Tuple[]> result = null;
		try {
			result = this.run();
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
