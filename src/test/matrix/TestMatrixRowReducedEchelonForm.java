package test.matrix;

import org.junit.Test;

import test.harness.TestHarness;
import test.harness.TestMatrix;
import test.harness.TestMatrix.Helper;
import circuits.arithmetic.DenseMatrixLib;

public class TestMatrixRowReducedEchelonForm extends TestHarness {

	@Test
	public void testAllCases() throws Exception {
		for (int i = 0; i < 1; i++) {
			double[][] d1 = TestMatrix.randomMatrix(5, 5);
			double[][] d2 = TestMatrix.randomMatrix(5, 5);

			TestMatrix.runThreads(new Helper(d1, d2) {

				@Override
				public <T>T[][][] secureCompute(T[][][] a,
						T[][][] b, DenseMatrixLib<T> lib)
						throws Exception {
					return lib.rref(a);
				}

				@Override
				public double[][] plainCompute(double[][] a, double[][] b) {
					return DenseMatrixLib.rref(a);
				}
			});
		}
	}
}