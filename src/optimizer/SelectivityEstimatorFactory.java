package optimizer;

public class SelectivityEstimatorFactory {
	public static ISelectivityEstimator getEstimator(Estimator e, String relation1, String relation2, String[][] conditions) {
		switch (e) {
		case BY_RANDOM_SAMPLING:
			return new SelectivityEstimationByRandomSampling(relation1, relation2, conditions);

		default:
			return new SelectivityEstimationByRandomSampling(relation1, relation2, conditions);
		}
	}
}
