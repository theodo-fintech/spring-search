package com.sipios.springsearch.configuration

import com.sipios.springsearch.SpecificationsBuilder
import com.sipios.springsearch.anotation.SearchSpec
import org.slf4j.LoggerFactory
import org.springframework.core.MethodParameter
import org.springframework.data.jpa.domain.Specification
import org.springframework.lang.NonNull
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer

class SearchSpecificationResolver : HandlerMethodArgumentResolver {
    /**
     * Check if the method parameter is a Specification with the @SearchSpec annotation
     *
     * @param parameter THe method parameter to handle
     * @return True if it is the case
     */
    override fun supportsParameter(@NonNull parameter: MethodParameter): Boolean {
        return parameter.parameterType === Specification::class.java && parameter.hasParameterAnnotation(SearchSpec::class.java)
    }

    /**
     * Get the query parameter by the name defined in the [SearchSpec.searchParam]
     * Then use it to build the specification
     *
     * @param parameter THe method parameter to handle
     * @return True if it is the case
     */
    @Throws(Exception::class)
    override fun resolveArgument(
        @NonNull parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?
    ): Specification<*>? {
        val def = parameter.getParameterAnnotation(SearchSpec::class.java)

        return buildSpecification(parameter.genericParameterType.javaClass, webRequest.getParameter(def!!.searchParam))
    }

    private fun <T> buildSpecification(specClass: Class<T>, search: String?): Specification<T>? {
        logger.debug("Building specification for class {}", specClass)
        logger.debug("Search value found is {}", search)
        if (search == null || search.isEmpty()) {
            return null
        }
        val specBuilder = SpecificationsBuilder<T>()

        return specBuilder.withSearch(search).build()
    }

    companion object {
        private val logger = LoggerFactory.getLogger(SearchSpecificationResolver::class.java)
    }
}
