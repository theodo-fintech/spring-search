package com.sipios.springsearch

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table

@Entity
@Table(name = "author")
open class Author {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    open var id: Long? = null

    @OneToMany(mappedBy = "author", cascade = [CascadeType.ALL], orphanRemoval = true)
    open var books: MutableList<Book> = mutableListOf()

    @Column(name = "name")
    open var name: String? = null

    fun addBook(book: Book) {
        books.add(book)
        book.author = this
    }

    fun removeBook(book: Book) {
        books.remove(book)
        book.author = null
    }
}
