package iterator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import global.AttrType;
import heap.FieldNumberOutOfBoundException;
import heap.InvalidTupleSizeException;
import heap.InvalidTypeException;
import heap.Tuple;

/**
 *
 * This file contains an implementation of the inequality join
 * with only one condition
 * 
 * Assumption: All fields in the join predicate are of INTEGER type
 */

public class IEJoin2Tables1Predicate {
	Tuple[] t1;
	Tuple[] t2;
	int op1;
	int totalTuples = 0;
	int[] t1ProjColIndices;
	int[] t2ProjColIndices;

	public IEJoin2Tables1Predicate(Tuple[] l1, Tuple[] l2, int op1, int[] t1ProCols, int[] t2ProCols) {
		super();
		t1 = l1;
		t2 = l2;
		this.op1 = op1;
		this.t1ProjColIndices = t1ProCols;
		this.t2ProjColIndices = t2ProCols;
	}

	// [1] => id, [2] => time/duration, [3] and above => others
	public ArrayList<Tuple[]> run() throws FieldNumberOutOfBoundException, IOException {
		// TODO: Decide what to do when exception is raised
		long start = System.currentTimeMillis();
		ArrayList<Tuple[]> join_result = new ArrayList<>();
		Comparator<Tuple> ascending = new Comparator<Tuple>() {
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

		// sort tables by duration column
		Arrays.sort(t1, ascending);
		Arrays.sort(t2, ascending);

		System.out.format("Pre-processing time: %dms%n", (System.currentTimeMillis() - start));

		for (Tuple t : t1) {
			int i = Arrays.binarySearch(t2, t, ascending);
			switch (op1) {
			case 3:
				// gte
				addPrecedingTuples(join_result, t, i);
				break;

			case 2:
				// lte
				addSucceedingTuples(join_result, t, i);
				break;
			case 1:
				// lt
				// traverse equal tuples
				for (; i < t2.length && t.getIntFld(2) == t2[i].getIntFld(2); i++)
					;
				addSucceedingTuples(join_result, t, i);
				break;
			case 4:
				// gt
				// traverse equal tuples
				for (; i >= 0 && t.getIntFld(2) == t2[i].getIntFld(2); i--)
					;
				addPrecedingTuples(join_result, t, i);
				break;
			}

		}

		// System.out.println("Total tuples in join result: " + totalTuples);
		return join_result;

	}

	private void addSucceedingTuples(ArrayList<Tuple[]> join_result, Tuple t, int i) {
		while (i < t2.length) {
			join_result.add(new Tuple[] { t, t2[i] });
			totalTuples++;
			i++;
		}
	}

	private void addPrecedingTuples(ArrayList<Tuple[]> join_result, Tuple t, int i) {
		while (i >= 0) {
			join_result.add(new Tuple[] { t, t2[i] });
			totalTuples++;
			i--;
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
		System.out.println("Query: More time");
		IEJoin2Tables1Predicate iejoin = new IEJoin2Tables1Predicate(new Tuple[] { t1, t2, t3 }, new Tuple[] { tp1, tp2, tp3, tp4 }, 1,
				new int[] { 1 }, new int[] { 1 });

		iejoin.executeAndPrintResults();

	}

	public static String tupleToString(Tuple t, int[] projectionIndices) throws FieldNumberOutOfBoundException, IOException {
		StringBuilder sb = new StringBuilder();
		for (int col : projectionIndices) {
			sb.append(t.getIntFld(col) + ", ");
		}
		int len = sb.length();

		return sb.toString().substring(0, len - 2); // remove trailing space and comma
	}

	public void executeAndPrintResults() throws FieldNumberOutOfBoundException, IOException {
		ArrayList<Tuple[]> result = null;
		try {
			result = this.run();
		} catch (FieldNumberOutOfBoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		for (Tuple[] myTuples : result) {
			System.out.format("[%s, %s]\n", tupleToString(myTuples[0], t1ProjColIndices), tupleToString(myTuples[1], t2ProjColIndices));
		}
	}

}