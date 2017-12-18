package server;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class IOThread extends Thread {
	
	private Selector selector;
	public Queue<SocketChannel> sc_queue = new LinkedBlockingQueue<SocketChannel>();
	
	public IOThread() {
		try {
			selector = Selector.open();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		while(true) {
			try {
				selector.select(1000);
				Iterator<SelectionKey> it = selector.selectedKeys().iterator();
				while (it.hasNext()) {
					SelectionKey key = it.next();
					switch (key.readyOps()) {
					case SelectionKey.OP_READ:
						dealRead(key);
						break;
					}
					it.remove();
				}
				synchronized (sc_queue) {
					while (!sc_queue.isEmpty())
						accept(sc_queue.remove());
				}
			} catch (IOException e) {
				e.printStackTrace();
				try {
					sleep(1000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		}
	}
	
	private void dealRead(SelectionKey key) {
		SocketChannel sc = (SocketChannel)key.channel();
		ClientModel.map.get(sc).read();
	}
	
	private void accept(SocketChannel sc) throws IOException {
		sc.configureBlocking(false);
		sc.register(selector, SelectionKey.OP_READ);
		ClientModel ci = new ClientModel(sc);
		ClientModel.map.put(sc, ci);
		synchronized (ClientModel.game_queue) {
			ClientModel.game_queue.add(ci);
			ClientModel.game_queue.notifyAll();
		}
		System.out.println("A player connected: " + sc.socket().getRemoteSocketAddress().toString());
	}
	
}
