package nafos.server.handle.http

import io.netty.buffer.ByteBuf
import io.netty.handler.codec.http.HttpHeaders
import io.netty.handler.codec.http.HttpRequest
import io.netty.handler.codec.http.QueryStringDecoder
import io.netty.handler.codec.http.cookie.Cookie
import io.netty.handler.codec.http.cookie.ServerCookieDecoder
import io.netty.handler.codec.http.multipart.*
import io.netty.util.CharsetUtil
import nafos.server.util.JsonUtil
import java.util.HashMap

/**
 * @Description nafos-request
 * @Author      xinyu.huang
 * @Time        2019/11/24 20:05
 */
class NsRequest(
        request: HttpRequest,
        content: ByteBuf,
        trailingHeaders: HttpHeaders?
) : nafos.server.handle.http.BuildHttpObjectAggregator.AggregatedFullHttpRequest(request, content, trailingHeaders) {

    var cookies = setOf<Cookie>()

    var requestParams: Map<String, String> = mutableMapOf()

    var bodyParams: Map<String, Any> = mutableMapOf()

    var ip: String? = null

    private var httpPostRequestDecoder: HttpPostRequestDecoder? = null


    init {
        //cookies
        val cookieStr = headers().get("Cookie")
        cookieStr?.run {
            cookies = ServerCookieDecoder.LAX.decode(cookieStr)
        }
        //注册requestParams
        val map = mutableMapOf<String, String>()
        val decoder = QueryStringDecoder(uri())
        for (entry in decoder.parameters().entries) {
            map[entry.key] = entry.value[0]
        }
        requestParams = map
        //注册bodyParams
        var strContentType = headers().get("Content-Type")
        strContentType = strContentType?.trim() ?: ""
        bodyParams = if (strContentType.contains("application/json")) {
            getJsonParams()
        } else {
            getFormParams()
        }
    }


    /***
     *@Description 获取单个cookie
     *@Author      xinyu.huang
     *@Time        2019/11/24 20:16
     */
    fun getCookie(name: String): String? {
        val it = cookies.iterator()
        while (it.hasNext()) {
            val cookie = it.next()
            if (cookie.name() == name) {
                return cookie.value()
            }
        }
        return null
    }


    /***
     *@Description 解析from表单数据（Content-Type = x-www-form-urlencoded）,默认格式
     *@Author      xinyu.huang
     *@Time        2019/11/24 20:18
     */
    internal fun getFormParams(): Map<String, Any> {
        val params = HashMap<String, Any>()
        httpPostRequestDecoder = HttpPostRequestDecoder(DefaultHttpDataFactory(false), this)
        val postData = httpPostRequestDecoder!!.bodyHttpDatas
        for (data in postData) {
            if (data.httpDataType == InterfaceHttpData.HttpDataType.Attribute) {
                val attribute = data as MemoryAttribute
                params[attribute.name] = attribute.value
            }
        }
        return params
    }

    /***
     *@Description 解析json数据（Content-Type = application/json）
     *@Author      xinyu.huang
     *@Time        2019/11/24 21:18
     */
    private fun getJsonParams(): Map<String, Any> {
        val jsonBuf = content()
        val jsonStr = jsonBuf.toString(CharsetUtil.UTF_8)
        return if (jsonStr.isNullOrBlank()) {
            HashMap()
        } else JsonUtil.jsonToMap(jsonStr)
    }


    override fun release(): Boolean {
        if (httpPostRequestDecoder != null) {
            httpPostRequestDecoder!!.destroy()
            httpPostRequestDecoder = null
        }
        return super.release()
    }
}