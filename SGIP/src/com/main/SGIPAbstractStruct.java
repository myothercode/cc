package com.main;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;



public abstract class SGIPAbstractStruct {
    public static final int    STRUCT_MAX_SIZE = 1024 * 4; // 4K
	public Head               head            = null;
	public static final String charset         = "GB2312";
	public final ByteOrder byteOrder =ByteOrder.BIG_ENDIAN;

	public final ByteBuffer getAllBuffer() throws Exception {
	  ByteBuffer buffer = ByteBuffer.allocate(this.getHead().getBuffer().capacity()
	        + this.getBuffer().capacity());
	  buffer.order(byteOrder);
	  buffer.put(this.getHead().getBuffer());
	  buffer.put(this.getBuffer());
	  buffer.flip();
	  
	  return buffer;
	}

	public final void setAllBuffer(ByteBuffer buffer) throws Exception {
	  buffer.order(byteOrder);
	  this.setHead(new Head());
	  this.getHead().setBuffer(buffer);
	  this.setBuffer(buffer);
	}

	public final void setHead(Head head) {
	  this.head = head;
	}

	public final Head getHead() {
	  return head;
	}

	public abstract ByteBuffer getBuffer()throws Exception;
	public abstract void setBuffer(ByteBuffer buffer) throws Exception;
  }

