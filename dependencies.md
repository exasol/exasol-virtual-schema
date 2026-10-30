<!-- @formatter:off -->
# Dependencies

## Compile Dependencies

| Dependency                      | License                |
| ------------------------------- | ---------------------- |
| [Virtual Schema Common JDBC][0] | [MIT License][1]       |
| [Exasol JDBC Driver][2]         | [EXAClient License][3] |
| [error-reporting-java][4]       | [MIT License][5]       |

## Test Dependencies

| Dependency                                      | License                          |
| ----------------------------------------------- | -------------------------------- |
| [Virtual Schema Common JDBC][0]                 | [MIT License][1]                 |
| [Hamcrest][6]                                   | [BSD-3-Clause][7]                |
| [JUnit Jupiter API][8]                          | [Eclipse Public License v2.0][9] |
| [JUnit Jupiter Params][8]                       | [Eclipse Public License v2.0][9] |
| [mockito-junit-jupiter][10]                     | [MIT][11]                        |
| [Test containers for Exasol on Docker][12]      | [MIT License][13]                |
| [Testcontainers :: JUnit Jupiter Extension][14] | [MIT][15]                        |
| [Matcher for SQL Result Sets][16]               | [MIT License][17]                |
| [SLF4J JDK14 Provider][18]                      | [MIT][19]                        |
| [Test Database Builder for Java][20]            | [MIT License][21]                |
| [Maven Project Version Getter][22]              | [MIT License][23]                |
| [udf-debugging-java][24]                        | [MIT License][25]                |
| [JaCoCo :: Agent][26]                           | [EPL-2.0][27]                    |

## Plugin Dependencies

| Dependency                                              | License                                        |
| ------------------------------------------------------- | ---------------------------------------------- |
| [SonarQube Scanner for Maven][28]                       | [GNU LGPL 3][29]                               |
| [Apache Maven Toolchains Plugin][30]                    | [Apache-2.0][31]                               |
| [Apache Maven Compiler Plugin][32]                      | [Apache-2.0][31]                               |
| [Apache Maven Enforcer Plugin][33]                      | [Apache-2.0][31]                               |
| [Maven Flatten Plugin][34]                              | [Apache Software License][31]                  |
| [org.sonatype.ossindex.maven:ossindex-maven-plugin][35] | [ASL2][36]                                     |
| [Maven Surefire Plugin][37]                             | [Apache-2.0][31]                               |
| [Versions Maven Plugin][38]                             | [Apache License, Version 2.0][31]              |
| [duplicate-finder-maven-plugin Maven Mojo][39]          | [Apache License 2.0][40]                       |
| [Apache Maven Artifact Plugin][41]                      | [Apache-2.0][31]                               |
| [Project Keeper Maven plugin][42]                       | [The MIT License][43]                          |
| [Apache Maven Assembly Plugin][44]                      | [Apache-2.0][31]                               |
| [Apache Maven JAR Plugin][45]                           | [Apache-2.0][31]                               |
| [Artifact reference checker and unifier][46]            | [MIT License][47]                              |
| [spdx-maven-plugin Maven Plugin][48]                    | [The Apache Software License, Version 2.0][36] |
| [Apache Maven Dependency Plugin][49]                    | [Apache-2.0][31]                               |
| [Maven Failsafe Plugin][50]                             | [Apache-2.0][31]                               |
| [JaCoCo :: Maven Plugin][51]                            | [EPL-2.0][27]                                  |
| [error-code-crawler-maven-plugin][52]                   | [MIT License][53]                              |
| [Git Commit Id Maven Plugin][54]                        | [GNU Lesser General Public License 3.0][55]    |
| [Apache Maven Clean Plugin][56]                         | [Apache-2.0][31]                               |
| [Apache Maven Resources Plugin][57]                     | [Apache-2.0][31]                               |
| [Apache Maven Install Plugin][58]                       | [Apache-2.0][31]                               |
| [Apache Maven Site Plugin][59]                          | [Apache-2.0][31]                               |

