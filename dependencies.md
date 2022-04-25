<!-- @formatter:off -->
# Dependencies

## Compile Dependencies

| Dependency                      | License                |
| ------------------------------- | ---------------------- |
| [Virtual Schema Common JDBC][0] | [MIT][1]               |
| [EXASolution JDBC Driver][2]    | [EXAClient License][3] |
| [error-reporting-java][4]       | [MIT][1]               |

## Test Dependencies

| Dependency                                      | License                           |
| ----------------------------------------------- | --------------------------------- |
| [Virtual Schema Common JDBC][0]                 | [MIT][1]                          |
| [Hamcrest][8]                                   | [BSD License 3][9]                |
| [JUnit Jupiter (Aggregator)][10]                | [Eclipse Public License v2.0][11] |
| [mockito-junit-jupiter][12]                     | [The MIT License][13]             |
| [JaCoCo :: Agent][14]                           | [Eclipse Public License 2.0][15]  |
| [Test containers for Exasol on Docker][16]      | [MIT][1]                          |
| [Testcontainers :: JUnit Jupiter Extension][18] | [MIT][19]                         |
| [Matcher for SQL Result Sets][20]               | [MIT][1]                          |
| [SLF4J JDK14 Binding][22]                       | [MIT License][23]                 |
| [Test Database Builder for Java][24]            | [MIT License][25]                 |
| [udf-debugging-java][26]                        | [MIT][1]                          |
| [Maven Project Version Getter][28]              | [MIT][1]                          |

## Plugin Dependencies

| Dependency                                              | License                           |
| ------------------------------------------------------- | --------------------------------- |
| [Maven Surefire Plugin][30]                             | [Apache License, Version 2.0][31] |
| [JaCoCo :: Maven Plugin][32]                            | [Eclipse Public License 2.0][15]  |
| [Project keeper maven plugin][34]                       | [MIT][1]                          |
| [Apache Maven Compiler Plugin][36]                      | [Apache License, Version 2.0][31] |
| [Apache Maven Assembly Plugin][38]                      | [Apache License, Version 2.0][31] |
| [Maven Failsafe Plugin][40]                             | [Apache License, Version 2.0][31] |
| [Apache Maven JAR Plugin][42]                           | [Apache License, Version 2.0][31] |
| [Apache Maven Source Plugin][44]                        | [Apache License, Version 2.0][31] |
| [Apache Maven Javadoc Plugin][46]                       | [Apache License, Version 2.0][31] |
| [Apache Maven GPG Plugin][48]                           | [Apache License, Version 2.0][31] |
| [org.sonatype.ossindex.maven:ossindex-maven-plugin][50] | [ASL2][51]                        |
| [Versions Maven Plugin][52]                             | [Apache License, Version 2.0][31] |
| [Apache Maven Enforcer Plugin][54]                      | [Apache License, Version 2.0][31] |
| [Artifact reference checker and unifier][56]            | [MIT][1]                          |
| [Apache Maven Deploy Plugin][58]                        | [Apache License, Version 2.0][31] |
| [Nexus Staging Maven Plugin][60]                        | [Eclipse Public License][61]      |
| [Apache Maven Dependency Plugin][62]                    | [Apache License, Version 2.0][31] |
| [error-code-crawler-maven-plugin][64]                   | [MIT][1]                          |
| [Reproducible Build Maven Plugin][66]                   | [Apache 2.0][51]                  |
| [Apache Maven Clean Plugin][68]                         | [Apache License, Version 2.0][31] |
| [Apache Maven Resources Plugin][70]                     | [Apache License, Version 2.0][31] |
| [Apache Maven Install Plugin][72]                       | [Apache License, Version 2.0][31] |
| [Apache Maven Site Plugin][74]                          | [Apache License, Version 2.0][31] |

[14]: https://www.eclemma.org/jacoco/index.html
[34]: https://github.com/exasol/project-keeper-maven-plugin
[4]: https://github.com/exasol/error-reporting-java
[51]: http://www.apache.org/licenses/LICENSE-2.0.txt
[30]: https://maven.apache.org/surefire/maven-surefire-plugin/
[3]: https://www.exasol.com/support/secure/attachment/155343/EXASOL_SDK-7.0.11.tar.gz
[1]: https://opensource.org/licenses/MIT
[12]: https://github.com/mockito/mockito
[28]: https://github.com/exasol/maven-project-version-getter
[52]: http://www.mojohaus.org/versions-maven-plugin/
[9]: http://opensource.org/licenses/BSD-3-Clause
[36]: https://maven.apache.org/plugins/maven-compiler-plugin/
[70]: https://maven.apache.org/plugins/maven-resources-plugin/
[25]: https://github.com/exasol/test-db-builder-java/blob/main/LICENSE
[68]: https://maven.apache.org/plugins/maven-clean-plugin/
[15]: https://www.eclipse.org/legal/epl-2.0/
[58]: https://maven.apache.org/plugins/maven-deploy-plugin/
[32]: https://www.jacoco.org/jacoco/trunk/doc/maven.html
[13]: https://github.com/mockito/mockito/blob/main/LICENSE
[20]: https://github.com/exasol/hamcrest-resultset-matcher
[66]: http://zlika.github.io/reproducible-build-maven-plugin
[23]: http://www.opensource.org/licenses/mit-license.php
[72]: https://maven.apache.org/plugins/maven-install-plugin/
[10]: https://junit.org/junit5/
[44]: https://maven.apache.org/plugins/maven-source-plugin/
[8]: http://hamcrest.org/JavaHamcrest/
[22]: http://www.slf4j.org
[56]: https://github.com/exasol/artifact-reference-checker-maven-plugin
[42]: https://maven.apache.org/plugins/maven-jar-plugin/
[24]: https://github.com/exasol/test-db-builder-java/
[60]: http://www.sonatype.com/public-parent/nexus-maven-plugins/nexus-staging/nexus-staging-maven-plugin/
[40]: https://maven.apache.org/surefire/maven-failsafe-plugin/
[19]: http://opensource.org/licenses/MIT
[0]: https://github.com/exasol/virtual-schema-common-jdbc
[61]: http://www.eclipse.org/legal/epl-v10.html
[16]: https://github.com/exasol/exasol-testcontainers
[62]: https://maven.apache.org/plugins/maven-dependency-plugin/
[74]: https://maven.apache.org/plugins/maven-site-plugin/
[31]: https://www.apache.org/licenses/LICENSE-2.0.txt
[54]: https://maven.apache.org/enforcer/maven-enforcer-plugin/
[2]: http://www.exasol.com
[11]: https://www.eclipse.org/legal/epl-v20.html
[50]: https://sonatype.github.io/ossindex-maven/maven-plugin/
[48]: https://maven.apache.org/plugins/maven-gpg-plugin/
[18]: https://testcontainers.org
[26]: https://github.com/exasol/udf-debugging-java
[46]: https://maven.apache.org/plugins/maven-javadoc-plugin/
[64]: https://github.com/exasol/error-code-crawler-maven-plugin
[38]: https://maven.apache.org/plugins/maven-assembly-plugin/
