package com.cloudata.appendlog;

import java.io.File;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.robotninjas.barge.ClusterConfig;
import org.robotninjas.barge.Replica;

import com.cloudata.appendlog.AppendLogClient.AppendLogEntry;
import com.cloudata.snapshots.LocalSnapshotStorage;
import com.cloudata.snapshots.SnapshotStorage;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.sun.jersey.api.client.Client;

public class AppendLogIntegrationTest {

    static AppendLogServer[] SERVERS;
    static File TEMPDIR;

    @BeforeClass
    public static void startServers() throws Exception {
        TEMPDIR = Files.createTempDir();

        SERVERS = new AppendLogServer[3];

        for (int i = 0; i < SERVERS.length; i++) {
            Replica self = Replica.fromString("localhost:" + (10000 + i));
            List<Replica> allMembers = Lists.newArrayList();
            for (int j = 0; j < SERVERS.length; j++) {
              allMembers.add(Replica.fromString("localhost:" + (10000 + j)));
            }

            int httpPort = 9990 + i;

            File baseDir = new File(TEMPDIR, "" + i);
            
            AppendLogConfig config = new AppendLogConfig();
            config.httpPort = httpPort;
            config.seedConfig = new ClusterConfig(self, allMembers, ClusterConfig.buildDefaultTimeouts());
            
            SnapshotStorage snapshotStorage = new LocalSnapshotStorage(new File(TEMPDIR, "snapshots"));
            SERVERS[i] = new AppendLogServer(baseDir, self, config, snapshotStorage);

            SERVERS[i].start();
        }

        Thread.sleep(2000);
    }

    @AfterClass
    public static void stopServers() throws Exception {
        for (int i = 0; i < SERVERS.length; i++) {
            SERVERS[i].stop();
        }

        rmdir(TEMPDIR);
    }

    private static void rmdir(File dir) {
        for (File f : dir.listFiles()) {
            if (f.isFile()) {
                f.delete();
            } else {
                rmdir(f);
            }
        }

        dir.delete();
    }

    @Test
    public void simpleTest() throws Exception {
        Client client = Client.create();
        client.setFollowRedirects(true);

        String url = SERVERS[0].getHttpUrl();

        long logId = 2;

        AppendLogClient appendLogClient = new AppendLogClient(client, url);

        for (int i = 1; i < 100; i++) {
            byte[] data = new byte[i];
            for (int j = 0; j < i; j++) {
                data[j] = (byte) (j % 0xff);
            }
            appendLogClient.append(logId, data);
        }

        long position = 0;
        for (int i = 1; i < 100; i++) {
            AppendLogEntry entry = appendLogClient.read(logId, position);
            byte[] data = entry.getData();
            Assert.assertEquals(i, data.length);

            for (int j = 0; j < i; j++) {
                Assert.assertEquals((byte) (j % 0xff), data[j]);
            }

            position = entry.getNextPosition();
        }

    }
}
