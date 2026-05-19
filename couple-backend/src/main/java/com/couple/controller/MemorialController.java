package com.couple.controller;

import com.couple.dto.ApiResponse;
import com.couple.dto.MemorialRequest;
import com.couple.dto.MemorialResponse;
import com.couple.service.MemorialService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/memorial")
public class MemorialController {

    private final MemorialService memorialService;

    public MemorialController(MemorialService memorialService) {
        this.memorialService = memorialService;
    }

    @PostMapping("/create")
    public ApiResponse<MemorialResponse> create(@Valid @RequestBody MemorialRequest req,
                                                HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return ApiResponse.ok(memorialService.create(userId, req));
    }

    @GetMapping("/list")
    public ApiResponse<List<MemorialResponse>> list(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return ApiResponse.ok(memorialService.list(userId));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<?> delete(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        memorialService.delete(userId, id);
        return ApiResponse.ok();
    }
}
