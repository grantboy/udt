/**
 * 
 */
package Test;

import java.util.concurrent.TimeUnit;

import judp.judpServer;
import judp.judpSocket;

/**
 * @author jinyu
 *
 */
public class TestServer {
	public static void main(String[] args) {
	  //192.168.30.128
		judpServer  server=new judpServer("192.168.1.10",1421);
		server.start();
		while(true)
		{
			final judpSocket socket=server.accept();
			Thread rec=new Thread(new Runnable() {
				@Override
				public void run() {
					try {
				 		byte[] data=new byte[1024];
				 		int r=0;
				 		while(r!=-1) {
				   			r=socket.readData(data);
				    		if(r==0) {
				    	
				    		try {
								TimeUnit.MILLISECONDS.sleep(100);
								continue;
							} catch (InterruptedException e) {
								e.printStackTrace();
				    		}
				    	}
				    	if(r==-1)
				    	{
				    		System.out.println("???-1???");
				    		break;
				    	}
				    	byte[]tmp=new byte[r];
				   		System.arraycopy(data, 0, tmp, 0, r);
				   		System.out.println(new String(tmp));
				 
				 	}
				  //socket.close();
				}catch(Exception ex) {
						ex.printStackTrace();
					}
				}
			}
			);

			rec.setDaemon(true);
			rec.setName(String.valueOf(socket.getID()));
			rec.start();
		}
		

	}
}
