# Welcome!
This is a Timetable App I built as an educational project with the [Foxminded Java EE course](https://foxminded.com.ua/ua/java/).

## Set-up
App reads 20 first, 20 last names, and 10 courses from txt files and randomly generates a university model with (currently) 300 students grouped by 30, 5 professors, 10 courses, and 5 auditoriums. Professor-courses are randomly assigned with max 2 professors per course and 4 courses per professor. App also generates a random schedule where:
* All groups take all courses
* Schedule cycles every two weeks
* Courses are taught Mon-Fri
* Each day is divided into 5 periods
* Semester runs September 7 - December 11, 2020 (14 weeks)

Timetable itself is organized in a way that ony two-week cycle of schedule templates is generated on startup. Those templates are later used to generate schedule items for specific dates upon request. If schedule items have already been created in the course of user interaction they are retrieved from the database. This set-up allows the following flexibility:
* Entire semester schedule is **not generated on startup** but rather on "as-needed" basis
* Courses can be **rescheduled once** per occurrence in which case same course in the next cycle will be generated from the unchanged template
* **Permanent rescheduling** is made easy: all it takes is updating a template (with any existing linked schedule items). All further schedule items generated from amended template will reflect the change

## Use cases
Students can view:
* Two-week schedule (templates for study cycle)
* Their day/week/month schedule
* Entire (unfiltered) day/week/month schedule

Faculty members can view:
* Two-week schedule (templates for study cycle)
* Their day/week/month schedule
* Entire (unfiltered) day/week/month schedule
* List of their courses
* List of course attendees

University management has a full spectrum of options:
* University
  * Full CRUD operations on students, faculty, auditoriums, courses, groups
  * Move students between groups
  * View professors courses and change those assignments
  * View professors' course attendees
* Timetable
  * View two-week schedule
  * View day/week/month schedule for student/professor/auditorium/entire (no filters)
  * View available professors/auditoriums for a specific period
  * Substitute professor/auditorium in a schedule item
  * Reschedule course: single occurrence or permanently


## Project transformations
As this is an educational project it started off small and underwent a series of metamorphoses:
* Decomposition and domain model UML
* Simple Maven project implementing the designed model (no DB, consol output)
* Added Spring Boot and DAO layer (Spring JDBC)
* Moved business logic to a proper service layer
* Added UI with Spring MVC, Thymeleaf and Bootstrap
* Upgraded DAO layer from JDBC to Hibernate
* Further transformation of DAO to Spring Data JPA

Work in progress:
* Validation
* REST
* Swagger 
