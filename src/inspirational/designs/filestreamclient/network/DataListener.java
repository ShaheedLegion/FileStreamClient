package inspirational.designs.filestreamclient.network;

public interface DataListener {
	public void onDataReceived(ClientSocketConnection.PacketType type, String data);
}
