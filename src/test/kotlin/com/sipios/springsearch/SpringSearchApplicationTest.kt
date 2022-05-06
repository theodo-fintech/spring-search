package com.sipios.springsearch

import com.fasterxml.jackson.databind.util.StdDateFormat
import com.sipios.springsearch.anotation.SearchSpec
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.UUID
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes = [SpringSearchApplication::class])
@Transactional
class SpringSearchApplicationTest {
    @Autowired
    lateinit var userRepository: UsersRepository

    @Test
    fun run() {
    }

    @Test
    fun canAddUsers() {
        userRepository.save(Users())

        Assertions.assertEquals(1, userRepository.findAll().count())
    }

    @Test
    fun canGetUserWithId() {
        val userId = userRepository.save(Users()).userId
        userRepository.save(Users())

        val specification = SpecificationsBuilder<Users>(
            SearchSpec::class.constructors.first().call("", true)
        ).withSearch("userId:$userId").build()
        Assertions.assertEquals(userId, userRepository.findAll(specification).get(0).userId)
    }

    @Test
    fun canGetUserWithName() {
        val aliceId = userRepository.save(Users(userFirstName = "Alice")).userId
        userRepository.save(Users(userFirstName = "Bob"))

        val specification = SpecificationsBuilder<Users>(
            SearchSpec::class.constructors.first().call("", true)
        ).withSearch("userFirstName:Alice").build()
        Assertions.assertEquals(aliceId, userRepository.findAll(specification).get(0).userId)
    }

    @Test
    fun canGetUserWithFirstNameAndLastName() {
        val aliceId = userRepository.save(Users(userFirstName = "Alice", userLastName = "One")).userId
        userRepository.save(Users(userFirstName = "Alice", userLastName = "Two"))
        userRepository.save(Users(userFirstName = "Bob", userLastName = "One"))
        userRepository.save(Users(userFirstName = "Bob", userLastName = "Two"))

        val specification = SpecificationsBuilder<Users>(
            SearchSpec::class.constructors.first().call("", true)
        ).withSearch("userFirstName:Alice AND userLastName:One").build()
        Assertions.assertEquals(aliceId, userRepository.findAll(specification).get(0).userId)
    }

    @Test
    fun canGetUserWithFrenchName() {
        val edouardProstId = userRepository.save(Users(userFirstName = "Édouard", userLastName = "Pröst")).userId

        val specification = SpecificationsBuilder<Users>(
            SearchSpec::class.constructors.first().call("", true)
        ).withSearch("userFirstName:Édouard AND userLastName:Pröst").build()
        Assertions.assertEquals(edouardProstId, userRepository.findAll(specification).get(0).userId)
    }

    @Test
    fun canGetUserWithChineseName() {
        val sunDemingId = userRepository.save(Users(userFirstName = "孫德明")).userId

        val specification = SpecificationsBuilder<Users>(
            SearchSpec::class.constructors.first().call("", true)
        ).withSearch("userFirstName:孫德明").build()
        Assertions.assertEquals(sunDemingId, userRepository.findAll(specification).get(0).userId)
    }

    @Test
    fun canGetUserWithChineseNameNoEncoding() {
        val sunDemingId = userRepository.save(Users(userFirstName = "孫德明")).userId

        val specification = SpecificationsBuilder<Users>(
            SearchSpec::class.constructors.first().call("", true)
        ).withSearch("userFirstName:孫德明").build()
        Assertions.assertEquals(sunDemingId, userRepository.findAll(specification).get(0).userId)
    }

    @Test
    fun canGetUserWithSpecialCharactersName() {
        val hackermanId = userRepository.save(Users(userFirstName = "&@#*\"''^^^\$``%=+§__hack3rman__")).userId

        val specification = SpecificationsBuilder<Users>(
            SearchSpec::class.constructors.first().call("", true)
        ).withSearch("userFirstName:&@#*\"''^^^\$``%=+§__hack3rman__").build()
        Assertions.assertEquals(hackermanId, userRepository.findAll(specification).get(0).userId)
    }

    @Test
    fun canGetUserWithSpaceInNameWithString() {
        val robertJuniorId = userRepository.save(Users(userFirstName = "robert junior")).userId

        val specification = SpecificationsBuilder<Users>(
            SearchSpec::class.constructors.first().call("", true)
        ).withSearch("userFirstName:'robert junior'").build()
        Assertions.assertEquals(robertJuniorId, userRepository.findAll(specification).get(0).userId)
    }

    @Test
    fun canGetUsersWithPartialStartingName() {
        val robertId = userRepository.save(Users(userFirstName = "robert")).userId
        val robertaId = userRepository.save(Users(userFirstName = "roberta")).userId
        userRepository.save(Users(userFirstName = "robot"))
        userRepository.save(Users(userFirstName = "röbert"))

        val specification = SpecificationsBuilder<Users>(
            SearchSpec::class.constructors.first().call("", true)
        ).withSearch("userFirstName:robe*").build()
        val robeUsers = userRepository.findAll(specification)
        Assertions.assertTrue(setOf(robertId, robertaId) == robeUsers.map { user -> user.userId }.toSet())
    }

    @Test
    fun canGetUsersWithPartialEndingName() {
        val robertId = userRepository.save(Users(userFirstName = "robert")).userId
        val robertaId = userRepository.save(Users(userFirstName = "roubert")).userId
        userRepository.save(Users(userFirstName = "robot"))
        userRepository.save(Users(userFirstName = "röbęrt"))

        val specification = SpecificationsBuilder<Users>(
            SearchSpec::class.constructors.first().call("", true)
        ).withSearch("userFirstName:*ert").build()
        val robeUsers = userRepository.findAll(specification)
        Assertions.assertTrue(setOf(robertId, robertaId) == robeUsers.map { user -> user.userId }.toSet())
    }

