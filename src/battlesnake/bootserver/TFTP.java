package battlesnake.bootserver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import battlesnake.logging.Log;
import battlesnake.packets.IP;
import battlesnake.packets.TFTPPacket;
import battlesnake.packets.TFTPSession;
import battlesnake.simpleservers.UDPServer;
import battlesnake.utils.Path;

/* TFTP server */
public class TFTP extends UDPServer {

	/* Log symbolic link traversals (Windows symlink hack only) */
	private static boolean LogSymlinks = false;

	/* Get the port that the server is bound to */
	@Override
	protected int getPort() {
		return Configuration.tftp_server_port;
	}

	/* Get the address that the server is bound to */
	@Override
	protected int getInterfaceAddress() {
		return Configuration.network_address;
	}

	/* Sends a TFTP packet */
	private void Send(InetSocketAddress Target, int Port, TFTPPacket rep) throws Exception {
		super.Send(Port, Target.getAddress(), rep.Encode());
	}

	/* Handle a RRQ */
	private TFTPPacket onReadRequest(TFTPPacket pkt, TFTPSession ses) throws Exception {
		/* Get target */
		String targetPath = Path.ParseDots(Path.Slashify(pkt.filename), false, false);
		/* Window symlink hack */
		if (Configuration.tftp_windows_symlink_hack) {
			String followedPath;
			while ((followedPath = FollowSymlink(ses.getRootPath(), targetPath)) != null) {
				if (LogSymlinks)
					Log.Add(this, Log.TYPE_INFO, "Followed symlink from \"" + targetPath + "\" to \"" + followedPath + "\".");
				targetPath = followedPath;
			}
		}
		/* Open the file */
		ses.openFile(ses.getRootPath() + targetPath, false);
		/* If packet has options, parse and acknowledge them */
		if (pkt.hasOptions())
			return onOptions(pkt, ses);
		/* Otherwise, start reading the file and send data back */
		TFTPPacket rep = TFTPPacket.DATA(1, null);
		ses.readFile(rep);
		return rep;
	}

	/* Handle options */
	private TFTPPacket onOptions(TFTPPacket pkt, TFTPSession ses) throws Exception {
		List<String> k = new ArrayList<String>();
		List<String> v = new ArrayList<String>();
		if (pkt.hasOption(TFTPPacket.OPTION_BLOCKSIZE)) {
			ses.setBlockSize(Integer.parseInt(pkt.getOption(TFTPPacket.OPTION_BLOCKSIZE)));
			k.add(TFTPPacket.OPTION_BLOCKSIZE);
			v.add(((Integer) ses.getBlockSize()).toString());
		}
		if (pkt.hasOption(TFTPPacket.OPTION_FILESIZE)) {
			k.add(TFTPPacket.OPTION_FILESIZE);
			v.add(((Long) ses.getFileSize()).toString());
		}
		return TFTPPacket.OACK(k.toArray(new String[] {}), v.toArray(new String[] {}));
	}

	/* Handle a WRQ */
	private TFTPPacket onWriteRequest(TFTPPacket pkt, TFTPSession ses) throws Exception {
		return TFTPPacket.ERROR(TFTPPacket.ERROR_ACCESS_VIOLATION, "Writing is not implemented on this server");
	}

	/* Handle data */
	private TFTPPacket onData(TFTPPacket pkt, TFTPSession ses) throws Exception {
		return TFTPPacket.ERROR(TFTPPacket.ERROR_ACCESS_VIOLATION, "Writing is not implemented on this server");
	}

	/* Handle a data acknowledge */
	private TFTPPacket onAcknowledge(TFTPPacket pkt, TFTPSession ses) throws Exception {
		TFTPPacket rep = TFTPPacket.DATA(pkt.block_id + 1, null);
		ses.readFile(rep);
		return rep;
	}

