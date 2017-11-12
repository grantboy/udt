/**    
 * ???????judpSendFile.java    
 *    
 * ?汾?????    
 * ?????2017??8??27??    
 * Copyright ???? Corporation 2017     
 * ???????    
 *    
 */
package judp;


import java.io.File;
import java.io.FileInputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.text.NumberFormat;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import udt.UDTReceiver;


/**    
 *     
 * ????????judp    
 * ???????judpSendFile    
 * ????????    ???????
 * ???????jinyu    
 * ???????2017??8??27?? ????4:30:42    
 * ??????jinyu    
 * ??????2017??8??27?? ????4:30:42    
 * ???????    
 * @version     
 *     
 */
public class judpSendFile {
    private final int serverPort;
    private final String host;
    //TODO configure pool size
   // private final ExecutorService threadPool=Executors.newFixedThreadPool(3);
    private final ExecutorService threadPool=Executors.newCachedThreadPool();
    public judpSendFile(int serverPort){
        this.serverPort=serverPort;
        this.host=null;

    }
    public judpSendFile(String localIP,int serverPort){
        this.serverPort=serverPort;
        this.host=localIP;

    }
    
    
  public void startSend() {
      threadPool.execute(new Runnable() {
          @Override
          public void run(){
              try{
                  UDTReceiver.connectionExpiryDisabled=true;
                  JudpServer server=null;
                  if(host==null) {
                      server=new JudpServer(serverPort);
                  } else {
                      server=new JudpServer(host,serverPort);
                  }

                  server.start();
                  while(true){
                      judpSocket socket=server.accept();
                      Thread.sleep(1000);
                      threadPool.execute(new RequestRunner(socket));
                  }
              }catch(Exception ex){
                throw new RuntimeException(ex);
              }
          }
      });
  }


  public static void usage(){
      System.out.println("Usage: java -cp ... udt.util.SendFile <server_port> " +   "[--verbose] [--localPort=<port>] [--localIP=<ip>]");
  }

  public static class RequestRunner implements Runnable{

        private final static Logger logger=Logger.getLogger(RequestRunner.class.getName());

        private final judpSocket socket;

        private final NumberFormat format=NumberFormat.getNumberInstance();

        private final boolean memMapped;

        private boolean verbose;

        public RequestRunner(judpSocket socket){
            this.socket=socket;
            format.setMaximumFractionDigits(3);
            memMapped=false;//true;
        }

        @Override
        public void run(){
            try{
                logger.info("Handling request from "+socket.getDestination());

               // UDTInputStream in=socket.getInputStream();
              //  UDTOutputStream out=socket.getOutputStream();
                byte[]readBuf=new byte[32768];
                ByteBuffer bb=ByteBuffer.wrap(readBuf);

                //read file name info
                int r=0;
                 while(true)
                    {
                      r=socket.readData(readBuf);
                       if(r==0)
                         Thread.sleep(100);
                       else
                       {
                           break;
                       }
                    }
                    if(r==-1)
                    {
                       socket.close();
                        return;
                    }
                //how many bytes to read for the file name
                byte[]len=new byte[4];
                bb.get(len);
                if(verbose){
                    StringBuilder sb = new StringBuilder();
                    for(int i=0;i<len.length;i++){
                        sb.append(Integer.toString(len[i]));
                        sb.append(" ");
                    }
                    System.out.println("[SendFile] name length data: "+sb.toString());
                }
                long length=ApplicationCode.decode(len, 0);
                if(verbose)System.out.println("[SendFile] name length: "+length);
                byte[]fileName=new byte[(int)length];
                bb.get(fileName);

                File file=new File(new String(fileName));
                System.out.println("[SendFile] File requested: '"+file.getPath()+"'");

                Thread.currentThread().setName("sendFile_"+file.getName());
                FileInputStream fis=null;
                try{
                    long size=file.length();
                    System.out.println("[SendFile] File size: "+size);
                    //send size info
                    socket.sendData(ApplicationCode.encode64(size));

                    long start=System.currentTimeMillis();
//                    //and send the file
                    if(memMapped){
                        copyFile(file,socket);
                    }else{
                        ApplicationCode.CopySocketFile(file, socket,socket.getDataStreamLen());
                    }
                    System.out.println("[SendFile] Finished sending data.");
                    long end=System.currentTimeMillis();
                    //System.out.println(socket.getSession().getStatistics().toString());
                    double rate=1000.0*size/1024/1024/(end-start);
                    System.out.println("[SendFile] Rate: "+format.format(rate)+" MBytes/sec. "+format.format(8*rate)+" MBit/sec.");
//                    if(Boolean.getBoolean("udt.sender.storeStatistics")){
//                        socket.getSession().getStatistics().writeParameterHistory(new File("udtstats-"+System.currentTimeMillis()+".csv"));
//                    }
                }finally{
                    socket.close();
                    if(fis!=null)fis.close();
                }
                logger.info("Finished request from "+socket.getDestination());
            }catch(Exception ex){
                ex.printStackTrace();
                throw new RuntimeException(ex);
            }
        }
    }


    @SuppressWarnings("resource")
    private static void copyFile(File file, judpSocket os)throws Exception{
        FileChannel c=new RandomAccessFile(file,"r").getChannel();
        MappedByteBuffer b=c.map(MapMode.READ_ONLY, 0, file.length());
        b.load();
        byte[]buf=new byte[1024*1024];
        int len=0;
        byte[] data=null;
        while(true){
            len=Math.min(buf.length, b.remaining());
            b.get(buf, 0, len);
            if(buf.length!=len)
            {
              data=new byte[len];
              System.arraycopy(data, 0, data, 0, len);
            }
            else
            {
                data=buf;
            }
            os.sendData(data);
            if(b.remaining()==0)break;
        }
       
    }   
}
