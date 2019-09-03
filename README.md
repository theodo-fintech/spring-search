<!-- PROJECT SHIELDS -->
<!--
*** I'm using markdown "reference style" links for readability.
*** Reference links are enclosed in brackets [ ] instead of parentheses ( ).
*** See the bottom of this document for the declaration of the reference variables
*** for contributors-url, forks-url, etc. This is an optional, concise syntax you may use.
*** https://www.markdownguide.org/basic-syntax/#reference-style-links
-->
[![Contributors][contributors-shield]][contributors-url]
[![Forks][forks-shield]][forks-url]
[![Stargazers][stars-shield]][stars-url]
[![Issues][issues-shield]][issues-url]
[![MIT License][license-shield]][license-url]


<!-- PROJECT LOGO -->
<br />
<p align="center">
  <a href="https://github.com/sipios/spring-search">
    <img src="https://i.imgur.com/ZmuY8gw.png" alt="Logo" width="80" height="80">
  </a>

  <h3 align="center">Spring Search</h3>

  <p align="center">
    Spring Search provides advanced search capabilities to a JPA entity
    <br />
    <a href="https://github.com/github_username/repo"><strong>Explore the docs »</strong></a>
    <br />
    <br />
    <a href="https://github.com/github_username/repo">View Demo</a>
    ·
    <a href="https://github.com/sipios/spring-search/issues">Report Bug</a>
    ·
    <a href="https://github.com/sipios/spring-search/issues">Request Feature</a>
  </p>
</p>



<!-- TABLE OF CONTENTS -->
## Table of Contents

* [About the Project](#about-the-project)
  * [Built With](#built-with)
* [Getting Started](#getting-started)
  * [Prerequisites](#prerequisites)
  * [Installation](#installation)
* [Usage](#usage)
* [Roadmap](#roadmap)
* [Contributing](#contributing)
* [License](#license)
* [Contact](#contact)
* [Acknowledgements](#acknowledgements)



<!-- ABOUT THE PROJECT -->
## About The Project

[![Spring-search screenshot][product-screenshot]](https://example.com)

Spring Search provides a simple query language to perform advanced searches for your JPA entities.

Let's say you manage cars, and you want to allow API consumers to search for:
* Cars that are blue **and** that were created after year 2006 **or** whose model name contains "Vanquish"
* Cars whose brand is "Aston Martin" **or** whose price is more than 10000$

You could either create custom repository methods for both these operations, which works well if you know in advance which fields users might want to perform searches on. You could also use spring-search that allows searching on all fields, combining logical operators and much more.

Please note that providing such a feature on your API does not come without risks such as performance issues and less clear capabilities for your API. [This article](http://www.bizcoder.com/don-t-design-a-query-string-you-will-one-day-regret) summarizes these risks well.

### Built With

* [Kotlin](https://kotlinlang.org/)
* [Spring Boot](https://spring.io/projects/spring-boot)
* []()



<!-- GETTING STARTED -->
## Getting Started

To get a local copy up and running follow these simple steps.

### Prerequisites

This is an example of how to list things you need to use the software and how to install them.
* npm
```sh
npm install npm@latest -g
```

### Installation
 
1. Clone the repo
```sh
git clone https:://github.com/sipios/spring-search.git
```

2. Install NPM packages
```sh
npm install
```

3. Make the library available
```sh
mvn install #inside the spring-search folder
```

4. Add the repo to your project inside your `pom.xml` file
```xml
<dependency>
    <groupId>com.sipios</groupId>
    <artifactId>spring-search</artifactId>
    <version>0.0.4-SNAPSHOT</version>
</dependency>
```


<!-- USAGE EXAMPLES -->
## Usage

Create a Rest repository extending JpaSpecificationExecutor
```
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource
public interface yourRepository extends JpaRepository<yourModel, Integer>, JpaSpecificationExecutor<yourModel> {

}
```

Import the library in your controller
```kotlin
import com.sipios.springsearch.anotation.SearchSpec;
```

Use it in your controller
```kotlin
@GetMapping("searchUrl")
fun yourFunctionNameHere(@SearchSpec specs: Specification<yourModelHere>): ResponseEntity<yourResponse> {
    return ResponseEntity(yourRepository.findAll(Specification.where(specs)), HttpStatus.OK)
}
```

1. Using the equal operator `:`
![equal operator example](./docs/images/equal-example.gif)

2. Using the not equal operator `!`
![not equal operator example](./docs/images/not-equal-example.gif)

3. Using the greater than operator `>`
![greater than operator example](./docs/images/greater-than-example.gif)

4. Using the less than operator `<`
![less than operator example](./docs/images/less-than-example.gif)

5. Using the starts with operator `*`  
*For the ends with operator, simply place `*` at the beginning of the word*.  
*For the contains operator, simply place `*` at the beginning and the end of the word*.
![starts with operator example](./docs/images/starts-with-example.gif)

6. Using the `OR` operator
![or operator example](./docs/images/or-example.gif)

7. Using the `AND` operator
![and operator example](./docs/images/and-example.gif)

8. Using parenthesis
![parenthesis example](./docs/images/parenthesis-example.gif)

9. Using space in nouns
![space example](./docs/images/space-example.gif)

10. Using special characters
![special characters example](./docs/images/special-characters-example.gif)

11. Using deep fields



_For more examples, please refer to the [Documentation](https://example.com)_



<!-- ROADMAP -->
## Roadmap

See the [open issues](https://github.com/sipios/spring-search/issues) for a list of proposed features (and known issues).



<!-- CONTRIBUTING -->
## Contributing

Contributions are what make the open source community such an amazing place to be learn, inspire, and create. Any contributions you make are **greatly appreciated**.

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request



<!-- LICENSE -->
## License

Distributed under the MIT License. See `LICENSE` for more information.



<!-- CONTACT -->
## Contact

Your Name - [@sipios_fintech](https://twitter.com/sipios_fintech) - contact@sipios.com

Project Link: [https://github.com/sipios/spring-search](https://github.com/sipios/spring-search)


<!-- MARKDOWN LINKS & IMAGES -->
<!-- https://www.markdownguide.org/basic-syntax/#reference-style-links -->
[contributors-shield]: https://img.shields.io/github/contributors/sipios/spring-search.svg?style=flat-square
[contributors-url]: https://github.com/sipios/spring-search/graphs/contributors
[forks-shield]: https://img.shields.io/github/forks/sipios/spring-search.svg?style=flat-square
[forks-url]: https://github.com/sipios/spring-search/network/members
[stars-shield]: https://img.shields.io/github/stars/sipios/spring-search.svg?style=flat-square
[stars-url]: https://github.com/sipios/spring-search/stargazers
[issues-shield]: https://img.shields.io/github/issues/sipios/spring-search.svg?style=flat-square
[issues-url]: https://github.com/sipios/spring-search/issues
[license-shield]: https://img.shields.io/github/license/sipios/spring-search.svg?style=flat-square
[license-url]: https://github.com/sipios/spring-search/blob/master/LICENSE.txt
[product-screenshot]: images/screenshot.png
