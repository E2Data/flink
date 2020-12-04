package org.apache.flink.runtime.e2data;

import org.apache.flink.runtime.rest.messages.DashboardConfiguration;
import org.apache.flink.runtime.util.EnvironmentInformation;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonCreator;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.flink.util.Preconditions;

import java.time.ZonedDateTime;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Objects;

public class E2DataDashboardConfiguration {

	public static final String FIELD_NAME_REFRESH_INTERVAL = "refresh-interval";
	public static final String FIELD_NAME_TIMEZONE_OFFSET = "timezone-offset";
	public static final String FIELD_NAME_TIMEZONE_NAME = "timezone-name";
	public static final String FIELD_NAME_FLINK_VERSION = "flink-version";
	public static final String FIELD_NAME_FLINK_REVISION = "flink-revision";
	public static final String FIELD_NAME_FLINK_FEATURES = "features";

	public static final String FIELD_NAME_FEATURE_WEB_SUBMIT = "web-submit";

	public static final String FIELD_E2DATA_FLINK_WEB_ADDRESS = "flink-web-address";

	@JsonProperty(FIELD_NAME_REFRESH_INTERVAL)
	private final long refreshInterval;

	@JsonProperty(FIELD_NAME_TIMEZONE_NAME)
	private final String timeZoneName;

	@JsonProperty(FIELD_NAME_TIMEZONE_OFFSET)
	private final int timeZoneOffset;

	@JsonProperty(FIELD_NAME_FLINK_VERSION)
	private final String flinkVersion;

	@JsonProperty(FIELD_NAME_FLINK_REVISION)
	private final String flinkRevision;

	@JsonProperty(FIELD_NAME_FLINK_FEATURES)
	private final DashboardConfiguration.Features features;

	@JsonProperty(FIELD_E2DATA_FLINK_WEB_ADDRESS)
	private final String flinkWebAddress;

	@JsonCreator
	public E2DataDashboardConfiguration(
		@JsonProperty(FIELD_NAME_REFRESH_INTERVAL) long refreshInterval,
		@JsonProperty(FIELD_NAME_TIMEZONE_NAME) String timeZoneName,
		@JsonProperty(FIELD_NAME_TIMEZONE_OFFSET) int timeZoneOffset,
		@JsonProperty(FIELD_NAME_FLINK_VERSION) String flinkVersion,
		@JsonProperty(FIELD_NAME_FLINK_REVISION) String flinkRevision,
		@JsonProperty(FIELD_NAME_FLINK_FEATURES) DashboardConfiguration.Features features,
		@JsonProperty(FIELD_E2DATA_FLINK_WEB_ADDRESS) String flinkWebAddress) {
		this.refreshInterval = refreshInterval;
		this.timeZoneName = Preconditions.checkNotNull(timeZoneName);
		this.timeZoneOffset = timeZoneOffset;
		this.flinkVersion = Preconditions.checkNotNull(flinkVersion);
		this.flinkRevision = Preconditions.checkNotNull(flinkRevision);
		this.features = features;
		this.flinkWebAddress = flinkWebAddress;
	}

	@JsonIgnore
	public long getRefreshInterval() {
		return refreshInterval;
	}

	@JsonIgnore
	public int getTimeZoneOffset() {
		return timeZoneOffset;
	}

	@JsonIgnore
	public String getTimeZoneName() {
		return timeZoneName;
	}

	@JsonIgnore
	public String getFlinkVersion() {
		return flinkVersion;
	}

	@JsonIgnore
	public String getFlinkRevision() {
		return flinkRevision;
	}

	@JsonIgnore
	public DashboardConfiguration.Features getFeatures() {
		return features;
	}

	@JsonIgnore
	public String getFlinkWebAddress() {
		return flinkWebAddress;
	}

	/**
	 * Collection of features that are enabled/disabled.
	 */
	public static final class Features {

		@JsonProperty(FIELD_NAME_FEATURE_WEB_SUBMIT)
		private final boolean webSubmitEnabled;

		@JsonCreator
		public Features(@JsonProperty(FIELD_NAME_FEATURE_WEB_SUBMIT) boolean webSubmitEnabled) {
			this.webSubmitEnabled = webSubmitEnabled;
		}

		@JsonIgnore
		public boolean isWebSubmitEnabled() {
			return webSubmitEnabled;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}
			E2DataDashboardConfiguration.Features features = (E2DataDashboardConfiguration.Features) o;
			return webSubmitEnabled == features.webSubmitEnabled;
		}

		@Override
		public int hashCode() {
			return Objects.hash(webSubmitEnabled);
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		E2DataDashboardConfiguration that = (E2DataDashboardConfiguration) o;
		return refreshInterval == that.refreshInterval &&
			timeZoneOffset == that.timeZoneOffset &&
			Objects.equals(timeZoneName, that.timeZoneName) &&
			Objects.equals(flinkVersion, that.flinkVersion) &&
			Objects.equals(flinkRevision, that.flinkRevision) &&
			Objects.equals(features, that.features) &&
			Objects.equals(flinkWebAddress, that.flinkWebAddress);
	}

	@Override
	public int hashCode() {
		return Objects.hash(refreshInterval, timeZoneName, timeZoneOffset, flinkVersion, flinkRevision, features, flinkWebAddress);
	}

	public static E2DataDashboardConfiguration from(long refreshInterval, ZonedDateTime zonedDateTime, boolean webSubmitEnabled, String flinkWebAddress) {

		final String flinkVersion = EnvironmentInformation.getVersion();

		final EnvironmentInformation.RevisionInformation revision = EnvironmentInformation.getRevisionInformation();
		final String flinkRevision;

		if (revision != null) {
			flinkRevision = revision.commitId + " @ " + revision.commitDate;
		} else {
			flinkRevision = "unknown revision";
		}

		return new E2DataDashboardConfiguration(
			refreshInterval,
			zonedDateTime.getZone().getDisplayName(TextStyle.FULL, Locale.getDefault()),
			// convert zone date time into offset in order to not do the day light saving adaptions wrt the offset
			zonedDateTime.toOffsetDateTime().getOffset().getTotalSeconds() * 1000,
			flinkVersion,
			flinkRevision,
			new DashboardConfiguration.Features(webSubmitEnabled),
			flinkWebAddress);
	}
}
