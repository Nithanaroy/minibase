package optimizer;

public class SelectivityEstimatorFactory {
	public static ISelectivityEstimator getEstimator(Estimator e, String relationsDir, String[][] conditions) {
		switch (e) {
		case BY_RANDOM_SAMPLING:
			return new SelectivityEstimationByRandomSampling(relationsDir, conditions);

		default:
			return new SelectivityEstimationByRandomSampling(relationsDir, conditions);
		}
	}
}
