# Nilai Akademik

**An academic grading application for Admin, Teacher, and Student workflows.**

[Bahasa Indonesia](readme.md) · [Source](https://github.com/KairunHanjun/nilaimhs) · [Apache 2.0](LICENSE)

Nilai Akademik connects user administration, grade entry, and student reports in a Java web application. This personal project explores academic data flows, authentication, and role-based access.

## Features

| Role | Workflow |
| --- | --- |
| Admin | Manage accounts and profiles, view statistics and grade summaries. |
| Teacher | Enter assignment, midterm, and final grades; update a profile. |
| Student | View a personal report, edit a profile, and print the report. |

The local portfolio demo uses **30% assignments + 30% midterm + 40% final**, with a passing threshold of **70**. Missing grades are distinguished from recorded zero grades.

## Stack

Java 25, Spring Boot 4.0.6, Thymeleaf, Spring Security, Spring Data JPA, PostgreSQL, Gradle Wrapper, and JUnit/Spring testing. The separate local portfolio demo uses in-memory H2 and fictional data.

## Run the repository source

Install **JDK 25**, prepare PostgreSQL, and supply datasource configuration for your environment. The first build downloads dependencies. Keep credentials in local configuration or environment variables.

```sh
git clone https://github.com/KairunHanjun/nilaimhs.git
cd nilaimhs
./gradlew bootRun
```

On Windows, use `gradlew.bat` instead of `./gradlew`. Review database configuration and the repository's dependency versions first; this is not a production-readiness claim.

## Local portfolio demo

> This section describes the demo prepared in the portfolio workspace. This README/LICENSE update does not include implementation changes that remain local. Confirm that `start-demo.ps1` and `application-demo.yml` exist before using these commands.

```powershell
.\start-demo.ps1
.\start-demo.ps1 -Port 8081
```

With the corresponding demo source available:

```sh
./gradlew bootRun --args='--spring.profiles.active=demo'
```

Open **http://127.0.0.1:8080** and stop with `Ctrl+C`. The launcher selects a sibling portable JDK when available; otherwise set `JAVA_HOME` to JDK 25.

| Role | Login ID | Demo password |
| --- | --- | --- |
| Admin | `admin` | `DemoNilai123!` |
| Teacher | `19870001` | `DemoNilai123!` |
| Student | `20260001` | `DemoNilai123!` |

Seed data contains **1 fictional admin, 3 teachers, and 8 students**. Restarting resets the in-memory database. These accounts are for local demonstrations only.

## Build and tests

```sh
./gradlew test bootJar
```

Reports are written to `build/reports/tests/test/index.html`; JAR files go to `build/libs/`, named according to the Gradle project version. Local demo checks covered authentication, role access, CSRF, account/profile consistency, grade validation, and incomplete/zero grades. These results refer to the local demo implementation, not repository CI.

## Structure

```text
src/main/java/         Controllers, services, repositories, configuration
src/main/resources/   Templates, assets, application configuration
src/test/             Application tests
gradle/               Gradle Wrapper
```

## Scope and next steps

Assessment weights remain fixed. Configurable curricula, academic periods, grade auditing, and production deployment remain development opportunities. Portfolio data does not represent a real school's adoption. Screenshots are held separately in the portfolio workspace at `../portfolio-materials/screenshots/nilaimhs` and are not included in this repository clone.

Originally developed independently by **Guntur Ahmad Lion Putranto**. Codex assisted with subsequent demo refinements and portfolio documentation.

## License

Project code is licensed under the **Apache License 2.0**; see [LICENSE](LICENSE). Third-party dependencies and assets retain their respective licenses.
