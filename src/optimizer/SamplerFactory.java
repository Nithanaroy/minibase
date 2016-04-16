package optimizer;

import java.io.IOException;

public class SamplerFactory {
	/**
	 * Instantiates a new ISampler of the given type
	 * 
	 * @param samplerName name of the sampler
	 * @param inputRelation complete file path of the relation to sample in CSV format
	 * @param sampleSize size of the sample, i.e. number of tuples
	 * @return a sampler instance
	 */
	public static ISampler getSampler(SamplerType samplerName, String inputRealation, int sampleSize) throws IOException {
		switch (samplerName) {
		case WITH_REPLACEMENT:
			return new WithReplacementSampler(inputRealation, sampleSize);

		case WITHOUT_REPLACEMENT:
			return new WithoutReplacementSampler(inputRealation, sampleSize);

		default:
			return new WithReplacementSampler(inputRealation, sampleSize);
		}
	}
}