[0]: https://github.com/exasol/virtual-schema-common-jdbc/
[1]: https://github.com/exasol/virtual-schema-common-jdbc/blob/main/LICENSE
[2]: https://www.exasol.com/
[3]: https://repo1.maven.org/maven2/com/exasol/exasol-jdbc/26.2.10/exasol-jdbc-26.2.10-license.txt
[4]: https://github.com/exasol/error-reporting-java/
[5]: https://github.com/exasol/error-reporting-java/blob/main/LICENSE
[6]: http://hamcrest.org/JavaHamcrest/
[7]: https://raw.githubusercontent.com/hamcrest/JavaHamcrest/master/LICENSE
[8]: https://junit.org/
[9]: https://www.eclipse.org/legal/epl-v20.html
[10]: https://github.com/mockito/mockito
[11]: https://opensource.org/licenses/MIT
[12]: https://github.com/exasol/exasol-testcontainers/
[13]: https://github.com/exasol/exasol-testcontainers/blob/main/LICENSE
[14]: https://java.testcontainers.org
[15]: http://opensource.org/licenses/MIT
[16]: https://github.com/exasol/hamcrest-resultset-matcher/
[17]: https://github.com/exasol/hamcrest-resultset-matcher/blob/main/LICENSE
[18]: http://www.slf4j.org
[19]: https://opensource.org/license/mit
[20]: https://github.com/exasol/test-db-builder-java/
[21]: https://github.com/exasol/test-db-builder-java/blob/main/LICENSE
[22]: https://github.com/exasol/maven-project-version-getter/
[23]: https://github.com/exasol/maven-project-version-getter/blob/main/LICENSE
[24]: https://github.com/exasol/udf-debugging-java/
[25]: https://github.com/exasol/udf-debugging-java/blob/main/LICENSE
[26]: https://www.eclemma.org/jacoco/index.html
[27]: https://www.eclipse.org/legal/epl-2.0/
[28]: https://docs.sonarsource.com/sonarqube-server/latest/extension-guide/developing-a-plugin/plugin-basics/sonar-scanner-maven/sonar-maven-plugin/
[29]: http://www.gnu.org/licenses/lgpl.txt
[30]: https://maven.apache.org/plugins/maven-toolchains-plugin/
[31]: https://www.apache.org/licenses/LICENSE-2.0.txt
[32]: https://maven.apache.org/plugins/maven-compiler-plugin/
[33]: https://maven.apache.org/enforcer/maven-enforcer-plugin/
[34]: https://www.mojohaus.org/flatten-maven-plugin/
[35]: https://sonatype.github.io/ossindex-maven/maven-plugin/
[36]: http://www.apache.org/licenses/LICENSE-2.0.txt
[37]: https://maven.apache.org/surefire/maven-surefire-plugin/
[38]: https://www.mojohaus.org/versions/versions-maven-plugin/
[39]: https://basepom.github.io/duplicate-finder-maven-plugin
[40]: http://www.apache.org/licenses/LICENSE-2.0.html
[41]: https://maven.apache.org/plugins/maven-artifact-plugin/
[42]: https://github.com/exasol/project-keeper/
[43]: https://github.com/exasol/project-keeper/blob/main/LICENSE
[44]: https://maven.apache.org/plugins/maven-assembly-plugin/
[45]: https://maven.apache.org/plugins/maven-jar-plugin/
[46]: https://github.com/exasol/artifact-reference-checker-maven-plugin/
[47]: https://github.com/exasol/artifact-reference-checker-maven-plugin/blob/main/LICENSE
[48]: https://github.com/spdx/spdx-maven-plugin
[49]: https://maven.apache.org/plugins/maven-dependency-plugin/
[50]: https://maven.apache.org/surefire/maven-failsafe-plugin/
[51]: https://www.jacoco.org/jacoco/trunk/doc/maven.html
[52]: https://github.com/exasol/error-code-crawler-maven-plugin/
[53]: https://github.com/exasol/error-code-crawler-maven-plugin/blob/main/LICENSE
[54]: https://github.com/git-commit-id/git-commit-id-maven-plugin
[55]: http://www.gnu.org/licenses/lgpl-3.0.txt
[56]: https://maven.apache.org/plugins/maven-clean-plugin/
[57]: https://maven.apache.org/plugins/maven-resources-plugin/
[58]: https://maven.apache.org/plugins/maven-install-plugin/
[59]: https://maven.apache.org/plugins/maven-site-plugin/
