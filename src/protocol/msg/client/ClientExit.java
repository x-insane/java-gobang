package protocol.msg.client;

import java.nio.ByteBuffer;

import protocol.msg.Message;

public class ClientExit extends Message {

	public ClientExit() {
		id = ClientCmd.EXIT;
	}	
	
	public ClientExit(ByteBuffer data) {
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
