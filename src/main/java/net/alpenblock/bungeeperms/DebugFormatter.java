package net.alpenblock.bungeeperms;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public final class DebugFormatter extends Formatter
{

    @Override
    public String format(LogRecord record)
    {
        StringBuilder builder = new StringBuilder();
        builder
                .append(new SimpleDateFormat("dd.MM.YYYY HH:mm:ss").format(new Date()))
                .append(": ")
                .append(record.getMessage())
                .append("\n");
        Throwable ex = record.getThrown();
        if (ex != null)
        {
            StringWriter writer = new StringWriter();
            ex.printStackTrace(new PrintWriter(writer));
            builder.append(writer);
        }
        return builder.toString();
    }
}