    @Test
    fun canGetUsersWithPartialNameAndSpecialCharacter() {
        val robertId = userRepository.save(Users(userFirstName = "rob*rt")).userId
        val robertaId = userRepository.save(Users(userFirstName = "rob*rta")).userId
        userRepository.save(Users(userFirstName = "robot"))
        userRepository.save(Users(userFirstName = "röb*rt"))

        val specification = SpecificationsBuilder<Users>(
            SearchSpec::class.constructors.first().call("", true)
        ).withSearch("userFirstName:rob**").build()
        val robeUsers = userRepository.findAll(specification)
        Assertions.assertTrue(setOf(robertId, robertaId) == robeUsers.map { user -> user.userId }.toSet())
    }

    @Test
    fun canGetUsersWithPartialNameContaining() {
        val robertId = userRepository.save(Users(userFirstName = "Robert")).userId
        val robertaId = userRepository.save(Users(userFirstName = "Roberta")).userId
        val toborobeId = userRepository.save(Users(userFirstName = "Toborobe")).userId
        val obertaId = userRepository.save(Users(userFirstName = "oberta")).userId
        userRepository.save(Users(userFirstName = "Robot"))
        userRepository.save(Users(userFirstName = "Röbert"))

        val specification = SpecificationsBuilder<Users>(
            SearchSpec::class.constructors.first().call("", true)
        ).withSearch("userFirstName:*obe*").build()
        val specificationUsers = userRepository.findAll(specification)
        Assertions.assertTrue(
            setOf(
                robertId,
                robertaId,
                toborobeId,
                obertaId
            ) == specificationUsers.map { user -> user.userId }.toSet()
        )
    }

    @Test
    fun canGetUsersWithPartialNameContainingWithSpecialCharacter() {
        val robertId = userRepository.save(Users(userFirstName = "Rob*rt")).userId
        val robertaId = userRepository.save(Users(userFirstName = "rob*rta")).userId
        val lobertaId = userRepository.save(Users(userFirstName = "Lob*rta")).userId
        val tobertaId = userRepository.save(Users(userFirstName = "Tob*rta")).userId
        userRepository.save(Users(userFirstName = "robot"))
        userRepository.save(Users(userFirstName = "röb*rt"))

        val specification = SpecificationsBuilder<Users>(
            SearchSpec::class.constructors.first().call("", true)
        ).withSearch("userFirstName:*ob**").build()
        val specificationUsers = userRepository.findAll(specification)
        Assertions.assertTrue(
            setOf(
                robertId,
                robertaId,
                lobertaId,
                tobertaId
            ) == specificationUsers.map { user -> user.userId }.toSet()
        )
    }

    @Test
    fun canGetUsersWithPartialNameContainingSpecialCharacterUsingSimpleString() {
        val robertId = userRepository.save(Users(userFirstName = "Rob*rt")).userId
        val robertaId = userRepository.save(Users(userFirstName = "rob*rta")).userId
        val lobertaId = userRepository.save(Users(userFirstName = "Lob*rta")).userId
        val tobertaId = userRepository.save(Users(userFirstName = "Tob*rta")).userId
        userRepository.save(Users(userFirstName = "robot"))
        userRepository.save(Users(userFirstName = "röb*rt"))

        val specification = SpecificationsBuilder<Users>(
            SearchSpec::class.constructors.first().call("", true)
        ).withSearch("userFirstName:'*ob**'").build()
        val specificationUsers = userRepository.findAll(specification)
        Assertions.assertTrue(
            setOf(
                robertId,
                robertaId,
                lobertaId,
                tobertaId
            ) == specificationUsers.map { user -> user.userId }.toSet()
        )
    }

    @Test
    fun canGetUsersWithPartialNameContainingWithSpecialCharacterUsingDoubleString() {
        val robertId = userRepository.save(Users(userFirstName = "Rob*rt")).userId
        val robertaId = userRepository.save(Users(userFirstName = "rob*rta")).userId
        val lobertaId = userRepository.save(Users(userFirstName = "Lob*rta")).userId
        val tobertaId = userRepository.save(Users(userFirstName = "Tob*rta")).userId
        userRepository.save(Users(userFirstName = "robot"))
        userRepository.save(Users(userFirstName = "röb*rt"))

        val specification = SpecificationsBuilder<Users>(
            SearchSpec::class.constructors.first().call("", true)
        ).withSearch("userFirstName:\"*ob**\"").build()
        val specificationUsers = userRepository.findAll(specification)
        Assertions.assertTrue(
            setOf(
                robertId,
                robertaId,
                lobertaId,
                tobertaId
            ) == specificationUsers.map { user -> user.userId }.toSet()
        )
    }

    @Test
    fun canGetUsersNotContaining() {
        val lobertaId = userRepository.save(Users(userFirstName = "Lobérta")).userId
        val tobertaId = userRepository.save(Users(userFirstName = "Toberta")).userId
        val robotId = userRepository.save(Users(userFirstName = "robot")).userId
        val roobertId = userRepository.save(Users(userFirstName = "röbert")).userId
        userRepository.save(Users(userFirstName = "Robèrt")).userId
        userRepository.save(Users(userFirstName = "robèrta")).userId

        val specification = SpecificationsBuilder<Users>(
            SearchSpec::class.constructors.first().call("", true)
        ).withSearch("userFirstName!*è*").build()
        val specificationUsers = userRepository.findAll(specification)
        Assertions.assertTrue(
            setOf(
                lobertaId,
                tobertaId,
                robotId,
                roobertId
            ) == specificationUsers.map { user -> user.userId }.toSet()
        )
    }

