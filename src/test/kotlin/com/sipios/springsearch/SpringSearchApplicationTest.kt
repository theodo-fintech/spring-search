package com.sipios.springsearch

import com.fasterxml.jackson.databind.util.StdDateFormat
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.transaction.annotation.Transactional

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes = [SpringSearchApplication::class])
@Transactional
class SpringSearchApplicationTest {
    @Autowired
    lateinit var userRepository: UsersRepository

    @Test
    fun run() {}

    @Test
    fun canAddUsers() {
        userRepository.save(Users())

        Assert.assertEquals(1, userRepository.findAll().count())
    }

    @Test
    fun canGetUserWithId() {
        val userId = userRepository.save(Users()).userId
        userRepository.save(Users())

        val specification = SpecificationsBuilder<Users>().withSearch("userId:$userId").build()
        Assert.assertEquals(userId, userRepository.findAll(specification).get(0).userId)
    }

    @Test
    fun canGetUserWithName() {
        val aliceId = userRepository.save(Users(userFirstName = "Alice")).userId
        userRepository.save(Users(userFirstName = "Bob"))

        val specification = SpecificationsBuilder<Users>().withSearch("userFirstName:Alice").build()
        Assert.assertEquals(aliceId, userRepository.findAll(specification).get(0).userId)
    }

    @Test
    fun canGetUserWithFirstNameAndLastName() {
        val aliceId = userRepository.save(Users(userFirstName = "Alice", userLastName = "One")).userId
        userRepository.save(Users(userFirstName = "Alice", userLastName = "Two"))
        userRepository.save(Users(userFirstName = "Bob", userLastName = "One"))
        userRepository.save(Users(userFirstName = "Bob", userLastName = "Two"))

        val specification = SpecificationsBuilder<Users>().withSearch("userFirstName:Alice AND userLastName:One").build()
        Assert.assertEquals(aliceId, userRepository.findAll(specification).get(0).userId)
    }

    @Test
    fun canGetUserWithFrenchName() {
        val edouardProstId = userRepository.save(Users(userFirstName = "Édouard", userLastName = "Pröst")).userId

        val specification = SpecificationsBuilder<Users>().withSearch("userFirstName:Édouard AND userLastName:Pröst").build()
        Assert.assertEquals(edouardProstId, userRepository.findAll(specification).get(0).userId)
    }

    @Test
    fun canGetUserWithChineseName() {
        val sunDemingId = userRepository.save(Users(userFirstName = "孫德明")).userId

        val specification = SpecificationsBuilder<Users>().withSearch("userFirstName:孫德明").build()
        Assert.assertEquals(sunDemingId, userRepository.findAll(specification).get(0).userId)
    }

    @Test
    fun canGetUserWithChineseNameNoEncoding() {
        val sunDemingId = userRepository.save(Users(userFirstName = "孫德明")).userId

        val specification = SpecificationsBuilder<Users>().withSearch("userFirstName:孫德明").build()
        Assert.assertEquals(sunDemingId, userRepository.findAll(specification).get(0).userId)
    }

    @Test
    fun canGetUserWithSpecialCharactersName() {
        val hackermanId = userRepository.save(Users(userFirstName = "&@#*\"''^^^\$``%=+§__hack3rman__")).userId

        val specification = SpecificationsBuilder<Users>().withSearch("userFirstName:&@#*\"''^^^\$``%=+§__hack3rman__").build()
        Assert.assertEquals(hackermanId, userRepository.findAll(specification).get(0).userId)
    }

    @Test
    fun canGetUserWithSpaceInNameWithString() {
        val robertJuniorId = userRepository.save(Users(userFirstName = "robert junior")).userId

        val specification = SpecificationsBuilder<Users>().withSearch("userFirstName:'robert junior'").build()
        Assert.assertEquals(robertJuniorId, userRepository.findAll(specification).get(0).userId)
    }

    @Test
    fun canGetUsersWithPartialName() {
        val robertId = userRepository.save(Users(userFirstName = "robert")).userId
        val robertaId = userRepository.save(Users(userFirstName = "roberta")).userId
        userRepository.save(Users(userFirstName = "robot"))
        userRepository.save(Users(userFirstName = "röbert"))

        val specification = SpecificationsBuilder<Users>().withSearch("userFirstName:robe*").build()
        val robeUsers = userRepository.findAll(specification)
        Assert.assertTrue(setOf(robertId, robertaId) == robeUsers.map { user -> user.userId }.toSet())
    }

