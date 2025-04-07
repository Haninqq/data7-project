package com.data7.instdesign.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Log4j2
public class RegisterDTO {
    private String userId;
    private String password;
    private String email;
    private String name;
    private String phone;
}
