package bitBlaster;

import dataPoints.cartesian.CartesianPoint;
import testloads.TestContext;
import testloads.TestContext.Context;

import java.util.BitSet;
import java.util.List;

public class Main {

    public static void main(String[] args) throws Exception {

        int noOfRefPoints = 40;
        final int nChoose2 = ((noOfRefPoints - 1) * noOfRefPoints) / 2;
        System.out.println(nChoose2);

        TestContext tc = new TestContext(Context.colors);
        tc.setSizes(tc.dataSize() / 10, noOfRefPoints);
        List<CartesianPoint> dat = tc.getData();
        List<CartesianPoint> refs = tc.getRefPoints();
        System.out.println(dat.size());
        System.out.println(refs.size());

        double[][] refDists = new double[noOfRefPoints][noOfRefPoints];
        for (int i = 0; i < noOfRefPoints - 1; i++) {
            for (int j = i + 1; j < noOfRefPoints; j++) {
                final double d = tc.metric().distance(refs.get(i), refs.get(j));
                refDists[i][j] = d;
            }
        }

        BitSet[] datarep = new BitSet[dat.size()];

        for (int n = 0; n < dat.size(); n++) {
            CartesianPoint p = dat.get(n);
            BitSet bs = new BitSet(nChoose2);
            int ptr = 0;
            for (int i = 0; i < noOfRefPoints - 1; i++) {
                final double d1 = tc.metric().distance(refs.get(i), p);
                for (int j = i + 1; j < noOfRefPoints; j++) {
                    final double d2 = tc.metric().distance(refs.get(j), p);
                    boolean leftCloser = d1 < d2;
                    bs.set(ptr, leftCloser);
                    ptr++;
                }
            }
            datarep[n] = bs;
        }

        double t = tc.getThreshold();
        List<CartesianPoint> queries = tc.getQueries();

        int sols = 0;
        int checks = 0;
        long t0 = System.currentTimeMillis();
        for (CartesianPoint q : queries) {

            BitSet bs1 = new BitSet(nChoose2);
            BitSet bs2 = new BitSet(nChoose2);

            int ptr = 0;
            for (int i = 0; i < noOfRefPoints - 1; i++) {
                double d1 = tc.metric().distance(refs.get(i), q);
                if (d1 < t) {
                    sols++;
                }
                for (int j = i + 1; j < noOfRefPoints; j++) {
                    double d2 = tc.metric().distance(refs.get(j), q);
                    if (j == noOfRefPoints - 1 && d2 < t) {
                        sols++;
                    }

// if ((d2 * d2 - d1 * d1) / refDists[i][j] > 2 * t) {
                    if ((d2 - d1) > 2 * t) {
                        bs1.set(ptr, true);
// } else if ((d1 * d1 - d2 * d2) / refDists[i][j] > 2 *
// t) {
                    } else if ((d1 - d2) > 2 * t) {
                        bs2.set(ptr, true);
                    }
                    ptr++;
                }
            }
            for (int i = 0; i < datarep.length; i++) {
                BitSet sbs = datarep[i].get(0, nChoose2);
                sbs.or(bs1);
                boolean notExcluded = sbs.equals(datarep[i]);
                if (notExcluded) {
                    sbs.flip(0, nChoose2);
                    sbs.or(bs2);
                    sbs.flip(0, nChoose2);
                    notExcluded = sbs.equals(datarep[i]);

                    if (notExcluded) {
                        checks++;
                        if (tc.metric().distance(q, dat.get(i)) < t) {
                            sols++;
                        }
                    }
                }
            }

        }

        System.out.println(checks / queries.size() + noOfRefPoints);
        System.out.println(sols);
        System.out.println("" + (System.currentTimeMillis() - t0));

    }
}