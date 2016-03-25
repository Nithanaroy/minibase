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
	int n;
	int op1;
	int op2;

	private int[] p,bp;

	public IEselfjoin2predicates(MyTuple[] l1, MyTuple[] l2, int n,int op1, int op2) {
		super();
		L1 = l1;
		L2 = l2;
		this.n = n;
		this.op1 = op1;
		this.op2 = op2;

		p = new int[l1.length];
		bp = new int[l1.length];
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

		// Compute L1,L2
		if (op1 == 4 || op1 == 2) {
			Arrays.sort(L1, descDuration);
		} else if (op1 == 1 || op1 == 3) {
			Arrays.sort(L1, ascDuration);
		}

		if (op2 == 4 || op2 == 2) {
			Arrays.sort(L2, ascCost);
		} else if (op2 == 1 || op2 == 3) {
			Arrays.sort(L2, descCost);
		}

		// Compute permutation arrays - P
		int i = 0;
		for (MyTuple myTuple : L2)
		{
			p[i++] = findId(L1,myTuple.id); // MyTuple implements Comparable
			//System.out.println("test");
			//System.out.println(p[i-1]);
		}
			

		
		// intialize the bit array, set all to zero

		int eqOff = 0;
		if ((op1 == 2 || op1 == 4) && (op2 == 2 || op2 == 4))
			eqOff = 0;
		else
			eqOff = 1;

		// Visit
		for (i = 0; i < n; i++) {
			int pos = p[i];
			for (int k = pos + eqOff; k < n; k++) { // TODO: check initialization
				if (bp[k] == 1) {
					// add tuples w.r.t. (L2[i],L2p[k]) to join result
					join_result.add(new MyTuple[] { L1[k], L1[i] });
				}
			}
			//System.out.println(bp.length);
			System.out.println(pos);
			bp[pos]=1;
		}
		System.out.println(join_result.toString());
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

	public static void main(String[] args) {
		// Table T
		MyTuple t1 = new MyTuple(404, 100, 6);
		MyTuple t2 = new MyTuple(498, 140, 11);
		MyTuple t3 = new MyTuple(676, 80, 10);
		MyTuple t4 = new MyTuple(742, 90, 5); 

		// Table T'
		/*MyTuple tp1 = new MyTuple(404, 100, 6);
		MyTuple tp2 = new MyTuple(498, 140, 11);
		MyTuple tp3 = new MyTuple(676, 80, 10);
		MyTuple tp4 = new MyTuple(742, 90, 7); */

		// Operators Map: 1 for <, 2 for <=, 3 for >= and 4 for >
		IEselfjoin2predicates iejoin = new IEselfjoin2predicates(new MyTuple[] { t1, t2, t3,t4 }, new MyTuple[] { t1, t2, t3,t4 },4,1,4);
		// TODO: Incorrect answer for the below case
		
		iejoin = new IEselfjoin2predicates(new MyTuple[] { t1, t2, t3,t4 }, new MyTuple[] { t1, t2, t3, t4 }, 4,4,1);

		ArrayList<MyTuple[]> result = iejoin.run();
		for (MyTuple[] myTuples : result) {
			System.out.format("[%s, %s]\n", myTuples[0], myTuples[1]);
		}

	}
}
