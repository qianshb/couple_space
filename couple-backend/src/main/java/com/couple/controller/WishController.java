package com.couple.controller;

import com.couple.dto.ApiResponse;
import com.couple.dto.WishRequest;
import com.couple.dto.WishResponse;
import com.couple.service.WishService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wish")
public class WishController {

    private final WishService wishService;

    public WishController(WishService wishService) {
        this.wishService = wishService;
    }

    @PostMapping("/create")
    public ApiResponse<WishResponse> create(@Valid @RequestBody WishRequest req,
                                            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return ApiResponse.ok(wishService.create(userId, req));
    }

    @GetMapping("/list")
    public ApiResponse<List<WishResponse>> list(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return ApiResponse.ok(wishService.list(userId));
    }

    @PutMapping("/toggle/{id}")
    public ApiResponse<WishResponse> toggle(@PathVariable Long id,
                                            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return ApiResponse.ok(wishService.toggle(userId, id));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<?> delete(@PathVariable Long id,
                                 HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        wishService.delete(userId, id);
        return ApiResponse.ok();
    }
}
