package optimizer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;

import global.AttrType;
import heap.FieldNumberOutOfBoundException;
import heap.InvalidTupleSizeException;
import heap.InvalidTypeException;
import heap.Tuple;
import iterator.IEjoin2t2predicates;

/**
 * Schedules the way a multiple IE join should excute based on Sampling estimation
 * @author nitinpasumarthy
 *
 */
public class Scheduler {

	private boolean D = true; // A flag which controls printing of debug statements

	/**
	 * Orders the conditions based on a estimator
	 * 
	 * @param queryFile complete path of the query file
	 * @param dataDir path to the directory where all relation files are found
	 * @return
	 */
	public IEJoinCondition[] schedule(String queryFile, String dataDir) throws FileNotFoundException, IOException, NumberFormatException,
			InvalidTypeException, InvalidTupleSizeException, FieldNumberOutOfBoundException {
		HashMap<String, String[][]> query = QueryParser.parse(queryFile);

		int cconditions = query.get("conditions").length;
		IEJoinCondition[] estimates = new IEJoinCondition[cconditions / 2];
		int k = 0, size;
		for (int i = 0; i < cconditions; i += 2, k++) {
			String[][] selectedConditions = new String[][] { query.get("conditions")[i], query.get("conditions")[i + 1] };
			size = SelectivityEstimatorFactory.getEstimator(Estimator.BY_RANDOM_SAMPLING, dataDir, selectedConditions).estimate(250, 250);
			estimates[k] = new IEJoinCondition(query.get("conditions")[i], query.get("conditions")[i + 1], size);
			System.out.format("%2d) %s\n", k, estimates[k]);
		}
		Arrays.sort(estimates);
		System.out.println(Arrays.toString(estimates));
		return estimates;
	}

	public void complexIEJoinRunner(IEJoinCondition[] conditions, String dataDir, String outputFileName)
			throws FieldNumberOutOfBoundException, InvalidTypeException, InvalidTupleSizeException, IOException {
		SuperList<String> joinedTables = new SuperList<>(); // list of tables joined
		HashMap<String, Integer> columnsCountPertable = new HashMap<>(); // number of columns in each table
		for (IEJoinCondition condition : conditions) {
			long start = System.currentTimeMillis();
			if (D)
				System.out.format("\nStarted condition, %s at %s\n", condition, start);
			// condition looks like this: [C1: [T1, 2, 4, T2, 3], C2: [T1, 5, 1, T2, 6]
			String table1 = condition.c1[0]; // Eg: T1
			String table2 = condition.c1[3]; // Eg: T2
			int op1 = Integer.parseInt(condition.c1[2]); // Eg: 4
			int op2 = Integer.parseInt(condition.c2[2]); // Eg: 1
			int t1_cond1 = Integer.parseInt(condition.c1[1]); // Eg: 2
			int t1_cond2 = Integer.parseInt(condition.c2[1]); // Eg: 5
			int t2_cond1 = Integer.parseInt(condition.c1[4]); // Eg: 3
			int t2_cond2 = Integer.parseInt(condition.c2[4]); // Eg: 6

			int t1 = joinedTables.find(table1);
			int t2 = joinedTables.find(table2);

			if (Math.max(t1, t2) >= 0) {
				// either table1 or table2 or both are joined, so lets use that/those joined table(s)
				if (t1 >= 0) {
					String[] tablesCombinedWith = joinedTables.get(String[].class, t1); // Eg: [T4, T5, T2]
					int prefixColumns = 0; // Eg: if table1 is T2, this should be #cols(T4) + #cols(T5)
					for (int i = 0; i < t1; i++)
						prefixColumns += columnsCountPertable.get(tablesCombinedWith[i]);
					table1 = TextUtils.join(tablesCombinedWith, ""); // Eg: T4T5T2
					t1_cond1 += prefixColumns;
					t1_cond2 += prefixColumns;

					// add table2 to the joinedTables in the list of table1 at the end
					joinedTables.append(t1, table2);
				}

				if (t2 >= 0) {
					String[] tablesCombinedWith = joinedTables.get(String[].class, t2);
					int prefixColumns = 0;
					for (int i = 0; i < t1; i++)
						prefixColumns += columnsCountPertable.get(tablesCombinedWith[i]);
					table2 = TextUtils.join(tablesCombinedWith, "");
					t2_cond1 += prefixColumns;
					t2_cond2 += prefixColumns;

					// add table1 to the joinedTables in the list of table2 in the beginning
					joinedTables.prepend(t2, table1);
				}
			} else {
				// neither table1 nor table2 were seen before, join them as is
				// and add them to joinedTables list in the same order as a new list
				int listIndex = joinedTables.append(-1, table1);
				joinedTables.append(listIndex, table2);
			}

			String table1Path = String.format("%s/%s.csv", dataDir, table1); // Assumption: All input files are .csv
			String table2Path = String.format("%s/%s.csv", dataDir, table2); // Assumption: All input files are .csv

			// Save the number of columns of each table. Do not get this from the Tuple as we are adding buffer columns
			columnsCountPertable.put(table1, countColumnsInTable(table1Path));
			columnsCountPertable.put(table2, countColumnsInTable(table2Path));

			Tuple[] r1 = scanRelation(table1Path);
			if (D)
				System.out.format("\tCompleted scan of %s of %s rows after %sms\n", table1, r1.length, System.currentTimeMillis() - start);
			Tuple[] r2 = scanRelation(table2Path);
			if (D)
				System.out.format("\tCompleted scan of %s of %s rows after %sms\n", table2, r2.length, System.currentTimeMillis() - start);
			Tuple[] interRes = new IEjoin2t2predicates(r1, r2, op1, op2, t1_cond1, t2_cond1, t1_cond2, t2_cond2).run();
			if (D)
				System.out.format("\tCompleted ie-join after %sms\n", System.currentTimeMillis() - start);
			// Note the name of the intermediate table - table1table2.csv
			// TODO: Waiting for changes in IE Join to accept export file path
			// exportTable(interRes, String.format("%s/%s%s.csv", dataDir, table1, table2)); // Assumption: All input files are in .csv format
			if (D)
				System.out.format("Completed export of %s rows after %sms\n", interRes.length, System.currentTimeMillis() - start);

			// free up memory
			r1 = null;
			r2 = null;
			interRes = null;
			System.gc();
		}
	}

