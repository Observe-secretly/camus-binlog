# 介绍
Camus是Linkedin开源的一个从Kafka到HDFS的数据管道，实际上它是一个MapReduce作业。从Kafka拉取指定topic到HDFS，供批量/离线数据处理、分析。

# Camus特点
- 自动发现topic
- Avro schema管理
- 按时间分区

# 扩展改造
目前Camus已经停止更新了。但是confluentinc还在维护。所以代码通过获取 https://github.com/confluentinc/camus 最新代码得到。原生的Camus对时间分区支持的非常棒。但是这还不够。我司业务需要把binlog数据保存到数仓中。在load过程时，我们希望Camus不仅仅可以根据时间进行分区，还能针对binlog的特殊格式，根据database和table进行单独的分区。所以就有了Camus-binlog。

# 关键扩展
对源码感兴趣的同学请关注如下几个类：
</br>
`com.linkedin.camus.etl.kafka.coders.BinlogStringMessageDecoder.java:111行`
</br>
`com.linkedin.camus.etl.kafka.common.CanalBinlogRecordWriterProvider.java`
</br>
`com.linkedin.camus.etl.kafka.partitioner.BaseBinlogPartitioner.java:47行`
</br>
`com.linkedin.camus.etl.kafka.partitioner.BinlogPartitioner.java`
</br>
`com.linkedin.camus.etl.kafka.mapred.EtlMultiOutputCommitter.java:170行`

# 示例配置
```
# Kafka brokers
kafka.brokers=ambari03:6667
# job名称
camus.job.name=test-fetch
# Kafka数据落地到HDFS的位置。Camus会按照topic名自动创建子目录
etl.destination.path=/camus
# HDFS上用来保存当前Camus job执行信息的位置，如offset、错误日志等
etl.execution.base.path=/camus/exec
# HDFS上保存Camus job执行历史的位置
etl.execution.history.path=/camus/exec/history
# 即core-site.xml中的fs.defaultFS参数
fs.default.name=hdfs://ambari02:8020
# Kafka消息解码器，默认有JsonStringMessageDecoder和KafkaAvroMessageDecoder
# Canal的Binlog是JSON格式的。当然我们也可以自定义解码器
#camus.message.decoder.class=com.linkedin.camus.etl.kafka.coders.JsonStringMessageDecoder
camus.message.decoder.class=com.linkedin.camus.etl.kafka.coders.BinlogStringMessageDecoder
# 落地到HDFS时的写入器，默认支持Avro、SequenceFile和字符串
# 这里我们采用一个自定义的WriterProvider，代码在后面
#etl.record.writer.provider.class=com.linkedin.camus.etl.kafka.common.StringRecordWriterProvider
etl.record.writer.provider.class=com.linkedin.camus.etl.kafka.common.CanalBinlogRecordWriterProvider

# JSON消息中的时间戳字段，用来做分区的
# 注意这里采用Binlog的业务时间，而不是日志时间
camus.message.timestamp.field=es
# 时间戳字段的格式
camus.message.timestamp.format=unix_milliseconds
# 时间分区的类型和格式，默认支持小时、天，也可以自定义时间
etl.partitioner.class=com.linkedin.camus.etl.kafka.partitioner.BinlogPartitioner
etl.destination.path.topic.sub.dirformat='pt_hour'=YYYYMMddHH
# 拉取过程中MR job的mapper数
mapred.map.tasks=20
# 按照时间戳字段，一次性拉取多少个小时的数据过后就停止，-1为不限制
kafka.max.pull.hrs=-1
# 时间戳早于多少天的数据会被抛弃而不入库
kafka.max.historical.days=3
# 每个mapper的最长执行分钟数，-1为不限制
kafka.max.pull.minutes.per.task=-1
# Kafka topic白名单和黑名单
kafka.whitelist.topics=binlog-test-limin
kafka.blacklist.topics=
# 设定输出数据的压缩方式，支持deflate、gzip和snappy
mapred.output.compress=false
# etl.output.codec=gzip
# etl.deflate.level=6
# 设定时区，以及一个时间分区的单位
etl.default.timezone=Asia/Shanghai
etl.output.file.time.partition.mins=60

kafka.client.name=test

```

# 沟通交流
QQ:914245697
