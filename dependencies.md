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
| [JaCoCo :: Agent][31]                           | [Eclipse Public License 2.0][32]  |

## Plugin Dependencies

| Dependency                                              | License                                        |
| ------------------------------------------------------- | ---------------------------------------------- |
| [SonarQube Scanner for Maven][33]                       | [GNU LGPL 3][34]                               |
| [Apache Maven Compiler Plugin][35]                      | [Apache License, Version 2.0][36]              |
| [Apache Maven Enforcer Plugin][37]                      | [Apache License, Version 2.0][36]              |
| [Maven Flatten Plugin][39]                              | [Apache Software Licenese][40]                 |
| [org.sonatype.ossindex.maven:ossindex-maven-plugin][41] | [ASL2][40]                                     |
| [Reproducible Build Maven Plugin][43]                   | [Apache 2.0][40]                               |
| [Maven Surefire Plugin][45]                             | [Apache License, Version 2.0][36]              |
| [Versions Maven Plugin][47]                             | [Apache License, Version 2.0][36]              |
| [Project keeper maven plugin][49]                       | [The MIT License][50]                          |
| [Apache Maven Assembly Plugin][51]                      | [Apache License, Version 2.0][36]              |
| [Apache Maven JAR Plugin][53]                           | [Apache License, Version 2.0][36]              |
| [Artifact reference checker and unifier][55]            | [MIT][5]                                       |
| [Apache Maven Deploy Plugin][57]                        | [Apache License, Version 2.0][36]              |
| [Apache Maven GPG Plugin][59]                           | [Apache License, Version 2.0][36]              |
| [Apache Maven Source Plugin][61]                        | [Apache License, Version 2.0][36]              |
| [Apache Maven Javadoc Plugin][63]                       | [Apache License, Version 2.0][36]              |
| [Nexus Staging Maven Plugin][65]                        | [Eclipse Public License][66]                   |
| [Apache Maven Dependency Plugin][67]                    | [Apache License, Version 2.0][36]              |
| [Maven Failsafe Plugin][69]                             | [Apache License, Version 2.0][36]              |
| [JaCoCo :: Maven Plugin][71]                            | [Eclipse Public License 2.0][32]               |
| [error-code-crawler-maven-plugin][73]                   | [MIT][5]                                       |
| [Maven Clean Plugin][75]                                | [The Apache Software License, Version 2.0][40] |
| [Maven Resources Plugin][77]                            | [The Apache Software License, Version 2.0][40] |
| [Maven Install Plugin][79]                              | [The Apache Software License, Version 2.0][40] |
| [Maven Site Plugin 3][81]                               | [The Apache Software License, Version 2.0][40] |

[31]: https://www.eclemma.org/jacoco/index.html
[4]: https://github.com/exasol/error-reporting-java
[40]: http://www.apache.org/licenses/LICENSE-2.0.txt
[45]: https://maven.apache.org/surefire/maven-surefire-plugin/
[3]: https://www.exasol.com/support/secure/attachment/155343/EXASOL_SDK-7.0.11.tar.gz
[75]: http://maven.apache.org/plugins/maven-clean-plugin/
[5]: https://opensource.org/licenses/MIT
[15]: https://github.com/mockito/mockito
[39]: https://www.mojohaus.org/flatten-maven-plugin/
[29]: https://github.com/exasol/maven-project-version-getter
[47]: http://www.mojohaus.org/versions-maven-plugin/
[49]: https://github.com/exasol/project-keeper/
[12]: http://opensource.org/licenses/BSD-3-Clause
[35]: https://maven.apache.org/plugins/maven-compiler-plugin/
[26]: https://github.com/exasol/test-db-builder-java/blob/main/LICENSE
[32]: https://www.eclipse.org/legal/epl-2.0/
[57]: https://maven.apache.org/plugins/maven-deploy-plugin/
[34]: http://www.gnu.org/licenses/lgpl.txt
[71]: https://www.jacoco.org/jacoco/trunk/doc/maven.html
[16]: https://github.com/mockito/mockito/blob/main/LICENSE
[21]: https://github.com/exasol/hamcrest-resultset-matcher
[43]: http://zlika.github.io/reproducible-build-maven-plugin
[24]: http://www.opensource.org/licenses/mit-license.php
[33]: http://sonarsource.github.io/sonar-scanner-maven/
[27]: https://github.com/exasol/udf-debugging-java/
[13]: https://junit.org/junit5/
[0]: https://github.com/exasol/virtual-schema-common-jdbc/
[6]: https://github.com/eclipse-ee4j/jsonp
[61]: https://maven.apache.org/plugins/maven-source-plugin/
[8]: https://projects.eclipse.org/license/secondary-gpl-2.0-cp
[11]: http://hamcrest.org/JavaHamcrest/
[23]: http://www.slf4j.org
[77]: http://maven.apache.org/plugins/maven-resources-plugin/
[55]: https://github.com/exasol/artifact-reference-checker-maven-plugin
[53]: https://maven.apache.org/plugins/maven-jar-plugin/
[25]: https://github.com/exasol/test-db-builder-java/
[65]: http://www.sonatype.com/public-parent/nexus-maven-plugins/nexus-staging/nexus-staging-maven-plugin/
[69]: https://maven.apache.org/surefire/maven-failsafe-plugin/
[20]: http://opensource.org/licenses/MIT
[66]: http://www.eclipse.org/legal/epl-v10.html
[17]: https://github.com/exasol/exasol-testcontainers
[50]: https://github.com/exasol/project-keeper/blob/main/LICENSE
[67]: https://maven.apache.org/plugins/maven-dependency-plugin/
[7]: https://projects.eclipse.org/license/epl-2.0
[36]: https://www.apache.org/licenses/LICENSE-2.0.txt
[37]: https://maven.apache.org/enforcer/maven-enforcer-plugin/
[2]: http://www.exasol.com
[14]: https://www.eclipse.org/legal/epl-v20.html
[1]: https://github.com/exasol/virtual-schema-common-jdbc/blob/main/LICENSE
[79]: http://maven.apache.org/plugins/maven-install-plugin/
[41]: https://sonatype.github.io/ossindex-maven/maven-plugin/
[59]: https://maven.apache.org/plugins/maven-gpg-plugin/
[19]: https://testcontainers.org
[81]: http://maven.apache.org/plugins/maven-site-plugin/
[63]: https://maven.apache.org/plugins/maven-javadoc-plugin/
[73]: https://github.com/exasol/error-code-crawler-maven-plugin
[51]: https://maven.apache.org/plugins/maven-assembly-plugin/
