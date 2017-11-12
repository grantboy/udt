/**
 * 
 */
package judp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

import udt.UDTServerSocket;
import udt.UDTSocket;

/**
 * @author jinyu
 * ??????????
 * ?????
 */
public class JudpServer {
    private UDTServerSocket server=null;
    //private final SynchronousQueue<judpSocket> sessionHandoff=new SynchronousQueue<judpSocket>();
    private boolean isStart=true;
    private boolean isSucess=true;
    private boolean isRWMaster=true;//?????????
    private boolean islagerRead=false;
    private static final Logger logger=Logger.getLogger(JudpServer.class.getName());

    /**
     * ???????
     */
    public void close()
    {
    	isStart=false;
    	server.getEndpoint().stop();
    }

    /**
     *
     * @param port ???
     */
    public JudpServer(int port)
    {

    	try {
    		server=new UDTServerSocket(port);
    	} catch (SocketException e) {
    		logger.log(Level.WARNING, "??????"+e.getMessage());
    		isSucess=false;
    	} catch (UnknownHostException e) {
    		isSucess=false;
    		e.printStackTrace();
    	}
    }

    /**
     *
     * @param localIP ????IP
     * @param port  ???
     */
    public JudpServer(String localIP, int port)
    {
    	try {
    		InetAddress  addr=	InetAddress.getByName(localIP);
    		server=new UDTServerSocket(addr,port);
    	} catch (SocketException e) {
    		logger.log(Level.WARNING, "??????"+e.getMessage());
    		isSucess=false;
    	} catch (UnknownHostException e) {
    		isSucess=false;
    		e.printStackTrace();
    	}
    }

    /**
     * ????????
     */
    public boolean start() {
      if(!isStart||!isSucess) {
    	  logger.log(Level.WARNING, "????????????????????????");
    	  return false;
      }
    	Thread serverThread=new Thread(new Runnable() {

    		@Override
    		public void run() {
    			while(isStart) {
    				try {
    					UDTSocket csocket=	server.accept();
    					try {
    						csocket.getInputStream().setLargeRead(islagerRead);
    						csocket.getInputStream().resetBufMaster(isRWMaster);
    					} catch (IOException e) {
    						e.printStackTrace();
    					}

    					SocketControls.getInstance().addSocket(csocket);
    				} catch (InterruptedException e) {
    					e.printStackTrace();
    				}
    			}
    		}

    	});
    	serverThread.setDaemon(true);
    	serverThread.setName("judpServer_"+System.currentTimeMillis());
    	serverThread.start();
    	return true;
    }
    /**
     * ???????????????д?????
     * ?????д?????????????????????????????
     * ??????????????ж??????????????????????????
     * ???????????????????
     * @param isRead
     */
    public void  setBufferRW(boolean isRead) {
    	this.isRWMaster=isRead;

    }

    /**
     * ???????????
     * ??? false
     * @param islarge
     */
    public void setLargeRead(boolean islarge)
    {
    	this.islagerRead=islarge;
    }
    /**
     * ?????????socket
     */
    public judpSocket accept() {
      UDTSocket socket=SocketControls.getInstance().getSocket();
      if(socket==null)
      {
    	  socket=SocketControls.getInstance().getSocket();
      }
      judpSocket jsocket=new judpSocket(socket);
      judpSocketManager.getInstance(socket.getEndpoint()).addSocket(jsocket);
      return jsocket;

    }
}
