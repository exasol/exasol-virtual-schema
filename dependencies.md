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
| [JUnit Jupiter (Aggregator)][8]                 | [Eclipse Public License v2.0][9] |
| [mockito-junit-jupiter][10]                     | [MIT][11]                        |
| [Test containers for Exasol on Docker][12]      | [MIT License][13]                |
| [Testcontainers :: JUnit Jupiter Extension][14] | [MIT][15]                        |
| [Matcher for SQL Result Sets][16]               | [MIT License][17]                |
| [SLF4J JDK14 Provider][18]                      | [MIT License][19]                |
| [Test Database Builder for Java][20]            | [MIT License][21]                |
| [Maven Project Version Getter][22]              | [MIT License][23]                |
| [udf-debugging-java][24]                        | [MIT License][25]                |
| [JaCoCo :: Agent][26]                           | [EPL-2.0][27]                    |

## Plugin Dependencies

| Dependency                                              | License                           |
| ------------------------------------------------------- | --------------------------------- |
| [Apache Maven Clean Plugin][28]                         | [Apache-2.0][29]                  |
| [Apache Maven Install Plugin][30]                       | [Apache-2.0][29]                  |
| [Apache Maven Resources Plugin][31]                     | [Apache-2.0][29]                  |
| [Apache Maven Site Plugin][32]                          | [Apache License, Version 2.0][29] |
| [SonarQube Scanner for Maven][33]                       | [GNU LGPL 3][34]                  |
| [Apache Maven Toolchains Plugin][35]                    | [Apache-2.0][29]                  |
| [Project Keeper Maven plugin][36]                       | [The MIT License][37]             |
| [Apache Maven Compiler Plugin][38]                      | [Apache-2.0][29]                  |
| [Apache Maven Enforcer Plugin][39]                      | [Apache-2.0][29]                  |
| [Maven Flatten Plugin][40]                              | [Apache Software Licenese][29]    |
| [org.sonatype.ossindex.maven:ossindex-maven-plugin][41] | [ASL2][42]                        |
| [Maven Surefire Plugin][43]                             | [Apache-2.0][29]                  |
| [Versions Maven Plugin][44]                             | [Apache License, Version 2.0][29] |
| [duplicate-finder-maven-plugin Maven Mojo][45]          | [Apache License 2.0][46]          |
| [Apache Maven Assembly Plugin][47]                      | [Apache-2.0][29]                  |
| [Apache Maven JAR Plugin][48]                           | [Apache-2.0][29]                  |
| [Artifact reference checker and unifier][49]            | [MIT License][50]                 |
| [Apache Maven Deploy Plugin][51]                        | [Apache-2.0][29]                  |
| [Apache Maven GPG Plugin][52]                           | [Apache-2.0][29]                  |
| [Apache Maven Source Plugin][53]                        | [Apache License, Version 2.0][29] |
| [Apache Maven Javadoc Plugin][54]                       | [Apache-2.0][29]                  |
| [Nexus Staging Maven Plugin][55]                        | [Eclipse Public License][56]      |
| [Apache Maven Dependency Plugin][57]                    | [Apache-2.0][29]                  |
| [Maven Failsafe Plugin][58]                             | [Apache-2.0][29]                  |
| [JaCoCo :: Maven Plugin][59]                            | [EPL-2.0][27]                     |
| [Quality Summarizer Maven Plugin][60]                   | [MIT License][61]                 |
| [error-code-crawler-maven-plugin][62]                   | [MIT License][63]                 |
| [Reproducible Build Maven Plugin][64]                   | [Apache 2.0][42]                  |

