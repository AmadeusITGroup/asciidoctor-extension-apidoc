package com.amadeus.asciidoc.apidoc;

import java.util.ArrayList;
import java.util.List;

import org.asciidoctor.log.LogHandler;
import org.asciidoctor.log.LogRecord;

class StubLogHandler implements LogHandler {

  List<LogRecord> logs = new ArrayList<>();

  @Override
  public void log(LogRecord logRecord) {
    logs.add(logRecord);
  }

}
