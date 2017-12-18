package server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;
import java.util.Random;

public final class GoBangServer extends Thread {
	
	private int port;
	private Selector selector;
	private PairThread pair_thread;
	private IOThread[] io_thread_pool;
	private static final int pool_size = 20;
	private Random rand = new Random();
	
	public GoBangServer(int port) {
		this.port = port;
		try {
			selector = Selector.open();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		try {
			ServerSocketChannel ssc = ServerSocketChannel.open();
			ssc.configureBlocking(false);
			ssc.socket().bind(new InetSocketAddress(port));
			ssc.register(selector, SelectionKey.OP_ACCEPT);
			// 启动匹配线程
			pair_thread = new PairThread();
			pair_thread.start();
			System.out.println("make_pair_thread start!");
			// 启动IO线程
			io_thread_pool = new IOThread[pool_size];
			for (int i=0;i<pool_size;i++) {
				io_thread_pool[i] = new IOThread();
				io_thread_pool[i].start();
			}
			System.out.println(pool_size + " io_thread start!\nListen on port " + port + "...");
			while(true) {
				selector.select();
				Iterator<SelectionKey> it = selector.selectedKeys().iterator();
				while (it.hasNext()) {
					SelectionKey key = it.next();
					switch (key.readyOps()) {
					case SelectionKey.OP_ACCEPT:
						dealAccept(key);
						break;
					}
					it.remove();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void dealAccept(SelectionKey key) {
		ServerSocketChannel server = (ServerSocketChannel)key.channel();
		try {
			IOThread ioThread = io_thread_pool[rand.nextInt(pool_size)];
			synchronized (ioThread.sc_queue) {
				ioThread.sc_queue.add(server.accept());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
