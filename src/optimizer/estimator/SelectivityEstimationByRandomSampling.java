package optimizer.estimator;

import java.io.FileNotFoundException;
import java.io.IOException;

import heap.FieldNumberOutOfBoundException;
import heap.InvalidTupleSizeException;
import heap.InvalidTypeException;
import heap.Tuple;
import iterator.IEjoin2t2predicates;
import optimizer.sampler.SamplerFactory;
import optimizer.sampler.SamplerType;

public class SelectivityEstimationByRandomSampling extends ISelectivityEstimator {

	public SelectivityEstimationByRandomSampling(String relationsDir, String[][] conditions) {
		super(relationsDir, conditions);
	}

	/**
	 * Creates samples of a certain size from Relations 1 and 2 from the query
	 * 
	 * Runs IEJoin to get the size of resultant relation
	 * 
	 * @param r1SampleSize number of tuples to use from R1
	 * @param r2SampleSize number of tuples to use from R2
	 * @return size of join result
	 */
	public int estimate(int r1SampleSize, int r2SampleSize) throws FileNotFoundException, IOException, NumberFormatException,
			InvalidTypeException, InvalidTupleSizeException, FieldNumberOutOfBoundException {

		// Get relation names from the conditions. As IE join works on two relations we can look at one of the conditions.
		// Each condition is of the form - [R,3,4,S,3]
		String relation1Path = this.getRelationsDir() + "//" + this.getConditions()[0][0] + ".csv"; // Assumption: Each relation file is a .csv file
		String relation2Path = this.getRelationsDir() + "//" + this.getConditions()[0][3] + ".csv";
		Tuple[] r1 = SamplerFactory.getSampler(SamplerType.WITH_REPLACEMENT, relation1Path, r1SampleSize).getSample();
		Tuple[] r2 = SamplerFactory.getSampler(SamplerType.WITH_REPLACEMENT, relation2Path, r2SampleSize).getSample();

		// System.out.println("Original R1");
		// ISampler.printTable(r1);
		//
		// System.out.println("Original R2");
		// ISampler.printTable(r2);

		// IE Join expects columns participating in the this.getConditions() are 2nd and 3rd columns
		// i.e. [0] column => Minibase reserved column
		// i.e. [1] column => ID column assigned by ISampler.createTuple()
		// i.e. [2] column => condition 1 column
		// i.e. [3] column => condition 2 column

		// For R1
		int t1_cond1 = Integer.parseInt(this.getConditions()[0][1]);
		int t1_cond2 = Integer.parseInt(this.getConditions()[1][1]);
		// swapColumnsToMatch(r1, condition1_col, condition2_col);

		// System.out.println("After Swap, R1");
		// ISampler.printTable(r1);

		// For R2
		int t2_cond1 = Integer.parseInt(this.getConditions()[0][4]);
		int t2_cond2 = Integer.parseInt(this.getConditions()[1][4]);
		// swapColumnsToMatch(r2, condition1_col, condition2_col);

		// System.out.println("After Swap, R2");
		// ISampler.printTable(r2);

		int condtionOp1 = Integer.parseInt(this.getConditions()[0][2]);
		int condtionOp2 = Integer.parseInt(this.getConditions()[1][2]);
		// return new IEJoin2Tables2Predicates(r1, r2, condtionOp1, condtionOp2, null, null).runForCount();
		return new IEjoin2t2predicates(r1, r2, condtionOp1, condtionOp2, t1_cond1, t2_cond1, t1_cond2, t2_cond2).runForCount();
	}

}
