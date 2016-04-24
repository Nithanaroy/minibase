package optimizer.estimator;

public class SelectivityEstimatorFactory {
	public static ISelectivityEstimator getEstimator(Estimator e, String relationsDir, String[][] conditions) {
		switch (e) {
		case BY_RANDOM_SAMPLING:
			return new SelectivityEstimationByRandomSampling(relationsDir, conditions);

		case UNIFORM_SAMPLING:
			return new SelectivityEstimationByUniformSampling(relationsDir, conditions);

		default:
			return new SelectivityEstimationByRandomSampling(relationsDir, conditions);
		}
	}
}