    @Test
    fun canGetUsersWithPartialNameAndSpecialCharacter() {
        val robertId = userRepository.save(Users(userFirstName = "rob*rt")).userId
        val robertaId = userRepository.save(Users(userFirstName = "rob*rta")).userId
        userRepository.save(Users(userFirstName = "robot"))
        userRepository.save(Users(userFirstName = "röb*rt"))

        val specification = SpecificationsBuilder<Users>().withSearch("userFirstName:rob**").build()
        val robeUsers = userRepository.findAll(specification)
        Assert.assertTrue(setOf(robertId, robertaId) == robeUsers.map { user -> user.userId }.toSet())
    }

    @Test
    fun canGetUsersWithPartialNameContaining() {
        val robertId = userRepository.save(Users(userFirstName = "Robert")).userId
        val robertaId = userRepository.save(Users(userFirstName = "Roberta")).userId
        val toborobeId = userRepository.save(Users(userFirstName = "Toborobe")).userId
        val obertaId = userRepository.save(Users(userFirstName = "oberta")).userId
        userRepository.save(Users(userFirstName = "Robot"))
        userRepository.save(Users(userFirstName = "Röbert"))

        val specification = SpecificationsBuilder<Users>().withSearch("userFirstName:*obe*").build()
        val specificationUsers = userRepository.findAll(specification)
        Assert.assertTrue(setOf(robertId, robertaId, toborobeId, obertaId) == specificationUsers.map { user -> user.userId }.toSet())
    }

    @Test
    fun canGetUsersWithPartialNameContainingWithSpecialCharacter() {
        val robertId = userRepository.save(Users(userFirstName = "Rob*rt")).userId
        val robertaId = userRepository.save(Users(userFirstName = "rob*rta")).userId
        val lobertaId = userRepository.save(Users(userFirstName = "Lob*rta")).userId
        val tobertaId = userRepository.save(Users(userFirstName = "Tob*rta")).userId
        userRepository.save(Users(userFirstName = "robot"))
        userRepository.save(Users(userFirstName = "röb*rt"))

        val specification = SpecificationsBuilder<Users>().withSearch("userFirstName:*ob**").build()
        val specificationUsers = userRepository.findAll(specification)
        Assert.assertTrue(setOf(robertId, robertaId, lobertaId, tobertaId) == specificationUsers.map { user -> user.userId }.toSet())
    }

    @Test
    fun canGetUsersWithPartialNameContainingSpecialCharacterUsingSimpleString() {
        val robertId = userRepository.save(Users(userFirstName = "Rob*rt")).userId
        val robertaId = userRepository.save(Users(userFirstName = "rob*rta")).userId
        val lobertaId = userRepository.save(Users(userFirstName = "Lob*rta")).userId
        val tobertaId = userRepository.save(Users(userFirstName = "Tob*rta")).userId
        userRepository.save(Users(userFirstName = "robot"))
        userRepository.save(Users(userFirstName = "röb*rt"))

        val specification = SpecificationsBuilder<Users>().withSearch("userFirstName:'*ob**'").build()
        val specificationUsers = userRepository.findAll(specification)
        Assert.assertTrue(setOf(robertId, robertaId, lobertaId, tobertaId) == specificationUsers.map { user -> user.userId }.toSet())
    }

    @Test
    fun canGetUsersWithPartialNameContainingWithSpecialCharacterUsingDoubleString() {
        val robertId = userRepository.save(Users(userFirstName = "Rob*rt")).userId
        val robertaId = userRepository.save(Users(userFirstName = "rob*rta")).userId
        val lobertaId = userRepository.save(Users(userFirstName = "Lob*rta")).userId
        val tobertaId = userRepository.save(Users(userFirstName = "Tob*rta")).userId
        userRepository.save(Users(userFirstName = "robot"))
        userRepository.save(Users(userFirstName = "röb*rt"))

        val specification = SpecificationsBuilder<Users>().withSearch("userFirstName:\"*ob**\"").build()
        val specificationUsers = userRepository.findAll(specification)
        Assert.assertTrue(setOf(robertId, robertaId, lobertaId, tobertaId) == specificationUsers.map { user -> user.userId }.toSet())
    }

