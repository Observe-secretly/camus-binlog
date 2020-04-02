package com.linkedin.camus.etl.kafka.common;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map.Entry;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.output.FileOutputCommitter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.linkedin.camus.coders.CamusWrapper;
import com.linkedin.camus.etl.IEtlKey;
import com.linkedin.camus.etl.RecordWriterProvider;
import com.linkedin.camus.etl.kafka.mapred.EtlMultiOutputFormat;

public class CanalBinlogRecordWriterProvider implements RecordWriterProvider {

    public CanalBinlogRecordWriterProvider(TaskAttemptContext context){
        // TODO 此方法好像是获取运行时传进来的参数。但是这里暂时先不传任何参数和逻辑。这个构造函数必须要。否则报错
    }

    static class CanalBinlogRecordWriter extends RecordWriter<IEtlKey, CamusWrapper> {

        private DataOutputStream outputStream;
        private String           fieldDelimiter;
        private String           rowDelimiter;

        public CanalBinlogRecordWriter(DataOutputStream outputStream, String fieldDelimiter, String rowDelimiter){
            this.outputStream = outputStream;
            this.fieldDelimiter = fieldDelimiter;
            this.rowDelimiter = rowDelimiter;
        }

        @Override
        public void write(IEtlKey key, CamusWrapper value) throws IOException, InterruptedException {
            if (value == null) {
                return;
            }

            String recordStr = (String) value.getRecord();
            JSONObject record = JSON.parseObject(recordStr, Feature.OrderedField);

            // 判定是否是binlog格式 //TODO 此方法可能需要加强，视情况而定 modify to tusu
            if (record.getString("isDdl") == null || record.getString("data") == null
                || record.getString("database") == null || record.getString("es") == null
                || record.getString("mysqlType") == null || record.getString("sqlType") == null
                || record.getString("table") == null || record.getString("ts") == null
                || record.getString("type") == null) {
                return;
            }

            if (record.getString("isDdl").equals("true")) {
                return;
            }

            JSONArray data = record.getJSONArray("data");
            for (int i = 0; i < data.size(); i++) {
                JSONObject obj = data.getJSONObject(i);
                if (obj != null) {
                    StringBuilder fieldsBuilder = new StringBuilder();
                    fieldsBuilder.append(record.getLong("id"));
                    fieldsBuilder.append(fieldDelimiter);
                    fieldsBuilder.append(record.getLong("es"));
                    fieldsBuilder.append(fieldDelimiter);
                    fieldsBuilder.append(record.getLong("ts"));
                    fieldsBuilder.append(fieldDelimiter);
                    fieldsBuilder.append(record.getString("type"));

                    for (Entry<String, Object> entry : obj.entrySet()) {
                        fieldsBuilder.append(fieldDelimiter);
                        fieldsBuilder.append(entry.getValue());
                    }
                    fieldsBuilder.append(rowDelimiter);

                    outputStream.write(fieldsBuilder.toString().getBytes());
                }
            }
        }

        @Override
        public void close(TaskAttemptContext context) throws IOException, InterruptedException {
            outputStream.close();
        }
    }

    @Override
    public String getFilenameExtension() {
        return "";
    }

    @Override
    public RecordWriter<IEtlKey, CamusWrapper> getDataRecordWriter(TaskAttemptContext context, String fileName,
                                                                   CamusWrapper data,
                                                                   FileOutputCommitter committer) throws IOException,
                                                                                                  InterruptedException {
        Configuration conf = context.getConfiguration();
        String rowDelimiter = conf.get("etl.output.record.delimiter", "\n");

        Path path = new Path(committer.getWorkPath(),
                             EtlMultiOutputFormat.getUniqueFile(context, fileName, getFilenameExtension()));
        FileSystem fs = path.getFileSystem(conf);
        FSDataOutputStream outputStream = fs.create(path, false);

        return new CanalBinlogRecordWriter(outputStream, "^", rowDelimiter);
    }

}
