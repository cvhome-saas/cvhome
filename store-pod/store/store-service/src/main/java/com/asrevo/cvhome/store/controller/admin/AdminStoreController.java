package com.asrevo.cvhome.store.controller.admin;

import com.asrevo.cvhome.storepod.commons.dto.CreateStoreResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/admin/store")
@AllArgsConstructor
@Slf4j
public class AdminStoreController {
    @PostMapping("create")
    public CreateStoreResponse create(@RequestBody CreateStoreResponse dto) {
        return new CreateStoreResponse(dto.name());
    }
}
