package optimizer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import global.AttrType;
import heap.FieldNumberOutOfBoundException;
import heap.InvalidTupleSizeException;
import heap.InvalidTypeException;
import heap.Tuple;

/**
 * Interface for Sampling data from a relation
 * 
 * @author nitinpasumarthy
 *
 */
abstract class ISampler {

	protected String relationFilePath = null;
	protected int sampleSize = 0;
	private AttrType[] Stypes;

	/**
	 * Instantiate a sampler
	 * 
	 * @param relationFilePath Complete path of the relation file from which sample has to be created in CSV format
	 * @param sampleSize Number of tuples to select from the relation
	 */
	ISampler(String relationFilePath, int sampleSize) throws IOException {
		this.relationFilePath = relationFilePath;
		this.sampleSize = sampleSize;

		// Set Stypes by reading number of columns from the input file
		try (BufferedReader br = new BufferedReader(new FileReader(relationFilePath))) {
			String line = br.readLine();
			int cols = line.split(",").length; // Assumption: input file is CSV
			Stypes = new AttrType[cols + 3]; // Minibase reserves the first column and 2 extra cols for self computation
			for (int i = 0; i < Stypes.length; i++) {
				Stypes[i] = new AttrType(AttrType.attrInteger); // Assumption: all are fields are integers
			}
		}

	}

	abstract Tuple[] getSample()
			throws IOException, NumberFormatException, InvalidTypeException, InvalidTupleSizeException, FieldNumberOutOfBoundException;

	public final AttrType[] getStypes() {
		return Stypes;
	}

	/**
	 * Sets the first {@code Stypes.length} values from the input line to Tuple
	 * 
	 * @param input line from the CSV file to parse
	 * @return a tuple
	 * @throws FieldNumberOutOfBoundException
	 * @throws NumberFormatException
	 */
	protected final Tuple createTuple(String input)
			throws InvalidTypeException, InvalidTupleSizeException, IOException, NumberFormatException, FieldNumberOutOfBoundException {
		String[] fields = input.split(",");
		Tuple t = new Tuple();
		t.setHdr((short) Stypes.length, Stypes, null);
		for (int i = 1; i < fields.length; i++) {
			t.setIntFld(i, Integer.parseInt(fields[i - 1]));
		}
		return t;
	}

	public static void printTable(Tuple[] tuples) throws IOException, FieldNumberOutOfBoundException {
		int noOfFields = tuples[0].noOfFlds();
		for (int i = 0; i < tuples.length; i++) {
			System.out.format("%2d) ", i + 1);
			for (int j = 1; j < noOfFields; j++) {
				System.out.format("%8d,", tuples[i].getIntFld(j));
			}
			System.out.println();
		}
	}
}
