package iterator;

import global.AttrType;
import global.GlobalConst;
import global.RID;
import global.SystemDefs;
import heap.Heapfile;
import heap.Tuple;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Vector;

public class IEselfjoin2predicates {
	MyTuple[] L1;
	MyTuple[] L2;
	int m;
	int n;
	int op1;
	int op2;

	private int[] p,bp;

	public IEselfjoin2predicates(MyTuple[] l1, MyTuple[] l2, int m, int n, int op1, int op2) {
		super();
		L1 = l1;
		L2 = l2;
		this.m = m;
		this.n = n;
		this.op1 = op1;
		this.op2 = op2;

		p = new int[l1.length];
		bp = new int[l1.length];
	}

	public /*ArrayList<MyTuple[]>*/ void iejoin() {
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

		// Compute L1,L2
		if (op1 == 3 || op1 == 2) {
			Arrays.sort(L1, descDuration);
		} else if (op1 == 1 || op1 == 4) {
			Arrays.sort(L1, ascDuration);
		}

		if (op2 == 3 || op2 == 2) {
			Arrays.sort(L2, ascCost);
		} else if (op2 == 1 || op2 == 4) {
			Arrays.sort(L2, descCost);
		}

		// Compute permutation arrays - P
		int i = 0;
		for (MyTuple myTuple : L2)
		{
			p[i++] = Arrays.binarySearch(L1,myTuple); // MyTuple implements Comparable
			System.out.println("test");
			System.out.println(p[i-1]);
		}
			

		
		// intialize the bit array, set all to zero

		int eqOff = 0;
		if ((op1 == 2 || op1 == 4) && (op2 == 2 || op2 == 4))
			eqOff = 0;
		else
			eqOff = 1;

		// Visit
		for (i = 0; i < m; i++) {
			int pos = p[i];
			for (int k = pos + eqOff; k < n; k++) { // TODO: check initialization
				if (bp[k] == 1) {
					// add tuples w.r.t. (L2[i],L2p[k]) to join result
					join_result.add(new MyTuple[] { L1[k], L1[i] });
				}
			}
			System.out.println("Saranya is a pandi");
			System.out.println(bp.length);
			System.out.println(pos);
			bp[pos]=1;
		}
		System.out.println(join_result.toString());

	}

	/**
	 * Finds the val in a
	 * 
	 * @param a
	 * @param val
	 * @return
	 */
	/*private int findDurationOffset(MyTuple[] a, int val) {
		int i = 0;
		for (MyTuple myTuple : a) {
			if (val < myTuple.duration) {
				return i;
			}
			i++;
		}
		return i;
	}*/

	/**
	 * Finds the val in a
	 * 
	 * @param a
	 * @param val
	 * @return
	 */
	/*
	private int findCostOffset(MyTuple[] a, int val) {
		int i = 0;
		for (MyTuple myTuple : a) {
			if (val < myTuple.cost) {
				return i;
			}
			i++;
		}
		return i;
	}*/
/*		
	public static void main(String[] argv) {
		// Test the class here
		MyTuple[] L1 = {new MyTuple(1,1,2),new MyTuple(2,2,1)};
		MyTuple[] L2={new MyTuple(1,2,2),new MyTuple(2,2,2)};
		IEselfjoin2predicates t = new IEselfjoin2predicates(L1,L2,2,2,1,2);
		t.iejoin();
		
	}
	*/
}