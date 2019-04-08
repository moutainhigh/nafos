package nafos.security.cache

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory

class FiberDo {
    companion object {
        private val logger = LoggerFactory.getLogger(FiberDo::class.java)

        /**
         * 定时清除cacheMap中过期的session记录
         */
        fun doClearTimeOutSession(millisecond: Long) {
            GlobalScope.launch {
                while (true) {
                    delay(millisecond)
                    CacheMapDao.delTimeOut()
                    logger.debug("清除cacheMap中login信息, 【over】")
                }
            }
        }
    }
}