    @Test
    fun canGetUsersNotContaining() {
        val lobertaId = userRepository.save(Users(userFirstName = "Lobérta")).userId
        val tobertaId = userRepository.save(Users(userFirstName = "Toberta")).userId
        val robotId = userRepository.save(Users(userFirstName = "robot")).userId
        val roobertId = userRepository.save(Users(userFirstName = "röbert")).userId
        userRepository.save(Users(userFirstName = "Robèrt")).userId
        userRepository.save(Users(userFirstName = "robèrta")).userId

        val specification = SpecificationsBuilder<Users>().withSearch("userFirstName!*è*").build()
        val specificationUsers = userRepository.findAll(specification)
        Assert.assertTrue(setOf(lobertaId, tobertaId, robotId, roobertId) == specificationUsers.map { user -> user.userId }.toSet())
    }

    @Test
    fun canGetUsersNotStartingWith() {
        val aliceId = userRepository.save(Users(userFirstName = "Alice")).userId
        val aliceId2 = userRepository.save(Users(userFirstName = "alice")).userId
        val bobId = userRepository.save(Users(userFirstName = "bob")).userId
        userRepository.save(Users(userFirstName = "Bob"))

        val specification = SpecificationsBuilder<Users>().withSearch("userFirstName!B*").build()
        val specificationUsers = userRepository.findAll(specification)
        Assert.assertTrue(setOf(aliceId, aliceId2, bobId) == specificationUsers.map { user -> user.userId }.toSet())
    }

    @Test
    fun canGetUsersNotEndingWith() {
        val bobId = userRepository.save(Users(userFirstName = "bob")).userId
        val alicEId = userRepository.save(Users(userFirstName = "alicE")).userId
        val boBId = userRepository.save(Users(userFirstName = "boB")).userId
        userRepository.save(Users(userFirstName = "alice"))

        val specification = SpecificationsBuilder<Users>().withSearch("userFirstName!*e").build()
        val specificationUsers = userRepository.findAll(specification)
        Assert.assertTrue(setOf(boBId, alicEId, bobId) == specificationUsers.map { user -> user.userId }.toSet())
    }

    @Test
    fun canGetUserWithBigFamily() {
        val userWith5ChildrenId = userRepository.save(Users(userChildrenNumber = 5)).userId
        val userWith6ChildrenId = userRepository.save(Users(userChildrenNumber = 6)).userId
        val user2With5ChildrenId = userRepository.save(Users(userChildrenNumber = 5)).userId
        userRepository.save(Users(userChildrenNumber = 1))
        userRepository.save(Users(userChildrenNumber = 2))
        userRepository.save(Users(userChildrenNumber = 4))
        userRepository.save(Users(userChildrenNumber = 2))

        val specification = SpecificationsBuilder<Users>().withSearch("userChildrenNumber>4").build()
        val specificationUsers = userRepository.findAll(specification)
        Assert.assertTrue(setOf(user2With5ChildrenId, userWith5ChildrenId, userWith6ChildrenId) == specificationUsers.map { user -> user.userId }.toSet())
    }

    @Test
    fun canGetUserWithChildrenEquals() {
        val user1With4ChildrenId = userRepository.save(Users(userChildrenNumber = 4)).userId
        val user2With4ChildrenId = userRepository.save(Users(userChildrenNumber = 4)).userId
        val user3With4ChildrenId = userRepository.save(Users(userChildrenNumber = 4)).userId
        userRepository.save(Users(userChildrenNumber = 5))
        userRepository.save(Users(userChildrenNumber = 1))
        userRepository.save(Users(userChildrenNumber = 2))
        userRepository.save(Users(userChildrenNumber = 6))
        userRepository.save(Users(userChildrenNumber = 2))
        userRepository.save(Users(userChildrenNumber = 5))

        val specification = SpecificationsBuilder<Users>().withSearch("userChildrenNumber:4").build()
        val specificationUsers = userRepository.findAll(specification)
        Assert.assertTrue(setOf(user1With4ChildrenId, user2With4ChildrenId, user3With4ChildrenId) == specificationUsers.map { user -> user.userId }.toSet())
    }