    @Test
    fun canGetUsersNotStartingWith() {
        val aliceId = userRepository.save(Users(userFirstName = "Alice")).userId
        val aliceId2 = userRepository.save(Users(userFirstName = "alice")).userId
        val bobId = userRepository.save(Users(userFirstName = "bob")).userId
        userRepository.save(Users(userFirstName = "Bob"))

        val specification = SpecificationsBuilder<Users>(
            SearchSpec::class.constructors.first().call("", true)
        ).withSearch("userFirstName!B*").build()
        val specificationUsers = userRepository.findAll(specification)
        Assertions.assertTrue(setOf(aliceId, aliceId2, bobId) == specificationUsers.map { user -> user.userId }.toSet())
    }

    @Test
    fun canGetUsersNotEndingWith() {
        val bobId = userRepository.save(Users(userFirstName = "bob")).userId
        val alicEId = userRepository.save(Users(userFirstName = "alicE")).userId
        val boBId = userRepository.save(Users(userFirstName = "boB")).userId
        userRepository.save(Users(userFirstName = "alice"))

        val specification = SpecificationsBuilder<Users>(
            SearchSpec::class.constructors.first().call("", true)
        ).withSearch("userFirstName!*e").build()
        val specificationUsers = userRepository.findAll(specification)
        Assertions.assertTrue(setOf(boBId, alicEId, bobId) == specificationUsers.map { user -> user.userId }.toSet())
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

        val specification = SpecificationsBuilder<Users>(
            SearchSpec::class.constructors.first().call("", true)
        ).withSearch("userChildrenNumber>4").build()
        val specificationUsers = userRepository.findAll(specification)
        Assertions.assertTrue(
            setOf(
                user2With5ChildrenId,
                userWith5ChildrenId,
                userWith6ChildrenId
            ) == specificationUsers.map { user -> user.userId }.toSet()
        )
    }

    @Test
    fun canGetUserWithSmallFamily() {
        userRepository.save(Users(userChildrenNumber = 5))
        userRepository.save(Users(userChildrenNumber = 6))
        userRepository.save(Users(userChildrenNumber = 5))
        val userWith1ChildrenId = userRepository.save(Users(userChildrenNumber = 1)).userId
        val userWith2ChildrenId = userRepository.save(Users(userChildrenNumber = 2)).userId
        userRepository.save(Users(userChildrenNumber = 4))
        val user2With2ChildrenId = userRepository.save(Users(userChildrenNumber = 2)).userId

        val specification = SpecificationsBuilder<Users>(
            SearchSpec::class.constructors.first().call("", true)
        ).withSearch("userChildrenNumber<4").build()
        val specificationUsers = userRepository.findAll(specification)
        Assertions.assertTrue(
            setOf(
                user2With2ChildrenId,
                userWith1ChildrenId,
                userWith2ChildrenId
            ) == specificationUsers.map { user -> user.userId }.toSet()
        )
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

        val specification = SpecificationsBuilder<Users>(
            SearchSpec::class.constructors.first().call("", true)
        ).withSearch("userChildrenNumber:4").build()
        val specificationUsers = userRepository.findAll(specification)
        Assertions.assertTrue(
            setOf(
                user1With4ChildrenId,
                user2With4ChildrenId,
                user3With4ChildrenId
            ) == specificationUsers.map { user -> user.userId }.toSet()
        )
    }

    @Test
    fun canGetUserWithChildrenNotEquals() {
        val userWith5ChildrenId = userRepository.save(Users(userChildrenNumber = 5)).userId
        val userWith1ChildId = userRepository.save(Users(userChildrenNumber = 1)).userId
        val userWith6ChildrenId = userRepository.save(Users(userChildrenNumber = 6)).userId
        userRepository.save(Users(userChildrenNumber = 2))
        userRepository.save(Users(userChildrenNumber = 2))

        val specification = SpecificationsBuilder<Users>(
            SearchSpec::class.constructors.first().call("", true)
        ).withSearch("userChildrenNumber!2").build()
        val specificationUsers = userRepository.findAll(specification)
        Assertions.assertTrue(
            setOf(
                userWith1ChildId,
                userWith5ChildrenId,
                userWith6ChildrenId
            ) == specificationUsers.map { user -> user.userId }.toSet()
        )
    }

    @Test
    fun canGetUserWithSmallerSalary() {
        val smallerSalaryUserId = userRepository.save(Users(userSalary = 2223.3F)).userId
        val smallerSalaryUser2Id = userRepository.save(Users(userSalary = 1500.2F)).userId
        userRepository.save(Users(userSalary = 4000.0F))
        userRepository.save(Users(userSalary = 2550.7F))
        userRepository.save(Users(userSalary = 2300.0F))

        val specification = SpecificationsBuilder<Users>(
            SearchSpec::class.constructors.first().call("", true)
        ).withSearch("userSalary<2300").build()
        val specificationUsers = userRepository.findAll(specification)
        Assertions.assertTrue(
            setOf(
                smallerSalaryUserId,
                smallerSalaryUser2Id
            ) == specificationUsers.map { user -> user.userId }.toSet()
        )
    }

    @Test
    fun canGetUserWithHigherFloatSalary() {
        val higherSalaryUserId = userRepository.save(Users(userSalary = 4000.1F)).userId
        val higherSalaryUser2Id = userRepository.save(Users(userSalary = 5350.7F)).userId
        userRepository.save(Users(userSalary = 2323.3F))
        userRepository.save(Users(userSalary = 1500.2F))

        val specification = SpecificationsBuilder<Users>(
            SearchSpec::class.constructors.first().call("", true)
        ).withSearch("userSalary>4000.001").build()
        val specificationUsers = userRepository.findAll(specification)
        Assertions.assertTrue(
            setOf(
                higherSalaryUserId,
                higherSalaryUser2Id
            ) == specificationUsers.map { user -> user.userId }.toSet()
        )
    }

