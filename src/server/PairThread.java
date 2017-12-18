package server;

public class PairThread extends Thread {
	
	@Override
	public void run() {
		while (true) {
			try {
				synchronized (ClientModel.game_queue) {
					ClientModel.game_queue.wait();
					if (ClientModel.game_queue.size() >= 2) {
						ClientModel ci1 = ClientModel.game_queue.remove();
						ClientModel ci2 = ClientModel.game_queue.remove();
						ClientModel.make_pair(ci1, ci2);
					}
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
}
