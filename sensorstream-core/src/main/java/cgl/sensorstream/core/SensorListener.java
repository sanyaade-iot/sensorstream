package cgl.sensorstream.core;

import cgl.iotcloud.core.api.thrift.TChannel;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;
import org.apache.curator.utils.ZKPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SensorListener {
    private Logger LOG = LoggerFactory.getLogger(SensorListener.class);

    private CuratorFramework client = null;
    private PathChildrenCache cache = null;

    private String sensorPath = null;
    private String channel = null;

    private String connectionString = null;

    private Map<String, ChannelListener> channelListeners = new HashMap<String, ChannelListener>();

    public SensorListener(String sensorPath, String channel, String connectionString) {
        try {
            this.sensorPath = sensorPath;
            this.channel = channel;
            this.connectionString = connectionString;

            client = CuratorFrameworkFactory.newClient(connectionString, new ExponentialBackoffRetry(1000, 3));
            client.start();

            cache = new PathChildrenCache(client, sensorPath, true);
            cache.start();
        } catch (Exception e) {
            String msg = "Failed to create the listener for ZK path " + sensorPath;
            LOG.error(msg);
            throw new RuntimeException(msg);
        }

    }

    private static void addListener(PathChildrenCache cache) {
        // a PathChildrenCacheListener is optional. Here, it's used just to log changes
        PathChildrenCacheListener listener = new PathChildrenCacheListener() {
            @Override
            public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
                switch (event.getType()) {
                    case CHILD_ADDED: {
                        System.out.println("Node added: " + ZKPaths.getNodeFromPath(event.getData().getPath()));
                        break;
                    }

                    case CHILD_UPDATED: {
                        System.out.println("Node changed: " + ZKPaths.getNodeFromPath(event.getData().getPath()));
                        break;
                    }

                    case CHILD_REMOVED: {
                        System.out.println("Node removed: " + ZKPaths.getNodeFromPath(event.getData().getPath()));
                        break;
                    }
                }
            }
        };
        cache.getListenable().addListener(listener);
    }

    public void start() {
        if (cache.getCurrentData().size() != 0) {
            for (ChildData data : cache.getCurrentData()) {
                String path = data.getPath();

                ChannelListener channelListener = new ChannelListener(path, channel, connectionString);
                channelListeners.put(path, channelListener);
            }
        }
    }

    private List<TChannel> getChannels(String sensorId) {
        return null;
    }

    public void close() {
        CloseableUtils.closeQuietly(cache);
        CloseableUtils.closeQuietly(client);
    }
}
