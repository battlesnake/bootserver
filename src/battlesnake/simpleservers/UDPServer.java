package battlesnake.simpleservers;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;

import battlesnake.logging.Log;
import battlesnake.packets.IP;

/* A server that responds to UDP packets on a specific port */
public abstract class UDPServer extends Server {

	DatagramSocket socket = null;
	private boolean stopping = false;
	private boolean running = false;

	protected void Send(int remoteport, InetAddress targetaddress,
			ByteBuffer data) throws Exception {
		socket.send(new DatagramPacket(data.array(), data.limit(),
				targetaddress, remoteport));
	}

	protected void Send(SocketAddress targetaddress, ByteBuffer data)
			throws Exception {
		socket.send(new DatagramPacket(data.array(), data.limit(),
				targetaddress));
	}

	protected abstract boolean OnReceive(InetSocketAddress sender,
			int remoteport, ByteBuffer data) throws Exception;

	protected abstract int getPort();

	protected abstract int getInterfaceAddress();

	@Override
	protected void PreRun() throws Exception {
		socket = new DatagramSocket(getPort(), InetAddress.getByAddress(IP
				.IntToBytes(getInterfaceAddress())));
		socket.setBroadcast(true);
		socket.setReuseAddress(true);
		socket.setSoTimeout(100);
	}

	@Override
	protected void PostRun() throws Exception {
		if (socket != null) {
			socket.close();
			socket = null;
		}
	}

	@Override
	public void Run() throws Exception {
		byte[] buffer = new byte[65536];
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
		while (!getStopping()) {
			try {
				socket.receive(packet);
			} catch (SocketTimeoutException e) {
				if (getStopping())
					break;
				else
					continue;
			}
			InetSocketAddress sender = ((InetSocketAddress) packet
					.getSocketAddress());
			try {
				if (!this.OnReceive(sender, packet.getPort(),
						ByteBuffer.wrap(buffer, 0, packet.getLength())))
					break;
			} catch (Exception e) {
				Log.Add(this, Log.TYPE_ERR, "Untrapped error ("
						+ e.getClass().getSimpleName()
						+ ") in message handler: " + e.getMessage());
			}
		}
	}

	@Override
	public void Stop() {
		setStopping(true);
	}

	@Override
	public synchronized void setStopping(boolean value) {
		stopping = value;
	}

	@Override
	public synchronized boolean getStopping() {
		return stopping;
	}

	@Override
	public synchronized void setRunning(boolean value) {
		running = value;
	}

	@Override
	public synchronized boolean getRunning() {
		return running;
	}

}
