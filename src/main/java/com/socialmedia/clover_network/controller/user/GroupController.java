package com.socialmedia.clover_network.controller.user;

import com.socialmedia.clover_network.config.AuthenticationHelper;
import com.socialmedia.clover_network.dto.req.GroupReq;
import com.socialmedia.clover_network.dto.res.ApiResponse;
import com.socialmedia.clover_network.enumuration.GroupMemberRole;
import com.socialmedia.clover_network.service.GroupService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RestController
@RequestMapping(path = "/api/group")
public class GroupController {

    private final Logger logger = LoggerFactory.getLogger(GroupController.class);

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

    @GetMapping("/get-group-info/{groupId}")
    public ResponseEntity<ApiResponse> getGroupInfo(@PathVariable String groupId) throws Exception {
        ApiResponse res = groupService.getGroupInfo(groupId);
        if (Objects.nonNull(res)) {
            return ResponseEntity.ok().body(res);
        } else {
            return ResponseEntity.internalServerError().body(null);
        }
    }

    @PostMapping(value = "/join", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ApiResponse> joinGroup(@RequestParam(name = "groupId") String groupId) {
        String currentUserId = AuthenticationHelper.getUserIdFromContext();
        if (currentUserId != null) {
            ApiResponse res = groupService.joinGroup(groupId, currentUserId);
            return ResponseEntity.ok().body(res);
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/list-member-group")
    public ResponseEntity<ApiResponse> listMemberOfGroup(@RequestParam(name = "groupId") String groupId,
                                                       @RequestParam(name = "roleId") GroupMemberRole roleId,
                                                       @RequestParam(name = "searchKey", defaultValue = "") String searchKey,
                                                       @RequestParam(name = "page") int page,
                                                       @RequestParam(name = "size") int size) {
        try {
            ApiResponse res;
            if (StringUtils.isEmpty(searchKey)) {
                res = groupService.getListMemberOfGroup(groupId, roleId, page, size);
            } else {
                res = groupService.searchMemberOfGroup(groupId, roleId, page, size, searchKey);
            }
            return ResponseEntity.ok().body(res);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseEntity.internalServerError().body(null);
        }
    }
}
