package com.couple.controller;

import com.couple.dto.*;
import com.couple.service.CoupleService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/couple")
public class CoupleController {

    private final CoupleService coupleService;

    public CoupleController(CoupleService coupleService) {
        this.coupleService = coupleService;
    }

    @PostMapping("/invite")
    public ApiResponse<Map<String, String>> createInvite(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String code = coupleService.generateInviteCode(userId);
        return ApiResponse.ok(Map.of("inviteCode", code));
    }

    @PostMapping("/bind")
    public ApiResponse<?> bind(@Valid @RequestBody BindRequest req,
                               HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        coupleService.bind(userId, req.getInviteCode());
        return ApiResponse.ok();
    }

    @GetMapping("/status")
    public ApiResponse<CoupleStatusResponse> status(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return ApiResponse.ok(coupleService.getStatus(userId));
    }
}
