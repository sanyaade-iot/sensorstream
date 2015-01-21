package cgl.sensorstream.core.kafka;

import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
import cgl.sensorstream.core.ComponentConfiguration;
import cgl.sensorstream.core.Utils;
import cgl.sensorstream.core.ZKDestinationChanger;
import cgl.sensorstream.core.rabbitmq.DefaultRabbitMQMessageBuilder;
import com.ss.commons.DestinationChanger;
import com.ss.commons.MessageBuilder;
import com.ss.commons.SpoutConfigurator;

import java.util.List;
import java.util.Map;

public class KafkaSpoutConfigurator implements SpoutConfigurator {
    private int queueSize = 64;

    private String messageBuilder;

    private List<String> fields;

    private String sensor;

    private String channel;

    private String zkConnectionString;

    private String topologyName;

    private ComponentConfiguration configuration;

    private String stream;

    public KafkaSpoutConfigurator(ComponentConfiguration configuration) {
        this.topologyName = configuration.getTopologyConfiguration().getTopologyName();
        this.sensor = configuration.getSensor();
        this.channel = configuration.getChannel();
        this.messageBuilder = configuration.getMessageBuilder();
        this.fields = configuration.getFields();
        this.queueSize = configuration.getQueueSize();
        this.zkConnectionString = configuration.getTopologyConfiguration().getZkConnectionString();
        this.configuration = configuration;
        this.stream = configuration.getStream();
    }

    @Override
    public MessageBuilder getMessageBuilder() {
        if (messageBuilder != null) {
            return (MessageBuilder) Utils.loadMessageBuilder(messageBuilder);
        } else {
            return new DefaultKafkaMessageBuilder();
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        if (stream == null) {
            outputFieldsDeclarer.declare(new Fields(fields));
        } else {
            outputFieldsDeclarer.declareStream(stream, new Fields(fields));
        }
    }

    @Override
    public int queueSize() {
        return queueSize;
    }

    @Override
    public Map<String, String> getProperties() {
        return configuration.getProperties();
    }

    @Override
    public DestinationChanger getDestinationChanger() {
        return new ZKDestinationChanger(topologyName, sensor, channel, zkConnectionString, false, true);
    }

    @Override
    public String getStream() {
        return stream;
    }
}
