package com.sipios.srql

import org.junit.Assert
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import org.junit.runner.RunWith
import org.springframework.transaction.annotation.Transactional


@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes = [SpringRestQueryLanguageApplication::class])
@Transactional
class SpringRestQueryLanguageApplicationTest {

    @Autowired
    private lateinit var userRepository: UsersRepository

    @Test
    fun run() {}

    @Test
    fun canAddUsers() {
        userRepository.save(Users())

        Assert.assertEquals(1, userRepository.findAll().count())
    }

    @Test
    fun canGetUserWithId() {
        var user = Users()
        userRepository.save(Users())
        user = userRepository.save(user)

        val specification = SpecificationsBuilder<Users>().withSearch("userId:" + user.userId).build()
        Assert.assertEquals(1, userRepository.findAll(specification).count())
    }

    @Test
    fun canGetUserWithName() {
        userRepository.save(Users(userFirstName = "Alice"))
        userRepository.save(Users(userFirstName = "Bob"))

        val specification = SpecificationsBuilder<Users>().withSearch("userFirstName:Alice").build()
        Assert.assertEquals(1, userRepository.findAll(specification).count())
    }

    @Test
    fun canGetUserWithFirstNameAndLastName() {
        userRepository.save(Users(userFirstName = "Alice", userLastName = "One"))
        userRepository.save(Users(userFirstName = "Alice", userLastName = "Two"))
        userRepository.save(Users(userFirstName = "Bob", userLastName = "One"))
        userRepository.save(Users(userFirstName = "Bob", userLastName = "Two"))

        val specification = SpecificationsBuilder<Users>().withSearch("userFirstName:Alice AND userLastName:One").build()
        Assert.assertEquals(1, userRepository.findAll(specification).count())
    }

    @Test
    fun canGetUserWithFrenchName() {
        userRepository.save(Users(userFirstName = "Édouard", userLastName = "Pröst"))

        val specification = SpecificationsBuilder<Users>().withSearch("userFirstName:Édouard AND userLastName:Pröst").build()
        Assert.assertEquals(1, userRepository.findAll(specification).count())
    }

    @Test
    fun canGetUserWithChineseName() {
        userRepository.save(Users(userFirstName = "毛澤東"))

        val specification = SpecificationsBuilder<Users>().withSearch("userFirstName:毛澤東").build()
        Assert.assertEquals(1, userRepository.findAll(specification).count())
    }

    @Test
    fun canGetUserWithSpecialCharactersName() {
        userRepository.save(Users(userFirstName = "__&@#*\"''^^^\$``%=+§()(hack3rman__"))

        val specification = SpecificationsBuilder<Users>().withSearch("userFirstName:__&@#*\"''^^^\$``%=+§()(hack3rman__").build()
        Assert.assertEquals(1, userRepository.findAll(specification).count())
    }

    @Test
    fun canGetUserWithBigFamily() {
        userRepository.save(Users(userChildrenNumber = 5))
        userRepository.save(Users(userChildrenNumber = 1))
        userRepository.save(Users(userChildrenNumber = 2))
        userRepository.save(Users(userChildrenNumber = 4))
        userRepository.save(Users(userChildrenNumber = 6))
        userRepository.save(Users(userChildrenNumber = 2))
        userRepository.save(Users(userChildrenNumber = 5))

        val specification = SpecificationsBuilder<Users>().withSearch("userChildrenNumber>4").build()
        Assert.assertEquals(3, userRepository.findAll(specification).count())
    }

    @Test
    fun canGetUserWithChildrenEquals() {
        userRepository.save(Users(userChildrenNumber = 5))
        userRepository.save(Users(userChildrenNumber = 1))
        userRepository.save(Users(userChildrenNumber = 2))
        userRepository.save(Users(userChildrenNumber = 4))
        userRepository.save(Users(userChildrenNumber = 4))
        userRepository.save(Users(userChildrenNumber = 4))
        userRepository.save(Users(userChildrenNumber = 4))
        userRepository.save(Users(userChildrenNumber = 6))
        userRepository.save(Users(userChildrenNumber = 2))
        userRepository.save(Users(userChildrenNumber = 5))

        val specification = SpecificationsBuilder<Users>().withSearch("userChildrenNumber:4").build()
        Assert.assertEquals(4, userRepository.findAll(specification).count())
    }

    @Test
    fun canGetUserWithSmallerSalary() {
        userRepository.save(Users(userSalary = 2223.3F))
        userRepository.save(Users(userSalary = 1500.2F))
        userRepository.save(Users(userSalary = 4000.0F))
        userRepository.save(Users(userSalary = 2550.7F))
        userRepository.save(Users(userSalary = 2300.0F))

        val specification = SpecificationsBuilder<Users>().withSearch("userSalary<2300").build()
        Assert.assertEquals(2, userRepository.findAll(specification).count())
    }

    @Test
    fun canGetUserWithHigherSalary() {
        userRepository.save(Users(userSalary = 2323.3F))
        userRepository.save(Users(userSalary = 1500.2F))
        userRepository.save(Users(userSalary = 4000.1F))
        userRepository.save(Users(userSalary = 5350.7F))

        val specification = SpecificationsBuilder<Users>().withSearch("userSalary>4000.001").build()
        Assert.assertEquals(2, userRepository.findAll(specification).count())
    }

    @Test
    fun canGetUserWithMedianSalary() {
        userRepository.save(Users(userSalary = 2323.3F))
        userRepository.save(Users(userSalary = 1500.2F))
        userRepository.save(Users(userSalary = 4000.1F))
        userRepository.save(Users(userSalary = 5350.7F))

        val specification = SpecificationsBuilder<Users>().withSearch("userSalary<4000.1 AND userSalary>1500.2").build()
        Assert.assertEquals(1, userRepository.findAll(specification).count())
    }

    @Test
    fun canGetUserWithParentheses() {
        userRepository.save(Users(userSalary = 1500.1F, userLastName = "One"))
        userRepository.save(Users(userSalary = 1500.2F, userLastName = "One"))
        userRepository.save(Users(userSalary = 1500.1F, userLastName = "Two"))
        userRepository.save(Users(userSalary = 1500.2F, userLastName = "Two"))
        userRepository.save(Users(userSalary = 1500.1F, userLastName = "Three"))
        userRepository.save(Users(userSalary = 1500.2F, userLastName = "Three"))

        val specification = SpecificationsBuilder<Users>().withSearch("userSalary>1500.1 AND ( userLastName:One OR userLastName:Two )").build()
        Assert.assertEquals(2, userRepository.findAll(specification).count())
    }
}
