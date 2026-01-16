package net.xolt.sbutils.util;

import net.xolt.sbutils.SbUtils;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class DnsUtils {

    public static List<String> srvLookup(String srvQuery) {
        List<String> records = dnsLookup(srvQuery, "SRV");

        if (records.isEmpty()) {
            SbUtils.LOGGER.error("skyblock.net has no SRV records!");
            return List.of();
        }

        List<String> result = new ArrayList<>();
        for (String record : records) {
            // SRV format: "priority weight port target"
            // Example: "0 5 25565 server.skyblock.net."
            String[] parts = record.split("\\s+");
            String target = parts[3];
            // Remove trailing dot if present
            if (target.endsWith(".")) {
                target = target.substring(0, target.length() - 1);
            }
            result.add(target);
        }
        return result;
    }

    public static List<InetAddress> aRecordLookup(String domain, boolean ipv6) {
        List<String> records = dnsLookup(domain, ipv6 ? "AAAA" : "A");

        if (records.isEmpty() && !ipv6) {
            SbUtils.LOGGER.error("The SRV target {} does not resolve to any {} address.", domain, ipv6 ? "IPv6" : "IPv4");
            return List.of();
        }

        List<InetAddress> result = new ArrayList<>();

        for (String record : records) {
            try {
                result.add(InetAddress.getByName(record));
            } catch (UnknownHostException e) {
                SbUtils.LOGGER.error(e.getLocalizedMessage());
            }
        }

        return result;
    }

    public static List<String> dnsLookup(String domain, String recordType) {
        List<String> result = new ArrayList<>();
        Hashtable<String, String> env = new Hashtable<>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.dns.DnsContextFactory");

        DirContext ctx;
        Attributes attrs;

        try {
            ctx = new InitialDirContext(env);
            attrs = ctx.getAttributes(domain, new String[]{recordType});
        } catch (NamingException e) {
            SbUtils.LOGGER.error("Failed to perform DNS lookup on " + domain + " for record type " + recordType);
            SbUtils.LOGGER.error(e.getLocalizedMessage());
            return result;
        }

        Attribute attr = attrs.get(recordType);

        if (attr == null) {
            return result;
        }

        for (int i = 0; i < attr.size(); i++) {
            try {
                result.add((String) attr.get(i));
            } catch (NamingException e) {
                SbUtils.LOGGER.error("One record of type \"" + recordType + "\" was unable to be retrieved for " + domain);
                SbUtils.LOGGER.error(e.getExplanation());
            }
        }

        return result;
    }
}
