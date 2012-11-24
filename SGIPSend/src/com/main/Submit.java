package com.main;

import static com.main.BusinessInfo.byteOrder;

import java.nio.ByteBuffer;



public class Submit {
	
	public String UserNumber;        //21B ���ոö���Ϣ���ֻ��ţ����ֶ��ظ�UserCountָ���Ĵ������ֻ�����ǰ��"86"�����־��  ��  ��  ��		
	public String ServiceType;       //10B ҵ����룬��SP����
	public byte FeeType;             //1B �Ʒ����� 1���,2,�����Ʒ�,3���¼Ʒ�
	public String FeeValue;          //6B ȡֵ��Χ0-99999����������Ϣ���շ�ֵ����λΪ�֣���SP������ڰ������շѵ��û�����ֵΪ����ѵ�ֵ
	public byte MorelatetoMTFlag;    //1B ����MT��Ϣ��ԭ��0-MO�㲥����ĵ�һ��MT��Ϣ��1-MO�㲥����ķǵ�һ��MT��Ϣ��2-��MO�㲥�����MT��Ϣ��3-ϵͳ���������MT��Ϣ��
	public String MessageContent;    //Message Length  ����Ϣ������
	public String Reserve;           //8B ��������չ��

	public int MessageLength;        //4B Integer ����Ϣ�ĳ���
	public String ChargeNumber;      //21B ���Ѻ��룬�ֻ�����ǰ��"86"�����־�����ҽ���Ⱥ���Ҷ��û��շ�ʱΪ�գ����Ϊ�գ����������Ϣ�����ķ�����UserNumber������û�֧�������Ϊȫ���ַ���"000000000000000000000"����ʾ��������Ϣ�����ķ�����SP֧����
	
	public String SPNumber="";          //21B SP�Ľ������==========
	public byte UserCount=1;           //1B ���ն���Ϣ���ֻ�������ȡֵ��Χ1��100
	public String CorpId="";            //5B ��ҵ���룬ȡֵ��Χ0-99999===========
	public String GivenValue="0";        //6B ȡֵ��Χ0-99999�������û��Ļ��ѣ���λΪ�֣���SP���壬��ָ��SP���û����͹��ʱ�����ͻ���
	public byte AgentFlag=1;           //1B ���շѱ�־��0��Ӧ�գ�1��ʵ��
	public byte Priority=0;            //1B ���ȼ�0-9�ӵ͵��ߣ�Ĭ��Ϊ0
	public String ExpireTime="";        //16B ����Ϣ��������ֹʱ�䣬���Ϊ�գ���ʾʹ�ö���Ϣ���ĵ�ȱʡֵ��ʱ������Ϊ16���ַ�����ʽΪ"yymmddhhmmsstnnp" ������"tnnp"ȡ�̶�ֵ"032+"����Ĭ��ϵͳΪ����ʱ��
	public String ScheduleTime="";      //16B ����Ϣ��ʱ���͵�ʱ�䣬���Ϊ�գ���ʾ���̷��͸ö���Ϣ��ʱ������Ϊ16���ַ�����ʽΪ"yymmddhhmmsstnnp" ������"tnnp"ȡ�̶�ֵ"032+"����Ĭ��ϵͳΪ����ʱ��
	public byte ReportFlag=1;          //1B ״̬������0-������Ϣֻ��������ʱҪ����״̬����1-������Ϣ��������Ƿ�ɹ���Ҫ����״̬����2-������Ϣ����Ҫ����״̬����3-������Ϣ��Я�����¼Ʒ���Ϣ�����·����û���Ҫ����״̬��������-����ȱʡ����Ϊ0
	public byte TP_pid=0;              //1B GSMЭ�����͡���ϸ������ο�GSM03.40�е�9.2.3.9
	public byte TP_udhi=0;             //1B GSMЭ�����͡���ϸ������ο�GSM03.40�е�9.2.3.23,��ʹ��1λ���Ҷ���
	public byte MessageCoding=15;       //1B ����Ϣ�ı����ʽ��0����ASCII�ַ���3��д������4�������Ʊ���8��UCS2����15: GBK���������μ�GSM3.38��4�ڣ�SMS Data Coding Scheme
	public byte MessageType=0;         //1B ��Ϣ���ͣ�0-����Ϣ��Ϣ����������

