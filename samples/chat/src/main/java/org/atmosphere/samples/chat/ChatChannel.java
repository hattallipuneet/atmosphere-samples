/*
 * Copyright 2015 Async-IO.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.atmosphere.samples.chat;

import org.atmosphere.client.TrackMessageSizeInterceptor;
import org.atmosphere.config.service.*;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResourceEvent;
import org.atmosphere.cpr.BroadcasterFactory;
import org.atmosphere.interceptor.AtmosphereResourceLifecycleInterceptor;
import org.atmosphere.interceptor.HeartbeatInterceptor;
import org.atmosphere.interceptor.SuspendTrackerInterceptor;
import org.atmosphere.samples.chat.custom.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;

import static org.atmosphere.cpr.ApplicationConfig.MAX_INACTIVE;

/**
 * Simple annotated class that demonstrate the power of Atmosphere. This class supports all transports, support
 * message length garantee, heart beat, message cache thanks to the {@link ManagedService}.
 */
@Config
@ManagedService(
        path = "/channel/{id: [a-zA-Z][a-zA-Z_0-9]*}",
        atmosphereConfig = MAX_INACTIVE + "=120000",
        interceptors = {
                AtmosphereResourceLifecycleInterceptor.class,
                TrackMessageSizeInterceptor.class,
                SuspendTrackerInterceptor.class,
                HeartbeatInterceptor.class
        }
)
public class ChatChannel {
    private final Logger logger = LoggerFactory.getLogger(ChatChannel.class);

// Uncomment for changing response's state
//    @Get
//    public void init(AtmosphereResource r) {
//        r.getResponse().setCharacterEncoding("UTF-8");
//    }

    // For demonstrating injection.
    @Inject
    private BroadcasterFactory factory;

    @Inject
    private AtmosphereResource r;

    @Inject
    private AtmosphereResourceEvent event;

    @Heartbeat
    public void onHeartbeat(final AtmosphereResourceEvent event) {
        logger.trace("Heartbeat send by {}", event.getResource());
    }

    /**
     * Invoked when the connection as been fully established and suspended, e.g ready for receiving messages.
     */
    @Ready
    public void onReady(/* In you don't want injection AtmosphereResource r */) {
        AutoChatterChannel.startAutoChatter(r.uuid());
    }

    /**
     * Invoked when the client disconnect or when an unexpected closing of the underlying connection happens.
     */
    @Disconnect
    public void onDisconnect(/** If you don't want to use injection AtmosphereResourceEvent event*/) {
        if (event.isCancelled()) {
            logger.info("Browser {} unexpectedly disconnected", event.getResource().uuid());
        } else if (event.isClosedByClient()) {
            logger.info("Browser {} closed the connection", event.getResource().uuid());
        }
        AutoChatterChannel.stopAutoChatter();
    }

    /**
     * Simple annotated class that demonstrate how {@link org.atmosphere.config.managed.Encoder} and {@link org.atmosphere.config.managed.Decoder
     * can be used.
     *
     * @param message an instance of {@link Message}
     * @return
     * @throws IOException
     */
    @org.atmosphere.config.service.Message(encoders = {JacksonEncoder.class}, decoders = {JacksonDecoder.class})
    public Message onMessage(Message message) throws IOException {
        logger.info("{} just send {}", message.getAuthor(), message.getMessage());
        return message;
    }

}