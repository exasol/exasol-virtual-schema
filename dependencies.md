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
| [Hamcrest][11]                                  | [BSD License 3][12]               |
| [JUnit Jupiter (Aggregator)][13]                | [Eclipse Public License v2.0][14] |
| [mockito-junit-jupiter][15]                     | [The MIT License][16]             |
| [Test containers for Exasol on Docker][17]      | [MIT][5]                          |
| [Testcontainers :: JUnit Jupiter Extension][19] | [MIT][20]                         |
| [Matcher for SQL Result Sets][21]               | [MIT][5]                          |
| [SLF4J JDK14 Binding][23]                       | [MIT License][24]                 |
| [Test Database Builder for Java][25]            | [MIT License][26]                 |
| [udf-debugging-java][27]                        | [MIT][5]                          |
| [Maven Project Version Getter][29]              | [MIT][5]                          |
| [exasol-test-setup-abstraction-java][31]        | [MIT License][32]                 |
| [JaCoCo :: Agent][33]                           | [Eclipse Public License 2.0][34]  |

## Plugin Dependencies

| Dependency                                              | License                                        |
| ------------------------------------------------------- | ---------------------------------------------- |
| [SonarQube Scanner for Maven][35]                       | [GNU LGPL 3][36]                               |
| [Apache Maven Compiler Plugin][37]                      | [Apache License, Version 2.0][38]              |
| [Apache Maven Enforcer Plugin][39]                      | [Apache License, Version 2.0][38]              |
| [Maven Flatten Plugin][41]                              | [Apache Software Licenese][42]                 |
| [org.sonatype.ossindex.maven:ossindex-maven-plugin][43] | [ASL2][42]                                     |
| [Reproducible Build Maven Plugin][45]                   | [Apache 2.0][42]                               |
| [Maven Surefire Plugin][47]                             | [Apache License, Version 2.0][38]              |
| [Versions Maven Plugin][49]                             | [Apache License, Version 2.0][38]              |
| [Project keeper maven plugin][51]                       | [The MIT License][52]                          |
| [Apache Maven Assembly Plugin][53]                      | [Apache License, Version 2.0][38]              |
| [Apache Maven JAR Plugin][55]                           | [Apache License, Version 2.0][38]              |
| [Artifact reference checker and unifier][57]            | [MIT][5]                                       |
| [Apache Maven Deploy Plugin][59]                        | [Apache License, Version 2.0][38]              |
| [Apache Maven GPG Plugin][61]                           | [Apache License, Version 2.0][38]              |
| [Apache Maven Source Plugin][63]                        | [Apache License, Version 2.0][38]              |
| [Apache Maven Javadoc Plugin][65]                       | [Apache License, Version 2.0][38]              |
| [Nexus Staging Maven Plugin][67]                        | [Eclipse Public License][68]                   |
| [Apache Maven Dependency Plugin][69]                    | [Apache License, Version 2.0][38]              |
| [Maven Failsafe Plugin][71]                             | [Apache License, Version 2.0][38]              |
| [JaCoCo :: Maven Plugin][73]                            | [Eclipse Public License 2.0][34]               |
| [error-code-crawler-maven-plugin][75]                   | [MIT][5]                                       |
| [Maven Clean Plugin][77]                                | [The Apache Software License, Version 2.0][42] |
| [Maven Resources Plugin][79]                            | [The Apache Software License, Version 2.0][42] |
| [Maven Install Plugin][81]                              | [The Apache Software License, Version 2.0][42] |
| [Maven Site Plugin 3][83]                               | [The Apache Software License, Version 2.0][42] |

[33]: https://www.eclemma.org/jacoco/index.html
[4]: https://github.com/exasol/error-reporting-java
[42]: http://www.apache.org/licenses/LICENSE-2.0.txt
[47]: https://maven.apache.org/surefire/maven-surefire-plugin/
[3]: https://www.exasol.com/support/secure/attachment/155343/EXASOL_SDK-7.0.11.tar.gz
[77]: http://maven.apache.org/plugins/maven-clean-plugin/
[5]: https://opensource.org/licenses/MIT
[15]: https://github.com/mockito/mockito
[41]: https://www.mojohaus.org/flatten-maven-plugin/
[29]: https://github.com/exasol/maven-project-version-getter
[49]: http://www.mojohaus.org/versions-maven-plugin/
[51]: https://github.com/exasol/project-keeper/
[12]: http://opensource.org/licenses/BSD-3-Clause
[37]: https://maven.apache.org/plugins/maven-compiler-plugin/
[26]: https://github.com/exasol/test-db-builder-java/blob/main/LICENSE
[31]: https://github.com/exasol/exasol-test-setup-abstraction-java/
[34]: https://www.eclipse.org/legal/epl-2.0/
[59]: https://maven.apache.org/plugins/maven-deploy-plugin/
[36]: http://www.gnu.org/licenses/lgpl.txt
[73]: https://www.jacoco.org/jacoco/trunk/doc/maven.html
[16]: https://github.com/mockito/mockito/blob/main/LICENSE
[21]: https://github.com/exasol/hamcrest-resultset-matcher
[45]: http://zlika.github.io/reproducible-build-maven-plugin
[32]: https://github.com/exasol/exasol-test-setup-abstraction-java/blob/main/LICENSE
[24]: http://www.opensource.org/licenses/mit-license.php
[35]: http://sonarsource.github.io/sonar-scanner-maven/
[27]: https://github.com/exasol/udf-debugging-java/
[13]: https://junit.org/junit5/
[0]: https://github.com/exasol/virtual-schema-common-jdbc/
[6]: https://github.com/eclipse-ee4j/jsonp
[63]: https://maven.apache.org/plugins/maven-source-plugin/
[8]: https://projects.eclipse.org/license/secondary-gpl-2.0-cp
[11]: http://hamcrest.org/JavaHamcrest/
[23]: http://www.slf4j.org
[79]: http://maven.apache.org/plugins/maven-resources-plugin/
[57]: https://github.com/exasol/artifact-reference-checker-maven-plugin
[55]: https://maven.apache.org/plugins/maven-jar-plugin/
[25]: https://github.com/exasol/test-db-builder-java/
[67]: http://www.sonatype.com/public-parent/nexus-maven-plugins/nexus-staging/nexus-staging-maven-plugin/
[71]: https://maven.apache.org/surefire/maven-failsafe-plugin/
[20]: http://opensource.org/licenses/MIT
[68]: http://www.eclipse.org/legal/epl-v10.html
[17]: https://github.com/exasol/exasol-testcontainers
[52]: https://github.com/exasol/project-keeper/blob/main/LICENSE
[69]: https://maven.apache.org/plugins/maven-dependency-plugin/
[7]: https://projects.eclipse.org/license/epl-2.0
[38]: https://www.apache.org/licenses/LICENSE-2.0.txt
[39]: https://maven.apache.org/enforcer/maven-enforcer-plugin/
[2]: http://www.exasol.com
[14]: https://www.eclipse.org/legal/epl-v20.html
[1]: https://github.com/exasol/virtual-schema-common-jdbc/blob/main/LICENSE
[81]: http://maven.apache.org/plugins/maven-install-plugin/
[43]: https://sonatype.github.io/ossindex-maven/maven-plugin/
[61]: https://maven.apache.org/plugins/maven-gpg-plugin/
[19]: https://testcontainers.org
[83]: http://maven.apache.org/plugins/maven-site-plugin/
[65]: https://maven.apache.org/plugins/maven-javadoc-plugin/
[75]: https://github.com/exasol/error-code-crawler-maven-plugin
[53]: https://maven.apache.org/plugins/maven-assembly-plugin/
