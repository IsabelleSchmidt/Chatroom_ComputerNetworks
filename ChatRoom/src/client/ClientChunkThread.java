package client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;
import message.Chunk;
import message.Status;
import server.EndpointInfo;

/**
 * Chunk thread send all chunks of one message to other client.
 *
 */
public class ClientChunkThread extends Thread {
	
	DatagramSocket udpSocket;
	EndpointInfo recipientInfo;
	Chunk[] chunks;
	Status[] acks;
	Timer timeoutTimer;
	
	InetAddress ipAddress;
	int serverPort = 8888;
	
	private final int timeOut = 5000; // time of timeout in milliseconds
	
	public ClientChunkThread(DatagramSocket udpSocket, EndpointInfo recipientInfo, Chunk[] chunks) {
		this.udpSocket = udpSocket;
		this.recipientInfo = recipientInfo;
		this.chunks = chunks;
		this.acks = new Status[chunks.length];
		this.timeoutTimer = new Timer(true);
		
		try {
			this.ipAddress = InetAddress.getByName("localhost");
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		for (int i = 0; i < chunks.length; i++) {
			sendMessage(chunks[i]);
			setPacketStatus(i, Status.SENT);
			
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		//Es wird geprueft, ob alle ACKs vom Server angekommen sind. Wenn ja, wird Thread beendet
		new Thread() {
			boolean check = true;
			
			@Override
			public void run() {
				
				while (check) {
					if (transferCompleted()) {
						check = false;
						this.interrupt();
						break;
					}
					
					// Wenn es noch Pakete gibt, die nicht bestaetigt wurden, warte 0.4 Sek und versuche wieder.
					try {
						Thread.sleep(400);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			
		}.start();
	}
	
	public void setPacketStatus(int nr, Status status) {
		acks[nr] = status;
	}
		
	public void sendMessage(Chunk chunk) {
		DatagramPacket packet = new DatagramPacket(
									chunk.toString().getBytes(), chunk.toString().length(), 
									ipAddress, recipientInfo.getPort());
		try {
			udpSocket.send(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		timeoutTimer.schedule(new PacketTimeout(chunk), timeOut); // Starte Timer beim Senden
	}
	
	public boolean transferCompleted() {
		boolean acked = true;
		
		for (int i = 0; i < acks.length; i++) {
			acked = acked & (acks[i] == Status.ACKED);
		}
			
		return acked;
	}
	
	/**
	 * Timer wird fuer ein bestimmtes Chunk gestartet. 
	 * Wenn Zeit abgelaufen ist und die Bestaetigung vom Server nicht erhalten wurde, sendet der Client den Chunk erneut.
	 * 
	 *
	 */
	private class PacketTimeout extends TimerTask {
        private Chunk chunk;

        public PacketTimeout(Chunk chunk) {
            this.chunk = chunk;
        }

        public void run() {
            try {
                if (!(acks[chunk.getChunkNr()] == Status.ACKED)) {
                    sendMessage(chunk);
                    System.out.println("CLIENT: Resend " + chunk.toString());
                    setPacketStatus(chunk.getChunkNr(), Status.RESENT);
                }
            } catch (Exception e) {
            	
            }
        }
    }
	
}
