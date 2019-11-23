import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class Client implements Runnable{

	private int port;

	public Client(int port) {
		this.port = port;
	}

	@Override
	public void run() {

		try {
			//Il client è bloccante
			SocketChannel client = SocketChannel.open();
			client.connect(new InetSocketAddress("127.0.0.1", port));
			

			ByteBuffer outputBuffer = null; 
			ByteBuffer inputBuffer = null;

			//Stream per la lettura da stdin
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

			while(true) {
				
				outputBuffer =  ByteBuffer.allocate(512); //utilizzato per scrivere nel SocketChannel
				inputBuffer = ByteBuffer.allocate(531); //utilizzato per leggere dal SocketChannel
				
				String msg = reader.readLine(); //lettura da stdin

				try {
					outputBuffer.put(msg.getBytes());
				} catch (BufferOverflowException e) {
					System.out.println("String too long, retry.");
					continue;
				}
				outputBuffer.flip();
				System.out.println("Sent: " + new String(outputBuffer.array()));
				client.write(outputBuffer); //la stringa dell'utente viene inserita nel canale
				
				client.read(inputBuffer); //si attente che venga ricevuta la stringa dal server
				System.out.println("Received: " + new String(inputBuffer.array()));
			}

		} catch(IOException ex) {
			ex.printStackTrace();
			return;
		}



	}

}
