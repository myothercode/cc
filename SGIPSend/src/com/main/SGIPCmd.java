package com.main;

public class SGIPCmd {
	public static final int SGIP_BIND         =0x00000001;   //SP�ø���Ϣ��SMG�����������ӵ�����
	public static final int SGIP_BIND_RESP    =0x80000001;   //SP��Bind�����Ӧ��
	public static final int SGIP_UNBIND       =0x00000002;   //SP�ø���Ϣ��SMG֪ͨ��Ҫ�Ͽ����е�����
    public static final int SGIP_UNBIND_RESP  =0x80000002;   //SP��Unbind�����Ӧ��
	public static final int SGIP_SUBMIT       =0x00000003;   //SP�ø���Ϣ��SMG�����Ͷ���Ϣ
	public static final int SGIP_SUBMIT_RESP  =0x80000003;   //SMG��Submit�����Ӧ��
	public static final int SGIP_DELIVER      =0x00000004;   //SMG����һ������Ϣ��SP
	public static final int SGIP_DELIVER_RESP =0x80000004;   //SP�ø���Ϣ�Դ�SMG���յ���Deliver��������Ӧ��
	public static final int SGIP_REPORT       =0x00000005;   //SMG�ø�����֪ͨSPһ��Submit���������͵�MT��ǰ���
	public static final int SGIP_REPORT_RESP  =0x80000005;   //SP�Դ�SMG���յ���Report��������Ӧ��
	  

}
