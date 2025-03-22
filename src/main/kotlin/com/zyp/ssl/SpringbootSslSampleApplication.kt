package com.zyp.ssl

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication


@SpringBootApplication
class SpringbootSslSampleApplication

/**
 * http默认会进入登陆页面
 * 账号：user
 * 密码为控制台输出：Using generated security password: ef97682c-d2a1-4e98-9037-609964d33b6b
 */
fun main(args: Array<String>) {
    runApplication<SpringbootSslSampleApplication>(*args)
}
