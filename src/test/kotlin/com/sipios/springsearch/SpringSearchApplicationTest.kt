package com.sipios.springsearch

import com.fasterxml.jackson.databind.util.StdDateFormat
import com.sipios.springsearch.anotation.SearchSpec
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.Date
import java.util.UUID
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes = [SpringSearchApplication::class])
@Transactional
class SpringSearchApplicationTest {
    @Autowired
    lateinit var userRepository: UsersRepository

    @Autowired
    lateinit var authorRepository: AuthorRepository

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
            SearchSpec::class.constructors.first().call("", true, emptyArray<String>())
        ).withSearch("userId:$userId").build()
        Assertions.assertEquals(userId, userRepository.findAll(specification).get(0).userId)
    }

    @Test
    fun canGetUserWithName() {
        val aliceId = userRepository.save(Users(userFirstName = "Alice")).userId
        userRepository.save(Users(userFirstName = "Bob"))

        val specification = SpecificationsBuilder<Users>(
            SearchSpec::class.constructors.first().call("", true, emptyArray<String>())
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
            SearchSpec::class.constructors.first().call("", true, emptyArray<String>())
        ).withSearch("userFirstName:Alice AND userLastName:One").build()
        Assertions.assertEquals(aliceId, userRepository.findAll(specification).get(0).userId)
    }

    @Test
    fun canGetUserWithFrenchName() {
        val edouardProstId = userRepository.save(Users(userFirstName = "Édouard", userLastName = "Pröst")).userId

        val specification = SpecificationsBuilder<Users>(
            SearchSpec::class.constructors.first().call("", true, emptyArray<String>())
        ).withSearch("userFirstName:Édouard AND userLastName:Pröst").build()
        Assertions.assertEquals(edouardProstId, userRepository.findAll(specification).get(0).userId)
    }

    @Test
    fun canGetUserWithChineseName() {
        val sunDemingId = userRepository.save(Users(userFirstName = "孫德明")).userId

        val specification = SpecificationsBuilder<Users>(
            SearchSpec::class.constructors.first().call("", true, emptyArray<String>())
        ).withSearch("userFirstName:孫德明").build()
        Assertions.assertEquals(sunDemingId, userRepository.findAll(specification).get(0).userId)
    }

    @Test
    fun canGetUserWithChineseNameNoEncoding() {
        val sunDemingId = userRepository.save(Users(userFirstName = "孫德明")).userId

        val specification = SpecificationsBuilder<Users>(
            SearchSpec::class.constructors.first().call("", true, emptyArray<String>())
        ).withSearch("userFirstName:孫德明").build()
        Assertions.assertEquals(sunDemingId, userRepository.findAll(specification).get(0).userId)
    }

    @Test
    fun canGetUserWithSpecialCharactersName() {
        val hackermanId = userRepository.save(Users(userFirstName = "&@#*\"''^^^\$``%=+§__hack3rman__")).userId

        val specification = SpecificationsBuilder<Users>(
            SearchSpec::class.constructors.first().call("", true, emptyArray<String>())
        ).withSearch("userFirstName:&@#*\"''^^^\$``%=+§__hack3rman__").build()
        Assertions.assertEquals(hackermanId, userRepository.findAll(specification).get(0).userId)
    }

    @Test
    fun canGetUserWithSpaceInNameWithString() {
        val robertJuniorId = userRepository.save(Users(userFirstName = "robert junior")).userId

        val specification = SpecificationsBuilder<Users>(
            SearchSpec::class.constructors.first().call("", true, emptyArray<String>())
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
            SearchSpec::class.constructors.first().call("", true, emptyArray<String>())
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
            SearchSpec::class.constructors.first().call("", true, emptyArray<String>())
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
            SearchSpec::class.constructors.first().call("", true, emptyArray<String>())
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
            SearchSpec::class.constructors.first().call("", true, emptyArray<String>())
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
            SearchSpec::class.constructors.first().call("", true, emptyArray<String>())
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
            SearchSpec::class.constructors.first().call("", true, emptyArray<String>())
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
            SearchSpec::class.constructors.first().call("", true, emptyArray<String>())
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
            SearchSpec::class.constructors.first().call("", true, emptyArray<String>())
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
            SearchSpec::class.constructors.first().call("", true, emptyArray<String>())
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
            SearchSpec::class.constructors.first().call("", true, emptyArray<String>())
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
            SearchSpec::class.constructors.first().call("", true, emptyArray<String>())
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
            SearchSpec::class.constructors.first().call("", true, emptyArray<String>())
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
            SearchSpec::class.constructors.first().call("", true, emptyArray<String>())
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
            SearchSpec::class.constructors.first().call("", true, emptyArray<String>())
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
    fun canGetUserWithHugeDebt() {
        val userWith1BDebtId = userRepository.save(Users(userDebt = 4000000000)).userId
        val userWith2BDebtId = userRepository.save(Users(userDebt = 5000000000)).userId
        val userWith3BDebtId = userRepository.save(Users(userDebt = 6000000000)).userId
        userRepository.save(Users(userDebt = 0))
        userRepository.save(Users(userDebt = 1000))
        userRepository.save(Users(userDebt = 20000))
        userRepository.save(Users(userDebt = 300000))

        val specification = SpecificationsBuilder<Users>(SearchSpec::class.constructors.first().call("", true)).withSearch("userDebt>1000000000").build()
        val specificationUsers = userRepository.findAll(specification)
        Assertions.assertTrue(setOf(userWith1BDebtId, userWith2BDebtId, userWith3BDebtId) == specificationUsers.map { user -> user.userId }.toSet())
    }

    @Test
    fun canGetUserWithSmallDebt() {
        userRepository.save(Users(userDebt = 4000000000))
        userRepository.save(Users(userDebt = 5000000000))
        userRepository.save(Users(userDebt = 6000000000))
        val userWith1KDebtId = userRepository.save(Users(userDebt = 1000)).userId
        val userWith2KDebtId = userRepository.save(Users(userDebt = 2000)).userId

        val specification = SpecificationsBuilder<Users>(SearchSpec::class.constructors.first().call("", true)).withSearch("userDebt>0 AND userDebt<1000000000").build()
        val specificationUsers = userRepository.findAll(specification)
        Assertions.assertTrue(setOf(userWith1KDebtId, userWith2KDebtId) == specificationUsers.map { user -> user.userId }.toSet())
    }

    @Test
    fun canGetUserWithNoDebt() {
        val user1WithNoDebtId = userRepository.save(Users(userDebt = 0)).userId
        val user2WithNoDebtId = userRepository.save(Users(userDebt = 0)).userId
        val user3WithNoDebtId = userRepository.save(Users(userDebt = 0)).userId
        userRepository.save(Users(userDebt = 1000))
        userRepository.save(Users(userDebt = 2000))
        userRepository.save(Users(userDebt = 3000))

        val specification = SpecificationsBuilder<Users>(SearchSpec::class.constructors.first().call("", true)).withSearch("userDebt:0").build()
        val specificationUsers = userRepository.findAll(specification)
        Assertions.assertTrue(setOf(user1WithNoDebtId, user2WithNoDebtId, user3WithNoDebtId) == specificationUsers.map { user -> user.userId }.toSet())
    }

    @Test
    fun canGetUserWithDebt() {
        val user1WithNoDebtId = userRepository.save(Users(userDebt = 1000)).userId
        val user2WithNoDebtId = userRepository.save(Users(userDebt = 2000)).userId
        userRepository.save(Users(userDebt = 0))
        userRepository.save(Users(userDebt = 0))

        val specification = SpecificationsBuilder<Users>(SearchSpec::class.constructors.first().call("", true)).withSearch("userDebt!0").build()
        val specificationUsers = userRepository.findAll(specification)
        Assertions.assertTrue(setOf(user1WithNoDebtId, user2WithNoDebtId) == specificationUsers.map { user -> user.userId }.toSet())
    }

    @Test
    fun canGetUserWithSmallerSalary() {
        val smallerSalaryUserId = userRepository.save(Users(userSalary = 2223.3F)).userId
        val smallerSalaryUser2Id = userRepository.save(Users(userSalary = 1500.2F)).userId
        userRepository.save(Users(userSalary = 4000.0F))
        userRepository.save(Users(userSalary = 2550.7F))
        userRepository.save(Users(userSalary = 2300.0F))

        val specification = SpecificationsBuilder<Users>(
            SearchSpec::class.constructors.first().call("", true, emptyArray<String>())
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
            SearchSpec::class.constructors.first().call("", true, emptyArray<String>())
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
            SearchSpec::class.constructors.first().call("", true, emptyArray<String>())
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
            SearchSpec::class.constructors.first().call("", true, emptyArray<String>())
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
            SearchSpec::class.constructors.first().call("", true, emptyArray<String>())
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
            SearchSpec::class.constructors.first().call("", true, emptyArray<String>())
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
            SearchSpec::class.constructors.first().call("", true, emptyArray<String>())
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
            SearchSpec::class.constructors.first().call("", true, emptyArray<String>())
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
            SearchSpec::class.constructors.first().call("", true, emptyArray<String>())
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
            SearchSpec::class.constructors.first().call("", true, emptyArray<String>())
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
            SearchSpec::class.constructors.first().call("", true, emptyArray<String>())
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
            SearchSpec::class.constructors.first().call("", true, emptyArray<String>())
        ).withSearch("createdAt>'2019-01-02'").build()
        var specificationUsers = userRepository.findAll(specification)
        Assertions.assertEquals(1, specificationUsers.size)

        specification = SpecificationsBuilder<Users>(
            SearchSpec::class.constructors.first().call("", true, emptyArray<String>())
        ).withSearch("createdAt>'2019-01-04'").build()
        specificationUsers = userRepository.findAll(specification)
        Assertions.assertEquals(0, specificationUsers.size)
    }

    @Test
    fun canGetUsersAfterEqualDate() {
        val sdf = StdDateFormat()
        userRepository.save(Users(createdAt = sdf.parse("2019-01-01")))
        userRepository.save(Users(createdAt = sdf.parse("2019-01-03")))

        var specification = SpecificationsBuilder<Users>(
            SearchSpec::class.constructors.first().call("", true, emptyArray<String>())
        ).withSearch("createdAt>:'2019-01-01'").build()
        var specificationUsers = userRepository.findAll(specification)
        Assertions.assertEquals(2, specificationUsers.size)

        specification = SpecificationsBuilder<Users>(
            SearchSpec::class.constructors.first().call("", true, emptyArray<String>())
        ).withSearch("createdAt>:'2019-01-04'").build()
        specificationUsers = userRepository.findAll(specification)
        Assertions.assertEquals(0, specificationUsers.size)
    }

    @Test
    fun canGetUsersEarlierEqualDate() {
        val sdf = StdDateFormat()
        userRepository.save(Users(createdAt = sdf.parse("2019-01-01")))
        userRepository.save(Users(createdAt = sdf.parse("2019-01-03")))

        var specification = SpecificationsBuilder<Users>(
            SearchSpec::class.constructors.first().call("", true, emptyArray<String>())
        ).withSearch("createdAt<:'2019-01-01'").build()
        var specificationUsers = userRepository.findAll(specification)
        Assertions.assertEquals(1, specificationUsers.size)

        specification = SpecificationsBuilder<Users>(
            SearchSpec::class.constructors.first().call("", true, emptyArray<String>())
        ).withSearch("createdAt<:'2019-01-03'").build()
        specificationUsers = userRepository.findAll(specification)
        Assertions.assertEquals(2, specificationUsers.size)
    }

    @Test
    fun canGetUsersAtPreciseDate() {
        val sdf = StdDateFormat()
        val date = sdf.parse("2019-01-01")
        userRepository.save(Users(createdAt = date))

        val specification = SpecificationsBuilder<Users>(
            SearchSpec::class.constructors.first().call("", true, emptyArray<String>())
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
            SearchSpec::class.constructors.first().call("", true, emptyArray<String>())
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
            SearchSpec::class.constructors.first().call("", false, emptyArray<String>())
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
            SearchSpec::class.constructors.first().call("", false, emptyArray<String>())
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
            SearchSpec::class.constructors.first().call("", false, emptyArray<String>())
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
            SearchSpec::class.constructors.first().call("", false, emptyArray<String>())
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
            SearchSpec::class.constructors.first().call("", false, emptyArray<String>())
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
            SearchSpec::class.constructors.first().call("", false, emptyArray<String>())
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
            SearchSpec::class.constructors.first().call("", true, emptyArray<String>())
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
            SearchSpec::class.constructors.first().call("", true, emptyArray<String>())
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
            SearchSpec::class.constructors.first().call("", true, emptyArray<String>())
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
            SearchSpec::class.constructors.first().call("", true, emptyArray<String>())
        ).withSearch("updatedInstantAt>'2020-01-11T09:20:30Z'").build()
        val robotUsers = userRepository.findAll(specification)
        Assertions.assertEquals(1, robotUsers.size)
        Assertions.assertEquals("robot", robotUsers[0].userFirstName)
    }

    @Test
    fun canGetUsersWithUpdateInstantAtGreaterThanEqualSearch() {
        userRepository.save(
            Users(
                userFirstName = "john",
                updatedInstantAt = Instant.parse("2020-01-10T10:15:30Z")
            )
        )
        userRepository.save(Users(userFirstName = "robot", updatedInstantAt = Instant.parse("2020-01-11T10:20:30Z")))

        val specification = SpecificationsBuilder<Users>(
            SearchSpec::class.constructors.first().call("", true, emptyArray<String>())
        ).withSearch("updatedInstantAt>:'2020-01-11T09:20:30Z'").build()
        val robotUsers = userRepository.findAll(specification)
        Assertions.assertEquals(1, robotUsers.size)
        Assertions.assertEquals("robot", robotUsers[0].userFirstName)
    }

    @Test
    fun canGetUsersWithUpdateInstantAtLessThanEqualSearch() {
        userRepository.save(
            Users(
                userFirstName = "john",
                updatedInstantAt = Instant.parse("2020-01-10T10:15:30Z")
            )
        )
        userRepository.save(Users(userFirstName = "robot", updatedInstantAt = Instant.parse("2020-01-11T10:20:30Z")))

        val specification = SpecificationsBuilder<Users>(
            SearchSpec::class.constructors.first().call("", true, emptyArray<String>())
        ).withSearch("updatedInstantAt<:'2020-01-11T09:20:30Z'").build()
        val robotUsers = userRepository.findAll(specification)
        Assertions.assertEquals(1, robotUsers.size)
        Assertions.assertEquals("john", robotUsers[0].userFirstName)
    }

    @Test
    fun canGetUsersWithUpdatedAtLessSearch() {
        userRepository.save(Users(userFirstName = "HamidReza", updatedAt = LocalDateTime.parse("2020-01-10T10:15:30")))
        userRepository.save(Users(userFirstName = "robot", updatedAt = LocalDateTime.parse("2020-01-11T10:20:30")))

        val specification = SpecificationsBuilder<Users>(
            SearchSpec::class.constructors.first().call("", true, emptyArray<String>())
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
            SearchSpec::class.constructors.first().call("", true, emptyArray<String>())
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
            SearchSpec::class.constructors.first().call("", true, emptyArray<String>())
        ).withSearch("updatedAt!'2020-01-11T10:20:30'").build()
        val hamidrezaUsers = userRepository.findAll(specification)
        Assertions.assertEquals(1, hamidrezaUsers.size)
        Assertions.assertEquals("HamidReza", hamidrezaUsers[0].userFirstName)
    }

    @Test
    fun canGetUsersWithUpdatedAtGreaterThanEqualSearch() {
        userRepository.save(Users(userFirstName = "john", updatedAt = LocalDateTime.parse("2020-01-10T10:15:30")))
        userRepository.save(Users(userFirstName = "robot", updatedAt = LocalDateTime.parse("2020-01-11T10:20:30")))
        userRepository.save(Users(userFirstName = "robot2", updatedAt = LocalDateTime.parse("2020-01-12T10:20:30")))
        val specification = SpecificationsBuilder<Users>(
            SearchSpec::class.constructors.first().call("", true, emptyArray<String>())
        ).withSearch("updatedAt>:'2020-01-11T10:20:30'").build()
        val users = userRepository.findAll(specification)
        Assertions.assertEquals(2, users.size)
        Assertions.assertFalse(users.any { user -> user.userFirstName == "john" })
    }

    @Test
    fun canGetUsersWithUpdatedAtLessThanEqualSearch() {
        userRepository.save(Users(userFirstName = "john", updatedAt = LocalDateTime.parse("2020-01-10T10:15:30")))
        userRepository.save(Users(userFirstName = "robot", updatedAt = LocalDateTime.parse("2020-01-11T10:20:30")))
        userRepository.save(Users(userFirstName = "robot2", updatedAt = LocalDateTime.parse("2020-01-12T10:20:30")))
        val specification = SpecificationsBuilder<Users>(
            SearchSpec::class.constructors.first().call("", true, emptyArray<String>())
        ).withSearch("updatedAt<:'2020-01-11T10:20:30'").build()
        val users = userRepository.findAll(specification)
        Assertions.assertEquals(2, users.size)
        Assertions.assertFalse(users.any { user -> user.userFirstName == "robot2" })
    }

    @Test
    fun canGetUsersWithUpdatedDateAtGreaterSearch() {
        userRepository.save(Users(userFirstName = "HamidReza", updatedDateAt = LocalDate.parse("2020-01-10")))
        userRepository.save(Users(userFirstName = "robot", updatedDateAt = LocalDate.parse("2020-01-11")))

        val specification = SpecificationsBuilder<Users>(
            SearchSpec::class.constructors.first().call("", true, emptyArray<String>())
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
            SearchSpec::class.constructors.first().call("", true, emptyArray<String>())
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
            SearchSpec::class.constructors.first().call("", true, emptyArray<String>())
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
            SearchSpec::class.constructors.first().call("", true, emptyArray<String>())
        ).withSearch("updatedDateAt!'2020-01-11'").build()
        val hamidrezaUsers = userRepository.findAll(specification)
        Assertions.assertEquals(1, hamidrezaUsers.size)
        Assertions.assertEquals("HamidReza", hamidrezaUsers[0].userFirstName)
    }

    @Test
    fun canGetUsersWithUpdatedDateAtLessThanEqualSearch() {
        userRepository.save(Users(userFirstName = "john", updatedDateAt = LocalDate.parse("2020-01-10")))
        userRepository.save(Users(userFirstName = "robot", updatedDateAt = LocalDate.parse("2020-01-11")))
        userRepository.save(Users(userFirstName = "robot2", updatedDateAt = LocalDate.parse("2020-01-12")))

        val specification = SpecificationsBuilder<Users>(
            SearchSpec::class.constructors.first().call("", true, emptyArray<String>())
        ).withSearch("updatedDateAt<:'2020-01-11'").build()
        val users = userRepository.findAll(specification)
        Assertions.assertEquals(2, users.size)
        Assertions.assertFalse(users.any { user -> user.userFirstName == "robot2" })
    }

    @Test
    fun canGetUsersWithUpdatedDateAtGreaterThanEqualSearch() {
        userRepository.save(Users(userFirstName = "john", updatedDateAt = LocalDate.parse("2020-01-10")))
        userRepository.save(Users(userFirstName = "robot", updatedDateAt = LocalDate.parse("2020-01-11")))
        userRepository.save(Users(userFirstName = "robot2", updatedDateAt = LocalDate.parse("2020-01-12")))

        val specification = SpecificationsBuilder<Users>(
            SearchSpec::class.constructors.first().call("", true, emptyArray<String>())
        ).withSearch("updatedDateAt>:'2020-01-11'").build()
        val users = userRepository.findAll(specification)
        Assertions.assertEquals(2, users.size)
        Assertions.assertFalse(users.any { user -> user.userFirstName == "john" })
    }

    @Test
    fun canGetUsersWithUpdatedTimeAtGreaterSearch() {
        userRepository.save(Users(userFirstName = "HamidReza", updatedTimeAt = LocalTime.parse("10:15:30")))
        userRepository.save(Users(userFirstName = "robot", updatedTimeAt = LocalTime.parse("10:20:30")))

        val specification = SpecificationsBuilder<Users>(
            SearchSpec::class.constructors.first().call("", true, emptyArray<String>())
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
            SearchSpec::class.constructors.first().call("", true, emptyArray<String>())
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
            SearchSpec::class.constructors.first().call("", true, emptyArray<String>())
        ).withSearch("updatedTimeAt:'10:15:30'").build()
        val hamidrezaUsers = userRepository.findAll(specification)
        Assertions.assertEquals(1, hamidrezaUsers.size)
        Assertions.assertEquals("HamidReza", hamidrezaUsers[0].userFirstName)
    }

    @Test
    fun canGetUsersWithUpdatedTimeAtLessThanEqualSearch() {
        userRepository.save(Users(userFirstName = "john", updatedTimeAt = LocalTime.parse("10:15:30")))
        userRepository.save(Users(userFirstName = "robot", updatedTimeAt = LocalTime.parse("10:20:30")))
        userRepository.save(Users(userFirstName = "robot2", updatedTimeAt = LocalTime.parse("10:25:30")))

        val specification = SpecificationsBuilder<Users>(
            SearchSpec::class.constructors.first().call("", true, emptyArray<String>())
        ).withSearch("updatedTimeAt<:'10:20:30'").build()
        val users = userRepository.findAll(specification)
        Assertions.assertEquals(2, users.size)
        Assertions.assertFalse(users.any { user -> user.userFirstName == "robot2" })
    }

    @Test
    fun canGetUsersWithUpdatedTimeAtGreaterThanEqualSearch() {
        userRepository.save(Users(userFirstName = "john", updatedTimeAt = LocalTime.parse("10:15:30")))
        userRepository.save(Users(userFirstName = "robot", updatedTimeAt = LocalTime.parse("10:20:30")))
        userRepository.save(Users(userFirstName = "robot2", updatedTimeAt = LocalTime.parse("10:25:30")))

        val specification = SpecificationsBuilder<Users>(
            SearchSpec::class.constructors.first().call("", true, emptyArray<String>())
        ).withSearch("updatedTimeAt>:'10:20:30'").build()
        val users = userRepository.findAll(specification)
        Assertions.assertEquals(2, users.size)
        Assertions.assertFalse(users.any { user -> user.userFirstName == "john" })
    }

    @Test
    fun canGetUsersWithUpdatedTimeAtNotEqualSearch() {
        userRepository.save(Users(userFirstName = "HamidReza", updatedTimeAt = LocalTime.parse("10:15:30")))
        userRepository.save(Users(userFirstName = "robot", updatedTimeAt = LocalTime.parse("10:20:30")))

        val specification = SpecificationsBuilder<Users>(
            SearchSpec::class.constructors.first().call("", true, emptyArray<String>())
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
            SearchSpec::class.constructors.first().call("", true, emptyArray<String>())
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
            SearchSpec::class.constructors.first().call("", true, emptyArray<String>())
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
            SearchSpec::class.constructors.first().call("", true, emptyArray<String>())
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
            SearchSpec::class.constructors.first().call("", true, emptyArray<String>())
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
            SearchSpec::class.constructors.first().call("", true, emptyArray<String>())
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
            SearchSpec::class.constructors.first().call("", true, emptyArray<String>())
        ).withSearch("uuid!'$userUUID'").build()
        val robotUsers = userRepository.findAll(specification)
        Assertions.assertEquals(1, robotUsers.size)
        Assertions.assertEquals(user2UUID, robotUsers[0].uuid)
    }

    @Test
    fun canGetUsersWithNumberOfChildrenLessOrEqualSearch() {
        userRepository.save(Users(userFirstName = "john", userChildrenNumber = 2))
        userRepository.save(Users(userFirstName = "jane", userChildrenNumber = 3))
        userRepository.save(Users(userFirstName = "joe", userChildrenNumber = 4))
        val specification = SpecificationsBuilder<Users>(
            SearchSpec::class.constructors.first().call("", true, emptyArray<String>())
        ).withSearch("userChildrenNumber<:2").build()
        val users = userRepository.findAll(specification)
        Assertions.assertEquals(1, users.size)
        Assertions.assertEquals("john", users[0].userFirstName)
    }

    @Test
    fun canGetUsersWithNumberOfChildrenGreaterOrEqualSearch() {
        userRepository.save(Users(userFirstName = "john", userChildrenNumber = 2))
        userRepository.save(Users(userFirstName = "jane", userChildrenNumber = 3))
        userRepository.save(Users(userFirstName = "joe", userChildrenNumber = 4))
        val specification = SpecificationsBuilder<Users>(
            SearchSpec::class.constructors.first().call("", true, emptyArray<String>())
        ).withSearch("userChildrenNumber>:3").build()
        val users = userRepository.findAll(specification)
        Assertions.assertEquals(2, users.size)
    }

    @Test
    fun canGetUsersWithNumberOfChildrenLessSearch() {
        userRepository.save(Users(userFirstName = "john", userChildrenNumber = 2))
        userRepository.save(Users(userFirstName = "jane", userChildrenNumber = 3))
        userRepository.save(Users(userFirstName = "joe", userChildrenNumber = 4))
        val specification = SpecificationsBuilder<Users>(
            SearchSpec::class.constructors.first().call("", true, emptyArray<String>())
        ).withSearch("userChildrenNumber<3").build()
        val users = userRepository.findAll(specification)
        Assertions.assertEquals(1, users.size)
        Assertions.assertEquals("john", users[0].userFirstName)
    }

    @Test
    fun canGetUserWithNameIn() {
        val johnId = userRepository.save(Users(userFirstName = "john", userChildrenNumber = 2)).userId
        val janeId = userRepository.save(Users(userFirstName = "jane", userChildrenNumber = 3)).userId
        userRepository.save(Users(userFirstName = "joe", userChildrenNumber = 4))
        val specification = SpecificationsBuilder<Users>(
            SearchSpec::class.constructors.first().call("", true, emptyArray<String>())
        ).withSearch("userFirstName IN [\"john\", \"jane\"]").build()
        val users = userRepository.findAll(specification)
        Assertions.assertTrue(setOf(johnId, janeId) == users.map { user -> user.userId }.toSet())
    }

    @Test
    fun canGetUserWithNameInEmptyList() {
        userRepository.save(Users(userFirstName = "john", userChildrenNumber = 2))
        userRepository.save(Users(userFirstName = "jane", userChildrenNumber = 3))
        userRepository.save(Users(userFirstName = "joe", userChildrenNumber = 4))
        val specification = SpecificationsBuilder<Users>(
            SearchSpec::class.constructors.first().call("", true, emptyArray<String>())
        ).withSearch("userFirstName IN []").build()
        val users = userRepository.findAll(specification)
        Assertions.assertTrue(users.isEmpty())
    }

    @Test
    fun canGetUserWithNameNotIn() {
        userRepository.save(Users(userFirstName = "john", userChildrenNumber = 2))
        userRepository.save(Users(userFirstName = "jane", userChildrenNumber = 3))
        val joeId = userRepository.save(Users(userFirstName = "joe", userChildrenNumber = 4)).userId
        val specification = SpecificationsBuilder<Users>(
            SearchSpec::class.constructors.first().call("", true, emptyArray<String>())
        ).withSearch("userFirstName NOT IN [\"john\", \"jane\"]").build()
        val users = userRepository.findAll(specification)
        Assertions.assertTrue(setOf(joeId) == users.map { user -> user.userId }.toSet())
    }

    @Test
    fun canGetUserWithChildrenNumberNotIn() {
        userRepository.save(Users(userFirstName = "john", userChildrenNumber = 2))
        userRepository.save(Users(userFirstName = "jane", userChildrenNumber = 3))
        val joeId = userRepository.save(Users(userFirstName = "joe", userChildrenNumber = 4)).userId
        val specification = SpecificationsBuilder<Users>(
            SearchSpec::class.constructors.first().call("", true, emptyArray<String>())
        ).withSearch("userChildrenNumber NOT IN [2, 3]").build()
        val users = userRepository.findAll(specification)
        Assertions.assertTrue(setOf(joeId) == users.map { user -> user.userId }.toSet())
    }

    @Test
    fun canGetUserWithChildrenNumberIn() {
        val johnId = userRepository.save(Users(userFirstName = "john", userChildrenNumber = 2)).userId
        val janeId = userRepository.save(Users(userFirstName = "jane", userChildrenNumber = 3)).userId
        userRepository.save(Users(userFirstName = "joe", userChildrenNumber = 4))
        val specification = SpecificationsBuilder<Users>(
            SearchSpec::class.constructors.first().call("", true, emptyArray<String>())
        ).withSearch("userChildrenNumber IN [2, 3]").build()
        val users = userRepository.findAll(specification)
        Assertions.assertTrue(setOf(janeId, johnId) == users.map { user -> user.userId }.toSet())
    }

    @Test
    fun canGetUserWithTypeIn() {
        val johnId = userRepository.save(Users(userFirstName = "john", type = UserType.TEAM_MEMBER)).userId
        val janeId = userRepository.save(Users(userFirstName = "jane", type = UserType.ADMINISTRATOR)).userId
        userRepository.save(Users(userFirstName = "joe", type = UserType.MANAGER))
        val specification = SpecificationsBuilder<Users>(
            SearchSpec::class.constructors.first().call("", true, emptyArray<String>())
        ).withSearch("type IN [ADMINISTRATOR, TEAM_MEMBER]").build()
        val users = userRepository.findAll(specification)
        Assertions.assertTrue(setOf(janeId, johnId) == users.map { user -> user.userId }.toSet())
    }

    @Test
    fun canGetUserWithIn() {
        val johnId =
            userRepository.save(Users(userFirstName = "john", updatedDateAt = LocalDate.parse("2020-01-10"))).userId
        val janeId =
            userRepository.save(Users(userFirstName = "jane", updatedDateAt = LocalDate.parse("2020-01-15"))).userId
        userRepository.save(Users(userFirstName = "joe", updatedDateAt = LocalDate.parse("2021-01-10")))
        val specification = SpecificationsBuilder<Users>(
            SearchSpec::class.constructors.first().call("", true, emptyArray<String>())
        ).withSearch("updatedDateAt IN ['2020-01-10', '2020-01-15']").build()
        val users = userRepository.findAll(specification)
        Assertions.assertTrue(setOf(janeId, johnId) == users.map { user -> user.userId }.toSet())
    }

    @Test
    fun canGetAuthorsWithEmptyBook() {
        val johnBook = Book()
        val john = Author()
        john.name = "john"
        john.addBook(johnBook)
        authorRepository.save(john)
        val janeBook = Book()
        val jane = Author()
        jane.name = "jane"
        jane.addBook(janeBook)
        authorRepository.save(jane)
        val specification = SpecificationsBuilder<Author>(
            SearchSpec::class.constructors.first().call("", true, emptyArray<String>())
        ).withSearch("books IS EMPTY").build()
        val users = authorRepository.findAll(specification)
        Assertions.assertTrue(users.isEmpty())
    }

    @Test
    fun cantSearchForEmptyWithNonFieldProperties() {
        val johnBook = Book()
        val john = Author()
        john.name = "john"
        john.addBook(johnBook)
        authorRepository.save(john)
        val janeBook = Book()
        val jane = Author()
        jane.name = "jane"
        jane.addBook(janeBook)
        authorRepository.save(jane)
        val specification = SpecificationsBuilder<Author>(
            SearchSpec::class.constructors.first().call("", true, emptyArray<String>())
        ).withSearch("name IS EMPTY").build()
        Assertions.assertThrows(
            ResponseStatusException::class.java
        ) { authorRepository.findAll(specification) }
        val specification2 = SpecificationsBuilder<Author>(
            SearchSpec::class.constructors.first().call("", true, emptyArray<String>())
        ).withSearch("name IS NOT EMPTY").build()
        Assertions.assertThrows(
            ResponseStatusException::class.java
        ) { authorRepository.findAll(specification2) }
    }
    @Test
    fun canGetAuthorsWithEmptyBookWithResult() {
        val johnBook = Book()
        val john = Author()
        john.name = "john"
        john.addBook(johnBook)
        authorRepository.save(john)
        val jane = Author()
        jane.name = "jane"
        authorRepository.save(jane)
        val specification = SpecificationsBuilder<Author>(
            SearchSpec::class.constructors.first().call("", true, emptyArray<String>())
        ).withSearch("books IS EMPTY").build()
        val users = authorRepository.findAll(specification)
        Assertions.assertTrue(users.size == 1)
        Assertions.assertTrue(users[0].name == jane.name)
    }

    @Test
    fun canGetAuthorsWithBooksNotEmpty() {
        val johnBook = Book()
        val john = Author()
        john.name = "john"
        john.addBook(johnBook)
        authorRepository.save(john)
        val jane = Author()
        jane.name = "jane"
        authorRepository.save(jane)
        val specification = SpecificationsBuilder<Author>(
            SearchSpec::class.constructors.first().call("", true, emptyArray<String>())
        ).withSearch("books IS NOT EMPTY").build()
        val users = authorRepository.findAll(specification)
        Assertions.assertTrue(users.size == 1)
        Assertions.assertTrue(users[0].name == john.name)
    }

    @Test
    fun canGetAuthorsWithBooksNotEmptyAllResult() {
        val johnBook = Book()
        val john = Author()
        john.name = "john"
        john.addBook(johnBook)
        authorRepository.save(john)
        val jane = Author()
        jane.name = "jane"
        val janeBook = Book()
        jane.addBook(janeBook)
        authorRepository.save(jane)
        val specification = SpecificationsBuilder<Author>(
            SearchSpec::class.constructors.first().call("", true, emptyArray<String>())
        ).withSearch("books IS NOT EMPTY").build()
        val users = authorRepository.findAll(specification)
        Assertions.assertTrue(users.size == 2)
    }

    @Test
    fun canGetAuthorsWithBooksNotEmptyNoResult() {
        val john = Author()
        john.name = "john"
        authorRepository.save(john)
        val jane = Author()
        jane.name = "jane"
        authorRepository.save(jane)
        val specification = SpecificationsBuilder<Author>(
            SearchSpec::class.constructors.first().call("", true, emptyArray<String>())
        ).withSearch("books IS NOT EMPTY").build()
        val users = authorRepository.findAll(specification)
        Assertions.assertTrue(users.size == 0)
    }
    @Test
    fun cantGetAuthorsWithBooksNull() {
        val john = Author()
        john.name = "john"
        authorRepository.save(john)
        val jane = Author()
        jane.name = "jane"
        authorRepository.save(jane)
        val spec = SpecificationsBuilder<Author>(
            SearchSpec::class.constructors.first().call("", true, emptyArray<String>())
        ).withSearch("books IS NULL").build()
        Assertions.assertThrows(
            ResponseStatusException::class.java
        ) { authorRepository.findAll(spec) }
        val specNotNull = SpecificationsBuilder<Author>(
            SearchSpec::class.constructors.first().call("", true, emptyArray<String>())
        ).withSearch("books IS NOT NULL").build()
        Assertions.assertThrows(
            ResponseStatusException::class.java
        ) { authorRepository.findAll(specNotNull) }
    }

    @Test
    fun canGetUsersWithNumberOfChildrenBetween() {
        userRepository.save(Users(userFirstName = "john", userChildrenNumber = 2))
        userRepository.save(Users(userFirstName = "jane", userChildrenNumber = 3))
        userRepository.save(Users(userFirstName = "joe", userChildrenNumber = 5))
        userRepository.save(Users(userFirstName = "jean", userChildrenNumber = 10))
        val specification = SpecificationsBuilder<Users>(
            SearchSpec::class.constructors.first().call("", true, emptyArray<String>())
        ).withSearch("userChildrenNumber BETWEEN 4 AND 10").build()
        val users = userRepository.findAll(specification)
        Assertions.assertEquals(2, users.size)
        val setNames = users.map { user -> user.userFirstName }.toSet()
        Assertions.assertEquals(setOf("joe", "jean"), setNames)
    }

    @Test
    fun canGetUsersWithUpdatedDateBetween() {
        userRepository.save(Users(userFirstName = "john", updatedDateAt = LocalDate.parse("2020-01-10")))
        userRepository.save(Users(userFirstName = "jane", updatedDateAt = LocalDate.parse("2020-01-11")))
        userRepository.save(Users(userFirstName = "joe", updatedDateAt = LocalDate.parse("2020-01-12")))
        userRepository.save(Users(userFirstName = "jean", updatedDateAt = LocalDate.parse("2020-01-13")))
        val specification = SpecificationsBuilder<Users>(
            SearchSpec::class.constructors.first().call("", true, emptyArray<String>())
        ).withSearch("updatedDateAt BETWEEN 2020-01-12 AND 2020-01-13").build()
        val users = userRepository.findAll(specification)
        Assertions.assertEquals(2, users.size)
        val setNames = users.map { user -> user.userFirstName }.toSet()
        Assertions.assertEquals(setOf("joe", "jean"), setNames)
    }

    @Test
    fun canGetUsersWithUpdatedDateNotBetween() {
        userRepository.save(Users(userFirstName = "john", updatedDateAt = LocalDate.parse("2020-01-10")))
        userRepository.save(Users(userFirstName = "jane", updatedDateAt = LocalDate.parse("2020-01-11")))
        userRepository.save(Users(userFirstName = "joe", updatedDateAt = LocalDate.parse("2020-01-12")))
        userRepository.save(Users(userFirstName = "jean", updatedDateAt = LocalDate.parse("2020-01-13")))
        val specification = SpecificationsBuilder<Users>(
            SearchSpec::class.constructors.first().call("", true, emptyArray<String>())
        ).withSearch("updatedDateAt NOT BETWEEN 2020-01-12 AND 2020-01-13").build()
        val users = userRepository.findAll(specification)
        Assertions.assertEquals(2, users.size)
        val setNames = users.map { user -> user.userFirstName }.toSet()
        Assertions.assertEquals(setOf("john", "jane"), setNames)
    }

    @Test
    fun canGetUsersWithUpdatedDateBetweenAndIdIn() {
        userRepository.save(Users(userFirstName = "john", updatedDateAt = LocalDate.parse("2020-01-10")))
        userRepository.save(Users(userFirstName = "jane", updatedDateAt = LocalDate.parse("2020-01-11")))
        val joeId = userRepository.save(Users(userFirstName = "joe", updatedDateAt = LocalDate.parse("2020-01-12"))).userId
        val jeanId = userRepository.save(Users(userFirstName = "jean", updatedDateAt = LocalDate.parse("2020-01-13"))).userId
        val specification = SpecificationsBuilder<Users>(
            SearchSpec::class.constructors.first().call("", true, emptyArray<String>())
        ).withSearch("updatedDateAt BETWEEN 2020-01-11 AND 2020-01-13 AND userId IN [$joeId, $jeanId]").build()
        val users = userRepository.findAll(specification)
        Assertions.assertEquals(2, users.size)
        val setNames = users.map { user -> user.userFirstName }.toSet()
        Assertions.assertEquals(setOf("joe", "jean"), setNames)
    }

    @Test
    fun canGetUsersWithUserFirstNameBetween() {
        userRepository.save(Users(userFirstName = "abel"))
        userRepository.save(Users(userFirstName = "bob"))
        userRepository.save(Users(userFirstName = "connor"))
        userRepository.save(Users(userFirstName = "david"))
        val specification = SpecificationsBuilder<Users>(
            SearchSpec::class.constructors.first().call("", true, emptyArray<String>())
        ).withSearch("userFirstName BETWEEN 'aaron' AND 'cyrano'").build()
        val users = userRepository.findAll(specification)
        Assertions.assertEquals(3, users.size)
        val setNames = users.map { user -> user.userFirstName }.toSet()
        Assertions.assertEquals(setOf("abel", "bob", "connor"), setNames)
    }
    @Test
    fun canGetUsersWithUserFirstNameNotBetween() {
        userRepository.save(Users(userFirstName = "abel"))
        userRepository.save(Users(userFirstName = "bob"))
        userRepository.save(Users(userFirstName = "connor"))
        userRepository.save(Users(userFirstName = "david"))
        val specification = SpecificationsBuilder<Users>(
            SearchSpec::class.constructors.first().call("", true, emptyArray<String>())
        ).withSearch("userFirstName NOT BETWEEN 'aaron' AND 'cyrano'").build()
        val users = userRepository.findAll(specification)
        Assertions.assertEquals(1, users.size)
        val setNames = users.map { user -> user.userFirstName }.toSet()
        Assertions.assertEquals(setOf("david"), setNames)
    }
    @Test
    fun canGetUsersWithUserFirstNameGt() {
        userRepository.save(Users(userFirstName = "abel"))
        userRepository.save(Users(userFirstName = "bob"))
        userRepository.save(Users(userFirstName = "connor"))
        userRepository.save(Users(userFirstName = "david"))
        val specification = SpecificationsBuilder<Users>(
            SearchSpec::class.constructors.first().call("", true, emptyArray<String>())
        ).withSearch("userFirstName > barry'").build()
        val users = userRepository.findAll(specification)
        Assertions.assertEquals(3, users.size)
        val setNames = users.map { user -> user.userFirstName }.toSet()
        Assertions.assertEquals(setOf("connor", "david", "bob"), setNames)
    }

    @Test
    fun canGetUsersWithUserFirstNameLt() {
        userRepository.save(Users(userFirstName = "abel"))
        userRepository.save(Users(userFirstName = "bob"))
        userRepository.save(Users(userFirstName = "connor"))
        userRepository.save(Users(userFirstName = "david"))
        val specification = SpecificationsBuilder<Users>(
            SearchSpec::class.constructors.first().call("", true, emptyArray<String>())
        ).withSearch("userFirstName < barry'").build()
        val users = userRepository.findAll(specification)
        Assertions.assertEquals(1, users.size)
        val setNames = users.map { user -> user.userFirstName }.toSet()
        Assertions.assertEquals(setOf("abel"), setNames)
    }

    @Test
    fun canGetUsersWithUserFirstNameCaseSensitive() {
        userRepository.save(Users(userFirstName = "abel"))
        userRepository.save(Users(userFirstName = "Aaron"))
        userRepository.save(Users(userFirstName = "connor"))
        userRepository.save(Users(userFirstName = "david"))
        // create spec with case sensitive flag
        val specification = SpecificationsBuilder<Users>(
            SearchSpec::class.constructors.first().call("", true, emptyArray<String>())
        ).withSearch("userFirstName : A*").build()
        val users = userRepository.findAll(specification)
        Assertions.assertEquals(1, users.size)
        Assertions.assertEquals("Aaron", users[0].userFirstName)
    }
    // test for a wrong search, should throw an exception during the parse
    @Test
    fun badRequestWithWrongSearch() {
        Assertions.assertThrows(ResponseStatusException::class.java) {
            SpecificationsBuilder<Users>(
                SearchSpec::class.constructors.first().call("", true, emptyArray<String>())
            ).withSearch("userFirstName : ").build()
        }
        Assertions.assertThrows(ResponseStatusException::class.java) {
            SpecificationsBuilder<Users>(
                SearchSpec::class.constructors.first().call("", true, emptyArray<String>())
            ).withSearch("updatedDateAt BETWEEN  AND 2020-01-11").build()
        }
        Assertions.assertThrows(ResponseStatusException::class.java) {
            SpecificationsBuilder<Users>(
                SearchSpec::class.constructors.first().call("", true, emptyArray<String>())
            ).withSearch("updatedDateAt BETWEEN 2020-01-11 AND").build()
        }
        Assertions.assertThrows(ResponseStatusException::class.java) {
            SpecificationsBuilder<Author>(
                SearchSpec::class.constructors.first().call("", true, emptyArray<String>())
            ).withSearch("books IS EMPT ").build()
        }
        Assertions.assertThrows(ResponseStatusException::class.java) {
            SpecificationsBuilder<Author>(
                SearchSpec::class.constructors.first().call("", true, emptyArray<String>())
            ).withSearch("books IS NOT EMPT ").build()
        }
        Assertions.assertThrows(ResponseStatusException::class.java) {
            SpecificationsBuilder<Users>(
                SearchSpec::class.constructors.first().call("", true, emptyArray<String>())
            ).withSearch("userId IN [").build()
        }
    }

    @Test
    fun canGetUsersWithNullColumn() {
        userRepository.save(Users(userFirstName = "john", type = null))
        userRepository.save(Users(userFirstName = "jane", type = UserType.ADMINISTRATOR))
        userRepository.save(Users(userFirstName = "joe", type = UserType.MANAGER))
        userRepository.save(Users(userFirstName = "jean", type = null))
        val specification = SpecificationsBuilder<Users>(
            SearchSpec::class.constructors.first().call("", true, emptyArray<String>())
        ).withSearch("type IS NULL").build()
        val users = userRepository.findAll(specification)
        Assertions.assertEquals(2, users.size)
        val setNames = users.map { user -> user.userFirstName }.toSet()
        Assertions.assertEquals(setOf("john", "jean"), setNames)
    }

    @Test
    fun canGetUsersWithNotNullColumn() {
        userRepository.save(Users(userFirstName = "john", type = null))
        userRepository.save(Users(userFirstName = "jane", type = UserType.ADMINISTRATOR))
        userRepository.save(Users(userFirstName = "joe", type = UserType.MANAGER))
        userRepository.save(Users(userFirstName = "jean", type = null))
        val specification = SpecificationsBuilder<Users>(
            SearchSpec::class.constructors.first().call("", true, emptyArray<String>())
        ).withSearch("type IS NOT NULL").build()
        val users = userRepository.findAll(specification)
        Assertions.assertEquals(2, users.size)
        val setNames = users.map { user -> user.userFirstName }.toSet()
        Assertions.assertEquals(setOf("jane", "joe"), setNames)
    }

    @Test
    fun canGetUsersWithNotNullFirstName() {
        userRepository.save(Users(userFirstName = "john", type = null))
        userRepository.save(Users(userFirstName = "jane", type = UserType.ADMINISTRATOR))
        userRepository.save(Users(userFirstName = "joe", type = UserType.MANAGER))
        userRepository.save(Users(userFirstName = "jean", type = null))
        val specification = SpecificationsBuilder<Users>(
            SearchSpec::class.constructors.first().call("", true, emptyArray<String>())
        ).withSearch("userFirstName IS NOT NULL").build()
        val users = userRepository.findAll(specification)
        Assertions.assertEquals(4, users.size)
        val setNames = users.map { user -> user.userFirstName }.toSet()
        Assertions.assertEquals(setOf("john", "jane", "joe", "jean"), setNames)
    }

    @Test
    fun canGetUserWithNullSalary() {
        userRepository.save(Users(userFirstName = "john", userSalary = 100.0F))
        userRepository.save(Users(userFirstName = "jane", userSalary = 1000.0F))
        val specification = SpecificationsBuilder<Users>(
            SearchSpec::class.constructors.first().call("", false, emptyArray<String>())
        ).withSearch("userSalary IS NULL").build()
        val users = userRepository.findAll(specification)
        Assertions.assertEquals(0, users.size)
    }

    @Test
    fun canNotSearchABlackListedField() {
        Assertions.assertThrows(ResponseStatusException::class.java) {
            SpecificationsBuilder<Users>(
                SearchSpec::class.constructors.first().call("", false, arrayOf("userFirstName"))
            ).withSearch("userFirstName : A* AND userId : 3").build()
        }
    }

    @Test
    fun canGetUsersWithUUIDNull() {
        val userUUID = UUID.randomUUID()
        userRepository.save(Users(userFirstName = "Diego", uuid = userUUID))
        userRepository.save(Users(userFirstName = "Diego two", uuid = null))
        val specification = SpecificationsBuilder<Users>(
            SearchSpec::class.constructors.first().call("", false, emptyArray<String>())
        ).withSearch("uuid IS NULL").build()
        val robotUsers = userRepository.findAll(specification)
        Assertions.assertEquals(1, robotUsers.size)
        Assertions.assertEquals(null, robotUsers[0].uuid)
    }

    @Test
    fun canGetUsersWithUpdatedDateAtNull() {
        userRepository.save(Users(userFirstName = "john", updatedDateAt = LocalDate.parse("2020-01-10")))
        userRepository.save(Users(userFirstName = "jane", updatedDateAt = LocalDate.parse("2020-01-11")))
        userRepository.save(Users(userFirstName = "joe", updatedDateAt = null))
        userRepository.save(Users(userFirstName = "jean", updatedDateAt = null))
        val specification = SpecificationsBuilder<Users>(
            SearchSpec::class.constructors.first().call("", false, emptyArray<String>())
        ).withSearch("updatedDateAt IS NULL").build()
        val users = userRepository.findAll(specification)
        Assertions.assertEquals(2, users.size)
        val setNames = users.map { user -> user.userFirstName }.toSet()
        Assertions.assertEquals(setOf("joe", "jean"), setNames)
    }

    @Test
    fun canGetUsersWithUpdatedDateTimeAtNotNull() {
        userRepository.save(Users(userFirstName = "john", updatedAt = LocalDateTime.parse("2020-01-10T10:15:30")))
        userRepository.save(Users(userFirstName = "jane", updatedAt = LocalDateTime.parse("2020-01-11T10:15:30")))
        userRepository.save(Users(userFirstName = "joe", updatedAt = null))
        userRepository.save(Users(userFirstName = "jean", updatedAt = LocalDateTime.parse("2020-01-13T10:15:30")))
        val specification = SpecificationsBuilder<Users>(
            SearchSpec::class.constructors.first().call("", false, emptyArray<String>())
        ).withSearch("updatedAt IS NOT NULL").build()
        val users = userRepository.findAll(specification)
        Assertions.assertEquals(3, users.size)
        val setNames = users.map { user -> user.userFirstName }.toSet()
        Assertions.assertEquals(setOf("john", "jane", "jean"), setNames)
    }
    @Test
    fun canGetUsersWithActiveNull() {
        userRepository.save(Users(userFirstName = "john", active = true))
        userRepository.save(Users(userFirstName = "jane", active = false))
        userRepository.save(Users(userFirstName = "joe", active = null))
        userRepository.save(Users(userFirstName = "jean", active = null))
        val specification = SpecificationsBuilder<Users>(
            SearchSpec::class.constructors.first().call("", false, emptyArray<String>())
        ).withSearch("active IS NULL").build()
        val users = userRepository.findAll(specification)
        Assertions.assertEquals(2, users.size)
        val setNames = users.map { user -> user.userFirstName }.toSet()
        Assertions.assertEquals(setOf("joe", "jean"), setNames)
    }
    @Test
    fun canGetUsersWithUserChildrenNumberNull() {
        userRepository.save(Users(userFirstName = "john", userChildrenNumber = 2))
        userRepository.save(Users(userFirstName = "jane", userChildrenNumber = 3))
        userRepository.save(Users(userFirstName = "joe", userChildrenNumber = null))
        userRepository.save(Users(userFirstName = "jean", userChildrenNumber = null))
        val specification = SpecificationsBuilder<Users>(
            SearchSpec::class.constructors.first().call("", false, emptyArray<String>())
        ).withSearch("userChildrenNumber IS NULL").build()
        val users = userRepository.findAll(specification)
        Assertions.assertEquals(2, users.size)
        val setNames = users.map { user -> user.userFirstName }.toSet()
        Assertions.assertEquals(setOf("joe", "jean"), setNames)
    }
    @Test
    fun canGetUsersWithCreatedAtNull() {
        userRepository.save(Users(userFirstName = "john", createdAt = Date()))
        userRepository.save(Users(userFirstName = "jane", createdAt = Date()))
        userRepository.save(Users(userFirstName = "joe", createdAt = null))
        userRepository.save(Users(userFirstName = "jean", createdAt = null))
        val specification = SpecificationsBuilder<Users>(
            SearchSpec::class.constructors.first().call("", false, emptyArray<String>())
        ).withSearch("createdAt IS NULL").build()
        val users = userRepository.findAll(specification)
        Assertions.assertEquals(2, users.size)
        val setNames = users.map { user -> user.userFirstName }.toSet()
        Assertions.assertEquals(setOf("joe", "jean"), setNames)
    }
}