    @Test
    fun canGetUserWithMedianSalary() {
        val medianUserId = userRepository.save(Users(userSalary = 2323.3F)).userId
        userRepository.save(Users(userSalary = 1500.2F))
        userRepository.save(Users(userSalary = 4000.1F))
        userRepository.save(Users(userSalary = 5350.7F))

        val specification = SpecificationsBuilder<Users>(
            SearchSpec::class.constructors.first().call("", true)
        ).withSearch("userSalary<4000.1 AND userSalary>1500.2").build()
        val specificationUsers = userRepository.findAll(specification)
        Assertions.assertTrue(setOf(medianUserId) == specificationUsers.map { user -> user.userId }.toSet())
    }

    @Test
    fun canGetUsersWithAgeHigher() {
        val olderUserId = userRepository.save(Users(userAgeInSeconds = 23222223.3)).userId
        userRepository.save(Users(userAgeInSeconds = 23222223.2))
        userRepository.save(Users(userAgeInSeconds = 23222223.0))

        val specification = SpecificationsBuilder<Users>(
            SearchSpec::class.constructors.first().call("", true)
        ).withSearch("userAgeInSeconds>23222223.2").build()
        val specificationUsers = userRepository.findAll(specification)
        Assertions.assertTrue(setOf(olderUserId) == specificationUsers.map { user -> user.userId }.toSet())
    }

    @Test
    fun canGetUsersWithAgeLower() {
        val youngerUserId = userRepository.save(Users(userAgeInSeconds = 23222223.0)).userId
        userRepository.save(Users(userAgeInSeconds = 23222223.2))
        userRepository.save(Users(userAgeInSeconds = 23222223.3))

        val specification = SpecificationsBuilder<Users>(
            SearchSpec::class.constructors.first().call("", true)
        ).withSearch("userAgeInSeconds<23222223.2").build()
        val specificationUsers = userRepository.findAll(specification)
        Assertions.assertTrue(setOf(youngerUserId) == specificationUsers.map { user -> user.userId }.toSet())
    }

    @Test
    fun canGetUsersWithAgeEqual() {
        val middleUserId = userRepository.save(Users(userAgeInSeconds = 23222223.2)).userId
        userRepository.save(Users(userAgeInSeconds = 23222223.3))
        userRepository.save(Users(userAgeInSeconds = 23222223.0))

        val specification = SpecificationsBuilder<Users>(
            SearchSpec::class.constructors.first().call("", true)
        ).withSearch("userAgeInSeconds:23222223.2").build()
        val specificationUsers = userRepository.findAll(specification)
        Assertions.assertTrue(setOf(middleUserId) == specificationUsers.map { user -> user.userId }.toSet())
    }

    @Test
    fun canGetUserWithParentheses() {
        val userOneWithHigherSalaryId = userRepository.save(Users(userSalary = 1500.2F, userLastName = "One")).userId
        val userTwoWithHigherSalaryId = userRepository.save(Users(userSalary = 1500.2F, userLastName = "Two")).userId
        userRepository.save(Users(userSalary = 1500.1F, userLastName = "One"))
        userRepository.save(Users(userSalary = 1500.1F, userLastName = "Two"))
        userRepository.save(Users(userSalary = 1500.1F, userLastName = "Three"))
        userRepository.save(Users(userSalary = 1500.2F, userLastName = "Three"))

        val specification = SpecificationsBuilder<Users>(
            SearchSpec::class.constructors.first().call("", true)
        ).withSearch("userSalary>1500.1 AND ( userLastName:One OR userLastName:Two )").build()
        val specificationUsers = userRepository.findAll(specification)
        Assertions.assertTrue(
            setOf(
                userOneWithHigherSalaryId,
                userTwoWithHigherSalaryId
            ) == specificationUsers.map { user -> user.userId }.toSet()
        )
    }

    @Test
    fun canGetUsersWithInterlinkedConditions() {
        val userOneWithSmallerSalaryId = userRepository.save(Users(userSalary = 1501F, userLastName = "One")).userId
        val userOeId = userRepository.save(Users(userSalary = 1501F, userLastName = "Oe")).userId
        userRepository.save(Users(userSalary = 1501F, userLastName = "One one"))
        userRepository.save(Users(userSalary = 1501F, userLastName = "Oneone"))
        userRepository.save(Users(userSalary = 1501F, userLastName = "O n e"))
        userRepository.save(Users(userSalary = 1502F, userLastName = "One"))

        val specification = SpecificationsBuilder<Users>(
            SearchSpec::class.constructors.first().call("", true)
        ).withSearch("userSalary<1502 AND ( ( userLastName:One OR userLastName:one ) OR userLastName!*n* )").build()
        val specificationUsers = userRepository.findAll(specification)
        Assertions.assertTrue(
            setOf(
                userOneWithSmallerSalaryId,
                userOeId
            ) == specificationUsers.map { user -> user.userId }.toSet()
        )
    }

