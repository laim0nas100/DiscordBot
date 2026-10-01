package com.github.laim0nas100.discordbot;

import com.github.laim0nas100.commonslb.threads.service.ServiceExecutorAggregatorBase;
import com.github.laim0nas100.uncheckedutils.Checked;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import net.dv8tion.jda.api.JDA;

/**
 *
 * @author Lemmin
 */
public class Services {

    public Services(JDA jda, boolean async) {
        this.jda = Objects.requireNonNull(jda);
        this.async = async;
    }

    public final boolean async;
    public final JDA jda;

    public void assertJdaConnected() {
        JDA.Status status = jda.getStatus();
        if (status != JDA.Status.CONNECTED) {
            throw new IllegalStateException("JDA is not connected");
        }
    }

    public ServiceExecutorAggregatorBase executors = new ServiceExecutorAggregatorBase() {
        @Override
        protected ExecutorService createExecutor() {
            return Checked.createDefaultExecutorService();
        }
    };

    public ExecutorService getCacheExecutor(String guid) {
        return executors.getOrCreate(cacheExeName(guid), Executors::newSingleThreadExecutor);
    }

    public static String cacheExeName(String guid) {
        return guid + "_cache";
    }
    
    public void shutdown(){
        jda.shutdown();
        executors.shutdown();
    }

}
