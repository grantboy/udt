/**    
 * �ļ�����TestSendFile.java    
 *    
 * �汾��Ϣ��    
 * ���ڣ�2017��8��27��    
 * Copyright ���� Corporation 2017     
 * ��Ȩ����    
 *    
 */
package Test;

import java.io.IOException;

import judp.judpSendFile;

/**    
 *     
 * ��Ŀ���ƣ�judp    
 * �����ƣ�TestSendFile    
 * ��������    
 * �����ˣ�jinyu    
 * ����ʱ�䣺2017��8��27�� ����6:32:25    
 * �޸��ˣ�jinyu    
 * �޸�ʱ�䣺2017��8��27�� ����6:32:25    
 * �޸ı�ע��    
 * @version     
 *     
 */
public class TestSendFile {

    /**    
        
     * TODO(����������������������� �C ��ѡ)     
       
     * @param   name    
        
     * @return  
       
     *  
    
       
    */
    public static void main(String[] args) {
        judpSendFile jsend=new judpSendFile("192.168.1.10",1421);
        jsend.startSend();
        
        try {
            System.in.read();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

}
