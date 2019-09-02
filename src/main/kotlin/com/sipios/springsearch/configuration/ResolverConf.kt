package com.sipios.springsearch.configuration

import org.springframework.context.annotation.Configuration
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class ResolverConf : WebMvcConfigurer {

    /**
     * Register a SearchSpecificationResolver instance to the list of argument resolver used by Spring MVC
     * @param argumentResolvers The current list of argumentResolversUsed by Spring MVC
     */
    override fun addArgumentResolvers(argumentResolvers: MutableList<HandlerMethodArgumentResolver>) {
        argumentResolvers.add(SearchSpecificationResolver())
    }
}
