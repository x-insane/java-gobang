package protocol.msg.client;

import java.nio.ByteBuffer;

import protocol.msg.Message;

public class Play extends Message {
	
	public byte x, y;
	
	public Play(int x, int y) {
		this.x = (byte) x;
		this.y = (byte) y;
		id = ClientCmd.PLAY;
	}	
	
	public Play(ByteBuffer data) {
		serialize(data);
	}

	@Override
	public ByteBuffer serialize() {
		ByteBuffer data = ByteBuffer.allocate(length());
		data.put(id).put(x).put(y);
		return data;
	}

	@Override
	public void serialize(ByteBuffer data) {
		x = data.get();
		y = data.get();
	}

	@Override
	public int length() {
		return 3;
	}

}
