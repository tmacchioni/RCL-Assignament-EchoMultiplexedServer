/**
 * Tiny Echo Server
 * 
 * Msg max 512 bytes
 * 
 * BUG: deadlock if a message contains only a newline
 * 
 * @author Tommaso Macchioni
 *
 */

public class MainClass {
	
	private static final int DEFAULT_PORT = 11000;

	public static void main(String[] args) throws InterruptedException{

		int port;
		
		try {
			port = Integer.parseInt(args[0]);
		} catch(RuntimeException ex) {
			port = DEFAULT_PORT;
		}
		

		Thread serverThread = new Thread(new Server(port));
		Thread clientThread = new Thread(new Client(port));
		
		serverThread.start();
		clientThread.start();
		
		try {
			clientThread.join();
			serverThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		return;
		
		
	}
}
