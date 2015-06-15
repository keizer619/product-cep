/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.sample.performance;

import org.apache.log4j.Logger;
import org.wso2.carbon.databridge.agent.AgentHolder;
import org.wso2.carbon.databridge.agent.DataPublisher;
import org.wso2.carbon.databridge.commons.Event;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Random;

public class Client {
    private static Logger log = Logger.getLogger(Client.class);

    public static void main(String[] args) {
        log.info(Arrays.deepToString(args));
        try {
            log.info("Starting WSO2 Performance Test Client");

            AgentHolder.setConfigPath(DataPublisherUtil.getDataAgentConfigPath());
            DataPublisherUtil.setTrustStoreParams();

            String protocol = args[0];
            String host = args[1];
            String port = args[2];
            String username = args[3];
            String password = args[4];
            String eventCount = args[5];
            String elapsedCount = args[6];
            String warmUpCount = args[7];

            //create data publisher
            DataPublisher dataPublisher = new DataPublisher(protocol, "tcp://" + host + ":" + port, null, username,
                    password);

            //Publish event for a valid stream
            publishEvents(dataPublisher, Long.parseLong(eventCount), Long.parseLong(elapsedCount),
                    Long.parseLong(warmUpCount));

            dataPublisher.shutdownWithAgent();
        } catch (Throwable e) {
            log.error(e);
        }
    }

    private static void publishEvents(DataPublisher dataPublisher, long eventCount, long elapsedCount,
                                      long warmUpCount) {
        long counter = 0;
        // Random randomGenerator = new Random();
        String doorStreamId = "deviceDoorStream:1.0.0";
        String healthStreamId = "deviceHealthStream:1.0.0";

        long lastTime = System.currentTimeMillis();
        DecimalFormat decimalFormat = new DecimalFormat("#");

        while (counter < eventCount) {

            int deviceId = (int) (counter % 100);


            Event doorEvent = new Event(doorStreamId, System.currentTimeMillis(),
                    new Object[]{},
                    new Object[]{},
                    new Object[]{4, deviceId, 70, "2015 - 02 - 17 16:00:39", 70, "2015 - 02 - 17 16:00:39", "2015 - 02 - 17 16:00:39", 45});

            dataPublisher.publish(doorEvent);


            Event healthEvent = new Event(healthStreamId, System.currentTimeMillis(),
                    new Object[]{},
                    new Object[]{},
                    new Object[]{100, deviceId, 45, 70, "2015 - 02 - 17 16:00:39", 4, 4, 2.3, 6.6, 8.9, false, 45, 45, "2015 - 02 - 17 16:00:39"});

            dataPublisher.publish(healthEvent);

            if ((counter > warmUpCount) && ((counter + 2) % elapsedCount == 0)) {

                long currentTime = System.currentTimeMillis();
                long elapsedTime = currentTime - lastTime;
                double throughputPerSecond = (((double) elapsedCount) / elapsedTime) * 1000;
                lastTime = currentTime;
                log.info("Sent " + elapsedCount + " sensor events in " + elapsedTime
                        + " milliseconds with total throughput of " + decimalFormat.format(throughputPerSecond)
                        + " events per second.");
            }

            counter = counter + 2;
        }
    }
}
