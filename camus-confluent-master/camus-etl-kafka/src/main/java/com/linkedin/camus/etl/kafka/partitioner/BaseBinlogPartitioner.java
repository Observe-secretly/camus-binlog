package com.linkedin.camus.etl.kafka.partitioner;

import java.util.Locale;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.JobContext;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;

import com.linkedin.camus.etl.IEtlKey;
import com.linkedin.camus.etl.Partitioner;
import com.linkedin.camus.etl.kafka.common.DateUtils;

public class BaseBinlogPartitioner extends Partitioner {

    public static final String DEFAULT_TIME_ZONE      = "America/Los_Angeles";

    /** Size of a partition in milliseconds. */
    private long               outfilePartitionMillis = 0;
    private DateTimeFormatter  outputDirFormatter;

    /**
     * Initialize the partitioner. This method must be invoked once, and before any any other method.
     * 
     * @param outfilePartitionMillis duration of a partition, e.g. {@code 3,600,000} for hour partitions
     * @param destSubTopicPathFormat format of output sub-dir to be created under topic directory, typically something
     * like {@code "'hourly'/YYYY/MM/dd/HH"}. For formatting rules see {@link org.joda.time.format.DateTimeFormat}.
     * @param locale locale to use for formatting of path
     * @param outputTimeZone time zone to use for date calculations
     */
    protected void init(long outfilePartitionMillis, String destSubTopicPathFormat, Locale locale,
                        DateTimeZone outputTimeZone) {
        this.outfilePartitionMillis = outfilePartitionMillis;
        this.outputDirFormatter = DateUtils.getDateTimeFormatter(destSubTopicPathFormat,
                                                                 outputTimeZone).withLocale(locale);
    }

    private Text database = null;
    private Text table    = null;

    @Override
    public String encodePartition(JobContext context, IEtlKey key) {
        database = (Text) key.getPartitionMap().get(new Text("database"));
        table = (Text) key.getPartitionMap().get(new Text("table"));

        return String.valueOf(DateUtils.getPartition(outfilePartitionMillis, key.getTime(),
                                                     outputDirFormatter.getZone()))
               + "." + database.toString() + "." + table.toString();

    }

    @Override
    public String generatePartitionedPath(JobContext context, String topic, String encodedPartition) {
        int index = encodedPartition.indexOf(".");

        DateTime bucket = new DateTime(Long.parseLong(encodedPartition.substring(0, index)));
        return topic + "/" + bucket.toString(outputDirFormatter) + "/" + encodedPartition.substring(index + 1);
    }

    @Override
    public String generateFileName(JobContext context, String topic, String brokerId, int partitionId, int count,
                                   long offset, String encodedPartition) {
        return topic + "." + brokerId + "." + partitionId + "." + count + "." + offset + "." + encodedPartition;
    }

    @Override
    public String getWorkingFileName(JobContext context, String topic, String brokerId, int partitionId,
                                     String encodedPartition) {
        return "data." + topic.replace('.', '_') + "." + brokerId + "." + partitionId + "." + encodedPartition;
    }

}
