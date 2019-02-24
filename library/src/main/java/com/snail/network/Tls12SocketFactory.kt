package com.snail.network

import java.io.IOException
import java.net.InetAddress
import java.net.Socket

import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory

/**
 * 描述: 让安卓4.4.4以下的系统支持TLS协议，方法加入okhttp中
 * 时间: 2018/12/7 21:47
 * 作者: zengfansheng
 */
internal class Tls12SocketFactory(private val delegate: SSLSocketFactory) : SSLSocketFactory() {

    override fun getDefaultCipherSuites(): Array<String> {
        return delegate.defaultCipherSuites
    }

    override fun getSupportedCipherSuites(): Array<String> {
        return delegate.supportedCipherSuites
    }

    @Throws(IOException::class)
    override fun createSocket(s: Socket, host: String, port: Int, autoClose: Boolean): Socket? {
        return patch(delegate.createSocket(s, host, port, autoClose))
    }

    @Throws(IOException::class)
    override fun createSocket(host: String, port: Int): Socket? {
        return patch(delegate.createSocket(host, port))
    }

    @Throws(IOException::class)
    override fun createSocket(host: String, port: Int, localHost: InetAddress, localPort: Int): Socket? {
        return patch(delegate.createSocket(host, port, localHost, localPort))
    }

    @Throws(IOException::class)
    override fun createSocket(host: InetAddress, port: Int): Socket? {
        return patch(delegate.createSocket(host, port))
    }

    @Throws(IOException::class)
    override fun createSocket(address: InetAddress, port: Int, localAddress: InetAddress, localPort: Int): Socket? {
        return patch(delegate.createSocket(address, port, localAddress, localPort))
    }

    private fun patch(s: Socket): Socket {
        if (s is SSLSocket) {
            s.enabledProtocols = tlsSupportVersion
        }
        return s
    }

    companion object {
        private val tlsSupportVersion = arrayOf("TLSv1.1", "TLSv1.2")
    }
}