	private Tuple[] scanRelation(String relationFilePath)
			throws InvalidTypeException, InvalidTupleSizeException, IOException, NumberFormatException, FieldNumberOutOfBoundException {
		List<Tuple> relation = new ArrayList<Tuple>();
		try (BufferedReader bufferedReader = new BufferedReader(new FileReader(relationFilePath))) {
			String line = bufferedReader.readLine().trim();
			int columnsCount = line.split(",").length + 3; // Minibase reserves the first col and 2 more cols for self computation

			AttrType[] Stypes = new AttrType[columnsCount];
			for (int i = 0; i < columnsCount; i++)
				Stypes[i] = new AttrType(AttrType.attrInteger); // Assumption: All columns are of integer type

			do {
				String[] tupleData = line.trim().split(",");
				Tuple t = new Tuple();
				t.setHdr((short) columnsCount, Stypes, null);

				for (int i = 0; i < tupleData.length; i++) {
					t.setIntFld(i + 1, Integer.parseInt(tupleData[i].trim())); // First column is reserved
				}
				relation.add(t);
			} while ((line = bufferedReader.readLine()) != null);

		}
		Tuple[] tupleArray = new Tuple[relation.size()];
		return relation.toArray(tupleArray);
	}

	/**
	 * computes the number of columns in a table
	 * @param relationFilePath complete location where the relation is saved
	 * @return number of columns in that relation
	 */
	private int countColumnsInTable(String relationFilePath) throws FileNotFoundException, IOException {
		try (BufferedReader br = new BufferedReader(new FileReader(relationFilePath))) {
			return br.readLine().split(",").length; // Assumption: Given file is a CSV
		}
	}

