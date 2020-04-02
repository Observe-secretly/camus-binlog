Important Configuration Options
-------------------------------

Camus provides configurations to reduce overhead in operations. Below is an explanation of
how these configurations work, and why they are needed.

    #. ``kafka.max.pull.hrs``: This configuration limits timestamp duration for each topic partition
       pull from Kafka. Since Camus output is partitioned by timestamp, large pulls from Kafka
       may create a large number of files. For example, if you use hourly partition, a
       pull of 3 days data in a Camus job will simultaneously write to 72 files and some versions of
       Hadoop don't behave well in this case. Therefore, the ability to limit the timestamp duration
       is needed.

       The way how this works is that the timestamp for each event is compared to the timestamp of
       the first event. If the timestamp is greater than duration set,
       the pull is stopped and the next Camus job will pickup from where it left off.

    #. ``kafka.max.pull.minutes.per.task``: This configuration limits the pull time for MapReduce
       tasks. If the MapReduce task time is exceeded, the pulls are halted and the pull of this topic
       partition in the next Camus job will continue from the offset where it left off.

    #. ``kafka.max.historical.days``: This configuration limits the pull for historic data.
       Events older than the specified number of days will be ignored. This is useful in for
       situations where a valid offset is not available and a topic partition is pulled from the
       earliest offset.

    #. ``etl.fail.on.errors``: This configuration controls the behavior of a Camus job in case of
       exceptions. The default behavior is that Camus catches exceptions when decoding data and
       write them to the error file associated with the Camus job. This is reasonable as we don't
       want a single bad record to fail the Camus job. However, if you want to ensure data
       compatibility, you may want to set this configuration with value ``true`` to make sure that a
       Camus job fails in case of exceptions.