    @Test
    fun canGetUsersWithInterlinkedConditionsNoSpaces() {
        val userOneWithSmallerSalaryId = userRepository.save(Users(userSalary = 1501F, userLastName = "One")).userId
        val userOeId = userRepository.save(Users(userSalary = 1501F, userLastName = "Oe")).userId
        userRepository.save(Users(userSalary = 1501F, userLastName = "One one"))
        userRepository.save(Users(userSalary = 1501F, userLastName = "Oneone"))
        userRepository.save(Users(userSalary = 1501F, userLastName = "O n e"))
        userRepository.save(Users(userSalary = 1502F, userLastName = "One"))

        val specification = SpecificationsBuilder<Users>(
            SearchSpec::class.constructors.first().call("", true)
        ).withSearch("userSalary<1502 AND ((userLastName:One OR userLastName:one) OR userLastName!*n*)").build()
        val specificationUsers = userRepository.findAll(specification)
        Assertions.assertTrue(
            setOf(
                userOneWithSmallerSalaryId,
                userOeId
            ) == specificationUsers.map { user -> user.userId }.toSet()
        )
    }

    @Test
    fun canGetUsersByBoolean() {
        userRepository.save(Users(isAdmin = true))
        userRepository.save(Users(isAdmin = false))

        val specification = SpecificationsBuilder<Users>(
            SearchSpec::class.constructors.first().call("", true)
        ).withSearch("isAdmin:true").build()
        val specificationUsers = userRepository.findAll(specification)
        Assertions.assertEquals(1, specificationUsers.size)
    }

    @Test
    fun canGetUsersEarlierThanDate() {
        val sdf = StdDateFormat()
        userRepository.save(Users(createdAt = sdf.parse("2019-01-01")))
        userRepository.save(Users(createdAt = sdf.parse("2019-01-03")))

        val specification = SpecificationsBuilder<Users>(
            SearchSpec::class.constructors.first().call("", true)
        ).withSearch("createdAt<'2019-01-02'").build()
        val specificationUsers = userRepository.findAll(specification)
        Assertions.assertEquals(1, specificationUsers.size)
    }

    @Test
    fun canGetUsersAfterDate() {
        val sdf = StdDateFormat()
        userRepository.save(Users(createdAt = sdf.parse("2019-01-01")))
        userRepository.save(Users(createdAt = sdf.parse("2019-01-03")))

        var specification = SpecificationsBuilder<Users>(
            SearchSpec::class.constructors.first().call("", true)
        ).withSearch("createdAt>'2019-01-02'").build()
        var specificationUsers = userRepository.findAll(specification)
        Assertions.assertEquals(1, specificationUsers.size)

        specification = SpecificationsBuilder<Users>(
            SearchSpec::class.constructors.first().call("", true)
        ).withSearch("createdAt>'2019-01-04'").build()
        specificationUsers = userRepository.findAll(specification)
        Assertions.assertEquals(0, specificationUsers.size)
    }

    @Test
    fun canGetUsersAtPreciseDate() {
        val sdf = StdDateFormat()
        val date = sdf.parse("2019-01-01")
        userRepository.save(Users(createdAt = date))

        val specification = SpecificationsBuilder<Users>(
            SearchSpec::class.constructors.first().call("", true)
        ).withSearch("createdAt:'${sdf.format(date)}'").build()
        val specificationUsers = userRepository.findAll(specification)
        Assertions.assertEquals(1, specificationUsers.size)
    }

    @Test
    fun canGetUsersAtPreciseDateNotEqual() {
        val sdf = StdDateFormat()
        val date = sdf.parse("2019-01-01")
        userRepository.save(Users(createdAt = date))

        val specification = SpecificationsBuilder<Users>(
            SearchSpec::class.constructors.first().call("", true)
        ).withSearch("createdAt!'2019-01-02'").build()
        val specificationUsers = userRepository.findAll(specification)
        Assertions.assertEquals(1, specificationUsers.size)
    }

    @Test
    fun canGetUsersWithCaseInsensitiveLowerCaseSearch() {
        val robertId = userRepository.save(Users(userFirstName = "ROBERT")).userId
        val robertaId = userRepository.save(Users(userFirstName = "roberta")).userId
        userRepository.save(Users(userFirstName = "robot"))
        userRepository.save(Users(userFirstName = "röbęrt"))

        val specification = SpecificationsBuilder<Users>(
            SearchSpec::class.constructors.first().call("", false)
        ).withSearch("userFirstName:robe*").build()
        val robeUsers = userRepository.findAll(specification)
        Assertions.assertTrue(setOf(robertId, robertaId) == robeUsers.map { user -> user.userId }.toSet())
    }

    @Test
    fun canGetUsersWithCaseInsensitiveUpperCaseSearch() {
        val robertId = userRepository.save(Users(userFirstName = "ROBERT")).userId
        val roubertId = userRepository.save(Users(userFirstName = "roubert")).userId
        userRepository.save(Users(userFirstName = "robot"))
        userRepository.save(Users(userFirstName = "röbęrt"))

        val specification = SpecificationsBuilder<Users>(
            SearchSpec::class.constructors.first().call("", false)
        ).withSearch("userFirstName:*ert").build()
        val robeUsers = userRepository.findAll(specification)
        Assertions.assertTrue(setOf(robertId, roubertId) == robeUsers.map { user -> user.userId }.toSet())
    }

    @Test
    fun canGetUsersWithCaseInsensitiveContainsSearch() {
        val robertId = userRepository.save(Users(userFirstName = "ROBERT")).userId
        val roubertId = userRepository.save(Users(userFirstName = "roubert")).userId
        userRepository.save(Users(userFirstName = "robot"))
        userRepository.save(Users(userFirstName = "röbęrt"))

        val specification = SpecificationsBuilder<Users>(
            SearchSpec::class.constructors.first().call("", false)
        ).withSearch("userFirstName:*er*").build()
        val robeUsers = userRepository.findAll(specification)
        Assertions.assertTrue(setOf(robertId, roubertId) == robeUsers.map { user -> user.userId }.toSet())
    }

