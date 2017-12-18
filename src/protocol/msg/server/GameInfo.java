package protocol.msg.server;

import java.nio.ByteBuffer;

import protocol.Player;
import protocol.msg.Message;

public class GameInfo extends Message {
	
	public Player player;
	
	public GameInfo(Player player) {
		id = ServerCmd.GAME_INFO;
		this.player = player;
	}
	
	public GameInfo(ByteBuffer data) {
		serialize(data);
	}

	@Override
	public ByteBuffer serialize() {
		ByteBuffer data = ByteBuffer.allocate(length());
		data.put(id).put( (byte) (player == Player.Black ? 0x01 : 0x02) );
		return data;
	}

	@Override
	public void serialize(ByteBuffer data) {
		player = data.get() == 0x01 ? Player.Black : Player.Blue;
	}

	@Override
	public int length() {
		return 2;
	}

}
