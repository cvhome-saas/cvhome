package com.asrevo.cvhome.certificatemanager.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.net.URL;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderChallenges {

    private URL location;

    private Map<String, Map<String, String>> challenges;
}
