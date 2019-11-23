import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

public class Server implements Runnable{
	
	private int port;

	public Server(int port) {
		this.port = port;
	}

	@Override
	public void run() {

		ServerSocketChannel server = null;

		try {
			server = ServerSocketChannel.open();
			server.socket().bind(new InetSocketAddress(port));
			
			//server NON BLOCCANTE
			server.configureBlocking(false);

			Selector selector = Selector.open();
			server.register(selector, SelectionKey.OP_ACCEPT); 


			while(true) {
				selector.select();

				Set<SelectionKey> readKeys = selector.selectedKeys();

				Iterator<SelectionKey> iterator = readKeys.iterator();

				while(iterator.hasNext()) {

					SelectionKey key = iterator.next();
					iterator.remove();

					try {

						if(key.isAcceptable()) { //E' stata ricevuta una richiesta di connessione
							ServerSocketChannel myServer = (ServerSocketChannel) key.channel();
							SocketChannel client = myServer.accept();
							System.out.println("Accepted connection from " + client);
							
							client.configureBlocking(false); //si configura la SocketChannel del client NON bloccante 
							
							client.register(selector, SelectionKey.OP_READ);  //si aggiunge la flag di lettura nel ready set del client
							
						} else if(key.isReadable()){//Il client è leggibile
							SocketChannel client = (SocketChannel) key.channel();
							ByteBuffer input = ByteBuffer.allocate(531);

							client.read(input); //leggiamo il messaggio
							input.put(" (echoed by server)".getBytes()); //aggiungamo un'ulteriore parte
							
							input.flip();
							key.attach(input); //alleghiamo il messaggio nella sua key
							  
							key.interestOps(SelectionKey.OP_WRITE); //aggiunta della flag di scrittura nel ready set del client
							
						} else if(key.isWritable()) {//Il client è scrivibile
							SocketChannel client = (SocketChannel) key.channel();
							ByteBuffer output = (ByteBuffer) key.attachment(); //si estrae l'oggetto dall'allegato della chiave

							if(output != null) { 
								while(output.hasRemaining()) {
									client.write(output);
								}
							}
							key.interestOps(SelectionKey.OP_READ); 
						}

					}catch(IOException ex) {

						key.cancel();

						try { key.channel().close(); } 
						catch (IOException e) { e.printStackTrace(); }
					}
				}
			}
		} catch(IOException ex) {
			ex.printStackTrace();
			try { server.close(); } catch (IOException e) {	e.printStackTrace();}
		}
	}

}