    @Test
    fun canGetUsersWithCaseInsensitiveDoesntContainSearch() {
        userRepository.save(Users(userFirstName = "ROBERT"))
        val roubertId = userRepository.save(Users(userFirstName = "roubert")).userId
        userRepository.save(Users(userFirstName = "robot"))

        val specification = SpecificationsBuilder<Users>(
            SearchSpec::class.constructors.first().call("", false)
        ).withSearch("userFirstName!*rob*").build()
        val robeUsers = userRepository.findAll(specification)
        Assertions.assertTrue(setOf(roubertId) == robeUsers.map { user -> user.userId }.toSet())
    }

    @Test
    fun canGetUsersWithCaseInsensitiveDoesntStartSearch() {
        userRepository.save(Users(userFirstName = "ROBERT"))
        val roubertId = userRepository.save(Users(userFirstName = "roubert")).userId
        userRepository.save(Users(userFirstName = "robot"))

        val specification = SpecificationsBuilder<Users>(
            SearchSpec::class.constructors.first().call("", false)
        ).withSearch("userFirstName!rob*").build()
        val robeUsers = userRepository.findAll(specification)
        Assertions.assertTrue(setOf(roubertId) == robeUsers.map { user -> user.userId }.toSet())
    }

    @Test
    fun canGetUsersWithCaseInsensitiveDoesntEndSearch() {
        userRepository.save(Users(userFirstName = "ROBERT"))
        userRepository.save(Users(userFirstName = "roubert"))
        val robotId = userRepository.save(Users(userFirstName = "robot")).userId

        val specification = SpecificationsBuilder<Users>(
            SearchSpec::class.constructors.first().call("", false)
        ).withSearch("userFirstName!*rt").build()
        val robotUsers = userRepository.findAll(specification)
        Assertions.assertTrue(setOf(robotId) == robotUsers.map { user -> user.userId }.toSet())
    }

    @Test
    fun canGetUsersWithUserTypeEqualSearch() {
        userRepository.save(Users(userFirstName = "Hamid", type = UserType.TEAM_MEMBER))
        userRepository.save(Users(userFirstName = "Reza", type = UserType.TEAM_MEMBER))
        userRepository.save(Users(userFirstName = "Ireh", type = UserType.TEAM_MEMBER))
        userRepository.save(Users(userFirstName = "robot", type = UserType.ADMINISTRATOR))

        val specification = SpecificationsBuilder<Users>(
            SearchSpec::class.constructors.first().call("", false)
        ).withSearch("type:ADMINISTRATOR").build()
        val robeUsers = userRepository.findAll(specification)
        Assertions.assertEquals(1, robeUsers.size)
        Assertions.assertEquals("robot", robeUsers[0].userFirstName)
    }

    @Test
    fun canGetUsersWithUserTypeNotEqualSearch() {
        userRepository.save(Users(userFirstName = "HamidReza", type = UserType.TEAM_MEMBER))
        userRepository.save(Users(userFirstName = "Ireh", type = UserType.ADMINISTRATOR))
        userRepository.save(Users(userFirstName = "robot", type = UserType.TEAM_MEMBER))

        val specification = SpecificationsBuilder<Users>(
            SearchSpec::class.constructors.first().call("", false)
        ).withSearch("type!TEAM_MEMBER").build()
        val irehUsers = userRepository.findAll(specification)
        Assertions.assertEquals(1, irehUsers.size)
        Assertions.assertEquals("Ireh", irehUsers[0].userFirstName)
    }

    @Test
    fun canGetUsersWithUpdatedAtGreaterSearch() {
        userRepository.save(Users(userFirstName = "HamidReza", updatedAt = LocalDateTime.parse("2020-01-10T10:15:30")))
        userRepository.save(Users(userFirstName = "robot", updatedAt = LocalDateTime.parse("2020-01-11T10:20:30")))

        val specification = SpecificationsBuilder<Users>(
            SearchSpec::class.constructors.first().call("", false)
        ).withSearch("updatedAt>'2020-01-11T09:20:30'").build()
        val robotUsers = userRepository.findAll(specification)
        Assertions.assertEquals(1, robotUsers.size)
        Assertions.assertEquals("robot", robotUsers[0].userFirstName)
    }

    @Test
    fun canGetUsersWithUpdateInstantAtGreaterSearch() {
        userRepository.save(
            Users(
                userFirstName = "HamidReza",
                updatedInstantAt = Instant.parse("2020-01-10T10:15:30Z")
            )
        )
        userRepository.save(Users(userFirstName = "robot", updatedInstantAt = Instant.parse("2020-01-11T10:20:30Z")))

        val specification = SpecificationsBuilder<Users>(
            SearchSpec::class.constructors.first().call("", false)
        ).withSearch("updatedInstantAt>'2020-01-11T09:20:30Z'").build()
        val robotUsers = userRepository.findAll(specification)
        Assertions.assertEquals(1, robotUsers.size)
        Assertions.assertEquals("robot", robotUsers[0].userFirstName)
    }

    @Test
    fun canGetUsersWithUpdatedAtLessSearch() {
        userRepository.save(Users(userFirstName = "HamidReza", updatedAt = LocalDateTime.parse("2020-01-10T10:15:30")))
        userRepository.save(Users(userFirstName = "robot", updatedAt = LocalDateTime.parse("2020-01-11T10:20:30")))

        val specification = SpecificationsBuilder<Users>(
            SearchSpec::class.constructors.first().call("", false)
        ).withSearch("updatedAt<'2020-01-11T09:20:30'").build()
        val hamidrezaUsers = userRepository.findAll(specification)
        Assertions.assertEquals(1, hamidrezaUsers.size)
        Assertions.assertEquals("HamidReza", hamidrezaUsers[0].userFirstName)
    }

