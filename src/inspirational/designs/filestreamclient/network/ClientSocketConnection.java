package inspirational.designs.filestreamclient.network;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import android.util.Log;

@SuppressWarnings("deprecation")
public class ClientSocketConnection {
	public class ServerPerson {
		public String name;
		public String image;

		public ServerPerson(String name, String image) {
			this.name = name;
			this.image = image;
		}
	}

	public enum PacketType {
		ALIAS(2), ALIAS_ACK(3), QUERY(4), QUERY_ACK(5), MSG(6), MSG_ACK(7), PVT(
				8), PVT_ACK(9), LST(16), LST_ACK(17), FO(18), FO_ACK(19), FI(20), FI_ACK(
				21), JOIN(22), JOIN_ACK(23), LEAVE(24), LEAVE_ACK(25);

		private int type;

		PacketType(int type) {
			this.type = type;
		}

		public int getType() {
			return type;
		}

		public static PacketType getPacketType(int ordinal) {
			PacketType[] values = PacketType.values();
			for (PacketType v : values) {
				if (v.getType() == ordinal)
					return v;
			}
			return null;
		}
	}

	public PacketType getAck(PacketType type) {
		int typeValue = type.getType();
		if (typeValue == PacketType.ALIAS.getType())
			return PacketType.ALIAS_ACK;
		else if (typeValue == PacketType.QUERY.getType())
			return PacketType.QUERY_ACK;
		else if (typeValue == PacketType.MSG.getType())
			return PacketType.MSG_ACK;
		else if (typeValue == PacketType.PVT.getType())
			return PacketType.PVT_ACK;
		else if (typeValue == PacketType.LST.getType())
			return PacketType.LST_ACK;
		else if (typeValue == PacketType.FO.getType())
			return PacketType.FO_ACK;
		else if (typeValue == PacketType.FI.getType())
			return PacketType.FI_ACK;
		else if (typeValue == PacketType.JOIN.getType())
			return PacketType.JOIN_ACK;
		else if (typeValue == PacketType.LEAVE.getType())
			return PacketType.LEAVE_ACK;
		return PacketType.MSG_ACK;
	}

	private String IP = "";
	private int PORT = 54547;
	private int SALT = 0x16540000;
	private Socket m_socket;
	private static ClientSocketConnection instance = null;
	private int sequence = 4;
	private int[] header = new int[7];
	private int[] inHeader = new int[7];
	private DataListener listener = null;
	private boolean isRunning = false;
	private BufferedInputStream inputStream;
	private List<Integer> byteList = new ArrayList<Integer>();
	private final Lock _mutex = new ReentrantLock(true);

	public static ClientSocketConnection getInstance() {
		if (instance == null)
			instance = new ClientSocketConnection();
		return instance;
	}

	private ClientSocketConnection() {

	}

	public void setDataListener(DataListener listener) {
		this.listener = listener;
	}

	// We already know our IP and PORT, so we can simply initiate the
	// connection.
	public void connect(ArrayList<ServerPerson> people) {
		// The first step is to ask for the server ip from the covalent host.
		URI uri;
		URI peopleUri;
		HttpClient httpclient = new DefaultHttpClient();
		try {
			try {
				uri = new URI("http://www.shaheedabdol.co.za/covalent.php?request=fs_clnt_6253_2635");
				HttpResponse response = httpclient.execute(new HttpGet(uri));
				StatusLine statusLine = response.getStatusLine();
				if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					response.getEntity().writeTo(out);
					String responseString = out.toString();
					out.close();
					JSONObject obj = new JSONObject(responseString);

					if (obj.has("result")) {
						String successStr = obj.getString("result");
						if (successStr.equalsIgnoreCase("success")) {
							IP = obj.getString("server_ip");
							PORT = obj.getInt("server_port");
							peopleUri = new URI("http://www.shaheedabdol.co.za/covalent.php?users=fs_clnt_6253_2635");
							HttpResponse peopleResponse = httpclient.execute(new HttpGet(peopleUri));
							statusLine = peopleResponse.getStatusLine();

							if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
								ByteArrayOutputStream peopleout = new ByteArrayOutputStream();
								peopleResponse.getEntity().writeTo(peopleout);
								String peopleResponseString = peopleout.toString();
								peopleout.close();
								JSONObject personObj = new JSONObject(peopleResponseString);

								if (personObj.has("result")) {
									String peopleSuccessStr = personObj.getString("result");
									if (peopleSuccessStr.equalsIgnoreCase("success")) {
										if (personObj.has("users")) {
											JSONObject child = personObj.getJSONObject("users");
											if (child != null) {
												Iterator<?> json_keys = child.keys();
												while (json_keys.hasNext()) {
													String user_name = (String) json_keys.next();
													String user_image = child.getString(user_name);
													people.add(new ServerPerson(user_name, user_image));
												}
											}
										}
									}
								}
							}
						}
					} else {
						response.getEntity().getContent().close();
						Log.d("FileStreamClient", statusLine.getReasonPhrase());
					}
				}
			} catch (URISyntaxException e1) {
				e1.printStackTrace();
			} catch (Throwable t) {
				t.printStackTrace();
			}

