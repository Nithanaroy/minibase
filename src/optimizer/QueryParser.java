package optimizer;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * TODO: Place this file in the correct package, 'optimizer' is not the right place
 * 
 * Parses the query file
 * 
 * @author nitinpasumarthy
 *
 */
public class QueryParser {

	/**
	 * 
	 * 
	 * 
	 */

	/**
	 * Parses the query file and separates into individual components
	 * 
	 * Sample input:
	 * R_1 S_1
	 * R S
	 * R_3 4 S_3
	 * AND
	 * R_4 4 S_4
	 * @param queryFilePath complete file path of the query
	 * @return A HashMap, {"projection": [ [R,1,S,1] ], "tables": [ [R,S] ], "conditions": [ [R,3,4,S,3], [R,4,4,S,4] ] }
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static HashMap<String, String[][]> parse(String queryFilePath) throws FileNotFoundException, IOException {
		String[] projection, tables;
		String[][] conditions;
		List<String[]> conditions_temp;
		try (BufferedReader br = new BufferedReader(new FileReader(queryFilePath))) {
			/**
			 * R_1 S_1		=> [R, 1, S, 1] 	<-- projection
			 * R S			=> [R, S]			<-- tables
			 * R_3 4 S_3	=> [R, 3, 4, S, 3]	<-- condition 1
			 * AND			=> IGNORE			<-- Assumption: this line is ignored as conditions are always joined by AND
			 * R_4 4 S_4	=> [R, 4, 4, S, 4]	<-- conditon 2
			 */
			projection = br.readLine().trim().split("[ _]"); // first line has projection information
			tables = br.readLine().trim().split(" "); // second line has tables list
			conditions_temp = new ArrayList<>();
			String cond = null;
			while ((cond = br.readLine()) != null) {
				if (cond.equals("AND") || cond.equals("OR"))
					continue;
				conditions_temp.add(cond.trim().split("[ _]"));
			}
			conditions = new String[conditions_temp.size()][];
			HashMap<String, String[][]> res = new HashMap<>(3);
			res.put("projection", new String[][] { projection });
			res.put("tables", new String[][] { tables });
			res.put("conditions", conditions_temp.toArray(conditions));
			return res;
		}
	}

	public static void main(String[] args) {
		try {
			QueryParser.parse("./data/phase3/query_2c.txt");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
