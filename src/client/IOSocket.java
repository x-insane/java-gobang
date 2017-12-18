package client;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

import protocol.Game;
import protocol.GameStatus;
import protocol.Player;
import protocol.msg.client.Play;
import protocol.msg.client.Restart;
import protocol.msg.server.GameInfo;
import protocol.msg.server.OtherPlay;
import protocol.msg.server.Result;
import protocol.msg.server.ServerCmd;

interface UpdateHook {
	public void update();
}

public class IOSocket extends Thread {
	
	private String host = "gobang.gotohope.cn";
	private int port = 13140;
	private Selector selector;
	private SocketChannel sc;
	private UpdateHook update_hook = null;
	
	public Game game;
	public GameStatus status;
	public Player player = Player.None;
	
	public IOSocket() {
		game = new Game();
	}
	
	public IOSocket(String host, int port) {
		this.host = host;
		this.port = port;
		game = new Game();
	}
	
	public void register_update_hook(UpdateHook uh) {
		update_hook = uh;
	}
	
	private void update() {
		if (update_hook != null)
			update_hook.update();
	}
	
	@Override
	public void run() {
		try {
			selector = Selector.open();
			sc = SocketChannel.open();
			sc.configureBlocking(false);
			System.out.print("Connecting server...");
			status = GameStatus.Connecting;
			if (!sc.connect(new InetSocketAddress(host, port))) {
				while (!sc.finishConnect()) {
					System.out.print(".");
					sleep(500);
				}
			}
			System.out.println("\nConnected!");
			status = GameStatus.Matching;
			update();
			sc.register(selector, SelectionKey.OP_READ);
			while(true) {
				selector.select();
				Iterator<SelectionKey> it = selector.selectedKeys().iterator();
				while (it.hasNext()) {
					SelectionKey key = it.next();
					switch (key.readyOps()) {
					case SelectionKey.OP_READ:
						dealRead();
						break;
					}
					it.remove();
				}
			}
		} catch (ConnectException ce) {
			System.out.println("\nConnect fail!");
			status = GameStatus.CannotConnectServer;
			update();
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void Go(int x, int y) {
		game.play(x, y, player);
		write(new Play(x, y).serialize());
		status = GameStatus.WaitOtherGo;
		update();
	}
	
	public void restart() {
		if (game.winner == Player.None)
			return;
		write(new Restart().serialize());
		game.clear();
		status = GameStatus.WaitOtherStart;
		update();
	}
	
	private void dealRead() {
		try {
			while (true) {
				ByteBuffer header = ByteBuffer.allocate(4);
				int size = 0;
				while (size != 4) {
					int tmp = sc.read(header);
					if (tmp == 0 || tmp == -1)
						break;
					size += tmp;
				}
				if (size == 0)
					break;
				if (size != 4)
					throw new RuntimeException("receive broken package header: size="+size);
				header.flip();
				size = header.getInt();
				ByteBuffer data = ByteBuffer.allocate(size);
				int len = 0;
				while (len != size) {
					int tmp = sc.read(data);
					if (tmp == 0 || tmp == -1)
						break;
					len += tmp;
				}
				if (len != size)
					throw new RuntimeException("receive broken package: size=" + len);
				data.flip();
				receive(data);
			}
		} catch (IOException e) {
			status = GameStatus.Disconnected;
			System.out.println("Disconnected!");
			update();
		}
	}
	
	private void receive(ByteBuffer data) {
		byte cmd = data.get();
		switch (cmd) {
		case ServerCmd.GAME_INFO:
			GameInfo info = new GameInfo(data);
			player = info.player;
			game.clear();
			if (player == Player.Black)
				status = GameStatus.WaitGo;
			else
				status = GameStatus.WaitOtherGo;
			break;
		case ServerCmd.OTHER_PLAY:
			OtherPlay point = new OtherPlay(data);
			game.play(point.x, point.y, Player.op(player));
			status = GameStatus.WaitGo;
			break;
		case ServerCmd.RESULT:
			Result result = new Result(data);
			game.winner = result.winner;
			status = GameStatus.Over;
			break;
		case ServerCmd.DISCONNECT:
			System.out.println("The other player disconnected! Rematching...");
			status = GameStatus.OtherDisconnect;
			break;
		}
		update();
	}
	
	public void write(ByteBuffer data) {
		int size = data.position();
		ByteBuffer pack = ByteBuffer.allocate(4 + size);
		pack.putInt(size);
		data.flip();
		pack.put(data);
		pack.flip();
		try {
			while (pack.hasRemaining())
				sc.write(pack);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
