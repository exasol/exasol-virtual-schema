<!-- @formatter:off -->
# Dependencies

## Compile Dependencies

| Dependency                      | License                                                                                                      |
| ------------------------------- | ------------------------------------------------------------------------------------------------------------ |
| [Virtual Schema Common JDBC][0] | [MIT License][1]                                                                                             |
| [EXASolution JDBC Driver][2]    | [EXAClient License][3]                                                                                       |
| [error-reporting-java][4]       | [MIT][5]                                                                                                     |
| [JSON-P Default Provider][6]    | [Eclipse Public License 2.0][7]; [GNU General Public License, version 2 with the GNU Classpath Exception][8] |

## Test Dependencies

| Dependency                                      | License                           |
| ----------------------------------------------- | --------------------------------- |
| [Virtual Schema Common JDBC][0]                 | [MIT License][1]                  |
| [Hamcrest][9]                                   | [BSD License 3][10]               |
| [JUnit Jupiter (Aggregator)][11]                | [Eclipse Public License v2.0][12] |
| [mockito-junit-jupiter][13]                     | [The MIT License][14]             |
| [Test containers for Exasol on Docker][15]      | [MIT][5]                          |
| [Testcontainers :: JUnit Jupiter Extension][16] | [MIT][17]                         |
| [Matcher for SQL Result Sets][18]               | [MIT][5]                          |
| [SLF4J JDK14 Binding][19]                       | [MIT License][20]                 |
| [Test Database Builder for Java][21]            | [MIT License][22]                 |
| [udf-debugging-java][23]                        | [MIT][5]                          |
| [Maven Project Version Getter][24]              | [MIT][5]                          |
| [exasol-test-setup-abstraction-java][25]        | [MIT License][26]                 |
| [JaCoCo :: Agent][27]                           | [Eclipse Public License 2.0][28]  |

## Plugin Dependencies

| Dependency                                              | License                                        |
| ------------------------------------------------------- | ---------------------------------------------- |
| [SonarQube Scanner for Maven][29]                       | [GNU LGPL 3][30]                               |
| [Apache Maven Compiler Plugin][31]                      | [Apache License, Version 2.0][32]              |
| [Apache Maven Enforcer Plugin][33]                      | [Apache License, Version 2.0][32]              |
| [Maven Flatten Plugin][34]                              | [Apache Software Licenese][35]                 |
| [org.sonatype.ossindex.maven:ossindex-maven-plugin][36] | [ASL2][35]                                     |
| [Maven Surefire Plugin][37]                             | [Apache License, Version 2.0][32]              |
| [Versions Maven Plugin][38]                             | [Apache License, Version 2.0][32]              |
| [Project keeper maven plugin][39]                       | [The MIT License][40]                          |
| [Apache Maven Assembly Plugin][41]                      | [Apache License, Version 2.0][32]              |
| [Apache Maven JAR Plugin][42]                           | [Apache License, Version 2.0][32]              |
| [Artifact reference checker and unifier][43]            | [MIT][5]                                       |
| [Apache Maven Deploy Plugin][44]                        | [Apache License, Version 2.0][32]              |
| [Apache Maven GPG Plugin][45]                           | [Apache License, Version 2.0][32]              |
| [Apache Maven Source Plugin][46]                        | [Apache License, Version 2.0][32]              |
| [Apache Maven Javadoc Plugin][47]                       | [Apache License, Version 2.0][32]              |
| [Nexus Staging Maven Plugin][48]                        | [Eclipse Public License][49]                   |
| [Apache Maven Dependency Plugin][50]                    | [Apache License, Version 2.0][32]              |
| [Maven Failsafe Plugin][51]                             | [Apache License, Version 2.0][32]              |
| [JaCoCo :: Maven Plugin][52]                            | [Eclipse Public License 2.0][28]               |
| [error-code-crawler-maven-plugin][53]                   | [MIT License][54]                              |
| [Reproducible Build Maven Plugin][55]                   | [Apache 2.0][35]                               |
| [Maven Clean Plugin][56]                                | [The Apache Software License, Version 2.0][35] |
| [Maven Resources Plugin][57]                            | [The Apache Software License, Version 2.0][35] |
| [Maven Install Plugin][58]                              | [The Apache Software License, Version 2.0][35] |
| [Maven Site Plugin 3][59]                               | [The Apache Software License, Version 2.0][35] |

