package com.main;
import java.nio.ByteBuffer;


public class Resp  extends SGIPAbstractStruct{
	public byte Result;   //1B 0：接收成功;其它: 错误码
	public String Reserve="";//8B 保留，扩展用
	
	public static final int STRUCT_SIZE=9;
	
	public Resp() {
		Head head = new Head(0);
		head.Msglen=Head.STRUCT_SIZE+this.STRUCT_SIZE;
		this.setHead(head);
	}
	
	public Resp(int Seq_no){
		Head head = new Head(Seq_no);		
		head.Msglen=Head.STRUCT_SIZE+this.STRUCT_SIZE;
		this.setHead(head);
	}
	
	public Resp(int CrtTime,int Seq_no){
		Head head = new Head(CrtTime,Seq_no);		
		head.Msglen=Head.STRUCT_SIZE+this.STRUCT_SIZE;
		this.setHead(head);
	}
	
	@Override
	public ByteBuffer getBuffer() {
		ByteBuffer buffer = ByteBuffer.allocate(STRUCT_SIZE);
		buffer.order(byteOrder);
		buffer.put(Result);
		//buffer.put(Reserve.getBytes());		
		for (int i = Reserve.getBytes().length; i < 8; i++) {
			buffer.put((byte)0);
		}			
		buffer.flip();
		return buffer;
	}

	@Override
	public void setBuffer(ByteBuffer buffer) throws Exception {
		buffer.order(byteOrder);
		Result = buffer.get();

		
	}
  }

