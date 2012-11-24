package com.tdt.log4j.extend;

import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.TriggeringEventEvaluator;


public final class MockTriggeringEventEvaluator implements TriggeringEventEvaluator {
	public boolean isTriggeringEvent(LoggingEvent event) {
		return true;
	}
}