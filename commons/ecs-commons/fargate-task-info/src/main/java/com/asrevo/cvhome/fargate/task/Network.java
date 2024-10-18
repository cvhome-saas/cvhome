package com.asrevo.cvhome.fargate.task;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Network {
    @JsonProperty("NetworkMode")
    private String networkMode;

    @JsonProperty("IPv4Addresses")
    private ArrayList<String> iPv4Addresses;

    @JsonProperty("AttachmentIndex")
    private int attachmentIndex;

    @JsonProperty("MACAddress")
    private String mACAddress;

    @JsonProperty("IPv4SubnetCIDRBlock")
    private String iPv4SubnetCIDRBlock;

    @JsonProperty("DomainNameServers")
    private ArrayList<String> domainNameServers;

    @JsonProperty("DomainNameSearchList")
    private ArrayList<String> domainNameSearchList;

    @JsonProperty("PrivateDNSName")
    private String privateDNSName;

    @JsonProperty("SubnetGatewayIpv4Address")
    private String subnetGatewayIpv4Address;
}
