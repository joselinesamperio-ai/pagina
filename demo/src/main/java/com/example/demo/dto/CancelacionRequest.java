package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;

public record CancelacionRequest(@NotBlank String password) {
}
