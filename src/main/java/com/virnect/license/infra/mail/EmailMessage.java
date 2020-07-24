package com.virnect.license.infra.mail;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author jeonghyeon.chang (johnmark)
 * @project PF-Auth
 * @email practice1356@gmail.com
 * @description Email Message Definition Class
 * @since 2020.03.25
 */

@Getter
@Setter
@ToString
public class EmailMessage {
    private String to;
    private String subject;
    private String message;
}
