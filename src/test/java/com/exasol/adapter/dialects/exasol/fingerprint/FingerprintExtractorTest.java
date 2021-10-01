package com.exasol.adapter.dialects.exasol.fingerprint;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class FingerprintExtractorTest {

    @Test
    void testExtractFingerprintFromLocalhostUrl() {
        assertThat(FingerprintExtractor.extractFingerprint(
                "jdbc:exa:localhost/fingerprint:1234;validateservercertificate=1"), equalTo("fingerprint"));
    }

    @Test
    void testExtractFingerprint() {
        assertThat(FingerprintExtractor.extractFingerprint(
                "jdbc:exa:127.0.0.1/fingerprint:1234;validateservercertificate=1"), equalTo("fingerprint"));
    }

    @Test
    void testExtractFingerprintFailed() {
        assertThrows(IllegalStateException.class,
                () -> FingerprintExtractor.extractFingerprint("jdbc:exa:127.0.0.1:1234;validateservercertificate=1"));
    }

}
