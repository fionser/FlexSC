package test.oram;

import java.security.SecureRandom;

import oram.CircuitOram;

import org.junit.Test;

import util.Utils;
import flexsc.CompEnv;
import flexsc.Mode;
import flexsc.PMCompEnv;
import flexsc.Party;


public class CountCircuitOramBasic {
	int N;
	int logN;
	final int capacity = 3;
	int writecount = 1;
	int dataSize;

	public CountCircuitOramBasic() {
	}

	SecureRandom rng = new SecureRandom();
	boolean breaksignal = false;

	class GenRunnable extends network.Server implements Runnable {
		int port;

		GenRunnable(int port) {
			this.port = port;
		}


		public void run() {
			try {
				listen(port);

				@SuppressWarnings("unchecked")
				CompEnv<Boolean> env = CompEnv.getEnv(Mode.COUNT, Party.Alice,
						is, os);
				CircuitOram<Boolean> client = new CircuitOram<Boolean>(env, N,
						dataSize, capacity, 80);
//				System.out.println("logN:" + client.logN + ", N:" + client.N);

				for (int i = 0; i < writecount; ++i) {
					Boolean[] scNewValue = client.env.inputOfAlice(Utils
							.fromInt(1, client.lengthOfPos));
					Boolean[] scData = client.env.inputOfAlice(Utils.fromInt(
							1, client.lengthOfData));
					PMCompEnv pm = (PMCompEnv) env;
					pm.statistic.flush();
					client.write(client.lib.toSignals(1),
							Utils.fromInt(1, client.lengthOfPos),
							scNewValue, scData);

					System.out.println(pm.statistic.andGate*(logN*4-7));
					os.flush();
					
				}

				disconnect();
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
	}

	class EvaRunnable extends network.Client implements Runnable {
		String host;
		int port;


		EvaRunnable(String host, int port) {
			this.host = host;
			this.port = port;
		}

		public void run() {
			try {
				connect(host, port);

				@SuppressWarnings("unchecked")
				CompEnv<Boolean> env = CompEnv.getEnv(Mode.COUNT, Party.Bob,
						is, os);
				CircuitOram<Boolean> server = new CircuitOram<Boolean>(env, N,
						dataSize, capacity, 80);

				for (int i = 0; i < writecount; ++i) {
					Boolean[] scNewValue = server.env
							.inputOfAlice(new boolean[server.lengthOfPos]);
					Boolean[] scData = server.env
							.inputOfAlice(new boolean[server.lengthOfData]);

					server.write(server.lib.toSignals(1),
							Utils.fromInt(1, server.lengthOfPos),
							scNewValue, scData);
				}


				disconnect();
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
	}

	GenRunnable gen = new GenRunnable(1234);
	EvaRunnable eva = new EvaRunnable("localhost", 1234);

	@Test
	public void runThreads() throws Exception {
		for(int i = 10; i <= 30; i+=2 ) {
			this.logN = i;
			this.N = 1<<i;
			this.dataSize = i+i+i+i+32+32;
			System.out.print(i+"\t");
		Thread tGen = new Thread(gen);
		Thread tEva = new Thread(eva);
		tGen.start();
		Thread.sleep(10);
		tEva.start();
		tGen.join();
//		System.out.println();
		}
	}
}