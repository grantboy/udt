/**    
 * �ļ�����TestRecFile.java    
 *    
 * �汾��Ϣ��    
 * ���ڣ�2017��8��27��    
 * Copyright ���� Corporation 2017     
 * ��Ȩ����    
 *    
 */
package Test;

import java.io.IOException;

import judp.JudpRecviceFile;

/**    
 *     
 * ��Ŀ���ƣ�judp    
 * �����ƣ�TestRecFile    
 * ��������    
 * �����ˣ�jinyu    
 * ����ʱ�䣺2017��8��27�� ����6:32:42    
 * �޸��ˣ�jinyu    
 * �޸�ʱ�䣺2017��8��27�� ����6:32:42    
 * �޸ı�ע��    
 * @version     
 *     
 */
public class TestRecFile {
    
    public static void main(String[] args) {
        JudpRecviceFile rec=new JudpRecviceFile("192.168.1.10", 1422, "E:\\Study\\java\\filesudt\\send\\12.rmvb", "E:\\Study\\java\\filesudt\\rec\\1.rmvb");
        rec.start();
        try {
            System.in.read();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}
