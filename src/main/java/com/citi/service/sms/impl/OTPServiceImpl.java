package com.citi.service.sms.impl;

import com.citi.service.sms.OTPService;
import com.citi.util.CapString;
import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

import javax.net.ssl.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.Properties;

public class OTPServiceImpl implements OTPService {
    private static Logger logger = Logger.getLogger(OTPServiceImpl.class);


    private Properties prop;

    public OTPServiceImpl(Properties prop){
        this.prop = prop;
    }

    /**
     *
     * @param phoneNumbers +8869xxxxxxxx
     * @param message
     *            要傳送的訊息
     * @return
     * @throws Exception
     */
    public String sendOTPbySMS(String phoneNumbers, String message) throws Exception {
        if (CapString.isEmpty(phoneNumbers)) {
            throw new IllegalArgumentException("There is no phone number.");
        }
        if (CapString.isEmpty(message)) {
            throw new IllegalArgumentException("Message is blank.");
        }
        StringBuffer answer = new StringBuffer();
        String host = prop.getProperty("sms.host");
        String port = prop.getProperty("sms.port");
        String entry = prop.getProperty("sms.entry");
        String username = prop.getProperty("sms.username");
        String password = prop.getProperty("sms.password");
        String proxyEnable = prop.getProperty("proxy.enable");
        String proxyHost = prop.getProperty("proxy.host");
        String proxyPort = prop.getProperty("proxy.port");
        String encoding = prop.getProperty("sms.encoding");
        int timeout = 3000;
        BufferedReader recv = null;
        HttpsURLConnection s = null;
        BufferedWriter writer = null;
        if (CapString.isEmpty(host)) {
            throw new IllegalArgumentException("sms.host is blank.");
        }
        if (CapString.isEmpty(port)) {
            throw new IllegalArgumentException("sms.port is blank.");
        }
        if (CapString.isEmpty(username)) {
            throw new IllegalArgumentException("sms.username is blank.");
        }
        if (CapString.isEmpty(password)) {
            throw new IllegalArgumentException("sms.password is blank.");
        }
        if (CapString.isEmpty(entry)) {
            throw new IllegalArgumentException("sms.entry is blank.");
        }
        if (CapString.isEmpty(encoding)) {
            encoding = "BIG5";
        }
        if ("true".equalsIgnoreCase(proxyEnable)) {
            if (CapString.isEmpty(proxyHost) || CapString.isEmpty(proxyPort)) {
                logger.error("proxy doesn't set.");
            }
        } else {
            proxyHost = null;
            proxyPort = "-1";
        }

        try {
            String tempMessage = "[MSISDN]\n";
            tempMessage += "List=" + phoneNumbers + "\n";
            tempMessage += "[MESSAGE]\nText=";
            message = tempMessage + ("UTF8".equalsIgnoreCase(encoding) ? new String(Base64.encodeBase64(message.getBytes("BIG5"))) : message);
            message += "\n[SETUP]\n";
            message += "DCS=" + encoding + "\n";
            message += "[END]";
            // HTTPS
            s = HttpsConnectionOpener.openConnection("https", host, port, entry, timeout, "true".equalsIgnoreCase(proxyEnable), proxyHost, proxyPort);
            if (s == null) {
                throw new IllegalArgumentException("The httpd connection couldn't be opened ");
            }
            s.setRequestMethod("POST");
            s.addRequestProperty("Authorization", "Basic " + new String(Base64.encodeBase64((username + ":" + password).getBytes())));
            s.addRequestProperty("CONTENT-LENGTH", Integer.toString(message.getBytes().length));
            int count = 0;
            int length = message.length() + count;
            logger.debug("length=" + length);
            logger.debug("sending message:\n" + message);
            writer = new BufferedWriter(new OutputStreamWriter(s.getOutputStream(), "BIG5"));
            writer.write(message);
            writer.flush();
            recv = new BufferedReader(new InputStreamReader(s.getInputStream()));
            String line = null;
            while ((line = recv.readLine()) != null) {
                answer.append(line).append("\n");
            }
        } catch (Exception e) {
            logger.error("proxy doesn't set.", e);
        } finally {
        	if(s!=null)
        		s.disconnect();
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                }
            }
            if (recv != null) {
                try {
                    recv.close();
                } catch (IOException e) {
                }
            }
        }

        return answer.toString();
    }

    static class HttpsConnectionOpener implements Runnable {
        private String protocol;
        private String host;
        private String port;
        private String entry;
        private String proxyHost;
        private String proxyPort;
        private boolean proxyEnable;
        private HttpsURLConnection connection;

        public static HttpsURLConnection openConnection(String protocol, String host, String port, String entry, int timeout, boolean proxyEnable, String proxyHost, String proxyPort) {
            HttpsConnectionOpener opener = proxyEnable ? new HttpsConnectionOpener(protocol, host, port, entry, proxyHost, proxyPort) : new HttpsConnectionOpener(protocol, host, port, entry);
            Thread t = new Thread(opener);
            t.start();
            try {
                t.join(timeout);
            } catch (InterruptedException exception) {
            }
            return opener.getConnection();
        }

        public HttpsConnectionOpener(String protocol, String host, String port, String entry) {
            this.protocol = protocol;
            this.host = host;
            this.port = port;
            this.entry = entry;
            proxyEnable = false;
            proxyHost = null;
            proxyPort = null;
            connection = null;
        }

        public HttpsConnectionOpener(String protocol, String host, String port, String entry, String proxyHost, String proxyPort) {
            this.protocol = protocol;
            this.host = host;
            this.port = port;
            this.entry = entry;
            this.proxyHost = proxyHost;
            this.proxyPort = proxyPort;
            proxyEnable = true;
            connection = null;
        }

        public void run() {
            try {
                if (proxyEnable) {
                    Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, Integer.parseInt(proxyPort)));
                    connection = this.getHttpConnection(protocol, host, port, proxy);
                } else {
                    connection = this.getHttpConnection(protocol, host, port, null);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public HttpsURLConnection getConnection() {
            return connection;
        }

        private HttpsURLConnection getHttpConnection(String protocol, String host, String port, Proxy proxy) throws Exception {
            String str = protocol + "://" + host + ":" + port + entry;
            URL localURL = new URL(str);
            HttpsURLConnection localHttpsURLConnection;
            if (proxy == null) {
                localHttpsURLConnection = (HttpsURLConnection) localURL.openConnection();
            } else {
                localHttpsURLConnection = (HttpsURLConnection) localURL.openConnection(proxy);
            }
            if ("https".equalsIgnoreCase(protocol)) {
                TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    }

                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    }
                } };
                SSLContext sc = SSLContext.getInstance("SSL");
                sc.init(null, trustAllCerts, new java.security.SecureRandom());
                localHttpsURLConnection.setSSLSocketFactory(sc.getSocketFactory());
            }
            localHttpsURLConnection.setHostnameVerifier(new IPHostNameVerifier());
            localHttpsURLConnection.setDoOutput(true);
            return localHttpsURLConnection;
        }

        private class IPHostNameVerifier implements HostnameVerifier {
            public boolean verify(String paramString, SSLSession paramSSLSession) {
                if (paramString.compareTo(paramSSLSession.getPeerHost()) != 0) {
                    return false;
                }
                try {
                    String str1 = paramSSLSession.getPeerCertificateChain()[0].getSubjectDN().toString();
                    System.out.println(str1);
                    int i = str1.indexOf("CN=");
                    if (i == -1) {
                        return false;
                    }
                    String str2 = str1.substring(i + 3, str1.indexOf(',', i));
                    if (paramString.compareTo(str2) == 0) {
                        return true;
                    }
                } catch (SSLPeerUnverifiedException localSSLPeerUnverifiedException) {
                    localSSLPeerUnverifiedException.printStackTrace();
                }
                return false;
            }
        }

    }
}
