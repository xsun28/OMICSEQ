package com.omicseq.utils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author zejun.du
 */
public class MiscUtils {
    private static Logger logger = LoggerFactory.getLogger(MiscUtils.class);

    private static String server_ip = null;

    /**
     * get server ip
     * 
     * @return
     */
    public static String getServerIP() {
        if (null != server_ip) {
            if (logger.isDebugEnabled()) {
                logger.info("Server ip : " + server_ip);
            }
            return server_ip;
        }
        String localIp = null;
        String netIp = null;
        try {
            Enumeration<NetworkInterface> netInterfaces = NetworkInterface.getNetworkInterfaces();
            InetAddress ip = null;
            boolean netFinded = false;

            while (netInterfaces.hasMoreElements() && !netFinded) {
                NetworkInterface ni = netInterfaces.nextElement();
                Enumeration<InetAddress> address = ni.getInetAddresses();
                while (address.hasMoreElements()) {
                    ip = address.nextElement();
                    if (!ip.isSiteLocalAddress() && !ip.isLoopbackAddress() && ip.getHostAddress().indexOf(":") == -1) {
                        netIp = ip.getHostAddress();
                        netFinded = true;
                        break;
                    } else if (ip.isSiteLocalAddress() && !ip.isLoopbackAddress()
                            && ip.getHostAddress().indexOf(":") == -1) {
                        localIp = ip.getHostAddress();
                    }

                }
            }
        } catch (SocketException e) {
            logger.error("Error in getting server ip.");
        }

        String ip = "";
        if (netIp != null && !"".equals(netIp)) {
            ip = netIp;
        } else {
            ip = localIp;
        }
        if (logger.isInfoEnabled()) {
            logger.info("Local ip : " + ip);
        }
        server_ip = ip;
        return ip;
    }
}
