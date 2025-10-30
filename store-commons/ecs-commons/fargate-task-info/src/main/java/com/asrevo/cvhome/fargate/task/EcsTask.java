package com.asrevo.cvhome.fargate.task;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EcsTask {

	@JsonProperty("Cluster")
	private String cluster;

	@JsonProperty("TaskARN")
	private String taskARN;

	@JsonProperty("Family")
	private String family;

	@JsonProperty("Revision")
	private String revision;

	@JsonProperty("DesiredStatus")
	private String desiredStatus;

	@JsonProperty("KnownStatus")
	private String knownStatus;

	@JsonProperty("Limits")
	private Limits limits;

	@JsonProperty("PullStartedAt")
	private String pullStartedAt;

	@JsonProperty("PullStoppedAt")
	private String pullStoppedAt;

	@JsonProperty("AvailabilityZone")
	private String availabilityZone;

	@JsonProperty("LaunchType")
	private String launchType;

	@JsonProperty("Containers")
	private ArrayList<Container> containers;

	@JsonProperty("ClockDrift")
	private ClockDrift clockDrift;

	@JsonProperty("EphemeralStorageMetrics")
	private EphemeralStorageMetrics ephemeralStorageMetrics;

}
