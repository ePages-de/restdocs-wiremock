# Spring REST Docs WireMock Integration

![](https://img.shields.io/github/license/ePages-de/restdocs-wiremock.svg?branch=master)
[ ![Build Status](https://travis-ci.org/ePages-de/restdocs-wiremock.svg)](https://travis-ci.org/ePages-de/restdocs-wiremock)
[ ![Coverage Status](https://coveralls.io/repos/github/ePages-de/restdocs-wiremock/badge.svg?branch=master)](https://coveralls.io/r/ePages-de/restdocs-wiremock)
[ ![Download](https://api.bintray.com/packages/epages/maven/restdocs-wiremock/images/download.svg)](https://bintray.com/epages/maven/restdocs-wiremock/_latestVersion)

This is a plugin for auto-generating [WireMock](http://wiremock.org/) stubs
as part of documenting your REST API with [Spring REST Docs](http://projects.spring.io/spring-restdocs/).

The basic idea is to use the requests and responses from the integration tests as stubs for testing your client's 
API contract. The mock templates can be packaged as jar files and be published into your company's
artifact repository for this purpose.

Details and background information can be read on our [ePages Developer Blog](https://developer.epages.com/blog/2016/07/14/wiremock.html).

<!-- TOC depthFrom:2 depthTo:3 -->

- [Contents](#contents)
- [How to include `restdocs-wiremock` into your server project](#how-to-include-restdocs-wiremock-into-your-server-project)
    - [Dependencies](#dependencies)
    - [Producing snippets](#producing-snippets)
    - [The WireMock stubs jar](#the-wiremock-stubs-jar)
- [How to use WireMock in your client tests](#how-to-use-wiremock-in-your-client-tests)
    - [Dependencies](#dependencies-1)
    - [Configuring your test to use the WireMock stubs](#configuring-your-test-to-use-the-wiremock-stubs)
- [Building from source](#building-from-source)
- [Publishing](#publishing)
- [Other resources](#other-resources)

<!-- /TOC -->
## Contents

This repository consists of four projects

* `restdocs-wiremock`: The library to extend Spring REST Docs with WireMock stub snippet generation.
* `restdocs-server`: A sample server documenting its REST API (i.e. the Spring REST Docs "notes" example).
   Besides producing human-readable documentation it will also generate JSON snippets to be used as stubs for WireMock.
* `wiremock-spring-boot-starter`: A spring boot starter which adds a `WireMockServer` to your client's ApplicationContext for integration testing.
  This is optional, but highly recommended when verifying your client contract in a SpringBootTest.
* `restdocs-client`: A sample client using the server API, with integration testing its client contract against the stubs provided via WireMock.


## How to include `restdocs-wiremock` into your server project

### Dependencies

The project is published on `jcenter` from `bintray`, so firstly, you need to add `jcenter` as package
repository for your project.

Then, add `restdocs-wiremock` as a dependency in test scope. As `restdocs-wiremock` only depends on `spring-restdocs-core`,
you also need to add either `spring-restdocs-mockmvc` or `spring-restdocs-restassured`, depending on your test scenario.
It is recommended to use the `spring-boot` gradle plugin to enable dependency management from the Spring IO Platform.

In gradle it would look like this:

```groovy
dependencies {
  testCompile('com.epages:restdocs-wiremock:0.7.26')
  testCompile('org.springframework.restdocs:spring-restdocs-mockmvc')
}
```

Spring Boot 1.3.x users: Please make sure you use Spring REST Docs `1.1.+` or newer. The `1.0.x.RELEASE`, included with Spring Boot 1.3 is not compatible with this plugin.
The example below shows how to set Spring REST Docs to this version, when using Spring dependency management.

```groovy
dependencyManagement.imports {
    ext['spring-restdocs.version'] = '1.2.2.RELEASE'
}
```

When using maven:

```xml
<properties>
	<spring-restdocs.version>1.2.2.RELEASE</spring-restdocs.version>
</properties>
<dependency>
	<groupId>com.epages</groupId>
	<artifactId>restdocs-wiremock</artifactId>
	<version>0.7.26</version>
	<scope>test</scope>
</dependency>
<dependency>
	<groupId>org.springframework.restdocs</groupId>
	<artifactId>spring-restdocs-mockmvc</artifactId>
	<scope>test</scope>
</dependency>
```

### Producing snippets

During REST Docs run, snippets like the one below are generated and put into a dedicated jar file, which you can publish into your artifact repository. 

Integration into your test code is as simple as adding `wiremockJson()` from `com.epages.restdocs.WireMockDocumentation`
to the `document()` calls for Spring REST Docs. For example:

```java
@RunWith(SpringJUnit4ClassRunner.class)
//...
class ApiDocumentation {
    // ... the usual test setup.
    void testGetSingleNote() {
        this.mockMvc.perform(get("/notes/1").accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andDo(document("get-note",
          wiremockJson(),
          responseFields( ... )
        ));
    }
}
```

The snippet below is the resulting snippet of a `200 OK` response to `/notes/1`, with
the response body as provided by the integration test.

```json
{
  "request" : {
    "method" : "GET",
    "urlPath" : "/notes/1"
  },
  "response" : {
    "status" : 200,
    "headers" : {
      "Content-Type" : [ "application/hal+json" ],
      "Content-Length" : [ "344" ]
    },
    "body" : "{\n  \"title\" : \"REST maturity model\",\n  \"body\" : \"http://martinfowler.com/articles/richardsonMaturityModel.html\",\n  \"_links\" : {\n    \"self\" : {\n      \"href\" : \"http://localhost:8080/notes/1\"\n    },\n    \"note\" : {\n      \"href\" : \"http://localhost:8080/notes/1\"\n    },\n    \"tags\" : {\n      \"href\" : \"http://localhost:8080/notes/1/tags\"\n    }\n  }\n}"
  }
}
```

#### More flexible request matching using `urlPathPattern`

The above snippet has a shortcoming. WireMock will only match a request if the url matches the complete path, including the id.
This is really inflexible.
We can do better by using `RestDocumentationRequestBuilders` to construct the request using a url template, instead of `MockMvcRequestBuilders`.

```java
@RunWith(SpringJUnit4ClassRunner.class)
//...
class ApiDocumentation {
    // ... the usual test setup.
    void testGetSingleNote() {
        this.mockMvc.perform(RestDocumentationRequestBuilders.get("/notes/{id}", 1)
          .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andDo(document("get-note",
          wiremockJson(),
          responseFields( ... )
        ));
    }
}
```

This generates a snippet that uses `urlPathPattern` instead if `urlPath`. 
So WireMock would match a request with any id value.

```json
{
  "request" : {
    "method" : "GET",
    "urlPathPattern" : "/notes/[^/]+"
  },
  "response" : {
    //...
  }
}
```

#### Response Templating

WireMock stubs with static responses are too inflexible in many use cases. That is why WireMock offers [Response Templating](http://wiremock.org/docs/response-templating/). Response Templating allows us to make our responses more dynamic by using expressions instead of static values.

`restdocs-wiremock` allows us to use response template expressions:

```java
this.mockMvc.perform(get("/notes/{id}", id))
  .andDo(document("get-note",
    wiremockJson(idFieldReplacedWithPathParameterValue()),
    //..
));
```

`idFieldReplacedWithPathParameterValue()` is a convenience method offered by `WireMockDocumentation` for the common use case to replace the `id` field in a response with the value of the second path segment.

The following variant would give the same result:

```java
wiremockJson(templatedResponseField("id").replacedWithWireMockTemplateExpression("request.requestLine.pathSegments.[1]"));
```

We can also address the path parameter by the URI template name.:
```java
wiremockJson(templatedResponseField("id").replacedWithUriTemplateVariableValue("id");)
```

The resulting stub contains the expression and WireMock will replace the `id` field with the value of the path segment when serving a response based on this stub.

```json
{
  "request" : {
    "method" : "GET",
    "urlPattern" : "/notes/[^/]+"
  },
  "response" : {
    "transformers" : [ "response-template" ],
    "body" : "{\"id\":\"{{request.requestLine.pathSegments.[1]}}\",\"title\":\"REST maturity model\"}",
    "status" : 200
  }
}
```

WireMock also offers expressions to generate random values.

```java
wiremockJson(templatedResponseField("id").replacedWithWireMockTemplateExpression("randomValue length=33 type='ALPHANUMERIC'")
```

Also replacing date values often comes in handy:

```java
wiremockJson(templatedResponseField("date").replacedWithWireMockTemplateExpression("now")
```

### The WireMock stubs jar

On the server side you need to collect the WireMock stubs and publish them into an artifact repository.
In gradle this can be achieved by a custom jar task.

```groovy
task wiremockJar(type: Jar) {
	description = 'Generates the jar file containing the wiremock stubs for your REST API.'
	group = 'Build'
	classifier = 'wiremock'
	dependsOn project.tasks.test
	from (snippetsDir) {
		include '**/wiremock-stub.json'
		into "wiremock/${project.name}/mappings"
	}
}
```

*TODO: Add maven example.*

On the client side, add a dependency to the test-runtime to the jar containing the WireMock stubs. After
that, the JSON files can be accessed as classpath resources.

```groovy
testRuntime (group:'com.epages', name:'restdocs-server', version:'0.7.26', classifier:'wiremock', ext:'jar')
``` 

## How to use WireMock in your client tests

Integrating a WireMock server can easily be achieved by including our `wiremock-spring-boot-starter` into your project.
It adds a `wireMockServer` bean, which you can auto-wire in your test code. By default, we start WireMock on a dynamic port,
and set a `wiremock.port` property to the port WireMock is running on. This property can be used to point your clients
to the location of the `WireMock` server.

Services based on `spring-cloud-netflix`, i.e. using `feign` and `ribbon`, are auto-configured for you.

### Dependencies

To add a dependency via gradle, extend your `build.gradle` with the following line:

```groovy
  testCompile('com.epages:wiremock-spring-boot-starter:0.7.26')
```


When using maven, add the following dependency in test scope.

```xml
<dependency>
	<groupId>com.epages</groupId>
	<artifactId>wiremock-spring-boot-starter</artifactId>
	<version>0.7.26</version>
	<scope>test</scope>
</dependency>
```

*Important note for Spring Cloud users*: The BOM produced by Spring Cloud includes a transitive resolution of WireMock 
version 1.55, (via `spring-cloud-aws` -> `aws-java-sdk-core` -> `wiremock (test)`). As versions from BOM always override 
transitive versions coming in through maven dependencies, you need to add an explicit dependency on WireMock 2.x to your
project, like shown in the following gradle example:

```groovy
  testCompile('com.github.tomakehurst:wiremock:2.10.1')
```

### Configuring your test to use the WireMock stubs

Here is an excerpt of the sample test from the restdocs-client project to illustrate the usage.

```java
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { ClientApplication.class })
@ActiveProfiles("test") // (1)
@WireMockTest(stubPath = "wiremock/restdocs-server") // (2) 
public class NoteServiceTest {

    @Autowired
    private WireMockServer wireMockServer; // (3)

    ....
}
```

1. Configure your test to point to your WireMock server.
   In our example we are using a Spring Expression inside `application-test.properties` to point our noteservice to
   WireMock: `noteservice.baseUri=http://localhost:${wiremock.port}/`
2. The `@WireMockTest` annotation enables the `wireMockServer` bean, which can be accessed
   from your test's application context. By default, it starts a WireMockServer on a dynamic port, but you could also
   set it to a fixed port. The `stubPath` property can be used to point to a classpath resource folder that
   holds your json stubs.
3. If you want, you can auto-wire the `WireMockServer` instance, and re-configure it, just as described in the official
   [WireMock documentation](http://wiremock.org/).

It is possible to read-in a subset of mappings for each test, by repeating the `@WireMockTest` annotation on the test method.
The `stubPath` is concatenated from the values given on the test class and the test method, just as a `@RequestMapping` annotation in Spring would.
In the example given below, the resulting stubPath provided to WireMock is composed as `wiremock/myservice/specific-mappings`.

```java
@WireMockTest(stubPath = "wiremock/myservice")
public class MyTest {
    @Test
    @WireMockTest(stubPath = "specific-mappings")
    public void testDifferentMappings() {
     ....
    }
```

## Building from source

Please execute at least step 1 + 2 if before importing restdocs-wiremock into your IDE.

1. Publish the current restdocs-wiremock library code into your local maven repository.

  ```shell
  ./gradlew restdocs-wiremock:build restdocs-wiremock:publishToMavenLocal
  ```

2.  Run the server tests, which uses the WireMock integration into Spring REST Docs. 
    As a result, there is a `restdocs-server-wiremock` jar file in your local maven repository.
    Mind that this jar only contains a set of json files without explicit dependency on WireMock itself. 

  ```shell
  ./gradlew restdocs-server:build restdocs-server:publishToMavenLocal
  ```

3. Run the client tests, that expect a specific API from the server. 
   By mocking a server via WireMock the client can be tested in isolation, but would notice a breaking change.

  ```shell
  ./gradlew restdocs-client:build
  ```

## Publishing

This project makes use of the [axion-release-plugin](https://github.com/allegro/axion-release-plugin)
and publishing is automated in travis, when a new release is tagged in git.

Locally you should be able to create a new release by running the `release` task on gradle. A successful
travis build of this tag should finally end up on [bintray](https://bintray.com/epages/maven/restdocs-wiremock/).

```shell
./gradlew clean build release
```

## Other resources

- A similar approach is taken by [Spring Cloud Contract](https://cloud.spring.io/spring-cloud-contract/)

