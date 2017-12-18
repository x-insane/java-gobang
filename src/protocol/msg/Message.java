package protocol.msg;

import java.nio.ByteBuffer;

public abstract class Message {
	public byte id;
	public abstract ByteBuffer serialize();
	public abstract void serialize(ByteBuffer data);
	public abstract int length();
}