    @Test
    fun canGetUserWithChildrenNotEquals() {
        val userWith5ChildrenId = userRepository.save(Users(userChildrenNumber = 5)).userId
        val userWith1ChildId = userRepository.save(Users(userChildrenNumber = 1)).userId
        val userWith6ChildrenId = userRepository.save(Users(userChildrenNumber = 6)).userId
        userRepository.save(Users(userChildrenNumber = 2))
        userRepository.save(Users(userChildrenNumber = 2))

        val specification = SpecificationsBuilder<Users>().withSearch("userChildrenNumber!2").build()
        val specificationUsers = userRepository.findAll(specification)
        Assert.assertTrue(setOf(userWith1ChildId, userWith5ChildrenId, userWith6ChildrenId) == specificationUsers.map { user -> user.userId }.toSet())
    }

    @Test
    fun canGetUserWithSmallerSalary() {
        val smallerSalaryUserId = userRepository.save(Users(userSalary = 2223.3F)).userId
        val smallerSalaryUser2Id = userRepository.save(Users(userSalary = 1500.2F)).userId
        userRepository.save(Users(userSalary = 4000.0F))
        userRepository.save(Users(userSalary = 2550.7F))
        userRepository.save(Users(userSalary = 2300.0F))

        val specification = SpecificationsBuilder<Users>().withSearch("userSalary<2300").build()
        val specificationUsers = userRepository.findAll(specification)
        Assert.assertTrue(setOf(smallerSalaryUserId, smallerSalaryUser2Id) == specificationUsers.map { user -> user.userId }.toSet())
    }

    @Test
    fun canGetUserWithHigherSalary() {
        val higherSalaryUserId = userRepository.save(Users(userSalary = 4000.1F)).userId
        val higherSalaryUser2Id = userRepository.save(Users(userSalary = 5350.7F)).userId
        userRepository.save(Users(userSalary = 2323.3F))
        userRepository.save(Users(userSalary = 1500.2F))

        val specification = SpecificationsBuilder<Users>().withSearch("userSalary>4000.001").build()
        val specificationUsers = userRepository.findAll(specification)
        Assert.assertTrue(setOf(higherSalaryUserId, higherSalaryUser2Id) == specificationUsers.map { user -> user.userId }.toSet())
    }

    @Test
    fun canGetUserWithMedianSalary() {
        val medianUserId = userRepository.save(Users(userSalary = 2323.3F)).userId
        userRepository.save(Users(userSalary = 1500.2F))
        userRepository.save(Users(userSalary = 4000.1F))
        userRepository.save(Users(userSalary = 5350.7F))

        val specification = SpecificationsBuilder<Users>().withSearch("userSalary<4000.1 AND userSalary>1500.2").build()
        val specificationUsers = userRepository.findAll(specification)
        Assert.assertTrue(setOf(medianUserId) == specificationUsers.map { user -> user.userId }.toSet())
    }

    @Test
    fun canGetUsersWithAge() {
        val olderUserId = userRepository.save(Users(userAgeInSeconds = 23222223.3)).userId
        userRepository.save(Users(userAgeInSeconds = 23222223.2))
        userRepository.save(Users(userAgeInSeconds = 23222223.0))

        val specification = SpecificationsBuilder<Users>().withSearch("userAgeInSeconds>23222223.2").build()
        val specificationUsers = userRepository.findAll(specification)
        Assert.assertTrue(setOf(olderUserId) == specificationUsers.map { user -> user.userId }.toSet())
    }

    @Test
    fun canGetUserWithParentheses() {
        val userOneWithHigherSalaryId = userRepository.save(Users(userSalary = 1500.2F, userLastName = "One")).userId
        val userTwoWithHigherSalaryId = userRepository.save(Users(userSalary = 1500.2F, userLastName = "Two")).userId
        userRepository.save(Users(userSalary = 1500.1F, userLastName = "One"))
        userRepository.save(Users(userSalary = 1500.1F, userLastName = "Two"))
        userRepository.save(Users(userSalary = 1500.1F, userLastName = "Three"))
        userRepository.save(Users(userSalary = 1500.2F, userLastName = "Three"))

        val specification = SpecificationsBuilder<Users>().withSearch("userSalary>1500.1 AND ( userLastName:One OR userLastName:Two )").build()
        val specificationUsers = userRepository.findAll(specification)
        Assert.assertTrue(setOf(userOneWithHigherSalaryId, userTwoWithHigherSalaryId) == specificationUsers.map { user -> user.userId }.toSet())
    }

