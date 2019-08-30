package com.sipios.springsearch

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
class SpringSearchApplicationTest {

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

        val specification = SpecificationsBuilder<Users>().withSearch("userFirstName:%C3%89douard AND userLastName:Pr%C3%B6st").build()
        Assert.assertEquals(1, userRepository.findAll(specification).count())
    }

    @Test
    fun canGetUserWithChineseName() {
        userRepository.save(Users(userFirstName = "毛澤東"))

        val specification = SpecificationsBuilder<Users>().withSearch("userFirstName:%E6%AF%9B%E6%BE%A4%E6%9D%B1").build()
        Assert.assertEquals(1, userRepository.findAll(specification).count())
    }

    @Test
    fun canGetUserWithSpecialCharactersName() {
        userRepository.save(Users(userFirstName = "&@#*\"''^^^\$``%=+§()(__hack3rman__"))

        val specification = SpecificationsBuilder<Users>().withSearch("userFirstName:%26%40%23%2A%22%27%27%5E%5E%5E%24%60%60%25%3D%2B%C2%A7%28%29%28__hack3rman__").build()
        Assert.assertEquals(1, userRepository.findAll(specification).count())
    }

    @Test
    fun canGetUserWithSpaceInName() {
        userRepository.save(Users(userFirstName = "robert junior"))

        val specification = SpecificationsBuilder<Users>().withSearch("userFirstName:robert%20junior").build()
        Assert.assertEquals(1, userRepository.findAll(specification).count())
    }

    @Test
    fun canGetUsersWithPartialName() {
        userRepository.save(Users(userFirstName = "robert"))
        userRepository.save(Users(userFirstName = "roberta"))
        userRepository.save(Users(userFirstName = "robot"))
        userRepository.save(Users(userFirstName = "röbert"))

        val specification = SpecificationsBuilder<Users>().withSearch("userFirstName:robe*").build()
        Assert.assertEquals(2, userRepository.findAll(specification).count())
    }

    @Test
    fun canGetUsersWithPartialName2() {
        userRepository.save(Users(userFirstName = "rob*rt"))
        userRepository.save(Users(userFirstName = "rob*rta"))
        userRepository.save(Users(userFirstName = "robot"))
        userRepository.save(Users(userFirstName = "röb*rt"))

        val specification = SpecificationsBuilder<Users>().withSearch("userFirstName:rob%2A*").build()
        Assert.assertEquals(2, userRepository.findAll(specification).count())
    }

    @Test
    fun canGetUsersWithPartialName3() {
        userRepository.save(Users(userFirstName = "Robert"))
        userRepository.save(Users(userFirstName = "Roberta"))
        userRepository.save(Users(userFirstName = "Loberta"))
        userRepository.save(Users(userFirstName = "Toberta"))
        userRepository.save(Users(userFirstName = "Toborobe"))
        userRepository.save(Users(userFirstName = "oberta"))
        userRepository.save(Users(userFirstName = "Robot"))
        userRepository.save(Users(userFirstName = "Röbert"))

        val specification = SpecificationsBuilder<Users>().withSearch("userFirstName:*obe*").build()
        Assert.assertEquals(6, userRepository.findAll(specification).count())
    }

    @Test
    fun canGetUsersWithPartialName4() {
        userRepository.save(Users(userFirstName = "Rob*rt"))
        userRepository.save(Users(userFirstName = "rob*rta"))
        userRepository.save(Users(userFirstName = "Lob*rta"))
        userRepository.save(Users(userFirstName = "Tob*rta"))
        userRepository.save(Users(userFirstName = "robot"))
        userRepository.save(Users(userFirstName = "röb*rt"))

        val specification = SpecificationsBuilder<Users>().withSearch("userFirstName:*ob%2A*").build()
        Assert.assertEquals(4, userRepository.findAll(specification).count())
    }

    @Test
    fun canGetUsersNotContaining() {
        userRepository.save(Users(userFirstName = "Robèrt"))
        userRepository.save(Users(userFirstName = "robèrta"))
        userRepository.save(Users(userFirstName = "Lobérta"))
        userRepository.save(Users(userFirstName = "Toberta"))
        userRepository.save(Users(userFirstName = "robot"))
        userRepository.save(Users(userFirstName = "röbert"))

        val specification = SpecificationsBuilder<Users>().withSearch("userFirstName!*%C3%A8*").build()
        Assert.assertEquals(4, userRepository.findAll(specification).count())
    }

    @Test
    fun canGetUsersNotStratingWith() {
        userRepository.save(Users(userFirstName = "Alice"))
        userRepository.save(Users(userFirstName = "Bob"))
        userRepository.save(Users(userFirstName = "alice"))
        userRepository.save(Users(userFirstName = "bob"))

        val specification = SpecificationsBuilder<Users>().withSearch("userFirstName!B*").build()
        Assert.assertEquals(3, userRepository.findAll(specification).count())
    }

    @Test
    fun canGetUsersNotEndingWith() {
        userRepository.save(Users(userFirstName = "alice"))
        userRepository.save(Users(userFirstName = "bob"))
        userRepository.save(Users(userFirstName = "alicE"))
        userRepository.save(Users(userFirstName = "boB"))

        val specification = SpecificationsBuilder<Users>().withSearch("userFirstName!*e").build()
        Assert.assertEquals(3, userRepository.findAll(specification).count())
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
    fun canGetUserWithChildrenNotEquals() {
        userRepository.save(Users(userChildrenNumber = 5))
        userRepository.save(Users(userChildrenNumber = 1))
        userRepository.save(Users(userChildrenNumber = 6))
        userRepository.save(Users(userChildrenNumber = 2))
        userRepository.save(Users(userChildrenNumber = 5))

        val specification = SpecificationsBuilder<Users>().withSearch("userChildrenNumber!2").build()
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
    fun canGetUsersWithAge() {
        userRepository.save(Users(userAgeInSeconds = 23222223.3))
        userRepository.save(Users(userAgeInSeconds = 23222223.2))
        userRepository.save(Users(userAgeInSeconds = 23222223.0))

        val specification = SpecificationsBuilder<Users>().withSearch("userAgeInSeconds>23222223.2").build()
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

    @Test
    fun interlinkedTest1() {
        userRepository.save(Users(userSalary = 1501F, userLastName = "One"))
        userRepository.save(Users(userSalary = 1501F, userLastName = "One one"))
        userRepository.save(Users(userSalary = 1501F, userLastName = "Oneone"))
        userRepository.save(Users(userSalary = 1501F, userLastName = "Oe"))
        userRepository.save(Users(userSalary = 1501F, userLastName = "O n e"))
        userRepository.save(Users(userSalary = 1502F, userLastName = "One"))

        val specification = SpecificationsBuilder<Users>().withSearch("userSalary<1502 AND ( ( userLastName:One OR userLastName:one ) OR userLastName!*n* )").build()
        Assert.assertEquals(2, userRepository.findAll(specification).count())
    }
}
