package battlesnake.packets;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

import battlesnake.bootserver.Configuration;
import battlesnake.utils.Path;

/* Handles a TFTP session */
public class TFTPSession {

	/* Session properties */
	private int remoteIP = 0;
	private int remotePort = 0;
	private int blocksize = 512;
	private RandomAccessFile file = null;
	private boolean eof = false;
	private String filename = "";
	private String rootpath = "";
	private long lastactivity = 0;

	/*
	 * Constructor for a session, use TFTPSession.Manager.{New|FindOrStart}
	 * instead of this
	 */
	public TFTPSession(int remoteIP, int remotePort, String rootpath, boolean canreceive) {
		this.remoteIP = remoteIP;
		this.remotePort = remotePort;
		this.rootpath = Path.IncludeTrailingSlash(Path.Slashify(rootpath));
	}

	/* Property getter */
	public int getRemoteIP() {
		return remoteIP;
	}

	/* Property getter */
	public int getRemotePort() {
		return remotePort;
	}

	/* Property getter */
	public int getBlockSize() {
		return blocksize;
	}

	/* Property setter */
	public void setBlockSize(int value) {
		blocksize = value;
	}

	/* Property getter */
	public String getFilename() {
		return filename;
	}

	/* Property getter */
	public String getRootPath() {
		return rootpath;
	}

	/* Close a session */
	public void closeSession() {
		closeFile();
	}

	/* Open a file */
	public void openFile(String filename, boolean write) throws Exception {
		if (file != null)
			throw new Exception("Cannot open a file for this TFTP session, a file is already open");
		if (write)
			throw new Exception("Writing not implemented");
		file = new RandomAccessFile(filename, "r");
		eof = file.length() == 0;
	}

	/* Close the file */
	public void closeFile() {
		if (file != null) {
			try {
				file.close();
			}
			catch (IOException e) {
			}
			file = null;
		}
	}

	/* At end of file? */
	public boolean endOfFile() {
		return eof;
	}

	/* Get the size of the open file */
	public long getFileSize() throws IOException {
		return file.length();
	}

	/* Read a block from a file */
	public boolean readFile(TFTPPacket reply) throws IOException {
		byte[] buff = new byte[this.blocksize];
		if (file.getFilePointer() != (long) this.blocksize * (reply.block_id - 1))
			file.seek((long) this.blocksize * (reply.block_id - 1));
		int len = file.read(buff);
		if (len < 0) {
			len = 0;
			eof = true;
			return false;
		}
		reply.data = ByteBuffer.wrap(buff, 0, len);
		return true;
	}

	/* Keep session active */
	public void heartbeat() {
		lastactivity = new Date().getTime();
	}

	/* Session manager */
	public static class Manager {

		/* Session list */
		private static Map<Integer, TFTPSession> Sessions = new TreeMap<Integer, TFTPSession>();
		/* Session timeout value (seconds) */
		public static int Timeout = 300;

		/* Create a new session */
		public static TFTPSession New(int remoteIP, int remotePort) {
			TFTPSession ses = new TFTPSession(remoteIP, remotePort, Configuration.tftp_root_folder, false);
			Sessions.put(remoteIP, ses);
			return ses;
		}

		/* Kill a session */
		public static void End(int remoteIP) {
			Sessions.remove(remoteIP);
		}

		/* Find a session by client IP */
		public static TFTPSession Find(int remoteIP) {
			if (Sessions.containsKey(remoteIP))
				return Sessions.get(remoteIP);
			else
				return null;
		}

		/*
		 * Get a session for a specific client, creating a new one if
		 * needed
		 */
		public static TFTPSession FindOrStart(int remoteIP, int remotePort) {
			if (Sessions.containsKey(remoteIP))
				return Sessions.get(remoteIP);
			else
				return New(remoteIP, remotePort);
		}

		/* Remove dead (timed-out) sessions */
		public static void Prune() {
			for (Map.Entry<Integer, TFTPSession> e : Sessions.entrySet())
				if (e.getValue().lastactivity + Timeout * 1000 < new Date().getTime()) {
					e.getValue().closeSession();
					Sessions.remove(e.getKey());
				}
		}
	}

}
