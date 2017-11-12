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
 * ����˽��շ�װ
 * �����
 */
public class judpServer  {
    private UDTServerSocket server=null;
    //private final SynchronousQueue<judpSocket> sessionHandoff=new SynchronousQueue<judpSocket>();
    private boolean isStart=true;
    private boolean isSucess=true;
    private boolean isRWMaster=true;//��Ĭ��ֵһ��
    private boolean islagerRead=false;
    private static final Logger logger=Logger.getLogger(judpServer.class.getName());

    /**
     * �رշ����
     */
    public void close()
    {
    	isStart=false;
    	server.getEndpoint().stop();
    }

    /**
     *
     * @param port �˿�
     */
    public judpServer(int port)
    {

    	try {
    		server=new UDTServerSocket(port);
    	} catch (SocketException e) {
    		logger.log(Level.WARNING, "��ʧ�ܣ�"+e.getMessage());
    		isSucess=false;
    	} catch (UnknownHostException e) {
    		isSucess=false;
    		e.printStackTrace();
    	}
    }

    /**
     *
     * @param localIP ����IP
     * @param port  �˿�
     */
    public judpServer(String localIP,int port)
    {
    	try {
    		InetAddress  addr=	InetAddress.getByName(localIP);
    		server=new UDTServerSocket(addr,port);

    	} catch (SocketException e) {
    		logger.log(Level.WARNING, "��ʧ�ܣ�"+e.getMessage());
    		isSucess=false;
    	} catch (UnknownHostException e) {
    		isSucess=false;
    		e.printStackTrace();
    	}
    }

    /**
     * ��������
     */
    public boolean start() {
      if(!isStart||!isSucess) {
    	  logger.log(Level.WARNING, "�Ѿ��رյļ���������˿ڲ���ʹ��");
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
     * �����Ƕ�ȡΪ������д��Ϊ��
     * �����д��Ϊ��������ȡ�ٶ���ʱ�����ݸ��Ƕ�ʧ
     * Ĭ�϶�ȡΪ������û�ж�ȡ�������ǣ��������ݣ��ȴ��ظ�
     * ���ô����ݶ�ȡ��������
     * @param isRead
     */
    public void  setBufferRW(boolean isRead) {
    	this.isRWMaster=isRead;

    }

    /**
     * ���ô����ݶ�ȡ
     * Ĭ�� false
     * @param islarge
     */
    public void setLargeRead(boolean islarge)
    {
    	this.islagerRead=islarge;
    }
    /**
     * �������ӵ�socket
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
