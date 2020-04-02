package com.linkedin.camus.etl.kafka.partitioner;

import static com.linkedin.camus.etl.kafka.mapred.EtlMultiOutputFormat.ETL_DEFAULT_TIMEZONE;
import static com.linkedin.camus.etl.kafka.mapred.EtlMultiOutputFormat.ETL_DESTINATION_PATH_TOPIC_SUBDIRFORMAT;
import static com.linkedin.camus.etl.kafka.mapred.EtlMultiOutputFormat.ETL_DESTINATION_PATH_TOPIC_SUBDIRFORMAT_LOCALE;
import static com.linkedin.camus.etl.kafka.mapred.EtlMultiOutputFormat.ETL_OUTPUT_FILE_TIME_PARTITION_MINS;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.LocaleUtils;
import org.apache.hadoop.conf.Configuration;
import org.joda.time.DateTimeZone;

public class BinlogPartitioner extends BaseBinlogPartitioner {

    private static final String DEFAULT_TOPIC_SUB_DIR_FORMAT       = "'hourly'/YYYY/MM/dd/HH";
    private static final String DEFAULT_PARTITION_DURATION_MINUTES = "60";
    private static final String DEFAULT_LOCALE                     = Locale.US.toString();

    @Override
    public void setConf(Configuration conf) {
        if (conf != null) {
            String destPathTopicSubDirFormat = conf.get(ETL_DESTINATION_PATH_TOPIC_SUBDIRFORMAT,
                                                        DEFAULT_TOPIC_SUB_DIR_FORMAT);
            long partitionDurationMinutes = Long.parseLong(conf.get(ETL_OUTPUT_FILE_TIME_PARTITION_MINS,
                                                                    DEFAULT_PARTITION_DURATION_MINUTES));
            Locale locale = LocaleUtils.toLocale(conf.get(ETL_DESTINATION_PATH_TOPIC_SUBDIRFORMAT_LOCALE,
                                                          DEFAULT_LOCALE));
            DateTimeZone outputTimeZone = DateTimeZone.forID(conf.get(ETL_DEFAULT_TIMEZONE, DEFAULT_TIME_ZONE));
            long outfilePartitionMs = TimeUnit.MINUTES.toMillis(partitionDurationMinutes);

            init(outfilePartitionMs, destPathTopicSubDirFormat, locale, outputTimeZone);
        }

        super.setConf(conf);
    }

}
