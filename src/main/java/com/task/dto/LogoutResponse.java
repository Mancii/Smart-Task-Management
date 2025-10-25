package com.task.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LogoutResponse implements Serializable {

	private static final long serialVersionUID = 6646058894527565826L;

	private String status;

}
