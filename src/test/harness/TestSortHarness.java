package test.harness;

import org.junit.Assert;

import test.Utils;
import flexsc.CompEnv;
import flexsc.Mode;
import flexsc.Party;


public class TestSortHarness<T> extends TestHarness<T>{
	public abstract class Helper {
		int[] intA;
		boolean[][] a;
		Mode m;
		public Helper(int[] aa, Mode m) {
			this.m = m;
			intA = aa;
			a = new boolean[aa.length][32];
			for(int i = 0; i < intA.length; ++i)
				a[i] = Utils.fromInt(aa[i], 32);
		}
		public abstract T[][] secureCompute(T[][] Signala, CompEnv<T> e) throws Exception;
		public abstract int[] plainCompute(int[] intA2);
	}

	class GenRunnable extends network.Server implements Runnable {
		boolean[][] z;
		Helper h;
		GenRunnable (Helper h) {
			this.h = h;
		}

		public void run() {
			try {
				listen(54321);
				@SuppressWarnings("unchecked")
				CompEnv<T> gen = CompEnv.getEnv(h.m, Party.Alice, is, os);

				T[][] a = gen.newTArray(h.a.length, h.a[0].length);//new T[h.a.length][h.a[0].length];
				for(int i = 0; i < a.length; ++i)
					a[i] = gen.inputOfBob(new boolean[32]);

				T[][] d = h.secureCompute(a, gen);
				os.flush();
				
				z = new boolean[d.length][d[0].length];
				for (int i = 0; i < d.length; i++)
					z[i] = gen.outputToAlice(d[i]);
				os.flush();
				
				disconnect();
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
	}

	class EvaRunnable extends network.Client implements Runnable {
		Helper h;
		EvaRunnable (Helper h) {
			this.h = h;
		}

		public void run() {
			try {
				connect("localhost", 54321);				
				@SuppressWarnings("unchecked")
				CompEnv<T> eva = CompEnv.getEnv(h.m, Party.Bob, is, os);
				
				T[][] a = eva.newTArray(h.a.length, h.a[0].length);
				for(int i = 0; i < a.length; ++i)
					a[i] = eva.inputOfBob(h.a[i]);

				T[][] d = h.secureCompute(a, eva);
				
				for (int i = 0; i < d.length; i++)
					eva.outputToAlice(d[i]);
				os.flush();

				disconnect();
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
	}

	public void runThreads(Helper helper) throws Exception {
		GenRunnable gen = new GenRunnable(helper);
		EvaRunnable eva = new EvaRunnable(helper);
		Thread tGen = new Thread(gen);
		Thread tEva = new Thread(eva);
		tGen.start(); Thread.sleep(5);
		tEva.start();
		tGen.join();

		for(int i = 0; i < gen.z.length-1; ++i) {
			Assert.assertTrue(Utils.toInt(gen.z[i]) < Utils.toInt(gen.z[i+1]));
		}
	}

}