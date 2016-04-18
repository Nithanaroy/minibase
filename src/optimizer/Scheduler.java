package optimizer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

import heap.FieldNumberOutOfBoundException;
import heap.InvalidTupleSizeException;
import heap.InvalidTypeException;

public class Scheduler {

	/**
	 * Orders the conditions based on a estimator
	 * 
	 * @param queryFile complete path of the query file
	 * @param dataDir path to the directory where all relation files are found
	 * @return
	 */
	public String[][] schedule(String queryFile, String dataDir) throws FileNotFoundException, IOException, NumberFormatException,
			InvalidTypeException, InvalidTupleSizeException, FieldNumberOutOfBoundException {
		HashMap<String, String[][]> query = QueryParser.parse(queryFile);

		int cconditions = query.get("conditions").length;
		RelationSizeForConditions[] estimates = new RelationSizeForConditions[cconditions / 2];
		int k = 0, size;
		for (int i = 0; i < cconditions; i += 2, k++) {
			String[][] selectedConditions = new String[][] { query.get("conditions")[i], query.get("conditions")[i + 1] };
			size = SelectivityEstimatorFactory.getEstimator(Estimator.BY_RANDOM_SAMPLING, dataDir, selectedConditions).estimate(100, 100);
			estimates[k] = new RelationSizeForConditions(query.get("conditions")[i], query.get("conditions")[i + 1], size);
			System.out.format("%2d) %s\n", k, estimates[k]);
		}
		Arrays.sort(estimates);
		System.out.println(Arrays.toString(estimates));
		return query.get("conditions"); // TODO: Call this for all conditions and order based selectivity
	}

	private int factorial(int n) {
		if (n == 1 || n == 0)
			return 1;
		return n * factorial(n - 1);
	}

	private int ncr(int n, int r) {
		return factorial(n) / (factorial(n - r) * factorial(r));
	}

	public static void main(String[] args) {
		Scheduler s = new Scheduler();
		try {
			s.schedule("./data/phase4/query_8c.txt", "./data/phase4/");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

class RelationSizeForConditions implements Comparable<RelationSizeForConditions> {
	String[] c1, c2;
	int size;

	public RelationSizeForConditions(String[] condition1, String[] condition2, int size) {
		c1 = condition1;
		c2 = condition2;
		this.size = size;
	}

	@Override
	public int compareTo(RelationSizeForConditions o) {
		return this.size - o.size;
	}

	@Override
	public String toString() {
		return String.format("C1: %s, C2: %s, Size: %d", Arrays.toString(c1), Arrays.toString(c2), this.size);
	}
}