	/* Follow a symbolic link (Windows symlink hack) */
	/*
	 * Note: do not call if the path is already valid, it will fail rather
	 * than passing through
	 */
	private String FollowSymlink(String root, String rel) throws Exception {
		/* Get relative path */
		rel = Path.ParseDots(Path.Slashify(rel), false, false);
		/* Split relative path into individual elements */
		String[] rea = Path.SplitPath(rel);
		String pre = "", post = "";
		/*
		 * Rebuild path (TODO: implement following of double-symlinked
		 * directories)
		 */
		for (int i = 0; i < rea.length; i++) {
			if (i > 0)
				pre += File.separator;
			pre += rea[i];
			/*
			 * If current part of path is a file, store the
			 * remainder of the path and attempt to parse the file
			 * as a symlink
			 */
			if (new File(root + pre).exists() && new File(root + pre).isFile()) {
				for (int j = i + 1; j < rea.length; j++)
					post += File.separator + rea[j];
				break;
			}
		}
		/* Sanity check */
		if (!new File(root + pre).exists() || !new File(root + pre).isFile())
			return null;
		/* Open the file */
		RandomAccessFile f = new RandomAccessFile(root + pre, "r");
		try {
			/* File size check */
			if (f.length() >= 200 || f.length() == 0)
				return null;
			/*
			 * Read file a byte at a time, verifying chars are in
			 * range 0x20..0x7F
			 */
			byte[] t = new byte[200];
			int p = 0;
			int i;
			while ((i = f.read()) != -1) {
				if (i < 32 || i > 127)
					return null;
				t[p] = (byte) i;
				p++;
			}
			/* Clean the path and test it */
			String s = Path.Slashify(new String(t).substring(0, p));
			String base = new File(pre).getParent();
			if (base == null)
				base = "";
			base = Path.ParseDots(Path.IncludeTrailingSlash(base) + s, false, false);
			/* If path is a valid file, return it */
			String full = base + post;
			if (s.length() > 0 && new File(root + full).isFile())
				return full;
			/* Otherwise fail */
			return null;
		}
		/* Close the file */
		finally {
			f.close();
		}
	}

	/* Handles received packets */
	@Override
	protected boolean OnReceive(InetSocketAddress sender, int remotePort, ByteBuffer data) throws Exception {
		/* Get client ip address */
		int addr = IP.BytesToInt(sender.getAddress().getAddress());
		/* Get session */
		TFTPSession ses = TFTPSession.Manager.FindOrStart(addr, remotePort);
		/* Parse packet */
		TFTPPacket pkt = new TFTPPacket(data);
		/* Log received packet if configuration specifies to */
		if (pkt.op != TFTPPacket.OP_DATA && pkt.op != TFTPPacket.OP_ACK && Configuration.tftp_log_packets)
			Log.Add(this, Log.TYPE_INFO, "TFTP " + TFTPPacket.GetOpText(pkt.op) + " received from " + IP.IntToStr(addr));
		/* Generate a reply */
		ses.heartbeat();
		TFTPPacket rep = null;
		try {
			switch (pkt.op) {
			case TFTPPacket.OP_RRQ:
				rep = onReadRequest(pkt, ses);
				break;

			case TFTPPacket.OP_WRQ:
				rep = onWriteRequest(pkt, ses);
				break;
			case TFTPPacket.OP_OACK:
				rep = TFTPPacket.ACK(0);
				break;
			case TFTPPacket.OP_DATA:
				rep = onData(pkt, ses);
				break;
			case TFTPPacket.OP_ACK:
				if (!ses.endOfFile())
					rep = onAcknowledge(pkt, ses);
				break;
			case TFTPPacket.OP_ERROR:
			default:
				rep = null;
			}
		}
		/* File not found */
		catch (FileNotFoundException e) {
			rep = TFTPPacket.ERROR(TFTPPacket.ERROR_FILE_NOT_FOUND, e.getMessage());
		}
		/* I/O error */
		catch (IOException e) {
			rep = TFTPPacket.ERROR(TFTPPacket.ERROR_UNDEFINED, e.getMessage());
		}
		/* Unknown error */
		catch (Exception e) {
			rep = TFTPPacket.ERROR(TFTPPacket.ERROR_UNDEFINED, e.getMessage());
		}
		/* Log and send reply */
		if (rep != null) {
			/* Log reply packet */
			if (rep.op == TFTPPacket.OP_RRQ || rep.op == TFTPPacket.OP_WRQ || rep.op == TFTPPacket.OP_OACK && Configuration.tftp_log_packets)
				Log.Add(this, Log.TYPE_INFO, "TFTP " + TFTPPacket.GetOpText(rep.op) + " sent to " + IP.IntToStr(addr));
			if (rep.op == TFTPPacket.OP_ERROR && Configuration.tftp_log_packets)
				Log.Add(this, Log.TYPE_INFO, "TFTP " + TFTPPacket.GetOpText(rep.op) + " " + rep.ErrorCode + ":\"" + rep.ErrorMessage + "\" sent to " + IP.IntToStr(addr));
			/* Send packet */
			Send(sender, remotePort, rep);
		}
		return true;
	}
}
