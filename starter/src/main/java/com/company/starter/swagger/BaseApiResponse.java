package com.company.starter.swagger;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD})
@ApiResponse(responseCode = "200", content = @Content)
@ApiResponse(responseCode = "202", content = @Content)
@ApiResponse(responseCode = "204", content = @Content)
@ApiResponse(responseCode = "400", content = @Content)
@ApiResponse(responseCode = "401", content = @Content)
@ApiResponse(responseCode = "403", content = @Content)
@ApiResponse(responseCode = "404", content = @Content)
@ApiResponse(responseCode = "405", content = @Content)
@ApiResponse(responseCode = "408", content = @Content)
@ApiResponse(responseCode = "409", content = @Content)
@ApiResponse(responseCode = "413", content = @Content)
@ApiResponse(responseCode = "415", content = @Content)
@ApiResponse(responseCode = "422", content = @Content)
@ApiResponse(responseCode = "429", content = @Content)
@ApiResponse(responseCode = "500", content = @Content)
@ApiResponse(responseCode = "501", content = @Content)
@ApiResponse(responseCode = "502", content = @Content)
@ApiResponse(responseCode = "503", content = @Content)
@ApiResponse(responseCode = "504", content = @Content)
public @interface BaseApiResponse {
}
