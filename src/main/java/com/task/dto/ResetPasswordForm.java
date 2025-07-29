package com.task.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResetPasswordForm implements Serializable{

	private static final long serialVersionUID = 2644833352912412365L;
	
	private String username;
	private String newPassword;
	private String mobile;
	private String email;

}
