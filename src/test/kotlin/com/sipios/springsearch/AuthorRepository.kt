package com.sipios.springsearch

import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.repository.CrudRepository
import org.springframework.data.rest.core.annotation.RepositoryRestResource

@RepositoryRestResource(path = "author")
interface AuthorRepository : CrudRepository<Author, Long>, JpaSpecificationExecutor<Author>
