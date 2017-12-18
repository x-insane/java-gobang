package protocol.msg.server;

import java.nio.ByteBuffer;

import protocol.msg.Message;

public class DisConnect extends Message {

	public DisConnect() {
		id = ServerCmd.DISCONNECT;
	}	
	
	public DisConnect(ByteBuffer data) {
		serialize(data);
	}
	
	@Override
	public ByteBuffer serialize() {
		ByteBuffer data = ByteBuffer.allocate(length());
		data.put(id);
		return data;
	}

	@Override
	public void serialize(ByteBuffer data) {
		
	}

	@Override
	public int length() {
		return 1;
	}

}
