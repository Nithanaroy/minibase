package iterator;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Comparator;
import java.util.List;

import global.AttrType;
import heap.FieldNumberOutOfBoundException;
import heap.InvalidTupleSizeException;
import heap.InvalidTypeException;
import heap.Tuple;
import optimizer.Scheduler;

public class IEjoin2t2predicates {
	Tuple[] L1;
	Tuple[] L2;
	int l1_len;
	int l2_len;
	int n;
	int op1;
	int op2;
	int T1_size;
	int T2_size;

	int t1_c1;
	int t2_c1;
	int t1_c2;
	int t2_c2;

	private int[] p;
	private BitSet bp;

	public IEjoin2t2predicates(Tuple[] l1, Tuple[] l2, int op1, int op2, int t1c1, int t2c1, int t1c2, int t2c2)
			throws FieldNumberOutOfBoundException, IOException {
		super();
		L1 = l1;
		L2 = l2;

		this.op1 = op1;
		this.op2 = op2;

		t1_c1 = t1c1;
		t2_c1 = t2c1;
		t1_c2 = t1c2;
		t2_c2 = t2c2;

		l1_len = l1.length;
		l2_len = l2.length;

		T1_size = l1_len == 0 ? 0 : l1[0].noOfFlds(); // handle empty tables
		T2_size = l2_len == 0 ? 0 : l2[0].noOfFlds(); // handle empty tables

		n = l1_len + l2_len;

		// Orginal Table
		/*System.out.println("Table 1");
		for(int k=0;k<l1.length;k++)
		{
			System.out.format("[%s]\n", tupleToString(l1[k]));
		}
		
		System.out.println("Table 2");
		for(int k=0;k<l2.length;k++)
		{
			System.out.format("[%s]\n", tupleToString(l2[k]));
		}
		 */
		// swap
		swap(l1, t1_c1, t1_c2, 0);
		swap(l2, t2_c1, t2_c2, 1);

		// Swapped Table
		/*System.out.println("swapped Table 1");
		for(int k=0;k<l1.length;k++)
		{
			System.out.format("[%s]\n", tupleToString(l1[k]));
		}
		
		System.out.println("swapped Table 2");
		for(int k=0;k<l2.length;k++)
		{
			System.out.format("[%s]\n", tupleToString(l2[k]));
		}
		 */
		L1 = new Tuple[l1_len + l2_len];
		System.arraycopy(l1, 0, L1, 0, l1_len);
		System.arraycopy(l2, 0, L1, l1_len, l2_len);

		L2 = new Tuple[l1_len + l2_len];
		System.arraycopy(l1, 0, L2, 0, l1_len);
		System.arraycopy(l2, 0, L2, l1_len, l2_len);

		int ind = 0;
		for (int i = 0; i < L2.length; i++) {
			ind++;
			L1[i].setIntFld(2, ind);
			L2[i].setIntFld(2, ind);
		}

		p = new int[L1.length];
		bp = new BitSet(L1.length);

		// Combined Table
		/*System.out.println("Combined Table");
		for(int k=0;k<L1.length;k++)
		{
			System.out.format("[%s]\n", tupleToString(L1[k]));
		}*/
	}

	private void unswap(Tuple A) throws FieldNumberOutOfBoundException, IOException {
		int c1, c2;
		int size = A.noOfFlds();
		size = size - 2;
		int[] temp = new int[size];

		if (A.getIntFld(1) == 0) {
			c1 = t1_c1;
			c2 = t1_c2;
		} else {
			c1 = t2_c1;
			c2 = t2_c2;
		}

		for (int i = 0; i < size; i++) {
			temp[i] = A.getIntFld(i + 3);
		}
		A.setIntFld(c1, temp[0]);
		A.setIntFld(c2, temp[1]);

		for (int i = 2, j = 0; i < size; j++) {
			if (j + 1 != c1 && j + 1 != c2) {
				A.setIntFld(j + 1, temp[i]);
				i++;
			}
		}
	}

