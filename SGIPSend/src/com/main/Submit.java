package com.main;

import static com.main.BusinessInfo.byteOrder;

import java.nio.ByteBuffer;



public class Submit {
	
	public String UserNumber;        //21B 接收该短消息的手机号，该字段重复UserCount指定的次数，手机号码前加"86"国别标志…  …  …  …		
	public String ServiceType;       //10B 业务代码，由SP定义
	public byte FeeType;             //1B 计费类型 1免费,2,按条计费,3包月计费
	public String FeeValue;          //6B 取值范围0-99999，该条短消息的收费值，单位为分，由SP定义对于包月制收费的用户，该值为月租费的值
	public byte MorelatetoMTFlag;    //1B 引起MT消息的原因0-MO点播引起的第一条MT消息；1-MO点播引起的非第一条MT消息；2-非MO点播引起的MT消息；3-系统反馈引起的MT消息。
	public String MessageContent;    //Message Length  短消息的内容
	public String Reserve;           //8B 保留，扩展用

	public int MessageLength;        //4B Integer 短消息的长度
	public String ChargeNumber;      //21B 付费号码，手机号码前加"86"国别标志；当且仅当群发且对用户收费时为空；如果为空，则该条短消息产生的费用由UserNumber代表的用户支付；如果为全零字符串"000000000000000000000"，表示该条短消息产生的费用由SP支付。
	
	public String SPNumber="";          //21B SP的接入号码==========
	public byte UserCount=1;           //1B 接收短消息的手机数量，取值范围1至100
	public String CorpId="";            //5B 企业代码，取值范围0-99999===========
	public String GivenValue="0";        //6B 取值范围0-99999，赠送用户的话费，单位为分，由SP定义，特指由SP向用户发送广告时的赠送话费
	public byte AgentFlag=1;           //1B 代收费标志，0：应收；1：实收
	public byte Priority=0;            //1B 优先级0-9从低到高，默认为0
	public String ExpireTime="";        //16B 短消息寿命的终止时间，如果为空，表示使用短消息中心的缺省值。时间内容为16个字符，格式为"yymmddhhmmsstnnp" ，其中"tnnp"取固定值"032+"，即默认系统为北京时间
	public String ScheduleTime="";      //16B 短消息定时发送的时间，如果为空，表示立刻发送该短消息。时间内容为16个字符，格式为"yymmddhhmmsstnnp" ，其中"tnnp"取固定值"032+"，即默认系统为北京时间
	public byte ReportFlag=1;          //1B 状态报告标记0-该条消息只有最后出错时要返回状态报告1-该条消息无论最后是否成功都要返回状态报告2-该条消息不需要返回状态报告3-该条消息仅携带包月计费信息，不下发给用户，要返回状态报告其它-保留缺省设置为0
	public byte TP_pid=0;              //1B GSM协议类型。详细解释请参考GSM03.40中的9.2.3.9
	public byte TP_udhi=0;             //1B GSM协议类型。详细解释请参考GSM03.40中的9.2.3.23,仅使用1位，右对齐
	public byte MessageCoding=15;       //1B 短消息的编码格式。0：纯ASCII字符串3：写卡操作4：二进制编码8：UCS2编码15: GBK编码其它参见GSM3.38第4节：SMS Data Coding Scheme
	public byte MessageType=0;         //1B 信息类型：0-短消息信息其它：待定

	public String idnum;
	
	public static final int STRUCT_SIZE=144;//不含短消息内容的长度
	
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
		System.out.println("总消息长度是："+this.head.msgLen+"::短消息内容长度:"+MessageContent.getBytes().length);
		System.out.println("消息是："+this.MessageContent);
		ByteBuffer buffer = ByteBuffer.allocate(this.head.msgLen);
		buffer.order(byteOrder);
		buffer.put(this.head.getBuffer());
		buffer.put(this.getBuffer());
		buffer.flip();
		return buffer;
	}
}
