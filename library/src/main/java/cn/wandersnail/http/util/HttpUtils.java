package cn.wandersnail.http.util;

import android.webkit.MimeTypeMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import cn.wandersnail.http.factory.Tls12SocketFactory;
import okhttp3.OkHttpClient;


/**
 * date: 2019/8/23 13:50
 * author: zengfansheng
 */
public class HttpUtils {
    public static class SSLParams {
        public SSLSocketFactory sSLSocketFactory;
        public X509TrustManager trustManager;
    }
    
    public static String getBaseUrl(@NonNull String url) {
        int index = url.indexOf("://");
        String subUrl = url.substring(index + 3);
        String urlHead = url.substring(0, index + 3);
        index = subUrl.indexOf("/");
        return index != -1 ? urlHead + subUrl.substring(0, index) : url;
    }
    
    public static SSLParams getSslSocketFactory(InputStream[] certificates, InputStream bksFile, String password) {
        SSLParams sslParams = new SSLParams();
        try {
            TrustManager[] trustManagers = prepareTrustManager(certificates);
            KeyManager[] keyManagers = prepareKeyManager(bksFile, password);
            SSLContext sslContext = SSLContext.getInstance("TLS");
            X509TrustManager trustManager;
            if (trustManagers != null) {
                trustManager = new MyTrustManager(chooseTrustManager(trustManagers));
            } else {
                trustManager = new UnSafeTrustManager();
            }
            sslContext.init(keyManagers, new TrustManager[]{trustManager}, null);
            sslParams.sSLSocketFactory = sslContext.getSocketFactory();
            sslParams.trustManager = trustManager;
            return sslParams;
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }
    
    public static class UnSafeTrustManager implements X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) {
            
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) {

        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }
    
    private static TrustManager[] prepareTrustManager(InputStream[] certificates) {
        if (certificates == null || certificates.length == 0) return null;
        try {
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null);
            for (int i = 0; i < certificates.length; i++) {
                String certificateAlias = String.valueOf(i);
                InputStream certificate = certificates[i];
                keyStore.setCertificateEntry(certificateAlias, certificateFactory.generateCertificate(certificate));
                try {
                    certificate.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);
            return trustManagerFactory.getTrustManagers();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static KeyManager[] prepareKeyManager(InputStream bksFile, String password) {
        try {
            if (bksFile == null || password == null) return null;
            KeyStore clientKeyStore = KeyStore.getInstance("BKS");
            clientKeyStore.load(bksFile, password.toCharArray());
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(clientKeyStore, password.toCharArray());
            return keyManagerFactory.getKeyManagers();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static X509TrustManager chooseTrustManager(TrustManager[] trustManagers) {
        for (TrustManager manager : trustManagers) {
            if (manager instanceof X509TrustManager) {
                return (X509TrustManager) manager;
            }
        }
        return null;
    }
    
    private static class MyTrustManager implements X509TrustManager {
        private X509TrustManager localTrustManager;
        private X509TrustManager defaultTrustManager;

        MyTrustManager(X509TrustManager localTrustManager) throws NoSuchAlgorithmException, KeyStoreException {
            this.localTrustManager = localTrustManager;
            TrustManagerFactory var4 = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            var4.init((KeyStore) null);
            defaultTrustManager = chooseTrustManager(var4.getTrustManagers());
        }

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            try {
                defaultTrustManager.checkServerTrusted(chain, authType);
            } catch (CertificateException ignore) {
                localTrustManager.checkServerTrusted(chain, authType);
            }
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }
    
    @NonNull
    public static OkHttpClient.Builder initHttpsClient(boolean isBypassAuth, @NonNull OkHttpClient.Builder builder) {
        if (isBypassAuth) {
            SSLParams sslParams = getSslSocketFactory(null, null, null);
            if (sslParams.sSLSocketFactory != null && sslParams.trustManager != null) {
                builder.sslSocketFactory(sslParams.sSLSocketFactory, sslParams.trustManager);
            }
        } else {
            SSLContext sslContext;
            try {
                sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, null, null);
            } catch (Exception e) {
                e.printStackTrace();
                return builder;
            }
            SSLSocketFactory socketFactory = new Tls12SocketFactory(sslContext.getSocketFactory());
            builder.sslSocketFactory(socketFactory, new UnSafeTrustManager());
        }
        return builder;
    }
    
    public static void closeQuietly(Closeable... closeables) {
        if (closeables != null) {
            for (Closeable closeable : closeables) {
                try {
                    closeable.close();
                } catch (Exception ignore) {
                }
            }
        }
    }

    @Nullable
    public static String getMimeType(@NonNull String filename) {
        String extension = MimeTypeMap.getFileExtensionFromUrl(filename);
        if (extension == null) {
            return null;
        }
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
    }
}