[0]: https://github.com/exasol/virtual-schema-common-jdbc/
[1]: https://github.com/exasol/virtual-schema-common-jdbc/blob/main/LICENSE
[2]: http://www.exasol.com/
[3]: https://repo1.maven.org/maven2/com/exasol/exasol-jdbc/24.2.0/exasol-jdbc-24.2.0-license.txt
[4]: https://github.com/exasol/error-reporting-java/
[5]: https://github.com/exasol/error-reporting-java/blob/main/LICENSE
[6]: http://hamcrest.org/JavaHamcrest/
[7]: https://raw.githubusercontent.com/hamcrest/JavaHamcrest/master/LICENSE
[8]: https://junit.org/junit5/
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
[19]: http://www.opensource.org/licenses/mit-license.php
[20]: https://github.com/exasol/test-db-builder-java/
[21]: https://github.com/exasol/test-db-builder-java/blob/main/LICENSE
[22]: https://github.com/exasol/maven-project-version-getter/
[23]: https://github.com/exasol/maven-project-version-getter/blob/main/LICENSE
[24]: https://github.com/exasol/udf-debugging-java/
[25]: https://github.com/exasol/udf-debugging-java/blob/main/LICENSE
[26]: https://www.eclemma.org/jacoco/index.html
[27]: https://www.eclipse.org/legal/epl-2.0/
[28]: https://maven.apache.org/plugins/maven-clean-plugin/
[29]: https://www.apache.org/licenses/LICENSE-2.0.txt
[30]: https://maven.apache.org/plugins/maven-install-plugin/
[31]: https://maven.apache.org/plugins/maven-resources-plugin/
[32]: https://maven.apache.org/plugins/maven-site-plugin/
[33]: http://sonarsource.github.io/sonar-scanner-maven/
[34]: http://www.gnu.org/licenses/lgpl.txt
[35]: https://maven.apache.org/plugins/maven-toolchains-plugin/
[36]: https://github.com/exasol/project-keeper/
[37]: https://github.com/exasol/project-keeper/blob/main/LICENSE
[38]: https://maven.apache.org/plugins/maven-compiler-plugin/
[39]: https://maven.apache.org/enforcer/maven-enforcer-plugin/
[40]: https://www.mojohaus.org/flatten-maven-plugin/
[41]: https://sonatype.github.io/ossindex-maven/maven-plugin/
[42]: http://www.apache.org/licenses/LICENSE-2.0.txt
[43]: https://maven.apache.org/surefire/maven-surefire-plugin/
[44]: https://www.mojohaus.org/versions/versions-maven-plugin/
[45]: https://basepom.github.io/duplicate-finder-maven-plugin
[46]: http://www.apache.org/licenses/LICENSE-2.0.html
[47]: https://maven.apache.org/plugins/maven-assembly-plugin/
[48]: https://maven.apache.org/plugins/maven-jar-plugin/
[49]: https://github.com/exasol/artifact-reference-checker-maven-plugin/
[50]: https://github.com/exasol/artifact-reference-checker-maven-plugin/blob/main/LICENSE
[51]: https://maven.apache.org/plugins/maven-deploy-plugin/
[52]: https://maven.apache.org/plugins/maven-gpg-plugin/
[53]: https://maven.apache.org/plugins/maven-source-plugin/
[54]: https://maven.apache.org/plugins/maven-javadoc-plugin/
[55]: http://www.sonatype.com/public-parent/nexus-maven-plugins/nexus-staging/nexus-staging-maven-plugin/
[56]: http://www.eclipse.org/legal/epl-v10.html
[57]: https://maven.apache.org/plugins/maven-dependency-plugin/
[58]: https://maven.apache.org/surefire/maven-failsafe-plugin/
[59]: https://www.jacoco.org/jacoco/trunk/doc/maven.html
[60]: https://github.com/exasol/quality-summarizer-maven-plugin/
[61]: https://github.com/exasol/quality-summarizer-maven-plugin/blob/main/LICENSE
[62]: https://github.com/exasol/error-code-crawler-maven-plugin/
[63]: https://github.com/exasol/error-code-crawler-maven-plugin/blob/main/LICENSE
[64]: http://zlika.github.io/reproducible-build-maven-plugin
