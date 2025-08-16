package com.platform.groundcontrol

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.ComponentScan

@SpringBootApplication
@EnableCaching
@ComponentScan(basePackages = ["com.platform.groundcontrol"])
class GroundControlApplication

fun main(args: Array<String>) {
	runApplication<GroundControlApplication>(*args)
}
