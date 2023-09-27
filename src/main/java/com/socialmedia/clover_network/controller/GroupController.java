package com.socialmedia.clover_network.controller;

import com.socialmedia.clover_network.dto.req.GroupReq;
import com.socialmedia.clover_network.dto.res.ApiResponse;
import com.socialmedia.clover_network.service.GroupService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RestController
@RequestMapping(path = "/api/group")
public class GroupController {
    private final GroupService groupService;

    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    @PostMapping("/create-new-group")
    public ResponseEntity<ApiResponse> createNewGroup(@RequestBody GroupReq groupReq) throws Exception {
        ApiResponse res = groupService.createNewGroup(groupReq);
        if (Objects.nonNull(res)) {
            return ResponseEntity.ok().body(res);
        } else {
            return ResponseEntity.internalServerError().body(null);
        }
    }

    @GetMapping("/list-all-group-of-user")
    public ResponseEntity<ApiResponse> getListAllGroupOfUser() throws Exception {
        ApiResponse res = groupService.getListAllGroupOfUser();
        if (Objects.nonNull(res)) {
            return ResponseEntity.ok().body(res);
        } else {
            return ResponseEntity.internalServerError().body(null);
        }
    }

    @GetMapping(value = "/join", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ApiResponse> joinGroup(@RequestParam(name = "groupId") String groupId) {
        ApiResponse res = groupService.getListAllGroupOfUser();
        if (Objects.nonNull(res)) {
            return ResponseEntity.ok().body(res);
        } else {
            return ResponseEntity.internalServerError().body(null);
        }
    }
}