    @Test
    fun canGetUsersWithUpdatedAtEqualSearch() {
        userRepository.save(Users(userFirstName = "HamidReza", updatedAt = LocalDateTime.parse("2020-01-10T10:15:30")))
        userRepository.save(Users(userFirstName = "robot", updatedAt = LocalDateTime.parse("2020-01-11T10:20:30")))

        val specification = SpecificationsBuilder<Users>(
            SearchSpec::class.constructors.first().call("", false)
        ).withSearch("updatedAt:'2020-01-10T10:15:30'").build()
        val hamidrezaUsers = userRepository.findAll(specification)
        Assertions.assertEquals(1, hamidrezaUsers.size)
        Assertions.assertEquals("HamidReza", hamidrezaUsers[0].userFirstName)
    }

    @Test
    fun canGetUsersWithUpdatedAtNotEqualSearch() {
        userRepository.save(Users(userFirstName = "HamidReza", updatedAt = LocalDateTime.parse("2020-01-10T10:15:30")))
        userRepository.save(Users(userFirstName = "robot", updatedAt = LocalDateTime.parse("2020-01-11T10:20:30")))

        val specification = SpecificationsBuilder<Users>(
            SearchSpec::class.constructors.first().call("", false)
        ).withSearch("updatedAt!'2020-01-11T10:20:30'").build()
        val hamidrezaUsers = userRepository.findAll(specification)
        Assertions.assertEquals(1, hamidrezaUsers.size)
        Assertions.assertEquals("HamidReza", hamidrezaUsers[0].userFirstName)
    }

    @Test
    fun canGetUsersWithUpdatedDateAtGreaterSearch() {
        userRepository.save(Users(userFirstName = "HamidReza", updatedDateAt = LocalDate.parse("2020-01-10")))
        userRepository.save(Users(userFirstName = "robot", updatedDateAt = LocalDate.parse("2020-01-11")))

        val specification = SpecificationsBuilder<Users>(
            SearchSpec::class.constructors.first().call("", false)
        ).withSearch("updatedDateAt>'2020-01-10'").build()
        val robotUsers = userRepository.findAll(specification)
        Assertions.assertEquals(1, robotUsers.size)
        Assertions.assertEquals("robot", robotUsers[0].userFirstName)
    }

    @Test
    fun canGetUsersWithUpdatedDateAtLessSearch() {
        userRepository.save(Users(userFirstName = "HamidReza", updatedDateAt = LocalDate.parse("2020-01-10")))
        userRepository.save(Users(userFirstName = "robot", updatedDateAt = LocalDate.parse("2020-01-11")))

        val specification = SpecificationsBuilder<Users>(
            SearchSpec::class.constructors.first().call("", false)
        ).withSearch("updatedDateAt<'2020-01-11'").build()
        val hamidrezaUsers = userRepository.findAll(specification)
        Assertions.assertEquals(1, hamidrezaUsers.size)
        Assertions.assertEquals("HamidReza", hamidrezaUsers[0].userFirstName)
    }

    @Test
    fun canGetUsersWithUpdatedDateAtEqualSearch() {
        userRepository.save(Users(userFirstName = "HamidReza", updatedDateAt = LocalDate.parse("2020-01-10")))
        userRepository.save(Users(userFirstName = "robot", updatedDateAt = LocalDate.parse("2020-01-11")))

        val specification = SpecificationsBuilder<Users>(
            SearchSpec::class.constructors.first().call("", false)
        ).withSearch("updatedDateAt:'2020-01-10'").build()
        val hamidrezaUsers = userRepository.findAll(specification)
        Assertions.assertEquals(1, hamidrezaUsers.size)
        Assertions.assertEquals("HamidReza", hamidrezaUsers[0].userFirstName)
    }

    @Test
    fun canGetUsersWithUpdatedDateAtNotEqualSearch() {
        userRepository.save(Users(userFirstName = "HamidReza", updatedDateAt = LocalDate.parse("2020-01-10")))
        userRepository.save(Users(userFirstName = "robot", updatedDateAt = LocalDate.parse("2020-01-11")))

        val specification = SpecificationsBuilder<Users>(
            SearchSpec::class.constructors.first().call("", false)
        ).withSearch("updatedDateAt!'2020-01-11'").build()
        val hamidrezaUsers = userRepository.findAll(specification)
        Assertions.assertEquals(1, hamidrezaUsers.size)
        Assertions.assertEquals("HamidReza", hamidrezaUsers[0].userFirstName)
    }

    @Test
    fun canGetUsersWithUpdatedTimeAtGreaterSearch() {
        userRepository.save(Users(userFirstName = "HamidReza", updatedTimeAt = LocalTime.parse("10:15:30")))
        userRepository.save(Users(userFirstName = "robot", updatedTimeAt = LocalTime.parse("10:20:30")))

        val specification = SpecificationsBuilder<Users>(
            SearchSpec::class.constructors.first().call("", false)
        ).withSearch("updatedTimeAt>'10:15:30'").build()
        val robotUsers = userRepository.findAll(specification)
        Assertions.assertEquals(1, robotUsers.size)
        Assertions.assertEquals("robot", robotUsers[0].userFirstName)
    }

    @Test
    fun canGetUsersWithUpdatedTimeAtLessSearch() {
        userRepository.save(Users(userFirstName = "HamidReza", updatedTimeAt = LocalTime.parse("10:15:30")))
        userRepository.save(Users(userFirstName = "robot", updatedTimeAt = LocalTime.parse("10:20:30")))

        val specification = SpecificationsBuilder<Users>(
            SearchSpec::class.constructors.first().call("", false)
        ).withSearch("updatedTimeAt<'10:16:30'").build()
        val hamidrezaUsers = userRepository.findAll(specification)
        Assertions.assertEquals(1, hamidrezaUsers.size)
        Assertions.assertEquals("HamidReza", hamidrezaUsers[0].userFirstName)
    }

