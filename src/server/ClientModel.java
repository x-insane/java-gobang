package server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import protocol.Game;
import protocol.GameStatus;
import protocol.Player;
import protocol.msg.client.ClientCmd;
import protocol.msg.client.Play;
import protocol.msg.server.DisConnect;
import protocol.msg.server.GameInfo;
import protocol.msg.server.OtherPlay;
import protocol.msg.server.Result;

public class ClientModel {
	
	public static Map<SocketChannel, ClientModel> map = new HashMap<SocketChannel, ClientModel>();
	public static Queue<ClientModel> game_queue = new LinkedBlockingQueue<ClientModel>();
	
	private SocketChannel sc;
	private ClientModel pair;
	private Game game;
	private Player player;
	private GameStatus status;
	
	public static void make_pair(ClientModel a, ClientModel b) {
		if (!a.sc.isConnected() || !b.sc.isConnected()) {
			if (a.sc.isConnected())
				game_queue.add(a);
			if (b.sc.isConnected())
				game_queue.add(b);
			game_queue.notifyAll();
			return;
		}
		a.pair = b;
		b.pair = a;
		a.game = b.game = new Game();
		a.status = b.status = GameStatus.WaitStart;
		a.player = Player.Black;
		b.player = Player.Blue;
		a.write(new GameInfo(a.player).serialize());
		b.write(new GameInfo(b.player).serialize());
		System.out.println("A new game start!");
	}
	
	public ClientModel(SocketChannel sc) {
		this.sc = sc;
		status = GameStatus.Matching;
	}
	
	public synchronized void receive(ByteBuffer data) {
		byte cmd = data.get();
		switch (cmd) {
		case ClientCmd.PLAY:
			Play point = new Play(data);
			game.play(point.x, point.y, player);
			pair.write(new OtherPlay(point.x, point.y).serialize());
			pair.status = GameStatus.WaitGo;
			status = GameStatus.WaitOtherGo;
			Player winner = game.winner;
			if (winner != Player.None) {
				write(new Result(winner).serialize());
				pair.write(new Result(winner).serialize());
				status = pair.status = GameStatus.Over;
			}
			break;
		case ClientCmd.RESTART:
			status = GameStatus.WaitOtherStart;
			if (pair.status == GameStatus.WaitOtherStart) {
				game.clear();
				player = Player.op(player);
				pair.player = Player.op(pair.player);
				write(new GameInfo(player).serialize());
				pair.write(new GameInfo(pair.player).serialize());
				if (player == Player.Black) {
					status = GameStatus.WaitGo;
					pair.status = GameStatus.WaitOtherGo;
				} else {
					pair.status = GameStatus.WaitGo;
					status = GameStatus.WaitOtherGo;
				}
			}
			break;
		}
	}
	
	public synchronized void write(ByteBuffer data) {
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
			System.out.println("A player disconnected.");
			close();
		}
	}
	
	public synchronized void read() {
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
					throw new RuntimeException("receive broken package header");
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
					throw new RuntimeException("receive broken package");
				data.flip();
				receive(data);
			}
		} catch (IOException e) {
			System.out.println("A player disconnected.");
			close();
		}
	}
	
	public synchronized void close() {
		if (pair != null) {
			pair.write(new DisConnect().serialize());
			pair.pair = null;
			pair.close();
			pair = null;
			try {
				sc.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			map.remove(sc);
		} else {
			if (status == GameStatus.Rematching || status == GameStatus.Matching) {
				try {
					sc.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				map.remove(sc);
			} else {
				status = GameStatus.Rematching;
				game_queue.add(this); // 重新匹配
			}
		}
	}
}
