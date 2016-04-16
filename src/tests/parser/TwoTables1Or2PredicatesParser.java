package tests.parser;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import global.AttrType;
import heap.FieldNumberOutOfBoundException;
import heap.InvalidTupleSizeException;
import heap.InvalidTypeException;
import heap.Tuple;
import iterator.IEJoin2Tables1Predicate;
import iterator.IEJoin2Tables2Predicates;

public class TwoTables1Or2PredicatesParser {
	public static void setTuple(Tuple t, AttrType s, String value, Integer pos)
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

	public static Tuple[] generateData(String fileName, Integer pos1, Integer pos2)
			throws FieldNumberOutOfBoundException, IOException, InvalidTypeException, InvalidTupleSizeException {
		String line = null;
		AttrType[] Stypes;
		List<Tuple> queryList = new ArrayList<Tuple>();
		try {
			FileReader fileReader = new FileReader(fileName);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			int counter = 0;
			int columnsCount = bufferedReader.readLine().trim().split(",").length + 4; // First four columns are for office use ;)
			Stypes = new AttrType[columnsCount];
			for (int i = 0; i < columnsCount; i++) {
				Stypes[i] = new AttrType(AttrType.attrInteger); // Assumption: All columns are of integer type
			}
			while ((line = bufferedReader.readLine()) != null) {
				String[] tupleData = line.split(",");
				Tuple t = new Tuple();
				t.setHdr((short) columnsCount, Stypes, null);
				t.setIntFld(1, counter++); // id column
				setTuple(t, Stypes[pos1], tupleData[pos1], 2); // condition one column
				setTuple(t, Stypes[pos2], tupleData[pos2], 3); // condition two column
				for (int i = 0; i < tupleData.length; i++) {
					setTuple(t, Stypes[i], tupleData[i], i + 3);
				}
				queryList.add(t);
			}
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

	public static void main(String args[])
			throws IOException, FieldNumberOutOfBoundException, IOException, InvalidTypeException, InvalidTupleSizeException {
		String line = null;
		String queryFilePath = "/Volumes/350GB/Documents/workspace/minibase/data/phase3/query_2c.txt";
		String sourceDirPath = "/Volumes/350GB/Documents/workspace/minibase/data/phase3/";

		if (args.length == 2) {
			// Use command line args, else use the above default values
			queryFilePath = args[0].trim();
			sourceDirPath = args[1].trim();
			if (sourceDirPath.charAt(sourceDirPath.length() - 1) != '/')
				sourceDirPath += "/";
		}

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
			// Or we could just do this:
			// ex.printStackTrace();
		}

		System.out.println("Given Query: ");
		int listSize = queryList.size();
		for (int i = 0; i < listSize; ++i)
			System.out.println(queryList.get(i));
		System.out.println();

		String[] filesToRead = queryList.get(1).split(" ");

		// projection fields + offsets
		// Parse a line like R_1 S_1 which indicates 1st column in R and 1st column S
		ArrayList<Integer> projColumnIndices = new ArrayList<>(2); // Assumption: For now there are only two columns
		for (String projStr : queryList.get(0).split(" ")) {
			projColumnIndices.add(Integer.parseInt(projStr.split("_")[1].trim()) - 1 + 3); // Change to zero based index and add offset added by generateData
			// Assumption: The 1st index in the list is for 1st relation and the 2nd is for 2nd relation.
		}
		Integer[] colIndicesWithOffset = new Integer[2];
		projColumnIndices.toArray(colIndicesWithOffset);

		if (queryList.size() == 3) {
			// Single predicate query
			if (filesToRead.length == 2) {
				System.out.println("Running query2c - 2 Tables and 1 Predicate");

				// variable queryList is [1K_1 2K_1, 1K 2K, 1K_3 4 2K_5]
				int t1cond1Col = Integer.parseInt(queryList.get(2).split(" ")[0].trim().split("_")[1].trim()); // get 3 from 1K_3 4 2K_5
				int t2cond1Col = Integer.parseInt(queryList.get(2).split(" ")[2].trim().split("_")[1].trim()); // get 4 from 1K_3 4 2K_5
				int op1 = Integer.parseInt(queryList.get(2).split(" ")[1].trim()); // get 4 from 1K_3 4 2K_4

				// All condition column indices are zero based where as input is 1 based. So subtract 1 from t(i)cond1Col where i = {1, 2}
				Tuple[] T = generateData(sourceDirPath + filesToRead[0] + ".txt", --t1cond1Col, t1cond1Col); // last arg is unused
				Tuple[] T1 = generateData(sourceDirPath + filesToRead[1] + ".txt", --t2cond1Col, t2cond1Col); // last arg is unused

				new IEJoin2Tables1Predicate(T, T1, op1, new int[] { colIndicesWithOffset[0] }, new int[] { colIndicesWithOffset[1] })
						.executeAndPrintResults();
			}
		} else if (queryList.size() == 5) {
			if (filesToRead.length == 2) {
				System.out.println("Running query2c - 2 Tables and 2 Predicates");

				// variable queryList is [R_1 S_1, R S, R_3 2 S_4, AND, R_5 1 S_6]
				int t1cond1Col = Integer.parseInt(queryList.get(2).split(" ")[0].trim().split("_")[1].trim()); // get 3 from R_3 2 S_4
				int t2cond1Col = Integer.parseInt(queryList.get(2).split(" ")[2].trim().split("_")[1].trim()); // get 4 from R_3 2 S_4
				int t1cond2Col = Integer.parseInt(queryList.get(4).split(" ")[0].trim().split("_")[1].trim()); // get 5 from R_5 1 S_6
				int t2cond2Col = Integer.parseInt(queryList.get(4).split(" ")[2].trim().split("_")[1].trim()); // get 6 from R_5 1 S_6
				int op1 = Integer.parseInt(queryList.get(2).split(" ")[1].trim()); // get 2 from R_3 2 S_4
				int op2 = Integer.parseInt(queryList.get(4).split(" ")[1].trim()); // get 1 from R_5 1 S_6

				// All condition column indices are zero based where as input is 1 based. So subtract 1 from t(i)cond(i)Col where i = {1, 2}
				Tuple[] T = generateData(sourceDirPath + filesToRead[0] + ".txt", --t1cond1Col, --t1cond2Col);
				Tuple[] T1 = generateData(sourceDirPath + filesToRead[1] + ".txt", --t2cond1Col, --t2cond2Col);

				new IEJoin2Tables2Predicates(T, T1, op1, op2, new int[] { colIndicesWithOffset[0] }, new int[] { colIndicesWithOffset[1] })
						.executeAndPrintResults();

			}
		} else {
			System.out.println("This query is not formatted for 2c");
		}
	}

	@SuppressWarnings("unused")
	private static void profileTwoPredicates(int op1, int op2, Tuple[] T, Tuple[] T1, int[] colIndicesWithOffset)
			throws FieldNumberOutOfBoundException, IOException {
		long start = System.currentTimeMillis();
		int c = new IEJoin2Tables2Predicates(T, T1, op1, op2, new int[] { colIndicesWithOffset[0] }, new int[] { colIndicesWithOffset[1] })
				.run().size();
		long end = System.currentTimeMillis();
		System.out.format("Found %d tuples\nTime Taken: %d\n", c, end - start);
	}

	@SuppressWarnings("unused")
	private static void profileSinglePredicate(int op1, Tuple[] T, Tuple[] T1, int[] colIndicesWithOffset)
			throws FieldNumberOutOfBoundException, IOException {
		long start = System.currentTimeMillis();
		int c = new IEJoin2Tables1Predicate(T, T1, op1, new int[] { colIndicesWithOffset[0] }, new int[] { colIndicesWithOffset[1] }).run()
				.size();
		long end = System.currentTimeMillis();
		System.out.format("Found %d tuples\nTime Taken: %d\n", c, end - start);
	}
}