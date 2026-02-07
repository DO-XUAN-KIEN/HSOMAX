package Game.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class Message {
	public byte cmd;
	private ByteArrayOutputStream os;
	private DataOutputStream dos;
	private ByteArrayInputStream is;
	private DataInputStream dis;

        
        public byte readByte() throws IOException {
    return dis.readByte(); // hoặc reader.readByte()
}

public int readInt() throws IOException {
    return dis.readInt(); // hoặc reader.readInt()
}

	public Message(int cmd) {
		this.cmd = (byte) cmd;
		this.os = new ByteArrayOutputStream();
		this.dos = new DataOutputStream(os);
	}

	public Message(byte cmd, byte[] data) {
		this.cmd = cmd;
		this.is = new ByteArrayInputStream(data);
		this.dis = new DataInputStream(is);
	}
        public void sendMessage(Message m) {
    try {
        synchronized (dos) {
            byte[] data = m.getData();
            dos.writeShort(data.length);
            dos.write(data);
            dos.flush();
        }
    } catch (IOException e) {
        e.printStackTrace();
    }
}


	public DataOutputStream writer() {
		return dos;
	}

	public DataInputStream reader() {
		return dis;
	}

	public byte[] getData() {
		return os.toByteArray();
	}

	public void cleanup() throws IOException {
		if (os != null) {
			os.close();
		}
		if (is != null) {
			is.close();
		}
		if (dis != null) {
			dis.close();
		}
		if (dos != null) {
			dos.close();
		}
	}
}
