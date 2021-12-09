package util;

import java.util.ArrayList;
import java.util.List;

public class Timer {

	public abstract static class Command {
		public abstract void execute();
	}

	public abstract static class CommandW {
		public abstract void execute(boolean warmup);
	}

	public static double time(final CommandW cw, boolean accurate) {

		cw.execute(true);
		System.gc();

		Command c = new Command() {

			@Override
			public void execute() {
				cw.execute(false);
			}
		};
		return time(c, accurate);
	}

	/**
	 * @param c
	 * @param accurate
	 * @return mean time in milliseconds
	 */
	@SuppressWarnings("boxing")
	@Deprecated
	public static double time(Command c, boolean accurate) {

		List<Integer> ts = new ArrayList<Integer>();
		boolean finished = false;
		double mean = 0;
		int iterations = 0;
		while (!finished) {
			System.gc();

			long t0 = System.currentTimeMillis();
			c.execute();
			long t = System.currentTimeMillis() - t0;

			ts.add((int) t);
			mean = mean(ts);

			double stDev = stDev(ts, mean);
			double stdErrMean = stDev / Math.sqrt(ts.size());

			if (!accurate || (iterations > 4 && (stdErrMean / mean) < 0.01)
					|| iterations > 30) {
				finished = true;
			}

			iterations++;
		}

		return mean;

	}

	private static double stDev(List<Integer> vals, double mean) {
		double acc = 0;
		for (double d : vals) {
			acc += (mean - d) * (mean - d);
		}
		return Math.sqrt(acc / vals.size());
	}

	private static double mean(List<Integer> vals) {
		double acc = 0;
		for (double d : vals) {
			acc += d;
		}
		return acc / (double) vals.size();
	}
}
