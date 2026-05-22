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

| Dependency                                              | License                                     |
| ------------------------------------------------------- | ------------------------------------------- |
| [SonarQube Scanner for Maven][28]                       | [GNU LGPL 3][29]                            |
| [Apache Maven Toolchains Plugin][30]                    | [Apache-2.0][31]                            |
| [Project Keeper Maven plugin][32]                       | [The MIT License][33]                       |
| [Apache Maven Compiler Plugin][34]                      | [Apache-2.0][31]                            |
| [Apache Maven Enforcer Plugin][35]                      | [Apache-2.0][31]                            |
| [Maven Flatten Plugin][36]                              | [Apache Software License][31]               |
| [org.sonatype.ossindex.maven:ossindex-maven-plugin][37] | [ASL2][38]                                  |
| [Maven Surefire Plugin][39]                             | [Apache-2.0][31]                            |
| [Versions Maven Plugin][40]                             | [Apache License, Version 2.0][31]           |
| [duplicate-finder-maven-plugin Maven Mojo][41]          | [Apache License 2.0][42]                    |
| [Apache Maven Artifact Plugin][43]                      | [Apache-2.0][31]                            |
| [Apache Maven Assembly Plugin][44]                      | [Apache-2.0][31]                            |
| [Apache Maven JAR Plugin][45]                           | [Apache-2.0][31]                            |
| [Artifact reference checker and unifier][46]            | [MIT License][47]                           |
| [Apache Maven Deploy Plugin][48]                        | [Apache-2.0][31]                            |
| [Apache Maven GPG Plugin][49]                           | [Apache-2.0][31]                            |
| [Apache Maven Source Plugin][50]                        | [Apache-2.0][31]                            |
| [Apache Maven Javadoc Plugin][51]                       | [Apache-2.0][31]                            |
| [Central Publishing Maven Plugin][52]                   | [The Apache License, Version 2.0][31]       |
| [Apache Maven Dependency Plugin][53]                    | [Apache-2.0][31]                            |
| [Maven Failsafe Plugin][54]                             | [Apache-2.0][31]                            |
| [JaCoCo :: Maven Plugin][55]                            | [EPL-2.0][27]                               |
| [Quality Summarizer Maven Plugin][56]                   | [MIT License][57]                           |
| [error-code-crawler-maven-plugin][58]                   | [MIT License][59]                           |
| [Git Commit Id Maven Plugin][60]                        | [GNU Lesser General Public License 3.0][61] |
| [Apache Maven Clean Plugin][62]                         | [Apache-2.0][31]                            |
| [Apache Maven Resources Plugin][63]                     | [Apache-2.0][31]                            |
| [Apache Maven Install Plugin][64]                       | [Apache-2.0][31]                            |
| [Apache Maven Site Plugin][65]                          | [Apache-2.0][31]                            |

[0]: https://github.com/exasol/virtual-schema-common-jdbc/
[1]: https://github.com/exasol/virtual-schema-common-jdbc/blob/main/LICENSE
[2]: https://www.exasol.com/
[3]: https://repo1.maven.org/maven2/com/exasol/exasol-jdbc/26.2.7/exasol-jdbc-26.2.7-license.txt
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
[32]: https://github.com/exasol/project-keeper/
[33]: https://github.com/exasol/project-keeper/blob/main/LICENSE
[34]: https://maven.apache.org/plugins/maven-compiler-plugin/
[35]: https://maven.apache.org/enforcer/maven-enforcer-plugin/
[36]: https://www.mojohaus.org/flatten-maven-plugin/
[37]: https://sonatype.github.io/ossindex-maven/maven-plugin/
[38]: http://www.apache.org/licenses/LICENSE-2.0.txt
[39]: https://maven.apache.org/surefire/maven-surefire-plugin/
[40]: https://www.mojohaus.org/versions/versions-maven-plugin/
[41]: https://basepom.github.io/duplicate-finder-maven-plugin
[42]: http://www.apache.org/licenses/LICENSE-2.0.html
[43]: https://maven.apache.org/plugins/maven-artifact-plugin/
[44]: https://maven.apache.org/plugins/maven-assembly-plugin/
[45]: https://maven.apache.org/plugins/maven-jar-plugin/
[46]: https://github.com/exasol/artifact-reference-checker-maven-plugin/
[47]: https://github.com/exasol/artifact-reference-checker-maven-plugin/blob/main/LICENSE
[48]: https://maven.apache.org/plugins/maven-deploy-plugin/
[49]: https://maven.apache.org/plugins/maven-gpg-plugin/
[50]: https://maven.apache.org/plugins/maven-source-plugin/
[51]: https://maven.apache.org/plugins/maven-javadoc-plugin/
[52]: https://central.sonatype.org
[53]: https://maven.apache.org/plugins/maven-dependency-plugin/
[54]: https://maven.apache.org/surefire/maven-failsafe-plugin/
[55]: https://www.jacoco.org/jacoco/trunk/doc/maven.html
[56]: https://github.com/exasol/quality-summarizer-maven-plugin/
[57]: https://github.com/exasol/quality-summarizer-maven-plugin/blob/main/LICENSE
[58]: https://github.com/exasol/error-code-crawler-maven-plugin/
[59]: https://github.com/exasol/error-code-crawler-maven-plugin/blob/main/LICENSE
[60]: https://github.com/git-commit-id/git-commit-id-maven-plugin
[61]: http://www.gnu.org/licenses/lgpl-3.0.txt
[62]: https://maven.apache.org/plugins/maven-clean-plugin/
[63]: https://maven.apache.org/plugins/maven-resources-plugin/
[64]: https://maven.apache.org/plugins/maven-install-plugin/
[65]: https://maven.apache.org/plugins/maven-site-plugin/
