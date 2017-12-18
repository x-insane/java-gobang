package protocol.msg.server;

import java.nio.ByteBuffer;

import protocol.Player;
import protocol.msg.Message;

public class Result extends Message {
	
	public Player winner;
	
	public Result(Player winner) {
		this.winner = winner;
		id = ServerCmd.RESULT;
	}	
	
	public Result(ByteBuffer data) {
		serialize(data);
	}

	@Override
	public ByteBuffer serialize() {
		ByteBuffer data = ByteBuffer.allocate(length());
		data.put(id).put( (byte) (winner == Player.Black ? 0x01 : 0x02) );
		return data;
	}

	@Override
	public void serialize(ByteBuffer data) {
		winner = data.get() == 0x01 ? Player.Black : Player.Blue;
	}

	@Override
	public int length() {
		return 2;
	}

}
