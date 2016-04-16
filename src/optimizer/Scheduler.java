package optimizer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;

import heap.FieldNumberOutOfBoundException;
import heap.InvalidTupleSizeException;
import heap.InvalidTypeException;

public class Scheduler {

	/**
	 * Orders the conditions based on a estimator 
	 * @param queryFile complete path of the query file
	 * @param dataDir path to the directory where all relation files are found
	 * @return
	 */
	public String[][] schedule(String queryFile, String dataDir) throws FileNotFoundException, IOException, NumberFormatException,
			InvalidTypeException, InvalidTupleSizeException, FieldNumberOutOfBoundException {
		HashMap<String, String[][]> query = QueryParser.parse(queryFile);
		String r1 = String.format("%s/%s.csv", dataDir, query.get("tables")[0][0]); // Assumption: Relation files have .csv as extension
		String r2 = String.format("%s/%s.csv", dataDir, query.get("tables")[0][1]);
		ISelectivityEstimator estimator = SelectivityEstimatorFactory.getEstimator(Estimator.BY_RANDOM_SAMPLING, r1, r2,
				query.get("conditions"));
		System.out.println(estimator.estimate(10, 10));
		return query.get("conditions"); // TODO: Call this for all conditions and order based selectivity
	}

	public static void main(String[] args) {
		Scheduler s = new Scheduler();
		try {
			s.schedule("./data/phase4/query.txt", "./data/phase4/");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
