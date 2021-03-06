package me.egg82.antivpn.apis;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import me.egg82.antivpn.APIException;
import ninja.egg82.json.JSONWebUtil;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TeohAPI implements API {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public String getName() { return "teoh"; }

    public boolean isKeyRequired() { return false; }

    private static AtomicInteger requests = new AtomicInteger(0);
    private static ScheduledExecutorService threadPool = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder().setNameFormat("AntiVPN-TeohAPI-%d").build());

    static {
        threadPool.scheduleAtFixedRate(() -> requests.set(0), 0L, 24L, TimeUnit.HOURS);
    }

    public boolean getResult(String ip) throws APIException {
        if (ip == null) {
            throw new IllegalArgumentException("ip cannot be null.");
        }

        if (requests.getAndIncrement() >= 1000) {
            throw new APIException(true, "API calls to this source have been limited to 1,000/day as per request.");
        }

        JSONObject json;
        try {
            json = JSONWebUtil.getJsonObject("https://ip.teoh.io/api/vpn/" + ip, "egg82/AntiVPN");
        } catch (IOException | ParseException ex) {
            logger.error(ex.getMessage(), ex);
            throw new APIException(false, ex);
        }
        if (json == null || json.get("vpn_or_proxy") == null) {
            throw new APIException(false, "Could not get result from " + getName());
        }

        String proxy = (String) json.get("vpn_or_proxy");
        return proxy.equalsIgnoreCase("yes");
    }
}
