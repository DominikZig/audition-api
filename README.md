# Audition API

The purpose of this Spring Boot application is to test general knowledge of SpringBoot, Java, Gradle etc. It is created
for hiring needs of our company but can be used for other purposes.

## Overarching expectations & Assessment areas

<pre>
This is not a university test. 
This is meant to be used for job applications and MUST showcase your full skillset. 
<b>As such, PRODUCTION-READY code must be written and submitted. </b> 
</pre>

- clean, easy to understand code
- good code structures
- Proper code encapsulation
- unit tests with minimum 80% coverage.
- A Working application to be submitted.
- Observability. Does the application contain Logging, Tracing and Metrics instrumentation?
- Input validation.
- Proper error handling.
- Ability to use and configure rest template. We allow for half-setup object mapper and rest template
- Not all information in the Application is perfect. It is expected that a person would figure these out and correct.

## Getting Started

### Prerequisite tooling

- Any Springboot/Java IDE. Ideally IntelliJIdea.
- Java 17
- Gradle 8

### Prerequisite knowledge

- Java
- SpringBoot
- Gradle
- Junit

### Importing Google Java codestyle into INtelliJ

```
- Go to IntelliJ Settings
- Search for "Code Style"
- Click on the "Settings" icon next to the Scheme dropdown
- Choose "Import -> IntelliJ Idea code style XML
- Pick the file "google_java_code_style.xml" from root directory of the application
__Optional__
- Search for "Actions on Save"
    - Check "Reformat Code" and "Organise Imports"
```

---
**NOTE** -
It is highly recommended that the application be loaded and started up to avoid any issues.

---

## Audition Application information

This section provides information on the application and what the needs to be completed as part of the audition
application.

The audition consists of multiple TODO statements scattered throughout the codebase. The applicants are expected to:

- Complete all the TODO statements.
- Add unit tests where applicants believe it to be necessary.
- Make sure that all code quality check are completed.
- Gradle build completes sucessfully.
- Make sure the application if functional.

## Submission process

Applicants need to do the following to submit their work:

- Clone this repository
- Complete their work and zip up the working application.
- Applicants then need to send the ZIP archive to the email of the recruiting manager. This email be communicated to the
  applicant during the recruitment process.

  
---

## Additional Information based on the implementation

This section MUST be completed by applicants. It allows applicants to showcase their view on how an application
can/should be documented.
Applicants can choose to do this in a separate markdown file that needs to be included when the code is committed.

### Documentation

This application is documented using OpenAPI. It is automatically generated at the /api-docs endpoint and can also be
accessed in UI
form at /swagger-ui/index.html. A Postman Collection has also been provided in the root dir for convenience.

In terms of code-level documentation, well named variables, methods and classes have been used over excessive comments.
However, comments
are used where needed to provide additional context.

Thorough tests have also been added to every layer of the application. This serves as living documentation and can be
used to discover the functionality of each method as well as verifying the correctness of the application.

### Future Considerations

Some considerations for major future improvements include:

- Upgrading to JDK 21 (and latest dependencies etc). Currently, this project is still on JDK 17. This can be upgraded
  safely and quickly using OpenRewrite.
- Refactoring/migrating to declarative HTTP interface and Spring RestClient. Currently, this project uses Spring
  RestTemplate,
  which is not recommended in latest versions of Spring. This can be migrated to Spring RestClient for a more modern,
  less
  verbose API.