	private void swap(Tuple[] A, int c1, int c2, int b) throws FieldNumberOutOfBoundException, IOException {

		int size = 0;

		if (b == 0)
			size = T1_size;
		else
			size = T2_size;

		size = size - 2;

		if (size < 0) // happens when one of the tables is empty
			return;

		int[] temp = new int[size];
		// System.out.println("size = "+size);
		for (int k = 0; k < A.length; k++) {
			for (int i = 0; i < size; i++) {
				temp[i] = A[k].getIntFld(i + 1);
			}

			A[k].setIntFld(1, b);
			A[k].setIntFld(2, k + 1);
			A[k].setIntFld(3, temp[c1 - 1]);
			A[k].setIntFld(4, temp[c2 - 1]);

			for (int i = 5, j = 0; j < size; j++) {
				if (j != c1 - 1 && j != c2 - 1) {
					// System.out.println("i = "+i+" j = "+j+" size = "+A[k].noOfFlds()+" c1 = "+c1+" c2 = "+c2);
					A[k].setIntFld(i, temp[j]);
					i++;
				}
			}
		}
	}

	private void sorting() throws FieldNumberOutOfBoundException, IOException {

		Comparator<Tuple> asc_col_1 = new Comparator<Tuple>() {
			@Override
			public int compare(Tuple o1, Tuple o2) {
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
				else if (diff == 0) {
					try {
						diff = o1.getIntFld(4) - o2.getIntFld(4);
					} catch (FieldNumberOutOfBoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
					if (diff > 1) {
						if (((op1 == 4) && (op2 == 1 || op2 == 2)) || ((op1 == 3) && (op2 == 4 || op2 == 3)))
							return 1;
						else
							return -1;
					} else if (diff == 0)
						return 0;
					else {
						if (((op1 == 4) && (op2 == 1 || op2 == 2)) || ((op1 == 3) && (op2 == 4 || op2 == 3)))
							return -1;
						else
							return 1;
					}
				} else
					return -1;
			}
		};

		Comparator<Tuple> desc_col_1 = new Comparator<Tuple>() {
			@Override
			public int compare(Tuple o1, Tuple o2) {
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
				else if (diff == 0) {
					try {
						diff = o1.getIntFld(4) - o2.getIntFld(4);
					} catch (FieldNumberOutOfBoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
					if (diff > 1) {
						if (((op1 == 1) && (op2 == 1 || op2 == 2)) || ((op1 == 2) && (op2 == 4 || op2 == 3)))
							return 1;
						else
							return -1;
					} else if (diff == 0)
						return 0;
					else {
						if (((op1 == 1) && (op2 == 1 || op2 == 2)) || ((op1 == 2) && (op2 == 4 || op2 == 3)))
							return -1;
						else
							return 1;
					}
				} else
					return 1;
			}
		};
		Comparator<Tuple> asc_col_2 = new Comparator<Tuple>() {
			@Override
			public int compare(Tuple o1, Tuple o2) {
				int diff = 0;
				try {
					diff = o1.getIntFld(4) - o2.getIntFld(4);
				} catch (FieldNumberOutOfBoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				if (diff == 0) {
					try {
						diff = o1.getIntFld(3) - o2.getIntFld(3);
					} catch (FieldNumberOutOfBoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
					if (diff > 1) {
						if (((op2 == 1) && (op1 == 4 || op1 == 3)) || ((op2 == 2) && (op1 == 1 || op1 == 2)))
							return 1;
						else
							return -1;
					} else if (diff == 0)
						return 0;
					else {
						if (((op2 == 1) && (op1 == 4 || op1 == 3)) || ((op2 == 2) && (op1 == 1 || op1 == 2)))
							return -1;
						else
							return 1;
					}

				} else
					return diff;
			}
		};
		Comparator<Tuple> desc_col_2 = new Comparator<Tuple>() {
			@Override
			public int compare(Tuple o1, Tuple o2) {
				int diff = 0;
				try {
					diff = o1.getIntFld(4) - o2.getIntFld(4);
				} catch (FieldNumberOutOfBoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

				if (diff == 0) {

					try {
						diff = o1.getIntFld(3) - o2.getIntFld(3);
					} catch (FieldNumberOutOfBoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
					if (diff > 1) {
						if (((op2 == 4) && (op1 == 4 || op1 == 3)) || ((op2 == 3) && (op1 == 1 || op1 == 2)))
							return 1;
						else
							return -1;
					} else if (diff == 0)
						return 0;
					else {
						if (((op2 == 4) && (op1 == 4 || op1 == 3)) || ((op2 == 3) && (op1 == 1 || op1 == 2)))
							return -1;
						else
							return 1;
					}

				} else
					return -diff;
			}
		};

		// Compute L1,L2

		if (op1 == 1 || op1 == 2) {
			Arrays.sort(L1, desc_col_1);
		} else if (op1 == 4 || op1 == 3) {
			Arrays.sort(L1, asc_col_1);
		}

		if (op2 == 1 || op2 == 2) {
			Arrays.sort(L2, asc_col_2);
		} else if (op2 == 3 || op2 == 4) {
			Arrays.sort(L2, desc_col_2);
		}
		// System.out.println("Reach 2");
		/*System.out.println("\nL1 = ");
		for(int k=0;k<L1.length;k++)
		{
			System.out.format("[%s]\n", tupleToString(L1[k]));
		}
		System.out.println("\nL2 = ");
		for(int k=0;k<L2.length;k++)
			System.out.format("[%s]\n", tupleToString(L2[k]));
		 */
		// Compute permutation arrays - P
		int i = 0, j = 0;
		int[] bitarray = new int[L2.length];

		for (Tuple a : L2) {
			j = 0;
			for (Tuple b : L1) {
				if (bitarray[j] != 1 && (a.getIntFld(2) == b.getIntFld(2))) {
					p[i] = j;
					bitarray[j] = 1;
					break;
				} else
					j++;
			}
			i++;
		}
		// p[i++] = findId(L1, myTuple.getIntFld(2));
		// }

		/*System.out.println("\nP = ");
		for(int k=0;k<p.length;k++)
			System.out.print(p[k]+" ");
		*/
	}

	public int runForCount() throws FieldNumberOutOfBoundException, IOException, InvalidTypeException, InvalidTupleSizeException {
		sorting();
		int eqOff = 0;
		if ((op1 == 2 || op1 == 3) && (op2 == 2 || op2 == 3))
			eqOff = 0;
		else
			eqOff = 1;

		 return usingBitsetOptimized(eqOff);
	}

	public int run(String outputFile) throws FieldNumberOutOfBoundException, IOException, InvalidTypeException, InvalidTupleSizeException {
		ArrayList<Tuple> join_result = new ArrayList<>();
		// System.out.println("Reach 1");
		sorting();
		// intialize the bit array, set all to zero
		int eqOff = 0;
		if ((op1 == 2 || op1 == 3) && (op2 == 2 || op2 == 3))
			eqOff = 0;
		else
			eqOff = 1;
		// Visit
		// System.out.println("Reach 3");
		int total = usingBitsetOptimized(join_result, eqOff, outputFile);
		Tuple[] res = new Tuple[join_result.size()];
		join_result.toArray(res);
		return total;
	}

	private boolean pass(int j, int i) throws FieldNumberOutOfBoundException, IOException {
		if ((op1 == 1 && op2 == 1 && (L1[j].getIntFld(3) < L1[p[i]].getIntFld(3)) && (L1[j].getIntFld(4) < L1[p[i]].getIntFld(4)))
				|| (op1 == 1 && op2 == 2 && (L1[j].getIntFld(3) < L1[p[i]].getIntFld(3)) && (L1[j].getIntFld(4) <= L1[p[i]].getIntFld(4)))
				|| (op1 == 1 && op2 == 3 && (L1[j].getIntFld(3) < L1[p[i]].getIntFld(3)) && (L1[j].getIntFld(4) >= L1[p[i]].getIntFld(4)))
				|| (op1 == 1 && op2 == 4 && (L1[j].getIntFld(3) < L1[p[i]].getIntFld(3)) && (L1[j].getIntFld(4) > L1[p[i]].getIntFld(4)))
				|| (op1 == 2 && op2 == 1 && (L1[j].getIntFld(3) <= L1[p[i]].getIntFld(3)) && (L1[j].getIntFld(4) < L1[p[i]].getIntFld(4)))
				|| (op1 == 2 && op2 == 2 && (L1[j].getIntFld(3) <= L1[p[i]].getIntFld(3)) && (L1[j].getIntFld(4) <= L1[p[i]].getIntFld(4)))
				|| (op1 == 2 && op2 == 3 && (L1[j].getIntFld(3) <= L1[p[i]].getIntFld(3)) && (L1[j].getIntFld(4) >= L1[p[i]].getIntFld(4)))
				|| (op1 == 2 && op2 == 4 && (L1[j].getIntFld(3) <= L1[p[i]].getIntFld(3)) && (L1[j].getIntFld(4) > L1[p[i]].getIntFld(4)))
				|| (op1 == 3 && op2 == 1 && (L1[j].getIntFld(3) >= L1[p[i]].getIntFld(3)) && (L1[j].getIntFld(4) < L1[p[i]].getIntFld(4)))
				|| (op1 == 3 && op2 == 2 && (L1[j].getIntFld(3) >= L1[p[i]].getIntFld(3)) && (L1[j].getIntFld(4) <= L1[p[i]].getIntFld(4)))
				|| (op1 == 3 && op2 == 3 && (L1[j].getIntFld(3) >= L1[p[i]].getIntFld(3)) && (L1[j].getIntFld(4) >= L1[p[i]].getIntFld(4)))
				|| (op1 == 3 && op2 == 4 && (L1[j].getIntFld(3) >= L1[p[i]].getIntFld(3)) && (L1[j].getIntFld(4) > L1[p[i]].getIntFld(4)))
				|| (op1 == 4 && op2 == 1 && (L1[j].getIntFld(3) > L1[p[i]].getIntFld(3)) && (L1[j].getIntFld(4) < L1[p[i]].getIntFld(4)))
				|| (op1 == 4 && op2 == 2 && (L1[j].getIntFld(3) > L1[p[i]].getIntFld(3)) && (L1[j].getIntFld(4) <= L1[p[i]].getIntFld(4)))
				|| (op1 == 4 && op2 == 3 && (L1[j].getIntFld(3) > L1[p[i]].getIntFld(3)) && (L1[j].getIntFld(4) >= L1[p[i]].getIntFld(4)))
				|| (op1 == 4 && op2 == 4 && (L1[j].getIntFld(3) > L1[p[i]].getIntFld(3)) && (L1[j].getIntFld(4) > L1[p[i]].getIntFld(4))))
			return true;

		return false;

	}

	private int usingBitsetOptimized(int eqOff) throws FieldNumberOutOfBoundException, IOException {
		int count = 0;
		for (int i = 0; i < L1.length; i++) {
			int pos = p[i];
			bp.set(pos);
			for (int j = pos + eqOff; j < L1.length; j++) {
				if (bp.get(j)) {
					if (L1[j].getIntFld(1) == 0 && L1[p[i]].getIntFld(1) == 1 && pass(j, i)) {
						count++;
					}
				}
			}
		}
		return count;
	}

	private int usingBitsetOptimized(ArrayList<Tuple> join_result, int eqOff, String outputFile)
			throws FieldNumberOutOfBoundException, IOException, InvalidTypeException, InvalidTupleSizeException {
		if (T1_size * T1_size == 0) {
			// One of the tables is empty, just create an empty result file and return
			Scheduler.exportTable(new Tuple[0], outputFile, false);
			return 0;
		}
		int count = 0;
		int total = 0;
		boolean flag = false;

		Tuple temp1 = new Tuple();
		int size1 = T1_size;
		AttrType[] Stypes1 = new AttrType[size1];

		Tuple temp2 = new Tuple();
		int size2 = T2_size;
		AttrType[] Stypes2 = new AttrType[size2];

		for (int i = 0; i < size1; i++)
			Stypes1[i] = new AttrType(AttrType.attrInteger);

		temp1.setHdr((short) (size1), Stypes1, null);

		for (int i = 0; i < size2; i++)
			Stypes2[i] = new AttrType(AttrType.attrInteger);

		temp2.setHdr((short) (size2), Stypes2, null);

		int size = T1_size + T2_size - 4;
		AttrType[] Stypes = new AttrType[size];
		for (int l = 0; l < size; l++)
			Stypes[l] = new AttrType(AttrType.attrInteger);

		for (int i = 0; i < L1.length; i++) {
			int off2 = p[i];
			bp.set(off2); // = 1;
			int k = bp.nextSetBit(off2 + eqOff);
			while (k >= 0) {
				Tuple temp = new Tuple();

				temp.setHdr((short) (size), Stypes, null);

				if (L1[k].getIntFld(1) == 0 && L1[p[i]].getIntFld(1) == 1 && pass(k, i)) {

					for (int t = 1, m = 1; t <= size1; t++, m++) {
						temp1.setIntFld(m, L1[k].getIntFld(t));
					}
					for (int t = 1, m = 1; t <= size2; t++, m++) {
						temp2.setIntFld(m, L1[p[i]].getIntFld(t));
					}
					unswap(temp1);
					unswap(temp2);
					int m = 1;
					for (int t = 1; t <= size1 - 2; t++, m++) {
						temp.setIntFld(m, temp1.getIntFld(t));
					}
					for (int t = 1; t <= size2 - 2; t++, m++) {
						temp.setIntFld(m, temp2.getIntFld(t));
					}

					// join_result.add(new Tuple[] { L1[k], L1[p[i]] });

					join_result.add(temp);

					if (count == 500000) {
						Tuple[] res = new Tuple[join_result.size()];
						join_result.toArray(res);
						Scheduler.exportTable(res, outputFile, flag);
						join_result.clear();
						res = null;
						System.gc();
						count = 0;
						flag = true;
						total = total + 500000;
						// System.out.println("Written = "+ total);
					}
					// System.out.format("%s\n", tupleToStr(res[0],res[0].noOfFlds()));
					count++;
				}

				k = bp.nextSetBit(k + 1);

			}
		}
		Tuple[] res = new Tuple[join_result.size()];
		join_result.toArray(res);
		optimizer.Scheduler.exportTable(res, outputFile, flag);
		join_result = null;
		System.gc();
		total = total + count;
		System.out.println("\tSaved " + total + " tuples");
		return total;
	}

	@SuppressWarnings("unused")
	private int findId(Tuple[] a, int id) throws FieldNumberOutOfBoundException, IOException {
		int i = 0;
		for (Tuple myTuple : a) {
			if (myTuple.getIntFld(2) == id)
				return i;
			i++;
		}
		return -1;
	}

	@SuppressWarnings("unused")
	private static String tupleToStr(Tuple t, int size) throws FieldNumberOutOfBoundException, IOException {
		String str = "";
		for (int i = 1; i <= size; i++)
			str = str + t.getIntFld(i) + "	";
		return String.format(str);
	}

	private static void setTuple(Tuple t, AttrType s, String value, Integer pos)
			throws FieldNumberOutOfBoundException, IOException, InvalidTypeException, InvalidTupleSizeException {
		Integer attType = s.attrType;
		switch (attType) {
		case AttrType.attrInteger:
			t.setIntFld(pos, Integer.parseInt(value));
			break;
		case AttrType.attrString:
			t.setStrFld(pos, value);
			break;
		case AttrType.attrSymbol:
			t.setFloFld(pos, Float.parseFloat(value));
			break;
		}
	}

	private static Tuple[] generateData(String fileName)
			throws FieldNumberOutOfBoundException, IOException, InvalidTypeException, InvalidTupleSizeException {
		String line = null;
		AttrType[] Stypes;
		List<Tuple> queryList = new ArrayList<Tuple>();
		try {
			FileReader fileReader = new FileReader(fileName);
			BufferedReader bufferedReader = new BufferedReader(fileReader);

			line = bufferedReader.readLine();
			int columnsCount = line.trim().split(",").length + 2; // First four columns are for office use ;)
			// System.out.println("columnsCount = "+columnsCount);
			Stypes = new AttrType[columnsCount];
			for (int i = 0; i < columnsCount; i++) {
				Stypes[i] = new AttrType(AttrType.attrInteger); // Assumption: All columns are of integer type
			}
			do {
				String[] tupleData = line.split(",");
				// System.out.print("data = "+tupleData[0]);
				Tuple t = new Tuple();
				t.setHdr((short) columnsCount, Stypes, null);

				for (int i = 1; i <= tupleData.length; i++) {
					setTuple(t, Stypes[i], tupleData[i - 1], i);
				}
				queryList.add(t);
			} while ((line = bufferedReader.readLine()) != null);
			// Always close files.
			bufferedReader.close();
		} catch (FileNotFoundException ex) {
			System.out.println("Unable to open file '" + fileName + "'");
		} catch (IOException ex) {
			System.out.println("Error reading file '" + fileName + "'");
			// Or we could just do this:
			// ex.printStackTrace();
		}
		Tuple[] tupleArray = new Tuple[queryList.size()];
		return queryList.toArray(tupleArray);
	}

	public static void main(String[] args)
			throws FieldNumberOutOfBoundException, IOException, InvalidTypeException, InvalidTupleSizeException {

		String queryFilePath = "./data/query_2c.txt";
		String sourceDirPath = "./data/";

		String line = null;
		List<String> queryList = new ArrayList<String>();
		try {
			FileReader fileReader = new FileReader(queryFilePath);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			while ((line = bufferedReader.readLine()) != null) {
				queryList.add(line);
			}
			// Always close files.
			bufferedReader.close();
		} catch (FileNotFoundException ex) {
			System.out.println("Unable to open file '" + queryFilePath + "'");
		} catch (IOException ex) {
			System.out.println("Error reading file '" + queryFilePath + "'");
		}

		/*System.out.println("Given Query: ");
		int listSize = queryList.size();
		for (int i = 0; i < listSize; ++i)
			System.out.println(queryList.get(i));
		System.out.println();
		 */
		String[] filesToRead = queryList.get(1).split(" ");
		// variable queryList is [R_1 S_1, R S, R_2 1 S_2, AND, R_4 1 S_4]
		int t1_cond1 = Integer.parseInt(queryList.get(2).split(" ")[0].trim().split("_")[1].trim()); // get 2 from R_2 1 S_2
		int t2_cond1 = Integer.parseInt(queryList.get(2).split(" ")[2].trim().split("_")[1].trim()); // get 2 from R_2 1 S_2
		int t1_cond2 = Integer.parseInt(queryList.get(4).split(" ")[0].trim().split("_")[1].trim()); // get 4 from R_4 1 S_4
		int t2_cond2 = Integer.parseInt(queryList.get(4).split(" ")[2].trim().split("_")[1].trim()); // get 4 from R_4 1 S_4
		int op1 = Integer.parseInt(queryList.get(2).split(" ")[1].trim()); // get 2 from R_2 1 S_2
		int op2 = Integer.parseInt(queryList.get(4).split(" ")[1].trim()); // get 1 from R_4 1 S_4

		// All condition column indices are zero based where as input is 1 based. So subtract 1 from t(i)cond(i)Col where i = {1, 2}
		Tuple[] T1 = generateData(sourceDirPath + filesToRead[0] + ".csv");
		Tuple[] T2 = generateData(sourceDirPath + filesToRead[1] + ".csv");
		// System.out.println("file 1 = "+filesToRead[0]+" file 2 = "+filesToRead[1]);

		String filename = "C:\\Supriya\\ASU\\Database_mgmt_impl_sys\\javaminibase\\Query\\data\\temp.csv";
		// System.out.println("Count = "+ new IEjoin2t2predicates(T1, T2, op1, op2,t1_cond1,t2_cond1,t1_cond2,t2_cond2).runForCount());

		new IEjoin2t2predicates(T1, T2, op1, op2, t1_cond1, t2_cond1, t1_cond2, t2_cond2).printResults(filename);
	}

	public void printResults(String outputFile)
			throws FieldNumberOutOfBoundException, IOException, InvalidTypeException, InvalidTupleSizeException {
		// Tuple[] result = null;
		// result = this.run();
		this.run(outputFile);

		System.out.println("----------------------------------------------------");
		System.out.println("------------------------RESULT----------------------");
		System.out.println("----------------------------------------------------");

		// for (int j = 0; j < result.length; j++)
		// System.out.format("%s\n", tupleToStr(result[j],result[j].noOfFlds()));
	}
}