    @Test
    fun canGetUsersWithInterlinkedConditions() {
        val userOneWithSmallerSalaryId = userRepository.save(Users(userSalary = 1501F, userLastName = "One")).userId
        val userOeId = userRepository.save(Users(userSalary = 1501F, userLastName = "Oe")).userId
        userRepository.save(Users(userSalary = 1501F, userLastName = "One one"))
        userRepository.save(Users(userSalary = 1501F, userLastName = "Oneone"))
        userRepository.save(Users(userSalary = 1501F, userLastName = "O n e"))
        userRepository.save(Users(userSalary = 1502F, userLastName = "One"))

        val specification = SpecificationsBuilder<Users>().withSearch("userSalary<1502 AND ( ( userLastName:One OR userLastName:one ) OR userLastName!*n* )").build()
        val specificationUsers = userRepository.findAll(specification)
        Assert.assertTrue(setOf(userOneWithSmallerSalaryId, userOeId) == specificationUsers.map { user -> user.userId }.toSet())
    }

    @Test
    fun canGetUsersWithInterlinkedConditionsNoSpaces() {
        val userOneWithSmallerSalaryId = userRepository.save(Users(userSalary = 1501F, userLastName = "One")).userId
        val userOeId = userRepository.save(Users(userSalary = 1501F, userLastName = "Oe")).userId
        userRepository.save(Users(userSalary = 1501F, userLastName = "One one"))
        userRepository.save(Users(userSalary = 1501F, userLastName = "Oneone"))
        userRepository.save(Users(userSalary = 1501F, userLastName = "O n e"))
        userRepository.save(Users(userSalary = 1502F, userLastName = "One"))

        val specification = SpecificationsBuilder<Users>().withSearch("userSalary<1502 AND ((userLastName:One OR userLastName:one) OR userLastName!*n*)").build()
        val specificationUsers = userRepository.findAll(specification)
        Assert.assertTrue(setOf(userOneWithSmallerSalaryId, userOeId) == specificationUsers.map { user -> user.userId }.toSet())
    }

    @Test
    fun canGetUsersByBoolean() {
        userRepository.save(Users(isAdmin = true))
        userRepository.save(Users(isAdmin = false))

        val specification = SpecificationsBuilder<Users>().withSearch("isAdmin:true").build()
        val specificationUsers = userRepository.findAll(specification)
        Assert.assertEquals(1, specificationUsers.size)
    }

    @Test
    fun canGetUsersEarlierThanDate() {
        val sdf = StdDateFormat()
        userRepository.save(Users(createdAt = sdf.parse("2019-01-01")))
        userRepository.save(Users(createdAt = sdf.parse("2019-01-03")))

        val specification = SpecificationsBuilder<Users>().withSearch("createdAt<'2019-01-02'").build()
        val specificationUsers = userRepository.findAll(specification)
        Assert.assertEquals(1, specificationUsers.size)
    }

    @Test
    fun canGetUsersAfterDate() {
        val sdf = StdDateFormat()
        userRepository.save(Users(createdAt = sdf.parse("2019-01-01")))
        userRepository.save(Users(createdAt = sdf.parse("2019-01-03")))

        var specification = SpecificationsBuilder<Users>().withSearch("createdAt>'2019-01-02'").build()
        var specificationUsers = userRepository.findAll(specification)
        Assert.assertEquals(1, specificationUsers.size)

        specification = SpecificationsBuilder<Users>().withSearch("createdAt>'2019-01-04'").build()
        specificationUsers = userRepository.findAll(specification)
        Assert.assertEquals(0, specificationUsers.size)
    }

    @Test
    fun canGetUsersAtPreciseDate() {
        val sdf = StdDateFormat()
        val date = sdf.parse("2019-01-01")
        userRepository.save(Users(createdAt = date))

        val specification = SpecificationsBuilder<Users>().withSearch("createdAt:'${sdf.format(date)}'").build()
        val specificationUsers = userRepository.findAll(specification)
        Assert.assertEquals(1, specificationUsers.size)
    }
}