	public String idnum;
	
	public static final int STRUCT_SIZE=144;//��������Ϣ���ݵĳ���
	
	public Head head = null;
	
	public Submit(){
		Head head1=new Head();
		head1.cmdId=SGIPCmd.SGIP_SUBMIT;
		this.head=head1;
	}
	
	private ByteBuffer getBuffer(){
		//this.MessageLength=this.STRUCT_SIZE+MessageContent.getBytes().length+20;
		this.ChargeNumber=this.UserNumber;
		this.MessageLength=MessageContent.getBytes().length;
		ByteBuffer buffer = ByteBuffer.allocate(this.STRUCT_SIZE+MessageContent.getBytes().length);
		buffer.order(byteOrder);
		buffer.put(SPNumber.getBytes());
		for(int i=0;i<21-SPNumber.getBytes().length;i++){
			buffer.put((byte)0);
		}
		buffer.put(ChargeNumber.getBytes());
		for(int i=0;i<21-ChargeNumber.getBytes().length;i++){
			buffer.put((byte)0);
		}
		buffer.put(UserCount);
		buffer.put(UserNumber.getBytes());
		for(int i=0;i<21-UserNumber.getBytes().length;i++){
			buffer.put((byte)0);
		}
		buffer.put(CorpId.getBytes());
		for(int i=0;i<5-CorpId.getBytes().length;i++){
			buffer.put((byte)0);
		}
		buffer.put(ServiceType.getBytes());
		for(int i=0;i<10-ServiceType.getBytes().length;i++){
			buffer.put((byte)0);
		}
		buffer.put(FeeType);
		buffer.put(FeeValue.getBytes());
		for(int i=0;i<6-FeeValue.getBytes().length;i++){
			buffer.put((byte)0);
		}
		buffer.put(GivenValue.getBytes());
		for(int i=0;i<6-GivenValue.getBytes().length;i++){
			buffer.put((byte)0);
		}
		buffer.put(AgentFlag);
		buffer.put(MorelatetoMTFlag);
		buffer.put(Priority);
		buffer.put(ExpireTime.getBytes());
		for(int i=0;i<16-ExpireTime.getBytes().length;i++){
			buffer.put((byte)0);
		}
		buffer.put(ScheduleTime.getBytes());
		for(int i=0;i<16-ScheduleTime.getBytes().length;i++){
			buffer.put((byte)0);
		}
		buffer.put(ReportFlag);
		buffer.put(TP_pid);
		buffer.put(TP_udhi);
		buffer.put(MessageCoding);
		buffer.put(MessageType);
		buffer.putInt(MessageLength);
		try {
			buffer.put(MessageContent.getBytes("GB2312"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		buffer.put(Reserve.getBytes());
		for(int i=0;i<8-Reserve.getBytes().length;i++){
			buffer.put((byte)0);
		}
		buffer.flip();
		
		return buffer;
		
		
	}
	public ByteBuffer getAllSubmitBuffer() {
		this.head.msgLen=this.STRUCT_SIZE+MessageContent.getBytes().length+this.head.STRUCT_SIZE;
		System.out.println("����Ϣ�����ǣ�"+this.head.msgLen+"::����Ϣ���ݳ���:"+MessageContent.getBytes().length);
		System.out.println("��Ϣ�ǣ�"+this.MessageContent);
		ByteBuffer buffer = ByteBuffer.allocate(this.head.msgLen);
		buffer.order(byteOrder);
		buffer.put(this.head.getBuffer());
		buffer.put(this.getBuffer());
		buffer.flip();
		return buffer;
	}
}
