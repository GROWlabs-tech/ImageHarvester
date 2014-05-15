package eu.europeana.harvester.domain;

import org.joda.time.Duration;

import java.util.UUID;

/**
 * Describes the link check limits. Fow now these are global limits that apply to all link checking in all collections.
 */
public class LinkCheckLimits {

    private final String id;

    /**
     * The bandwidth limit usage for write (ie. sending). Measured in bytes. 0 means no limit.
     */
    private final Long bandwidthLimitWriteInBytesPerSec;

    /**
     * The bandwidth limit usage for read (ie. receiving). Measured in bytes. 0 means no limit.
     */
    private final Long bandwidthLimitReadInBytesPerSec;

    /**
     * The time threshold after which the retrieval is terminated. 0 means no limit.
     */
    private final Duration terminationThresholdTimeLimit;

    /**
     * The content size threshold after which the retrieval is terminated. 0 means no limit.
     */
    private final Long terminationThresholdSizeLimitInBytes;

    public LinkCheckLimits() {
        this.id = null;
        this.bandwidthLimitWriteInBytesPerSec = null;
        this.bandwidthLimitReadInBytesPerSec = null;
        this.terminationThresholdTimeLimit = null;
        this.terminationThresholdSizeLimitInBytes = null;
    }

    public LinkCheckLimits(Long bandwidthLimitWriteInBytesPerSec, Long bandwidthLimitReadInBytesPerSec,
                           Duration terminationThresholdTimeLimit, Long terminationThresholdSizeLimitInBytes) {
        this.id = UUID.randomUUID().toString();
        this.bandwidthLimitWriteInBytesPerSec = bandwidthLimitWriteInBytesPerSec;
        this.bandwidthLimitReadInBytesPerSec = bandwidthLimitReadInBytesPerSec;
        this.terminationThresholdTimeLimit = terminationThresholdTimeLimit;
        this.terminationThresholdSizeLimitInBytes = terminationThresholdSizeLimitInBytes;
    }

    public LinkCheckLimits(String id, Long bandwidthLimitWriteInBytesPerSec, Long bandwidthLimitReadInBytesPerSec,
                           Duration terminationThresholdTimeLimit, Long terminationThresholdSizeLimitInBytes) {
        this.id = id;
        this.bandwidthLimitWriteInBytesPerSec = bandwidthLimitWriteInBytesPerSec;
        this.bandwidthLimitReadInBytesPerSec = bandwidthLimitReadInBytesPerSec;
        this.terminationThresholdTimeLimit = terminationThresholdTimeLimit;
        this.terminationThresholdSizeLimitInBytes = terminationThresholdSizeLimitInBytes;
    }

    public String getId() {
        return id;
    }

    public Long getBandwidthLimitWriteInBytesPerSec() {
        return bandwidthLimitWriteInBytesPerSec;
    }

    public Long getBandwidthLimitReadInBytesPerSec() {
        return bandwidthLimitReadInBytesPerSec;
    }

    public Duration getTerminationThresholdTimeLimit() {
        return terminationThresholdTimeLimit;
    }

    public Long getTerminationThresholdSizeLimitInBytes() {
        return terminationThresholdSizeLimitInBytes;
    }
}