			m_socket = new Socket(IP, PORT);
			m_socket.setKeepAlive(true);

			Log.d("FileStreamClient", "IP found connecting to remote host.");
			// Once we have received the packet, we can update the ui.
			// Spin up a thread to send/receive data.
			if (m_socket.isConnected()) {
				Log.d("FileStreamClient", "Socket is connected!");
				isRunning = true;
				inputStream = new BufferedInputStream(m_socket.getInputStream());

				// Now that we have been created, we can spin up the buffers.
				new Thread(new Runnable() {
					@Override
					public void run() {
						while (isRunning) {
							// Fetch as much data as we can.
							try {
								_mutex.lock();
								while (inputStream.available() > 0) {
									// Read it into some kind of buffer thing
									byteList.add(inputStream.read());
								}
								_mutex.unlock();
								processInputBuffer();
							} catch (IOException e) {
								e.printStackTrace();
							}
							try {
								Thread.sleep(100);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					}
				}).start();
			} else
				Log.d("FileStreamClient", "Socket is not connected!");
		} catch (UnknownHostException e) {
			e.printStackTrace();
			Log.d("FileStreamClient", "Could not resolve host.");
		} catch (IOException e) {
			e.printStackTrace();
			Log.d("FileStreamClient", "Some other Error.");
		}
	}

	// This is where it gets very hairy.
	public void send(final PacketType id, final int flags, final String message) {
		new Runnable() {
			@Override
			public void run() {
				sendInternal(id, flags, message);
			}
		}.run();
	}

	public void sendInternal(PacketType id, int flags, String message) {
		// Send the message on another thread.
		// we don't care about the type we are sending - we only care about
		// receiving packets.
		if (m_socket != null && m_socket.isConnected()) {
			OutputStream str;
			try {
				str = m_socket.getOutputStream();
				ByteArrayOutputStream buffer = new ByteArrayOutputStream();

				header[0] = id.getType();
				header[1] = flags;
				header[2] = 0;
				header[3] = 1;
				header[4] = message.length();
				header[5] = sequence++;
				header[6] = 0;

				ByteBuffer buf = ByteBuffer
						.allocate(4 * (header.length + message.length()));
				for (int i = 0; i < 7; ++i)
					buf.putInt(header[i]);

				if (message != null && message.length() > 0)
					for (int i = 0; i < message.length(); ++i)
						buf.putInt(message.charAt(i) + SALT);

				// This gets hairy very quickly.
				buffer.write(buf.array());
				str.write(buffer.toByteArray());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	// This is nearly as hairy as trying to send data generically.
	void processInputBuffer() {
		// Need to spin up a new runnable to pass data back via the callback.
		_mutex.lock();

		if (byteList.size() >= header.length * 4) {
			// If we could construct the packet, then we are sorted ... else we
			// have some hard work to deal with.
			int offset = 0;
			for (int i = 0; i < 7; ++i) {
				int data = byteList.get(offset + 0);
				data <<= 8;
				data |= byteList.get(offset + 1);
				data <<= 8;
				data |= byteList.get(offset + 2);
				data <<= 8;
				data |= byteList.get(offset + 3);

				inHeader[i] = data;

				offset += 4;
			}

			// We have a complete packet.
			if ((byteList.size() - offset) >= inHeader[4] && inHeader[4] >= 0) {
				// We can pull the packet type from the inbound data, then
				// unsalt the packet.
				PacketType type = PacketType.getPacketType(inHeader[0]);
				String msg = "";

				if (inHeader[4] > 0) {
					for (int i = 0; i < inHeader[4]; ++i) {
						int data = byteList.get(offset + 0);
						data <<= 8;
						data |= byteList.get(offset + 1);
						data <<= 8;
						data |= byteList.get(offset + 2);
						data <<= 8;
						data |= byteList.get(offset + 3);
						data -= SALT;

						msg += ("" + (char) data);
						offset += 4;
					}
				}
				// Clear out the list.
				for (int i = 0; i < (offset); ++i)
					byteList.remove(0);

				if (listener != null && msg.length() == inHeader[4]) {
					// Execute the listener on the ui thread. This can't be done
					// here though.
					listener.onDataReceived(type, msg);
				}
			}
		}
		_mutex.unlock();
	}
}
