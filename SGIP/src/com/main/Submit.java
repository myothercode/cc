package com.main;
import java.nio.ByteBuffer;



  public class Submit  extends SGIPAbstractStruct{
	
	public String SPNumber;          //21B SP�Ľ������
	public String ChargeNumber;      //21B ���Ѻ��룬�ֻ�����ǰ��"86"�����־�����ҽ���Ⱥ���Ҷ��û��շ�ʱΪ�գ����Ϊ�գ����������Ϣ�����ķ�����UserNumber������û�֧�������Ϊȫ���ַ���"000000000000000000000"����ʾ��������Ϣ�����ķ�����SP֧����
	public byte UserCount;           //1B ���ն���Ϣ���ֻ�������ȡֵ��Χ1��100
	public String UserNumber;        //21B ���ոö���Ϣ���ֻ��ţ����ֶ��ظ�UserCountָ���Ĵ������ֻ�����ǰ��"86"�����־��  ��  ��  ��
	public String CorpId;            //5B ��ҵ���룬ȡֵ��Χ0-99999
	public String ServiceType;       //10B ҵ����룬��SP����
	public byte FeeType;             //1B �Ʒ�����
	public String FeeValue;          //6B ȡֵ��Χ0-99999����������Ϣ���շ�ֵ����λΪ�֣���SP������ڰ������շѵ��û�����ֵΪ����ѵ�ֵ
	public String GivenValue;        //6B ȡֵ��Χ0-99999�������û��Ļ��ѣ���λΪ�֣���SP���壬��ָ��SP���û����͹��ʱ�����ͻ���
	public byte AgentFlag;           //1B ���շѱ�־��0��Ӧ�գ�1��ʵ��
	public byte MorelatetoMTFlag;    //1B ����MT��Ϣ��ԭ��0-MO�㲥����ĵ�һ��MT��Ϣ��1-MO�㲥����ķǵ�һ��MT��Ϣ��2-��MO�㲥�����MT��Ϣ��3-ϵͳ���������MT��Ϣ��
	public byte Priority;            //1B ���ȼ�0-9�ӵ͵��ߣ�Ĭ��Ϊ0
	public String ExpireTime="";        //16B ����Ϣ��������ֹʱ�䣬���Ϊ�գ���ʾʹ�ö���Ϣ���ĵ�ȱʡֵ��ʱ������Ϊ16���ַ�����ʽΪ"yymmddhhmmsstnnp" ������"tnnp"ȡ�̶�ֵ"032+"����Ĭ��ϵͳΪ����ʱ��
	public String ScheduleTime="";      //16B ����Ϣ��ʱ���͵�ʱ�䣬���Ϊ�գ���ʾ���̷��͸ö���Ϣ��ʱ������Ϊ16���ַ�����ʽΪ"yymmddhhmmsstnnp" ������"tnnp"ȡ�̶�ֵ"032+"����Ĭ��ϵͳΪ����ʱ��
	public byte ReportFlag;          //1B ״̬������0-������Ϣֻ��������ʱҪ����״̬����1-������Ϣ��������Ƿ�ɹ���Ҫ����״̬����2-������Ϣ����Ҫ����״̬����3-������Ϣ��Я�����¼Ʒ���Ϣ�����·����û���Ҫ����״̬��������-����ȱʡ����Ϊ0
	public byte TP_pid;              //1B GSMЭ�����͡���ϸ������ο�GSM03.40�е�9.2.3.9
	public byte TP_udhi;             //1B GSMЭ�����͡���ϸ������ο�GSM03.40�е�9.2.3.23,��ʹ��1λ���Ҷ���
	public byte MessageCoding;       //1B ����Ϣ�ı����ʽ��0����ASCII�ַ���3��д������4�������Ʊ���8��UCS2����15: GBK���������μ�GSM3.38��4�ڣ�SMS Data Coding Scheme
	public byte MessageType;         //1B ��Ϣ���ͣ�0-����Ϣ��Ϣ����������
	public int MessageLength;        //4B Integer ����Ϣ�ĳ���
	public String MessageContent;    //Message Length  ����Ϣ������
	public String Reserve;           //8B ��������չ��

	
	public static final int STRUCT_SIZE=144;//��������Ϣ���ݵĳ���
	
	public Submit() {
		Head head = new Head();
		head.Msglen=Head.STRUCT_SIZE+this.STRUCT_SIZE;
		head.Cmd_id=SGIP.SGIP_SUBMIT;
		this.setHead(head);
	}
	
	@Override
	public ByteBuffer getBuffer() {
		ByteBuffer buffer = ByteBuffer.allocate(STRUCT_SIZE+MessageLength);
		buffer.order(byteOrder);
		
		buffer.put(SPNumber.getBytes());
		for (int i = SPNumber.getBytes().length; i < 21; i++) {
			buffer.put((byte)0);
		}
		buffer.put(ChargeNumber.getBytes());
		for (int i = ChargeNumber.getBytes().length; i < 21; i++) {
			buffer.put((byte)0);
		}
		
		buffer.put(UserCount);
		
		buffer.put(UserNumber.getBytes());
		for (int i = UserNumber.getBytes().length; i < 21; i++) {
			buffer.put((byte)0);
		}
		buffer.put(CorpId.getBytes());
		for (int i = CorpId.getBytes().length; i < 5; i++) {
			buffer.put((byte)0);
		}
		buffer.put(ServiceType.getBytes());
		for (int i = ServiceType.getBytes().length; i < 10; i++) {
			buffer.put((byte)0);
		}
		
		buffer.put(FeeType);
		
		buffer.put(FeeValue.getBytes());
		for (int i = FeeValue.getBytes().length; i < 6; i++) {
			buffer.put((byte)0);
		}
		buffer.put(GivenValue.getBytes());
		for (int i = GivenValue.getBytes().length; i < 6; i++) {
			buffer.put((byte)0);
		}
		
		buffer.put(AgentFlag);
		buffer.put(MorelatetoMTFlag);
		buffer.put(Priority);
		
		buffer.put(ExpireTime.getBytes());
		for (int i = ExpireTime.getBytes().length; i < 16; i++) {
			buffer.put((byte)0);
		}
		buffer.put(ScheduleTime.getBytes());
		for (int i = ScheduleTime.getBytes().length; i < 16; i++) {
			buffer.put((byte)0);
		}
		
		buffer.put(ReportFlag);
		buffer.put(TP_pid);
		buffer.put(TP_udhi);
		buffer.put(MessageCoding);
		buffer.put(MessageType);
		buffer.putInt(MessageLength);
		
		try{
			buffer.put(MessageContent.getBytes("GB2312"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		for (int i = 0; i < 8; i++) {
			buffer.put((byte)0);
		}

		buffer.flip();
		return buffer;
	}
	
	@Override
	public void setBuffer(ByteBuffer buffer) throws Exception {

	}


  }

