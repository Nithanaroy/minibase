package optimizer.sampler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Random;

import heap.FieldNumberOutOfBoundException;
import heap.InvalidTupleSizeException;
import heap.InvalidTypeException;
import heap.Tuple;

public class WithReplacementSampler extends ISampler {

	WithReplacementSampler(String relationFilePath, int sampleSize) throws IOException {
		super(relationFilePath, sampleSize);
	}

	@Override
	public Tuple[] getSample()
			throws IOException, NumberFormatException, InvalidTypeException, InvalidTupleSizeException, FieldNumberOutOfBoundException {
		/**
		 * Get the number of lines in the file, L
		 * Generate random indices for lines to select, [0, L)
		 * Return selected lines indices
		 * Note: Assumes whole file fits in memory
		 */
		Random r = new Random();
		List<String> lines = Files.readAllLines(Paths.get(relationFilePath), StandardCharsets.UTF_8);
		if (sampleSize > lines.size())
			throw new IllegalArgumentException(String.format("Given sample size, %d is more than the number of lines, %d in the file, %s",
					sampleSize, lines.size(), relationFilePath));

		Tuple[] selected = new Tuple[sampleSize];
		for (int i = 0; i < sampleSize; i++) {
			selected[i] = createTuple(lines.get(r.nextInt(lines.size())));
		}
		return selected;
	}

	public static void main(String[] args) {
		// Testing the class
		String inputFile = "./data/phase4/F1r.csv";
		int sampleSize = 10;
		try {
			ISampler mySampler = SamplerFactory.getSampler(SamplerType.WITH_REPLACEMENT, inputFile, sampleSize);
			Tuple[] tuples = mySampler.getSample();

			// Print the selected tuples
			printTable(tuples);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
