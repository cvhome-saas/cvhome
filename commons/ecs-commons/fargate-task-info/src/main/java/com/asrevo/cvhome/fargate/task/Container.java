package com.asrevo.cvhome.fargate.task;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Map;

@Getter
@Setter
public class Container {
    @JsonProperty("DockerId")
    private String dockerId;
    @JsonProperty("Name")
    private String name;
    @JsonProperty("DockerName")
    private String dockerName;
    @JsonProperty("Image")
    private String image;
    @JsonProperty("ImageID")
    private String imageID;
    @JsonProperty("Labels")
    private Map<String, String> labels;
    @JsonProperty("DesiredStatus")
    private String desiredStatus;
    @JsonProperty("KnownStatus")
    private String knownStatus;
    @JsonProperty("Limits")
    private Limits limits;
    @JsonProperty("CreatedAt")
    private String createdAt;
    @JsonProperty("StartedAt")
    private String startedAt;
    @JsonProperty("LastRestartAt")
    private String lastRestartAt;
    @JsonProperty("Type")
    private String type;
    @JsonProperty("LogDriver")
    private String logDriver;
    @JsonProperty("LogOptions")
    private Map<String, String> logOptions;
    @JsonProperty("ContainerARN")
    private String containerARN;
    @JsonProperty("Networks")
    private ArrayList<Network> networks;
    @JsonProperty("Snapshotter")
    private String snapshotter;
    @JsonProperty("Health")
    private Health health;
}