    @Test
    fun canGetUsersWithUpdatedTimeAtEqualSearch() {
        userRepository.save(Users(userFirstName = "HamidReza", updatedTimeAt = LocalTime.parse("10:15:30")))
        userRepository.save(Users(userFirstName = "robot", updatedTimeAt = LocalTime.parse("10:20:30")))

        val specification = SpecificationsBuilder<Users>(
            SearchSpec::class.constructors.first().call("", false)
        ).withSearch("updatedTimeAt:'10:15:30'").build()
        val hamidrezaUsers = userRepository.findAll(specification)
        Assertions.assertEquals(1, hamidrezaUsers.size)
        Assertions.assertEquals("HamidReza", hamidrezaUsers[0].userFirstName)
    }

    @Test
    fun canGetUsersWithUpdatedTimeAtNotEqualSearch() {
        userRepository.save(Users(userFirstName = "HamidReza", updatedTimeAt = LocalTime.parse("10:15:30")))
        userRepository.save(Users(userFirstName = "robot", updatedTimeAt = LocalTime.parse("10:20:30")))

        val specification = SpecificationsBuilder<Users>(
            SearchSpec::class.constructors.first().call("", false)
        ).withSearch("updatedTimeAt!'10:20:30'").build()
        val hamidrezaUsers = userRepository.findAll(specification)
        Assertions.assertEquals(1, hamidrezaUsers.size)
        Assertions.assertEquals("HamidReza", hamidrezaUsers[0].userFirstName)
    }

    @Test
    fun canGetUsersWithDurationGreaterSearch() {
        userRepository.save(Users(userFirstName = "HamidReza", validityDuration = Duration.parse("PT10H")))
        userRepository.save(Users(userFirstName = "robot", validityDuration = Duration.parse("PT15H")))

        val specification = SpecificationsBuilder<Users>(
            SearchSpec::class.constructors.first().call("", false)
        ).withSearch("validityDuration>'PT10H'").build()
        val robotUsers = userRepository.findAll(specification)
        Assertions.assertEquals(1, robotUsers.size)
        Assertions.assertEquals("robot", robotUsers[0].userFirstName)
    }

    @Test
    fun canGetUsersWithDurationLessSearch() {
        userRepository.save(Users(userFirstName = "HamidReza", validityDuration = Duration.parse("PT10H")))
        userRepository.save(Users(userFirstName = "robot", validityDuration = Duration.parse("PT15H")))

        val specification = SpecificationsBuilder<Users>(
            SearchSpec::class.constructors.first().call("", false)
        ).withSearch("validityDuration<'PT11H'").build()
        val hamidrezaUsers = userRepository.findAll(specification)
        Assertions.assertEquals(1, hamidrezaUsers.size)
        Assertions.assertEquals("HamidReza", hamidrezaUsers[0].userFirstName)
    }

    @Test
    fun canGetUsersWithDurationEqualSearch() {
        userRepository.save(Users(userFirstName = "HamidReza", validityDuration = Duration.parse("PT10H")))
        userRepository.save(Users(userFirstName = "robot", validityDuration = Duration.parse("PT15H")))

        val specification = SpecificationsBuilder<Users>(
            SearchSpec::class.constructors.first().call("", false)
        ).withSearch("validityDuration:'PT10H'").build()
        val hamidrezaUsers = userRepository.findAll(specification)
        Assertions.assertEquals(1, hamidrezaUsers.size)
        Assertions.assertEquals("HamidReza", hamidrezaUsers[0].userFirstName)
    }

    @Test
    fun canGetUsersWithDurationNotEqualSearch() {
        userRepository.save(Users(userFirstName = "HamidReza", validityDuration = Duration.parse("PT10H")))
        userRepository.save(Users(userFirstName = "robot", validityDuration = Duration.parse("PT15H")))

        val specification = SpecificationsBuilder<Users>(
            SearchSpec::class.constructors.first().call("", false)
        ).withSearch("validityDuration!'PT10H'").build()
        val robotUsers = userRepository.findAll(specification)
        Assertions.assertEquals(1, robotUsers.size)
        Assertions.assertEquals("robot", robotUsers[0].userFirstName)
    }

    @Test
    fun canGetUsersWithUUIDEqualSearch() {
        val userUUID = UUID.randomUUID()
        userRepository.save(Users(userFirstName = "Diego", uuid = userUUID))
        val specification = SpecificationsBuilder<Users>(
            SearchSpec::class.constructors.first().call("", false)
        ).withSearch("uuid:'$userUUID'").build()
        val robotUsers = userRepository.findAll(specification)
        Assertions.assertEquals(1, robotUsers.size)
        Assertions.assertEquals(userUUID, robotUsers[0].uuid)
    }

    @Test
    fun canGetUsersWithUUIDNotEqualSearch() {
        val userUUID = UUID.randomUUID()
        val user2UUID = UUID.randomUUID()
        userRepository.save(Users(userFirstName = "Diego", uuid = userUUID))
        userRepository.save(Users(userFirstName = "Diego two", uuid = user2UUID))
        val specification = SpecificationsBuilder<Users>(
            SearchSpec::class.constructors.first().call("", false)
        ).withSearch("uuid!'$userUUID'").build()
        val robotUsers = userRepository.findAll(specification)
        Assertions.assertEquals(1, robotUsers.size)
        Assertions.assertEquals(user2UUID, robotUsers[0].uuid)
    }
}
