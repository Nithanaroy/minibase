package iterator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import global.AttrType;
import global.SystemDefs;
import heap.FieldNumberOutOfBoundException;
import heap.InvalidTupleSizeException;
import heap.InvalidTypeException;
import heap.Tuple;
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

public class IESelfJoin1Predicate {
	Tuple[] L1;
	int n;
	int op1;
	static int p;
	public IESelfJoin1Predicate(Tuple[] l1,int op1,int proj) {
		this(l1,l1.length,op1,proj);
	}

	//l1 array of tuples, n size of table, op1 inequality operator
	public IESelfJoin1Predicate(Tuple[] l1,int n, int op1,int proj) {
		super();
		L1 = l1;
		this.n = n;
		this.op1 = op1;
		this.p = proj;
	}

	// [1] => id, [2] => duration, [3] => cost, [4] and above => others
	public ArrayList<Tuple[]> run() throws FieldNumberOutOfBoundException, IOException {
		long startTime1 = System.nanoTime();
		// TODO: Decide what to do when exception is raised
		ArrayList<Tuple[]> join_result = new ArrayList();
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

		// Compute L1, L1', L2, L2'
		long startTime2 = System.nanoTime();
		if (op1 == 3 || op1 == 4) {
			Arrays.sort(L1, descDuration);
		} else if (op1 == 1 || op1 == 2) {
			Arrays.sort(L1, ascDuration);
		}
		long endTime2 = System.nanoTime();


		long startTime3 = System.nanoTime();
		// Visit
		if(op1 == 3 || op1 == 2){
			for (int i = 0; i < n; i++) {
				for(int j = i; j < n; j++) {
					join_result.add(new Tuple[] { L1[i], L1[j] });
				}
			}
		}
		else if(op1 == 4 || op1 == 1){
			for (int i = 0; i < n; i++) {
				for(int j = i+1; j < n; j++) {
					if(L1[i].getIntFld(2)!=L1[j].getIntFld(2))
						join_result.add(new Tuple[] { L1[i], L1[j] });
				}
			}
		}
		long endTime3 = System.nanoTime();
		long endTime1 = System.nanoTime();
		System.out.println("Time taken Total = "+(endTime1 - startTime1) + " ns"); 
		System.out.println("Time taken preprocessing = "+(endTime2 - startTime2) + " ns"); 
		System.out.println("Time taken Visiting = "+(endTime3 - startTime3) + " ns"); 

		return join_result;

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
		return String.format("[%d]", t.getIntFld(p));
	}

	public static void main(String[] args)  throws FieldNumberOutOfBoundException, IOException, InvalidTypeException, InvalidTupleSizeException{
		AttrType[] Stypes = new AttrType[4];
		Stypes[0] = new AttrType(AttrType.attrInteger);
		Stypes[1] = new AttrType(AttrType.attrInteger);
		Stypes[2] = new AttrType(AttrType.attrInteger);
		Stypes[3] = new AttrType(AttrType.attrInteger);

		// Table T'
		Tuple t1 = create(1, 795743, 79574300, Stypes);
		Tuple t2 = create(2, 975055, 97505500, Stypes);
		Tuple t3 = create(3, 774238, 77423800, Stypes);
		Tuple t4 = create(4, 1403557, 140355700, Stypes);

		// Operators Map: 1 for <, 2 for <=, 3 for >= and 4 for >
//		System.out.print ("\n-------------\n");
		long startTime = System.nanoTime();

		IESelfJoin1Predicate iejoin = new IESelfJoin1Predicate(new Tuple[] {t1,t2,t3,t4},4,1,2);

//		long endTime = System.nanoTime();
//		System.out.println("Time taken = "+(endTime - startTime) + " ns"); 
		iejoin.printResults();

		System.out.print ("\n------------\n");

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
		System.out.println("length = ");
		System.out.println(result.size());
		//for (Tuple[] myTuples : result) {
			//System.out.format("[%s, %s]\n", tupleToString(myTuples[0]), tupleToString(myTuples[1]));
		//}
	}
}