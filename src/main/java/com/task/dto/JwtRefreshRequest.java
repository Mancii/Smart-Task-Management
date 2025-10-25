package com.task.dto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JwtRefreshRequest implements Serializable {

private static final long serialVersionUID = 1898736269687613244L;

private String refreshToken;
}