[0]: https://github.com/exasol/virtual-schema-common-jdbc/
[1]: https://github.com/exasol/virtual-schema-common-jdbc/blob/main/LICENSE
[2]: http://www.exasol.com
[3]: https://www.exasol.com/support/secure/attachment/155343/EXASOL_SDK-7.0.11.tar.gz
[4]: https://github.com/exasol/error-reporting-java
[5]: https://opensource.org/licenses/MIT
[6]: https://github.com/eclipse-ee4j/jsonp
[7]: https://projects.eclipse.org/license/epl-2.0
[8]: https://projects.eclipse.org/license/secondary-gpl-2.0-cp
[9]: http://hamcrest.org/JavaHamcrest/
[10]: http://opensource.org/licenses/BSD-3-Clause
[11]: https://junit.org/junit5/
[12]: https://www.eclipse.org/legal/epl-v20.html
[13]: https://github.com/mockito/mockito
[14]: https://github.com/mockito/mockito/blob/main/LICENSE
[15]: https://github.com/exasol/exasol-testcontainers
[16]: https://testcontainers.org
[17]: http://opensource.org/licenses/MIT
[18]: https://github.com/exasol/hamcrest-resultset-matcher
[19]: http://www.slf4j.org
[20]: http://www.opensource.org/licenses/mit-license.php
[21]: https://github.com/exasol/test-db-builder-java/
[22]: https://github.com/exasol/test-db-builder-java/blob/main/LICENSE
[23]: https://github.com/exasol/udf-debugging-java/
[24]: https://github.com/exasol/maven-project-version-getter
[25]: https://github.com/exasol/exasol-test-setup-abstraction-java/
[26]: https://github.com/exasol/exasol-test-setup-abstraction-java/blob/main/LICENSE
[27]: https://www.eclemma.org/jacoco/index.html
[28]: https://www.eclipse.org/legal/epl-2.0/
[29]: http://sonarsource.github.io/sonar-scanner-maven/
[30]: http://www.gnu.org/licenses/lgpl.txt
[31]: https://maven.apache.org/plugins/maven-compiler-plugin/
[32]: https://www.apache.org/licenses/LICENSE-2.0.txt
[33]: https://maven.apache.org/enforcer/maven-enforcer-plugin/
[34]: https://www.mojohaus.org/flatten-maven-plugin/
[35]: http://www.apache.org/licenses/LICENSE-2.0.txt
[36]: https://sonatype.github.io/ossindex-maven/maven-plugin/
[37]: https://maven.apache.org/surefire/maven-surefire-plugin/
[38]: http://www.mojohaus.org/versions-maven-plugin/
[39]: https://github.com/exasol/project-keeper/
[40]: https://github.com/exasol/project-keeper/blob/main/LICENSE
[41]: https://maven.apache.org/plugins/maven-assembly-plugin/
[42]: https://maven.apache.org/plugins/maven-jar-plugin/
[43]: https://github.com/exasol/artifact-reference-checker-maven-plugin
[44]: https://maven.apache.org/plugins/maven-deploy-plugin/
[45]: https://maven.apache.org/plugins/maven-gpg-plugin/
[46]: https://maven.apache.org/plugins/maven-source-plugin/
[47]: https://maven.apache.org/plugins/maven-javadoc-plugin/
[48]: http://www.sonatype.com/public-parent/nexus-maven-plugins/nexus-staging/nexus-staging-maven-plugin/
[49]: http://www.eclipse.org/legal/epl-v10.html
[50]: https://maven.apache.org/plugins/maven-dependency-plugin/
[51]: https://maven.apache.org/surefire/maven-failsafe-plugin/
[52]: https://www.jacoco.org/jacoco/trunk/doc/maven.html
[53]: https://github.com/exasol/error-code-crawler-maven-plugin/
[54]: https://github.com/exasol/error-code-crawler-maven-plugin/blob/main/LICENSE
[55]: http://zlika.github.io/reproducible-build-maven-plugin
[56]: http://maven.apache.org/plugins/maven-clean-plugin/
[57]: http://maven.apache.org/plugins/maven-resources-plugin/
[58]: http://maven.apache.org/plugins/maven-install-plugin/
[59]: http://maven.apache.org/plugins/maven-site-plugin/
