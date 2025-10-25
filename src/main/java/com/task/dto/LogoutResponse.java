package com.task.dto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LogoutResponse implements Serializable {

private static final long serialVersionUID = 6646058894527565826L;

private String status;
}