	/**
	 * Saves a Table to disk
	 * @param t table to save
	 * @param outputFilePath complete file path where to save
	 * @param append if true data will be appended, else data will be overwritten
	 */
	public void exportTable(Tuple[] t, String outputFilePath, boolean append) throws IOException, FieldNumberOutOfBoundException {
		if (t.length == 0)
			return;
		int cols = t[0].noOfFlds();
		try (BufferedWriter w = new BufferedWriter(new FileWriter(outputFilePath, append))) {
			for (Tuple tuple : t) {
				StringBuilder line = new StringBuilder();
				for (int i = 1; i <= cols; i++) {
					line.append(tuple.getIntFld(i) + ",");
				}
				w.write(line.substring(0, line.length() - 1) + "\n"); // ignore ending comma and save
			}
			w.flush();
		}
	}

	private int factorial(int n) {
		if (n == 1 || n == 0)
			return 1;
		return n * factorial(n - 1);
	}

	@SuppressWarnings("unused")
	private int ncr(int n, int r) {
		return factorial(n) / (factorial(n - r) * factorial(r));
	}

	public static void main(String[] args) {
		Scheduler s = new Scheduler();
		try {
			long start = System.currentTimeMillis();
			String query = "./data/phase4/query_8c.txt";
			String dataDir = "./data/phase4/";
			IEJoinCondition[] estimates = s.schedule(query, dataDir);
			s.complexIEJoinRunner(estimates, dataDir, "./data/res.csv");
			if (s.D)
				System.out.format("Completed after: %sms\n", (System.currentTimeMillis() - start));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

class IEJoinCondition implements Comparable<IEJoinCondition> {
	public String[] c1, c2;
	public int size;

	public IEJoinCondition(String[] condition1, String[] condition2, int size) {
		c1 = condition1;
		c2 = condition2;
		this.size = size;
	}

	@Override
	public int compareTo(IEJoinCondition o) {
		return this.size - o.size;
	}

	@Override
	public String toString() {
		return String.format("[C1: %s, C2: %s, Size: %d]", Arrays.toString(c1), Arrays.toString(c2), this.size);
	}
}

/**
 * A helper class for containing methods for managing Lists of lists in this dumb Java where more
 * than business logic such classes are required
 *  
 * @author nitinpasumarthy
 *
 * @param <T>
 */
class SuperList<T> {
	private ArrayList<Deque<T>> l;

	public SuperList() {
		l = new ArrayList<>();
	}

	/**
	 * Appends the {@code value} in the given list {@code index} at the end
	 * Creates a new list if {@code index} is negative 
	 * @param index index of the list to insert
	 * @param value value to append at the end
	 * @return index of the list inserted
	 */
	public int append(int index, T value) {
		if (index < 0) {
			// create a new list
			Deque<T> x = new ArrayDeque<>();
			x.add(value);
			l.add(x);
			return l.size() - 1; // we need zero based index
		} else {
			l.get(index).addLast(value);
			return index;
		}
	}

	/**
	 * Adds the {@code value} to the given list {@code index} at the beginning
	 * @param index index of the list to prepend this value
	 * @param value value to preprend
	 */
	public void prepend(int index, T value) {
		l.get(index).addFirst(value);
	}

	/**
	 * Finds the given value in all the lists using equals() method of value
	 * @param value value to search
	 * @return zero based index of the given value in the list. -1 if not found
	 */
	public int find(T value) {
		int index = 0;
		for (Deque<T> deque : l) {
			for (T t : deque) {
				if (t.equals(value))
					return index;
			}
			index++;
		}
		return -1;
	}

	/**
	 * get the list item
	 * @param type type of item to return, if String array, this would be String[].class
	 * @param index index of the item
	 * @return array of items at the given index in the list
	 */
	public T[] get(Class<T[]> type, int index) {
		// Thanks to http://stackoverflow.com/a/4221845/1585523 for explaining this concept
		T[] res = type.cast(Array.newInstance(type.getComponentType(), l.get(index).size()));
		l.get(index).toArray(res);
		return res;
	}

	@Override
	public String toString() {
		return l.toString();
	}
}

class TextUtils {
	/**
	 * Combines an array of string with the separator
	 * @param a array of Strings to combine
	 * @param sep separator to place in between each String
	 * @return combined string
	 */
	public static String join(String[] a, String sep) {
		StringBuilder s = new StringBuilder();
		for (String str : a) {
			s.append(str + sep);
		}
		return s.substring(0, s.length() - sep.length());
	}
}
