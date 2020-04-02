.. _camus_config:

Camus Configuration Options
===========================

Schema Registry Configuration
-----------------------------
``schema.registry.url``
 URL for schema registry. This is required for Avro message decoder to interact with schema registry.

 * Type: string
 * Required: Yes

``max.schemas.per.subject``
 The max number of schema objects allowed per subject.

 * Type: int
 * Required: No
 * Default: 1000

``is.new.producer``
 Set to true if the data Camus is consuming from Kafka was created using the new producer, or
 set to false if it was created with the old producer.

 * Type: boolean
 * Required: No
 * Default: true

Camus Job Configuration
-----------------------
``camus.job.name``
 The name of the Camus job.

 * Type: string
 * Required: No
 * Default: "Camus Job"


``camus.message.decoder.class``
 Decoder class for Kafka Messages to Avro Records. The default value is Confluent version of
 AvroMessageDecoder which integrates schema registry.

 * Type: string
 * Required: No
 * Default: io.confluent.camus.etl.kafka.coders.AvroMessageDecoder

``etl.record.writer.provider.class``
 Class for writing records to HDFS/S3.

 * Type: string
 * Required: No
 * Default: com.linkedin.camus.etl.kafka.common.AvroRecordWriterProvider

``etl.partitioner.class``
 Class for partitioning Camus output. The default partitioner partitions incoming data
 into hourly partitions

 * Type: string
 * Required: No
 * Default: com.linkedin.camus.etl.kafka.partitioner.DefaultPartitioner

``camus.work.allocator.class``
 Class for creating input splits from ETL requests

 * Type: string
 * Required: No
 * Default: com.linkedin.camus.workallocater.BaseAllocator

``etl.destination.path``
 Top-level output directory, sub-directories will be dynamically created for each topic pulled.

 * Type: string
 * Required: Yes

``etl.execution.base.path``
 HDFS location where you want to keep execution files, i.e. offsets, error logs and count files.

 * Type: string
 * Required: Yes

``etl.execution.history.path``
 HDFS location to keep historical execution files, usually a sub directory of ``etl.execution.base.path``.

 * Type: string
 * Required: Yes

``hdfs.default.classpath.dir``
 All files in this directory will be added to the distributed cache and placed on the classpath for Hadoop tasks.

 * Type: string
 * Required: No
 * Default: null

``mapred.map.tasks``
 Max hadoop tasks to use, each task can pull multiple topic partitions.

 * Type: int
 * Required: No
 * Default: 30

Kafka Configuration
-------------------
``kafka.brokers``
 List of Kafka brokers for Camus to pull metadata from.

 * Type: string
 * Required: Yes

``kafka.max.pull.hrs``
 The max duration from the timestamp of the first record to the timestamp of the last record. When the
 max is reached, the pull will cease. -1 means no limit.

 * Type: int
 * Required: No
 * Default: -1

``kafka.max.historical.days``
 Events with a timestamp older than this will be discarded. -1 means no limit.

 * Type: int
 * Required: No
 * Default: -1

``kafka.max.pull.minutes.per.task``
 Max minutes for each mapper to pull messages.

 * Type: int
 * Required: No
 * Default: -1

``kafka.blacklist.topics``
 Nothing on the blacklist is pulled from Kafka.

 * Type: string
 * Required: No
 * Default: null

``kafka.whitelist.topics``
 If whitelist has values, only whitelisted topic are pulled from Kafka.

 * Type: string
 * Required: No
 * Default: null

``etl.output.record.delimiter``
 Delimiter for writing string records.

 * Type: string
 * Required: No
 * Default: "\\n"

Example Configuration
---------------------
.. literalinclude:: camus.properties
   :language: ruby