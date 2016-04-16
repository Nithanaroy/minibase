package optimizer;

import java.io.FileNotFoundException;
import java.io.IOException;

import heap.FieldNumberOutOfBoundException;
import heap.InvalidTupleSizeException;
import heap.InvalidTypeException;
import heap.Tuple;
import iterator.IEJoin2Tables2Predicates;

public class SelectivityEstimationByRandomSampling extends ISelectivityEstimator {

	public SelectivityEstimationByRandomSampling(String relation1, String relation2, String[][] conditions) {
		super(relation1, relation2, conditions);
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

		Tuple[] r1 = SamplerFactory.getSampler(SamplerType.WITH_REPLACEMENT, this.getRelation1(), r1SampleSize).getSample();
		Tuple[] r2 = SamplerFactory.getSampler(SamplerType.WITH_REPLACEMENT, this.getRelation2(), r2SampleSize).getSample();

		System.out.println("Original R1");
		ISampler.printTable(r1);

		System.out.println("Original R2");
		ISampler.printTable(r2);

		// IE Join expects columns participating in the this.getConditions() are 2nd and 3rd columns
		// i.e. [0] column => Minibase reserved column
		// i.e. [1] column => ID column assigned by ISampler.createTuple()
		// i.e. [2] column => condition 1 column
		// i.e. [3] column => condition 2 column

		// For R1
		int condition1_col = Integer.parseInt(this.getConditions()[0][1]);
		int condition2_col = Integer.parseInt(this.getConditions()[1][1]);
		swapColumnsToMatch(r1, condition1_col, condition2_col);

		System.out.println("After Swap, R1");
		ISampler.printTable(r1);

		// For R2
		condition1_col = Integer.parseInt(this.getConditions()[0][4]);
		condition2_col = Integer.parseInt(this.getConditions()[1][4]);
		swapColumnsToMatch(r2, condition1_col, condition2_col);

		System.out.println("After Swap, R2");
		ISampler.printTable(r2);

		int condtionOp1 = Integer.parseInt(this.getConditions()[0][2]);
		int condtionOp2 = Integer.parseInt(this.getConditions()[1][2]);
		return new IEJoin2Tables2Predicates(r1, r2, condtionOp1, condtionOp2, null, null).runForCount();
	}

}
