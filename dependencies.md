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
| [Test containers for Exasol on Docker][14]      | [MIT][1]                          |
| [Testcontainers :: JUnit Jupiter Extension][16] | [MIT][17]                         |
| [Matcher for SQL Result Sets][18]               | [MIT][1]                          |
| [SLF4J JDK14 Binding][20]                       | [MIT License][21]                 |
| [Test Database Builder for Java][22]            | [MIT License][23]                 |
| [udf-debugging-java][24]                        | [MIT][1]                          |
| [Maven Project Version Getter][26]              | [MIT][1]                          |
| [JaCoCo :: Agent][28]                           | [Eclipse Public License 2.0][29]  |

## Plugin Dependencies

| Dependency                                              | License                                        |
| ------------------------------------------------------- | ---------------------------------------------- |
| [SonarQube Scanner for Maven][30]                       | [GNU LGPL 3][31]                               |
| [Apache Maven Compiler Plugin][32]                      | [Apache License, Version 2.0][33]              |
| [Apache Maven Enforcer Plugin][34]                      | [Apache License, Version 2.0][33]              |
| [Maven Flatten Plugin][36]                              | [Apache Software Licenese][37]                 |
| [org.sonatype.ossindex.maven:ossindex-maven-plugin][38] | [ASL2][37]                                     |
| [Reproducible Build Maven Plugin][40]                   | [Apache 2.0][37]                               |
| [Maven Surefire Plugin][42]                             | [Apache License, Version 2.0][33]              |
| [Versions Maven Plugin][44]                             | [Apache License, Version 2.0][33]              |
| [Project keeper maven plugin][46]                       | [The MIT License][47]                          |
| [Apache Maven Assembly Plugin][48]                      | [Apache License, Version 2.0][33]              |
| [Apache Maven JAR Plugin][50]                           | [Apache License, Version 2.0][33]              |
| [Artifact reference checker and unifier][52]            | [MIT][1]                                       |
| [Apache Maven Deploy Plugin][54]                        | [Apache License, Version 2.0][33]              |
| [Apache Maven GPG Plugin][56]                           | [Apache License, Version 2.0][33]              |
| [Apache Maven Source Plugin][58]                        | [Apache License, Version 2.0][33]              |
| [Apache Maven Javadoc Plugin][60]                       | [Apache License, Version 2.0][33]              |
| [Nexus Staging Maven Plugin][62]                        | [Eclipse Public License][63]                   |
| [Apache Maven Dependency Plugin][64]                    | [Apache License, Version 2.0][33]              |
| [Maven Failsafe Plugin][66]                             | [Apache License, Version 2.0][33]              |
| [JaCoCo :: Maven Plugin][68]                            | [Eclipse Public License 2.0][29]               |
| [error-code-crawler-maven-plugin][70]                   | [MIT][1]                                       |
| [Maven Clean Plugin][72]                                | [The Apache Software License, Version 2.0][37] |
| [Maven Resources Plugin][74]                            | [The Apache Software License, Version 2.0][37] |
| [Maven Install Plugin][76]                              | [The Apache Software License, Version 2.0][37] |
| [Maven Site Plugin 3][78]                               | [The Apache Software License, Version 2.0][37] |

[28]: https://www.eclemma.org/jacoco/index.html
[4]: https://github.com/exasol/error-reporting-java
[37]: http://www.apache.org/licenses/LICENSE-2.0.txt
[42]: https://maven.apache.org/surefire/maven-surefire-plugin/
[3]: https://www.exasol.com/support/secure/attachment/155343/EXASOL_SDK-7.0.11.tar.gz
[72]: http://maven.apache.org/plugins/maven-clean-plugin/
[1]: https://opensource.org/licenses/MIT
[12]: https://github.com/mockito/mockito
[36]: https://www.mojohaus.org/flatten-maven-plugin/
[26]: https://github.com/exasol/maven-project-version-getter
[44]: http://www.mojohaus.org/versions-maven-plugin/
[46]: https://github.com/exasol/project-keeper/
[9]: http://opensource.org/licenses/BSD-3-Clause
[32]: https://maven.apache.org/plugins/maven-compiler-plugin/
[23]: https://github.com/exasol/test-db-builder-java/blob/main/LICENSE
[29]: https://www.eclipse.org/legal/epl-2.0/
[54]: https://maven.apache.org/plugins/maven-deploy-plugin/
[31]: http://www.gnu.org/licenses/lgpl.txt
[68]: https://www.jacoco.org/jacoco/trunk/doc/maven.html
[13]: https://github.com/mockito/mockito/blob/main/LICENSE
[18]: https://github.com/exasol/hamcrest-resultset-matcher
[40]: http://zlika.github.io/reproducible-build-maven-plugin
[21]: http://www.opensource.org/licenses/mit-license.php
[30]: http://sonarsource.github.io/sonar-scanner-maven/
[24]: https://github.com/exasol/udf-debugging-java/
[10]: https://junit.org/junit5/
[58]: https://maven.apache.org/plugins/maven-source-plugin/
[8]: http://hamcrest.org/JavaHamcrest/
[20]: http://www.slf4j.org
[74]: http://maven.apache.org/plugins/maven-resources-plugin/
[52]: https://github.com/exasol/artifact-reference-checker-maven-plugin
[50]: https://maven.apache.org/plugins/maven-jar-plugin/
[22]: https://github.com/exasol/test-db-builder-java/
[62]: http://www.sonatype.com/public-parent/nexus-maven-plugins/nexus-staging/nexus-staging-maven-plugin/
[66]: https://maven.apache.org/surefire/maven-failsafe-plugin/
[17]: http://opensource.org/licenses/MIT
[0]: https://github.com/exasol/virtual-schema-common-jdbc
[63]: http://www.eclipse.org/legal/epl-v10.html
[14]: https://github.com/exasol/exasol-testcontainers
[47]: https://github.com/exasol/project-keeper/blob/main/LICENSE
[64]: https://maven.apache.org/plugins/maven-dependency-plugin/
[33]: https://www.apache.org/licenses/LICENSE-2.0.txt
[34]: https://maven.apache.org/enforcer/maven-enforcer-plugin/
[2]: http://www.exasol.com
[11]: https://www.eclipse.org/legal/epl-v20.html
[76]: http://maven.apache.org/plugins/maven-install-plugin/
[38]: https://sonatype.github.io/ossindex-maven/maven-plugin/
[56]: https://maven.apache.org/plugins/maven-gpg-plugin/
[16]: https://testcontainers.org
[78]: http://maven.apache.org/plugins/maven-site-plugin/
[60]: https://maven.apache.org/plugins/maven-javadoc-plugin/
[70]: https://github.com/exasol/error-code-crawler-maven-plugin
[48]: https://maven.apache.org/plugins/maven-assembly-plugin/
